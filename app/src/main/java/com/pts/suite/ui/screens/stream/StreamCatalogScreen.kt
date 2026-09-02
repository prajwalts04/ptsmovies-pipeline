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

    val filteredMovies = remember(movies, searchQuery, selectedGenre) {
        movies.filter {
            (selectedGenre == "All" || it.genres.contains(selectedGenre)) &&
            (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
        }
    }

    val filteredSeries = remember(series, searchQuery, selectedGenre) {
        series.filter {
            (selectedGenre == "All" || it.genres.contains(selectedGenre)) &&
            (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
        }
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
            placeholder = { Text("Search movies, TV shows...", color = Graphite400, fontSize = 13.sp) },
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
            val tabs = listOf("all" to "All", "movies" to "Movies", "series" to "Series", "watchlist" to "Watchlist")
            tabs.forEach { (tabId, label) ->
                val isSelected = selectedTab == tabId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) EmeraldGreen else DarkSurface)
                        .border(1.dp, if (isSelected) EmeraldGreen else SketchBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedTab = tabId }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) PitchBlack else Graphite200,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Horizontal Genre Filter Chips
        if (allGenres.size > 1 && searchQuery.isEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(allGenres) { genre ->
                    val isGenSel = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isGenSel) Graphite200 else DarkSurfaceElevated)
                            .border(1.dp, if (isGenSel) Graphite100 else SketchBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedGenre = genre }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = genre,
                            color = if (isGenSel) PitchBlack else Graphite400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Main Media Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Optional Hero Banner on 'All' tab when not searching
            if (selectedTab == "all" && searchQuery.isBlank() && selectedGenre == "All" && featuredItem != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SketchBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectMovie(featuredItem) }
                    ) {
                        AsyncImage(
                            model = featuredItem.poster,
                            contentDescription = featuredItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, PitchBlack.copy(alpha = 0.9f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "FEATURED MOVIE",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = featuredItem.title,
                                color = Graphite100,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "★ ${featuredItem.rating}",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = featuredItem.year ?: "", color = Graphite400, fontSize = 12.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldGreen)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("PLAY", color = PitchBlack, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Grid Items
            when (selectedTab) {
                "all" -> {
                    items(filteredMovies) { movie ->
                        MediaPosterCard(
                            title = movie.title,
                            year = movie.year,
                            rating = movie.rating,
                            poster = movie.poster,
                            type = "Movie",
                            onClick = { onSelectMovie(movie) }
                        )
                    }
                    items(filteredSeries) { ser ->
                        MediaPosterCard(
                            title = ser.title,
                            year = ser.year,
                            rating = ser.rating,
                            poster = ser.poster,
                            type = "Series (${ser.totalEpisodes} eps)",
                            onClick = { onSelectSeries(ser) }
                        )
                    }
                }
                "movies" -> {
                    items(filteredMovies) { movie ->
                        MediaPosterCard(
                            title = movie.title,
                            year = movie.year,
                            rating = movie.rating,
                            poster = movie.poster,
                            type = "Movie",
                            onClick = { onSelectMovie(movie) }
                        )
                    }
                }
                "series" -> {
                    items(filteredSeries) { ser ->
                        MediaPosterCard(
                            title = ser.title,
                            year = ser.year,
                            rating = ser.rating,
                            poster = ser.poster,
                            type = "Series (${ser.totalEpisodes} eps)",
                            onClick = { onSelectSeries(ser) }
                        )
                    }
                }
                "watchlist" -> {
                    val watchIds = watchlist.map { it.title.lowercase() }
                    val watchMovies = movies.filter { watchIds.contains(it.title.lowercase()) }
                    val watchSeries = series.filter { watchIds.contains(it.title.lowercase()) }

                    items(watchMovies) { movie ->
                        MediaPosterCard(
                            title = movie.title,
                            year = movie.year,
                            rating = movie.rating,
                            poster = movie.poster,
                            type = "Movie",
                            onClick = { onSelectMovie(movie) }
                        )
                    }
                    items(watchSeries) { ser ->
                        MediaPosterCard(
                            title = ser.title,
                            year = ser.year,
                            rating = ser.rating,
                            poster = ser.poster,
                            type = "Series",
                            onClick = { onSelectSeries(ser) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPosterCard(
    title: String,
    year: String?,
    rating: String?,
    poster: String?,
    type: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(Graphite800)
        ) {
            if (!poster.isNullOrEmpty()) {
                AsyncImage(
                    model = poster,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Graphite400,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }

            // Rating Badge (top-right)
            if (!rating.isNullOrEmpty() && rating != "N/A") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PitchBlack.copy(alpha = 0.85f))
                        .border(1.dp, SketchBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "★ $rating",
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Type Badge (top-left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurface.copy(alpha = 0.85f))
                    .border(1.dp, SketchBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = type.uppercase(),
                    color = EmeraldGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Title and Year
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                color = Graphite100,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!year.isNullOrEmpty()) {
                Text(
                    text = year,
                    color = Graphite400,
                    fontSize = 11.sp
                )
            }
        }
    }
}
