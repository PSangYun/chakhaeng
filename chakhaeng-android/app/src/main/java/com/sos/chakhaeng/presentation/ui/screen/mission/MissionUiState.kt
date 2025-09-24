package com.sos.chakhaeng.presentation.ui.screen.mission

import com.sos.chakhaeng.domain.model.profile.Mission

data class MissionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val missions: List<Mission> = emptyList()
) {
    companion object {
        fun loading() = MissionUiState(isLoading = true)

        fun error(message: String) = MissionUiState(
            isLoading = false,
            error = message
        )

        fun success(missions: List<Mission>) = MissionUiState(
            isLoading = false,
            missions = missions
        )
    }

    val hasData: Boolean
        get() = missions.isNotEmpty() && !isLoading && error == null

    val activeMissions: List<Mission>
        get() = missions.filter { !it.isCompleted }

    val completedMissions: List<Mission>
        get() = missions.filter { it.isCompleted }

    val completedMissionsCount: Int
        get() = completedMissions.size

    val totalMissionsCount: Int
        get() = missions.size

    val progressPercentage: Float
        get() = if (totalMissionsCount > 0) {
            (completedMissionsCount.toFloat() / totalMissionsCount.toFloat()) * 100f
        } else 0f
}