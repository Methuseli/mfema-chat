package com.mfemachat.chatapp.util;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.mfemachat.chatapp.dto.UserUpdateDto;
import com.mfemachat.chatapp.models.User;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    void updateUserFromDto(UserUpdateDto userDto, @MappingTarget User user);
}
