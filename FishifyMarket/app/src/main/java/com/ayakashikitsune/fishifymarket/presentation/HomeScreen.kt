package com.ayakashikitsune.fishifymarket.presentation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ayakashikitsune.fishifymanager.presentation.LoginScreen
import com.ayakashikitsune.fishifymanager.presentation.SignUpRegistrationScreen
import com.ayakashikitsune.fishifymarket.datamodel.ShopItem
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.domain.NavigationItemList
import com.ayakashikitsune.fishifymarket.domain.NavigationItemList.AboutNav.addToRoute
import com.ayakashikitsune.fishifymarket.domain.ViewItemAs
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    vm: FishifyViewModel
) {
    val list = vm.shopItemInfoFlow

    val lazyGridState = rememberLazyGridState()
    val navHostController = rememberNavController()
    var navState by remember { mutableStateOf<NavigationItemList>(NavigationItemList.HomeNav) }
    val statenav by navHostController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val snackbarHostState = SnackbarHostState()
    Scaffold(
        topBar = { AppbarComponent(title = { statenav?.destination?.route }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                changeState = {
                    navState = it
                    navHostController.navigate(it.title) {
                        popUpTo(it.title) { inclusive = true }
                    }
                },
                getState = { navState },
                navstate = { statenav?.destination?.route },
            )
        },
        modifier = Modifier.navigationBarsPadding(),
    ) { paddingval ->
        BackHandler() {
            navState = NavigationItemList.HomeNav
            navHostController.navigate(NavigationItemList.HomeNav.title) {
                popUpTo(NavigationItemList.HomeNav.title) { inclusive = true }
            }
        }
        Surface(
            modifier = Modifier
                .padding(paddingval)
                .fillMaxSize()
        ) {
            NavHost(
                navController = navHostController,
                startDestination = NavigationItemList.LoginNav.title,
            ) {
                composable(NavigationItemList.LoginNav.title) {
                    LoginScreen(
                        navControl = navHostController,
                        snackbarHostState = snackbarHostState,
                        viewModel = vm
                    )
                }
                composable(NavigationItemList.SignUpNav.title) {
                    SignUpRegistrationScreen(
                        navHostController = navHostController,
                        snackbarHostState = snackbarHostState,
                        vm = vm,
                    )
                }
                composable(NavigationItemList.HomeNav.title) {
                    LaunchedEffect(key1 = true){
                        vm.readAllShopItem {
                            coroutine.launch {
                                snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                            }
                        }
                    }
                    HomeScreenList(
                        lazyGridState = lazyGridState,
                        list = list,
                        context = context,
                        navHostController = navHostController
                    )
                }
                composable(NavigationItemList.SearchNav.title) {
                    SearchScreen(
                        vm = vm,
                        navHostController = navHostController
                    )
                }
                composable(NavigationItemList.CartNav.title) {
                    CartScreen(
                        vm = vm,
                        snackbarHostState = snackbarHostState
                    )
                }
                composable(NavigationItemList.OrdersNav.title) {
                    OrderStatusScreen(
                        viewModel = vm
                    )
                }
                composable(NavigationItemList.ProfileNav.title) {
                    ProfileScreen(
                        vm = vm,
                        navHostController = navHostController,
                        snackbarHostState = snackbarHostState
                    )
                }
                composable(NavigationItemList.EditprofileNav.title) {
                    EditProfileScreen(
                        viewModel = vm,
                        navHostController = navHostController
                    )
                }
                composable(NavigationItemList.AboutNav.title) {
                    AboutScreen(
                        navHostController
                    )
                }
                composable(
                    NavigationItemList.DetailsNav.title.addToRoute("{iid}"),
                    arguments = listOf(
                        navArgument("iid") {
                            type = NavType.StringType
                        }
                    )
                ) { backstack ->
                    DetailsScreen(
                        IID = backstack.arguments?.getString("iid") ?: "",
                        navHostController = navHostController,
                        vm = vm,
                        showSnackbar = {
                            coroutine.launch {
                                snackbarHostState.showSnackbar(
                                    it,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenList(
    lazyGridState: LazyGridState,
    list: List<ShopItem>,
    context: Context,
    navHostController: NavHostController
) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list.size) {
                ShopItemUI(
                    shopItem = list[it],
                    viewItemAs = ViewItemAs.GridView,
                    context = context,
                    onclick = {
                        navHostController.navigate(
                            NavigationItemList.DetailsNav.title.addToRoute(
                                it
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(
    changeState: (NavigationItemList) -> Unit,
    getState: () -> NavigationItemList,
    navstate: () -> String?,
) {

    val navlist = listOf(
        NavigationItemList.HomeNav,
        NavigationItemList.SearchNav,
        NavigationItemList.CartNav,
        NavigationItemList.OrdersNav,
        NavigationItemList.ProfileNav,
    )
    when (navstate()) {
        NavigationItemList.HomeNav.title,
        NavigationItemList.SearchNav.title,
        NavigationItemList.CartNav.title,
        NavigationItemList.ProfileNav.title,
        NavigationItemList.OrdersNav.title -> {
            NavigationBar {
                navlist.map {
                    NavigationBarItem(
                        selected = getState() == it,
                        onClick = {
                            changeState(it)
                        },
                        label = { Text(text = it.title) },
                        icon = { Icon(imageVector = it.icon, contentDescription = it.title) }
                    )
                }
            }
        }

        NavigationItemList.LoginNav.title,
        NavigationItemList.SignUpNav.title -> {
        }

        else -> {}
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppbarComponent(
    title: () -> String?,
) {

    when (title()) {
        NavigationItemList.HomeNav.title,
        NavigationItemList.SearchNav.title -> {
            CenterAlignedTopAppBar(
                title = { Text(text = title() ?: "") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            )
        }

        NavigationItemList.OrdersNav.title,
        NavigationItemList.CartNav.title,
        NavigationItemList.AboutNav.title,
        NavigationItemList.ProfileNav.title -> {
            TopAppBar(
                title = { Text(text = title() ?: "") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
            )
        }

        else -> {

        }
    }
}


@Preview(showSystemUi = true, showBackground = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun Homepreview() {
//    HomeScreen(vm = viewModel())
    Scaffold(
        bottomBar = {
            BottomAppBar() {
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Cancel")
                }
                FilledTonalButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Add to cart")
                }
            }
        }
    ) {
        val p = it
    }
}