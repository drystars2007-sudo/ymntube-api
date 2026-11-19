# YmnTube API

A Java 21 / Spring Boot REST API that uses the published [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) library for public YouTube search and video metadata.

The extractor is included as the pinned `v0.25.2` JitPack dependency in `pom.xml`; its source and the unrelated NewPipe Android app are not vendored here. This API returns display metadata only. It does not expose media stream or download URLs. Use YouTube's official watch page or embed player for playback.

## Run locally

Requirements: a JDK 21 installation and internet access on first run. The Maven bootstrap wrapper downloads Maven 3.9.9 to the user's Maven cache.

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux (requires `curl` or `wget` and `unzip`):

```sh
bash ./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`.

## API

Search YouTube and return up to 20 results:

```sh
curl "http://localhost:8080/api/v1/search?q=lofi%20music&limit=5"
```

Fetch metadata for a YouTube video ID (11 characters):

```sh
curl "http://localhost:8080/api/v1/videos/dQw4w9WgXcQ"
```

Responses contain `id`, `title`, `uploader`, `thumbnailUrl`, `watchUrl`, `durationSeconds`, and `viewCount`. Invalid input returns HTTP 400; upstream extraction failures return HTTP 502.

## Test and build

```sh
bash ./mvnw -B verify
```

On Windows, use `.\mvnw.cmd -B verify`.

## Container

```sh
docker build -t ymntube-api .
docker run --rm -p 8080:8080 ymntube-api
```

## Deployment and operations

Build the executable JAR with `bash ./mvnw -B package` and run it using `java -jar target/ymntube-api-0.1.0-SNAPSHOT.jar`, or deploy the Docker image to a Java 21 container host. Put a public deployment behind an API gateway or reverse proxy that provides rate limiting and access controls appropriate to your use; this starter intentionally does not include authentication or an in-process rate limiter. No API credentials or secrets are required.

Extractor requests use the JDK HTTP client with configurable 10-second connect and 20-second request timeouts. Set `EXTRACTOR_HTTP_CONNECT_TIMEOUT` or `EXTRACTOR_HTTP_REQUEST_TIMEOUT` to Spring duration values (for example, `5s` or `30s`). The service also honors Spring's standard `SERVER_PORT` setting. The extractor and supported upstream services may change independently; keep the pinned dependency updated and review its current terms and supported-service status before deployment.

## License and attribution

This project is distributed under the GNU GPL version 3 or (at your option) any later version, consistent with NewPipe Extractor's license. See [the full GPL-3.0-or-later text](https://www.gnu.org/licenses/gpl-3.0.html). The API uses NewPipe Extractor (GPL-3.0-or-later) and Spring Boot (Apache-2.0) as published dependencies; consult their respective project pages for notices and source. NewPipe Extractor is a separate project by NewPipe e.V. and contributors; this API is not affiliated with or endorsed by NewPipe or YouTube. YouTube is a trademark of Google LLC.
