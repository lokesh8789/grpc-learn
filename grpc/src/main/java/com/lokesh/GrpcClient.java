package com.lokesh;

import com.google.protobuf.Empty;
import com.lokesh.interceptor.DeadlineInterceptor;
import com.lokesh.sec01.*;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GrpcClient {
    public static void main(String[] args) throws InterruptedException {
//        Metadata.Key<String> key = Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER);
//        Metadata metadata = new Metadata();
//        metadata.put(key, "lokesh");
//        ClientInterceptor apiKeyInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
//                .intercept(new DeadlineInterceptor(Duration.ofMillis(2000)), apiKeyInterceptor)
                .build();

//        unary(managedChannel);
        serverStreaming(managedChannel);
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

        BankServiceGrpc.BankServiceStub asyncStub = BankServiceGrpc.newStub(managedChannel)
//                .withCallCredentials(new UserSessionToken("user-token-1"))
//                .withCompression("gzip")
                .withDeadline(Deadline.after(2, TimeUnit.SECONDS));
        ResponseObserver<AccountBalance> observer = ResponseObserver.create();
        asyncStub.getAccountBalance(BankCheckRequest.newBuilder().setAccountNumber(-1).build(), observer);
        observer.await();
        System.out.println("Items: " + observer.getItems().size());
    }

    private static void serverStreaming(ManagedChannel managedChannel) {
        try {
            BankServiceGrpc.BankServiceBlockingStub blockingStub = BankServiceGrpc.newBlockingStub(managedChannel)
                    .withWaitForReady()
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS));
            Iterator<AccountBalance> accountStream = blockingStub.getAllAccountStream(Empty.getDefaultInstance());
//            accountStream.forEachRemaining(System.out::println);
            while (accountStream.hasNext()) {
                System.out.println(accountStream.next());
            }
        } catch (Exception e) {
            System.out.println("error");
        }
        try {
            Thread.sleep(Duration.ofSeconds(10));
        } catch (InterruptedException ignored) {}

        BankServiceGrpc.BankServiceStub asyncStub = BankServiceGrpc.newStub(managedChannel)
                .withExecutor(Executors.newVirtualThreadPerTaskExecutor());
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

    private static class UserSessionToken extends CallCredentials {

        private static final String TOKEN_FORMAT = "%s %s";
        private final String jwt;

        public UserSessionToken(String jwt) {
            this.jwt = jwt;
        }

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
            executor.execute(() -> {
                var metadata = new Metadata();
                metadata.put(Constants.USER_TOKEN_KEY, TOKEN_FORMAT.formatted(Constants.BEARER, jwt));
                metadataApplier.apply(metadata);
            });
        }

    }
}
