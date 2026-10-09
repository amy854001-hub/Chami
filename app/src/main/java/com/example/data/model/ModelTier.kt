package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TierFlash11Color
import com.example.ui.theme.TierFlash20Color
import com.example.ui.theme.TierFlash37Color

enum class ModelTier(
    val id: String,
    val displayName: String,
    val badge: String,
    val summary: String,
    val description: String,
    val color: Color,
    val allowsCode: Boolean,
    val allowsImageGeneration: Boolean,
    val apiModelName: String,
    val systemPrompt: String
) {
    FLASH_1_1(
        id = "flash_1_1",
        displayName = "Flash 1.1",
        badge = "Fast & Brief",
        summary = "Basic tasks & quick answers",
        description = "Configured for basic tasks, lightweight queries, and brief text answers.",
        color = TierFlash11Color,
        allowsCode = true,
        allowsImageGeneration = false,
        apiModelName = "gemini-flash-latest",
        systemPrompt = "You are Charmi Flash 1.1, a lightweight, ultra-fast AI assistant. Provide concise, direct, clear, and brief text answers. Keep responses short and to the point without unnecessary conversational filler."
    ),

    FLASH_2_0(
        id = "flash_2_0",
        displayName = "Flash 2.0",
        badge = "Conversational • No Code",
        summary = "Everyday natural dialogue",
        description = "Configured for natural, everyday conversational chatting (explicitly restricted from writing code).",
        color = TierFlash20Color,
        allowsCode = false,
        allowsImageGeneration = false,
        apiModelName = "gemini-3.5-flash",
        systemPrompt = "You are Charmi Flash 2.0, a warm, engaging, and empathetic conversational AI companion for everyday chatting, advice, ideas, and storytelling. MANDATORY RESTRICTION: You are STRICTLY FORBIDDEN from writing, formatting, or outputting any computer programming code, code snippets, scripts, or syntax (e.g., Python, JavaScript, HTML, SQL, etc.). If the user asks for code, politely decline: explain that Flash 2.0 is conversational-only, and provide a friendly conceptual or plain-English overview instead."
    ),

    FLASH_3_7(
        id = "flash_3_7",
        displayName = "Flash 3.7.0",
        badge = "Powerhouse • Reasoning & Code",
        summary = "Deep reasoning, full code & visuals",
        description = "Configured as the powerhouse model capable of deep reasoning, full code generation with syntax highlighting, and integrated image generation.",
        color = TierFlash37Color,
        allowsCode = true,
        allowsImageGeneration = true,
        apiModelName = "gemini-3.5-flash",
        systemPrompt = "You are Charmi Flash 3.7.0, the flagship frontier powerhouse model. You excel at complex analytical reasoning, step-by-step problem breakdown, comprehensive production code generation, and visual creative ideation. Present clear, structured, deep responses. When generating code, format it in markdown with language tags."
    );

    companion object {
        fun fromId(id: String): ModelTier {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FLASH_3_7
        }
    }
}
