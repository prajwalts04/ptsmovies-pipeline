package com.pts.suite.ui.screens.ssh

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.pts.suite.ui.theme.*

/**
 * AnsiParser provides full ANSI escape sequence parsing, SGR color codes,
 * 256-color palette, TrueColor RGB, and terminal buffer manipulation.
 */
object AnsiParser {

    // Standard 8 ANSI Colors
    val ANSI_BLACK = Color(0xFF000000)
    val ANSI_RED = Color(0xFFEF4444)
    val ANSI_GREEN = Color(0xFF22C55E)
    val ANSI_YELLOW = Color(0xFFEAB308)
    val ANSI_BLUE = Color(0xFF3B82F6)
    val ANSI_MAGENTA = Color(0xFFA855F7)
    val ANSI_CYAN = Color(0xFF06B6D4)
    val ANSI_WHITE = Color(0xFFE2E8F0)

    // Bright 8 ANSI Colors
    val ANSI_BRIGHT_BLACK = Color(0xFF64748B)
    val ANSI_BRIGHT_RED = Color(0xFFF87171)
    val ANSI_BRIGHT_GREEN = Color(0xFF4ADE80)
    val ANSI_BRIGHT_YELLOW = Color(0xFFFACC15)
    val ANSI_BRIGHT_BLUE = Color(0xFF60A5FA)
    val ANSI_BRIGHT_MAGENTA = Color(0xFFC084FC)
    val ANSI_BRIGHT_CYAN = Color(0xFF22D3EE)
    val ANSI_BRIGHT_WHITE = Color(0xFFFFFFFF)

    val DEFAULT_FG = Graphite100
    val DEFAULT_BG = Color.Transparent

    /**
     * Resolves 256-color palette index (0-255) to Compose Color.
     */
    fun color256(index: Int): Color {
        return when (index) {
            0 -> ANSI_BLACK
            1 -> ANSI_RED
            2 -> ANSI_GREEN
            3 -> ANSI_YELLOW
            4 -> ANSI_BLUE
            5 -> ANSI_MAGENTA
            6 -> ANSI_CYAN
            7 -> ANSI_WHITE
            8 -> ANSI_BRIGHT_BLACK
            9 -> ANSI_BRIGHT_RED
            10 -> ANSI_BRIGHT_GREEN
            11 -> ANSI_BRIGHT_YELLOW
            12 -> ANSI_BRIGHT_BLUE
            13 -> ANSI_BRIGHT_MAGENTA
            14 -> ANSI_BRIGHT_CYAN
            15 -> ANSI_BRIGHT_WHITE
            in 16..231 -> {
                // 6x6x6 color cube
                val i = index - 16
                val r = (i / 36) * 51
                val g = ((i % 36) / 6) * 51
                val b = (i % 6) * 51
                Color(r, g, b)
            }
            in 232..255 -> {
                // Grayscale ramp from index 232 (darkest) to 255 (lightest)
                val gray = (index - 232) * 10 + 8
                Color(gray, gray, gray)
            }
            else -> DEFAULT_FG
        }
    }

