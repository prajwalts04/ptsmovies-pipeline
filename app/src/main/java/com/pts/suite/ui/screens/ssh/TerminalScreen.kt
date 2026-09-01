package com.pts.suite.ui.screens.ssh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.ui.theme.*

@Composable
fun TerminalScreen(
    onBack: () -> Unit
) {
    var outputLines by remember {
        mutableStateOf(
            listOf(
                "Connecting to Raspberry Pi (pts-webssh)...",
                "Linux raspberrypi 6.6.20+rpt-rpi-v8 #1 SMP PREEMPT Debian",
                "Welcome to PTS Terminal.",
                "Type commands below or use quick accessory keys."
            )
        )
    }
    var inputCommand by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Terminal Window Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(DarkSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "pi@raspberrypi: ~", color = EmeraldGreen, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "SSH :22", color = Graphite400, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }

        // Terminal Console Output Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkSurface)
                .border(1.dp, SketchBorder)
                .padding(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(outputLines) { line ->
                    Text(
                        text = line,
                        color = if (line.startsWith(">")) EmeraldGreen else Graphite200,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Accessory Keys Bar (ESC, TAB, CTRL, ALT, UP, DOWN)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ESC", "TAB", "CTRL", "ALT", "CLEAR").forEach { key ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, SketchBorder, RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = key, color = Graphite300, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Terminal Command Line Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(DarkSurface)
                .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$ ", color = EmeraldGreen, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            BasicTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                textStyle = TextStyle(color = Graphite100, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                cursorBrush = SolidColor(EmeraldGreen),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputCommand.isNotBlank()) {
                        val cmd = inputCommand.trim()
                        outputLines = outputLines + "> $cmd"
                        when (cmd) {
                            "clear" -> outputLines = emptyList()
                            "uptime" -> outputLines = outputLines + "up 14 days, 3 users, load average: 0.12, 0.24, 0.28"
                            "pm2 list" -> outputLines = outputLines + "pts-hub [online], pts-stream [online], pts-vault [online], pts-files [online]"
                            else -> outputLines = outputLines + "Executed: $cmd"
                        }
                        inputCommand = ""
                    }
                }),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
