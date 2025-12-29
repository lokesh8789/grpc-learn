package com.lokesh;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ResponseObserver<T> implements StreamObserver<T> {

    private final List<T> list = Collections.synchronizedList(new ArrayList<T>());
    private Throwable throwable;
    private final CountDownLatch countDownLatch;

    private ResponseObserver(int count) {
        this.countDownLatch = new CountDownLatch(count);
    }

    public static <T> ResponseObserver<T> create() {
        return new ResponseObserver<>(1);
    }

    public static <T> ResponseObserver<T> create(int count) {
        return new ResponseObserver<>(count);
    }

    @Override
    public void onNext(T t) {
        System.out.println("onNext " + t);
        list.add(t);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("onError " + throwable);
        if (throwable instanceof StatusRuntimeException statusRuntimeException) {
//            Metadata metadata = Status.trailersFromThrowable(throwable);
            if (Objects.nonNull(statusRuntimeException.getTrailers())) {
                Metadata.Key<String> key = Metadata.Key.of("errorCode", Metadata.ASCII_STRING_MARSHALLER);
                System.out.println("errorCode: " + statusRuntimeException.getTrailers().get(key));
            }
        }
        this.throwable = throwable;
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("onCompleted " + list.size());
        countDownLatch.countDown();
    }

    public void await() {
        try {
            countDownLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> getItems() {
        return list;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
