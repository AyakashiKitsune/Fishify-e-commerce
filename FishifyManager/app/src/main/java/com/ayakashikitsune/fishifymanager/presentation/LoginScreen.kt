package com.ayakashikitsune.fishifymanager.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.domain.ScreenDestination
import com.ayakashikitsune.fishifymanager.ui.theme.FishifyManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymanager.R as localResource


@Composable
fun LoginScreen(
    navControl: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: FishifyManagerViewModel
) {
    val coroutine = rememberCoroutineScope() //reserve for login
    val focusManager = LocalFocusManager.current

    var isLoadingState by remember { mutableStateOf(false) }

    val emailValue = remember { mutableStateOf("rolan.l.bscs@gmail.com") }
    val passwordValue = remember { mutableStateOf("qwertyuiop") }

    BackHandler() {
        focusManager.clearFocus()
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            FancyLogoText()
            AsyncImage(
                model = localResource.drawable.fishifylogo,
                contentDescription = "App logo",
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 12.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .aspectRatio(1f)

            )
            EmailPasswordField(
                email = { emailValue.value },
                password = { passwordValue.value },
                emailOnchange = { email -> emailValue.value = email },
                passwordOnchange = { password -> passwordValue.value = password },
                focusManager = focusManager
            )
            Button(
                onClick = {
                        coroutine.launch(Dispatchers.IO) {
                            viewModel.login(
                                email = emailValue.value,
                                password = passwordValue.value,
                                onSuccess = {
                                    navControl.navigate(ScreenDestination.OverViewScreen.destination) {
                                        popUpTo(ScreenDestination.OverViewScreen.destination) {
                                            inclusive = true
                                        }
                                    }
                                },
                                isLoadingModeState = { isLoadingState = !isLoadingState },
                                onFail = { errMessage ->
                                    coroutine.launch {
                                        snackbarHostState.showSnackbar(
                                            message = errMessage ?: "none",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
            ) {
                Text(text = "Login")
            }

            Button(
                onClick = {
                    navControl.navigate(ScreenDestination.SignUpRegistrationScreen.destination)
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer))
            ) {
                Text(text = "Sign Up here")
            }


        }


        /*Loading Operation*/
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
}

@Composable
fun FancyLogoText() {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("Fishify Manager\n")
            }
            withStyle(style = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Light)) {
                append("From ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Sea")
                }
                withStyle(style = SpanStyle()) {
                    append(", to ")
                }
                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily.Cursive
                    )
                ) {
                    append("Doorstep")
                }
            }

        },
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun EmailPasswordField(
    email: () -> String,
    password: () -> String,
    emailOnchange: (String) -> Unit,
    passwordOnchange: (String) -> Unit,
    focusManager: FocusManager
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        /*email field*/
        OutlinedTextField(
            value = email(),
            onValueChange = { emailOnchange(it) },
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            maxLines = 1,
            placeholder = { Text(text = "Email / Username") },
            label = { Text(text = "Email / Username") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "username or email"
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        /*password field*/
        val visibilePassword = remember { mutableStateOf(true) }
        val showError = remember { mutableStateOf(false) }
        OutlinedTextField(
            value = password(),
            onValueChange = { password ->
                if (password.length > 12)
                    showError.value = true
                else
                    passwordOnchange(password)
            },
            isError = showError.value,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            maxLines = 1,
            placeholder = { Text(text = "Password") },
            label = { Text(text = "Password") },
            leadingIcon = { Icon(imageVector = Icons.Default.Password, contentDescription = null) },
            trailingIcon = {
                val image = if (visibilePassword.value)
                    Icons.Default.Visibility to "show password"
                else
                    Icons.Default.VisibilityOff to "hide password"

                IconButton(onClick = { visibilePassword.value = !visibilePassword.value }) {
                    Icon(imageVector = image.first, contentDescription = image.second)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            visualTransformation = if (visibilePassword.value) PasswordVisualTransformation() else VisualTransformation.None,
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginScreenPrev() {
    FishifyManagerTheme {
        LoginScreen(
            navControl = rememberNavController(),
            viewModel = viewModel(),
            snackbarHostState = SnackbarHostState()
        )
    }
}