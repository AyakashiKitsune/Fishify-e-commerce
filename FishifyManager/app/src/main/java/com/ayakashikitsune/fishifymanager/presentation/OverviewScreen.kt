package com.ayakashikitsune.fishifymanager.presentation

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.core.graphics.decodeBitmap
import androidx.navigation.NavHostController
import com.ayakashikitsune.fishifymanager.data.models.AnnoucementAction
import com.ayakashikitsune.fishifymanager.data.models.AnnoucementData
import com.ayakashikitsune.fishifymanager.data.models.OrderStatus
import com.ayakashikitsune.fishifymanager.data.models.ShopItem
import com.ayakashikitsune.fishifymanager.data.models.Sold
import com.ayakashikitsune.fishifymanager.data.models.toPerOrderItem
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OverViewScreen(
    navController: NavHostController,
    viewModel: FishifyManagerViewModel,
    paddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState
) {

    val localCoroutine = rememberCoroutineScope()
    val shopItem = viewModel.shopItemInfoFlow
    val orders = viewModel.onDeliveryInfoFlow
    val localHeight = LocalConfiguration.current.screenHeightDp.dp
    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {

            /*when launch get list of all shopItems for this user */
            viewModel.readAllShopItem(showSnackBar = {
                localCoroutine.launch {
                    snackbarHostState.showSnackbar(
                        message = it,
                        duration = SnackbarDuration.Short
                    )
                }
            })
            viewModel.readOrders()
        }
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {


        SalesOverview(
            data = shopItem,
            modifier = Modifier.aspectRatio(3 / 2f)
        )
        StatisticView(
            modifier = Modifier.aspectRatio(2f / 3f),
            data = orders.sortedBy { it.datetimePlaced }
                .filterNot { it.statusOfOrder == OrderStatus.CANCELLED.state }.take(4).map {
                it.orders.map { it.toPerOrderItem().quantity }.reduce { acc, i -> acc + i }
            },
            xLabels = orders.sortedBy { it.datetimePlaced }
                .filterNot { it.statusOfOrder == OrderStatus.CANCELLED.state }.take(4)
                .map { "${SimpleDateFormat("MMM dd\nhh:mma").format(it.datetimePlaced.toDate())}" },
            title = "Sold Statistics"
        )
    }

}

@Composable
fun BannerOverview(announcementData: AnnoucementData, viewModel: FishifyManagerViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var image by remember{ mutableStateOf(ImageBitmap(1,1)) }
    with(announcementData){
        Card(
            modifier = Modifier.padding(8.dp)
                .drawWithCache {
                    this.onDrawBehind {
                        drawImage(
                            image,
                        )
                    }
                }
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(text = description, style = MaterialTheme.typography.headlineMedium)
            Text(text =
                when(action){
                    AnnoucementAction.GOTO.state -> {"$action item : $afterAction" }
                    AnnoucementAction.POPOUT.state -> {"close banner"}
                    else ->{"none"}
                } , style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun SalesOverview(
    data: List<ShopItem>,
    modifier: Modifier,
) {
    val listOfColumns = arrayOf("Rank", "Name", "Sold")
    val weigthDistribution = arrayOf(1f, 2f, 1f)
    val namePosition = arrayOf(TextAlign.Center, TextAlign.Start, TextAlign.Center)
    var rank = 1

    LaunchedEffect(key1 = true) {
        rank = 1
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 18.dp)
            .clickable {

            }
    ) {
        Text(
            text = "Sales OverView",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
        if (data.isEmpty()) {
            Text(
                text = "There are no sales made",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
        } else {
            TableTabs(
                listNameColumns = listOfColumns,
                weigthDistribution = weigthDistribution,
                namePosition = namePosition
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                data.sortedBy { it.name }.sortedByDescending { it.sold }.map {
                    SalesTiles(
                        rank = rank++,
                        shopItem = it,
                        weigthDistribution = weigthDistribution
                    )
                }
            }
        }
    }
}

@Composable
fun TableTabs(
    listNameColumns: Array<String>,
    weigthDistribution: Array<Float>,
    namePosition: Array<TextAlign>
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {

        for (i in 0..listNameColumns.size - 1) {
            Card(
                modifier = Modifier
                    .weight(weigthDistribution[i])
                    .padding(horizontal = 4.dp),
            ) {
                Text(
                    text = listNameColumns[i],
                    textAlign = namePosition[i],
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SalesTiles(
    rank: Int,
    shopItem: ShopItem,
    weigthDistribution: Array<Float>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$rank",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(weigthDistribution[0])
                    .padding(horizontal = 6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(100),
                    )
                    .padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.labelMedium

            )
            Text(
                text = shopItem.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(weigthDistribution[1])
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = "${Sold(shopItem.sold)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(weigthDistribution[2])
                    .padding(horizontal = 4.dp)

            )
        }
    }
}

@Composable
private fun StatisticView(
    modifier: Modifier,
    title: String = "title",
    data: List<Int> = listOf(0, 0, 0),
    xLabels: List<String> = listOf("2002", "2003", "2004"),
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$title",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )
        if (data.isNotEmpty() && xLabels.isNotEmpty()) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                val maxheight = this.maxHeight
                val maxwidth = this.maxWidth
                BoxWithConstraints(
                    modifier = Modifier
                        .height(maxheight * 0.8f)
                        .width(maxwidth * 0.8f)
                        .fillMaxWidth()
                        .align(Alignment.TopEnd)
                ) {
                    // graph
                    val widthPercol = this.maxWidth * 0.12f
                    val heightbars = this.maxHeight
                    val maxdata = data.max()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        data.map {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Card(
                                    modifier = Modifier
                                        .width(widthPercol)
                                        .height(heightbars * (it / maxdata.toFloat())),
                                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)
                                ) {
                                    Text(
                                        text = "$it",
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .width(maxwidth * 0.2f)
                        .height(maxheight * 0.8f)
                        .fillMaxHeight()
                ) {
                    // y col
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawLine(
                                    Color.Black,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height + 5.dp.toPx()),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                    ) {
                        val maxdata = data.max()
                        var iterate = 1
                        val cuts = when (maxdata) {
                            0, 1, 2, 3, 4 -> 3
                            else -> 5
                        }
                        if (maxdata > cuts) {
                            iterate = maxdata / cuts
                        }
                        val numberofLines = (maxdata / iterate).toInt()
                        repeat(numberofLines) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = "${maxdata - (iterate * it)}",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(maxheight * 0.2f)
                        .width(maxWidth * 0.8f)
                        .align(Alignment.BottomEnd)
                ) {
                    // x col
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawLine(
                                    Color.Black,
                                    start = Offset(0f, 5.dp.toPx()),
                                    end = Offset(size.width, 5.dp.toPx()),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                    ) {
                        xLabels.map {
                            Text(
                                text = it,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 4.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

object FakeData {
    val salesfake = arrayListOf<ShopItem>(
        ShopItem(IID = "1"), ShopItem(), ShopItem(),
        ShopItem(), ShopItem(), ShopItem(),
        ShopItem(), ShopItem(), ShopItem(),
        ShopItem(), ShopItem(), ShopItem(),
        ShopItem(), ShopItem(), ShopItem(),
    )
}


@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun OverViewScreenPrev() {
    FishifyManagerTheme() {
        Column() {
            StatisticView(
                modifier = Modifier.aspectRatio(4f / 3f)
            )
        }
    }
}
