package com.stockup.backend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockup.backend.common.response.ApiError;
import com.stockup.backend.common.response.ApiResponse;
import com.stockup.backend.common.response.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Object> body = new ApiResponse<>(
                false,
                ResponseMessage.FORBIDDEN.getMessage(),
                null,
                List.of(new ApiError(null, "You do not have permission to access this resource.")),
                Instant.now()
        );

        objectMapper.writeValue(response.getWriter(), body);
    }
}
