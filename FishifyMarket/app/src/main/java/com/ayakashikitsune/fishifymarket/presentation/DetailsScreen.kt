package com.ayakashikitsune.fishifymarket.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymarket.datamodel.ShopItem
import com.ayakashikitsune.fishifymarket.datamodel.UserClient
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.ui.theme.FishifyMarketTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ayakashikitsune.fishifymarket.R as localstorage


@Composable
fun DetailsScreen(
    IID: String,
    navHostController: NavHostController,
    vm: FishifyViewModel,
    showSnackbar: (String) -> Unit,
) {

    var shopItem by remember { mutableStateOf(ShopItem()) }
    val localContext = LocalContext.current
    val height = LocalConfiguration.current.screenHeightDp.dp
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(key1 = true) {
        try {
            shopItem = vm.readSpecificShopItem(IID, { showSnackbar(it) })!!
        } catch (e: Exception) {
            navHostController.popBackStack()
        }
    }
    BackHandler() {
        navHostController.popBackStack()
    }
    Scaffold(
        bottomBar = {
            BottomAppBar() {
                OutlinedButton(
                    onClick = { navHostController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Cancel")
                }
                FilledTonalButton(
                    onClick = {
                        coroutine.launch(Dispatchers.IO) {
                            vm.addThisItemToCart(
                                shopItem,
                                withContext(Dispatchers.Main) {
                                    {showSnackbar(it) }
                                }
                            )
                        }
                        navHostController.popBackStack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Add to cart")
                }
            }
        }
    ) {
        val padding = it
        Column() {
            AsyncImage(
                model = ImageRequest.Builder(localContext)
                    .data(shopItem.imageLink)
                    .placeholder(localstorage.drawable.placeholderloadingimage)
                    .error(localstorage.drawable.brokenimage)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(height / 3)
                    .fillMaxWidth()
            )
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "${shopItem.name}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Chiplets(
                        title = "${shopItem.price} php",
                        icon = Icons.Default.Payment,
                        style = MaterialTheme.typography.titleLarge,
                        fontweight = FontWeight.SemiBold,
                    )
                }
            }
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(localContext)
                            .data(shopItem.OID)
                            .placeholder(localstorage.drawable.placeholderloadingimage)
                            .error(localstorage.drawable.brokenimage)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(text = "${shopItem.storeName}")
                }
            }
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${shopItem.description}",
                        modifier = Modifier
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CardOnBottomSheetItem(
    title: String,
    height: Dp,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .height(height)
            .fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun detailspreview() {
    FishifyMarketTheme {
        val shopItem = ShopItem()
        var quantity by remember {
            mutableStateOf(1)
        }
        val userClient = UserClient()
        val coroutine = rememberCoroutineScope()
        val context = LocalContext.current
        val height = LocalConfiguration.current.screenHeightDp.dp
        val state = rememberBottomSheetScaffoldState(
            SheetState(skipPartiallyExpanded = false)
        )
        BottomSheetScaffold(
            scaffoldState = state,
            sheetPeekHeight = height / 1.5f,
            sheetContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {

                    CardOnBottomSheetItem(
                        title = "Quantity ",
                        height = height / 6,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        QuantitySelector(
                            onDecrease = { quantity-- },
                            onIncrease = { quantity++ },
                            quantityVal = { quantity },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    CardOnBottomSheetItem(
                        title = "Address ",
                        height = height / 5,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "${userClient.address}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    CardOnBottomSheetItem(
                        title = "Payment Method ",
                        height = height / 5,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Cash On Delivery",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Row(
                        modifier = Modifier.height(height / 10)
                    ) {
                        OutlinedButton(onClick = { coroutine.launch { state.bottomSheetState.hide() } }) {
                            Text(text = "Cancel")
                        }
                        FilledTonalButton(onClick = { }) {
                            Text(text = "Add to cart")
                        }
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier.padding(it)
            ) {
                Button(onClick = {
                    coroutine.launch {
                        println("pr")
                        state.bottomSheetState.show()
//                    state.bottomSheetState.show()
                    }
                }) {
                    Text(text = "expand")
                }
            }
        }
    }
}