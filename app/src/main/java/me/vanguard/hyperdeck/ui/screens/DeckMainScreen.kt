package me.vanguard.hyperdeck.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import me.vanguard.hyperdeck.data.api.InvidiousClient
import me.vanguard.hyperdeck.data.model.PlayableMedia
import me.vanguard.hyperdeck.data.model.VideoItem
import me.vanguard.hyperdeck.ui.components.HyperPlayer
import me.vanguard.hyperdeck.ui.theme.*

@Composable
fun DeckMainScreen(
    initialVideoId: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val apiClient = remember { InvidiousClient() }
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Vault (CD Rack), 1: Search, 2: Now Playing
    var inputUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var activeMedia by remember { mutableStateOf<PlayableMedia?>(null) }
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
            )
        )
    }

    fun playVideo(videoId: String) {
        isLoading = true
        coroutineScope.launch {
            val result = apiClient.fetchVideoDetails(videoId)
            result.onSuccess { media ->
                activeMedia = media
            }
            isLoading = false
        }
    }

    LaunchedEffect(initialVideoId) {
        initialVideoId?.let { id ->
            playVideo(id)
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
                modifier = Modifier.width(90.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = "HyperDeck Logo",
                    tint = HyperAccent,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

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
                    label = { Text("Search", fontSize = 11.sp) }
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
                // Header & Burn Bar
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

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = HyperAccent,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active ExoPlayer Deck
                activeMedia?.let { media ->
                    HyperPlayer(
                        media = media,
                        modifier = Modifier.padding(bottom = 16.dp),
                        onClose = { activeMedia = null }
                    )
                }

                // Quick Burn / Link Importer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(HyperSurface)
                        .border(1.dp, HyperCardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
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
                            if (inputUrl.isNotBlank()) {
                                val id = InvidiousClient.extractVideoId(inputUrl)
                                vaultItems.add(
                                    VideoItem(
                                        id = id,
                                        title = "Loading Metadata...",
                                        thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg"
                                    )
                                )
                                playVideo(id)
                                inputUrl = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, "Burn", tint = HyperAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                if (selectedTab == 0) {
                    // "The Vault" (Your CD Collection)
                    Text(
                        text = "YOUR VAULT (${vaultItems.size} DISCS)",
                        color = HyperTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(vaultItems) { item ->
                            DiscCard(
                                item = item,
                                isPlaying = activeMedia?.videoId == item.id,
                                onPlay = { playVideo(item.id) },
                                onDelete = { vaultItems.remove(item) }
                            )
                        }
                    }
                } else {
                    // Zero-Algo Search Tab
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search YouTube cleanly...", color = HyperTextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = HyperAccent) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val res = apiClient.search(searchQuery)
                                        res.onSuccess { searchResults = it }
                                        isLoading = false
                                    }
                                }) {
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

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { result ->
                            DiscCard(
                                item = result,
                                isPlaying = activeMedia?.videoId == result.id,
                                onPlay = {
                                    vaultItems.add(result)
                                    playVideo(result.id)
                                },
                                onDelete = null
                            )
                        }
                    }
                }
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
            .background(if (isPlaying) HyperAccent.copy(alpha = 0.15f) else HyperSurface)
            .border(
                1.dp,
                if (isPlaying) HyperAccent else HyperCardBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable { onPlay() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail styled as a Disc / CD Album art
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
            // Center CD hole overlay aesthetic
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(HyperBlack.copy(alpha = 0.8f))
                    .border(1.dp, HyperAccent.copy(alpha = 0.5f), CircleShape)
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
                modifier = Modifier.size(24.dp)
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
