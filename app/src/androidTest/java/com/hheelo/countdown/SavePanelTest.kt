package com.hheelo.countdown

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SavePanelTest {

    @get:Rule
    val composeRule = createComposeRule()

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
                    savedMessage = "已保存并刷新桌面小组件",
                    tintHex = CountdownColorHex.Brand,
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("已保存并刷新桌面小组件").assertIsDisplayed()
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

        composeRule.onNodeWithText("已保存并刷新桌面小组件").assertDoesNotExist()
        composeRule.onNodeWithText("保存失败，请重试").assertDoesNotExist()
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

        composeRule.onNodeWithText(
            "当前内置了 2026 年大陆法定节假日数据；自定义倒计时支持多条配置。后续年份可在 HolidayCalendar.kt 中继续追加。",
            substring = true
        ).assertIsDisplayed()
    }
}
