package com.ayakashikitsune.fishifymanager.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymanager.data.models.ShopItem
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.domain.ScreenDestination
import com.ayakashikitsune.fishifymanager.domain.ScreenDestination.AddItemScreen.addToRoute
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ayakashikitsune.fishifymanager.R as localResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyShopsItemScreen(
    paddingValues: PaddingValues,
    viewModel: FishifyManagerViewModel,
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val localCoroutine = rememberCoroutineScope()

    val listOfShopItem = viewModel.shopItemInfoFlow
    val sorted = listOfShopItem.sortedBy { it.name }

    var showAlertDialog by remember { mutableStateOf(false) }
    var index by remember { mutableStateOf(0) }


    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {
            viewModel.readAllShopItem(showSnackBar = {
                localCoroutine.launch {
                    snackbarHostState.showSnackbar(
                        message = it,
                        duration = SnackbarDuration.Long
                    )
                }
            })
        }
    }

    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = {
                Text(
                    text = "Are you sure?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null) },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        showAlertDialog = false
                        localCoroutine.launch(Dispatchers.IO) {
                            viewModel.deleteShopItem(
                                shopItem = sorted[index],
                                showSnackBar = {
                                    localCoroutine.launch {
                                        snackbarHostState.showSnackbar(
                                            message = it,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Ok")
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this item ${sorted[index].name}"
                )
            },

            )
    }

    if (listOfShopItem.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Empty Items\nClick the \"+\" button to add new Item",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            state = scrollState
        ) {
            items(
                count = sorted.size,
            ) {
                ShopItemTiles(
                    context = context,
                    shopItem = sorted[it],
                    onEdit = {
                        navHostController.navigate(
                            ScreenDestination.AddItemScreen.destination
                                .addToRoute("false") // isAdditem
                                .addToRoute("$it") // index
                        )
                    },
                    onDelete = {
                        index = it
                        showAlertDialog = true
                    },
                    onEnable = { result ->
                        localCoroutine.launch(Dispatchers.IO) {
                            viewModel.updateShopItem(
                                shopItem = sorted[it].copy(visibleToCustomers = result),
                                imageUri = null,
                                showSnackBar = {
                                    localCoroutine.launch {
                                        snackbarHostState.showSnackbar(
                                            message = it,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            )
                        }
                    }
                )
                if (sorted.last() == sorted[it]) {
                    Spacer(
                        modifier = Modifier
                            .padding(50.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ShopItemTiles(
    context: Context,
    shopItem: ShopItem,
    onEdit: () -> Unit,
    onEnable: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    var isVisisbleToCustomers by remember { mutableStateOf(shopItem.visibleToCustomers) }
    Card(
        modifier = Modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .placeholder(localResource.drawable.placeholderloadingimage)
                    .error(localResource.drawable.brokenimage)
                    .data(shopItem.imageLink)
                    .crossfade(true)
                    .build(),
                contentDescription = shopItem.name,
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(3f),
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(6f)
                        .padding(horizontal = 12.dp)
                        .wrapContentHeight()
                ) {
                    Text(
                        text = shopItem.name,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    AnimatedVisibility(visible = !isExpanded) {
                        Text(
                            text = shopItem.description,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    /*enable or disable the item*/
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = isExpanded) {
            Column() {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Description:",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = shopItem.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Price: ${shopItem.price.toInt()} PHP",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    ElevatedButton(onClick = { onEdit() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit ${shopItem.name}"
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = "Edit")

                    }


                    Switch(
                        checked = isVisisbleToCustomers,
                        onCheckedChange = {
                            isVisisbleToCustomers = !isVisisbleToCustomers
                            onEnable(isVisisbleToCustomers)
                        },
                        thumbContent = {
                            Icon(
                                imageVector = if (isVisisbleToCustomers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                            )
                        },
                    )

                    ElevatedButton(onClick = { onDelete() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "edit ${shopItem.name}"
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = "Delete")
                    }
                }

            }
        }

    }
}

@Composable
fun FABaddItem(
    onClickFAB: () -> Unit,
) {
    ExtendedFloatingActionButton(
        onClick = onClickFAB,

        ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "add item")
        Text(
            text = "Add Item", style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, showSystemUi = true, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Composable
fun previewl() {
    FishifyManagerTheme {
        Column() {
        }
    }
}