package com.library.user.interfaces.http;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void bypassesSwaggerActuatorAndOpenApiPaths() {
        assertThat(filter.shouldNotFilter(request("/swagger-ui/index.html"))).isTrue();
        assertThat(filter.shouldNotFilter(request("/v3/api-docs"))).isTrue();
        assertThat(filter.shouldNotFilter(request("/actuator/health"))).isTrue();
        assertThat(filter.shouldNotFilter(request("/api/v1/users/me"))).isFalse();
    }

    @Test
    void rejectsMissingHeader() throws Exception {
        var request = request("/api/v1/users/me");
        var response = new MockHttpServletResponse();
        var chain = Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getErrorMessage()).isEqualTo("X-Correlation-Id header is required");
    }

    @Test
    void propagatesHeaderWhenPresent() throws Exception {
        var request = request("/api/v1/users/me");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "corr-1");
        var response = new MockHttpServletResponse();
        var chain = Mockito.mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("corr-1");
        Mockito.verify(chain).doFilter(request, response);
    }

    private MockHttpServletRequest request(String uri) {
        var request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
