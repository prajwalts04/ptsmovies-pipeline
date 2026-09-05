package com.pts.suite.ui.screens.hub

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pts.suite.ui.theme.*
import java.util.Locale

data class ParsedBulkItem(
    val season: Int,
    val episode: Int,
    val epCode: String,
    val downloadUrl: String,
    val isValid: Boolean = true
)

object BulkUrlParser {

    private val HEADER_KEYWORDS = setOf(
        "season", "series", "episode", "ep", "ep.", "link", "url", "download", "title", "name", "#"
    )

    private val EP_REGEX = "(?i)S(\\d+)E(\\d+)".toRegex()
    private val EP_NUM_ONLY_REGEX = "(?i)(?:episode|ep)\\s*(\\d+)".toRegex()

    /**
     * Checks if a line looks like a header row that should be skipped.
     */
    fun isHeaderLine(line: String): Boolean {
        val trimmed = line.trim().lowercase(Locale.ROOT)
        if (trimmed.isBlank()) return true

        // If line contains a URL (http:// or https:// or magnet:), it's not a pure header
        if (trimmed.contains("http://") || trimmed.contains("https://") || trimmed.contains("magnet:")) {
            return false
        }

        val tokens = trimmed.split("[,|\\t\\s]+".toRegex()).filter { it.isNotBlank() }
        val matchingKeywords = tokens.count { token ->
            HEADER_KEYWORDS.any { kw -> token == kw || token.startsWith(kw) }
        }

        return matchingKeywords >= 1 && tokens.size <= 5
    }

    /**
     * Parses raw pasted text into structured ParsedBulkItem list.
     * Supports 3-column, 2-column, and 1-column formats with tab, comma, pipe, or space delimiters.
     */
    fun parse(rawText: String, defaultSeason: Int = 1, defaultStartEp: Int = 1): List<ParsedBulkItem> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<ParsedBulkItem>()
        var currentSequentialEp = defaultStartEp

        for (line in lines) {
            if (isHeaderLine(line)) continue

            // Determine delimiter: tab, pipe, or comma
            val parts = when {
                line.contains("\t") -> line.split("\t").map { it.trim() }
                line.contains("|") -> line.split("|").map { it.trim() }
                line.contains(",") && line.count { it == ',' } in 1..4 -> line.split(",").map { it.trim() }
                else -> listOf(line)
            }.filter { it.isNotBlank() }

            if (parts.isEmpty()) continue

            when (parts.size) {
                // 3 Columns: Season | Episode | URL
                3 -> {
                    val sNum = parts[0].filter { it.isDigit() }.toIntOrNull() ?: defaultSeason
                    val epNum = parts[1].filter { it.isDigit() }.toIntOrNull() ?: currentSequentialEp
                    val url = parts[2]
                    val epCode = String.format("S%02dE%02d", sNum, epNum)
                    result.add(ParsedBulkItem(sNum, epNum, epCode, url, isValid = isValidUrl(url)))
                }

                // 2 Columns: S01E01 | URL  or  1 | URL
                2 -> {
                    val first = parts[0]
                    val url = parts[1]

                    val sMatch = EP_REGEX.find(first)
                    if (sMatch != null) {
                        val sNum = sMatch.groupValues[1].toIntOrNull() ?: defaultSeason
                        val epNum = sMatch.groupValues[2].toIntOrNull() ?: currentSequentialEp
                        val epCode = String.format("S%02dE%02d", sNum, epNum)
                        result.add(ParsedBulkItem(sNum, epNum, epCode, url, isValid = isValidUrl(url)))
                    } else {
                        val epNum = first.filter { it.isDigit() }.toIntOrNull() ?: currentSequentialEp
                        val epCode = String.format("S%02dE%02d", defaultSeason, epNum)
                        result.add(ParsedBulkItem(defaultSeason, epNum, epCode, url, isValid = isValidUrl(url)))
                    }
                }

                // 1 Column / Raw Text: Try to extract S01E01 from URL or filename, otherwise sequential
                1 -> {
                    val url = parts[0]
                    val sMatch = EP_REGEX.find(url)
                    if (sMatch != null) {
                        val sNum = sMatch.groupValues[1].toIntOrNull() ?: defaultSeason
                        val epNum = sMatch.groupValues[2].toIntOrNull() ?: currentSequentialEp
                        val epCode = String.format("S%02dE%02d", sNum, epNum)
                        result.add(ParsedBulkItem(sNum, epNum, epCode, url, isValid = isValidUrl(url)))
                    } else {
                        val epNum = currentSequentialEp++
                        val epCode = String.format("S%02dE%02d", defaultSeason, epNum)
                        result.add(ParsedBulkItem(defaultSeason, epNum, epCode, url, isValid = isValidUrl(url)))
                    }
                }

                // More than 3 columns: Look for URL in the parts and extract season/episode from others
                else -> {
                    val urlIndex = parts.indexOfFirst { isValidUrl(it) }
                    val url = if (urlIndex != -1) parts[urlIndex] else parts.last()
                    val otherParts = parts.filterIndexed { idx, _ -> idx != urlIndex }

                    val sNum = otherParts.firstOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: defaultSeason
                    val epNum = otherParts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: currentSequentialEp++
                    val epCode = String.format("S%02dE%02d", sNum, epNum)
                    result.add(ParsedBulkItem(sNum, epNum, epCode, url, isValid = isValidUrl(url)))
                }
            }
        }

