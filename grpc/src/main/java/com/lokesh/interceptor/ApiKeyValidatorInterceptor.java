package com.lokesh.interceptor;

import io.grpc.*;

import java.util.Objects;

public class ApiKeyValidatorInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        System.out.println("Method Name: " + serverCall.getMethodDescriptor().getFullMethodName());
        Metadata.Key<String> key = Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER);
        String apiKey = metadata.get(key);
        if (Objects.equals(apiKey, "lokesh")) {
            return serverCallHandler.startCall(serverCall, metadata);
        }
        serverCall.close(Status.UNAUTHENTICATED.withDescription("Wrong API Key"), metadata);
        return new ServerCall.Listener<ReqT>() {};
    }
}
