package com.github.danilkozyrev.filestorageapi.mapper;

import com.github.danilkozyrev.filestorageapi.domain.Role;
import com.github.danilkozyrev.filestorageapi.domain.User;
import com.github.danilkozyrev.filestorageapi.dto.projection.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        implementationName = "DefaultUserInfoMapper",
        implementationPackage = "com.github.danilkozyrev.filestorageapi.mapper.impl")
public interface UserInfoMapper {

    @Mapping(source = "dateCreated", target = "dateRegistered")
    UserInfo mapUser(User user);

    List<UserInfo> mapUsers(Iterable<User> users);

    default String roleToString(Role role) {
        return role.getName();
    }

}
