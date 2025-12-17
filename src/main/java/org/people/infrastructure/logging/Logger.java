package org.people.infrastructure.logging;

import datadog.trace.api.CorrelationIdentifier;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

public class Logger {
    private final org.slf4j.Logger logger;
    private final String className;

    private Logger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.className = clazz.getSimpleName();
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    public void info(String message) {
        addDatadogContext();
        logger.info(message);
        clearContext();
    }

    public void info(String message, Object... args) {
        addDatadogContext();
        logger.info(message, args);
        clearContext();
    }

    public void info(String message, Map<String, String> context) {
        addDatadogContext();
        addCustomContext(context);
        logger.info(message);
        clearContext();
    }

    public void debug(String message) {
        addDatadogContext();
        logger.debug(message);
        clearContext();
    }

    public void debug(String message, Object... args) {
        addDatadogContext();
        logger.debug(message, args);
        clearContext();
    }

    public void debug(String message, Map<String, String> context) {
        addDatadogContext();
        addCustomContext(context);
        logger.debug(message);
        clearContext();
    }

    public void warn(String message) {
        addDatadogContext();
        logger.warn(message);
        clearContext();
    }

    public void warn(String message, Object... args) {
        addDatadogContext();
        logger.warn(message, args);
        clearContext();
    }

    public void warn(String message, Throwable throwable) {
        addDatadogContext();
        logger.warn(message, throwable);
        clearContext();
    }

    public void warn(String message, Map<String, String> context) {
        addDatadogContext();
        addCustomContext(context);
        logger.warn(message);
        clearContext();
    }

    public void error(String message) {
        addDatadogContext();
        logger.error(message);
        clearContext();
    }

    public void error(String message, Object... args) {
        addDatadogContext();
        logger.error(message, args);
        clearContext();
    }

    public void error(String message, Throwable throwable) {
        addDatadogContext();
        logger.error(message, throwable);
        clearContext();
    }

    public void error(String message, Throwable throwable, Map<String, String> context) {
        addDatadogContext();
        addCustomContext(context);
        logger.error(message, throwable);
        clearContext();
    }

    public void error(String message, Map<String, String> context) {
        addDatadogContext();
        addCustomContext(context);
        logger.error(message);
        clearContext();
    }

    private void addDatadogContext() {
        try {
            String traceId = CorrelationIdentifier.getTraceId();
            String spanId = CorrelationIdentifier.getSpanId();

            if (traceId != null && !traceId.isEmpty()) {
                MDC.put("dd.trace_id", traceId);
            }

            if (spanId != null && !spanId.isEmpty()) {
                MDC.put("dd.span_id", spanId);
            }

            MDC.put("service", "people");
            MDC.put("class", className);
        } catch (Exception e) {
            logger.debug("Error adding Datadog context", e);
        }
    }

    private void addCustomContext(Map<String, String> context) {
        if (context != null && !context.isEmpty()) {
            context.forEach(MDC::put);
        }
    }

    private void clearContext() {
        MDC.clear();
    }

    public void withContext(Map<String, String> context, Runnable runnable) {
        try {
            addDatadogContext();
            addCustomContext(context);
            runnable.run();
        } finally {
            clearContext();
        }
    }
}