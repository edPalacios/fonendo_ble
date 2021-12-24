package com.ep.fonendoplayer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.tooling.preview.Preview
import com.ep.fonendoplayer.ui.theme.FonendoPlayerTheme
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ep.fonendoplayer.screens.CharacteristicsScreen
import com.ep.fonendoplayer.screens.ScannerScreen
import com.ep.fonendoplayer.screens.ServicesScreen
import com.juul.kable.Advertisement
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow


@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to make bluetooth work: to find the devices we need to access the location of the user, so permission is requried to the user
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1234) // ignore the request code but be sure you dont deny permissions!!

        // inside here we build the view
        setContent {
            FonendoPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "scanner") {
                        composable("scanner") { ScannerScreen() }
                        composable("services/{address}", arguments = listOf(navArgument("address") { type = NavType.StringType })) { backStackEntry ->
                            val address = backStackEntry.arguments?.getString("address")
                            ServicesScreen()
                        }
                        composable("characteristics") { CharacteristicsScreen() }
                    }

                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FonendoPlayerTheme {
        ScannerScreen()
    }
}
