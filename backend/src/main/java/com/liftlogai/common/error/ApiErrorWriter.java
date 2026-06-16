package com.liftlogai.common.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorWriter {

    public void write(HttpServletResponse response, int status, String errorCode, String message, String path)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"timestamp":"%s","status":%d,"errorCode":"%s","message":"%s","path":"%s"}\
                """.formatted(
                Instant.now(),
                status,
                escape(errorCode),
                escape(message),
                escape(path)
        ));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
