package com.ayakashikitsune.fishifymanager.domain

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.ayakashikitsune.fishifymanager.data.models.OrderItem
import com.ayakashikitsune.fishifymanager.data.models.Owner
import com.ayakashikitsune.fishifymanager.data.models.ShopItem
import com.ayakashikitsune.fishifymanager.data.models.toPerOrderItem
import com.ayakashikitsune.fishifymanager.data.services.FirebaseService
import com.ayakashikitsune.fishifymanager.data.services.TaskResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class FishifyManagerViewModel : ViewModel() {
    /*Logger tag*/
    private val TagLogger = "Viewmodel"

    /*Firebase Services*/
    private val _authService = FirebaseService.EmailAuthenticationService()
    private val _userFirestoreService = FirebaseService.FirebaseFireStoreUserService()
    private val _storageService = FirebaseService.FirebaseStorageItemService()
    private val _shopItemFirestoreService = FirebaseService.FirebaseFireStoreItemService()
    private val _orderItemFirestoreService = FirebaseService.FirebaseFirestoreOrderService()

    /*currentUser*/
    private var _currentUser: FirebaseUser? = null

    /*Stateflow of*//*Owner's Value*/
    private val _ownersInfo = MutableStateFlow(Owner())
    val ownerInfoFlow = _ownersInfo.asStateFlow()

    /*Delivery OrdersList*/
    private val _onDeliveryInfo = mutableStateListOf<OrderItem>()
    val onDeliveryInfoFlow = _onDeliveryInfo

    /*Shopitems list*/
    private var _shopItemInfo = mutableStateListOf<ShopItem>()
    val shopItemInfoFlow = _shopItemInfo

    /*************************************************************************//*FIREBASE FIRESTORE*/
    suspend fun createUserinfoService(
        owner: Owner,
        onfail: (String) -> Unit
    ) {
        val uid = _currentUser!!.uid
        val newOwner = Owner(
            name = owner.name,
            OID = uid,
            address = owner.address,
            contactNumber = owner.contactNumber,
            email = owner.email,
        )/*for first time signing up */
        _ownersInfo.update {
            newOwner
        }
        _userFirestoreService.insertUserData(owner = newOwner, showSnackbar = { onfail(it) })
    }

    suspend fun readUserinfoService() {
        try {
            val uid = _currentUser!!.uid
            val owner = _userFirestoreService.readUserData(uid)
            if (owner != null) _ownersInfo.update {
                owner
            }
            else throw Exception("Error try logging in again")
        } catch (e: Exception) {
            Log.d("readUserInfoService", "vm readUserInfoService $e")
        }

    }

    suspend fun updateUserinfoService(
        uri: Uri?,
        owner: Owner,
        showSnackBar: (String) -> Unit
    ) {
        var newowner = owner
        if (uri != null) {
            runBlocking {
                uploadImage(filename = owner.OID, uri)
            }
            val link = getImage(owner.OID, owner.OID)
            newowner = owner.copy(profilePicture = link.toString())
        }
        _ownersInfo.update { newowner }
        _userFirestoreService.updateUserData(uid = newowner.OID, newowner, { showSnackBar(it) })
    }

    /*****************************************************************//*FIREBASE AUTHENTICATION*/
    suspend fun signup(
        email: String,
        password: String,
        owner: Owner,
        onSuccess: () -> Unit,
        isLoadingModeState: () -> Unit,
        onFail: (String) -> Unit
    ) {
        try {
            isLoadingModeState()
            val result = _authService.signUpWithEmail(email, password)
            _currentUser = result.user
            delay(500)
            Log.d(TagLogger, "result ${result.taskResult}")
            when (result.taskResult) {
                TaskResult.TASKCOMPLETED -> {
                    createUserinfoService(owner = owner.copy(OID = _currentUser!!.uid),
                        onfail = { errMessage -> onFail(errMessage) })
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }

                TaskResult.TASKFAILED -> throw Exception("Sign up TaskFailed ${result.error}")
            }
            isLoadingModeState()
        } catch (e: Exception) {
            onFail("Error: ${e.message}")
        }
    }

    suspend fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        isLoadingModeState: () -> Unit,
        onFail: (String?) -> Unit
    ) {
        try {
            isLoadingModeState()
            val data = _authService.loginWithEmail(email, password)
            val result = data
            _currentUser = result.user
            delay(500)
            Log.d(TagLogger, "Login result ${result.taskResult} ${_currentUser?.uid}")
            when (result.taskResult) {
                TaskResult.TASKCOMPLETED -> {/*when you sign in expected this your currentuser is not null*/
                    readUserinfoService()
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }

                TaskResult.TASKFAILED -> throw Exception("Login TaskFailed ${result.error}")
            }
            isLoadingModeState()
        } catch (e: Exception) {
            Log.d(TagLogger, "${e.printStackTrace()}")
            onFail("$e")
        }
    }

    suspend fun signout(
        afterExecution: () -> Unit,
        showSnackBar: (String) -> Unit
    ) {
        _authService.signOut()
        showSnackBar("Log out Success")
        afterExecution()
    }

    suspend fun changePassword(
        password: String,
        showSnackBar: (String) -> Unit
    ) {
        _authService.changePassword(password, { showSnackBar(it) })
    }

    /*******************************************************************//*FIREBASE STORAGE*/
    suspend fun getImage(
        filename: String,
        oid: String
    ): Uri {
        return _storageService.fetchImage(filename = filename, oid = oid)
    }

    suspend fun uploadImage(
        filename: String,
        uri: Uri
    ) {
        _storageService.uploadImage(filename = filename, oid = _ownersInfo.value.OID, uri = uri)
    }

    suspend fun deleteImage(filename: String) {
        _storageService.deleteImage(filename = filename, oid = _ownersInfo.value.OID)
    }

    /********************************************************//*FIRESTORE SHOPITEMS*/
    suspend fun addShopItem(
        shopItem: ShopItem,
        imageUri: Uri,
        showSnackBar: (String) -> Unit
    ) {
        val document = _shopItemFirestoreService.database.document()  // new document
        // get the ID
        val id = document.id
        // apply the IID
        val newShopItem = shopItem.copy(IID = id, storeName = _ownersInfo.value.storeName)
        val funname = "addShopItem"
        runBlocking {
            _storageService.uploadImage(
                filename = newShopItem.IID, uri = imageUri, oid = _currentUser!!.uid
            )
        }
        val fetchImageLink = getImage(
            filename = newShopItem.IID, oid = _currentUser!!.uid
        ).toString()
        val newitem = newShopItem.copy(
            OID = _currentUser!!.uid, imageLink = fetchImageLink, imageUri = "$imageUri"
        )
        _shopItemInfo.add(newitem)
        _shopItemFirestoreService.insertShopItemData(shopItem = newitem,
            showSnackbar = { showSnackBar(it) })
        Log.d(TagLogger, "$funname is $newitem")
    }

    suspend fun readAllShopItem(showSnackBar: (String) -> Unit): List<ShopItem> {
        val funname = "readAllShopItem"
        _shopItemInfo.clear()
        val result = _shopItemFirestoreService.readAllShopItemData(oid = _ownersInfo.value.OID,
            showSnackbar = { showSnackBar(it) })
        result.map {
            _shopItemInfo.add(it)
        }
        Log.d(TagLogger, "$funname vm is $result")

        return result
    }

    suspend fun updateShopItem(
        shopItem: ShopItem,
        imageUri: Uri?,
        showSnackBar: (String) -> Unit
    ) {
        var newitem: ShopItem = shopItem
        if (imageUri != null) {
            runBlocking {
                _storageService.uploadImage(
                    filename = shopItem.IID, uri = imageUri, oid = shopItem.OID
                )
            }
            val fetchImageLink = getImage(
                filename = shopItem.IID, oid = shopItem.OID
            ).toString()
            newitem = shopItem.copy(
                OID = shopItem.OID, imageLink = fetchImageLink, imageUri = "$imageUri"
            )
            Log.d(TagLogger, "new $newitem")
        }
        runBlocking {
            /*add first before removing*/
            var result = 0
            for (i in 0.._shopItemInfo.size - 1) {
                if (_shopItemInfo[i].IID == newitem.IID) {
                    result = i
                }
            }
            _shopItemInfo.set(result, newitem)
        }

        _shopItemFirestoreService.updateShopItemData(shopItem = newitem,
            showSnackbar = { showSnackBar(it) })
    }

    suspend fun incrementSold(orderItem: OrderItem) {
        var x = -1
        _onDeliveryInfo.forEachIndexed { index, orderItem2 ->
            if (orderItem2.OrderID == orderItem.OrderID) {
                x = index
            }
        }
        _onDeliveryInfo.removeAt(x)
        orderItem.orders.map { it.toPerOrderItem() }.forEach {
                var i = -1
                _shopItemInfo.forEachIndexed { index, shopItem ->
                    if (it.IID == shopItem.IID) {
                        i = index
                    }
                }
                val item = _shopItemInfo[i]
                _shopItemFirestoreService.incrementsold(item, it.quantity)
            }
    }

    suspend fun deleteShopItem(shopItem: ShopItem, showSnackBar: (String) -> Unit) {
        try {
            _shopItemInfo.remove(shopItem)
            _shopItemFirestoreService.deleteShopItemData(iid = shopItem.IID,
                showSnackbar = { showSnackBar(it) })
            deleteImage(filename = shopItem.IID)
            showSnackBar("Delete Success")
        } catch (e: Exception) {
            showSnackBar("$e")
        }

    }

    /***************************************************************************************************//*FIRESTORE ORDERITEMS*/
    suspend fun readOrders() {
        val ordersList = _orderItemFirestoreService.readOrders()
        _onDeliveryInfo.clear()
        _onDeliveryInfo.addAll(ordersList)
    }

    suspend fun updateOrder(orderItem: OrderItem) {
        _orderItemFirestoreService.updateOrder(orderItem)
    }

    suspend fun deleteOrder(orderItem: OrderItem) {
        _orderItemFirestoreService.deleteOrder(orderItem)
    }
}


@SuppressLint("Range")
fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null)
    var result = ""
    try {
        if ((cursor != null) && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    } finally {
        if (cursor != null) {
            cursor.close()
        };
    }
    return result
}
