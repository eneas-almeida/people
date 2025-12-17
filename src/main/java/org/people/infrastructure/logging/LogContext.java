package org.people.infrastructure.logging;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class LogContext {

    private static final ThreadLocal<Map<String, String>> contextHolder = ThreadLocal.withInitial(HashMap::new);

    private LogContext() {
    }

    public static void add(String key, String value) {
        if (key != null && value != null) {
            contextHolder.get().put(key, value);
            MDC.put(key, value);
        }
    }

    public static void addAll(Map<String, String> context) {
        if (context != null && !context.isEmpty()) {
            contextHolder.get().putAll(context);
            context.forEach(MDC::put);
        }
    }

    public static String get(String key) {
        return contextHolder.get().get(key);
    }

    public static Map<String, String> getAll() {
        return new HashMap<>(contextHolder.get());
    }

    public static void remove(String key) {
        contextHolder.get().remove(key);
        MDC.remove(key);
    }

    public static void clear() {
        contextHolder.get().clear();
        MDC.clear();
    }

    public static void setUserId(String userId) {
        add("user_id", userId);
    }

    public static void setRequestId(String requestId) {
        add("request_id", requestId);
    }

    public static void setCorrelationId(String correlationId) {
        add("correlation_id", correlationId);
    }

    public static void setMethod(String method) {
        add("method", method);
    }

    public static void setEndpoint(String endpoint) {
        add("endpoint", endpoint);
    }

    public static void setDuration(Long durationMs) {
        if (durationMs != null) {
            add("duration_ms", String.valueOf(durationMs));
        }
    }

    public static void setStatusCode(String statusCode) {
        add("status_code", statusCode);
    }

    public static void setError(String error) {
        add("error", error);
    }

    public static void setErrorMessage(String errorMessage) {
        add("error_message", errorMessage);
    }
}