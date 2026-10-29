package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ClockCustomization
import com.example.data.model.ClockFontStyle
import com.example.data.repository.AppLauncherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aura TV", appName)
    }

    @Test
    fun `verify default depth wallpaper has paired background and foreground layers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AppLauncherRepository(context)
        val activeWallpaper = repository.getActiveWallpaper()
        assertNotNull(activeWallpaper)
        assertEquals("depth_tokyo", activeWallpaper.id)
        assertNotNull(activeWallpaper.bgDrawableRes)
        assertNotNull(activeWallpaper.fgDrawableRes)
        assertTrue("Must be genuine depth wallpaper with transparent foreground", activeWallpaper.isGenuineDepth)
    }

    @Test
    fun `verify built-in depth wallpapers library has valid paired assets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AppLauncherRepository(context)
        val library = repository.builtInDepthWallpapers
        assertEquals(4, library.size)

        for (theme in library) {
            assertNotNull("${theme.id} must have background", theme.bgDrawableRes)
            assertNotNull("${theme.id} must have transparent foreground", theme.fgDrawableRes)
            assertTrue(theme.isGenuineDepth)
        }
    }

    @Test
    fun `verify clock customization save and retrieve`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AppLauncherRepository(context)
        val custom = ClockCustomization(
            fontStyle = ClockFontStyle.ELEGANT_THIN,
            colorHex = 0xFF67E8F9,
            sizeSp = 112,
            verticalOffsetY = 45,
            showDate = false,
            is24Hour = true
        )
        repository.saveClockCustomization(custom)
        val loaded = repository.getClockCustomization()
        assertEquals(ClockFontStyle.ELEGANT_THIN, loaded.fontStyle)
        assertEquals(0xFF67E8F9, loaded.colorHex)
        assertEquals(112, loaded.sizeSp)
        assertEquals(45, loaded.verticalOffsetY)
        assertEquals(false, loaded.showDate)
        assertEquals(true, loaded.is24Hour)
    }

    @Test
    fun `verify PIN verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AppLauncherRepository(context)
        assertTrue(repository.verifyPin("0000"))
    }

    @Test
    fun `verify gemini chat repository initial state and history clearing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val chatRepo = com.example.data.gemini.GeminiChatRepository(context)
        
        // Initial welcome message present
        val messages = chatRepo.messages.value
        assertTrue("Welcome message must be present", messages.isNotEmpty())
        assertEquals("welcome_msg", messages.first().id)

        // Clear history resets to welcome message
        chatRepo.clearHistory()
        assertEquals(1, chatRepo.messages.value.size)

        // Toggle history saving
        chatRepo.setHistoryEnabled(false)
        assertEquals(false, chatRepo.isHistoryEnabled.value)

        chatRepo.setHistoryEnabled(true)
        assertEquals(true, chatRepo.isHistoryEnabled.value)
    }
}
