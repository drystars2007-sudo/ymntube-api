package dev.ymntube.api.sample;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sample-video")
public class SampleVideoController {
    private static final String DOWNLOAD_FILENAME = "big-buck-bunny-sample.mp4";

    private final SampleVideoService sampleVideoService;

    public SampleVideoController(SampleVideoService sampleVideoService) {
        this.sampleVideoService = sampleVideoService;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download() {
        byte[] video = sampleVideoService.downloadSample();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .contentLength(video.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(DOWNLOAD_FILENAME)
                        .build()
                        .toString())
                .body(video);
    }
}
