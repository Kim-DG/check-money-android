package com.checkmoney

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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

@Parcelize
data class ProfileData(var name: String): Parcelable {}

@Parcelize
data class MoneyProfileData(var date: Date, var detail: String, var positive: String, var price: Int?, var category: String): Parcelable {
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

