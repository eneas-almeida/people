package org.bridge.infrastructure.entrypoint.grpc;

import com.bridge.grpc.ServiceProto;
import com.bridge.grpc.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.bridge.usecase.GetUserUseCase;
import org.bridge.usecase.ListUsersUseCase;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
	private final GetUserUseCase getUserUseCase;
	private final ListUsersUseCase listUsersUseCase;

	public UserGrpcService(GetUserUseCase getUserUseCase, ListUsersUseCase listUsersUseCase) {
		this.getUserUseCase = getUserUseCase;
		this.listUsersUseCase = listUsersUseCase;
	}

	@Override
	public void getUser(ServiceProto.UserRequest request, StreamObserver<ServiceProto.UserResponse> responseObserver) {
		int userId = request.getId();

		getUserUseCase.execute(userId)
				.subscribe(
						user -> {
							ServiceProto.UserResponse response = ServiceProto.UserResponse.newBuilder()
									.setId(user.getId())
									.setName(user.getName())
									.setEmail(user.getEmail())
									.build();

							responseObserver.onNext(response);
							responseObserver.onCompleted();
						},
						error -> {
							responseObserver.onError(Status.INTERNAL
									.withDescription("Error fetching user: " + error.getMessage())
									.asRuntimeException());
						}
				);
	}

	@Override
	public void listUsers(ServiceProto.ListUsersRequest request, StreamObserver<ServiceProto.ListUsersResponse> responseObserver) {
		listUsersUseCase.execute()
				.collectList()
				.subscribe(
						users -> {
							ServiceProto.ListUsersResponse.Builder responseBuilder = ServiceProto.ListUsersResponse.newBuilder();

							users.forEach(user -> {
								ServiceProto.UserResponse userResponse = ServiceProto.UserResponse.newBuilder()
										.setId(user.getId())
										.setName(user.getName())
										.setEmail(user.getEmail())
										.build();
								responseBuilder.addUsers(userResponse);
							});

							responseObserver.onNext(responseBuilder.build());
							responseObserver.onCompleted();
						},
						error -> {
							responseObserver.onError(Status.INTERNAL
									.withDescription("Error listing users: " + error.getMessage())
									.asRuntimeException());
						}
				);
	}
}