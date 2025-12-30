package com.grpc.spring;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class ExceptionHandler {
    @GrpcExceptionHandler(RuntimeException.class)
    public Status handleRuntimeException(RuntimeException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }
}
