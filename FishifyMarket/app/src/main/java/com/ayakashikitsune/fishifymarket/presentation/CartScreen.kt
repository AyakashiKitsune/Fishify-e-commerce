package com.ayakashikitsune.fishifymarket.presentation

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymarket.datamodel.PerOrderItem
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.ui.theme.FishifyMarketTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymarket.R as localresource

@Composable
fun CartScreen(
    vm: FishifyViewModel,
    snackbarHostState: SnackbarHostState
) {
    val list by remember(vm.listofCartItemFlow) { derivedStateOf { vm.listofCartItemFlow } }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        vm.readUserCart()
    }

    CartList(list = { list }, context = context, vm = vm, snackbarHostState = snackbarHostState)
}

@Composable
fun CartList(
    list: () -> List<PerOrderItem>,
    context: Context,
    vm: FishifyViewModel,
    snackbarHostState: SnackbarHostState
) {
    val lazyListState = rememberLazyListState()
    val listOfItemsMarked = remember { mutableStateListOf<PerOrderItem>() }
    val coroutine = rememberCoroutineScope()
    val showSummary by remember {
        derivedStateOf {
            listOfItemsMarked.size > 0
        }
    }
    val totalPrice by remember {
        derivedStateOf {
            if (listOfItemsMarked.isEmpty()) {
                0
            } else {
                listOfItemsMarked.map { it.price * it.quantity }
                    .reduce { acc, price -> acc + price }
            }
        }
    }

    if (list().isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Empty Cart", style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Column() {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(6f),
                state = lazyListState
            ) {
                list().groupBy { it.ownerID }
                    .map {
                        item {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(
                                    MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, start = 8.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(localresource.drawable.stick) // fetch image store
                                            .placeholder(localresource.drawable.placeholderloadingimage)
                                            .error(localresource.drawable.brokenimage)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .size(30.dp)
                                            .background(Color.White, MaterialTheme.shapes.small)
                                    )
                                    Text(
                                        text = it.value[0].storeName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(36.dp),
                                    modifier = Modifier.padding(
                                        bottom = 12.dp
                                    )
                                ) {
                                    it.value.forEach {
                                        CartTiles(
                                            context = context,
                                            perOrderItem = it,
                                            addItem = {
                                                listOfItemsMarked.add(it)
                                            },
                                            removeItem = {
                                                var i = -1
                                                listOfItemsMarked.forEachIndexed { index, perOrderItem ->
                                                    if (it.IID == perOrderItem.IID) {
                                                        i = index
                                                    }
                                                }
                                                if (i != -1) {
                                                    listOfItemsMarked.removeAt(i)
                                                }

                                            },
                                            quantityUpdated = { quantity, orderitem ->
                                                var i = -1
                                                listOfItemsMarked.forEachIndexed { index, perOrderItem ->
                                                    if (orderitem.IID == perOrderItem.IID) {
                                                        i = index
                                                    }
                                                }
                                                if (i != -1) {
                                                    listOfItemsMarked.set(
                                                        i,
                                                        orderitem.copy(quantity = quantity)
                                                    )
                                                }
                                                Log.d("test", listOfItemsMarked.toString())
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
            AnimatedVisibility(
                visible = showSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (listOfItemsMarked.size == 0) 1f else listOfItemsMarked.size.toFloat() + 2f)
                    .animateContentSize(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium.copy(
                        bottomEnd = CornerSize(0.dp),
                        bottomStart = CornerSize(0.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Summary", style = MaterialTheme.typography.headlineSmall)
                        Column(
                            modifier = Modifier.animateContentSize()
                        ) {
                            listOfItemsMarked.map {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .placeholder(localresource.drawable.placeholderloadingimage)
                                            .error(localresource.drawable.brokenimage)
                                            .data(it.imageLink)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .weight(1f)
                                    )
                                    Text(
                                        text = "${it.name}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${it.quantity}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${it.price}php",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)

                                    )
                                }
                            }
                        }
                        Spacer(Modifier.padding(12.dp))
                        Text(text = "Total Price", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "$totalPrice php",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Button(onClick = {
                            coroutine.launch(Dispatchers.IO) {
                                val listofperorder = arrayListOf<HashMap<String, Any>>()
                                listOfItemsMarked.map {
                                    listofperorder.add(it.toHashMap())
                                }
                                vm.confirmPurchase(
                                    perOrderItem = listofperorder,
                                    onSuccess = {
                                        coroutine.launch(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                "Order Placed",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onFail =
                                    {
                                        coroutine.launch(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                "$it",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                                vm.removeCartItems {
                                    val newOrderlist: ArrayList<HashMap<String, Any>>
                                    if (list().size == listOfItemsMarked.size) {
                                        newOrderlist = arrayListOf()
                                    } else {
                                        newOrderlist = arrayListOf<HashMap<String, Any>>().apply {
                                            addAll(list().filterNot { listOfItemsMarked.contains(it) }
                                                .map { it.toHashMap() })
                                        }
                                    }
                                    it.copy(ordersList = newOrderlist)
                                }
                            }
                        }) {
                            Text(text = "Confirm Purchase")
                        }
                    }
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTiles(
    context: Context,
    perOrderItem: PerOrderItem,
    addItem: (PerOrderItem) -> Unit,
    removeItem: (PerOrderItem) -> Unit,
    quantityUpdated: (Int, PerOrderItem) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    val bol = remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(0)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
        ) {
            IconToggleButton(
                checked = bol.value,
                onCheckedChange = {
                    bol.value = !bol.value
                    if (bol.value) {
                        addItem(perOrderItem)
                    } else {
                        removeItem(perOrderItem)
                    }
                },
                content = {
                    Icon(
                        imageVector = if (bol.value) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null
                    )
                }
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(perOrderItem.imageLink)
                    .placeholder(localresource.drawable.placeholderloadingimage)
                    .error(localresource.drawable.brokenimage)
                    .crossfade(true)
                    .build(),
                contentDescription = perOrderItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(90.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.small)

            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(3f),
            ) {
                Text(
                    text = perOrderItem.name[0].uppercase() + perOrderItem.name.substring(1),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Chiplets(
                            title = "\u20B1${perOrderItem.price}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontweight = FontWeight.Bold,
                            icon = Icons.Default.Payment,
                            tint = MaterialTheme.colorScheme.tertiary,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
                QuantitySelector(
                    onDecrease = {
                        if (quantity > 1) {
                            quantity--
                            quantityUpdated(quantity, perOrderItem)
                        }
                    },
                    onIncrease = {
                        quantity++
                        quantityUpdated(quantity, perOrderItem)
                    },
                    quantityVal = { quantity },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun QuantitySelector(
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    quantityVal: () -> Int,
    size: Dp = 30.dp,
    style: TextStyle
) {
    Card(
        shape = RoundedCornerShape(100),
        modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedIconButton(
                onClick = { onDecrease() },
                content = {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null
                    )
                },
                modifier = Modifier.size(size)
            )

            Text(
                text = "${quantityVal()} KL",
                style = style,
                modifier = Modifier.padding(4.dp)
            )
            FilledIconButton(
                onClick = { onIncrease() },
                content = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                modifier = Modifier.size(size)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun Cartscreenpreview() {
//    val service = FirebaseService.FirebaseFireStoreItemService()
//    val service1 = FirebaseService.FirebaseFireStoreOrderService()
//    var list = remember { mutableStateOf(listOf<ShopItem>()) }
//    var result = remember { mutableStateOf(listOf<OrderItem>()) }
//    val corountine = rememberCoroutineScope()
//    val localContext = LocalContext.current
//    LaunchedEffect(key1 = true) {
//        corountine.launch(Dispatchers.IO) {
////            val sellers = service.sellersList()
////            list.value = service.readAllShopItemData(sellers, {})
//            result.value = service1.readAllOrders(
//                "",
//                OrderStatus.SITTINGONCART
//            ) ?: listOf<OrderItem>()
//        }
//    }

    FishifyMarketTheme(darkTheme = true) {
//        CartList(
//            list = listOf(ShopItem().toPerOrderItem(), ShopItem().toPerOrderItem()),
//            context = LocalContext.current
//        )
    }
    //FakeData.salesfake
}
