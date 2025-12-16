package org.bridge.infrastructure.adapter.typicode;

import org.bridge.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
	User toUser(UserResponse response);
}