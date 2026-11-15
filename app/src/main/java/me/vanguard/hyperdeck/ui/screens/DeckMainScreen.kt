package me.vanguard.hyperdeck.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import me.vanguard.hyperdeck.data.api.MediaEngine
import me.vanguard.hyperdeck.data.model.VideoItem
import me.vanguard.hyperdeck.ui.components.HyperPlayer
import me.vanguard.hyperdeck.ui.theme.*

@Composable
fun DeckMainScreen(
    initialVideoId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mediaEngine = remember { MediaEngine() }
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Vault (CD Rack), 1: Search
    var inputUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var activeVideo by remember { mutableStateOf<VideoItem?>(null) }
    var searchResults by remember { mutableStateOf<List<VideoItem>>(emptyList()) }

    // Curated Library ("The Vault / CD Rack")
    val vaultItems = remember {
        mutableStateListOf(
            VideoItem(
                id = "dQw4w9WgXcQ",
                title = "Rick Astley - Never Gonna Give You Up",
                author = "Rick Astley",
                durationSeconds = 212,
                thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
            ),
            VideoItem(
                id = "jfKfPfyJRdk",
                title = "lofi hip hop radio - beats to relax/study to",
                author = "Lofi Girl",
                durationSeconds = 0,
                thumbnailUrl = "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg"
            )
        )
    }

    LaunchedEffect(initialVideoId) {
        initialVideoId?.let { id ->
            val newItem = VideoItem(
                id = id,
                title = "Shared Video ($id)",
                author = "YouTube",
                thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg"
            )
            vaultItems.add(0, newItem)
            activeVideo = newItem
            coroutineScope.launch {
                val metadata = mediaEngine.fetchVideoMetadata(id)
                metadata.onSuccess { meta ->
                    val index = vaultItems.indexOfFirst { it.id == id }
                    if (index >= 0) {
                        vaultItems[index] = meta
                        if (activeVideo?.id == id) activeVideo = meta
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(HyperBlack)
    ) {
        // Tablet Side Navigation Rail
        if (isTablet) {
            NavigationRail(
                containerColor = HyperSurface,
                modifier = Modifier.width(84.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = "HyperDeck Logo",
                    tint = HyperAccent,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))

                NavigationRailItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DiscFull, "Vault") },
                    label = { Text("Vault", fontSize = 11.sp) }
                )
                NavigationRailItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, "Search") },
                    label = { Text("Discover", fontSize = 11.sp) }
                )
            }
        }

        // Main Content Area
        Scaffold(
            containerColor = HyperBlack,
            bottomBar = {
                if (!isTablet) {
                    NavigationBar(
                        containerColor = HyperSurface,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.DiscFull, "Vault") },
                            label = { Text("Vault") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Search, "Search") },
                            label = { Text("Discover") }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "HYPERDECK",
                            color = HyperTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Zero-Algo Media Vault",
                            color = HyperTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (isSearching) {
                        CircularProgressIndicator(
                            color = HyperAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Burn / Link Importer Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HyperSurface)
                        .border(1.dp, HyperCardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Paste",
                        tint = HyperAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        placeholder = { Text("Burn YouTube URL to Vault...", color = HyperTextSecondary, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = HyperTextPrimary,
                            unfocusedTextColor = HyperTextPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            val clean = inputUrl.trim()
                            if (clean.isNotBlank()) {
                                val id = MediaEngine.extractVideoId(clean)
                                if (id.length in 10..15) {
                                    val newDisc = VideoItem(
                                        id = id,
                                        title = "Loading Metadata...",
                                        thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg"
                                    )
                                    vaultItems.add(0, newDisc)
                                    activeVideo = newDisc
                                    inputUrl = ""
                                    Toast.makeText(context, "Burning disc to Vault...", Toast.LENGTH_SHORT).show()

                                    coroutineScope.launch {
                                        val meta = mediaEngine.fetchVideoMetadata(id)
                                        meta.onSuccess { item ->
                                            val index = vaultItems.indexOfFirst { it.id == id }
                                            if (index >= 0) {
                                                vaultItems[index] = item
                                                if (activeVideo?.id == id) activeVideo = item
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter a valid YouTube link or ID", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, "Burn", tint = HyperAccent)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content View (Adaptive Dual-Pane on Tablet if playing)
                if (isTablet && activeVideo != null) {
                    // TABLET DUAL-PANE VIEW
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Pane: Active Deck Player (52% width)
                        Box(modifier = Modifier.weight(0.52f)) {
                            activeVideo?.let { vid ->
                                HyperPlayer(
                                    video = vid,
                                    onClose = { activeVideo = null }
                                )
                            }
                        }

                        // Right Pane: List / Vault (48% width)
                        Box(modifier = Modifier.weight(0.48f)) {
                            DeckListSection(
                                selectedTab = selectedTab,
                                vaultItems = vaultItems,
                                searchResults = searchResults,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onPerformSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        isSearching = true
                                        coroutineScope.launch {
                                            val res = mediaEngine.searchVideos(searchQuery)
                                            res.onSuccess { searchResults = it }
                                            res.onFailure {
                                                Toast.makeText(context, "Search instances busy, try direct URL", Toast.LENGTH_SHORT).show()
                                            }
                                            isSearching = false
                                        }
                                    }
                                },
                                activeVideoId = activeVideo?.id,
                                onSelectVideo = { item ->
                                    activeVideo = item
                                    if (!vaultItems.any { it.id == item.id }) {
                                        vaultItems.add(0, item)
                                    }
                                },
                                onDeleteVideo = { vaultItems.remove(it) }
                            )
                        }
                    }
                } else {
                    // PHONE OR TABLET (WITHOUT ACTIVE PLAYER)
                    Column(modifier = Modifier.fillMaxSize()) {
                        activeVideo?.let { vid ->
                            HyperPlayer(
                                video = vid,
                                modifier = Modifier.padding(bottom = 14.dp),
                                onClose = { activeVideo = null }
                            )
                        }

                        DeckListSection(
                            selectedTab = selectedTab,
                            vaultItems = vaultItems,
                            searchResults = searchResults,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onPerformSearch = {
                                if (searchQuery.isNotBlank()) {
                                    isSearching = true
                                    coroutineScope.launch {
                                        val res = mediaEngine.searchVideos(searchQuery)
                                        res.onSuccess { searchResults = it }
                                        res.onFailure {
                                            Toast.makeText(context, "Search instances busy, try direct URL", Toast.LENGTH_SHORT).show()
                                        }
                                        isSearching = false
                                    }
                                }
                            },
                            activeVideoId = activeVideo?.id,
                            onSelectVideo = { item ->
                                activeVideo = item
                                if (!vaultItems.any { it.id == item.id }) {
                                    vaultItems.add(0, item)
                                }
                            },
                            onDeleteVideo = { vaultItems.remove(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeckListSection(
    selectedTab: Int,
    vaultItems: List<VideoItem>,
    searchResults: List<VideoItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPerformSearch: () -> Unit,
    activeVideoId: String?,
    onSelectVideo: (VideoItem) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit
) {
    if (selectedTab == 0) {
        // Vault / CD Rack
        Text(
            text = "YOUR VAULT (${vaultItems.size} DISCS)",
            color = HyperTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(vaultItems, key = { it.id }) { item ->
                DiscCard(
                    item = item,
                    isPlaying = activeVideoId == item.id,
                    onPlay = { onSelectVideo(item) },
                    onDelete = { onDeleteVideo(item) }
                )
            }
        }
    } else {
        // Zero-Algo Search Tab
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search YouTube cleanly...", color = HyperTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = HyperAccent) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onPerformSearch) {
                        Icon(Icons.Default.ArrowForward, "Run Search", tint = HyperAccent)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HyperAccent,
                unfocusedBorderColor = HyperCardBorder,
                focusedTextColor = HyperTextPrimary,
                unfocusedTextColor = HyperTextPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(searchResults, key = { it.id }) { result ->
                DiscCard(
                    item = result,
                    isPlaying = activeVideoId == result.id,
                    onPlay = { onSelectVideo(result) },
                    onDelete = null
                )
            }
        }
    }
}

@Composable
fun DiscCard(
    item: VideoItem,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)) // HyperOS Squircle
            .background(if (isPlaying) HyperAccent.copy(alpha = 0.18f) else HyperSurface)
            .border(
                1.dp,
                if (isPlaying) HyperAccent else HyperCardBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable { onPlay() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail styled as a Disc / CD Album Art
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(HyperCard)
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Center CD Hole Overlay
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(HyperBlack.copy(alpha = 0.85f))
                    .border(1.dp, HyperAccent.copy(alpha = 0.6f), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = HyperTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.author.ifEmpty { "ID: ${item.id}" },
                color = HyperTextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Playing",
                tint = HyperAccent,
                modifier = Modifier.size(26.dp)
            )
        } else {
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = HyperAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        onDelete?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = HyperRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
