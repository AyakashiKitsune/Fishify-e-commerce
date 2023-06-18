package com.ayakashikitsune.fishifymarket.datamodel.service

import android.net.Uri
import android.util.Log
import com.ayakashikitsune.fishifymarket.datamodel.OrderItem
import com.ayakashikitsune.fishifymarket.datamodel.ShopItem
import com.ayakashikitsune.fishifymarket.datamodel.UserClient
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseService {
    /*used for authentication login and sign up
    * 1. sign up UserClient
    * 2. login UserClient
    * */
    class EmailAuthenticationService {
        private val LogTagger = "EmailAuthenticationService"
        private val auth = Firebase.auth
        suspend fun signUpWithEmail(
            email: String,
            password: String
        ): FirebaseUser? {
            val funname = "signUpWithEmail"
            auth.initializeRecaptchaConfig()
            val fetch = auth.createUserWithEmailAndPassword(email, password)
                .addOnFailureListener {
                    Log.d(LogTagger, "$funname error: $it:")
                }
                .addOnSuccessListener {
                    Log.d(LogTagger, "$funname: Success")
                }
            try {
                val data = fetch.await()
                if (fetch.isSuccessful) {
                    val result = data.user
                    return result
                } else {
                    throw Exception(fetch.exception)
                }
            } catch (e: Exception) {
                return null
            }
        }

        suspend fun changePassword(
            password: String,
            showSnackbar: (String) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                auth.currentUser?.updatePassword(password)
                    ?.addOnSuccessListener {
                        showSnackbar("Password updated")
                    }
                    ?.addOnFailureListener {
                        showSnackbar("Failed to change  Password")
                    }
            }
        }

        suspend fun loginWithEmail(
            email: String,
            password: String
        ): FirebaseUser? {
            val funname = "loginWithEmail"
            auth.initializeRecaptchaConfig()
            val fetch = auth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener {
                    Log.d(LogTagger, "$funname error: $it:")
                }
                .addOnSuccessListener {
                    Log.d(LogTagger, "$funname: Success")
                }
            try {
                val data = fetch.await()
                val result = data.user
                return result
            } catch (e: Exception) {
                return null
            }
        }

        suspend fun signOut() {
            withContext(Dispatchers.IO) {
                auth.signOut()
            }
        }
    }

    /*used to interface with Firestore to
    * 1. Add UserProfile
    * 2. read UserProfile
    * 3. update UserProfile
    * 4. delete UserProfile*/
    class FirebaseFireStoreUserService {
        /*point to user collection*/
        private val PATH_COLLECTION = "users"
        private val TAG = "FirebaseFireStoreUserService"
        private val database = Firebase.firestore.collection(PATH_COLLECTION)

        /*create*/
        suspend fun insertUserData(
            user: UserClient,
            showSnackbar: (message: String) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                val funname = "insertUserData"
                database
                    .document(user.UID)
                    .set(user.toHashMap())
                    .addOnSuccessListener {
                        Log.i(TAG, "$funname: Account added")
                        showSnackbar("Account successfully Signed Up")
                    }
                    .addOnFailureListener { err ->
                        Log.w(TAG, "$funname $err")
                    }
            }
        }

        /*read*/
        suspend fun readUserData(uid: String): UserClient? {
            return withContext(Dispatchers.IO) {
                val fetch = database
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        Log.d("read data", "result ${doc.data}")
                    }
                try {
                    val data = fetch.await()
                    val result = data.toObject(UserClient::class.java)
                    return@withContext result
                } catch (e: Exception) {
                    Log.w("read data", e)
                    return@withContext null
                }
            }
        }

        /*update*/
        suspend fun updateUserData(
            user: UserClient,
            showSnackbar: (String) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                val funname = "updateUserData"
                database
                    .document(user.UID)
                    .update(user.toHashMap())
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success ${user.UID}")
                        showSnackbar("Adding to Cart success")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "$funname fail ${user.UID}")
                        showSnackbar("Failed to Add item")
                    }
            }
        }

        /*delete*/
        suspend fun deleteUserData(
            uid: String,
            onSuccess: () -> Unit,
            onFail: (Exception) -> Unit
        ) {
            val funname = "deleteUserData"
            database.document(uid)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "$funname success")
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    Log.d(TAG, "$funname failed")
                    onFail(error)
                }
        }
    }

    /*used to inteface with Firestore to
    * 1. Read all Shopitems
    * 2. read specific Shopitem
    * */
    class FirebaseFireStoreItemService {
        private val PATH_COLLECTION = "items"
        private val LoggerTag = "FirebaseFireStoreItemService"
        private val database = Firebase.firestore.collection(PATH_COLLECTION)
        /*read*/
        data class Pref(val listOfStoreOwners: ArrayList<String> = arrayListOf<String>())

        suspend fun sellersList(): List<String> {
            val getlistofSellers = Firebase.firestore.collection("preferences")
                .document("systemPref")
                .get()
                .addOnSuccessListener {
                    Log.d(LoggerTag, " info ${it.data}")
                }
            val listofsellers = getlistofSellers.await()
            val sellerlist = listofsellers.toObject(Pref::class.java)
            return sellerlist!!.listOfStoreOwners
        }

        suspend fun readAllShopItemData(
            list: List<String>,
            showSnackbar: (String) -> Unit
        ): List<ShopItem> {
            return withContext(Dispatchers.IO) {
                val funname = "readAllData"

                val fetch = database
                    .whereIn("OID", list)
                    .get()
                    .addOnFailureListener {
                        Log.d(LoggerTag, "$funname fail: $it")
                    }
                    .addOnSuccessListener {
                        Log.d(LoggerTag, "$funname success: ${it.documents}")
                    }
                try {
                    val data = fetch.await()
                    val result = data.toObjects(ShopItem::class.java)
                    return@withContext result
                } catch (e: Exception) {
                    Log.w(LoggerTag, "$funname error: $e")
                    showSnackbar("Failed to read database, $e")
                    return@withContext emptyList()
                }
            }
        }

        suspend fun readSpecificShopItemData(
            IID: String,
            showSnackbar: (String) -> Unit
        ): ShopItem? {
            return withContext(Dispatchers.IO) {
                val funname = "readSpecificShopItemData"
                val fetch = database.document(IID)
                    .get()
                    .addOnFailureListener {
                        Log.d(LoggerTag, "$funname fail: $it")
                    }
                    .addOnSuccessListener {
                        Log.d(LoggerTag, "$funname success: ${it}")
                    }
                try {
                    val data = fetch.await()

                    val result = data.toObject(ShopItem::class.java)
                    return@withContext result

                } catch (e: Exception) {
                    Log.w(LoggerTag, "$funname $e")
                    showSnackbar("Failed to read database, $e")
                    return@withContext null
                }
            }
        }
    }

    /*used to interface Firebase Storage to
    * 1. fetch image as URI from Shopitems*/
    class FirebaseStorageItemService {
        private val PATH_COLLECTION = "items/images"
        private val TAG = "FirebaseStorageItemService"
        private val storage = Firebase.storage.reference

        suspend fun fetchImage(
            iid: String,
            oid: String
        ): Uri {
            return withContext(Dispatchers.IO) {
                val funname = "fetchImage"
                val fetch = storage
                    .child(PATH_COLLECTION)
                    .child(oid) //owner id
                    .child(iid) // filename
                    .downloadUrl
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success $it")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "$funname fail $it")
                    }
                try {
                    val data = fetch.await()
                    Log.d(TAG, "$funname data: $data")
                    val result = data
                    return@withContext result
                } catch (e: Exception) {
                    Log.d(TAG, "$funname fail $e")
                    return@withContext Uri.EMPTY
                }
            }
        }

        /*userclient cant delete and upload any images to shopitems*/
    }

    /*used to intefacce Firebase Storage to
    * 1. upload image as profile picture
    * 2. delete profile picture
    * 3. update profile picture*/
    class FirebaseStorageUserService {
        private val PATH_COLLECTION = "users/images"
        private val TAG = "FirebaseStorageUserService"
        private val storage = Firebase.storage.reference

        suspend fun uploadImage(
            uid: String,
            uri: Uri
        ) {
            val funname = "uploadImage"
            try {
                storage
                    .child(PATH_COLLECTION)
                    .child(uid)
                    .child(uid)
                    .putFile(uri)
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success upload")
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "$funname fail $e")
                        throw e
                    }.await()
            } catch (e: Exception) {
                storage
                    .child(PATH_COLLECTION)
                    .child(uid)
                    .child(uid)
                    .putFile(uri)
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success upload")
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "$funname fail $e")
                    }
            }
        }

        suspend fun fetchImage(
            filename: String,
            uid: String
        ): Uri {
            return withContext(Dispatchers.IO) {
                val funname = "fetchImage"
                val fetch = storage
                    .child(PATH_COLLECTION)
                    .child(uid) //owner id
                    .child(filename) // filename
                    .downloadUrl
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success $it")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "$funname fail $it")
                    }
                try {
                    val data = fetch.await()
                    Log.d(TAG, "$funname data: $data")
                    val result = data
                    return@withContext result
                } catch (e: Exception) {
                    Log.d(TAG, "$funname fail $e")
                    return@withContext Uri.EMPTY
                }
            }
        }

        suspend fun deleteImage(
            filename: String,
            uid: String
        ) {
            withContext(Dispatchers.IO) {
                val funname = "deleteImage"
                storage
                    .child(PATH_COLLECTION)
                    .child(uid) // owner id
                    .child(filename) // filename
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success")
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "$funname fail $e")
                    }
            }
        }

    }

    /*used to interface Firestore to
    * 1. add/place orders
    * 2. read all orders from specific userclient
    * 3. delete/cancel (within certain time) orders
    * 4. update one Order states or items
    * */
    class FirebaseFireStoreOrderService {
        private val PATH_COLLECTION = "orders"
        private val TAG = "FirebaseFireStoreOrderService"
        private val database = Firebase.firestore.collection(PATH_COLLECTION)

        fun createID(): String {
            return database.document().id
        }

        suspend fun addOrder(
            orderItem: OrderItem,
            onSuccess: () -> Unit,
            onFail: (Exception) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                val id = createID()
                database.document(id)
                    .set(orderItem.copy(OrderID = id).toHashMap())
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { error ->
                        onFail(error)
                    }
            }
        }

        suspend fun readAllOrders(
            UID: String,
        ): List<OrderItem>? {
            val funname = "readAllOrders"
            return withContext(Dispatchers.IO) {
                val fetch = database
                    .whereEqualTo("UID", UID)
                    .get()
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success ${it.documents}")
                    }
                    .addOnFailureListener { error ->
                        Log.d(TAG, "$funname failed $error")
                    }
                try {
                    val data = fetch.await()
                    val result = data.toObjects(OrderItem::class.java)
                    Log.d(TAG, "result test : $result")
                    result
                } catch (e: Exception) {
                    Log.d(TAG, "$funname failed $e")
                    null
                }
            }
        }

        fun deleteOrder(
            orderID: String,
            onSuccess: () -> Unit,
            onFail: (Exception) -> Unit
        ) {
            database.document(orderID)
                .delete()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    onFail(error)
                }
        }

        fun updateOrder(
            orderItem: OrderItem,
            onSuccess: () -> Unit,
            onFail: (Exception) -> Unit,
        ) {
            database.document(orderItem.OrderID)
                .update(orderItem.toHashMap())
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    onFail(error)
                }
        }
    }
}
