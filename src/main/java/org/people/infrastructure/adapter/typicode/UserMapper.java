package org.people.infrastructure.adapter.typicode;

import org.people.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
	User toUser(UserResponse response);
}