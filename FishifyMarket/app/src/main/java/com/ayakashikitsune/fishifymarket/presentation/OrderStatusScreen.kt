package com.ayakashikitsune.fishifymarket.presentation

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CancelPresentation
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ScheduleSend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymarket.datamodel.OrderItem
import com.ayakashikitsune.fishifymarket.datamodel.OrderStatus
import com.ayakashikitsune.fishifymarket.datamodel.toPerOrderItem
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.ui.theme.FishifyMarketTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymarket.R as localresource


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrderStatusScreen(
    viewModel: FishifyViewModel
) {
    val listOfOrders = viewModel.orderStatusListInfoFlow
    var tabState by remember { mutableStateOf(0) }
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(key1 = true) {
        viewModel.readAllUserclientsOrder()
    }
    val scrollState = rememberScrollState()
    val tabs = listOf(
        OrderStatus.REQUESTING,
        OrderStatus.CONFIRMED,
        OrderStatus.DELIVERY,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED,
    )
    var contentTransform by remember { mutableStateOf(fadeIn() with fadeOut()) }
    val swipperModifier = Modifier.pointerInput(Unit) {
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
                if (pressedpos.x > newPos.x) {
                    //left
                    if (tabState in 0..3) {
                        tabState++
                        contentTransform =
                            fadeIn() + slideInHorizontally(animationSpec = tween(delayMillis = 300)) { it } with fadeOut() + slideOutHorizontally { -it }
                    }
                } else {
                    //right
                    if (tabState in 1..4) {
                        tabState--
                        contentTransform =
                            fadeIn() + slideInHorizontally(animationSpec = tween(delayMillis = 300)) { -it } with fadeOut() + slideOutHorizontally { it }
                    }
                }
                if (tabState >= 3) {
                    coroutine.launch {
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                } else {
                    coroutine.launch {
                        scrollState.scrollTo(0)
                    }
                }
            }
        )
    }
    Column(
        modifier = swipperModifier
    ) {
        TabsRow(
            changeTabState = { tabState = it },
            tabs = tabs,
            currenttab = { tabState },
            scrollState = scrollState,
            coroutine = coroutine
        )
        val grouped by remember(tabState) { derivedStateOf { listOfOrders.filter { it.statusOfOrder == tabs[tabState].state } } }
        AnimatedContent(
            targetState = grouped,
            transitionSpec = { contentTransform }
        ) {
            Column(
                modifier = swipperModifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (grouped.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Empty ${tabs.get(tabState)} orders")
                    }
                } else {
                    grouped.map {
                        OrderStatusItemsPreview(orderItem = it)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusItemsPreview(orderItem: OrderItem) {
    val context = LocalContext.current
    var expand by remember { mutableStateOf(false) }
    val iconExpand by remember { derivedStateOf { if (expand) Icons.Default.ExpandMore else Icons.Default.ExpandLess } }

    Card(
        shape = RoundedCornerShape(0),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        val date = orderItem.datetimePlaced.toDate()
        val simpleDate = SimpleDateFormat("MMM dd yyyy  hh:mma")
        val currentDate = simpleDate.format(date)
        val icon: ImageVector = when (orderItem.statusOfOrder) {
            OrderStatus.REQUESTING.state -> Icons.Default.ScheduleSend
            OrderStatus.CONFIRMED.state -> Icons.Default.Inventory
            OrderStatus.DELIVERY.state -> Icons.Default.LocalShipping
            OrderStatus.DELIVERED.state -> Icons.Default.LocalShipping
            OrderStatus.CANCELLED.state -> Icons.Default.CancelPresentation
            else -> {
                Icons.Default.CancelPresentation
            }
        }
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row() {
                Column() {
                    Text(text = "Order Status", style = MaterialTheme.typography.headlineMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = "   ${orderItem.statusOfOrder}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    if (orderItem.statusOfOrder == OrderStatus.CANCELLED.state) {
                        Text(text = "Reason", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "   -${orderItem.reasonCancelled}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Date ordered", style = MaterialTheme.typography.headlineSmall)
                    Text(text = "   ${currentDate}", style = MaterialTheme.typography.labelLarge)
                    if (orderItem.statusOfOrder == OrderStatus.DELIVERY.state) {
                        Text(text = "On Delivery", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = "   ${simpleDate.format(orderItem.doneDelivery?.toDate())}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
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
                    }",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
                )
            }
        }
    }

}

@Composable
fun TabsRow(
    changeTabState: (Int) -> Unit,
    tabs: List<OrderStatus>,
    currenttab: () -> Int,
    scrollState: ScrollState,
    coroutine: CoroutineScope
) {
    val color = MaterialTheme.colorScheme.tertiary
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {
        tabs.forEachIndexed { index, s ->
            val i = index
            val cardColor =
                animateColorAsState(targetValue = if (i == currenttab()) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            Card(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(100))
                    .clickable {
                        changeTabState(i)
                        if (currenttab() >= 3) {
                            coroutine.launch {
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                        } else {
                            coroutine.launch {
                                scrollState.scrollTo(0)
                            }
                        }
                    },
                shape = RoundedCornerShape(100),
                colors = CardDefaults.cardColors(containerColor = cardColor.value)
            ) {
                Text(
                    text = s.state,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 24.dp)
                        .drawBehind {
                            if (i == currenttab()) {
                                drawLine(
                                    color,
                                    start = Offset(
                                        0f + 6.dp.toPx(),
                                        this.size.height + 5.dp.toPx()
                                    ),
                                    end = Offset(
                                        this.size.width - 6.dp.toPx(),
                                        this.size.height + 5.dp.toPx()
                                    ),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AlertScreenPreview() {
    FishifyMarketTheme() {
//        OrderStatusScreen()
    }
}