package dev.ymntube.api.sample;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SampleVideoServiceTest {
    @Test
    void fetchesOnlyFixedLicensedUrlAndReturnsBody() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<InputStream> response = response(
                200, "video/mp4", "4", new byte[]{1, 2, 3, 4});
        stubResponse(client, response);
        SampleVideoService service = new SampleVideoService(client, Duration.ofSeconds(1), 8);

        assertArrayEquals(new byte[]{1, 2, 3, 4}, service.downloadSample());

        verify(client).send(
                org.mockito.ArgumentMatchers.argThat(request ->
                        request.uri().equals(SampleVideoService.SAMPLE_VIDEO_URI)),
                any());
    }

    @Test
    void rejectsContentLengthLargerThanConfiguredLimit() throws Exception {
        HttpClient client = mock(HttpClient.class);
        stubResponse(client, response(200, "video/mp4", "5", new byte[]{1}));
        SampleVideoService service = new SampleVideoService(client, Duration.ofSeconds(1), 4);

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                service::downloadSample);

        assertEquals(413, exception.getStatusCode().value());
    }

    @Test
    void rejectsBodyThatExceedsLimitWithoutContentLength() throws Exception {
        HttpClient client = mock(HttpClient.class);
        stubResponse(client, response(200, "video/mp4", null, new byte[]{1, 2, 3, 4, 5}));
        SampleVideoService service = new SampleVideoService(client, Duration.ofSeconds(1), 4);

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                service::downloadSample);

        assertEquals(413, exception.getStatusCode().value());
    }

    @Test
    void rejectsNonSuccessUpstreamResponse() throws Exception {
        HttpClient client = mock(HttpClient.class);
        stubResponse(client, response(302, "video/mp4", null, new byte[0]));
        SampleVideoService service = new SampleVideoService(client, Duration.ofSeconds(1), 4);

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                service::downloadSample);

        assertEquals(502, exception.getStatusCode().value());
    }

    @Test
    void sampleClientDoesNotFollowRedirects() {
        HttpClient client = new SampleVideoConfiguration()
                .sampleVideoHttpClient(Duration.ofSeconds(1));

        assertEquals(HttpClient.Redirect.NEVER, client.followRedirects());
    }

    private void stubResponse(
            HttpClient client, HttpResponse<InputStream> response) throws Exception {
        when(client.<InputStream>send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<InputStream>>any()))
                .thenReturn(response);
    }

    private HttpResponse<InputStream> response(
            int status, String contentType, String contentLength, byte[] body) {
        @SuppressWarnings("unchecked")
        HttpResponse<InputStream> response = mock(HttpResponse.class);
        HttpHeaders headers = HttpHeaders.of(
                contentLength == null
                        ? Map.of("Content-Type", List.of(contentType))
                        : Map.of(
                                "Content-Type", List.of(contentType),
                                "Content-Length", List.of(contentLength)),
                (name, value) -> true);
        when(response.statusCode()).thenReturn(status);
        when(response.headers()).thenReturn(headers);
        when(response.body()).thenReturn(new ByteArrayInputStream(body));
        return response;
    }
}
