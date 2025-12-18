use std::io::Write;
use std::error::Error;
use std::fs::File;
use std::io::{BufReader, BufWriter, Read};
use std::path::Path;
use std::process::{exit, Command};
use std::sync::{LazyLock, Mutex};
use chrono::{DateTime, Utc};
use eframe::egui;
use futures_util::StreamExt;
use reqwest::blocking::Client;
use reqwest::Response;
use semver::Version;
use serde::Deserialize;

const URL: &str = "https://api.github.com/repos/d-catte/Westward/releases/latest";
static PROGRESS: LazyLock<Mutex<f32>> = LazyLock::new(|| Mutex::new(0.0));

fn main() -> Result<(), eframe::Error> {
    let updates: Option<(Release, Status)> = check_for_updates();
    if let Some(update) = updates {
        let options = eframe::NativeOptions::default();
        eframe::run_native(
            "Westward Updater",
            options,
            Box::new(|_| Ok(Box::new(App::create(update.0, update.1)))),
            )
    } else {
        launch_westward();
        Ok(())
    }
}

fn check_for_updates() -> Option<(Release, Status)> {
    let latest_release = get_latest_release();
    if let Ok(latest_release) = latest_release {
        let latest_version_tag = latest_release.parse_tag().unwrap();
        let latest_version = get_latest_version(&latest_release);
        if latest_version.is_some() {
            if let Some(current_version) = get_current_version() {
                if current_version < latest_version_tag {
                    return Some((latest_release, Status::UpdateAvailable))
                }
            } else {
                return Some((latest_release, Status::NotInstalled))
            }
        }
    }
    None
}

fn install(latest_release: &Release, asset: &Asset) {
    let clone_url = asset.browser_download_url.clone();
    let clone_tag = latest_release.tag_name.clone();
    tokio::spawn(async move {
        if let Err(e) = download_latest_westward(&*clone_url).await {
            eprintln!("Download failed: {}", e);
        } else {
            write_new_version(&clone_tag);
            launch_westward()
        }
    });
}

fn write_new_version(tag: &String) {
    let current_version_file = File::create("version");
    if let Ok(file) = current_version_file {
        let mut writer = BufWriter::new(file);
        write!(writer, "{}", tag).unwrap();
        writer.flush().unwrap();
    }
}

fn get_current_version() -> Option<Version> {
    let current_version_file = File::open("version");
    if let Ok(file) = current_version_file {
        let mut reader = BufReader::new(file);
        let mut contents = String::new();
        reader.read_to_string(&mut contents).expect("Failed to read file");
        Some(Version::parse(&contents).unwrap())
    } else {
        None
    }
}

fn get_latest_release() -> Result<Release, Box<dyn Error>> {
    let client = Client::new();

   let output: Release = client
       .get(URL)
       .header("User-Agent", "westward-updater")
       .send()?
       .error_for_status()?
       .json()?;
    Ok(output)
}

/// Westward release asset names are as follows:
/// Universal Jar: westward-{tag}.jar
/// Windows Native: westward-windows-{tag}.exe
/// Linux Native: westward-linux-{tag}
/// MacOS Intel Native: westward-macos-i-{tag}
/// MacOS Arm Native: westward-macos-a-{tag}
fn get_latest_version(release: &Release) -> Option<Asset> {
    let search_term = if cfg!(target_os = "windows") {
        "westward-windows"
    } else if cfg!(target_os = "macos") {
        if cfg!(target_arch = "arm") || cfg!(target_arch = "aarch64") {
            "westward-macos-a"
        } else {
            "westward-macos-i"
        }
    } else {
        "westward-linux"
    };
    for asset in release.assets.iter() {
        if asset.name.starts_with(search_term) {
            return Some(asset.clone());
        }
    }
    None
}

