package com.ayakashikitsune.fishifymanager.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenDestination(
    val destination: String,
    val icon: ImageVector,
    val title: String,
){
    object LoginScreen: ScreenDestination("LoginScreen", Icons.Default.Login, "Login")
    object SignUpRegistrationScreen: ScreenDestination ("SignUpRegistrationScreen", Icons.Default.Login, "Sign up")

    object OverViewScreen: ScreenDestination("OverViewScreen",Icons.Default.Home, "Home")
    object ModifyShopsItemScreen: ScreenDestination("ModifyShopsItemScreen", Icons.Default.ListAlt, "Items")
    object AddItemScreen : ScreenDestination("AddItemScreen", Icons.Default.Add, "Add Item")
    object OnConfirmDeliveryScreen : ScreenDestination("OnConfirmDeliveryScreen", Icons.Default.LocalShipping, "On Delivery")
    object ProfileScreen: ScreenDestination("ProfileScreen",Icons.Default.Person, "Profile")
    object AboutScreen: ScreenDestination("AboutScreen",Icons.Default.Person, "About")
    object EditProfileScreen: ScreenDestination("EditProfileScreen",Icons.Default.Person, "Edit Profile")

    fun String.addToRoute(
        newRoute : String,
    ) : String{
        return  this.plus("/$newRoute")
    }
}
