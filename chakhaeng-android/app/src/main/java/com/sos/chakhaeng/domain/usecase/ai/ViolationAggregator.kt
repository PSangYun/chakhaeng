package com.sos.chakhaeng.domain.usecase.ai

import com.sos.chakhaeng.domain.model.violation.ViolationEvent
import javax.inject.Inject

class ViolationAggregator @Inject constructor() {

    // 위반 유형 우선순위 (숫자 클수록 우선)
    // 필요시 여기만 추가/수정하세요.
    private val PRIORITY = mapOf(
        "신호위반" to 100,
        "헬멧 미착용" to 70,
        // "횡단보도 침범" to 60, ... 등 향후 확장
    )

    // 유형을 정규화(접미사/세부타입 있을 때 대비). 지금은 그대로 사용해도 OK.
    private fun baseType(type: String): String = type.trim()

    fun aggregate(events: List<ViolationEvent>): List<ViolationEvent> {
        if (events.isEmpty()) return emptyList()

        // 1) (선택) 동일 타입/유사 confidence 중복 제거 — 과도한 중복을 줄여 안정화
        val deduped = events
            .groupBy { it.type to (it.confidence * 100).toInt() } // 0.01 단위로 라운딩
            .map { (_, group) -> group.maxBy { e -> e.confidence } }

        // 2) 프레임당 1건만 반환: "우선순위 → 신뢰도"로 최상위 선택
        val top = deduped.maxWith(
            compareBy<ViolationEvent>(
                { PRIORITY[baseType(it.type)] ?: 0 }   // 우선순위
            ).thenBy { it.confidence }                 // 동률이면 신뢰도 높은 것
        )
        return listOf(top)
    }
}
