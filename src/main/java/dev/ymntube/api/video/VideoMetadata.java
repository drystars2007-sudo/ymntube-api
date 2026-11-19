package dev.ymntube.api.video;

public record VideoMetadata(
        String id,
        String title,
        String uploader,
        String thumbnailUrl,
        String watchUrl,
        long durationSeconds,
        long viewCount) {
}
