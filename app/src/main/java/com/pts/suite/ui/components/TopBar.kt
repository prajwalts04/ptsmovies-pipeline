package com.pts.suite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pts.suite.data.api.SystemStats
import com.pts.suite.data.api.UserInfo
import com.pts.suite.ui.theme.*

@Composable
fun TopBar(
    user: UserInfo?,
    stats: SystemStats? = null,
    onOpenDrawer: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(PitchBlack)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: 3-line hamburger menu + PTS Branding Lockup
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Main Navigation Menu",
                    tint = Graphite100
                )
            }

            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "PTS",
                color = Graphite100,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Middle: Live System Telemetry Badge
        if (stats?.cpu != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, SketchBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Text(
                    text = "${stats.cpu.percent.toInt()}% CPU",
                    color = EmeraldGreen,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "•",
                    color = Graphite400,
                    fontSize = 10.sp
                )
                Text(
                    text = "${stats.memory?.percent ?: 0}% RAM",
                    color = Graphite200,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Right: Profile Avatar & Username
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, SketchBorder, RoundedCornerShape(20.dp))
                .clickable { onOpenProfile() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = user?.username ?: "User",
                color = Graphite100,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // Profile Picture from local storage or cloud
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Graphite800)
                    .border(1.dp, EmeraldGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user?.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user!!.avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Graphite200,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
