package com.lokesh.sec01;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.stream.Stream;

public class BankService extends BankServiceGrpc.BankServiceImplBase {
    @Override
    public void getAccountBalance(BankCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        responseObserver.onNext(AccountBalance.newBuilder()
                .setBalance(34)
                .setAccountNumber(request.getAccountNumber())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAccount(Empty request, StreamObserver<AllAccountResponse> responseObserver) {
        responseObserver.onNext(AllAccountResponse.newBuilder()
                        .addAllAccounts(List.of(AccountBalance.newBuilder().setAccountNumber(3453532).build(),
                                AccountBalance.newBuilder().setAccountNumber(3432).build()))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAccountStream(Empty request, StreamObserver<AccountBalance> responseObserver) {
        Stream.iterate(0, i -> i + 1)
                .limit(10)
                .map(s -> AccountBalance.newBuilder().setAccountNumber(s).build())
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<BankCheckRequest> getFinalBalance(StreamObserver<FinalBalance> responseObserver) {
        return new StreamObserver<>() {
            int sum = 0;
            @Override
            public void onNext(BankCheckRequest bankCheckRequest) {
                System.out.println("Received Request: " + bankCheckRequest);
                sum += 2;
            }
            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }
            @Override
            public void onCompleted() {
                responseObserver.onNext(FinalBalance.newBuilder().setBalance(sum).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<BankCheckRequest> getBalanceByEach(StreamObserver<AccountBalance> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(BankCheckRequest bankCheckRequest) {
                System.out.println("Received Request: " + bankCheckRequest);
                responseObserver.onNext(AccountBalance.newBuilder().setAccountNumber(bankCheckRequest.getAccountNumber()).build());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
