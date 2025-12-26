package org.people.infrastructure.client.dummy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.people.application.dto.PeopleResponse;

@Mapper(
	componentModel = "spring",
	implementationName = "DummyMapperImpl"
)
public interface DummyMapper {

	@Mapping(target = "name", expression = "java(response.firstName() + \" \" + response.lastName())")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "id", source = "id")
	PeopleResponse toPeopleResponse(DummyResponse response);
}
