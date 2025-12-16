package org.people.infrastructure.entrypoint.grpc;

import com.people.grpc.ServiceProto;
import com.people.grpc.PeopleServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.people.usecase.GetPeopleUseCase;
import org.people.usecase.ListPeopleUseCase;

@GrpcService
public class PeopleGrpcService extends PeopleServiceGrpc.PeopleServiceImplBase {
	private final GetPeopleUseCase getPeopleUseCase;
	private final ListPeopleUseCase listPeopleUseCase;

	public PeopleGrpcService(GetPeopleUseCase getPeopleUseCase, ListPeopleUseCase listPeopleUseCase) {
		this.getPeopleUseCase = getPeopleUseCase;
		this.listPeopleUseCase = listPeopleUseCase;
	}

	@Override
	public void getPeople(ServiceProto.PeopleRequest request, StreamObserver<ServiceProto.PeopleResponse> responseObserver) {
		int peopleId = request.getId();

		getPeopleUseCase.execute(peopleId)
				.subscribe(
						people -> {
							ServiceProto.PeopleResponse response = ServiceProto.PeopleResponse.newBuilder()
									.setId(people.getId())
									.setName(people.getName())
									.setEmail(people.getEmail())
									.build();

							responseObserver.onNext(response);
							responseObserver.onCompleted();
						},
						error -> {
							responseObserver.onError(Status.INTERNAL
									.withDescription("Error fetching people: " + error.getMessage())
									.asRuntimeException());
						}
				);
	}

	@Override
	public void listPeople(ServiceProto.ListPeopleRequest request, StreamObserver<ServiceProto.ListPeopleResponse> responseObserver) {
		listPeopleUseCase.execute()
				.collectList()
				.subscribe(
						peopleList -> {
							ServiceProto.ListPeopleResponse.Builder responseBuilder = ServiceProto.ListPeopleResponse.newBuilder();

							peopleList.forEach(people -> {
								ServiceProto.PeopleResponse peopleResponse = ServiceProto.PeopleResponse.newBuilder()
										.setId(people.getId())
										.setName(people.getName())
										.setEmail(people.getEmail())
										.build();
								responseBuilder.addPeople(peopleResponse);
							});

							responseObserver.onNext(responseBuilder.build());
							responseObserver.onCompleted();
						},
						error -> {
							responseObserver.onError(Status.INTERNAL
									.withDescription("Error listing people: " + error.getMessage())
									.asRuntimeException());
						}
				);
	}
}