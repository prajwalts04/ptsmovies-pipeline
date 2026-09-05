package com.pts.suite.ui.screens.stream

import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.SeriesItem
import com.pts.suite.data.api.WatchlistItem
import java.text.Normalizer
import java.util.Locale

object FuzzySearch {

    private val ROMAN_TO_INT = mapOf(
        "i" to "1",
        "ii" to "2",
        "iii" to "3",
        "iv" to "4",
        "v" to "5",
        "vi" to "6",
        "vii" to "7",
        "viii" to "8",
        "ix" to "9",
        "x" to "10",
        "xi" to "11",
        "xii" to "12",
        "xiii" to "13",
        "xiv" to "14",
        "xv" to "15",
        "xvi" to "16",
        "xvii" to "17",
        "xviii" to "18",
        "xix" to "19",
        "xx" to "20"
    )

    private val INT_TO_ROMAN = ROMAN_TO_INT.entries.associate { (k, v) -> v to k }

    /**
     * Normalizes text by removing diacritical marks (accents), converting to lowercase,
     * and trimming extra whitespaces.
     */
    fun normalize(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val nfd = Normalizer.normalize(input, Normalizer.Form.NFD)
        return nfd.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .trim()
    }

    /**
     * Strips all non-alphanumeric characters for compact acronym and subsequence comparison.
     */
    fun stripPunctuation(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return normalize(input).replace("[^a-z0-9]".toRegex(), "")
    }

    /**
     * Expands text with normalized roman numerals and arabic numerals for bidirectional matching.
     * E.g. "KGF Chapter 2" -> generates forms with "2" and "ii".
     */
    fun expandNumerals(input: String): List<String> {
        val normalized = normalize(input)
        val tokens = normalized.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return listOf(normalized)

        val romanVersion = tokens.joinToString(" ") { token ->
            ROMAN_TO_INT[token] ?: token
        }
        val intVersion = tokens.joinToString(" ") { token ->
            INT_TO_ROMAN[token] ?: token
        }

        return listOf(normalized, romanVersion, intVersion).distinct()
    }

    /**
     * Extracts acronym from multi-word title (e.g. "Game of Thrones" -> "got", "K.G.F" -> "kgf").
     */
    fun extractAcronym(input: String): String {
        val normalized = normalize(input)
        val words = normalized.split("[^a-z0-9]+".toRegex()).filter { it.isNotBlank() }
        return words.mapNotNull { it.firstOrNull() }.joinToString("")
    }

    /**
     * Checks if a target text matches the search query using multi-layer fuzzy heuristics:
     * 1. Direct contains (case & accent normalized)
     * 2. Roman/Arabic numeral expansion
     * 3. Multi-token conjunction match (all query tokens present in target)
     * 4. Punctuation-stripped compact match
     * 5. Acronym match
     */
    fun matches(
        query: String,
        title: String,
        description: String? = null,
        genres: List<String> = emptyList(),
        year: String? = null
    ): Boolean {
        val cleanQuery = normalize(query)
        if (cleanQuery.isBlank()) return true

        val queryTokens = cleanQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val titleExpansions = expandNumerals(title)
        val descNorm = normalize(description)
        val genresNorm = genres.map { normalize(it) }.joinToString(" ")
        val yearNorm = normalize(year)

        // Combined searchable content
        val combinedFields = listOf(
            titleExpansions.joinToString(" "),
            descNorm,
            genresNorm,
            yearNorm
        ).joinToString(" ")

        val queryExpansions = expandNumerals(cleanQuery)

        // Check 1: Direct substring in any expansion
        for (qExp in queryExpansions) {
            for (tExp in titleExpansions) {
                if (tExp.contains(qExp)) return true
            }
            if (combinedFields.contains(qExp)) return true
        }

        // Check 2: All query tokens present in combined searchable text
        val allTokensPresent = queryTokens.all { qToken ->
            val qTokenNum = ROMAN_TO_INT[qToken] ?: INT_TO_ROMAN[qToken] ?: qToken
            combinedFields.contains(qToken) || combinedFields.contains(qTokenNum)
        }
        if (allTokensPresent) return true

        // Check 3: Punctuation-stripped compact match (e.g. "s.h.i.e.l.d" vs "shield")
        val compactQuery = stripPunctuation(cleanQuery)
        val compactTitle = stripPunctuation(title)
        if (compactQuery.length >= 2 && compactTitle.contains(compactQuery)) return true

        // Check 4: Acronym matching (e.g. "kgf" matches "K.G.F Chapter 1" or "got" matches "Game of Thrones")
        val titleAcronym = extractAcronym(title)
        if (compactQuery.length >= 2 && (titleAcronym == compactQuery || titleAcronym.contains(compactQuery))) {
            return true
        }

        return false
    }

    /**
     * Filter movies list by query and genre.
     */
    fun filterMovies(
        movies: List<MovieItem>,
        query: String,
        selectedGenre: String = "All"
    ): List<MovieItem> {
        val genreFilter = if (selectedGenre.equals("All", ignoreCase = true)) null else selectedGenre
        return movies.filter { movie ->
            val genreMatch = genreFilter == null || movie.genres.any { it.equals(genreFilter, ignoreCase = true) }
            val queryMatch = matches(query, movie.title, movie.description, movie.genres, movie.year)
            genreMatch && queryMatch
        }
    }

    /**
     * Filter series list by query and genre.
     */
    fun filterSeries(
        series: List<SeriesItem>,
        query: String,
        selectedGenre: String = "All"
    ): List<SeriesItem> {
        val genreFilter = if (selectedGenre.equals("All", ignoreCase = true)) null else selectedGenre
        return series.filter { show ->
            val genreMatch = genreFilter == null || show.genres.any { it.equals(genreFilter, ignoreCase = true) }
            val queryMatch = matches(query, show.title, show.description, show.genres, show.year)
            genreMatch && queryMatch
        }
    }

    /**
     * Filter watchlist items by query.
     */
    fun filterWatchlist(
        watchlist: List<WatchlistItem>,
        query: String
    ): List<WatchlistItem> {
        if (query.isBlank()) return watchlist
        return watchlist.filter { item ->
            matches(query, item.title, year = item.year)
        }
    }
}
