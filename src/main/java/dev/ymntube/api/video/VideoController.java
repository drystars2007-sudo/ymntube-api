package dev.ymntube.api.video;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/search")
    public List<VideoMetadata> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) throws IOException, ExtractionException {
        if (q.isBlank() || q.length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "q must contain between 1 and 120 characters.");
        }
        if (limit < 1 || limit > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "limit must be between 1 and 20.");
        }
        return videoService.search(q.trim(), limit);
    }

    @GetMapping("/videos/{id}")
    public VideoMetadata getVideo(@PathVariable String id)
            throws IOException, ExtractionException {
        try {
            return videoService.getVideo(id);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
