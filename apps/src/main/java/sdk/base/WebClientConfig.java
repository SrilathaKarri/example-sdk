package sdk.base;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .clientConnector(new ReactorClientHttpConnector(
                                   HttpClient.create()
                                           .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                           .doOnConnected(conn -> conn
                                               .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                                               .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)))
                                           .responseTimeout(Duration.ofSeconds(10))
                ))
                .build();
    }
}