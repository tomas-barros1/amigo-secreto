package com.amigo.secreto.dtos;

import java.util.UUID;

public record FriendDrawDTO(UUID friendId, String friendUsername, String wishItem) {
}
