package com.censozepa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.censozepa.app.ui.navigation.BirdListScreen
import com.censozepa.app.ui.navigation.BirdMenuScreen
import com.censozepa.app.ui.navigation.CcaaListScreen
import com.censozepa.app.ui.navigation.FavoritesScreen
import com.censozepa.app.ui.navigation.GoogleDriveSyncScreen
import com.censozepa.app.ui.navigation.MainMenuScreen
import com.censozepa.app.ui.navigation.NearbyZepasScreen
import com.censozepa.app.ui.navigation.ObservationsScreen
import com.censozepa.app.ui.navigation.SettingsScreen
import com.censozepa.app.ui.navigation.ZepaBirdsListScreen
import com.censozepa.app.ui.navigation.ZepaDetailScreen
import com.censozepa.app.ui.navigation.ZepaListScreen
import com.censozepa.app.ui.navigation.ZepaSelectorForBirdsScreen
import com.censozepa.app.ui.screens.BirdListScreenContent
import com.censozepa.app.ui.screens.BirdMenuScreenContent
import com.censozepa.app.ui.screens.CcaaListScreenContent
import com.censozepa.app.ui.screens.FavoritesScreenContent
import com.censozepa.app.ui.screens.GoogleDriveSyncScreenContent
import com.censozepa.app.ui.screens.MainMenuScreenContent
import com.censozepa.app.ui.screens.NearbyZepasScreenContent
import com.censozepa.app.ui.screens.ObservationsScreenContent
import com.censozepa.app.ui.screens.SettingsScreenContent
import com.censozepa.app.ui.screens.ZepaBirdsListScreenContent
import com.censozepa.app.ui.screens.ZepaDetailScreenContent
import com.censozepa.app.ui.screens.ZepaListScreenContent
import com.censozepa.app.ui.screens.ZepaSelectorForBirdsScreenContent
import com.censozepa.app.ui.theme.CensoZEPATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialZepaId = intent?.getStringExtra("NAVIGATE_TO_ZEPA_ID")
        setContent {
            CensoZEPATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CensoZepaApp(initialZepaId)
                }
            }
        }
    }
}

@Composable
fun CensoZepaApp(initialZepaId: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(initialZepaId) {
        if (!initialZepaId.isNullOrBlank()) {
            navController.navigate(MainMenuScreen) {
                popUpTo(0)
            }
            navController.navigate(ZepaDetailScreen(initialZepaId))
        }
    }

    NavHost(navController = navController, startDestination = MainMenuScreen) {
        composable<MainMenuScreen> {
            MainMenuScreenContent(
                onSelectProvince = { navController.navigate(CcaaListScreen) },
                onViewObservations = { navController.navigate(ObservationsScreen) },
                onViewFavorites = { navController.navigate(FavoritesScreen) },
                onViewNearbyZepas = { navController.navigate(NearbyZepasScreen) },
                onViewBirdList = { navController.navigate(BirdMenuScreen) },
                onOpenSettings = { navController.navigate(SettingsScreen) }
            )
        }

        composable<CcaaListScreen> {
            CcaaListScreenContent(
                onBack = { navController.popBackStack() },
                onCcaaClick = { ccaa ->
                    navController.navigate(ZepaListScreen(ccaa.id, ccaa.nombre))
                }
            )
        }
        
        composable<ZepaListScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<ZepaListScreen>()
            ZepaListScreenContent(
                ccaaId = route.ccaaId,
                ccaaName = route.ccaaName,
                onBack = { navController.popBackStack() },
                onZepaClick = { zepa ->
                    navController.navigate(ZepaDetailScreen(zepa.id_codigo))
                }
            )
        }
        
        composable<ZepaDetailScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<ZepaDetailScreen>()
            ZepaDetailScreenContent(
                zepaId = route.zepaId,
                onBack = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate(MainMenuScreen) {
                        popUpTo<MainMenuScreen> { inclusive = false }
                    }
                }
            )
        }

        composable<ObservationsScreen> {
            ObservationsScreenContent(
                onBack = { navController.popBackStack() }
            )
        }

        composable<FavoritesScreen> {
            FavoritesScreenContent(
                onBack = { navController.popBackStack() },
                onZepaClick = { zepaId ->
                    navController.navigate(ZepaDetailScreen(zepaId))
                }
            )
        }

        composable<NearbyZepasScreen> {
            NearbyZepasScreenContent(
                onZepaClick = { zepaId ->
                    navController.navigate(ZepaDetailScreen(zepaId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<BirdMenuScreen> {
            BirdMenuScreenContent(
                onAllBirds = { navController.navigate(BirdListScreen) },
                onBirdsByZepa = { navController.navigate(ZepaSelectorForBirdsScreen) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<BirdListScreen> {
            BirdListScreenContent(
                onBack = { navController.popBackStack() }
            )
        }

        composable<ZepaSelectorForBirdsScreen> {
            ZepaSelectorForBirdsScreenContent(
                onZepaSelected = { zId, zName ->
                    navController.navigate(ZepaBirdsListScreen(zId, zName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<ZepaBirdsListScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<ZepaBirdsListScreen>()
            ZepaBirdsListScreenContent(
                zepaId = route.zepaId,
                zepaName = route.zepaName,
                onBack = { navController.popBackStack() }
            )
        }

        composable<GoogleDriveSyncScreen> {
            GoogleDriveSyncScreenContent(
                onBack = { navController.popBackStack() }
            )
        }

        composable<SettingsScreen> {
            SettingsScreenContent(
                onBack = { navController.popBackStack() },
                onOpenGoogleDriveSync = { navController.navigate(GoogleDriveSyncScreen) }
            )
        }
    }
}
