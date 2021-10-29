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

data class AuthConfirm(
    var auth_num: String,
    var email: String
)