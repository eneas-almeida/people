package org.people.infrastructure.client.typicode;

import org.mapstruct.Mapping;
import org.people.application.dto.PeopleResponse;
import org.mapstruct.Mapper;

@Mapper(
	componentModel = "spring",
	implementationName = "TypiCodeMapperImpl"
)
public interface TypiCodeMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "email", source = "email")
	PeopleResponse toPeopleResponse(TypiCodeResponse response);
}
