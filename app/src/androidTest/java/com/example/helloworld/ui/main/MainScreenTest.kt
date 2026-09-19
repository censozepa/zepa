package com.censozepa.app.ui.main

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.censozepa.app.MainActivity
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun appLaunchesSuccessfully() {
    // Basic test ensuring MainActivity launches
  }
}
