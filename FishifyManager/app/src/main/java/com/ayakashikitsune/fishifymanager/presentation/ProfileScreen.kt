package com.ayakashikitsune.fishifymanager.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ayakashikitsune.fishifymanager.data.models.Owner
import com.ayakashikitsune.fishifymanager.domain.FishifyManagerViewModel
import com.ayakashikitsune.fishifymanager.domain.ScreenDestination
import kotlinx.coroutines.launch
import com.ayakashikitsune.fishifymanager.R as localresource

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    navHostController: NavHostController,
    snackbarHostState : SnackbarHostState,
    viewModel: FishifyManagerViewModel

) {
    val coroutine = rememberCoroutineScope()
    val owner = viewModel.ownerInfoFlow.collectAsState()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "My profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Left

        )
        ProfileInfo(owner = owner.value)
        Spacer(modifier = Modifier.padding(12.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Left
        )
        SettingsInfo(
            navHostController = navHostController,
            logOut = {
                coroutine.launch {
                    viewModel.signout(
                        {},
                        {
                            coroutine.launch {
                                snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                            }
                        }
                    )
                    navHostController.navigate(ScreenDestination.LoginScreen.destination){
                        popUpTo(ScreenDestination.LoginScreen.destination){
                            inclusive = true
                        }
                    }
                }
            })
    }
}


@Composable
fun ProfileInfo(
    owner: Owner
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(if (owner.profilePicture == "default") localresource.drawable.stick else owner.profilePicture)
                .placeholder(localresource.drawable.placeholderloadingimage)
                .error(localresource.drawable.brokenimage)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(100))
                .background(Color.White)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = owner.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = owner.address,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = owner.email.toString(),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsInfo(
    navHostController: NavHostController,
    logOut: () -> Unit
) {
    val settingslist = listOf<Triple<String, ImageVector, () -> Unit>>(
        Triple("Profile settings", Icons.Rounded.Person, { navHostController.navigate(ScreenDestination.EditProfileScreen.destination) }),
        Triple("About App", Icons.Rounded.Info,{ navHostController.navigate(ScreenDestination.AboutScreen.destination) }),
        Triple("Sign out", Icons.Rounded.Logout) {logOut()}
    )
    Card(
        modifier = Modifier.padding(2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            settingslist.map {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.padding(2.dp),
                    onClick = it.third
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = it.second,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .weight(1f),
                        )
                        Text(
                            text = it.first,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                .weight(4f)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}