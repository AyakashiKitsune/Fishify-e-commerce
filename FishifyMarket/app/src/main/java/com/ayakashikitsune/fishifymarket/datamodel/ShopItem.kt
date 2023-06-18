package com.ayakashikitsune.fishifymarket.datamodel

import com.google.firebase.Timestamp

data class ShopItem(
    val name: String = fishNames[(Math.random() * fishNames.size).toInt()],
    val OID: String = "rakdandjan2039i10if",
    val storeName: String = "random name of store",
    val description: String = fishDescriptions[(Math.random() * fishNames.size).toInt()],
    val imageLink: String = "",
    val imageFilename: String = "",
    val imageUri: String = "",
    val price: Double = (Math.random() * 1000).toInt().toDouble(),
    var IID: String = (Math.random() * Int.MAX_VALUE).toInt().toString(),
    val rating: Double = 0.0,
    val sold: Int = 0,
    val dateMade: Timestamp = Timestamp.now(),
    val visibleToCustomers: Boolean = true,
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "IID" to IID,
            "OID" to OID,
            "storeName" to storeName,
            "name" to name,
            "description" to description,
            "imageLink" to imageLink,
            "imageUri" to imageUri,
            "imageFilename" to imageFilename,
            "rating" to rating,
            "price" to price,
            "sold" to sold,
            "dateMade" to dateMade,
            "visibleToCustomers" to visibleToCustomers
        )
    }

    fun toPerOrderItem(): PerOrderItem {
        return PerOrderItem(
            name = this.name,
            price = this.price,
            imageLink = this.imageLink,
            quantity = 1,
            ownerID = this.OID,
            storeName = this.storeName,
            IID = this.IID
        )
    }
}

val fishNames = listOf(
    "Guppy",
    "Angelfish",
    "Betta",
    "Goldfish",
    "Tetra",
    "Gourami",
    "Discus",
    "Molly",
    "Swordtail",
    "Platy",
    "Corydoras",
    "Killifish",
    "Rainbowfish",
    "Barb",
    "Rasbora",
    "Loach",
    "Pleco",
    "Danio",
    "Piranha",
    "Archerfish"
)
val fishDescriptions = listOf(
    "A small, brightly colored fish with long, flowing fins.",
    "A large, silver fish with a streamlined body and sharp teeth.",
    "A bottom-dwelling fish with a flat, round body and camouflaged markings.",
    "A sleek, torpedo-shaped fish with iridescent scales.",
    "A medium-sized fish with vibrant stripes and a prominent dorsal fin.",
    "A small, transparent fish with bioluminescent features.",
    "A long, eel-like fish with a narrow body and sharp spines.",
    "A fish with a humped back and a large mouth filled with sharp teeth.",
    "A fish with a bulbous head and eyes that bulge out.",
    "A fish with a fan-shaped tail and elaborate, colorful patterns.",
    "A small, freshwater fish known for its jumping ability.",
    "A deep-sea fish with a large head and enormous, sharp teeth.",
    "A fish with long, thin tendrils extending from its head.",
    "A flat, diamond-shaped fish with a long, whip-like tail.",
    "A fish with a rounded body and a mouth that resembles a beak.",
    "A fish with long, needle-like teeth and a voracious appetite.",
    "A fish with a broad, flattened body and wide, pectoral fins.",
    "A small, scaleless fish with a sucker-like mouth.",
    "A fish with a cylindrical body and a long, flowing tail fin.",
    "A fish with a distinctive hump on its back and a forked tail fin."
)

object FakeData {
    val salesfake = arrayListOf<ShopItem>(
        ShopItem(IID = "1"), ShopItem(OID = "ASdasda"), ShopItem(OID = "ASdasda"),
        ShopItem(), ShopItem(), ShopItem(OID = "ASdasda"),
        ShopItem(), ShopItem(), ShopItem(OID = "ASdas"),
        ShopItem(), ShopItem(OID = "ASdas"), ShopItem(),
    )
}


