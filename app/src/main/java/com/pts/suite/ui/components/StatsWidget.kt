package com.pts.suite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.data.api.SystemStats
import com.pts.suite.ui.theme.*

@Composable
fun SystemStatsWidget(stats: SystemStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.5.dp, SketchBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYSTEM TELEMETRY",
                color = Graphite100,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = "LIVE",
                color = EmeraldGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Row 1: CPU & RAM Meters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CPU Meter
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "CPU LOAD", fontSize = 11.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                Text(
                    text = "${stats.cpuPercent.toInt()}%",
                    fontSize = 18.sp,
                    color = Graphite100,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                LinearProgressIndicator(
                    progress = { stats.cpuPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = EmeraldGreen,
                    trackColor = Graphite800
                )
            }

            // RAM Meter
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "RAM USAGE", fontSize = 11.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                Text(
                    text = "${stats.ramUsedPercent.toInt()}% (${stats.ramUsedMB} MB)",
                    fontSize = 14.sp,
                    color = Graphite100,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                LinearProgressIndicator(
                    progress = { stats.ramUsedPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = if (stats.ramUsedPercent > 85f) DangerRed else EmeraldGreen,
                    trackColor = Graphite800
                )
            }
        }

        // Row 2: Storage Disk & Real-time Network Speeds
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Storage
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "STORAGE (/Data)", fontSize = 11.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                Text(
                    text = "${stats.diskFreeGB.toInt()} GB Free",
                    fontSize = 14.sp,
                    color = Graphite100,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.diskTotalGB.toInt()} GB Total",
                    fontSize = 11.sp,
                    color = Graphite400
                )
            }

            // Network I/O
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "NETWORK SPEED", fontSize = 11.sp, color = Graphite400, fontWeight = FontWeight.Bold)
                Text(
                    text = "⇓ ${stats.netDownloadSpeed}",
                    fontSize = 13.sp,
                    color = EmeraldGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "⇑ ${stats.netUploadSpeed}",
                    fontSize = 13.sp,
                    color = Graphite300,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
