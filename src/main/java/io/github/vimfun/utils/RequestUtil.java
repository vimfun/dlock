package io.github.vimfun.utils;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;


public class RequestUtil {

    public static String getRequestAsString(HttpServletRequest request, Logger log) {
        StringBuilder requestString = new StringBuilder();

        // Add request line
        requestString.append(request.getMethod()).append(" ")
                .append(request.getRequestURI());
        if (request.getQueryString() != null) {
            requestString.append("?").append(request.getQueryString());
        }
        requestString.append(" ").append(request.getProtocol()).append("\n");

        // Add headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            requestString.append(headerName).append(": ").append(headerValue).append("\n");
        }

        // Add body
        if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
            requestString.append("\n");
            String body = "CAN'T GET THE BODY OF THE REQUEST";
            try {
                body = IOUtils.toString(request.getReader());
            } catch (IOException e) {
                if (log == null) {
                    e.printStackTrace();
                } else {
                    log.warn(body + ": {}", e.getMessage(), e);
                }
            }
            requestString.append(body);
        }

        return requestString.toString();
    }

    public static String withReq(String msg, HttpServletRequest req, Logger log) {
        return msg + "\n\n-------------------\n" + getRequestAsString(req, log);
    }

    public static String withReq(String msg, HttpServletRequest req) {
        return withReq(msg, req, null);
    }

    public static String getStackTrace(Exception ex, HttpServletRequest req) {
        return withReq(ExceptionUtils.getStackTrace(ex), req, null);
    }

    public static String getStackTrace(Exception ex, HttpServletRequest req, Logger log) {
        return withReq(ExceptionUtils.getStackTrace(ex), req,log);
    }
}