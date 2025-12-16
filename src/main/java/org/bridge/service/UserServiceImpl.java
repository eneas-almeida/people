package org.bridge.service;

import com.bridge.grpc.ServiceProto;
import com.bridge.grpc.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void getUser(ServiceProto.UserRequest request, StreamObserver<ServiceProto.UserResponse> responseObserver) {
        int userId = request.getId();

        ServiceProto.UserResponse response = ServiceProto.UserResponse.newBuilder()
                .setId(userId)
                .setName("User " + userId)
                .setEmail("user" + userId + "@example.com")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}