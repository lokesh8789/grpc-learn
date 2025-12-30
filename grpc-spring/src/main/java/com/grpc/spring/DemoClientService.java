package com.grpc.spring;

import com.grpc.spring.proto.HelloRequest;
import com.grpc.spring.proto.HelloResponse;
import com.grpc.spring.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Callable;

@Service
@Slf4j
public class DemoClientService implements CommandLineRunner {
    @GrpcClient("another-service")
    HelloServiceGrpc.HelloServiceStub helloServiceStub;

    public void getHelloData() {
        helloServiceStub.withWaitForReady()
                .getHelloData(HelloRequest.newBuilder().setName("lok").build(), new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse helloResponse) {
                log.info("Got Response: {}", helloResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("Error: {}", throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Response Completed");
            }
        });
    }

    public void getHelloStream() {
        helloServiceStub
                .withWaitForReady()
                .getHelloStream(HelloRequest.newBuilder().setName("lok").build(), new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse helloResponse) {
                log.info("Got Stream Response: {}", helloResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("Error In [getHelloStream]: {}", throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Response Completed [getHelloStream]");
            }
        });
    }

    public void getHelloClientStream() {
        StreamObserver<HelloRequest> requestStreamObserver = helloServiceStub
                .withWaitForReady()
                .getHelloClientStream(new StreamObserver<HelloResponse>() {
                    @Override
                    public void onNext(HelloResponse helloResponse) {
                        log.info("Got ClientStream Response: {}", helloResponse);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.info("Error In [getHelloClientStream]: {}", throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Response Completed [getHelloClientStream]");
                    }
                });
        for (int i = 0; i < 5; i++) {
            handleException(() -> {
                Thread.sleep(Duration.ofSeconds(1));
                return null;
            });
            requestStreamObserver.onNext(HelloRequest.newBuilder().setName(i + " - " + "Lokesh").build());
        }
        requestStreamObserver.onCompleted();
    }

    public void getHelloBidiStream() {
        StreamObserver<HelloRequest> requestStreamObserver = helloServiceStub
                .withWaitForReady()
                .getHelloBidiStream(new StreamObserver<HelloResponse>() {
                    @Override
                    public void onNext(HelloResponse helloResponse) {
                        log.info("Got BidiStream Response: {}", helloResponse);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.info("Error In [getHelloBidiStream]: {}", throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Response Completed [getHelloBidiStream]");
                    }
                });
        for (int i = 0; i < 5; i++) {
            handleException(() -> {
                Thread.sleep(Duration.ofSeconds(1));
                return null;
            });
            requestStreamObserver.onNext(HelloRequest.newBuilder().setName(i + " - " + "Lokesh").build());
        }
        requestStreamObserver.onCompleted();
    }

    public void handleException(Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception ignored) {}
    }

    @Override
    public void run(String... args) throws Exception {
//        getHelloData();
//        getHelloStream();
//        getHelloClientStream();
        getHelloBidiStream();
    }
}
