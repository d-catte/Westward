package io.github.onu_eccs1621_sp2025.westward.utils.sound;

import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A simple WAV file loader for PCM 16‑bit mono 44100Hz files,
 * that loads into an OpenAL buffer.
 * @author Dylan Catte
 * @since 1.0.0 Beta 1
 * @version 1.0.0
 */
public final class SimpleWavLoader {

    /**
     * Loads a WAV file (16‑bit mono, 44100Hz) into an OpenAL buffer.
     *
     * @param wavFile the WAV file to load
     * @return the OpenAL buffer id, or 0 on failure
     */
    public static int loadWavToOpenALBuffer(File wavFile) {
        try (FileInputStream fis = new FileInputStream(wavFile)) {
            // Read entire file into ByteBuffer
            long fileLength = wavFile.length();
            ByteBuffer fileBuffer = MemoryUtil.memAlloc((int) fileLength);
            byte[] temp = new byte[8192];
            int read;
            while ((read = fis.read(temp)) != -1) {
                fileBuffer.put(temp, 0, read);
            }
            fileBuffer.flip();
            fileBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // Parse header (RIFF/WAVE) sufficiently
            // Skip "RIFF" 4 bytes
            if (fileBuffer.getInt() != 0x46464952) { // "RIFF" in little‑endian
                throw new IOException("Not a RIFF file.");
            }
            fileBuffer.getInt(); // skip overall size
            if (fileBuffer.getInt() != 0x45564157) { // "WAVE"
                throw new IOException("Not a WAVE file.");
            }

            // Find "fmt " chunk
            int fmtChunkID = fileBuffer.getInt();
            while (fmtChunkID != 0x20746D66) {
                int chunkSize = fileBuffer.getInt();
                fileBuffer.position(fileBuffer.position() + chunkSize);
                fmtChunkID = fileBuffer.getInt();
            }
            int fmtChunkSize = fileBuffer.getInt();
            int audioFormat = fileBuffer.getShort();
            int numChannels = fileBuffer.getShort();
            int sampleRate = fileBuffer.getInt();
            fileBuffer.getInt(); // byte rate
            fileBuffer.getShort(); // block align
            int bitsPerSample = fileBuffer.getShort();
            // skip any extra bytes in fmt chunk
            fileBuffer.position(fileBuffer.position() + (fmtChunkSize - 16));

            if (audioFormat != 1) {
                throw new IOException("Unsupported WAV encoding: non‑PCM.");
            }

            if (numChannels != 1) {
                throw new IOException("Unsupported WAV channel count: " + numChannels);
            }

            if (sampleRate != 44100) {
                throw new IOException("Unsupported WAV sample rate: expected 44100Hz, got " + sampleRate);
            }

            if (bitsPerSample != 16) {
                throw new IOException("Unsupported WAV bit depth: expected 16‑bit, got " + bitsPerSample);
            }

            // Find "data" chunk
            int dataChunkID = fileBuffer.getInt();
            while (dataChunkID != 0x61746164) { // "data"
                int chunkSize = fileBuffer.getInt();
                fileBuffer.position(fileBuffer.position() + chunkSize);
                dataChunkID = fileBuffer.getInt();
            }
            int dataSize = fileBuffer.getInt();

            // Prepare the data buffer
            ByteBuffer pcmData = MemoryUtil.memAlloc(dataSize);
            int oldLimit = fileBuffer.limit();
            int newLimit = fileBuffer.position() + dataSize;
            if (newLimit > oldLimit) {
                throw new IOException("WAV data chunk size mismatch.");
            }
            for (int i = 0; i < dataSize; i++) {
                pcmData.put(fileBuffer.get());
            }
            pcmData.flip();

            // Free buffers
            MemoryUtil.memFree(fileBuffer);

            // Clear errors
            AL10.alGetError();

            // Create buffer
            int alBuffer = AL10.alGenBuffers();
            AL10.alBufferData(alBuffer, AL10.AL_FORMAT_MONO16, pcmData, sampleRate);

            // Free buffer to not leak memory
            MemoryUtil.memFree(pcmData);

            // Check for errors
            int err = AL10.alGetError();
            if (err != AL10.AL_NO_ERROR) {
                DebugLogger.error("OpenAL error after loading WAV: " + err);
                AL10.alSourcei(alBuffer, AL10.AL_BUFFER, 0);
                AL10.alDeleteBuffers(alBuffer);
                return 0;
            }

            return alBuffer;

        } catch (IOException e) {
            DebugLogger.error("Failed to load WAV file: " + e.getMessage());
            return 0;
        }
    }
}
