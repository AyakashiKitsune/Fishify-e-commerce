package com.ayakashikitsune.fishifymarket.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

const val KEY_ID = "id"
sealed class NavigationItemList(val title: String, val icon: ImageVector) {
    object HomeNav              : NavigationItemList(title = "Home",            icon = Icons.Filled.Home)
    object SearchNav            : NavigationItemList(title = "Search",          icon = Icons.Filled.Search)
    object CartNav              : NavigationItemList(title = "Cart",            icon = Icons.Filled.ShoppingCart)
    object OrdersNav            : NavigationItemList(title = "Orders",          icon = Icons.Filled.Notifications)
    object ProfileNav           : NavigationItemList(title = "Profile",         icon = Icons.Filled.Person)
    object EditprofileNav       : NavigationItemList(title = "Edit Profile",    icon = Icons.Filled.Person)
    object DetailsNav           : NavigationItemList(title = "Details",         icon = Icons.Filled.Details)
    object AboutNav             : NavigationItemList(title = "About",           icon = Icons.Filled.Info)
    object LoginNav             : NavigationItemList(title = "Login",           icon = Icons.Filled.Login)
    object SignUpNav            : NavigationItemList(title = "Sign Up",         icon = Icons.Filled.HowToReg)

    fun String.addToRoute(
        newRoute : String,
    ) : String{
        return  this.plus("/$newRoute")
    }

}
