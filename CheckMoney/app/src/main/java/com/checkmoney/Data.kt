package com.checkmoney

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

//request-postEmail 이메일을 입력하고 인증번호 전송
data class Email(
    var email: String
)
//request-postGoogle 구글로그인
data class IdToken(
    var id_token: String?
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

data class AccountModel(
    var id: Int,
    var title: String,
    var description: String,
    var createdAt: String
)

data class TransactionModel(
    var id: Int,
    var is_consuption: Int,
    var price: Int,
    var detail: String,
    var date: Date,
    var category: Int,
    var account_id: Int
)

data class Transaction(
    var is_consuption: Int,
    var price: Int,
    var detail: String,
    var date: Date,
    var category: Int,
    var account_id: Int
)

data class EditTransaction(
    var is_consuption: Int,
    var price: Int,
    var detail: String,
    var date: Date,
    var category: Int,
)
//response
data class Result(
    var result: Boolean,
    var code: Int,
    var message: String?
)
//response
data class ResultAndToken(
    var result: Boolean,
    var code: Int,
    var message: String?,
    var access_token: String?,
    var refresh_token: String?
)
//response
data class ResultAccountList(
    var result: Boolean,
    var code: Int,
    var message: String,
    var rows: ArrayList<AccountModel>,
    var count: Int
)
//response
data class ResultTransactions(
    var result: Boolean,
    var code: Int,
    var message: String,
    var rows: ArrayList<TransactionModel>,
    var count: Int
)
//response
data class ResultId(
    var result: Boolean,
    var code: Int,
    var message: String?,
    var id: Int
)
//response
data class ErrorResult(
    var result: Boolean,
    var code: Int,
    var message: String?
)

@Parcelize
data class ProfileData(var title: String, var description: String, var id: Int): Parcelable {}

@Parcelize
data class MoneyProfileData(var id: Int, var is_consuption: Int, var price: Int, var detail: String, var date: DateType, var category: Int, var account_id: Int): Parcelable {
    companion object {
        const val PRICE_TYPE = 0
        const val DATE_TYPE = 1
    }
}

@Parcelize
data class DateType(var type: Int, var date: Date): Parcelable{}

@Parcelize
data class Date(var year: String, var month: String, var day: String, var time: String): Parcelable{}


object MoneyProfileDataList{
    var datas: MutableList<MoneyProfileData> = mutableListOf()
}

object ProfileDataList{
    var datas: MutableList<ProfileData> = mutableListOf()
}

object SpinnerArray{
    var sData : ArrayList<String> = arrayListOf()
    var sData2 : ArrayList<String> = arrayListOf()
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

object category{
    val category: ArrayList<String> = arrayListOf("식비","쇼핑","주거비","의료비","생활용품비","통신비","교통비","기타")
}

object consumtion{
    val consumtion: ArrayList<String> = arrayListOf("수입", "지출")
}

