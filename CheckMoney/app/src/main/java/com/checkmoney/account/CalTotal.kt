package com.checkmoney.account

interface CalTotal {
    fun calTotal(deleteConsum: Int, deletePrice: Long, addConsum: Int, addPrice: Long)
}