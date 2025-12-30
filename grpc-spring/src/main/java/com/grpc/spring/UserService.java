package com.grpc.spring;

import com.grpc.spring.proto.User;
import com.grpc.spring.proto.UserResponse;
import com.grpc.spring.proto.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@GrpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void getUserDetails(User request, StreamObserver<UserResponse> responseObserver) {
        log.info("User Request: {}", request);
        responseObserver.onNext(UserResponse.newBuilder()
                .setId(ThreadLocalRandom.current().nextInt(1000))
                .setName(request.getName())
                .addAllRoles(List.of("ADMIN", "OWNER"))
                .build());

        responseObserver.onCompleted();
    }
}
