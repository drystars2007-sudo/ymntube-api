package dev.ymntube.api.video;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VideoController.class)
class VideoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoService videoService;

    @Test
    void searchReturnsOnlyDisplayMetadata() throws Exception {
        when(videoService.search("cats", 5)).thenReturn(List.of(
                new VideoMetadata(
                        "abcdefghijk",
                        "Cat video",
                        "Example channel",
                        "https://i.ytimg.com/vi/abcdefghijk/hqdefault.jpg",
                        "https://www.youtube.com/watch?v=abcdefghijk",
                        42,
                        1000)));

        mockMvc.perform(get("/api/v1/search").param("q", "cats").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Cat video"))
                .andExpect(jsonPath("$[0].uploader").value("Example channel"))
                .andExpect(jsonPath("$[0].watchUrl")
                        .value("https://www.youtube.com/watch?v=abcdefghijk"))
                .andExpect(jsonPath("$[0].streamUrl").doesNotExist());

        verify(videoService).search("cats", 5);
    }

    @Test
    void rejectsBlankQueries() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "q must contain between 1 and 120 characters."));
    }

    @Test
    void rejectsOutOfRangeLimits() throws Exception {
        mockMvc.perform(get("/api/v1/search").param("q", "cats").param("limit", "21"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("limit must be between 1 and 20."));
    }

    @Test
    void returnsMetadataForVideoId() throws Exception {
        when(videoService.getVideo("abcdefghijk")).thenReturn(
                new VideoMetadata(
                        "abcdefghijk",
                        "Cat video",
                        "Example channel",
                        "https://i.ytimg.com/vi/abcdefghijk/hqdefault.jpg",
                        "https://www.youtube.com/watch?v=abcdefghijk",
                        42,
                        1000));

        mockMvc.perform(get("/api/v1/videos/abcdefghijk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abcdefghijk"))
                .andExpect(jsonPath("$.watchUrl")
                        .value("https://www.youtube.com/watch?v=abcdefghijk"))
                .andExpect(jsonPath("$.streamUrl").doesNotExist());
    }
}
