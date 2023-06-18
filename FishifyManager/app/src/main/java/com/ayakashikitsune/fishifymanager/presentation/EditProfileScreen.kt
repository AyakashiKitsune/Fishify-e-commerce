package com.ayakashikitsune.fishifymanager.presentation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.domain.getFileName
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymanager.R as localresource

@Composable
fun EditProfileScreen(
    viewModel: FishifyManagerViewModel,
    navHostController: NavHostController
) {
    BackHandler() {
        navHostController.popBackStack()
    }
    val owner = viewModel.ownerInfoFlow.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nameVar by remember { mutableStateOf(owner.value.name) }
    var addressVar by remember { mutableStateOf(owner.value.address) }
    var contactnumberVar by remember { mutableStateOf(owner.value.contactNumber) }
    var imageVar by remember { mutableStateOf(owner.value.profilePicture) } //link string
    var passwordVar by remember { mutableStateOf("") }
    var password2Var by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf(Uri.EMPTY) }
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        if (it != null) {
            uri = it
            imageVar = getFileName(context,it)
        }
    }
    val textfieldMod = Modifier
        .padding(8.dp)
        .fillMaxWidth()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        //edit name
        // image
        val imageState by remember {
            derivedStateOf {
                if (imageVar == "default") {
                    ImageRequest.Builder(context)
                        .data(localresource.drawable.stick)
                        .error(localresource.drawable.brokenimage)
                        .placeholder(localresource.drawable.placeholderloadingimage)
                        .build()
                } else {
                    ImageRequest.Builder(context)
                        .data(uri)
                        .data(owner.value.profilePicture)
                        .error(localresource.drawable.brokenimage)
                        .placeholder(localresource.drawable.placeholderloadingimage)
                        .build()
                }
            }
        }
        Row(
            modifier = Modifier
                .aspectRatio(3 / 2f)
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(100))
            ) {
                AsyncImage(
                    model = imageState,
                    contentDescription = null,
                    modifier = Modifier
                        .size(maxHeight - 16.dp, maxHeight - 16.dp)
                        .background(Color.White, RoundedCornerShape(100))
                        .clip(RoundedCornerShape(100))
                        .clickable {
                            resultLauncher.launch("image/*")
                        },
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .height(this.maxHeight / 3)
                        .width(maxHeight - 16.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                    )
                }
            }
        }
        OutlinedTextField(
            value = nameVar,
            label = { Text(text = "Name") },
            placeholder = { Text(text = "Name") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            onValueChange = { nameVar = it },
            modifier = textfieldMod,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences
            )
        )
        // address
        OutlinedTextField(
            value = addressVar,
            label = { Text(text = "Address") },
            leadingIcon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
            placeholder = { Text(text = "Address") },
            onValueChange = { addressVar = it },
            modifier = textfieldMod,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            )
        )
        // contact number
        OutlinedTextField(
            value = contactnumberVar,
            label = { Text(text = "Contact Number") },
            placeholder = { Text(text = "Contact Number") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = null
                )
            },
            onValueChange = { contactnumberVar = it },
            modifier = textfieldMod,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
        )
        // password when expanded
        var isPassshow by remember { mutableStateOf(true) }
        val visualTransformation by remember { derivedStateOf { if (isPassshow) PasswordVisualTransformation() else VisualTransformation.None } }
        var isExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Change password Area")
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = null
                )
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.animateContentSize()
            ) {

                OutlinedTextField(
                    value = passwordVar,
                    label = { Text(text = "Password") },
                    placeholder = { Text(text = "Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPassshow = !isPassshow }) {
                            Icon(
                                imageVector = if (isPassshow) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    onValueChange = { passwordVar = it },
                    modifier = textfieldMod,
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
                OutlinedTextField(
                    value = password2Var,
                    label = { Text(text = "Confirm Password") },
                    placeholder = { Text(text = "Confirm Password") },
                    trailingIcon = {
                        IconButton(onClick = { isPassshow = !isPassshow }) {
                            Icon(
                                imageVector = if (isPassshow) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null
                        )
                    },
                    onValueChange = { password2Var = it },
                    modifier = textfieldMod,
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )

            }
        }
        Button(
            onClick = {
            scope.launch {
                viewModel.updateUserinfoService(
                    owner  = owner.value.copy(
                        name = nameVar,
                        address = addressVar,
                        contactNumber = contactnumberVar,
                        profilePicture = imageVar,//name
                    ),
                    uri = if (uri != Uri.EMPTY) uri else null,
                    showSnackBar = {}
                )
                if (passwordVar.isNotEmpty()) {
                    if (passwordVar == password2Var) {
                        viewModel.changePassword(passwordVar) {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                        navHostController.popBackStack()
                    } else {
                        Toast.makeText(context,"password not match",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    navHostController.popBackStack()
                }
            }
        }) {
            Text(text = "Confirm Changes")
        }
    }
}