pub async fn download_latest_westward(url: &str) -> Result<(), Box<dyn Error>> {
    let extension_type = if cfg!(target_os = "windows") {
        ".exe"
    } else {
        ""
    };

    let path = if extension_type.is_empty() {
        "westward".to_string()
    } else {
        format!("westward{}", extension_type)
    };

    let client = reqwest::Client::new();
    let response: Response = client
        .get(url)
        .header("User-Agent", "westward-updater")
        .send()
        .await?
        .error_for_status()?;

    let total_size = response.content_length().unwrap_or(0);
    let mut downloaded: u64 = 0;

    let mut file = File::create(Path::new(&path))?;
    let mut stream = response.bytes_stream();

    while let Some(chunk) = stream.next().await {
        let chunk = chunk?;
        file.write_all(&chunk)?;
        downloaded += chunk.len() as u64;

        if total_size > 0 {
            let progress = downloaded as f32 / total_size as f32;
            *PROGRESS.lock().unwrap() = progress.min(1.0);
        }
    }

    // Ensure we end at exactly 100%
    *PROGRESS.lock().unwrap() = 1.0;

    Ok(())
}

fn launch_westward() {
    let executable = if cfg!(target_os = "windows") {
        "westward.exe"
    } else {
        "./westward"
    };

    // Launch Westward
    Command::new(executable)
        .spawn()
        .expect("Failed to launch westward");

    // Exit the updater
    exit(0);
}

#[derive(Debug, Deserialize)]
struct Release {
    tag_name: String,
    published_at: String,
    assets: Vec<Asset>,
}

impl Release {
    pub fn parse_tag(&self) -> Result<Version, semver::Error> {
        Version::parse(&self.tag_name)
    }
}

enum Status {
    NotInstalled,
    UpdateAvailable,
    Downloading,
    FailedToDownload,
}

#[derive(Debug, Deserialize)]
#[derive(Clone)]
struct Asset {
    name: String,
    browser_download_url: String,
}

struct App {
    status: Status,
    release: Release,
    error: String,
}

impl App {
    pub fn create(release: Release, status: Status) -> Self {
        Self {status, release, error: String::new() }
    }
}

impl eframe::App for App {
    fn update(&mut self, ctx: &egui::Context, _: &mut eframe::Frame) {
        egui::CentralPanel::default().show(ctx, |ui| {
            match &self.status {
                Status::NotInstalled => {
                    ui.label("Welcome to the Westward Installer");

                    if ui.button(format!("Install Westward {}", &self.release.tag_name)).clicked() {
                        let asset = get_latest_version(&self.release);
                        if let Some(asset) = asset {
                            self.status = Status::Downloading;
                            install(&self.release, &asset)
                        } else {
                            self.error = "No asset found".to_string();
                            self.status = Status::FailedToDownload;
                        }
                    }
                }
                Status::UpdateAvailable => {
                    ui.label("Welcome to the Westward Updater");
                    ui.label(format!("Update Available: {} from {}", &self.release.tag_name, format_github_time(&self.release.published_at).unwrap()));

                    if ui.button(format!("Update to Westward {}", &self.release.tag_name)).clicked() {
                        let asset = get_latest_version(&self.release);
                        if let Some(asset) = asset {
                            self.status = Status::Downloading;
                            install(&self.release, &asset)
                        } else {
                            self.error = "No asset found".to_string();
                            self.status = Status::FailedToDownload;
                        }
                    }
                }
                Status::Downloading => {
                    ui.label("Downloading...");
                    ui.add(egui::ProgressBar::new(*PROGRESS.lock().unwrap()));
                }
                Status::FailedToDownload => {
                    ui.label("Failed to download...");
                    ui.label(format!("{}", &self.error));
                    if ui.button("Attempt Westward Launch").clicked() {
                        launch_westward();
                    }
                    if ui.button("Exit").clicked() {
                        exit(0);
                    }
                }
            }
        });
    }
}

fn format_github_time(input: &str) -> Result<String, chrono::ParseError> {
    let dt: DateTime<Utc> = input.parse()?;
    Ok(dt.format("%B %d, %Y at %I:%M %p UTC").to_string())
}