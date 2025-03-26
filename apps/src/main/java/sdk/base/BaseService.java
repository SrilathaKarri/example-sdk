package sdk.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sdk.base.errors.EhrApiError;
import sdk.base.response.ApiResponse;


import java.util.*;

import static sdk.base.errors.EhrApiError.mapHttpStatusToErrorType;

/**
 * BaseService is an abstract class that provides core functionalities for interacting with APIs using WebClient.
 * <p>
 * This service includes:
 * <ul>
 *     <li>Setting up a WebClient instance with common headers.</li>
 *     <li>Handling API errors.</li>
 *     <li>Performing GET, POST, PUT, and DELETE requests.</li>
 *     <li>Utility methods for validation.</li>
 * </ul>
 * </p>

 */
@Service
public abstract class BaseService {

    protected final ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${facility.hpridAuth}")
    private String hpridAuth;

    private WebClient webClient;

    /**

     * Constructor initializes ObjectMapper.
     *
     * @param webClientBuilder WebClient builder used to create WebClient instances.
     */
    @Autowired
    public BaseService(WebClient.Builder webClientBuilder) {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Initializes the WebClient instance with base URL and request filters.
     * <p>
     * This method is executed after dependency injection (PostConstruct).
     * It applies:
     * <ul>
     *     <li>Common headers including authorization.</li>
     *     <li>Error handling mechanisms.</li>
     * </ul>
     * </p>
     */
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .filter(addCommonHeaders())
                .filter(handleErrors())
                .baseUrl(apiUrl)
                .build();
    }

    private boolean hasHpridAuth;

    @PostConstruct
    public void checkHpridAuth() {
        this.hasHpridAuth = hpridAuth != null && !hpridAuth.trim().isEmpty();
    }

    /**
     * Adds common headers to all outgoing API requests.
     * <p>
     * Includes:
     * <ul>
     *     <li>Authorization header with API key.</li>
     *     <li>Content-Type as JSON.</li>
     *     <li>Optional HPRID authentication header.</li>
     * </ul>
     * </p>
     *
     * @return ExchangeFilterFunction for modifying request headers.
     */
    private ExchangeFilterFunction addCommonHeaders() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(clientRequest)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json");

            if (hasHpridAuth) {
                requestBuilder.header("x-hprid-auth", hpridAuth);
            }

            return Mono.just(requestBuilder.build());
        });
    }

    /**
     * Handles API errors and transforms error responses into {@link EhrApiError}.
     *
     * @return ExchangeFilterFunction for error handling.
     */
    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(
                                new EhrApiError("Error: " + errorBody,
                                        mapHttpStatusToErrorType((HttpStatus) clientResponse.statusCode()))
                        ));
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Sends a GET request with query parameters.
     *
     * @param uri          The endpoint URI.
     * @param queryParams  The query parameters to be included.
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> get(String uri, Map<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(multiValueMap::add);
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(uri).queryParams(multiValueMap).build())
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    public <T> Mono<T> sendGetRequest1(String uri, Map<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(uri);
                    if (queryParams != null && !queryParams.isEmpty()) {
                        queryParams.forEach(uriBuilder::queryParam);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a simple GET request without query parameters.
     *
     * @param endpoint The API endpoint.
     * @param typeRef  The expected response type reference.
     * @param <T>      The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<ApiResponse<T>> get(String endpoint, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        return get(endpoint, Collections.emptyMap(), typeRef);
    }

    /**
     * Sends a POST request with a request body.
     *
     * @param uri          The API endpoint.
     * @param requestBody  The request payload.
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> post(String uri, Object requestBody, ParameterizedTypeReference<T> responseType) {

        return webClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }
    /**
     * Sends a PUT request to update a resource.
     *
     * @param endpoint The API endpoint.
     * @param data     The update data.
     * @param typeRef  The expected response type reference.
     * @param <T>      The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<ApiResponse<T>> sendPutRequest(String endpoint, Object data, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        return webClient.put()
                .uri(endpoint)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(typeRef)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a DELETE request to remove a resource.
     *
     * @param endpoint The API endpoint.
     * @param typeRef  The expected response type reference.
     * @param <T>      The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<ApiResponse<T>> sendDeleteRequest(String endpoint, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        return webClient.delete()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(typeRef)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

}
