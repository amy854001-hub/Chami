package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object VisualArtGenerator {

    /**
     * Saves a base64 encoded image string to local app cache and returns the file path.
     */
    fun saveBase64Image(context: Context, base64Data: String, mimeType: String = "image/jpeg"): String {
        val cleanData = if (base64Data.contains(",")) base64Data.substringAfter(",") else base64Data
        val decodedBytes = Base64.decode(cleanData, Base64.DEFAULT)
        val imageDir = File(context.filesDir, "generated_images").apply { mkdirs() }
        val extension = if (mimeType.contains("png")) "png" else "jpg"
        val imageFile = File(imageDir, "charmi_art_${UUID.randomUUID()}.$extension")
        FileOutputStream(imageFile).use { it.write(decodedBytes) }
        return imageFile.absolutePath
    }

    /**
     * Synthesizes a high-definition stylized artistic visual bitmap based on prompt keywords,
     * ensuring that visual generation always succeeds and renders seamlessly.
     */
    fun generateArtisticVisual(
        context: Context,
        prompt: String,
        aspectRatio: String = "1:1"
    ): String {
        val width = when (aspectRatio) {
            "16:9" -> 1024
            "9:16" -> 576
            else -> 800
        }
        val height = when (aspectRatio) {
            "16:9" -> 576
            "9:16" -> 1024
            else -> 800
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val promptLower = prompt.lowercase()
        val random = Random(prompt.hashCode().toLong())

        // Determine color theme based on prompt keywords
        val (colorStart, colorMid, colorEnd, accentColor) = when {
            promptLower.contains("cyberpunk") || promptLower.contains("neon") || promptLower.contains("future") ->
                listOf(0xFF0F0C29.toInt(), 0xFF302B63.toInt(), 0xFF24243E.toInt(), 0xFF00F2FE.toInt())
            promptLower.contains("sunset") || promptLower.contains("sunrise") || promptLower.contains("warm") || promptLower.contains("fire") ->
                listOf(0xFF2C3E50.toInt(), 0xFFFD746C.toInt(), 0xFFFF9068.toInt(), 0xFFFFD200.toInt())
            promptLower.contains("nature") || promptLower.contains("forest") || promptLower.contains("green") || promptLower.contains("leaf") ->
                listOf(0xFF0A192F.toInt(), 0xFF11998E.toInt(), 0xFF38EF7D.toInt(), 0xFFE0F7FA.toInt())
            promptLower.contains("space") || promptLower.contains("galaxy") || promptLower.contains("star") || promptLower.contains("cosmic") ->
                listOf(0xFF050510.toInt(), 0xFF1A103C.toInt(), 0xFF4A148C.toInt(), 0xFFB388FF.toInt())
            promptLower.contains("ocean") || promptLower.contains("sea") || promptLower.contains("water") || promptLower.contains("blue") ->
                listOf(0xFF0F2027.toInt(), 0xFF203A43.toInt(), 0xFF2C5364.toInt(), 0xFF48CAE4.toInt())
            else ->
                listOf(0xFF131314.toInt(), 0xFF202538.toInt(), 0xFF3A1C71.toInt(), 0xFF7DACF8.toInt())
        }

        // 1. Draw atmospheric background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(colorStart, colorMid, colorEnd),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Draw glowing radial aura
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val auraX = width * (0.3f + random.nextFloat() * 0.4f)
            val auraY = height * (0.3f + random.nextFloat() * 0.4f)
            val auraRadius = width.coerceAtLeast(height) * 0.45f
            shader = RadialGradient(
                auraX, auraY, auraRadius,
                intArrayOf(accentColor, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            alpha = 140
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)

        // 3. Draw artistic geometric / landscape silhouette layers
        val numMountains = 3
        for (i in 0 until numMountains) {
            val path = Path()
            val baseLevel = height * (0.55f + i * 0.15f)
            path.moveTo(0f, height.toFloat())
            path.lineTo(0f, baseLevel)

            var cx = 0f
            val segments = 6
            val segWidth = width.toFloat() / segments
            while (cx < width) {
                val peakY = baseLevel - (40f + random.nextFloat() * 120f * (1f - i * 0.25f))
                val nextX = (cx + segWidth).coerceAtMost(width.toFloat())
                path.quadTo(cx + segWidth / 2f, peakY, nextX, baseLevel + random.nextFloat() * 20f)
                cx = nextX
            }
            path.lineTo(width.toFloat(), height.toFloat())
            path.close()

            val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = when (i) {
                    0 -> Color.argb(120, 40, 45, 70)
                    1 -> Color.argb(180, 25, 28, 45)
                    else -> Color.argb(230, 15, 17, 26)
                }
            }
            canvas.drawPath(path, layerPaint)
        }

        // 4. Draw starry or particle accents
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        for (s in 0 until 60) {
            val sx = random.nextFloat() * width
            val sy = random.nextFloat() * (height * 0.65f)
            val sRadius = 1f + random.nextFloat() * 3.5f
            starPaint.alpha = 100 + random.nextInt(155)
            canvas.drawCircle(sx, sy, sRadius, starPaint)
        }

        // 5. Draw glowing celestial orb or geometric centerpiece
        val orbX = width * 0.5f
        val orbY = height * 0.38f
        val orbRadius = width * 0.14f

        val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                orbX, orbY, orbRadius,
                intArrayOf(Color.WHITE, accentColor, Color.TRANSPARENT),
                floatArrayOf(0f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
            alpha = 220
        }
        canvas.drawCircle(orbX, orbY, orbRadius, orbPaint)

        // 6. Draw subtle four-pointed Gemini sparkle star in the center
        val sparklePath = Path()
        val starSize = orbRadius * 0.75f
        sparklePath.moveTo(orbX, orbY - starSize)
        sparklePath.quadTo(orbX, orbY, orbX + starSize, orbY)
        sparklePath.quadTo(orbX, orbY, orbX, orbY + starSize)
        sparklePath.quadTo(orbX, orbY, orbX - starSize, orbY)
        sparklePath.quadTo(orbX, orbY, orbX, orbY - starSize)
        sparklePath.close()

        val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 240
        }
        canvas.drawPath(sparklePath, sparklePaint)

        // 7. Save bitmap to file and return path
        val imageDir = File(context.filesDir, "generated_images").apply { mkdirs() }
        val imageFile = File(imageDir, "charmi_art_${System.currentTimeMillis()}.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return imageFile.absolutePath
    }
}
