package dev.ymntube.api.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class SampleVideoService {
    static final URI SAMPLE_VIDEO_URI = URI.create(
            "https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4");
    private static final int MAX_SAMPLE_BYTES = 50 * 1024 * 1024;
    private static final String UPSTREAM_ERROR = "The licensed sample video could not be retrieved.";

    private final HttpClient httpClient;
    private final Duration requestTimeout;
    private final int maxBytes;

    public SampleVideoService(
            HttpClient httpClient,
            @Value("${sample-video.http.request-timeout:30s}") Duration requestTimeout,
            @Value("${sample-video.max-bytes:52428800}") int maxBytes) {
        if (maxBytes < 1 || maxBytes > MAX_SAMPLE_BYTES) {
            throw new IllegalArgumentException(
                    "sample-video.max-bytes must be between 1 and " + MAX_SAMPLE_BYTES + ".");
        }
        this.httpClient = httpClient;
        this.requestTimeout = requestTimeout;
        this.maxBytes = maxBytes;
    }

    public byte[] downloadSample() {
        HttpRequest request = HttpRequest.newBuilder(SAMPLE_VIDEO_URI)
                .timeout(requestTimeout)
                .header("Accept", "video/mp4, application/octet-stream")
                .header("Accept-Encoding", "identity")
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() != 200) {
                    throw upstreamFailure();
                }
                if (!isMp4(response)) {
                    throw upstreamFailure();
                }

                long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
                if (contentLength > maxBytes) {
                    throw tooLarge();
                }

                byte[] video = body.readNBytes(maxBytes + 1);
                if (video.length == 0) {
                    throw upstreamFailure();
                }
                if (video.length > maxBytes) {
                    throw tooLarge();
                }
                return video;
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw upstreamFailure();
        } catch (IOException | NumberFormatException exception) {
            throw upstreamFailure();
        }
    }

    private boolean isMp4(HttpResponse<?> response) {
        return response.headers().firstValue("Content-Type")
                .map(value -> value.toLowerCase().startsWith("video/mp4")
                        || value.toLowerCase().startsWith("application/octet-stream"))
                .orElse(false);
    }

    private ResponseStatusException tooLarge() {
        return new ResponseStatusException(
                HttpStatus.PAYLOAD_TOO_LARGE, "The licensed sample video exceeds the download size limit.");
    }

    private ResponseStatusException upstreamFailure() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, UPSTREAM_ERROR);
    }
}
