package mobappdev.example.nback_cimpl

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mobappdev.example.nback_cimpl.ui.screens.HomeScreen
import mobappdev.example.nback_cimpl.ui.screens.GameScreen
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.theme.NBack_CImplTheme
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM

/**
 * This is the MainActivity of the application
 *
 * Your navigation between the two (or more) screens should be handled here
 * For this application you need at least a homescreen (a start is already made for you)
 * and a gamescreen (you will have to make yourself, but you can use the same viewmodel)
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


class MainActivity() : ComponentActivity(), Parcelable {
    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBack_CImplTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val gameViewModel: GameVM = viewModel(factory = GameVM.Factory)

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                vm = gameViewModel,
                                navController = navController // Pass NavController here
                            )
                        }
                        composable("game") {
                            GameScreen(vm = gameViewModel,
                                navController = navController)
                        }
                    }
                }
            }
        }
    }
}