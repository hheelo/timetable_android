package com.hheelo.countdown

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SavePanelTest {

    @get:Rule
    val composeRule = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun savePanel_displaysSaveButton() {
        composeRule.setContent {
            CountdownTheme {
                SavePanel(
                    savedMessage = null,
                    tintHex = CountdownColorHex.Brand,
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("保存并刷新小组件").assertIsDisplayed()
    }

    @Test
    fun savePanel_saveButtonCallsOnSave() {
        var saved = false

        composeRule.setContent {
            CountdownTheme {
                SavePanel(
                    savedMessage = null,
                    tintHex = CountdownColorHex.Brand,
                    onSave = { saved = true }
                )
            }
        }

        composeRule.onNodeWithText("保存并刷新小组件").performClick()
        assertTrue("onSave callback should have been invoked", saved)
    }

    @Test
    fun savePanel_displaysSavedMessage() {
        composeRule.setContent {
            CountdownTheme {
                SavePanel(
                    savedMessage = context.getString(R.string.save_success_message),
                    tintHex = CountdownColorHex.Brand,
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.save_success_message)).assertIsDisplayed()
    }

    @Test
    fun savePanel_displaysErrorMessage() {
        composeRule.setContent {
            CountdownTheme {
                SavePanel(
                    savedMessage = "保存失败，请重试",
                    tintHex = CountdownColorHex.Brand,
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("保存失败，请重试").assertIsDisplayed()
    }

    @Test
    fun savePanel_noMessageWhenNull() {
        composeRule.setContent {
            CountdownTheme {
                SavePanel(
                    savedMessage = null,
                    tintHex = CountdownColorHex.Brand,
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.save_success_message)).assertDoesNotExist()
        composeRule.onNodeWithText("保存失败，请重试").assertDoesNotExist()
    }

    @Test
    fun savePanel_disablesButtonWhileSaving() {
        composeRule.setContent {
            CountdownTheme {
                SavePanel(
                    savedMessage = null,
                    tintHex = CountdownColorHex.Brand,
                    onSave = {},
                    isSaving = true
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.saving_and_refreshing_widget))
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun footer_displaysHolidayNoteTitle() {
        composeRule.setContent {
            CountdownTheme { Footer() }
        }

        composeRule.onNodeWithText("节假日说明").assertIsDisplayed()
    }

    @Test
    fun footer_displaysHolidayNoteBody() {
        composeRule.setContent {
            CountdownTheme { Footer() }
        }

        composeRule.onNodeWithText(context.getString(R.string.holiday_info_body)).assertIsDisplayed()
    }
}
