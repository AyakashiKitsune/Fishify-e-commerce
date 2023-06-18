package com.ayakashikitsune.fishifymarket.presentation

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymarket.datamodel.FakeData
import com.ayakashikitsune.fishifymarket.datamodel.ShopItem
import com.ayakashikitsune.fishifymarket.domain.ViewItemAs
import com.ayakashikitsune.fishifymarket.ui.theme.FishifyMarketTheme
import com.ayakashikitsune.fishifymarket.R as localresource

@Composable
fun ShopItemUI(shopItem: ShopItem, viewItemAs: ViewItemAs, context: Context,onclick: (String) -> Unit) {
    when (viewItemAs) {
        ViewItemAs.ListView -> ListviewItem(shopItem = shopItem, context,{onclick(it)})
        ViewItemAs.GridView -> GridviewItem(shopItem = shopItem, context,{onclick(it)})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GridviewItem(
    shopItem: ShopItem,
    context: Context,
    onclick: (String) -> Unit
) {
    val displayWidth = LocalConfiguration.current
    val width = displayWidth.screenWidthDp * 0.45f
    Surface(
        color = MaterialTheme.colorScheme.surface,
//        border = BorderStroke(1.dp,MaterialTheme.colorScheme.tertiary),
        shape = MaterialTheme.shapes.medium,
        onClick = {onclick(shopItem.IID)},
        modifier = Modifier
            .width(width.dp)
            .padding(4.dp),
        shadowElevation = 4.dp,
        tonalElevation = 13.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(shopItem.imageLink)
                    .placeholder(localresource.drawable.placeholderloadingimage)
                    .error(localresource.drawable.brokenimage)
                    .build(),
                contentDescription = shopItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(width.dp)
                    .aspectRatio(1f)
                    .padding(horizontal = 3.dp, vertical = 3.dp)
                    .clip(MaterialTheme.shapes.small)
            )


            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = shopItem.name[0].uppercase() + shopItem.name.substring(1),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Chiplets(
                                title = "\u20B1${shopItem.price}",
                                fontweight = FontWeight.ExtraBold,
                                icon = Icons.Filled.Payments,
                                tint = Color(76, 175, 80, 255)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Chiplets(
                                title = "${shopItem.rating}",
                                icon = Icons.Rounded.Star,
                                tint = Color(255, 238, 88, 255)

                            )
                            Chiplets(
                                title = shopItem.sold.toString(),
                                icon = Icons.Rounded.ShoppingBasket,
                                tint = Color(66, 165, 245, 255)

                            )
                        }
                    }
                }
            }


        }
    }
}


@Composable
private fun ListviewItem(shopItem: ShopItem, context: Context,onclick: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp,MaterialTheme.colorScheme.tertiary),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.padding(4.dp),
        onClick = {onclick(shopItem.IID)},
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(shopItem.imageLink)
                    .placeholder(localresource.drawable.placeholderloadingimage)
                    .error(localresource.drawable.brokenimage)
                    .build(),
                contentDescription = shopItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .aspectRatio(1f)
                    .padding(3.dp)
                    .clip(MaterialTheme.shapes.small)

            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(3f)
                    .padding(4.dp),
            ) {
                Text(
                    text = shopItem.name[0].uppercase() + shopItem.name.substring(1),
                    style = MaterialTheme.typography.titleLarge,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Chiplets(
                        title = "\u20B1${shopItem.price}",
                        fontweight = FontWeight.ExtraBold,
                        icon = Icons.Filled.Payments,
                        tint = Color(76, 175, 80, 255)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Chiplets(
                        title = "${shopItem.rating}",
                        icon = Icons.Rounded.Star,
                        tint = Color(255, 238, 88, 255)
                    )
                    Chiplets(
                        title = shopItem.sold.toString(),
                        icon = Icons.Rounded.ShoppingBasket,
                        tint = Color(66, 165, 245, 255)

                    )
                }
            }
            IconButton(
                onClick = {}, modifier = Modifier
                    .padding(end = 16.dp)
                    .weight(1f)
            ) {
                Icon(imageVector = Icons.Outlined.AddShoppingCart, contentDescription = null)
            }

        }
    }
}

@Composable
fun Chiplets(
    title: String,
    fontweight: FontWeight = FontWeight.Medium,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.primaryContainer

) {
    Box(
        modifier = Modifier.clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.tertiaryContainer,color)))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = icon.name,
                tint = tint,
            )
            Text(
                text = title,
                fontSize = style.fontSize,
                fontWeight = fontweight,
                style = style
            )
        }
    }
}


@Preview
@Composable
fun Shopitempreviewd() {
//    val viewmodel = viewModel<FishifyViewModel>()
    val list = FakeData.salesfake
    val context = LocalContext.current
    FishifyMarketTheme(darkTheme = false) {
        Column {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                list.map {
                    ShopItemUI(shopItem = it, viewItemAs = ViewItemAs.GridView, context,{})
                }
            }
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                list.map {
                    ShopItemUI(shopItem = it, viewItemAs = ViewItemAs.ListView, context,{})
                }
            }
        }
    }
}