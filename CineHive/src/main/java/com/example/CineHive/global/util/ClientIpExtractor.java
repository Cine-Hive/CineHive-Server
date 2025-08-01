package com.example.CineHive.global.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpExtractor {
    private ClientIpExtractor() {}

    public static String getClientIp(HttpServletRequest request) {
        String[] headerNames = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0];
            }
        }
        return request.getRemoteAddr();
    }
}