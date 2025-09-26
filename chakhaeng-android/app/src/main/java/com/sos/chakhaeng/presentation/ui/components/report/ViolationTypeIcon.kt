package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.R

@Composable
fun ViolationTypeIcon(
    violationType: ViolationType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(violationType.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = when (violationType) {
                ViolationType.ALL -> painterResource(R.drawable.ic_siren)
                ViolationType.WRONG_WAY -> painterResource(R.drawable.ic_wrong_way)
                ViolationType.SIGNAL -> painterResource(R.drawable.ic_traffic)
                ViolationType.LANE -> painterResource(R.drawable.lane)
                ViolationType.LOVE_BUG -> painterResource(R.drawable.ic_scooter)
                ViolationType.NO_PLATE -> painterResource(R.drawable.ic_plate)
                ViolationType.NO_HELMET -> painterResource(R.drawable.ic_helmet)
                ViolationType.OTHERS -> painterResource(R.drawable.ic_ete)
            },
            contentDescription = violationType.displayName,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
    }
}