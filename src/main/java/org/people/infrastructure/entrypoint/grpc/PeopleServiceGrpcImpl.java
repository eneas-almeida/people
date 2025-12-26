package org.people.infrastructure.entrypoint.grpc;

import com.people.grpc.ReactorPeopleServiceGrpc;
import com.people.grpc.ServiceProto.ListPeopleRequestGrpc;
import com.people.grpc.ServiceProto.ListPeopleResponseGrpc;
import com.people.grpc.ServiceProto.PeopleRequestGrpc;
import com.people.grpc.ServiceProto.PeopleResponseGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.people.application.service.PeopleService;
import reactor.core.publisher.Mono;

@GrpcService
@RequiredArgsConstructor
public class PeopleServiceGrpcImpl extends ReactorPeopleServiceGrpc.PeopleServiceImplBase {
	private final PeopleService peopleService;

	@Override
	public Mono<PeopleResponseGrpc> getPeople(Mono<PeopleRequestGrpc> request) {
		return request
				.flatMap(req -> peopleService.getById(req.getId()))
				.map(people -> PeopleResponseGrpc.newBuilder()
						.setId(people.getId())
						.setName(people.getName())
						.setEmail(people.getEmail())
						.build());
	}

	@Override
	public Mono<ListPeopleResponseGrpc> listPeople(Mono<ListPeopleRequestGrpc> request) {
		return request
				.flatMapMany(req -> peopleService.listAll())
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