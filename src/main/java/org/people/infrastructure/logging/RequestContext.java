package org.people.infrastructure.logging;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.UUID;

public class RequestContext {

    private static final String REQUEST_ID_KEY = "request_id";
    private static final String CORRELATION_ID_KEY = "correlation_id";
    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    private RequestContext() {
    }

    public static String generateRequestId() {
        String requestId = UUID.randomUUID().toString();
        requestIdHolder.set(requestId);
        LogContext.setRequestId(requestId);
        return requestId;
    }

    public static String getRequestId() {
        String requestId = requestIdHolder.get();
        if (requestId == null) {
            requestId = generateRequestId();
        }
        return requestId;
    }

    public static void setRequestId(String requestId) {
        requestIdHolder.set(requestId);
        LogContext.setRequestId(requestId);
    }

    public static Optional<String> getOptionalRequestId() {
        return Optional.ofNullable(requestIdHolder.get());
    }

    public static void setCorrelationId(String correlationId) {
        LogContext.setCorrelationId(correlationId);
    }

    public static void clear() {
        requestIdHolder.remove();
    }

    public static <T> Mono<T> withRequestId(Mono<T> mono) {
        String currentRequestId = getRequestId();
        return mono.contextWrite(Context.of(REQUEST_ID_KEY, currentRequestId));
    }

    public static <T> Mono<T> withContext(Mono<T> mono) {
        String requestId = getRequestId();
        return mono
                .doFirst(() -> {
                    requestIdHolder.set(requestId);
                    LogContext.setRequestId(requestId);
                })
                .contextWrite(Context.of(REQUEST_ID_KEY, requestId));
    }

    public static String getRequestIdFromContext(Context context) {
        return context.getOrDefault(REQUEST_ID_KEY, generateRequestId());
    }

    public static String getCorrelationIdFromContext(Context context) {
        return context.getOrDefault(CORRELATION_ID_KEY, null);
    }
}