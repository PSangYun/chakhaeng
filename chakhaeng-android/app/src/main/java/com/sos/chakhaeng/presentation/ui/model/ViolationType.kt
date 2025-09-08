package com.sos.chakhaeng.presentation.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.sos.chakhaeng.presentation.ui.theme.Orange

enum class ViolationType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    ALL("전체", Icons.Default.List, Color.Gray),
    TRAFFIC_LIGHT_VIOLATION("신호위반", Icons.Default.Traffic, Color.Red),
    SPEED_VIOLATION("속도위반", Icons.Default.Speed, Orange),
    LANE_VIOLATION("차선침범", Icons.Default.SyncAlt, Color.Yellow),
    CUTTING_IN("끼어들기", Icons.Default.TurnRight, Color.Blue);

    companion object {
        fun fromString(type: String): ViolationType {
            return when (type) {
                "TRAFFIC_LIGHT_VIOLATION" -> TRAFFIC_LIGHT_VIOLATION
                "SPEED_VIOLATION" -> SPEED_VIOLATION
                "LANE_VIOLATION" -> LANE_VIOLATION
                "CUTTING_IN" -> CUTTING_IN
                else -> ALL
            }
        }
    }
}