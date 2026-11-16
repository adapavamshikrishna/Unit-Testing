package com.company.department.client;

import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

import com.company.config.ServiceUrlProperties;
import com.company.department.dto.DepartmentResponseDTO;
import com.company.exception.DepartmentServiceBadResponseException;
import com.company.exception.DepartmentServiceTimeoutException;
import com.company.exception.DepartmentServiceUnavailableException;
import com.company.exception.ResourceNotFoundException;

@Component
public class DepartmentApiClient {

    private static final Logger log = LoggerFactory.getLogger(DepartmentApiClient.class);

    private final RestClient restClient;
    private final ServiceUrlProperties serviceUrlProperties;

    public DepartmentApiClient(@Qualifier("departmentRestClient") RestClient departmentRestClient,
                               ServiceUrlProperties serviceUrlProperties) {
        this.restClient = departmentRestClient;
        this.serviceUrlProperties = serviceUrlProperties;
    }

    public DepartmentResponseDTO getDepartmentById(Long departmentId) {
        String baseUrl = serviceUrlProperties.getDepartmentBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new DepartmentServiceUnavailableException(
                    "Department service URL is not configured");
        }

        String url = baseUrl.replaceAll("/+$", "") + "/" + departmentId;
        try {
            log.debug("Calling Department service to validate departmentId={}", departmentId);

            DepartmentResponseDTO response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(DepartmentResponseDTO.class);

            if (response == null || response.getId() == null
                    || !departmentId.equals(response.getId())) {
                log.error("Department service returned an invalid response for departmentId={}",
                        departmentId);
                throw new DepartmentServiceBadResponseException(
                        "Department service returned an invalid response");
            }

            log.debug("Department service validated departmentId={}", departmentId);
            return response;

        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();

            if (status.value() == 404) {
                log.warn("Department service reports departmentId={} was not found", departmentId);
                throw new ResourceNotFoundException("Department not found: " + departmentId);
            }

            if (status.is5xxServerError()) {
                log.error("Department service returned server error status={}", status.value());
                throw new DepartmentServiceUnavailableException(
                        "Department service is temporarily unavailable", ex);
            }

            log.warn("Department service rejected request with status={}", status.value());
            throw new DepartmentServiceBadResponseException(
                    "Department service returned an unexpected client error");

        } catch (ResourceAccessException ex) {
            if (hasTimeoutCause(ex)) {
                log.warn("Department service request timed out for departmentId={}", departmentId);
                throw new DepartmentServiceTimeoutException(
                        "Department service request timed out", ex);
            }

            log.error("Unable to connect to Department service");
            throw new DepartmentServiceUnavailableException(
                    "Department service is unavailable. Please try again later.", ex);

        } catch (DepartmentServiceBadResponseException | ResourceNotFoundException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Department service communication failed");
            throw new DepartmentServiceUnavailableException(
                    "Department service communication failed", ex);
        }
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
