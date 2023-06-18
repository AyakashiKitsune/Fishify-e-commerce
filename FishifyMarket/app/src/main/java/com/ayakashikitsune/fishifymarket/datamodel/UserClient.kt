package com.ayakashikitsune.fishifymarket.datamodel

data class UserClient(
    val UID: String = "1234567890",

    val name: String = "user-name",
    val contactNumber: String = "",
    val email: String = "yourname@email.com",
    val profilePicture: String = "default",
    val profileLink: String = "",
    val address: String = "location client",
    val bannedAccount: Boolean = false,
    val ordersList: ArrayList<HashMap<String, Any>> = arrayListOf(),
    val notificationsList: HashMap<String, HashMap<String, String>> = hashMapOf()
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "name" to name,
            "UID" to UID,
            "email" to email,
            "profilePicture" to profilePicture,
            "addresss" to address,
            "ordersList" to ordersList,
            "contactNumber" to contactNumber,
            "profileLink" to profileLink
        )
    }
}
