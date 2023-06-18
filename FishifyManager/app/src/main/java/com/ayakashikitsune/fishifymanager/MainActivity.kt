package com.ayakashikitsune.fishifymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.domain.ScreenDestination
import com.ayakashikitsune.fishifymanager.domain.ScreenDestination.AddItemScreen.addToRoute
import com.ayakashikitsune.fishifymanager.presentation.AboutScreen
import com.ayakashikitsune.fishifymanager.presentation.AddItemScreen
import com.ayakashikitsune.fishifymanager.presentation.DeliveryConfirmationPendingScreen
import com.ayakashikitsune.fishifymanager.presentation.EditProfileScreen
import com.ayakashikitsune.fishifymanager.presentation.FABaddItem
import com.ayakashikitsune.fishifymanager.presentation.LoginScreen
import com.ayakashikitsune.fishifymanager.presentation.ModifyShopsItemScreen
import com.ayakashikitsune.fishifymanager.presentation.OverViewScreen
import com.ayakashikitsune.fishifymanager.presentation.ProfileScreen
import com.ayakashikitsune.fishifymanager.presentation.SignUpRegistrationScreen
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FishifyManagerTheme {

                val fishifyManagerViewModel = viewModel<FishifyManagerViewModel>()
                val navcontrol = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val navstate by navcontrol.currentBackStackEntryAsState()
                val nestedScrollConnection = rememberNestedScrollInteropConnection()

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        when (navstate?.destination?.route) {
                            ScreenDestination.OverViewScreen.destination,
                            ScreenDestination.ModifyShopsItemScreen.destination,
                            ScreenDestination.OnConfirmDeliveryScreen.destination,
                            ScreenDestination.ProfileScreen.destination -> {
                                Bottombar(
                                    navHostController = navcontrol,
                                    navstate = {
                                        navstate?.destination?.route
                                            ?: ScreenDestination.OverViewScreen.destination
                                    }
                                )

                            }

                            else -> {

                            }
                        }
                    },
                    topBar = {
                        Topbar(
                            screenDestination = { navstate?.destination?.route ?: "" },
                            navHostController = navcontrol,
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        if (navstate?.destination?.route == ScreenDestination.ModifyShopsItemScreen.destination)
                            FABaddItem {
                                navcontrol.navigate(
                                    ScreenDestination.AddItemScreen.destination
                                        .addToRoute("true")
                                        .addToRoute("0")
                                )
                            }
                    },
                ) {
                    val padd = it
                    NavHost(
                        navController = navcontrol,
                        startDestination = ScreenDestination.LoginScreen.destination
                    ) {
                        composable(ScreenDestination.LoginScreen.destination) {
                            LoginScreen(
                                navControl = navcontrol,
                                viewModel = fishifyManagerViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }
                        composable(ScreenDestination.SignUpRegistrationScreen.destination) {
                            SignUpRegistrationScreen(
                                navHostController = navcontrol,
                                snackbarHostState = snackbarHostState,
                                vm = fishifyManagerViewModel
                            )
                        }

                        composable(ScreenDestination.OverViewScreen.destination) {
                            OverViewScreen(
                                navController = navcontrol,
                                viewModel = fishifyManagerViewModel,
                                snackbarHostState = snackbarHostState,
                                paddingValues = padd
                            )
                        }
                        composable(ScreenDestination.ModifyShopsItemScreen.destination) {
                            ModifyShopsItemScreen(
                                paddingValues = padd,
                                viewModel = fishifyManagerViewModel,
                                navHostController = navcontrol,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        composable(
                            route = ScreenDestination.AddItemScreen.destination.addToRoute("{isAddItem}")
                                .addToRoute("{index}"),
                            arguments = listOf(
                                navArgument("isAddItem") {
                                    type = NavType.BoolType
                                },
                                navArgument("index") {
                                    type = NavType.IntType
                                }
                            )
                        ) { backstack ->
                            AddItemScreen(
                                fishifyManagerViewModel = fishifyManagerViewModel,
                                snackbarHostState = snackbarHostState,
                                navHostController = navcontrol,
                                paddingValues = padd,
                                isAddItem = backstack.arguments!!.getBoolean("isAddItem"),
                                index = backstack.arguments?.getInt("index")
                            )
                        }

                        composable(ScreenDestination.OnConfirmDeliveryScreen.destination) {
                            DeliveryConfirmationPendingScreen(
                                navHostController = navcontrol,
                                paddingValues = padd,
                                viewModel = fishifyManagerViewModel
                            )
                        }
                        composable(ScreenDestination.ProfileScreen.destination) {
                            ProfileScreen(
                                paddingValues = padd,
                                navHostController = navcontrol,
                                snackbarHostState = snackbarHostState,
                                viewModel = fishifyManagerViewModel
                            )
                        }
                        composable(ScreenDestination.AboutScreen.destination) {
                            AboutScreen()
                        }
                        composable(ScreenDestination.EditProfileScreen.destination){
                            EditProfileScreen(
                                viewModel = fishifyManagerViewModel,
                                navHostController = navcontrol
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Topbar(
    screenDestination: () -> String,
    navHostController: NavHostController
) {
    when (screenDestination()) {
        ScreenDestination.OverViewScreen.destination -> {
            TopAppBar(title = { Text(text = ScreenDestination.OverViewScreen.title) })
        }

        ScreenDestination.OnConfirmDeliveryScreen.destination -> {
            CenterAlignedTopAppBar(title = { Text(text = ScreenDestination.OnConfirmDeliveryScreen.title) })
        }

        ScreenDestination.AddItemScreen.destination -> {
            TopAppBar(
                title = { Text(text = ScreenDestination.AddItemScreen.title) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }

        ScreenDestination.ProfileScreen.destination -> {
            TopAppBar(title = { Text(text = ScreenDestination.ProfileScreen.title) })
        }

        ScreenDestination.ModifyShopsItemScreen.destination -> {
            MediumTopAppBar(title = { Text(text = ScreenDestination.ModifyShopsItemScreen.title) })
        }

        else -> {

        }
    }
}

@Composable
fun Bottombar(
    navstate: () -> String,
    navHostController: NavHostController
) {
    val list = listOf(
        ScreenDestination.OverViewScreen,
        ScreenDestination.ModifyShopsItemScreen,
        ScreenDestination.OnConfirmDeliveryScreen,
        ScreenDestination.ProfileScreen
    )
    NavigationBar() {
        list.forEach {
            NavigationBarItem(
                selected = navstate() == it.destination,
                onClick = {
                    navHostController.navigate(it.destination)
                },
                icon = { Icon(imageVector = it.icon, contentDescription = it.title) },
                label = { Text(text = it.title, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}