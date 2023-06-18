package com.ayakashikitsune.fishifymarket.datamodel

import com.google.firebase.Timestamp

enum class OrderStatus(val state: String) {
    REQUESTING("REQUESTING"),   // on sellers orders screen
    CONFIRMED("CONFIRMED"), //  on delivery screen
    DELIVERY("DELIVERY"),   // mark as on delivery
    DELIVERED("DELIVERED"), // marked as done, delivered at home
    CANCELLED("CANCELLED")  // mark as not fulfilled
}

data class OrderItem(
    val UID: String = "",
    val OrderID: String = "",
    val orders: ArrayList<HashMap<String, Any>> = arrayListOf(),
    val datetimePlaced: Timestamp = Timestamp.now(),
    val statusOfOrder: String = OrderStatus.REQUESTING.state, // REQUESTING, CONFIRMED, DELIVERY,DELIVERED, CANCELLED
    val reasonCancelled: String = "",
    val doneDelivery: Timestamp? = null
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "UID" to UID,
            "OrderID" to OrderID,
            "orders" to orders,
            "datetimePlaced" to datetimePlaced,
            "statusOfOrder" to statusOfOrder,
            "reasonCancelled" to reasonCancelled,
            "doneDelivery" to doneDelivery
        )
    }
}

fun HashMap<String, Any>.toPerOrderItem(): PerOrderItem {
    return PerOrderItem(
        name = this["name"] as String,
        price = this["price"].toString().toDouble(),
        imageLink = this["imageLink"] as String,
        quantity = this["quantity"].toString().toInt(),
        ownerID = this["ownerID"] as String,
        storeName = this["storeName"] as String,
        IID = this["IID"] as String
    )
}

data class PerOrderItem(
    val name: String,
    val price: Double,
    val imageLink: String,
    val quantity: Int,
    val ownerID: String,
    val storeName: String,
    val IID: String,
) {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "name" to name,
            "price" to price,
            "imageLink" to imageLink,
            "quantity" to quantity,
            "ownerID" to ownerID,
            "storeName" to storeName,
            "IID" to IID
        )
    }
}

/*
* items : {
*       [
*           {
*           name        : shopitem name,
*           price       : shopitem price,
*           discount    : shopitem discount,
*           imageLink   : shopitem imagelink,
*           quantity    : shopitem quantity,
*           ownerID     : shopitem OID,
*
*           }
*       ]
* }*/
