package com.hheelo.countdown

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CustomEventEditorTest {

    @get:Rule
    val composeRule = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun defaultEvent(
        title: String = "新的目标",
        daysOffset: Long = 30,
        isPinned: Boolean = false
    ): CountdownEvent {
        return CountdownEvent(
            id = "test-event-id",
            title = title,
            targetDate = LocalDate.now().plusDays(daysOffset).toString(),
            isPinned = isPinned
        )
    }

    @Test
    fun editorHeader_displaysTitle() {
        composeRule.setContent {
            CountdownTheme {
                CustomEditorHeader(onAdd = {})
            }
        }

        composeRule.onNodeWithText("自定义倒计时").assertIsDisplayed()
    }

    @Test
    fun editorHeader_displaysDescription() {
        composeRule.setContent {
            CountdownTheme {
                CustomEditorHeader(onAdd = {})
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.editor_description)).assertIsDisplayed()
    }

    @Test
    fun editorHeader_addButtonDisplayed() {
        composeRule.setContent {
            CountdownTheme {
                CustomEditorHeader(onAdd = {})
            }
        }

        composeRule.onNodeWithText("新增").assertIsDisplayed()
    }

    @Test
    fun editorHeader_addButtonCallsOnAdd() {
        var addClicked = false

        composeRule.setContent {
            CountdownTheme {
                CustomEditorHeader(onAdd = { addClicked = true })
            }
        }

        composeRule.onNodeWithText("新增").performClick()
        assertTrue("onAdd callback should have been invoked", addClicked)
    }

    @Test
    fun eventEditor_displaysEventTitle() {
        val event = defaultEvent(title = "考研倒计时")

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithText("考研倒计时").assertIsDisplayed()
    }

    @Test
    fun eventEditor_displaysDateButton() {
        val event = defaultEvent()
        val targetDate = LocalDate.parse(event.targetDate)

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithText("目标日期 $targetDate", substring = true).assertIsDisplayed()
    }

    @Test
    fun eventEditor_displaysPinLabel() {
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithText("置顶显示").assertIsDisplayed()
    }

    @Test
    fun eventEditor_displaysFieldLabel() {
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithText("事件名称").assertIsDisplayed()
    }

    @Test
    fun eventEditor_deleteButtonPresent() {
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = true,
                    canMoveDown = true,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("删除").assertIsDisplayed()
    }

    @Test
    fun eventEditor_deleteCallsOnDelete() {
        var deleted = false
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = { deleted = true },
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("删除").performClick()
        assertTrue("onDelete callback should have been invoked", deleted)
    }

    @Test
    fun eventEditor_moveUpDisabledWhenCannotMoveUp() {
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = true,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("上移").assertIsNotEnabled()
    }

    @Test
    fun eventEditor_moveDownDisabledWhenCannotMoveDown() {
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = true,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("下移").assertIsNotEnabled()
    }

    @Test
    fun eventEditor_moveUpEnabledAndCallsCallback() {
        var movedUp = false
        val event = defaultEvent()

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = true,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = { movedUp = true },
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("上移").assertIsEnabled()
        composeRule.onNodeWithContentDescription("上移").performClick()
        assertTrue("onMoveUp should be called", movedUp)
    }

    @Test
    fun eventEditor_titleChangeCallsOnChange() {
        var updatedEvent: CountdownEvent? = null
        val event = defaultEvent(title = "旧标题")

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = { updatedEvent = it },
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {}
                )
            }
        }

        composeRule.onNodeWithTag("event-title-field").performTextReplacement("新标题")
        assertEquals("新标题", updatedEvent?.title)
    }

    @Test
    fun emptyState_displaysMessage() {
        composeRule.setContent {
            CountdownTheme { EmptyState() }
        }

        composeRule.onNodeWithText(context.getString(R.string.empty_state_hint)).assertIsDisplayed()
    }

    @Test
    fun reminderWithoutPermission_showsSettingsAction() {
        val event = defaultEvent().copy(reminderEnabled = true)
        var openedSettings = false

        composeRule.setContent {
            CountdownTheme {
                CustomEventEditor(
                    event = event,
                    highlighted = false,
                    canMoveUp = false,
                    canMoveDown = false,
                    onChange = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {},
                    notificationsGranted = false,
                    onOpenNotificationSettings = { openedSettings = true }
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.notification_permission_required))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.open_notification_settings))
            .performClick()
        assertTrue(openedSettings)
    }
}
