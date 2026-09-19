package com.censozepa.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashScreen

@Serializable
object CcaaListScreen

@Serializable
data class ZepaListScreen(val ccaaId: Int, val ccaaName: String)

@Serializable
data class ZepaDetailScreen(val zepaId: String)
