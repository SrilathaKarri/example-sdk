package sdk.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sdk.base.DTO.SearchFiltersDTO;
import sdk.base.errors.EhrApiError;
import sdk.base.utils.StringUtils;
import sdk.facility.enums.Region;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;


/**
 * BaseService is an abstract class that provides core functionalities for interacting with APIs using WebClient.
 * This service includes:
 * <ul>
 *     <li>Setting up a WebClient instance with common headers.</li>
 *     <li>Handling API errors.</li>
 *     <li>Performing GET, POST, PUT, and DELETE requests.</li>
 *     <li>Utility methods for validation.</li>
 * </ul>

 */
@Service
public abstract class Base {

    @Value("${api.url}")
    private String apiUrl;

    protected final ObjectMapper objectMapper;
    protected final WebClient webClient;

    protected Base(ObjectMapper objectMapper, WebClient webClient) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }


    /**
     * Sends a GET request with query parameters.
     *
     * @param url          The endpoint URL.
     * @param queryParams  The query parameters to be included.
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> get(String url, Map<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(multiValueMap::add);
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(url).queryParams(multiValueMap).build())
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a simple GET request without query parameters.
     *
     * @param url The API endpoint.
     * @param responseType  The expected response type reference.
     * @param <T>      The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> get(String url, ParameterizedTypeReference<T> responseType) {
        String fullUrl = String.format("%s%s", apiUrl, url);
        return webClient.get()
                .uri(URI.create(fullUrl))
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
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
    public <T> Mono<T> put(String endpoint, Object data, ParameterizedTypeReference<T> typeRef) {
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
    public <T> Mono<T> delete(String endpoint, ParameterizedTypeReference<T> typeRef) {
        return webClient.delete()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(typeRef)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Validates if a given string matches an enum value.
     * This method checks if a provided value corresponds to any constant within a specified Enum class.
     * If the enum is {@link Region}, it also validates region codes and aliases.
     *
     * @param value     The input value to validate.
     * @param enumClass The enum class to validate against.
     * @param <T>       The enum type.
     * @return {@code true} if the value is valid, otherwise {@code false}.
     */
    public static <T extends Enum<T>> boolean isValidEnum(String value, Class<T> enumClass) {
        if (value == null || enumClass == null) {
            return false;
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(enumConstant -> {
                    if (enumConstant instanceof Region region) {
                        return region.getCode().equalsIgnoreCase(value) ||
                                Arrays.stream(region.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(value));
                    }
                    return enumConstant.name().equalsIgnoreCase(value);
                });
    }


    /**
     * Transforms search filter fields from {@link SearchFiltersDTO} into a structured format
     * suitable for API requests.
     * This method converts user-provided search filters into a key-value mapping compatible
     * with the expected API request format.
     *
     * @param filters The {@link SearchFiltersDTO} object containing search parameters.
     * @return A {@code Map<String, Object>} representing transformed filter keys.
     * @throws IllegalArgumentException If date constraints are violated.
     */
    public Map<String, Object> transformFilterKeys(SearchFiltersDTO filters) {
        Map<String, Object> updatedFilters = new HashMap<>();

        // Map first name if present
        if (!StringUtils.isNullOrEmpty(filters.getFirstName())) {
            updatedFilters.put("name", filters.getFirstName());
        }

        // Map last name if present
        if (!StringUtils.isNullOrEmpty(filters.getLastName())) {
            updatedFilters.put("family", filters.getLastName());
        }

        // Map birthdate if present
        if (!StringUtils.isNullOrEmpty(filters.getBirthDate())) {
            updatedFilters.put("birthdate", filters.getBirthDate());
        }

        // Map gender if present
        if (filters.getGender() != null) {
            updatedFilters.put("gender", filters.getGender().getValue());
        }

        // Map phone number if present
        if (!StringUtils.isNullOrEmpty(filters.getPhone())) {
            updatedFilters.put("phone", filters.getPhone());
        }

        // Map state if present
        if (filters.getState() != null) {
            updatedFilters.put("address-state", filters.getState());
        }

        // Map postal code (pincode) if present
        if (!StringUtils.isNullOrEmpty(filters.getPincode())) {
            updatedFilters.put("address-postalcode", filters.getPincode());
        }

        // Map record count if present
        if (!StringUtils.isNullOrEmpty(filters.getCount())) {
            updatedFilters.put("_count", filters.getCount());
        }

        // Map email if present
        if (!StringUtils.isNullOrEmpty(filters.getEmailId())) {
            updatedFilters.put("email", filters.getEmailId());
        }

        // Map organization ID if present
        if (!StringUtils.isNullOrEmpty(filters.getOrganizationId())) {
            updatedFilters.put("identifier", filters.getOrganizationId());
        }

        // Map registration ID if present (overwrites organization ID if both exist)
        if (!StringUtils.isNullOrEmpty(filters.getRegistrationId())) {
            updatedFilters.put("identifier", filters.getRegistrationId());
        }

        // Map identifier if present (overwrites previous identifier mappings if any)
        if (!StringUtils.isNullOrEmpty(filters.getIdentifier())) {
            updatedFilters.put("identifier", filters.getIdentifier());
        }

        // Handle date filters (_lastUpdated) for search query
        if (!StringUtils.isNullOrEmpty(filters.getFromDate())) {
            LocalDate fromDate = LocalDate.parse(filters.getFromDate());
            if (fromDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("fromDate should not be in the future");
            }
            updatedFilters.put("_lastUpdated", Collections.singletonList("ge" + fromDate));
        }

        if (!StringUtils.isNullOrEmpty(filters.getToDate())) {
            LocalDate toDate = LocalDate.parse(filters.getToDate());
            if (toDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("toDate should not be in the future");
            }
            if (!StringUtils.isNullOrEmpty(filters.getFromDate()) && toDate.isBefore(LocalDate.parse(filters.getFromDate()))) {
                throw new IllegalArgumentException("toDate should be greater than or equal to fromDate");
            }

            // If _lastUpdated already has a "ge" value, append "le" value.
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> lastUpdatedList = objectMapper.convertValue(updatedFilters.get("_lastUpdated"), new TypeReference<>() {
            });
            if (lastUpdatedList == null) {
                lastUpdatedList = new ArrayList<>();
            }
            lastUpdatedList.add("le" + toDate);
            updatedFilters.put("_lastUpdated", lastUpdatedList);

        }
        return updatedFilters;
    }
}
