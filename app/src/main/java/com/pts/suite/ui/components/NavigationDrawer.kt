package com.pts.suite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pts.suite.data.api.SystemStats
import com.pts.suite.data.api.UserInfo
import com.pts.suite.ui.theme.*

enum class AppDestination(val title: String, val icon: ImageVector) {
    HUB("PTS Hub", Icons.Default.Bolt),
    STREAM("PTS Stream", Icons.Default.Movie),
    FILES("PTS Files", Icons.Default.Folder),
    VAULT("PTS Vault (Wallet & Notes)", Icons.Default.CreditCard),
    TERMINAL("PTS Mobile SSH", Icons.Default.Terminal),
    DOWNLOADS("Offline Downloads", Icons.Default.Download),
    PROFILE("Profile & Settings", Icons.Default.AccountCircle)
}

@Composable
fun PTSNavigationDrawerContent(
    user: UserInfo?,
    stats: SystemStats,
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = DarkSurface,
        drawerContentColor = Graphite100
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Drawer Header Card with User info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PitchBlack)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Graphite800)
                            .border(1.5.dp, EmeraldGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = user!!.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Graphite100,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = user?.username ?: "Prajwal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Graphite100
                        )
                        Text(
                            text = (user?.role ?: "admin").uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Divider(color = SketchBorder, thickness = 1.dp)

            // Live Telemetry Stats Widget inside Drawer
            Box(modifier = Modifier.padding(12.dp)) {
                SystemStatsWidget(stats = stats)
            }

            Divider(color = SketchBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Graphite100 else DarkSurface)
                            .border(1.dp, if (isSelected) Graphite100 else SketchBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onNavigate(destination)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.title,
                            tint = if (isSelected) PitchBlack else Graphite200,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = destination.title,
                            fontSize = 13.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PitchBlack else Graphite200
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
