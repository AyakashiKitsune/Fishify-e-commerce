package com.ayakashikitsune.fishifymarket.domain

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.ayakashikitsune.fishifymarket.datamodel.OrderItem
import com.ayakashikitsune.fishifymarket.datamodel.PerOrderItem
import com.ayakashikitsune.fishifymarket.datamodel.ShopItem
import com.ayakashikitsune.fishifymarket.datamodel.UserClient
import com.ayakashikitsune.fishifymarket.datamodel.service.FirebaseService
import com.ayakashikitsune.fishifymarket.datamodel.toPerOrderItem
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class FishifyViewModel : ViewModel() {
    val TagLogger = "Viewmodel"

    /*user profile*/
    private val _userProfileDetail = MutableStateFlow<UserClient>(UserClient())
    val userProfileFlow = _userProfileDetail.asStateFlow()

    /*Home sample UI Status*/
    private val _shopItemInfo = mutableStateListOf<ShopItem>()
    val shopItemInfoFlow = _shopItemInfo

    /*Cart sample data*/
    private val _listofCartItem = mutableStateListOf<PerOrderItem>()
    val listofCartItemFlow = _listofCartItem
    fun getRandomSearchRecommend() =
        shopItemInfoFlow.get((0..shopItemInfoFlow.size - 1).random()).name

    /*sellers list*/
    private val _sellersListInfo = mutableStateListOf<String>()
    val sellerListInfoFlow = _sellersListInfo

    private val _ordersStatusListInfo = mutableStateListOf<OrderItem>()
    val orderStatusListInfoFlow = _ordersStatusListInfo

    /*Firebase Services*/
    private val _authService = FirebaseService.EmailAuthenticationService()
    private val _userFirestoreService = FirebaseService.FirebaseFireStoreUserService()
    private val _storageService = FirebaseService.FirebaseStorageItemService()
    private val _shopItemFirestoreService = FirebaseService.FirebaseFireStoreItemService()
    private val _userProfileFireStorageService = FirebaseService.FirebaseStorageUserService()
    private val _orderFirestoreService = FirebaseService.FirebaseFireStoreOrderService()

    /*currentUser*/
    private var _currentUser: FirebaseUser? = null

    /*************************************************************************//*FIREBASE FIRESTORE LOGIN AND SIGNUP CREATION*/
    suspend fun createUserinfoService(
        user: UserClient, onfail: (String) -> Unit
    ) {
        val uid = _currentUser!!.uid
        val newUser = user.copy(UID = _currentUser!!.uid)/*for first time signing up */
        _userProfileDetail.update { newUser }
        _userFirestoreService.insertUserData(user = newUser, showSnackbar = { onfail(it) })
    }

    suspend fun readUserinfoService() {
        val funname = "readUserInfoService"
        try {
            val uid = _currentUser!!.uid
            val userClient = _userFirestoreService.readUserData(uid)
            if (userClient != null) {
                _userProfileDetail.update { userClient }
            } else {
                throw Exception("userProfile not found or banned")
            }
        } catch (e: Exception) {
            Log.d(TagLogger, "$funname $e")
        }

    }

    fun readUserCart() {
        _listofCartItem.clear()
        val items = _userProfileDetail.value.ordersList.map { it.toPerOrderItem() }
        _listofCartItem.addAll(items)
    }

    suspend fun updateUserinfoService(
        user: UserClient, uri: Uri?, showSnackBar: (String) -> Unit
    ) {
        var userClient = user
        if (uri != null) {
            runBlocking {
                uploadProfileImage(uri)
            }
            val link = fetchProfileImage(user.UID, user.UID)
            userClient = user.copy(profileLink = link.toString())
        }
        _userProfileDetail.update { userClient }
        _userFirestoreService.updateUserData(userClient, { showSnackBar(it) })
    }

    suspend fun removeCartItems(
        userClient: (UserClient) -> UserClient
    ) {
        val newUserClient = userClient(_userProfileDetail.value)
        _userProfileDetail.update {
            newUserClient
        }
        _userFirestoreService.updateUserData(newUserClient, showSnackbar = {})
    }

    suspend fun addThisItemToCart(shopItem: ShopItem, showSnackBar: (String) -> Unit) {
        var exist = -1
        _userProfileDetail.value.ordersList.forEachIndexed { index, hashMap ->
            if (hashMap["IID"] == shopItem.IID) {
                exist = index
            }
        }
        if (exist != -1) {
            showSnackBar("Item exist in Cart")
        } else {
            val newArraylist = _userProfileDetail.value.ordersList.apply {
                add(shopItem.toPerOrderItem().toHashMap())
            }
            val newCopy = _userProfileDetail.value.copy(
                ordersList = newArraylist
            )
            _userFirestoreService.updateUserData(newCopy, { showSnackBar(it) })
        }
    }

    /*****************************************************************//*FIREBASE AUTHENTICATION*/
    suspend fun signup(
        email: String,
        password: String,
        userClient: UserClient,
        onSuccess: () -> Unit,
        isLoadingModeState: () -> Unit,
        onFail: (String) -> Unit
    ) {
        try {
            isLoadingModeState()
            val result = _authService.signUpWithEmail(email, password)
            _currentUser = result
            delay(500)
            Log.d(TagLogger, "result ${result}")
            if (result != null) {
                createUserinfoService(user = userClient.copy(UID = _currentUser!!.uid),
                    onfail = { errMessage -> onFail(errMessage) })
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                throw Exception("Sign up TaskFailed ${result}")
            }
            isLoadingModeState()
        } catch (e: Exception) {
            Log.d(TagLogger, "${e.printStackTrace()}")
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
            val result = _authService.loginWithEmail(email, password)
            _currentUser = result
            delay(500)
            Log.d(TagLogger, "Login result ${result} ${_currentUser?.uid}")
            if (result != null) {
                readUserinfoService()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                throw Exception("Login TaskFailed ${result}")
            }
            isLoadingModeState()
        } catch (e: Exception) {
            Log.d(TagLogger, "${e.printStackTrace()}")
            onFail("$e")
        }
    }

    suspend fun changePassword(password: String, showSnackBar: (String) -> Unit) {
        _authService.changePassword(password, { showSnackBar(it) })
    }

    suspend fun signout(
        afterExecution: () -> Unit, showSnackBar: (String) -> Unit
    ) {
        _authService.signOut()
        showSnackBar("Log out Success")
        afterExecution()
    }

    /*******************************************************************//*FIREBASE STORAGE SHOPITEM*/
    suspend fun getShopitemImage(filename: String, oid: String): Uri {
        return _storageService.fetchImage(iid = filename, oid = oid)
    }


    /*FIREBASE STORAGE PROFILEPICTURE*/
    suspend fun uploadProfileImage(uri: Uri) {
        _userProfileFireStorageService.uploadImage(
            uid = _userProfileDetail.value.UID, uri = uri
        )
    }

    suspend fun fetchProfileImage(filename: String, uid: String): Uri {
        return _userProfileFireStorageService.fetchImage(filename = filename, uid = uid)
    }

    suspend fun deleteProfileImage(filename: String) {
        _userProfileFireStorageService.deleteImage(
            filename = filename, uid = _userProfileDetail.value.UID
        )
    }

    /********************************************************//*FIRESTORE FIRESTORE SHOPITEMS*/
    suspend fun getSellers() {
        _sellersListInfo.clear()
        val data = _shopItemFirestoreService.sellersList()
        data.map {
            _sellersListInfo.add(it)
        }
    }

    suspend fun readAllShopItem(
        showSnackBar: (String) -> Unit
    ): List<ShopItem> {
        val funname = "readAllShopItem"
        _shopItemInfo.clear()
        getSellers()
        val result = _shopItemFirestoreService.readAllShopItemData(list = _sellersListInfo.toList(),
            showSnackbar = { showSnackBar(it) })
        result.map {
            _shopItemInfo.add(it)
        }
        Log.d(TagLogger, "$funname vm is $result")

        return result.shuffled()
    }

    suspend fun readSpecificShopItem(
        IID: String, showSnackBar: (String) -> Unit
    ): ShopItem? {
        return _shopItemFirestoreService.readSpecificShopItemData(IID, { showSnackBar(it) })
    }

    suspend fun confirmPurchase(
        perOrderItem: ArrayList<HashMap<String, Any>>,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        val orderItem = OrderItem(
            UID = _userProfileDetail.value.UID, orders = perOrderItem, reasonCancelled = ""
        )
        _orderFirestoreService.addOrder(orderItem = orderItem,
            onFail = { onFail("$it") },
            onSuccess = { onSuccess() })
    }

    suspend fun readAllUserclientsOrder() {
        _ordersStatusListInfo.clear()
        val fetch = _orderFirestoreService.readAllOrders(_userProfileDetail.value.UID)
        if (fetch != null) {
            _ordersStatusListInfo.addAll(fetch)
        }
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
