package dev.ymntube.api.video;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class VideoService {
    private static final Pattern VIDEO_ID = Pattern.compile("[A-Za-z0-9_-]{11}");

    public List<VideoMetadata> search(String query, int limit) throws IOException, ExtractionException {
        StreamingService youtube = NewPipe.getService("YouTube");
        SearchExtractor extractor = youtube.getSearchExtractor(query);
        extractor.fetchPage();

        return extractor.getInitialPage().getItems().stream()
                .filter(StreamInfoItem.class::isInstance)
                .map(StreamInfoItem.class::cast)
                .map(this::toVideoMetadata)
                .filter(video -> video.id() != null)
                .limit(limit)
                .toList();
    }

    public VideoMetadata getVideo(String id) throws IOException, ExtractionException {
        if (!VIDEO_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Video ID must contain exactly 11 valid characters.");
        }

        String watchUrl = "https://www.youtube.com/watch?v=" + id;
        StreamInfo info = StreamInfo.getInfo(NewPipe.getService("YouTube"), watchUrl);
        return new VideoMetadata(
                id,
                info.getName(),
                info.getUploaderName(),
                firstThumbnail(info.getThumbnails().stream().map(image -> image.getUrl()).toList()),
                watchUrl,
                info.getDuration(),
                info.getViewCount());
    }

    private VideoMetadata toVideoMetadata(StreamInfoItem item) {
        String id = extractVideoId(item.getUrl());
        String watchUrl = id == null ? null : "https://www.youtube.com/watch?v=" + id;
        return new VideoMetadata(
                id,
                item.getName(),
                item.getUploaderName(),
                firstThumbnail(item.getThumbnails().stream().map(image -> image.getUrl()).toList()),
                watchUrl,
                item.getDuration(),
                item.getViewCount());
    }

    private String firstThumbnail(List<String> thumbnails) {
        return thumbnails.stream()
                .filter(url -> url.startsWith("https://"))
                .findFirst()
                .orElse(null);
    }

    private String extractVideoId(String videoUrl) {
        try {
            URI uri = URI.create(videoUrl);
            String query = uri.getRawQuery();
            if (query == null) {
                return null;
            }
            for (String parameter : query.split("&")) {
                String[] pair = parameter.split("=", 2);
                if (pair.length == 2 && pair[0].equals("v")) {
                    String id = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    return VIDEO_ID.matcher(id).matches() ? id : null;
                }
            }
        } catch (IllegalArgumentException exception) {
            return null;
        }
        return null;
    }
}
