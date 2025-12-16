package io.github.onu_eccs1621_sp2025.westward.utils;

import io.github.onu_eccs1621_sp2025.westward.TrailApplication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;

public final class NativeUtils {
    private static boolean isNative;
    private static FileSystem nativeResources;

    /**
     * If the current environment is in C/C++
     * @return If the code has been compiled to a Graal Native Image
     */
    public static boolean isNative() {
        return isNative;
    }

    /**
     * Gets a resource from the native binary, or using the
     * default Java method if not a native binary.
     * @param path Path in the resources folder
     * @return Path to the resource
     */
    public static Path getResource(String path) {
        if (isNative) {
            return nativeResources.getPath("defaultData/" + path);
        } else {
            URL resourceURL = TrailApplication.class.getResource("defaultData/" + path);
            if (resourceURL == null) {
                resourceURL = TrailApplication.class.getResource("/defaultData/" + path);
            }
            try {
                return Paths.get(resourceURL.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Safely copies a resource from the executable (native) or jar (non-native) to
     * the destination.
     * @param resourcePath The path to the resource in the resources folder
     * @param destination The destination path
     */
    public static void moveResource(String resourcePath, Path destination) {
        if (isNative) {
            try {
                Files.copy(nativeResources.getPath("defaultData/" + resourcePath), destination);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                Files.copy(TrailApplication.JAR_DATA_PATH.resolve(resourcePath), destination);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Loads the native file system
     */
    public static void init() {
        // Register the GraalVM Native Image file system
        // This hack registers NativeImageResourceFileSystemProvider & NativeImageResourceFileSystem when ran via Native Image
        // It allows lookups using "resource:/" URIs which means calls like Path.of(URI) will not fail.
        try {
            FileSystem filesystem = FileSystems.newFileSystem(URI.create("resource:/"), Collections.singletonMap("create", "true"));
            DebugLogger.info("Created {} filesystem", filesystem.getClass().getSimpleName());
            isNative = true;
            nativeResources = filesystem;
            setupAudio();
        } catch(Exception e) {
            // This will always happen outside a native image; there no such thing as a "resource:/" file system outside of native image
            DebugLogger.info("Not creating resource file system as not a native image.");
        }
    }

    private static void setupAudio() {
        System.setProperty("javax.sound.sampled.Clip", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.Port", "com.sun.media.sound.PortMixerProvider");
        System.setProperty("javax.sound.sampled.SourceDataLine", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.TargetDataLine", "com.sun.media.sound.DirectAudioDeviceProvider");
    }

    /**
     * Closes the resources file system on native builds
     */
    public static void cleanup() {
        if (isNative) {
            try {
                nativeResources.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
