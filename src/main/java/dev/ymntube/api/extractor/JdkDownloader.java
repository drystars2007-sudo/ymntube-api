package dev.ymntube.api.extractor;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdkDownloader extends Downloader {
    private final HttpClient client;
    private final Duration requestTimeout;

    public JdkDownloader(Duration connectTimeout, Duration requestTimeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.requestTimeout = requestTimeout;
    }

    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(request.url()))
                .timeout(requestTimeout)
                .header("User-Agent", "YmnTube-API/0.1");

        request.headers().forEach((name, values) -> values.forEach(value -> {
            if (!name.equalsIgnoreCase("host")
                    && !name.equalsIgnoreCase("content-length")
                    && !name.equalsIgnoreCase("connection")) {
                builder.setHeader(name, value);
            }
        }));

        byte[] body = request.dataToSend();
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);
        builder.method(request.httpMethod(), publisher);

        try {
            HttpResponse<String> response = client.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Map<String, List<String>> headers = new LinkedHashMap<>(response.headers().map());
            return new Response(
                    response.statusCode(),
                    "",
                    headers,
                    response.body(),
                    response.uri().toString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while requesting extractor data", exception);
        }
    }
}
