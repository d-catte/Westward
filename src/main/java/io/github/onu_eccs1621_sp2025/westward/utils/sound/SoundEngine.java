package io.github.onu_eccs1621_sp2025.westward.utils.sound;

import io.github.onu_eccs1621_sp2025.westward.utils.Config;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Manages the audio playback for the game using OpenAL
 * @author Dylan Catte
 * @since 1.0.0 Beta 1
 * @version 2.0
 */
public final class SoundEngine {

    /**
     * Music Volume from 0-100
     */
    public static final int[] MUSIC_VOLUME = { Config.getConfig().getMusicVolume() };
    /**
     * Sound Effect Volume from 0-100
     */
    public static final int[] SFX_VOLUME   = { Config.getConfig().getSfxVolume() };

    private static long device;
    private static long context;

    // For music
    private static int musicBuffer = 0;
    private static int musicSource = 0;
    private static String lastMusicId;

    // For SFX
    private static int sfxBuffer = 0;
    private static int sfxSource = 0;
    private static String lastSfxId;

    // Force skip if sound system failed
    private static boolean skipSounds = false;

    /**
     * Initialize OpenAL device + context. Call once at game startup.
     */
    public static void init() {
        try {
            device = ALC10.alcOpenDevice((ByteBuffer) null);
            if (device == NULL) {
                throw new IllegalStateException("Failed to open the default OpenAL device.");
            }
            var attribs = (IntBuffer) null;
            context = ALC10.alcCreateContext(device, attribs);
            ALC10.alcMakeContextCurrent(context);
            AL.createCapabilities(ALC.createCapabilities(device));
            DebugLogger.info("OpenAL initialized.");
        } catch (Exception e) {
            DebugLogger.warn("Failed to initialize OpenAL: " + e.getMessage());
            skipSounds = true;
        }
    }

    /**
     * Clean up OpenAL resources. Calls at shutdown.
     */
    public static void destroy() {
        if (!skipSounds) {
            AL10.alSourceStop(musicSource);
            AL10.alSourcei(musicSource, AL10.AL_BUFFER, 0);
            AL10.alSourceStop(sfxSource);
            AL10.alSourcei(sfxSource, AL10.AL_BUFFER, 0);
            AL10.alDeleteSources(musicSource);
            AL10.alDeleteSources(sfxSource);
            AL10.alDeleteBuffers(musicBuffer);
            AL10.alDeleteBuffers(sfxBuffer);

            ALC10.alcDestroyContext(context);
            ALC10.alcCloseDevice(device);
        }
    }

    /**
     * Loads a song into the SoundEngine
     * @param songId The name/id of the song
     * @param loop If the song should loop
     */
    public static void loadSong(final String songId, final boolean loop) {
        if (skipSounds) {
            return;
        }
        if (songId.equals(lastMusicId)) {
            // restart
            playMusic();
        } else {
            lastMusicId = songId;
            Path path = (Path) Registry.getAsset(Registry.AssetType.AUDIO, songId);
            process(path, loop, true);
        }
    }

    /**
     * Loads a sound effect into the SoundEngine
     * @param sfxId The name/id of the sfx
     */
    public static void loadSFX(final String sfxId) {
        if (skipSounds) {
            return;
        }
        if (sfxId.equals(lastSfxId)) {
            playSFX();
        } else {
            lastSfxId = sfxId;
            Path path = (Path) Registry.getAsset(Registry.AssetType.SFX, sfxId);
            process(path, false, false);
        }
    }

    /**
     * Loads a random song into the SoundEngine
     * @param loop If the song should loop
     */
    public static void loadRandomSong(final boolean loop) {
        if (!skipSounds) {
            final Path song = (Path) Registry.randomAsset(Registry.AssetType.AUDIO);
            process(song, loop, true);
        }
    }

    private static void process(final Path filePath, boolean shouldLoop, boolean music) {
        // Reset previous
        if (music) {
            stopMusic();
            if (musicBuffer != 0) {
                AL10.alDeleteBuffers(musicBuffer);
                AL10.alDeleteSources(musicSource);
            }
        } else {
            if (sfxBuffer != 0) {
                AL10.alDeleteBuffers(sfxBuffer);
                AL10.alDeleteSources(sfxSource);
            }
        }

        // Load PCM data from WAV file
        int bufferId = SimpleWavLoader.loadWavToOpenALBuffer(filePath.toFile());
        int source = AL10.alGenSources();
        AL10.alSourcei(source, AL10.AL_BUFFER, bufferId);

        if (shouldLoop) {
            AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_TRUE);
        }

        if (music) {
            musicBuffer = bufferId;
            musicSource = source;
            updateMusicVolume();
            playMusic();
        } else {
            sfxBuffer = bufferId;
            sfxSource = source;
            updateSfxVolume();
            playSFX();
        }
    }

    private static void playMusic() {
        if (skipSounds) {
            return;
        }
        AL10.alSourceStop(musicSource);
        AL10.alSourceRewind(musicSource);
        AL10.alSourcePlay(musicSource);
    }

    /**
     * Stops the currently playing music
     */
    public static void stopMusic() {
        if (musicSource != 0) {
            AL10.alSourceStop(musicSource);
        }
    }

    private static void playSFX() {
        if (skipSounds) {
            return;
        }
        AL10.alSourceStop(sfxSource);
        AL10.alSourceRewind(sfxSource);
        AL10.alSourcePlay(sfxSource);
    }

    /**
     * Refreshes the music's volume
     */
    public static void updateMusicVolume() {
        if (musicSource != 0) {
            float gain = MUSIC_VOLUME[0] / 100f;
            AL10.alSourcef(musicSource, AL10.AL_GAIN, gain);
        }
    }

    /**
     * Refreshes the sfx's volume
     */
    public static void updateSfxVolume() {
        if (sfxSource != 0) {
            float gain = SFX_VOLUME[0] / 100f;
            AL10.alSourcef(sfxSource, AL10.AL_GAIN, gain);
        }
    }

    /**
     * Invalidates cached id data
     */
    public static void invalidateCaches() {
        lastMusicId = null;
        lastSfxId   = null;
    }
}