package com.pts.suite.ui.screens.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.SeriesItem
import com.pts.suite.data.api.WatchlistItem
import com.pts.suite.ui.theme.*

@Composable
fun StreamCatalogScreen(
    movies: List<MovieItem>,
    series: List<SeriesItem>,
    watchlist: List<WatchlistItem>,
    onSelectMovie: (MovieItem) -> Unit,
    onSelectSeries: (SeriesItem) -> Unit,
    onToggleWatchlist: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }

    val allGenres = remember(movies, series) {
        val set = sortedSetOf("All")
        movies.forEach { set.addAll(it.genres) }
        series.forEach { set.addAll(it.genres) }
        set.toList()
    }

    // Multi-layer fuzzy search filtered lists
    val filteredMovies = remember(movies, searchQuery, selectedGenre) {
        FuzzySearch.filterMovies(movies, searchQuery, selectedGenre)
    }

    val filteredSeries = remember(series, searchQuery, selectedGenre) {
        FuzzySearch.filterSeries(series, searchQuery, selectedGenre)
    }

    val filteredWatchlist = remember(watchlist, searchQuery) {
        FuzzySearch.filterWatchlist(watchlist, searchQuery)
    }

    val watchlistIds = remember(watchlist) {
        watchlist.map { it.id }.toSet()
    }

    val featuredItem = remember(movies) { movies.firstOrNull() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp)
    ) {
        // Search Bar with Pitch Black Sketch styling
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search movies, series, cast, acronyms...", color = Graphite400, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Graphite300) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Graphite400)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = SketchBorder,
                focusedTextColor = Graphite100,
                unfocusedTextColor = Graphite200,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            )
        )

        // Top Category Pills: All, Movies, Series, Watchlist
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "all" to "All (${filteredMovies.size + filteredSeries.size})",
                "movies" to "Movies (${filteredMovies.size})",
                "series" to "Series (${filteredSeries.size})",
                "watchlist" to "Watchlist (${watchlist.size})"
            )
            tabs.forEach { (tabId, label) ->
                val isSelected = selectedTab == tabId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) EmeraldGreen else DarkSurface)
                        .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedTab = tabId }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) PitchBlack else Graphite200,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Genre filter row (when not on watchlist tab)
        if (selectedTab != "watchlist" && allGenres.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(allGenres) { genre ->
                    val isSelected = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Graphite800 else DarkSurface)
                            .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedGenre = genre }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) EmeraldGreen else Graphite300,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Media Grid Layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Featured Banner (on All tab when no search query)
            if (selectedTab == "all" && searchQuery.isBlank() && selectedGenre == "All" && featuredItem != null) {
                item(span = { GridItemSpan(2) }) {
                    FeaturedBanner(
                        movie = featuredItem,
                        isInWatchlist = watchlistIds.contains(featuredItem.id),
                        onPlay = { onSelectMovie(featuredItem) },
                        onToggleWatchlist = { onToggleWatchlist(featuredItem.id, "Movie") }
                    )
                }
            }

            when (selectedTab) {
                "all" -> {
                    // Combine movies and series
                    val combinedList = mutableListOf<Any>()
                    combinedList.addAll(filteredMovies)
                    combinedList.addAll(filteredSeries)

                    if (combinedList.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptySearchState(query = searchQuery)
                        }
                    } else {
                        items(filteredMovies, key = { "mov_${it.id}" }) { movie ->
                            MovieCard(
                                movie = movie,
                                isInWatchlist = watchlistIds.contains(movie.id),
                                onClick = { onSelectMovie(movie) },
                                onToggleWatchlist = { onToggleWatchlist(movie.id, "Movie") }
                            )
                        }
                        items(filteredSeries, key = { "ser_${it.id}" }) { show ->
                            SeriesCard(
                                series = show,
                                isInWatchlist = watchlistIds.contains(show.id),
                                onClick = { onSelectSeries(show) },
                                onToggleWatchlist = { onToggleWatchlist(show.id, "Series") }
                            )
                        }
                    }
                }

                "movies" -> {
                    if (filteredMovies.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptySearchState(query = searchQuery)
                        }
                    } else {
                        items(filteredMovies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                isInWatchlist = watchlistIds.contains(movie.id),
                                onClick = { onSelectMovie(movie) },
                                onToggleWatchlist = { onToggleWatchlist(movie.id, "Movie") }
                            )
                        }
                    }
                }

                "series" -> {
                    if (filteredSeries.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptySearchState(query = searchQuery)
                        }
                    } else {
                        items(filteredSeries, key = { it.id }) { show ->
                            SeriesCard(
                                series = show,
                                isInWatchlist = watchlistIds.contains(show.id),
                                onClick = { onSelectSeries(show) },
                                onToggleWatchlist = { onToggleWatchlist(show.id, "Series") }
                            )
                        }
                    }
                }

                "watchlist" -> {
                    if (filteredWatchlist.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = Graphite400,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Your watchlist is empty", color = Graphite300, fontSize = 14.sp)
                                    Text("Bookmark movies and series to watch later", color = Graphite400, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredWatchlist, key = { it.id }) { item ->
                            WatchlistCard(
                                item = item,
                                onClick = {
                                    if (item.type.equals("Series", ignoreCase = true)) {
                                        val targetSeries = series.find { it.id == item.id || it.title == item.title }
                                        if (targetSeries != null) onSelectSeries(targetSeries)
                                    } else {
                                        val targetMovie = movies.find { it.id == item.id || it.title == item.title }
                                        if (targetMovie != null) onSelectMovie(targetMovie)
                                    }
                                },
                                onRemove = { onToggleWatchlist(item.id, item.type) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedBanner(
    movie: MovieItem,
    isInWatchlist: Boolean,
    onPlay: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, EmeraldGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onPlay() }
    ) {
        AsyncImage(
            model = movie.poster,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PitchBlack.copy(alpha = 0.95f)),
                        startY = 60f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldGreen
                ) {
                    Text(
                        text = "FEATURED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PitchBlack,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!movie.year.isNullOrBlank()) {
                    Text(text = movie.year, color = Graphite300, fontSize = 11.sp)
                }

                if (!movie.rating.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldenYellow, modifier = Modifier.size(12.dp))
                        Text(text = " ${movie.rating}", color = GoldenYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = movie.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Graphite100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!movie.description.isNullOrBlank()) {
                Text(
                    text = movie.description,
                    fontSize = 11.sp,
                    color = Graphite300,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: MovieItem,
    isInWatchlist: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.5.dp, SketchBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Graphite900)
        ) {
            AsyncImage(
                model = movie.poster,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Badges: Type tag & Watchlist button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DarkSurface.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, SketchBorder)
                ) {
                    Text(
                        text = "Movie",
                        color = Graphite200,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .clickable { onToggleWatchlist() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.BookmarkCheck else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (isInWatchlist) EmeraldGreen else Graphite300,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Bottom Rating Badge
            if (!movie.rating.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldenYellow, modifier = Modifier.size(11.dp))
                    Text(text = " ${movie.rating}", color = GoldenYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Title and Year Info
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = movie.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Graphite100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = movie.year ?: "Movie",
                    fontSize = 11.sp,
                    color = Graphite400
                )
                if (movie.genres.isNotEmpty()) {
                    Text(
                        text = movie.genres.first(),
                        fontSize = 11.sp,
                        color = Graphite400,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesCard(
    series: SeriesItem,
    isInWatchlist: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.5.dp, SketchBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Graphite900)
        ) {
            AsyncImage(
                model = series.poster,
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldGreenDark,
                    border = BorderStroke(0.5.dp, EmeraldGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Series",
                        color = EmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .clickable { onToggleWatchlist() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.BookmarkCheck else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (isInWatchlist) EmeraldGreen else Graphite300,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (!series.rating.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurface.copy(alpha = 0.9f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldenYellow, modifier = Modifier.size(11.dp))
                    Text(text = " ${series.rating}", color = GoldenYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = series.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Graphite100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${series.seasons.size} Season${if (series.seasons.size > 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = Graphite400
                )
                if (series.totalEpisodes > 0) {
                    Text(
                        text = "${series.totalEpisodes} Eps",
                        fontSize = 11.sp,
                        color = Graphite400
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.5.dp, SketchBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Graphite900)
        ) {
            AsyncImage(
                model = item.poster,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DarkSurface.copy(alpha = 0.85f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = "Remove from Watchlist",
                    tint = DangerRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Graphite100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.type,
                    fontSize = 11.sp,
                    color = EmeraldGreen
                )
                if (!item.year.isNullOrBlank()) {
                    Text(
                        text = item.year,
                        fontSize = 11.sp,
                        color = Graphite400
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = Graphite400,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("No results for \"$query\"", color = Graphite300, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Try searching by alternative title, acronym, or year", color = Graphite400, fontSize = 12.sp)
        }
    }
}
