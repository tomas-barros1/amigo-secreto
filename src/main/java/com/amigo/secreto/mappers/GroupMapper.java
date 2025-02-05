package com.amigo.secreto.mappers;

import com.amigo.secreto.dtos.GroupCreateRequestDTO;
import com.amigo.secreto.models.Group;

public class GroupMapper {

    public static Group dtoToGroup(GroupCreateRequestDTO dto) {
        Group group = new Group();
        group.setOwnerId(dto.ownerId());
        group.setName(dto.name());
        return group;
    }

}
