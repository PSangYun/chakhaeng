package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.dto.response.UserDto
import com.sos.chakhaeng.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    name = name,
    pictureUrl = pictureUrl
)