package com.project.souklab.util;

import tools.jackson.databind.ObjectMapper;
import com.project.souklab.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ServletResponseUtil {

    private final ObjectMapper objectMapper;

    public void writeResponse(HttpServletResponse response, int status, ApiResponse<?> apiResponse) throws IOException {
        apiResponse.setCode(status);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
