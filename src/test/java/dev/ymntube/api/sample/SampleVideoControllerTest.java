package dev.ymntube.api.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleVideoController.class)
class SampleVideoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SampleVideoService sampleVideoService;

    @Test
    void downloadsLicensedSampleAsMp4Attachment() throws Exception {
        byte[] video = {0, 1, 2, 3};
        when(sampleVideoService.downloadSample()).thenReturn(video);

        mockMvc.perform(get("/api/v1/sample-video/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("video/mp4"))
                .andExpect(content().bytes(video))
                .andExpect(header().longValue("Content-Length", video.length))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"big-buck-bunny-sample.mp4\""));
    }
}
