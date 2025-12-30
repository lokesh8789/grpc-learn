package com.grpc.spring;

import com.grpc.spring.proto.HelloRequest;
import com.grpc.spring.proto.HelloResponse;
import com.grpc.spring.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Duration;
import java.util.concurrent.Callable;

@GrpcService
@Slf4j
public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void getHelloData(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        if (request.getName().equals("lok")) {
            throw new RuntimeException("This is a Bad Request");
        }
        responseObserver.onNext(HelloResponse.newBuilder().setData("Response: " + request.getName()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getHelloStream(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        for (int i = 0; i < 10; i++) {
            handleException(() -> {
                Thread.sleep(Duration.ofSeconds(1));
                return null;
            });
            log.info("Calling");
            responseObserver.onNext(HelloResponse.newBuilder().setData(i + " - " + request.getName()).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> getHelloClientStream(StreamObserver<HelloResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(HelloRequest helloRequest) {
                log.info("Got Request: {}", helloRequest);
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                log.info("Request Stream Completed");
                handleException(() -> {
                    Thread.sleep(2000);
                    return null;
                });
                responseObserver.onNext(HelloResponse.newBuilder().setData("All Request Data Received").build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<HelloRequest> getHelloBidiStream(StreamObserver<HelloResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(HelloRequest helloRequest) {
                log.info("Request Received: {}", helloRequest);
                responseObserver.onNext(HelloResponse.newBuilder().setData("Response: " + helloRequest.getName()).build());
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onCompleted() {
                log.info("Request Stream Finished");
                handleException(() -> {
                    Thread.sleep(2000);
                    return null;
                });
                responseObserver.onNext(HelloResponse.newBuilder().setData("Last Server Message").build());
                responseObserver.onCompleted();
            }
        };
    }

    public void handleException(Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception ignored) {}
    }
}
