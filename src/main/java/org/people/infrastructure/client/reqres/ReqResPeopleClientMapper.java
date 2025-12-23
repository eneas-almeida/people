package org.people.infrastructure.client.reqres;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.people.application.dto.PeopleResponse;

@Mapper(
	componentModel = "spring",
	implementationName = "ReqResPeopleClientMapperImpl"
)
public interface ReqResPeopleClientMapper {

	@Mapping(target = "name", expression = "java(response.firstName() + \" \" + response.lastName())")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "id", source = "id")
	PeopleResponse toPeopleResponse(ReqResPeopleClientResponse response);
}