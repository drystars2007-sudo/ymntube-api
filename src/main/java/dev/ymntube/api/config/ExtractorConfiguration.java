package dev.ymntube.api.config;

import dev.ymntube.api.extractor.JdkDownloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ExtractorConfiguration {
    @Bean
    Downloader extractorDownloader(
            @Value("${extractor.http.connect-timeout:10s}") Duration connectTimeout,
            @Value("${extractor.http.request-timeout:20s}") Duration requestTimeout) {
        Downloader downloader = new JdkDownloader(connectTimeout, requestTimeout);
        NewPipe.init(downloader);
        return downloader;
    }
}
