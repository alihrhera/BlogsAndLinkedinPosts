# Video Compression Implementation Details

This document explains the technical implementation of video compression in the KnowledgeSharing app, covering the transition and integration of multiple compression engines.

## Overview

The application supports two primary video compression engines to balance quality, speed, and device compatibility:
1. **FFmpeg**: A powerful, command-line based engine for fine-grained control.
2. **LightCompressor**: A high-level library optimized for Android's hardware codecs.

## 1. FFmpeg Implementation (`FfmpegCompressor.kt`)

The FFmpeg implementation uses the `ffmpeg-kit` library to execute compression commands.

### Compression Parameters
The current configuration focuses on high quality with a reasonable file size:
- **Codec**: `mpeg4` (for broad compatibility).
- **CRF (Constant Rate Factor)**: `20` (Lower values mean higher quality; 20 is high quality).
- **Preset**: `slow` (Provides better compression efficiency).
- **Audio**: `aac` codec at `128k` bitrate.
- **Flags**: `+faststart` (Enables faster playback start for web/streaming).

### Progress Tracking
Progress is calculated by parsing FFmpeg logs for the `time=` field and comparing it against the total video duration.

---

## 2. LightCompressor Implementation (`VideoCompressor.kt`)

`LightCompressor` is integrated as a more modern, hardware-accelerated alternative.

### Configuration
- **Quality**: `MEDIUM`
- **Resolution**: Resized to `720x1280` (HD) while maintaining aspect ratio.
- **Bitrate**: Fixed at `2 Mbps` for consistent output size.
- **Storage**: Uses a custom `StorageConfiguration` to manage compressed files in the application's cache directory, organized by item ID.

---

## 3. Workflow & Background Sync

The compression process is integrated into the app's background synchronization workflow:
- **`CompressWorker`**: A `WorkManager` worker that handles the compression task off the main thread.
- **`MediaCompressorFactory`**: A factory class that provides the appropriate compressor implementation based on the `CompressType` (FFmpeg or LightCompressor).
- **URI Handling**: Supports both file paths and Android `content://` URIs by copying content to a temporary cache before processing.

---

## Demonstration

The following video demonstrates the compression process and the resulting quality:

![Video](res/video_compression.mp4)

> [!NOTE]
> The compression process significantly reduces file size while maintaining visual clarity, ensuring faster uploads and better user experience.
