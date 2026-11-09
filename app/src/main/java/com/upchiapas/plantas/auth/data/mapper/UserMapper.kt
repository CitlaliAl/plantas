package com.upchiapas.plantas.auth.data.mapper

import com.upchiapas.plantas.auth.data.remote.dto.UserDto
import com.upchiapas.plantas.auth.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id ?: 0,
    name = name.orEmpty(),
    email = email.orEmpty()
)
