package com.ayakashi.kitsune.fishify.primitiveClasses


data class Owner(
    val name: String = FakeOwner.names.random(),
    val storeName: String = "random name of store",
    val address: String = "${FakeOwner.barangays.random()} ${FakeOwner.streets.random()} ${FakeOwner.cities.random()}",
    val contactNumber: String = FakeOwner.generateRandomContactNumber(),
    val email: String = name.plus("@Email.com"),

    val UID: String = (Math.random() * 100000).toString(),
    val profilePicture: String = "default",
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "name" to name,
            "storeName" to storeName,
            "UID" to UID,
            "email" to email,
            "address" to address,
            "contactNumber" to contactNumber,
            "profilePicture" to profilePicture,
        )
    }
}

val admin = Owner(
    name = "Admin",
    UID = "0129301923",
    email = "rolan@email.com",
    address = "",
    contactNumber = "09192391969",
)

object FakeOwner {
    val names = listOf(
        "Juan Dela Cruz",
        "Maria Santos",
        "Pedro Reyes",
        "Luisa Garcia",
        "Ramon Fernandez",
        "Carmen Del Rosario",
        "Jose Rizal",
        "Rosalinda Lim",
        "Emilio Aguinaldo",
        "Gemma Cruz",
        "Gabriel Mendoza",
        "Isabel Lopez",
        "Fernando Cruz",
        "Antonia Santiago",
        "Santiago Gutierrez",
        "Aurora Torres",
        "Rafael Gomez",
        "Adriana Rivera",
        "Manuel Villanueva",
        "Carolina Hernandez"
    )
    val barangays = listOf(
        "Barangay 1",
        "Barangay 2",
        "Barangay 3",
        "Barangay 4",
        "Barangay 5",
        "Barangay 6",
        "Barangay 7",
        "Barangay 8",
        "Barangay 9",
        "Barangay 10"
    )

    val streets = listOf(
        "Mabini Street",
        "Rizal Avenue",
        "Luna Street",
        "Bonifacio Avenue",
        "Quezon Street",
        "Magsaysay Boulevard",
        "Roxas Street",
        "Garcia Street",
        "Aguinaldo Avenue",
        "Laurel Street"
    )

    val cities = listOf(
        "Manila",
        "Quezon City",
        "Makati",
        "Cebu City",
        "Davao City",
        "Pasig",
        "Taguig",
        "Caloocan",
        "Parañaque",
        "Mandaluyong"
    )

    fun generateRandomContactNumber(): String {
        val prefixList =
            listOf("09", "0915", "0920", "0935", "0940", "0955", "0960", "0975", "0990")
        val randomPrefix = prefixList.random()

        val numberDigits = (0..7).map { (0..9).random() }.joinToString("")

        return randomPrefix + numberDigits
    }

}