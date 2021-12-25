package com.checkmoney

import android.annotation.SuppressLint
import android.os.Parcelable
import com.checkmoney.Main.TabFragmentMonth
import com.checkmoney.Main.TabFragmentYear
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

data class EmailPwd(
    var email: String,
    var newPassword: String
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

@Parcelize
data class TransactionModel(
    var id: Int,
    var is_consumption: Int,
    var price: Int,
    var detail: String,
    var date: String,
    var category: Int,
    var account_id: Int
):Parcelable

data class Transaction(
    var is_consumption: Int,
    var price: Int,
    var detail: String,
    var date: String,
    var category: Int,
    var account_id: Int
)

data class EditTransaction(
    var is_consumption: Int,
    var price: Int,
    var detail: String,
    var date: String,
    var category: Int,
)

data class EditMyInfo(
    var img_url: String?,
    var name: String,
    var password: String?,
    var new_password: String?
)
//-----------------------------------------------------------------------
//                              Response
//-----------------------------------------------------------------------
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
    var refresh_token: String?,
    var user_id: Int,
    var name: String
)

data class ResultAccountList(
    var result: Boolean,
    var code: Int,
    var message: String,
    var rows: ArrayList<AccountModel>,
    var count: Int
)

data class ResultTransactions(
    var result: Boolean,
    var code: Int,
    var message: String,
    var rows: ArrayList<TransactionModel>,
    var count: Int
)

data class ResultId(
    var result: Boolean,
    var code: Int,
    var message: String?,
    var id: Int
)

data class ResultMyInfo(
    var result: Boolean,
    var code: Int,
    var message: String,
    var id: Int,
    var email: String,
    var name: String,
    var img_url: String?,
    var provider: String
)

data class ResultImageUrl(
    var result: Boolean,
    var code: Int,
    var message: String,
    var url: String
)

data class ErrorResult(
    var result: Boolean,
    var code: Int,
    var message: String?
)

@Parcelize
data class ProfileData(var title: String, var description: String, var id: Int): Parcelable {}

@Parcelize
data class MoneyProfileData(var is_consumption: Int, var price: Int, var detail: String, var date: DateType, var category: Int, var account_id: Int): Parcelable {
    companion object {
        const val PRICE_TYPE = 0
        const val DATE_TYPE = 1
    }
}

@Parcelize
data class DateType(var id: Int, var type: Int, var date: Date): Parcelable{}

@Parcelize
data class Date(var year: String, var month: String, var day: String): Parcelable{}


object MoneyProfileDataList{
    var datas: MutableList<MoneyProfileData> = mutableListOf()
}

object ProfileDataList{
    var datas: MutableList<ProfileData> = mutableListOf()
}

object SubsProfileDataList{
    var datas: MutableList<TransactionModel> = mutableListOf()
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

object consumption{
    val consumption: ArrayList<String> = arrayListOf("수입", "지출")
}

interface ResourceStore {
    companion object {
        val tabList = listOf(
            "월별 차트","연도별 차트"
        )
        val pagerFragments = listOf(
            TabFragmentMonth.create(), TabFragmentYear.create())
    }
}

