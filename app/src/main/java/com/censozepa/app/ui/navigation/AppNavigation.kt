package com.censozepa.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object MainMenuScreen

@Serializable
object CcaaListScreen

@Serializable
data class ZepaListScreen(val ccaaId: Int, val ccaaName: String)

@Serializable
data class ZepaDetailScreen(val zepaId: String)

@Serializable
object ObservationsScreen

@Serializable
object FavoritesScreen

@Serializable
object NearbyZepasScreen

@Serializable
object BirdMenuScreen

@Serializable
object BirdListScreen

@Serializable
object ZepaSelectorForBirdsScreen

@Serializable
data class ZepaBirdsListScreen(val zepaId: String, val zepaName: String)

@Serializable
object SettingsScreen
