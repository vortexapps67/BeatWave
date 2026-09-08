package com.beatwave.music.ui.screens.settings

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.beatwave.music.LocalPlayerAwareWindowInsets
import com.beatwave.music.R
import com.beatwave.music.constants.RecommendationEngine
import com.beatwave.music.constants.RecommendationEngineKey
import com.beatwave.music.ui.component.DefaultDialog
import com.beatwave.music.ui.component.GlassSwitchCompat as Switch
import com.beatwave.music.ui.component.IconButton
import com.beatwave.music.ui.component.Material3SettingsGroup
import com.beatwave.music.ui.component.Material3SettingsItem
import com.beatwave.music.ui.menu.LoadingScreen
import com.beatwave.music.ui.utils.appTopBarWindowInsets
import com.beatwave.music.ui.utils.backToMain
import com.beatwave.music.utils.rememberEnumPreference
import com.beatwave.music.utils.rememberPreference
import com.beatwave.music.viewmodels.SpotifyImportViewModel
import com.music.spotify.SpotifyAuth
import com.music.spotify.SpotifyMapper
import com.music.spotify.models.SpotifyPlaylist
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: SpotifyImportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showSpotifyLogin by remember { mutableStateOf(false) }
    var showPlaylistsSheet by remember { mutableStateOf(false) }
    var playlistLink by rememberSaveable { mutableStateOf("") }
    val (recommendationEngine, onRecommendationEngineChange) = rememberEnumPreference(
        key = RecommendationEngineKey,
        defaultValue = RecommendationEngine.SPOTIFY
    )
    val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()

    val refreshEnabled = state.isAuthenticated && !state.isLoading
    val rotationAngle by if (state.isLoading) {
        val transition = rememberInfiniteTransition(label = "rotation")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotationAngle"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        )

        Text(
            text = stringResource(R.string.spotify),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 16.dp)
        )

        // Connection Card/Group
        Material3SettingsGroup(
            title = stringResource(R.string.spotify_account),
            items = listOf(
                if (state.isAuthenticated) {
                    Material3SettingsItem(
                        leadingContent = if (!state.accountAvatarUrl.isNullOrBlank()) {
                            {
                                AsyncImage(
                                    model = state.accountAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else null,
                        icon = if (state.accountAvatarUrl.isNullOrBlank()) painterResource(R.drawable.spotify) else null,
                        title = {
                            Text(
                                text = if (state.accountName.isNotBlank()) state.accountName
                                else stringResource(R.string.spotify_account),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        trailingContent = {
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(stringResource(R.string.action_logout))
                            }
                        },
                        onClick = {}
                    )
                } else {
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.spotify_connect)) },
                        description = { Text(stringResource(R.string.spotify_not_connected)) },
                        icon = painterResource(R.drawable.spotify),
                        onClick = { showSpotifyLogin = true }
                    )
                }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Material3SettingsGroup(
            title = "Import via Playlist Link",
            items = listOf(
                Material3SettingsItem(
                    title = {
                        OutlinedTextField(
                            value = playlistLink,
                            onValueChange = { playlistLink = it },
                            label = { Text("Spotify Playlist Link or ID") },
                            placeholder = { Text("https://open.spotify.com/playlist/...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            trailingIcon = {
                                if (playlistLink.isNotEmpty()) {
                                    IconButton(onClick = { playlistLink = "" }) {
                                        Icon(painterResource(R.drawable.close), contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    },
                    description = {
                        Button(
                            onClick = {
                                if (playlistLink.isBlank()) {
                                    android.widget.Toast.makeText(context, "Please enter a Spotify link or ID first", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.importPlaylistByLink(playlistLink)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isLoading
                        ) {
                            Text("Import Playlist")
                        }
                    },
                    icon = painterResource(R.drawable.link),
                    onClick = {}
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        val totalPlaylists = state.playlists.size + if (state.likedSongsCount > 0) 1 else 0
        Material3SettingsGroup(
            title = stringResource(R.string.playlists),
            items = listOf(
                Material3SettingsItem(
                    title = { Text(stringResource(R.string.spotify_select_sources)) },
                    description = {
                        Text(
                            if (state.isAuthenticated) {
                                if (totalPlaylists > 0) stringResource(R.string.spotify_available_count, totalPlaylists)
                                else stringResource(R.string.spotify_no_sources)
                            } else {
                                stringResource(R.string.spotify_not_connected)
                            }
                        )
                    },
                    icon = painterResource(R.drawable.bookmark_star_library),
                    enabled = state.isAuthenticated && totalPlaylists > 0 && !state.isLoading,
                    onClick = { showPlaylistsSheet = true }
                ),
                Material3SettingsItem(
                    title = { Text(stringResource(R.string.spotify_refresh)) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.sync),
                                contentDescription = null,
                                tint = if (!refreshEnabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        rotationZ = rotationAngle
                                    }
                            )
                        }
                    },
                    enabled = refreshEnabled,
                    onClick = { viewModel.loadSources() }
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Material3SettingsGroup(
            title = "Music Recommendations",
            items = listOf(
                Material3SettingsItem(
                    title = { Text("Use Spotify Recommendations") },
                    description = {
                        Text("Power Daily Discover, Quick Picks & Radio with Spotify recommendation algorithm")
                    },
                    icon = painterResource(R.drawable.spotify),
                    trailingContent = {
                        Switch(
                            checked = recommendationEngine == RecommendationEngine.SPOTIFY,
                            onCheckedChange = { isChecked ->
                                onRecommendationEngineChange(
                                    if (isChecked) RecommendationEngine.SPOTIFY else RecommendationEngine.YOUTUBE
                                )
                            }
                        )
                    },
                    onClick = {
                        onRecommendationEngineChange(
                            if (recommendationEngine == RecommendationEngine.SPOTIFY) RecommendationEngine.YOUTUBE else RecommendationEngine.SPOTIFY
                        )
                    }
                )
            )
        )

        // Info block
        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = stringResource(R.string.spotify_import_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    TopAppBar(
            windowInsets = appTopBarWindowInsets(),
        title = {},
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        )
    )

    if (showSpotifyLogin) {
        SpotifyLoginDialog(
            onDismiss = { showSpotifyLogin = false },
            onCookiesCaptured = { spDc, spKey ->
                showSpotifyLogin = false
                viewModel.connectWithCookies(spDc, spKey)
            }
        )
    }

    importProgress?.let { progress ->
        val isFinished = progress.isFinished || progress.percent >= 1f
        DefaultDialog(
            onDismiss = {
                if (isFinished) {
                    viewModel.dismissImportProgress()
                } else {
                    viewModel.cancelImport()
                }
            },
            title = {
                Text(
                    text = if (isFinished) {
                        stringResource(R.string.spotify_import_complete)
                    } else {
                        stringResource(R.string.spotify_import_in_progress)
                    }
                )
            },
            buttons = {
                if (isFinished) {
                    Button(
                        onClick = { viewModel.dismissImportProgress() },
                        shape = CircleShape
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                } else {
                    TextButton(onClick = { viewModel.cancelImport() }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.playlists) + ": " + progress.playlistName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFinished) {
                        stringResource(R.string.spotify_imported_songs_count, progress.currentSongIndex, progress.totalSongs)
                    } else {
                        stringResource(R.string.spotify_importing_songs_count, progress.currentSongIndex, progress.totalSongs)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { progress.percent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
    state.errorMessage?.let { error ->
        DefaultDialog(
            onDismiss = { viewModel.dismissError() },
            title = { Text("Error") },
            buttons = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showPlaylistsSheet) {
        SpotifyPlaylistBottomSheet(
            onDismiss = { showPlaylistsSheet = false },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotifyLoginDialog(
    onDismiss: () -> Unit,
    onCookiesCaptured: (spDc: String, spKey: String) -> Unit,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var captured by remember { mutableStateOf(false) }
    var pageProgress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
            webView?.destroy()
            webView = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.spotify_login_title)) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    painterResource(R.drawable.close),
                                    contentDescription = "Close"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { webView?.reload() }) {
                                Icon(
                                    painterResource(R.drawable.sync),
                                    contentDescription = "Reload"
                                )
                            }
                        },
                        windowInsets = appTopBarWindowInsets()
                    )
                    if (isLoading || pageProgress in 1..99) {
                        LinearProgressIndicator(
                            progress = { pageProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false

                                val rawUa = userAgentString.orEmpty()
                                userAgentString = if (rawUa.contains("; wv")) {
                                    rawUa.replace("; wv", "")
                                } else if (rawUa.isNotBlank()) {
                                    rawUa
                                } else {
                                    "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    pageProgress = newProgress
                                    if (newProgress >= 100) {
                                        isLoading = false
                                    }
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                private fun captureCookies(url: String?): Boolean {
                                    if (captured) return true
                                    cookieManager.flush()
                                    val allCookies = buildString {
                                        append(cookieManager.getCookie("https://open.spotify.com") ?: "")
                                        append(";")
                                        append(cookieManager.getCookie("https://accounts.spotify.com") ?: "")
                                        append(";")
                                        append(cookieManager.getCookie("https://spotify.com") ?: "")
                                        if (url != null) {
                                            append(";")
                                            append(cookieManager.getCookie(url) ?: "")
                                        }
                                    }
                                    val cookies = allCookies.split(";").associate {
                                        val parts = it.split("=")
                                        val key = parts.firstOrNull()?.trim().orEmpty()
                                        val valStr = parts.drop(1).joinToString("=").trim()
                                        key to valStr
                                    }
                                    val spDc = cookies["sp_dc"].orEmpty()
                                    if (spDc.isBlank()) return false
                                    captured = true
                                    val spKey = cookies["sp_key"].orEmpty()
                                    onCookiesCaptured(spDc, spKey)
                                    return true
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest,
                                ): Boolean = captureCookies(request.url?.toString())

                                override fun onPageStarted(
                                    view: WebView,
                                    url: String?,
                                    favicon: android.graphics.Bitmap?,
                                ) {
                                    isLoading = true
                                    captureCookies(url)
                                }

                                override fun onPageFinished(view: WebView, url: String?) {
                                    isLoading = false
                                    captureCookies(url)
                                }
                            }

                            webView = this
                            cookieManager.removeAllCookies(null)
                            cookieManager.flush()
                            loadUrl(SpotifyAuth.LOGIN_URL)
                        }
                    },
                    update = { view ->
                        webView = view
                    }
                )
            }
        }
    }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }
}
