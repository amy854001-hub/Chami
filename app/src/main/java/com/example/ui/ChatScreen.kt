package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelTier
import com.example.ui.components.ChatSidebarContent
import com.example.ui.components.FullscreenImageViewer
import com.example.ui.components.MessageInputBar
import com.example.ui.components.MessageItem
import com.example.ui.components.ModelSelectorBottomSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val sessions by viewModel.sessions.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedTier by viewModel.selectedModelTier.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isImageModeActive by viewModel.isImageModeActive.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isModelSelectorOpen by viewModel.isModelSelectorOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val fullscreenImageUri by viewModel.fullscreenImageUri.collectAsState()
    val statusNotice by viewModel.statusNotice.collectAsState()

    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    // Show status notice in snackbar
    LaunchedEffect(statusNotice) {
        statusNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearStatusNotice()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.width(320.dp)
            ) {
                ChatSidebarContent(
                    sessions = sessions,
                    currentSessionId = currentSession?.id,
                    onSelectSession = { session ->
                        viewModel.selectSession(session)
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.createNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onClearAll = {
                        viewModel.clearAllChats()
                        scope.launch { drawerState.close() }
                    },
                    themeMode = themeMode,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onOpenSettings = {
                        viewModel.setSettingsOpen(true)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        // Three-Tier Model Switcher Button in Header
                        Surface(
                            onClick = { viewModel.setModelSelectorOpen(true) },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, selectedTier.color.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("model_switcher_header_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(selectedTier.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedTier.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Model Tier",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Chat History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Quick theme toggle
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("top_bar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (themeMode == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // New Chat button
                        IconButton(
                            onClick = { viewModel.createNewChat() },
                            modifier = Modifier.testTag("top_bar_new_chat")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                MessageInputBar(
                    inputText = inputText,
                    onInputTextChange = { viewModel.setInputText(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    isGenerating = isGenerating,
                    selectedTier = selectedTier,
                    isImageModeActive = isImageModeActive,
                    onToggleImageMode = { viewModel.toggleImageMode() },
                    selectedAspectRatio = selectedAspectRatio,
                    onSelectAspectRatio = { viewModel.setAspectRatio(it) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty()) {
                    EmptyChatGreetingView(
                        selectedTier = selectedTier,
                        onSuggestionClick = { prompt, isImage ->
                            if (isImage) {
                                viewModel.selectModelTier(ModelTier.FLASH_3_7)
                                viewModel.setImageMode(true)
                                viewModel.sendMessage(prompt)
                            } else {
                                viewModel.sendMessage(prompt)
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageItem(
                                message = message,
                                onImageClick = { uri -> viewModel.setFullscreenImage(uri) }
                            )
                        }

                        if (isGenerating) {
                            item {
                                ThinkingStreamIndicator(tier = selectedTier, isImage = isImageModeActive)
                            }
                        }
                    }
                }
            }
        }
    }

    // Model Selector Bottom Sheet
    if (isModelSelectorOpen) {
        ModelSelectorBottomSheet(
            selectedTier = selectedTier,
            onTierSelected = { tier -> viewModel.selectModelTier(tier) },
            onDismiss = { viewModel.setModelSelectorOpen(false) }
        )
    }

    // Settings Dialog
    if (isSettingsOpen) {
        SettingsDialog(
            currentApiKey = viewModel.getApiKey(),
            onSaveApiKey = { key -> viewModel.saveApiKey(key) },
            themeMode = themeMode,
            onSetThemeMode = { viewModel.setThemeMode(it) },
            onClearAllChats = { viewModel.clearAllChats() },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }

    // Fullscreen Image Viewer
    fullscreenImageUri?.let { uri ->
        FullscreenImageViewer(
            imageUri = uri,
            onDismiss = { viewModel.setFullscreenImage(null) }
        )
    }
}

@Composable
fun EmptyChatGreetingView(
    selectedTier: ModelTier,
    onSuggestionClick: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Gemini Sparkle Avatar
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(GeminiBlue, GeminiPurple, GeminiCyan)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Charmi AI",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gemini Gradient Greeting
        Text(
            text = "Hello, I'm Charmi",
            style = MaterialTheme.typography.headlineMedium.copy(
                brush = Brush.linearGradient(
                    listOf(GeminiBlue, GeminiPurple, GeminiPink)
                )
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Powered by Google Gemini intelligence.\nChoose a suggestion or ask anything below:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Current tier badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = selectedTier.color.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, selectedTier.color.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(selectedTier.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${selectedTier.displayName}: ${selectedTier.summary}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = selectedTier.color
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Dynamic Suggestion Chips based on selected model tier
        val suggestions = when (selectedTier) {
            ModelTier.FLASH_1_1 -> listOf(
                Pair("Summarize quantum computing in 2 brief sentences", false),
                Pair("Give me 3 quick tips for better daily focus", false),
                Pair("What is the core difference between HTTP and HTTPS?", false)
            )
            ModelTier.FLASH_2_0 -> listOf(
                Pair("Brainstorm 5 fun weekend getaway concepts", false),
                Pair("Give me empathetic advice on managing work-life balance", false),
                Pair("Tell me an uplifting, creative short story", false)
            )
            ModelTier.FLASH_3_7 -> listOf(
                Pair("Write a production Kotlin Room database helper class", false),
                Pair("A futuristic cyberpunk skyline with neon violet towers", true),
                Pair("Architect a clean MVVM pattern for Android Compose", false),
                Pair("A tranquil glowing celestial orb over mountain mist", true)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { (text, isImage) ->
                Surface(
                    onClick = { onSuggestionClick(text, isImage) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isImage) Icons.Default.AutoAwesome else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isImage) GeminiPurple else selectedTier.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingStreamIndicator(tier: ModelTier, isImage: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(tier.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = tier.color,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isImage) "Charmi Flash 3.7.0 is crafting your visual..."
                    else "${tier.displayName} is thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(alphaAnim)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(tier.color.copy(alpha = alphaAnim))
                        )
                    }
                }
            }
        }
    }
}