        return result
    }

    private fun isValidUrl(url: String): Boolean {
        val u = url.trim().lowercase(Locale.ROOT)
        return u.startsWith("http://") || u.startsWith("https://") || u.startsWith("magnet:")
    }
}

@Composable
fun BulkUrlDialog(
    initialTitle: String = "",
    onDismiss: () -> Unit,
    onSubmit: (title: String, season: Int, items: List<ParsedBulkItem>) -> Unit
) {
    val context = LocalContext.current
    var seriesTitle by remember { mutableStateOf(initialTitle) }
    var rawText by remember { mutableStateOf("") }
    var defaultSeason by remember { mutableIntStateOf(1) }

    val parsedItems = remember(rawText, defaultSeason) {
        BulkUrlParser.parse(rawText, defaultSeason = defaultSeason)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PitchBlack.copy(alpha = 0.92f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.5.dp, SketchBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BULK URL PARSER & DISPATCH",
                        color = Graphite100,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Graphite300)
                    }
                }

                // Series Title & Season Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = seriesTitle,
                        onValueChange = { seriesTitle = it },
                        label = { Text("Series Title (e.g. Breaking Bad)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )

                    OutlinedTextField(
                        value = defaultSeason.toString(),
                        onValueChange = { defaultSeason = it.filter { c -> c.isDigit() }.toIntOrNull() ?: 1 },
                        label = { Text("Season") },
                        singleLine = true,
                        modifier = Modifier.width(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )
                }

                // Paste Text Area with Paste Button
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        placeholder = {
                            Text(
                                "Paste lines from Excel / Sheets / TSV / Raw URLs:\n" +
                                "• 3-Col: Season 1 | Episode 1 | URL\n" +
                                "• 2-Col: S01E01 | URL\n" +
                                "• 1-Col: https://download.link/S01E01.mkv",
                                color = Graphite400,
                                fontSize = 11.5.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = SketchBorder,
                            focusedTextColor = Graphite100,
                            unfocusedTextColor = Graphite200
                        )
                    )

                    // Quick Paste from Clipboard
                    Button(
                        onClick = {
                            val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipText = clipMgr?.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clipText.isNullOrBlank()) {
                                rawText = clipText
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, EmeraldGreen),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .height(30.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PASTE", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Parsed Summary Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PARSED EPISODES (${parsedItems.size})",
                        color = EmeraldGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (parsedItems.isNotEmpty()) {
                        val validCount = parsedItems.count { it.isValid }
                        Text(
                            text = "$validCount valid • ${parsedItems.size - validCount} invalid",
                            color = Graphite400,
                            fontSize = 11.sp
                        )
                    }
                }

                // Parsed Preview List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PitchBlack)
                        .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (parsedItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No items parsed yet. Paste download links above.", color = Graphite400, fontSize = 11.sp)
                            }
                        }
                    } else {
                        items(parsedItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (item.isValid) EmeraldGreenDark else Color(0x33EF4444)
                                ) {
                                    Text(
                                        text = item.epCode,
                                        color = if (item.isValid) EmeraldGreen else DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = item.downloadUrl,
                                    color = Graphite200,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Cancel and Enqueue All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Graphite200)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (parsedItems.isNotEmpty()) {
                                val title = if (seriesTitle.isNotBlank()) seriesTitle else "Series " + System.currentTimeMillis()
                                onSubmit(title, defaultSeason, parsedItems)
                            }
                        },
                        enabled = parsedItems.isNotEmpty() && parsedItems.any { it.isValid },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("ENQUEUE ALL (${parsedItems.size})", color = PitchBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
