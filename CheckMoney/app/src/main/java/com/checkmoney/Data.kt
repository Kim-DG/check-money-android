package com.checkmoney

data class Email(
    var email: String
)

data class IdToken(
    var id_token: String
)

data class Result(
    var result: String?
)

data class ResultAndToken(
    var result: String?,
    var token: String?
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