package com.pts.suite.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.SystemStats
import com.pts.suite.ui.components.AppDestination
import com.pts.suite.ui.components.SystemStatsWidget
import com.pts.suite.ui.theme.*

@Composable
fun DashboardScreen(
    stats: SystemStats,
    onNavigate: (AppDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // System Live Stats Card
        SystemStatsWidget(stats = stats)

        // Services Grid
        Text(
            text = "PI ECOSYSTEM SERVICES",
            color = Graphite400,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServiceShortcutCard(
                title = "PTS Stream",
                desc = "Movies & Series",
                icon = Icons.Default.Movie,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppDestination.STREAM) }
            )

            ServiceShortcutCard(
                title = "PTS Hub",
                desc = "Transcode Queue",
                icon = Icons.Default.Bolt,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppDestination.HUB) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServiceShortcutCard(
                title = "PTS Files",
                desc = "MergerFS /Data",
                icon = Icons.Default.Folder,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppDestination.FILES) }
            )

            ServiceShortcutCard(
                title = "PTS Vault",
                desc = "Cards & Notes",
                icon = Icons.Default.CreditCard,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppDestination.VAULT) }
            )
        }

        ServiceShortcutCard(
            title = "PTS Terminal (SSH)",
            desc = "Encrypted Linux Shell to Raspberry Pi",
            icon = Icons.Default.Terminal,
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(AppDestination.TERMINAL) }
        )
    }
}

@Composable
private fun ServiceShortcutCard(
    title: String,
    desc: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.2.dp, SketchBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, SketchBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
        }

        Column {
            Text(text = title, color = Graphite100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = Graphite400, fontSize = 11.5.sp)
        }
    }
}
