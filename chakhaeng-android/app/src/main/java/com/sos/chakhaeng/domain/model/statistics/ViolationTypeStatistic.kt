package com.sos.chakhaeng.domain.model.statistics

import com.sos.chakhaeng.domain.model.ViolationType

data class ViolationTypeStatistic(
    val violationType: ViolationType,
    val count: Int,
    val percentage: Int
)

