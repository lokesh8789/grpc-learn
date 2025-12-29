package com.lokesh;

import com.google.protobuf.Empty;
import com.lokesh.sec01.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GrpcClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();

        unary(managedChannel);
//        serverStreaming(managedChannel);
//        clientStreaming(managedChannel);
//        bidi(managedChannel);
//        flowControl(managedChannel);
    }

    private static void unary(ManagedChannel managedChannel) {
        BankServiceGrpc.BankServiceBlockingStub blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        AccountBalance accountBalance = blockingStub.getAccountBalance(BankCheckRequest.newBuilder().setAccountNumber(13214).build());
        System.out.println(accountBalance);

        AllAccountResponse allAccount = blockingStub.getAllAccount(Empty.newBuilder().build());
        System.out.println(allAccount);

        BankServiceGrpc.BankServiceStub asyncStub = BankServiceGrpc.newStub(managedChannel);
        ResponseObserver<AccountBalance> observer = ResponseObserver.create();
        asyncStub.getAccountBalance(BankCheckRequest.newBuilder().setAccountNumber(-1).build(), observer);
        observer.await();
        System.out.println("Items: " + observer.getItems().size());
    }

    private static void serverStreaming(ManagedChannel managedChannel) {
        BankServiceGrpc.BankServiceBlockingStub blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        Iterator<AccountBalance> accountStream = blockingStub.getAllAccountStream(Empty.getDefaultInstance());
        accountStream.forEachRemaining(System.out::println);

        BankServiceGrpc.BankServiceStub asyncStub = BankServiceGrpc.newStub(managedChannel);
        ResponseObserver<AccountBalance> observer = ResponseObserver.create();
        asyncStub.getAllAccountStream(Empty.getDefaultInstance(), observer);
        observer.await();
        System.out.println("Items: " + observer.getItems().size());
    }

    private static void clientStreaming(ManagedChannel managedChannel) {
//        BankServiceGrpc.BankServiceBlockingV2Stub blockingV2Stub = BankServiceGrpc.newBlockingV2Stub(managedChannel);
//        Stream.iterate(0, i -> i + 1)
//                .limit(10)
//                .forEach(i -> {
//                    try {
//                        blockingV2Stub.getFinalBalance()
//                                .write(BankCheckRequest.newBuilder()
//                                        .setAccountNumber(325)
//                                        .build(), 2, TimeUnit.SECONDS);
//                    } catch (InterruptedException | StatusException | TimeoutException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//        try {
//            FinalBalance finalBalance = blockingV2Stub.getFinalBalance().read();
//            System.out.println(finalBalance);
//        } catch (InterruptedException | StatusException e) {
//            throw new RuntimeException(e);
//        }

        BankServiceGrpc.BankServiceStub asyncStub = BankServiceGrpc.newStub(managedChannel);
        ResponseObserver<FinalBalance> observer = ResponseObserver.create();
        StreamObserver<BankCheckRequest> requestStreamObserver = asyncStub.getFinalBalance(observer);
        Stream.iterate(0, i -> i + 1)
                .limit(10)
                .forEach(i -> requestStreamObserver.onNext(BankCheckRequest.newBuilder().setAccountNumber(i).build()));
        requestStreamObserver.onCompleted();
        observer.await();
    }

    private static void bidi(ManagedChannel managedChannel) {
        BankServiceGrpc.BankServiceStub asyncStub = BankServiceGrpc.newStub(managedChannel);
        ResponseObserver<AccountBalance> observer = ResponseObserver.create();
        StreamObserver<BankCheckRequest> requestStreamObserver = asyncStub.getBalanceByEach(observer);
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> BankCheckRequest.newBuilder().setAccountNumber(i).build())
                .forEach(requestStreamObserver::onNext);
        requestStreamObserver.onCompleted();
        observer.await();
    }

    private static void flowControl(ManagedChannel managedChannel) {
        FlowControlServiceGrpc.FlowControlServiceStub asyncStub = FlowControlServiceGrpc.newStub(managedChannel);
//        ResponseObserver<Output> observer = ResponseObserver.create();
//        StreamObserver<RequestSize> requestObserver = asyncStub.getMessages(observer);
//        requestObserver.onNext(RequestSize.newBuilder().setSize(15).build());
//        requestObserver.onNext(RequestSize.newBuilder().setSize(10).build());
//        requestObserver.onNext(RequestSize.newBuilder().setSize(20).build());
//        requestObserver.onNext(RequestSize.newBuilder().setSize(30).build());
//        requestObserver.onNext(RequestSize.newBuilder().setSize(40).build());
//        observer.await();
        FlowControlResponseHandler responseObserver = new FlowControlResponseHandler();
        StreamObserver<RequestSize> requestObserver = asyncStub.getMessages(responseObserver);
        responseObserver.setRequestObserver(requestObserver);
        responseObserver.start();
        responseObserver.await();
    }

    private static class FlowControlResponseHandler implements StreamObserver<Output> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private StreamObserver<RequestSize> requestObserver;
        private int size;

        @Override
        public void onNext(Output output) {
            this.size--;
            this.process(output);
            if (this.size == 0) {
                System.out.println("----------------------------");
                this.request(ThreadLocalRandom.current().nextInt(1, 10));
            }
        }

        @Override
        public void onError(Throwable throwable) {
            latch.countDown();
        }

        @Override
        public void onCompleted() {
            this.requestObserver.onCompleted();
            System.out.println("Response Completed");
            latch.countDown();
        }

        public void setRequestObserver(StreamObserver<RequestSize> requestObserver) {
            this.requestObserver = requestObserver;
        }

        private void request(int size) {
            System.out.println("Requesting " + size + " items");
            this.size = size;
            this.requestObserver.onNext(RequestSize.newBuilder().setSize(size).build());
        }

        private void process(Output output) {
            System.out.println("Received: " + output);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(0, 500));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void await() {
            try {
                this.latch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void start() {
            request(1);
        }
    }
}
