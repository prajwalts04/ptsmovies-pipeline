package com.pts.suite.ui.screens.ssh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.ui.theme.*

data class QuickCommand(
    val title: String,
    val command: String,
    val category: String,
    val icon: ImageVector,
    val description: String
)

/**
 * QuickCommandDrawer displays categorized shortcut commands for managing
 * the Raspberry Pi server (htop, docker, pm2, sensors, df -h, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCommandDrawer(
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commands = listOf(
        // System Monitoring
        QuickCommand("htop / Process Monitor", "htop", "System", Icons.Default.Speed, "Interactive realtime process monitor"),
        QuickCommand("System Temperature", "vcgencmd measure_temp || sensors", "System", Icons.Default.Thermostat, "Raspberry Pi SoC CPU temperature"),
        QuickCommand("System Uptime & Load", "uptime", "System", Icons.Default.Timer, "Uptime, active users, load averages"),
        QuickCommand("Kernel & OS Info", "uname -a && cat /etc/os-release | head -n 5", "System", Icons.Default.Info, "Linux kernel version and Debian distro info"),

        // Services & PM2
        QuickCommand("PM2 Status", "pm2 status", "Services", Icons.Default.Bolt, "Status of all PTS background microservices"),
        QuickCommand("PM2 Realtime Logs", "pm2 logs --lines 30", "Services", Icons.Default.List, "Recent logs from all PM2 managed services"),
        QuickCommand("PM2 Restart All", "pm2 restart all", "Services", Icons.Default.Refresh, "Restarts all PTS services safely"),
        QuickCommand("Docker PS", "docker ps -a", "Services", Icons.Default.Apps, "List running and stopped Docker containers"),

        // Disk & Memory
        QuickCommand("Disk Usage (df -h)", "df -h", "Storage", Icons.Default.Storage, "Free and used disk space on MergerFS /Data pool"),
        QuickCommand("Memory Usage (free -h)", "free -h", "Storage", Icons.Default.Memory, "Total, used, free RAM and swap space"),
        QuickCommand("MergerFS Pool Tree", "du -sh /Data/* 2>/dev/null | sort -hr", "Storage", Icons.Default.Folder, "Disk usage per top-level /Data directory"),

        // Network & Connectivity
        QuickCommand("Network IP & Interfaces", "ip -br a", "Network", Icons.Default.Wifi, "IP addresses on eth0, wlan0, and tailscale"),
        QuickCommand("Open TCP Ports (netstat)", "ss -tulnp || netstat -tuln", "Network", Icons.Default.Lan, "Listening TCP & UDP server ports"),
        QuickCommand("Ping Gateway", "ping -c 4 1.1.1.1", "Network", Icons.Default.NetworkCheck, "Test WAN internet latency and packet loss")
    )

    var selectedCategory by remember { mutableStateOf("All") }
    var customCommandText by remember { mutableStateOf("") }

    val categories = listOf("All", "System", "Services", "Storage", "Network")
    val filteredCommands = remember(selectedCategory) {
        if (selectedCategory == "All") commands else commands.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Graphite400) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldGreen)
                    Text(
                        text = "Quick Command Runner",
                        color = Graphite100,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Graphite400)
                }
            }

            // Custom Command Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customCommandText,
                    onValueChange = { customCommandText = it },
                    placeholder = { Text("Type or paste bash command...", fontSize = 12.sp, color = Graphite400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SketchBorder,
                        focusedTextColor = Graphite100,
                        unfocusedTextColor = Graphite100,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (customCommandText.isNotBlank()) {
                            onExecuteCommand(customCommandText.trim())
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RUN", color = PitchBlack, fontWeight = FontWeight.Bold)
                }
            }

            // Category Filter Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) EmeraldGreen else DarkSurface)
                            .border(1.dp, if (isSel) EmeraldGreen else SketchBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) PitchBlack else Graphite300
                        )
                    }
                }
            }

            // Commands List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCommands) { cmd ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(1.dp, SketchBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                onExecuteCommand(cmd.command)
                                onDismiss()
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(cmd.icon, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = cmd.title,
                                        color = Graphite100,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cmd.command,
                                        color = Color(0xFF86EFAC),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = cmd.description,
                                        color = Graphite400,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
