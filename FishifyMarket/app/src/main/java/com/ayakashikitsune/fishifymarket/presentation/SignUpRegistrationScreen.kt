package com.ayakashikitsune.fishifymanager.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ayakashikitsune.fishifymarket.datamodel.UserClient
import com.ayakashikitsune.fishifymarket.domain.FishifyViewModel
import com.ayakashikitsune.fishifymarket.domain.NavigationItemList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymarket.R as localresource

@Composable
fun SignUpRegistrationScreen(
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    vm: FishifyViewModel
) {
    var isLoadingState by remember { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()

    val nameValue = remember { mutableStateOf("lance") }
    val emailValue = remember { mutableStateOf("lancerolan818@gmail.com") }
    val addressValue = remember { mutableStateOf("where") }
    val contactNumberValue = remember { mutableStateOf("09192345678") }
    val passwordValue = remember { mutableStateOf("zxcvbnm") }

    val params = mutableListOf<Triple<String, MutableState<String>, ImageVector>>(
        Triple("Name", nameValue, Icons.Default.Person),
        Triple("Email Address", emailValue, Icons.Default.Email),
        Triple("Home Address", addressValue, Icons.Default.Storefront),
        Triple("Contact Number", contactNumberValue, Icons.Default.Call),
        Triple("Password", passwordValue, Icons.Default.Password),
    )
    val showPass = remember { mutableStateOf(false) }
    BackHandler() {
        navHostController.popBackStack()
    }


    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            AsyncImage(
                model = localresource.drawable.fishifylogo,
                contentDescription = "App logo",
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .size(80.dp),
            )
            FancyLogoText()
            params.forEach { param ->
                SignUpTextField(
                    labels = { param.first },
                    stringValue = { param.second.value },
                    onchange = { param.second.value = it },
                    icon = param.third,
                    imeAction = if (params.last() == param) ImeAction.Done else ImeAction.Next,
                    visualTransformation =
                    if (params.last() == param) {
                        if (showPass.value) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        }
                    } else {
                        VisualTransformation.None
                    },
                    keyboardType = when (params.indexOf(param)) {
                        1 -> KeyboardType.Email
                        3 -> KeyboardType.Number
                        else -> KeyboardType.Text
                    },
                    trailingIcon = {
                        if (params.last() == param)
                            IconButton(
                                onClick = { showPass.value = !showPass.value }
                            ) {
                                Icon(
                                    imageVector = if (showPass.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                    }
                )
            }
            Button(
                onClick = {
                    coroutine.launch(Dispatchers.IO) {
                        vm.signup(
                            email = emailValue.value,
                            password = passwordValue.value,
                            userClient = UserClient(
                                name = nameValue.value,
                                address = addressValue.value,
                                contactNumber = contactNumberValue.value,
                                email = emailValue.value,
                            ),
                            onSuccess = {
                                navHostController.navigate(NavigationItemList.HomeNav.title) {
                                    popUpTo(NavigationItemList.HomeNav.title) {
                                        inclusive = true
                                    }
                                }
                            },
                            isLoadingModeState = { isLoadingState = !isLoadingState },
                            onFail = { errMessage ->
                                isLoadingState = false
                                coroutine.launch {
                                    snackbarHostState.showSnackbar(
                                        message = errMessage,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(top = 16.dp)
            ) {
                Text("Sign Up")
            }
            OutlinedButton(
                onClick = {
                    navHostController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Cancel Sign Up")
            }
            Spacer(modifier = Modifier.weight(1f))

        }
    }
    AnimatedVisibility(
        visible = isLoadingState,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SignUpTextField(
    labels: () -> String,
    stringValue: () -> String,
    onchange: (String) -> Unit,
    icon: ImageVector,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable () -> Unit
) {
    OutlinedTextField(
        value = stringValue(),
        onValueChange = { onchange(it) },
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        maxLines = 1,
        placeholder = { Text(text = labels()) },
        label = { Text(text = labels()) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = stringValue()) },
        keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun SignUpRegistrationScreenPrev() {
    SignUpRegistrationScreen(
        navHostController = rememberNavController(),
        snackbarHostState = SnackbarHostState(),
        vm = viewModel()
    )
}