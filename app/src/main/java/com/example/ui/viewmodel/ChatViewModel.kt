package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.model.ModelTier
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatRepository(
        context = application.applicationContext,
        chatDao = ChatDatabase.getDatabase(application.applicationContext).chatDao()
    )

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSession = MutableStateFlow<ChatSessionEntity?>(null)
    val currentSession: StateFlow<ChatSessionEntity?> = _currentSession.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _selectedModelTier = MutableStateFlow(ModelTier.FLASH_3_7)
    val selectedModelTier: StateFlow<ModelTier> = _selectedModelTier.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isImageModeActive = MutableStateFlow(false)
    val isImageModeActive: StateFlow<Boolean> = _isImageModeActive.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow("1:1")
    val selectedAspectRatio: StateFlow<String> = _selectedAspectRatio.asStateFlow()

    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(false)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isModelSelectorOpen = MutableStateFlow(false)
    val isModelSelectorOpen: StateFlow<Boolean> = _isModelSelectorOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _fullscreenImageUri = MutableStateFlow<String?>(null)
    val fullscreenImageUri: StateFlow<String?> = _fullscreenImageUri.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    private var messagesJob: Job? = null

    init {
        // Initialize default session if none exists
        viewModelScope.launch {
            sessions.collect { sessionList ->
                if (_currentSession.value == null && sessionList.isNotEmpty()) {
                    selectSession(sessionList.first())
                }
            }
        }
    }

    fun selectModelTier(tier: ModelTier) {
        _selectedModelTier.value = tier
        val session = _currentSession.value
        if (session != null) {
            viewModelScope.launch {
                repository.updateSessionModelTier(session.id, tier)
                _currentSession.value = session.copy(modelTierId = tier.id)
            }
        }
        if (!tier.allowsImageGeneration && _isImageModeActive.value) {
            _isImageModeActive.value = false
        }
    }

    fun selectSession(session: ChatSessionEntity) {
        _currentSession.value = session
        _selectedModelTier.value = ModelTier.fromId(session.modelTierId)
        _isSidebarOpen.value = false

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForSession(session.id).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            val tier = _selectedModelTier.value
            val newSession = repository.createNewSession(modelTier = tier, title = "New Chat")
            selectSession(newSession)
            _isSidebarOpen.value = false
            _isImageModeActive.value = false
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSession.value?.id == sessionId) {
                _currentSession.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearAllSessions()
            _currentSession.value = null
            _messages.value = emptyList()
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun toggleImageMode() {
        if (_selectedModelTier.value != ModelTier.FLASH_3_7) {
            // Automatically switch to Flash 3.7.0 as it is the powerhouse with image generation!
            selectModelTier(ModelTier.FLASH_3_7)
            _statusNotice.value = "Switched to Flash 3.7.0 for Image Generation ✨"
        }
        _isImageModeActive.value = !_isImageModeActive.value
    }

    fun setImageMode(active: Boolean) {
        _isImageModeActive.value = active
    }

    fun setAspectRatio(ratio: String) {
        _selectedAspectRatio.value = ratio
    }

    fun toggleTheme() {
        val current = _themeMode.value
        val newMode = if (current == "dark") "light" else "dark"
        _themeMode.value = newMode
        repository.saveThemeMode(newMode)
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        repository.saveThemeMode(mode)
    }

    fun setSidebarOpen(open: Boolean) {
        _isSidebarOpen.value = open
    }

    fun setModelSelectorOpen(open: Boolean) {
        _isModelSelectorOpen.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun setFullscreenImage(uri: String?) {
        _fullscreenImageUri.value = uri
    }

    fun clearStatusNotice() {
        _statusNotice.value = null
    }

    fun saveApiKey(key: String) {
        repository.saveCustomApiKey(key)
        _statusNotice.value = "Gemini API key saved securely!"
    }

    fun getApiKey(): String = repository.getApiKey()

    fun sendMessage(explicitPrompt: String? = null) {
        val prompt = explicitPrompt ?: _inputText.value.trim()
        if (prompt.isBlank() || _isGenerating.value) return

        _inputText.value = ""
        val isImage = _isImageModeActive.value && _selectedModelTier.value.allowsImageGeneration
        val aspectRatio = _selectedAspectRatio.value
        val tier = _selectedModelTier.value

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                // Ensure active session
                var session = _currentSession.value
                if (session == null) {
                    session = repository.createNewSession(modelTier = tier, title = "New Chat")
                    _currentSession.value = session
                    selectSession(session)
                }

                repository.sendMessage(
                    sessionId = session.id,
                    userPrompt = prompt,
                    modelTier = tier,
                    isImageRequest = isImage,
                    imageAspectRatio = aspectRatio
                )
            } catch (e: Exception) {
                _statusNotice.value = "Error: ${e.localizedMessage ?: "Failed to generate response"}"
            } finally {
                _isGenerating.value = false
                if (isImage) {
                    _isImageModeActive.value = false
                }
            }
        }
    }
}
