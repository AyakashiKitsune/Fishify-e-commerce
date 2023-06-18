package com.ayakashikitsune.fishifymanager.data.services

import android.net.Uri
import android.util.Log
import com.ayakashikitsune.fishifymanager.data.models.OrderItem
import com.ayakashikitsune.fishifymanager.data.models.Owner
import com.ayakashikitsune.fishifymanager.data.models.ShopItem
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseService {
    class EmailAuthenticationService {
        private val LogTagger = "EmailAuthenticationService"
        private val auth = Firebase.auth

        suspend fun signUpWithEmail(email: String, password: String): LoginResult {
            val funname = "signUpWithEmail"
            auth.initializeRecaptchaConfig()

            val fetch = auth.createUserWithEmailAndPassword(email, password).addOnFailureListener {
                Log.d(LogTagger, "$funname error: $it:")
            }.addOnSuccessListener {
                Log.d(LogTagger, "$funname: Success")
            }
            try {
                val data = fetch.await()
                if (fetch.isSuccessful) {
                    val result = data.user
                    val user = result
                    return LoginResult(user, TaskResult.TASKCOMPLETED)
                } else {
                    throw Exception(fetch.exception)
                }
            } catch (e: Exception) {
                return LoginResult(
                    user = null, taskResult = TaskResult.TASKFAILED, error = "$e"
                )
            }
        }

        suspend fun loginWithEmail(email: String, password: String): LoginResult {
            return withContext(Dispatchers.IO) {
                val funname = "loginWithEmail"
                auth.initializeRecaptchaConfig()
                val fetch = auth.signInWithEmailAndPassword(email, password).addOnFailureListener {
                    Log.d(LogTagger, "$funname error: $it:")
                }.addOnSuccessListener {
                    Log.d(LogTagger, "$funname: Success")
                }
                try {
                    val data = fetch.await()
                    if (fetch.isSuccessful) {
                        val result = data.user
                        val user = result
                        return@withContext LoginResult(user, TaskResult.TASKCOMPLETED)
                    } else {
                        throw Exception(fetch.exception)
                    }
                } catch (e: Exception) {
                    return@withContext LoginResult(
                        user = null, taskResult = TaskResult.TASKFAILED, error = "$e"
                    )
                }
            }
        }

        suspend fun signOut() {
            withContext(Dispatchers.IO) {
                auth.signOut()
            }
        }

        suspend fun changePassword(password: String, showSnackbar: (String) -> Unit) {
            withContext(Dispatchers.IO) {
                auth.currentUser?.updatePassword(password)?.addOnSuccessListener {
                    showSnackbar("Password updated")
                }?.addOnFailureListener {
                    showSnackbar("Failed to change  Password")
                }
            }
        }
    }

    class FirebaseFireStoreUserService {
        /*point to user collection*/
        private val PATH_COLLECTION = "storeOwner"
        private val TAG = "FirebaseFireStoreUserService"
        private val database = Firebase.firestore.collection(PATH_COLLECTION)

        suspend fun insertUserData(
            owner: Owner,
            showSnackbar: (message: String) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                val funname = "insertUserData"
                database.document(owner.OID).set(owner.toHashMap()).addOnSuccessListener {
                    Log.i(TAG, "$funname: Account added")
                    showSnackbar("Account successfully Signed Up")
                }.addOnFailureListener { err ->
                    Log.w(TAG, "$funname $err")
                }
            }
        }

        suspend fun readUserData(uid: String): Owner? {
            return withContext(Dispatchers.IO) {
                val fetch = database.document(uid).get().addOnSuccessListener { doc ->
                    Log.d("read data", "result ${doc.data}")
                }
                try {
                    val data = fetch.await()
                    val result = data.toObject(Owner::class.java)
                    return@withContext result
                } catch (e: Exception) {
                    Log.w("read data", e)
                    return@withContext null
                }
            }
        }

        suspend fun updateUserData(
            uid: String,
            owner: Owner,
            showSnackbar: (String) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                val funname = "updateUserData"
                database.document(uid).update(owner.toHashMap()).addOnSuccessListener {
                    Log.d(TAG, "$funname success $uid")
                    showSnackbar("")
                }.addOnFailureListener {
                    Log.d(TAG, "$funname fail $uid")
                }
            }
        }/*store Owners cant be deleted unless its requested to the admin*/
    }

    class FirebaseFireStoreItemService {
        private val PATH_COLLECTION = "items"
        private val LoggerTag = "FirebaseFireStoreItemService"
        val database = Firebase.firestore.collection(PATH_COLLECTION)

        suspend fun insertShopItemData(
            shopItem: ShopItem,
            showSnackbar: (String) -> Unit
        ) {
            val funname = "insertData"
            withContext(Dispatchers.IO) {
                /*send the Shop item*/
                database.document(shopItem.IID).set(shopItem.toHashMap()).addOnSuccessListener {
                    Log.i(LoggerTag, "$funname file added")
                    showSnackbar("Item added")
                }.addOnFailureListener { err ->
                    Log.w(LoggerTag, "$funname fail $err")
                    showSnackbar("Failed Added, $err")
                }
            }
        }

        suspend fun readAllShopItemData(
            oid: String,
            showSnackbar: (String) -> Unit
        ): List<ShopItem> {
            return withContext(Dispatchers.IO) {
                val funname = "readAllData"
                val fetch = database.whereEqualTo("OID", oid).get().addOnFailureListener {
                    Log.d(LoggerTag, "$funname: $it")
                }.addOnSuccessListener {
                    Log.d(LoggerTag, "$funname: ${it.documents}")
                }
                try {
                    val data = fetch.await()
                    val result = data.toObjects(ShopItem::class.java)
                    return@withContext result
                } catch (e: Exception) {
                    Log.w(LoggerTag, "$funname $e")
                    showSnackbar("Failed to read database, $e")
                    return@withContext emptyList()
                }
            }
        }

        suspend fun updateShopItemData(
            shopItem: ShopItem,
            showSnackbar: (String) -> Unit
        ) {
            withContext(Dispatchers.IO) {
                val funname = "updatedata"
                database.document(shopItem.IID).update(shopItem.toHashMap()).addOnSuccessListener {
                    Log.d(LoggerTag, "$funname success")
                    showSnackbar("Update success")
                }.addOnFailureListener {
                    Log.d(LoggerTag, "$funname ${shopItem.IID} fail $it")
                    showSnackbar("Update failed $it")
                }
            }
        }

        suspend fun deleteShopItemData(
            iid: String,
            showSnackbar: (String) -> Unit
        ) {
            val funname = "deleteData"
            withContext(Dispatchers.IO) {
                database.document(iid).delete().addOnSuccessListener {
                    Log.d(LoggerTag, "$funname succeed")
                    showSnackbar("Delete Success")
                }.addOnFailureListener {
                    Log.d(LoggerTag, "$funname fail $it")
                    showSnackbar("Delete failed $it")
                }

            }
        }

        suspend fun incrementsold(
            shopItem: ShopItem,
            quantity: Int
        ) {
            database.document(shopItem.IID).update("sold", shopItem.sold + quantity)
                .addOnSuccessListener { }.addOnFailureListener { }
        }
    }

    class FirebaseStorageItemService {
        private val PATH_COLLECTION = "items/images"
        private val TAG = "FirebaseStorageItemService"
        val storage = Firebase.storage.reference

        suspend fun fetchImage(
            filename: String,
            oid: String
        ): Uri {
            return withContext(Dispatchers.IO) {
                val funname = "fetchImage"
                val fetch = storage.child(PATH_COLLECTION).child(oid) //owner id
                    .child(filename) // filename
                    .downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "$funname success $it")
                    }.addOnFailureListener {
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
            oid: String
        ) {
            withContext(Dispatchers.IO) {
                val funname = "deleteImage"
                storage.child(PATH_COLLECTION).child(oid) // owner id
                    .child(filename) // filename
                    .delete().addOnSuccessListener {
                        Log.d(TAG, "$funname success")
                    }.addOnFailureListener { e ->
                        Log.d(TAG, "$funname fail $e")
                    }
            }
        }

        suspend fun uploadImage(
            filename: String,
            oid: String,
            uri: Uri
        ) {
            val funname = "uploadImage"
            try {
                storage.child(PATH_COLLECTION).child(oid).child(filename).putFile(uri)
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success upload")
                    }.addOnFailureListener { e ->
                        Log.d(TAG, "$funname fail $e")
                        throw e
                    }.await()
            } catch (e: Exception) {
                storage.child(PATH_COLLECTION).child(oid).child(filename).putFile(uri)
                    .addOnSuccessListener {
                        Log.d(TAG, "$funname success upload")
                    }.addOnFailureListener { e ->
                        Log.d(TAG, "$funname fail $e")
                    }
            }
        }
    }

    class FirebaseFirestoreOrderService {
        private val PATH_COLLECTION = "orders"
        private val LoggerTag = "FirebaseFirestoreOrderService"
        val database = Firebase.firestore.collection(PATH_COLLECTION)

        suspend fun readOrders(): List<OrderItem> {
            return withContext(Dispatchers.IO) {
                val funname = "readOrders"
                val fetch = database.get().addOnSuccessListener { }.addOnFailureListener { }
                try {
                    val data = fetch.await()
                    val result = data.toObjects(OrderItem::class.java)
                    result
                } catch (e: Exception) {
                    Log.d(LoggerTag, "$funname: $e")
                    emptyList()
                }
            }
        }

        suspend fun updateOrder(orderItem: OrderItem) {
            withContext(Dispatchers.IO) {
                val funname = "updateOrder"
                database.document(orderItem.OrderID).update(orderItem.toHashMap())
                    .addOnSuccessListener { Log.d(LoggerTag, "$funname : success") }
                    .addOnFailureListener { Log.d(LoggerTag, "$funname : fail") }
            }
        }

        suspend fun deleteOrder(orderItem: OrderItem) {
            withContext(Dispatchers.IO) {
                val funname = "deleteOrder"
                database.document(orderItem.OrderID).delete().addOnSuccessListener { }
                    .addOnFailureListener { }
            }
        }
    }
}

data class LoginResult(
    var user: FirebaseUser? = null, var taskResult: TaskResult, var error: String = ""
)

enum class TaskResult {
    TASKCOMPLETED, TASKFAILED,
}
