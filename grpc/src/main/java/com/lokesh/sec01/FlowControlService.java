package com.lokesh.sec01;

import io.grpc.stub.StreamObserver;

import java.util.stream.IntStream;

public class FlowControlService extends FlowControlServiceGrpc.FlowControlServiceImplBase {
    @Override
    public StreamObserver<RequestSize> getMessages(StreamObserver<Output> responseObserver) {
        return new RequestHandler(responseObserver);
    }

    private static class RequestHandler implements StreamObserver<RequestSize> {

        private final StreamObserver<Output> responseObserver;
        private Integer emitted;

        private RequestHandler(StreamObserver<Output> responseObserver) {
            this.responseObserver = responseObserver;
            this.emitted = 0;
        }

        @Override
        public void onNext(RequestSize requestSize) {
            IntStream.rangeClosed(emitted + 1, 100)
                    .limit(requestSize.getSize())
                    .forEach(i -> {
                        System.out.println("Emitting " + i + " of " + requestSize.getSize());
                        responseObserver.onNext(Output.newBuilder().setValue(i).build());
                    });
            emitted = emitted + requestSize.getSize();
            if (emitted >= 100) {
                System.out.println("No more message to emit");
                responseObserver.onCompleted();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            System.out.println("Error: " + throwable.getMessage());
        }

        @Override
        public void onCompleted() {
            responseObserver.onCompleted();
        }
    }
}
