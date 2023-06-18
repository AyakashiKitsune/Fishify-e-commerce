package com.ayakashikitsune.fishifymanager.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CancelScheduleSend
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymanager.data.models.OrderItem
import com.ayakashikitsune.fishifymanager.data.models.OrderStatus
import com.ayakashikitsune.fishifymanager.data.models.ShopItem
import com.ayakashikitsune.fishifymanager.data.models.toPerOrderItem
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymanager.R as localresource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DeliveryConfirmationPendingScreen(
    paddingValues: PaddingValues,
    navHostController: NavHostController,
    viewModel: FishifyManagerViewModel
) {
    val corountine = rememberCoroutineScope()
    val onDeliveryInfoState = viewModel.onDeliveryInfoFlow

    val sortByPendingOrder = remember {
        mutableStateOf(OrderStatus.REQUESTING.state)
    }
    val pendingWeight by animateFloatAsState(
        targetValue = if (sortByPendingOrder.value == OrderStatus.REQUESTING.state) 1f else 0.6f
    )
    val onConfirmWeight by animateFloatAsState(
        targetValue = if (sortByPendingOrder.value == OrderStatus.CONFIRMED.state) 1f else 0.6f
    )
    val listSorted =
        remember(sortByPendingOrder.value) { derivedStateOf { onDeliveryInfoState.filter { it.statusOfOrder == sortByPendingOrder.value } } }
    LaunchedEffect(key1 = true) {
        viewModel.readOrders()
    }
    var showAlertDialog by remember {
        mutableStateOf(false)
    }
    var declineOrderHolder by remember { mutableStateOf(OrderItem()) }
    if (showAlertDialog) {
        var reason by remember {
            mutableStateOf("")
        }
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            text = {
                Column() {
                    Text(text = "Which Reason ", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = reason, onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = "Reason ",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CancelScheduleSend,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = "Declining order with reason",
                    style = MaterialTheme.typography.headlineMedium,
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        corountine.launch {
                            viewModel.updateOrder(
                                declineOrderHolder.copy(
                                    statusOfOrder = OrderStatus.CANCELLED.state,
                                    reasonCancelled = reason
                                )
                            )
                            onDeliveryInfoState.remove(declineOrderHolder)
                            showAlertDialog = false
                        }
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAlertDialog = false }) { Text(text = "Cancel") }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabsCards(
                modifier = Modifier.weight(pendingWeight),
                onClick = { sortByPendingOrder.value = OrderStatus.REQUESTING.state },
                isEnable = { sortByPendingOrder.value == OrderStatus.CONFIRMED.state },
                title = "Pending"
            )
            TabsCards(
                modifier = Modifier.weight(onConfirmWeight),
                onClick = { sortByPendingOrder.value = OrderStatus.CONFIRMED.state },
                isEnable = { sortByPendingOrder.value == OrderStatus.REQUESTING.state },
                title = "On Confirmation"
            )
        }
        AnimatedContent(
            targetState = listSorted,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            modifier = Modifier
                .weight(11f)
                .pointerInput(Unit) {
                    var pressedpos = Offset.Zero
                    var newPos = Offset.Zero
                    detectHorizontalDragGestures(
                        onDragStart = {
                            pressedpos = it
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            newPos = change.position
                        },
                        onDragEnd = {
                            if (pressedpos.x < newPos.x) {
                                //left
                                sortByPendingOrder.value = OrderStatus.REQUESTING.state
                            } else {
                                //right
                                sortByPendingOrder.value = OrderStatus.CONFIRMED.state
                            }
                        }
                    )
                }
        ) {
            if (listSorted.value.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Empty ${
                            when (sortByPendingOrder.value) {
                                OrderStatus.REQUESTING.state -> "Pending"
                                OrderStatus.CONFIRMED.state -> "On confirmation"
                                else -> {}
                            }
                        } orders", style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    listSorted.value.map {
                        OnConfirmDeliveryItem(
                            orderItem = it,
                            onItemRemove = {
                                declineOrderHolder = it
                                showAlertDialog = true
                            },
                            setOrdertoConfirmed = {
                                corountine.launch {
                                    viewModel.updateOrder(it.copy(statusOfOrder = OrderStatus.CONFIRMED.state))
                                }
                            },
                            setOrdertoDelivery = {
                                corountine.launch {
                                    viewModel.incrementSold(it)
                                    viewModel.updateOrder(it.copy(statusOfOrder = OrderStatus.DELIVERY.state, doneDelivery = Timestamp.now()))
                                }
                            }
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun OnConfirmDeliveryItem(
    orderItem: OrderItem,
    onItemRemove: (OrderItem) -> Unit,
    setOrdertoConfirmed: (OrderItem) -> Unit,
    setOrdertoDelivery: (OrderItem) -> Unit,
) {
    val context = LocalContext.current
    var expand by remember { mutableStateOf(false) }
    val iconExpand by remember { derivedStateOf { if (expand) Icons.Default.ExpandMore else Icons.Default.ExpandLess } }
    Card(
        shape = RoundedCornerShape(0),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row() {
                Column() {
                    Text(text = "OrderID", style = MaterialTheme.typography.headlineSmall)
                    Text(text = "   ${orderItem.OrderID}")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { expand = !expand }) {
                    Icon(imageVector = iconExpand, contentDescription = null)
                }
            }
            AnimatedVisibility(
                visible = expand,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 400,
                        delayMillis = 200
                    )
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    orderItem.orders.map {
                        val item = it.toPerOrderItem()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .error(localresource.drawable.brokenimage)
                                    .placeholder(localresource.drawable.placeholderloadingimage)
                                    .data(item.imageLink)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(vertical = 8.dp)
                                    .weight(1f)
                            )
                            Text(
                                text = "${item.name}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.quantity}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${item.quantity * item.price}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Spacer(modifier = Modifier.weight(4f))
                Text(
                    text = "Total: ${
                        orderItem.orders.map { it.toPerOrderItem() }.map { it.price * it.quantity }
                            .reduce { acc, d -> acc + d }
                    }", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
                )
            }
            AnimatedVisibility(
                visible = expand,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut(tween(durationMillis = 200)) + slideOutVertically(
                    tween(
                        durationMillis = 200
                    )
                ) { -it / 2 },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if(orderItem.statusOfOrder != OrderStatus.CONFIRMED.state){
                        OutlinedButton(
                            onClick = { onItemRemove(orderItem) },
                            modifier = Modifier
                                .padding(4.dp)
                                .weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CancelScheduleSend,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                text = "Decline with Reason",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    FilledTonalButton(
                        onClick = {
                            if (orderItem.statusOfOrder == OrderStatus.REQUESTING.state) {
                                setOrdertoConfirmed(orderItem)
                            } else if (orderItem.statusOfOrder == OrderStatus.CONFIRMED.state) {
                                setOrdertoDelivery(orderItem)
                            }
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = "${
                                when (orderItem.statusOfOrder) {
                                    OrderStatus.CONFIRMED.state -> "Deliver Order"
                                    OrderStatus.REQUESTING.state -> "Accept Order"
                                    else -> ""
                                }
                            }", style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsCards(
    modifier: Modifier,
    onClick: () -> Unit,
    isEnable: () -> Boolean,
    title: String
) {
    Card(
        modifier = modifier,
        enabled = isEnable(),
        onClick = { onClick() },
        shape = RoundedCornerShape(100),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.tertiary),
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            disabledContentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.tertiaryContainer)
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
                .wrapContentSize(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnConfirmDeliveryScreenPrev() {
    // make a ondelivery tiles and screen
    // make a pending screen also the same thing
    FishifyManagerTheme() {
        Column() {
            OnConfirmDeliveryItem(
                orderItem = OrderItem(
                    orders = arrayListOf(
                        ShopItem().toPerOrderItem().copy(quantity = 3).toHashMap(),
                        ShopItem().toPerOrderItem().toHashMap()
                    )
                ), onItemRemove = {}, {}, {})
        }
    }
}