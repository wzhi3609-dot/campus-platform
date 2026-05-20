package com.campus.common.ratelimit;

import com.campus.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易 IP 级别速率限制拦截器，防止登录和注册接口被暴力请求
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000; // 1分钟

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        if (!path.equals("/api/auth/login") && !path.equals("/api/auth/register")) {
            return true;
        }

        String ip = getClientIp(request);
        long now = System.currentTimeMillis();
        WindowCounter counter = counters.computeIfAbsent(ip, k -> new WindowCounter(now));

        synchronized (counter) {
            if (now - counter.windowStart > WINDOW_MS) {
                counter.windowStart = now;
                counter.count = 0;
            }
            counter.count++;

            if (counter.count > MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                objectMapper.writeValue(response.getWriter(),
                        Result.error(429, "请求过于频繁，请稍后再试"));
                return false;
            }
            return true;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) {
            return xri;
        }
        return request.getRemoteAddr();
    }

    private static class WindowCounter {
        long windowStart;
        int count;

        WindowCounter(long start) {
            this.windowStart = start;
            this.count = 0;
        }
    }
}
