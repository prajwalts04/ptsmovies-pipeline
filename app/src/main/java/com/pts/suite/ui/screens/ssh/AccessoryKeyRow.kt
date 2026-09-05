package com.pts.suite.ui.screens.ssh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pts.suite.ui.theme.*

/**
 * AccessoryKeyRow provides hardware-like mobile accessory keys:
 * ESC, TAB, Sticky CTRL, Sticky ALT, Arrows (Up/Down/Left/Right),
 * HOME, END, Signals (Ctrl+C, Ctrl+D, Ctrl+Z, Ctrl+L), and Font Zoom (A+/A-).
 */
@Composable
fun AccessoryKeyRow(
    isCtrlActive: Boolean,
    isAltActive: Boolean,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onSendKey: (String) -> Unit,
    onSendControlChar: (Char) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleSymbols: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ESC Key (\u001B)
        AccessoryButton(label = "ESC", onClick = { onSendKey("\u001B") })

        // TAB Key (\t)
        AccessoryButton(label = "TAB", onClick = { onSendKey("\t") })

        // Sticky CTRL Toggle
        AccessoryToggleButton(
            label = "CTRL",
            isActive = isCtrlActive,
            onClick = onToggleCtrl
        )

        // Sticky ALT Toggle
        AccessoryToggleButton(
            label = "ALT",
            isActive = isAltActive,
            onClick = onToggleAlt
        )

        // Arrow Keys
        AccessoryButton(label = "▲", onClick = { onSendKey("\u001B[A") }) // UP
        AccessoryButton(label = "▼", onClick = { onSendKey("\u001B[B") }) // DOWN
        AccessoryButton(label = "◀", onClick = { onSendKey("\u001B[D") }) // LEFT
        AccessoryButton(label = "▶", onClick = { onSendKey("\u001B[C") }) // RIGHT

        // Navigation
        AccessoryButton(label = "HOME", onClick = { onSendKey("\u001B[H") })
        AccessoryButton(label = "END", onClick = { onSendKey("\u001B[F") })

        // Signals Quick Buttons
        AccessoryButton(
            label = "^C",
            accentColor = DangerRed,
            onClick = { onSendKey("\u0003") } // SIGINT
        )
        AccessoryButton(
            label = "^D",
            accentColor = GoldenYellow,
            onClick = { onSendKey("\u0004") } // EOF
        )
        AccessoryButton(
            label = "^Z",
            accentColor = Color(0xFF38BDF8),
            onClick = { onSendKey("\u001A") } // SIGTSTP
        )
        AccessoryButton(
            label = "^L",
            accentColor = EmeraldGreen,
            onClick = { onSendKey("\u000C") } // Clear Screen
        )

        // Font Zoom Adjusters
        AccessoryButton(label = "A+", onClick = onZoomIn)
        AccessoryButton(label = "A-", onClick = onZoomOut)

        // Extended Symbols Drawer Toggle
        AccessoryButton(label = "{ }", accentColor = EmeraldGreen, onClick = onToggleSymbols)
    }
}

/**
 * ExtendedSymbolsBar displays quick-tap UNIX and shell operators.
 */
@Composable
fun ExtendedSymbolsBar(
    onSendSymbol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val symbols = listOf(
        "|", "/", "\\", "~", "-", "_", "$", "&", "#", "!",
        "*", ">", "<", "=", "?", ":", ";", "'", "\"", "`",
        "(", ")", "[", "]", "{", "}"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(DarkSurfaceElevated)
            .border(0.5.dp, SketchBorder)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        symbols.forEach { sym ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurface)
                    .border(0.5.dp, SketchBorder, RoundedCornerShape(4.dp))
                    .clickable { onSendSymbol(sym) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sym,
                    color = EmeraldGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AccessoryButton(
    label: String,
    accentColor: Color = Graphite300,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, SketchBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = accentColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AccessoryToggleButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) EmeraldGreen else DarkSurfaceElevated)
            .border(1.dp, if (isActive) EmeraldGreen else SketchBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) PitchBlack else Graphite300,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
    }
}
