package com.pts.suite.ui.screens.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pts.suite.data.api.MovieItem
import com.pts.suite.data.api.SeriesItem
import com.pts.suite.data.api.WatchlistItem
import com.pts.suite.ui.components.DockTabItem
import com.pts.suite.ui.components.DynamicBottomDock
import com.pts.suite.ui.theme.*

@Composable
fun StreamCatalogScreen(
    movies: List<MovieItem>,
    series: List<SeriesItem>,
    watchlist: List<WatchlistItem>,
    onSelectMovie: (MovieItem) -> Unit,
    onSelectSeries: (SeriesItem) -> Unit
) {
    var selectedTab by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }

    val streamTabs = listOf(
        DockTabItem("all", "All", Icons.Default.Layers),
        DockTabItem("movies", "Movies", Icons.Default.Movie),
        DockTabItem("series", "Series", Icons.Default.Tv),
        DockTabItem("watchlist", "Watchlist", Icons.Default.Bookmark)
    )

    // Filter items
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

    Scaffold(
        bottomBar = {
            DynamicBottomDock(
                tabs = streamTabs,
                selectedTabId = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = PitchBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search movies, shows, or actors...", color = Graphite400, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Graphite300) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Graphite100,
                    unfocusedBorderColor = SketchBorder,
                    focusedTextColor = Graphite100,
                    unfocusedTextColor = Graphite200
                )
            )

            // Media Grid Content
            when (selectedTab) {
                "all", "movies" -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (selectedTab == "all" || selectedTab == "movies") {
                            items(filteredMovies) { movie ->
                                MediaPosterCard(
                                    title = movie.title,
                                    posterUrl = movie.poster,
                                    year = movie.year,
                                    rating = movie.rating,
                                    badge = "Movie",
                                    onClick = { onSelectMovie(movie) }
                                )
                            }
                        }
                        if (selectedTab == "all") {
                            items(filteredSeries) { show ->
                                MediaPosterCard(
                                    title = show.title,
                                    posterUrl = show.poster,
                                    year = show.year,
                                    rating = show.rating,
                                    badge = "${show.seasons.size} Seasons",
                                    onClick = { onSelectSeries(show) }
                                )
                            }
                        }
                    }
                }
                "series" -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredSeries) { show ->
                            MediaPosterCard(
                                title = show.title,
                                posterUrl = show.poster,
                                year = show.year,
                                rating = show.rating,
                                badge = "${show.seasons.size} Seasons",
                                onClick = { onSelectSeries(show) }
                            )
                        }
                    }
                }
                "watchlist" -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchlist) { item ->
                            MediaPosterCard(
                                title = item.title,
                                posterUrl = item.poster,
                                year = item.year,
                                rating = null,
                                badge = item.type,
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPosterCard(
    title: String,
    posterUrl: String?,
    year: String?,
    rating: String?,
    badge: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, SketchBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        // Poster Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(DarkSurfaceElevated)
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Badge Pill
            Text(
                text = badge,
                color = PitchBlack,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Graphite100)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Title and Meta
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                color = Graphite100,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = year ?: "", color = Graphite400, fontSize = 11.sp)
                if (!rating.isNullOrBlank()) {
                    Text(text = "★ $rating", color = GoldenYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