    /**
     * Parses raw terminal output string containing ANSI escape codes
     * into an AnnotatedString with color and style spans.
     */
    fun parseToAnnotatedString(rawText: String): AnnotatedString {
        return buildAnnotatedString {
            var currentFg: Color = DEFAULT_FG
            var currentBg: Color = DEFAULT_BG
            var isBold = false
            var isItalic = false
            var isUnderline = false
            var isStrikethrough = false
            var isDim = false

            fun getCurrentSpanStyle(): SpanStyle {
                return SpanStyle(
                    color = if (isDim) currentFg.copy(alpha = 0.5f) else currentFg,
                    background = currentBg,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = when {
                        isUnderline && isStrikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                        isUnderline -> TextDecoration.Underline
                        isStrikethrough -> TextDecoration.LineThrough
                        else -> TextDecoration.None
                    }
                )
            }

            var i = 0
            val len = rawText.length

            while (i < len) {
                val c = rawText[i]

                if (c == '\u001B' && i + 1 < len && rawText[i + 1] == '[') {
                    // Start of CSI sequence
                    var j = i + 2
                    while (j < len && (rawText[j] in '0'..'9' || rawText[j] == ';' || rawText[j] == '?')) {
                        j++
                    }
                    if (j < len) {
                        val commandChar = rawText[j]
                        val paramStr = rawText.substring(i + 2, j)

                        if (commandChar == 'm') {
                            // SGR (Select Graphic Rendition)
                            val params = if (paramStr.isEmpty()) {
                                listOf(0)
                            } else {
                                paramStr.split(';').mapNotNull { it.toIntOrNull() }
                            }

                            var pIdx = 0
                            while (pIdx < params.size) {
                                when (val p = params[pIdx]) {
                                    0 -> {
                                        // Reset
                                        currentFg = DEFAULT_FG
                                        currentBg = DEFAULT_BG
                                        isBold = false
                                        isItalic = false
                                        isUnderline = false
                                        isStrikethrough = false
                                        isDim = false
                                    }
                                    1 -> isBold = true
                                    2 -> isDim = true
                                    3 -> isItalic = true
                                    4 -> isUnderline = true
                                    9 -> isStrikethrough = true
                                    22 -> { isBold = false; isDim = false }
                                    23 -> isItalic = false
                                    24 -> isUnderline = false
                                    29 -> isStrikethrough = false

                                    // Standard Foreground
                                    30 -> currentFg = ANSI_BLACK
                                    31 -> currentFg = ANSI_RED
                                    32 -> currentFg = ANSI_GREEN
                                    33 -> currentFg = ANSI_YELLOW
                                    34 -> currentFg = ANSI_BLUE
                                    35 -> currentFg = ANSI_MAGENTA
                                    36 -> currentFg = ANSI_CYAN
                                    37 -> currentFg = ANSI_WHITE
                                    39 -> currentFg = DEFAULT_FG

                                    // Standard Background
                                    40 -> currentBg = ANSI_BLACK
                                    41 -> currentBg = ANSI_RED
                                    42 -> currentBg = ANSI_GREEN
                                    43 -> currentBg = ANSI_YELLOW
                                    44 -> currentBg = ANSI_BLUE
                                    45 -> currentBg = ANSI_MAGENTA
                                    46 -> currentBg = ANSI_CYAN
                                    47 -> currentBg = ANSI_WHITE
                                    49 -> currentBg = DEFAULT_BG

                                    // Bright Foreground
                                    90 -> currentFg = ANSI_BRIGHT_BLACK
                                    91 -> currentFg = ANSI_BRIGHT_RED
                                    92 -> currentFg = ANSI_BRIGHT_GREEN
                                    93 -> currentFg = ANSI_BRIGHT_YELLOW
                                    94 -> currentFg = ANSI_BRIGHT_BLUE
                                    95 -> currentFg = ANSI_BRIGHT_MAGENTA
                                    96 -> currentFg = ANSI_BRIGHT_CYAN
                                    97 -> currentFg = ANSI_BRIGHT_WHITE

                                    // Bright Background
                                    100 -> currentBg = ANSI_BRIGHT_BLACK
                                    101 -> currentBg = ANSI_BRIGHT_RED
                                    102 -> currentBg = ANSI_BRIGHT_GREEN
                                    103 -> currentBg = ANSI_BRIGHT_YELLOW
                                    104 -> currentBg = ANSI_BRIGHT_BLUE
                                    105 -> currentBg = ANSI_BRIGHT_MAGENTA
                                    106 -> currentBg = ANSI_BRIGHT_CYAN
                                    107 -> currentBg = ANSI_BRIGHT_WHITE

                                    // Extended 256 or TrueColor Foreground (38;5;N or 38;2;R;G;B)
                                    38 -> {
                                        if (pIdx + 2 < params.size && params[pIdx + 1] == 5) {
                                            currentFg = color256(params[pIdx + 2])
                                            pIdx += 2
                                        } else if (pIdx + 4 < params.size && params[pIdx + 1] == 2) {
                                            currentFg = Color(params[pIdx + 2], params[pIdx + 3], params[pIdx + 4])
                                            pIdx += 4
                                        }
                                    }

                                    // Extended 256 or TrueColor Background (48;5;N or 48;2;R;G;B)
                                    48 -> {
                                        if (pIdx + 2 < params.size && params[pIdx + 1] == 5) {
                                            currentBg = color256(params[pIdx + 2])
                                            pIdx += 2
                                        } else if (pIdx + 4 < params.size && params[pIdx + 1] == 2) {
                                            currentBg = Color(params[pIdx + 2], params[pIdx + 3], params[pIdx + 4])
                                            pIdx += 4
                                        }
                                    }
                                }
                                pIdx++
                            }
                        }
                        i = j + 1
                        continue
                    }
                }

                // Append visible or printable character with active style
                if (c != '\r') {
                    pushStyle(getCurrentSpanStyle())
                    append(c)
                    pop()
                }
                i++
            }
        }
    }
}

/**
 * TerminalBuffer maintains scrollback buffer history (up to maxLines)
 * and processes incoming PTY raw stream chunks.
 */
class TerminalBuffer(val maxLines: Int = 2000) {
    private val rawLines = mutableListOf<String>()
    private var currentLineBuilder = StringBuilder()

    fun append(text: String) {
        synchronized(this) {
            for (c in text) {
                if (c == '\n') {
                    rawLines.add(currentLineBuilder.toString())
                    currentLineBuilder.clear()
                    if (rawLines.size > maxLines) {
                        rawLines.removeAt(0)
                    }
                } else if (c == '\u000C') {
                    // Form Feed / Clear screen (Ctrl+L)
                    rawLines.clear()
                    currentLineBuilder.clear()
                } else if (c != '\r') {
                    currentLineBuilder.append(c)
                }
            }
        }
    }

    fun clear() {
        synchronized(this) {
            rawLines.clear()
            currentLineBuilder.clear()
        }
    }

    fun getLines(): List<String> {
        synchronized(this) {
            val list = ArrayList<String>(rawLines.size + 1)
            list.addAll(rawLines)
            if (currentLineBuilder.isNotEmpty()) {
                list.add(currentLineBuilder.toString())
            }
            return list
        }
    }

    fun getFormattedLines(): List<AnnotatedString> {
        return getLines().map { AnsiParser.parseToAnnotatedString(it) }
    }
}
