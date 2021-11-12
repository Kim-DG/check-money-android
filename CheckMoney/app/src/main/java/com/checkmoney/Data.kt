package com.checkmoney

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

data class Email(
    var email: String
)

data class IdToken(
    var id_token: String?
)

data class Result(
    var result: Boolean,
    var code: Int,
    var message: String?
)

data class ResultAndToken(
    var result: Boolean,
    var code: Int,
    var message: String?,
    var access_token: String?,
    var refresh_token: String?
)

data class ResultAccountList(
    var result: Boolean,
    var code: Int,
    var message: String,
    var rows: AccountModelList,
    var count: Int
)

data class ResultAccount(
    var result: Boolean,
    var code: Int,
    var message: String?,
    var id: Int
)

data class ErrorResult(
    var result: Boolean,
    var code: Int,
    var message: String?
)

data class AuthConfirm(
    var auth_num: String,
    var email: String
)

data class Join(
    val email: String,
    val password: String,
    val name: String
)

data class UserInfo(
    val email: String,
    val password: String
)

data class RefreshToken(
    var refresh_token: String?
)

data class Account(
    var title: String,
    var description: String
)

data class AccountModelList(
    var accountModel: MutableList<AccountModel>
)

data class AccountModel(
    var id: Int,
    var title: String,
    var description: String,
    var createdAt: String
)

@Parcelize
data class ProfileData(var title: String, var id: Int): Parcelable {}

@Parcelize
data class MoneyProfileData(var date: Date, var detail: String, var positive: String, var price: Long, var category: String): Parcelable {
    companion object {
        const val PRICE_TYPE = 0
        const val DATE_TYPE = 1
    }
}
@Parcelize
data class Date(var type: Int, var year: String, var month: String, var day: String, var time: String): Parcelable{}


object MoneyProfileDataList{
    var datas: MutableList<MoneyProfileData> = mutableListOf()
}

object ProfileDataList{
    var datas: MutableList<ProfileData> = mutableListOf()
}

object SpinnerArray{
    var sData : Array<String> = arrayOf()
    var sData2 : Array<String> = arrayOf()
}

object ListType{
    val TOTAL = 0
    val EXPENSE = 1
    val INCOME = 2
    var listype = -1
}

object ThisTime{
    var cal: Calendar = Calendar.getInstance()
}