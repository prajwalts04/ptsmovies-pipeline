package com.pts.suite

import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.SeriesItem
import com.pts.suite.data.api.WatchlistItem
import com.pts.suite.ui.screens.stream.FuzzySearch
import org.junit.Assert.*
import org.junit.Test

class FuzzySearchTest {

    @Test
    fun testDiacriticsNormalization() {
        assertEquals("cafe", FuzzySearch.normalize("café"))
        assertEquals("motley crue", FuzzySearch.normalize("Mötley Crüe"))
        assertEquals("amelie", FuzzySearch.normalize("Amélie"))
        assertEquals("resume", FuzzySearch.normalize("résumé"))

        assertTrue(FuzzySearch.matches("cafe", "Café de Paris"))
        assertTrue(FuzzySearch.matches("amelie", "Amélie (2001)"))
    }

    @Test
    fun testRomanNumeralsBidirectionalConversion() {
        // Query with roman numeral matching arabic in title
        assertTrue(FuzzySearch.matches("KGF II", "KGF Chapter 2"))
        assertTrue(FuzzySearch.matches("Avatar II", "Avatar 2"))
        assertTrue(FuzzySearch.matches("John Wick IV", "John Wick Chapter 4"))

        // Query with arabic numeral matching roman in title
        assertTrue(FuzzySearch.matches("KGF 2", "K.G.F Chapter II"))
        assertTrue(FuzzySearch.matches("Rocky 4", "Rocky IV"))
        assertTrue(FuzzySearch.matches("Star Wars 7", "Star Wars VII"))
    }

    @Test
    fun testTokenConjunctionMatching() {
        val title = "The Lord of the Rings: The Fellowship of the Ring"
        val desc = "A meek Hobbit from the Shire and eight companions set out on a journey"
        val genres = listOf("Action", "Adventure", "Fantasy")

        // Multiple tokens present in title and description
        assertTrue(FuzzySearch.matches("lord rings fellowship", title, desc, genres))
        assertTrue(FuzzySearch.matches("rings hobbit adventure", title, desc, genres))
        assertTrue(FuzzySearch.matches("fellowship fantasy", title, desc, genres))

        // Non-matching token fails
        assertFalse(FuzzySearch.matches("lord rings space", title, desc, genres))
    }

    @Test
    fun testAcronymAndSubsequenceMatching() {
        // Acronym initials
        assertTrue(FuzzySearch.matches("kgf", "K.G.F: Chapter 1"))
        assertTrue(FuzzySearch.matches("got", "Game of Thrones"))
        assertTrue(FuzzySearch.matches("lotr", "Lord of the Rings"))

        // Punctuation stripped match
        assertTrue(FuzzySearch.matches("agents of shield", "Agents of S.H.I.E.L.D."))
        assertTrue(FuzzySearch.matches("s.h.i.e.l.d", "Agents of SHIELD"))
        assertTrue(FuzzySearch.matches("mr robot", "Mr. Robot"))
    }

    @Test
    fun testFilterMoviesAndSeries() {
        val movies = listOf(
            MovieItem("1", "KGF Chapter 2", "2022", "8.3", null, "Action blockbuster", listOf("Action", "Drama"), "/path/kgf2.mp4", "kgf2.mp4", "1.4 GB"),
            MovieItem("2", "Avatar: The Way of Water", "2022", "7.6", null, "Sci-Fi adventure", listOf("Action", "Sci-Fi"), "/path/avatar2.mp4", "avatar2.mp4", "2.1 GB"),
            MovieItem("3", "Oppenheimer", "2023", "8.9", null, "Biographical drama", listOf("Biography", "Drama"), "/path/oppenheimer.mp4", "oppenheimer.mp4", "1.8 GB")
        )

        val series = listOf(
            SeriesItem("s1", "Game of Thrones", "2011", "9.2", null, "Fantasy epic", listOf("Action", "Drama", "Fantasy")),
            SeriesItem("s2", "Breaking Bad", "2008", "9.5", null, "Crime drama", listOf("Crime", "Drama", "Thriller"))
        )

        // Fuzzy filter movies with roman numeral
        val kgfResults = FuzzySearch.filterMovies(movies, "KGF II")
        assertEquals(1, kgfResults.size)
        assertEquals("1", kgfResults[0].id)

        // Genre filter + query
        val dramaResults = FuzzySearch.filterMovies(movies, "Oppenheimer", "Drama")
        assertEquals(1, dramaResults.size)

        val nonMatchingGenre = FuzzySearch.filterMovies(movies, "Oppenheimer", "Sci-Fi")
        assertEquals(0, nonMatchingGenre.size)

        // Filter series with acronym
        val gotResults = FuzzySearch.filterSeries(series, "got")
        assertEquals(1, gotResults.size)
        assertEquals("s1", gotResults[0].id)

        // Watchlist filter
        val watchlist = listOf(
            WatchlistItem("1", null, "KGF Chapter 2", "Movie", "2022", null),
            WatchlistItem("s1", null, "Game of Thrones", "Series", "2011", null)
        )
        val watchResults = FuzzySearch.filterWatchlist(watchlist, "kgf")
        assertEquals(1, watchResults.size)
    }

    @Test
    fun testEdgeCases() {
        // Empty query matches all
        assertTrue(FuzzySearch.matches("", "Any Movie Title"))
        assertTrue(FuzzySearch.matches("   ", "Any Movie Title"))

        // Null and blank fields
        assertTrue(FuzzySearch.matches("", "", null, emptyList(), null))
        assertFalse(FuzzySearch.matches("nonexistent", "Avatar"))

        // Special characters
        assertTrue(FuzzySearch.matches("f&f", "F&F Fast and Furious"))
        assertTrue(FuzzySearch.matches("wall-e", "WALL-E (2008)"))
    }
}
