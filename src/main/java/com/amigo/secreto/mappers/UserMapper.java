package com.amigo.secreto.mappers;

import com.amigo.secreto.dtos.UserCreateRequestDTO;
import com.amigo.secreto.models.User;

public class UserMapper {

    public static User dtoToUser(UserCreateRequestDTO dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setWishItem(dto.wishItem());
        return user;
    }

}
