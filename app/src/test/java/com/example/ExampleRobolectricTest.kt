package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ModelTier
import com.example.ui.components.ContentBlock
import com.example.ui.components.parseMarkdownBlocks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Charmi", appName)
    }

    @Test
    fun `model tier rules and configurations`() {
        // Flash 1.1: lightweight queries & brief answers
        val tier1 = ModelTier.FLASH_1_1
        assertEquals("Flash 1.1", tier1.displayName)
        assertTrue(tier1.allowsCode)
        assertFalse(tier1.allowsImageGeneration)

        // Flash 2.0: conversational chatting, explicitly restricted from writing code
        val tier2 = ModelTier.FLASH_2_0
        assertEquals("Flash 2.0", tier2.displayName)
        assertFalse(tier2.allowsCode)
        assertFalse(tier2.allowsImageGeneration)

        // Flash 3.7.0: powerhouse model capable of deep reasoning, full code & image generation
        val tier3 = ModelTier.FLASH_3_7
        assertEquals("Flash 3.7.0", tier3.displayName)
        assertTrue(tier3.allowsCode)
        assertTrue(tier3.allowsImageGeneration)
    }

    @Test
    fun `markdown code block parsing`() {
        val raw = "Here is the code:\n```kotlin\nval x = 42\n```\nAll done!"
        val blocks = parseMarkdownBlocks(raw)
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is ContentBlock.Text)
        assertTrue(blocks[1] is ContentBlock.Code)
        assertTrue(blocks[2] is ContentBlock.Text)

        val codeBlock = blocks[1] as ContentBlock.Code
        assertEquals("kotlin", codeBlock.language)
        assertEquals("val x = 42", codeBlock.code)
    }
}
