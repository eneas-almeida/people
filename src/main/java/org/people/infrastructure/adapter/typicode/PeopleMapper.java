package org.people.infrastructure.adapter.typicode;

import org.people.domain.entity.People;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PeopleMapper {
	People toPeople(PeopleResponse response);
}