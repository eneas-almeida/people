package org.people.infrastructure.logging;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@GrpcGlobalServerInterceptor
public class GrpcLoggingInterceptor implements ServerInterceptor {

    private static final Logger logger = Logger.getLogger(GrpcLoggingInterceptor.class);
    private static final Metadata.Key<String> REQUEST_ID_HEADER =
            Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> CORRELATION_ID_HEADER =
            Metadata.Key.of("x-correlation-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String requestId = extractOrGenerateRequestId(headers);
        String correlationId = headers.get(CORRELATION_ID_HEADER);
        String method = call.getMethodDescriptor().getFullMethodName();
        long startTime = System.currentTimeMillis();

        RequestContext.setRequestId(requestId);
        if (correlationId != null) {
            RequestContext.setCorrelationId(correlationId);
        }
        LogContext.setMethod(method);

        logger.info("gRPC request started - method: {}, requestId: {}", method, requestId);

        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                long duration = System.currentTimeMillis() - startTime;
                LogContext.setDuration(duration);
                LogContext.setStatusCode(status.getCode().name());

                if (status.isOk()) {
                    logger.info("gRPC request completed - method: {}, requestId: {}, duration: {}ms, status: {}",
                            method, requestId, duration, status.getCode());
                } else {
                    LogContext.setError(status.getCode().name());
                    LogContext.setErrorMessage(status.getDescription());
                    logger.error("gRPC request failed - method: {}, requestId: {}, duration: {}ms, status: {}, error: {}",
                            method, requestId, duration, status.getCode(), status.getDescription());
                }

                LogContext.clear();
                RequestContext.clear();
                super.close(status, trailers);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(wrappedCall, headers)) {
            @Override
            public void onMessage(ReqT message) {
                logger.debug("gRPC message received - method: {}, requestId: {}", method, requestId);
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                logger.debug("gRPC half close - method: {}, requestId: {}", method, requestId);
                super.onHalfClose();
            }

            @Override
            public void onCancel() {
                long duration = System.currentTimeMillis() - startTime;
                LogContext.setDuration(duration);
                logger.warn("gRPC request cancelled - method: {}, requestId: {}, duration: {}ms",
                        method, requestId, duration);
                LogContext.clear();
                RequestContext.clear();
                super.onCancel();
            }
        };
    }

    private String extractOrGenerateRequestId(Metadata headers) {
        String requestId = headers.get(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = RequestContext.generateRequestId();
        }
        return requestId;
    }
}