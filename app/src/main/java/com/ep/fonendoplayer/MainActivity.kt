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
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ep.fonendoplayer.navigation.*
import com.ep.fonendoplayer.navigation.ADDRESS
import com.ep.fonendoplayer.screens.CharacteristicsScreen
import com.ep.fonendoplayer.screens.ScannerScreen
import com.ep.fonendoplayer.screens.ServicesScreen
import kotlinx.coroutines.FlowPreview


@FlowPreview
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
                    val viewModel = viewModel<BluetoothViewModel>()
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = SCANNER) {
                        composable(SCANNER) { ScannerScreen(navController, viewModel) }
                        composable(SERVICES, arguments = listOf(navArgument(ADDRESS) { type = NavType.StringType })) { backStackEntry ->
                            // val address = backStackEntry.arguments?.getString("address")
                            ServicesScreen(navController, viewModel)
                        }
                        composable(CHARACTERISTICS, arguments = listOf(navArgument(SERVICE_UUID) { type = NavType.StringType })) { backStackEntry ->
                             val serviceUuId = requireNotNull(backStackEntry.arguments?.getString(SERVICE_UUID)) { "SERVICE_UUID cannot be null" }
                            // FIXME the value taken from arguments includes '{}' we dont need this. Find the proper way to pass argument without it.
                            CharacteristicsScreen(serviceUuId = serviceUuId.substring(1, serviceUuId.lastIndex), viewModel = viewModel)
                        }
                    }

                }
            }
        }
    }
}



@FlowPreview
@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FonendoPlayerTheme {
        val navController = rememberNavController()
        ScannerScreen(navController)
    }
}
