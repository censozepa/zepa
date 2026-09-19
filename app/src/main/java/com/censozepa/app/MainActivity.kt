package com.censozepa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.censozepa.app.ui.navigation.CcaaListScreen
import com.censozepa.app.ui.navigation.SplashScreen
import com.censozepa.app.ui.navigation.ZepaDetailScreen
import com.censozepa.app.ui.navigation.ZepaListScreen
import com.censozepa.app.ui.screens.CcaaListScreenContent
import com.censozepa.app.ui.screens.SplashScreenContent
import com.censozepa.app.ui.screens.ZepaDetailScreenContent
import com.censozepa.app.ui.screens.ZepaListScreenContent
import com.censozepa.app.ui.theme.CensoZEPATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CensoZEPATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CensoZepaApp()
                }
            }
        }
    }
}

@Composable
fun CensoZepaApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = SplashScreen) {
        composable<SplashScreen> {
            SplashScreenContent(
                onTimeout = {
                    navController.navigate(CcaaListScreen) {
                        popUpTo<SplashScreen> { inclusive = true }
                    }
                }
            )
        }
        composable<CcaaListScreen> {
            CcaaListScreenContent(
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}
