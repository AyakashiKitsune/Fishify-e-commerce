package com.ayakashikitsune.fishifymanager.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymanager.data.models.ShopItem
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.domain.getFileName
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ayakashikitsune.fishifymanager.R as localResource

@Composable
fun AddItemScreen(
    fishifyManagerViewModel: FishifyManagerViewModel,
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    isAddItem: Boolean = true,
    index: Int?,
) {

    val items = fishifyManagerViewModel.shopItemInfoFlow.sortedBy { it.name }

    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf(Uri.EMPTY) }
    val imageName = remember { mutableStateOf("") }

    val imageLink = remember { mutableStateOf("") }
    LaunchedEffect(key1 = true) {
        if (!isAddItem) {
            val item = items[index!!]
            name.value = item.name
            description.value = item.description
            price.value = item.price.toString()
            imageUri.value = fishifyManagerViewModel.getImage(item.IID, item.OID)
            imageLink.value = item.imageLink
            imageName.value = item.imageFilename
        }
    }

    val coroutine = rememberCoroutineScope()
    val showAlertDialog = remember { mutableStateOf(false) }
    val errMessage = remember { mutableStateOf(AnnotatedString("")) }
    val context = LocalContext.current
    val imageResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            if (it != null) {
                imageUri.value = it
                imageName.value = getFileName(context, it)
            }
        })

    if (showAlertDialog.value) {
        AlertDialog(
            onDismissRequest = { showAlertDialog.value = false },
            title = {
                Text(
                    text = "Check your Fields",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null) },
            confirmButton = {
                FilledTonalButton(onClick = { showAlertDialog.value = false }) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Ok")
                }
            },
            text = {
                Text(text = errMessage.value)
            },

            )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.weight(9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Fill up the Following Fields",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
                AddItemFields(
                    label = "Name",
                    icon = Icons.Default.Person,
                    onChangeString = { name.value = it },
                    stringValue = { name.value },
                    onError = { it.isEmpty() },
                )
                AddItemFields(
                    label = "Description",
                    icon = Icons.Default.Description,
                    onChangeString = { description.value = it },
                    stringValue = { description.value },
                    onError = { it.isEmpty() },
                    maxlines = Int.MAX_VALUE
                )
                AddItemFields(
                    label = "Price",
                    icon = Icons.Default.Payments,
                    onChangeString = { price.value = it },
                    stringValue = { price.value },
                    onError = { it.isEmpty() },
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Pick an Image",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .placeholder(localResource.drawable.shrimp)
                                .data(
//                                    if (isAddItem == false && imageUri.value == Uri.EMPTY)
//                                        imageLink.value
//                                    else
                                    imageUri.value
                                ).build(),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .weight(1f)
                                .clip(MaterialTheme.shapes.medium),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Image Filename : ${imageName.value.ifEmpty { "None" }}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Button(
                                onClick = {
                                    imageResult.launch("image/*")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Pick an image",
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            OutlinedButton(
                onClick = { navHostController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(text = "Cancel")
            }
            Button(
                onClick = {
                    errMessage.value = buildAnnotatedString {
                        if (name.value.isEmpty()) {
                            append("- Name is empty\n")
                        }
                        if (description.value.isEmpty()) {
                            append("- Description is empty\n")
                        }
                        if (price.value.isEmpty()) {
                            append("- Price is empty\n")
                        }
                        if (price.value == "0") {
                            append("- Price cant be 0\n")
                        }
                        if (imageUri.value?.path?.isEmpty() == true && isAddItem) {
                            append("- Image is required\n")
                        }
                    }
                    if (errMessage.value.text.isEmpty()) {
                        val newItem = ShopItem(
                            name = name.value,
                            description = description.value,
                            price = price.value.toDouble(),
                            dateMade = Timestamp.now(),
                            imageUri = "",
                            imageLink = "",
                            imageFilename = imageName.value
                        )
                        if (isAddItem) {
                            coroutine.launch(Dispatchers.IO) {
                                fishifyManagerViewModel.addShopItem(
                                    shopItem = newItem,
                                    imageUri = imageUri.value,
                                    showSnackBar = {
                                        coroutine.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                                withContext(Dispatchers.Main) {
                                    navHostController.popBackStack()
                                }
                            }
                        } else {
                            /*edit mode*/
                            coroutine.launch(Dispatchers.IO) {
                                fishifyManagerViewModel.updateShopItem(
                                    shopItem = newItem.copy(
                                        imageLink = items[index!!].imageLink,
                                        imageUri = items[index].imageUri,
                                        IID = items[index].IID,
                                        OID = items[index].OID
                                    ),
                                    imageUri = if (isAddItem) imageUri.value else null, // if true means igot image from firebase else igot it from storage
                                    showSnackBar = {
                                        coroutine.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                                withContext(Dispatchers.Main) {
                                    navHostController.popBackStack()
                                }
                            }
                        }

                    } else {
                        showAlertDialog.value = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.tertiaryContainer)
                )
            ) {
                Text(text = "Confirm Item")
            }
        }


    }
}

@Composable
fun AddItemFields(
    label: String,
    icon: ImageVector,
    onChangeString: (String) -> Unit,
    stringValue: () -> String,
    onError: (String) -> Boolean,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxlines: Int = 1
) {
    OutlinedTextField(
        value = stringValue(),
        onValueChange = { onChangeString(it) },
        label = { Text(text = label) },
        maxLines = maxlines,
        isError = onError(stringValue()),
        placeholder = { Text(text = stringValue()) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        trailingIcon = {
            if (stringValue().isNotBlank()) {
                IconButton(
                    onClick = { onChangeString("") },
                    content = { Icon(imageVector = Icons.Default.Close, contentDescription = null) }
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = keyboardType),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Preview(wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, showBackground = true, showSystemUi = true)
@Composable
fun AddItemScreenPrev() {
    FishifyManagerTheme {
        AddItemScreen(
            navHostController = rememberNavController(),
            fishifyManagerViewModel = viewModel(),
            snackbarHostState = SnackbarHostState(),
            paddingValues = PaddingValues(),
            index = 1
        )
    }
}