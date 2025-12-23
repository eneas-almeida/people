package org.people.infrastructure.entrypoint.grpc;

import com.people.grpc.ReactorPeopleServiceGrpc;
import com.people.grpc.ServiceProto.ListPeopleRequestGrpc;
import com.people.grpc.ServiceProto.ListPeopleResponseGrpc;
import com.people.grpc.ServiceProto.PeopleRequestGrpc;
import com.people.grpc.ServiceProto.PeopleResponseGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import org.people.application.usecase.GetPeopleUseCaseImpl;
import org.people.application.usecase.ListPeopleUseCaseImpl;
import reactor.core.publisher.Mono;

@GrpcService
public class PeopleGrpcServiceImpl extends ReactorPeopleServiceGrpc.PeopleServiceImplBase {
	private final GetPeopleUseCaseImpl getPeopleUseCase;
	private final ListPeopleUseCaseImpl listPeopleUseCase;

	public PeopleGrpcServiceImpl(GetPeopleUseCaseImpl getPeopleUseCase, ListPeopleUseCaseImpl listPeopleUseCase) {
		this.getPeopleUseCase = getPeopleUseCase;
		this.listPeopleUseCase = listPeopleUseCase;
	}

	@Override
	public Mono<PeopleResponseGrpc> getPeople(Mono<PeopleRequestGrpc> request) {
		return request
				.flatMap(req -> getPeopleUseCase.execute(req.getId()))
				.map(people -> PeopleResponseGrpc.newBuilder()
						.setId(people.getId())
						.setName(people.getName())
						.setEmail(people.getEmail())
						.build());
	}

	@Override
	public Mono<ListPeopleResponseGrpc> listPeople(Mono<ListPeopleRequestGrpc> request) {
		return request
				.flatMapMany(req -> listPeopleUseCase.execute())
				.map(people -> PeopleResponseGrpc.newBuilder()
						.setId(people.getId())
						.setName(people.getName())
						.setEmail(people.getEmail())
						.build())
				.collectList()
				.map(peopleList -> ListPeopleResponseGrpc.newBuilder()
						.addAllPeople(peopleList)
						.build());
	}
}