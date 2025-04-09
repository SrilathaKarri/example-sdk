package sdk.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sdk.base.errors.EhrApiError;

import static sdk.base.errors.EhrApiError.mapHttpStatusToErrorType;

/**
 * Configuration class for setting up {@link WebClient} to interact with external APIs.
 * <p>
 * This class initializes a {@link WebClient} bean with common headers and error handling mechanisms.
 * It reads configuration properties like API key, base URL, and optional HPRID authentication from
 * the application properties.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Base URL Configuration:</strong> Sets the API base URL for all outgoing requests.</li>
 *   <li><strong>Authorization Header:</strong> Adds an Authorization header using the provided API key.</li>
 *   <li><strong>Optional HPRID Auth:</strong> Adds an additional header if HPRID auth is configured.</li>
 *   <li><strong>Error Handling:</strong> Transforms HTTP error responses into {@link EhrApiError} for better error management.</li>
 * </ul>
 */
@Configuration
public class WebClientConfig {

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${facility.hpridAuth:#{null}}")
    private String hpridAuth;

    /**
     * Creates a {@link WebClient} bean with base URL, common headers, and error handling filters.
     * <p>
     * This client automatically includes authorization and content-type headers for each request
     * and handles API errors gracefully by mapping them to custom error types.
     * </p>
     *
     * @param builder the {@link WebClient.Builder} instance for building the client
     * @return a configured {@link WebClient} instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(apiUrl)
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addCommonHeaders());
                    exchangeFilterFunctions.add(handleErrors());
                })
                .build();
    }

    /**
     * Adds common headers (e.g., Authorization, Content-Type) to all outgoing API requests.
     * <p>
     * If the optional {@code hpridAuth} value is provided, an additional header "x-hprid-auth" is added.
     * </p>
     *
     * @return an {@link ExchangeFilterFunction} that modifies request headers
     */
    private ExchangeFilterFunction addCommonHeaders() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(clientRequest)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json");

            if (hpridAuth != null && !hpridAuth.trim().isEmpty()) {
                requestBuilder.header("x-hprid-auth", hpridAuth);
            }

            return Mono.just(requestBuilder.build());
        });
    }

    /**
     * Handles API errors by inspecting the response status code and mapping error responses to {@link EhrApiError}.
     * <p>
     * This helps in transforming HTTP errors into a consistent error format for easier debugging and logging.
     * </p>
     *
     * @return an {@link ExchangeFilterFunction} that handles API response errors
     */
    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(
                                new EhrApiError("Error: " + errorBody,
                                        mapHttpStatusToErrorType(clientResponse.statusCode()))
                        ));
            }
            return Mono.just(clientResponse);
        });
    }
}
