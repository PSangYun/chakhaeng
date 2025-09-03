package com.sos.chakhaeng.core.data.mapper

import com.sos.chakhaeng.core.data.model.response.UserDto
import com.sos.chakhaeng.core.domain.model.User

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    name = name,
    pictureUrl = pictureUrl
)