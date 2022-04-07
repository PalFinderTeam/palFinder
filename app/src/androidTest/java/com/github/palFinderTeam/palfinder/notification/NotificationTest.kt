package com.github.palFinderTeam.palfinder.notification

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.github.palFinderTeam.palfinder.MainActivity
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.utils.UIMockContextServiceModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationTest {
    private val uiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }
    private val timeout = 10000L

    @Rule
    @JvmField
    val activityRule = ActivityTestRule<MainActivity>(MainActivity::class.java)


    @Test
    fun postString() {
        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(UIMockContextServiceModule.UIMockContextService())
        handler.post(expectedTitle,expectedContent, R.drawable.icon_beer)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedTitle)), timeout)
        val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
        val text: UiObject2 = uiDevice.findObject(By.textStartsWith(expectedContent))
        assertEquals(expectedTitle, title.text)
        assertTrue(text.text.startsWith(expectedContent))
    }

    @Test
    fun postID() {
        val expectedTitle = "title"
        val expectedContent = "content"

        val handler = NotificationHandler(UIMockContextServiceModule.UIMockContextService())
        handler.post(R.string.testNotifTitle,R.string.testNotifContent, R.drawable.icon_beer)

        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedTitle)), timeout)
        val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
        val text: UiObject2 = uiDevice.findObject(By.textStartsWith(expectedContent))
        assertEquals(expectedTitle, title.text)
        assertTrue(text.text.startsWith(expectedContent))
    }
}