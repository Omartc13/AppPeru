package com.example.appmovil

enum class NotificationType {
    COMMENT, HEART
}

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val type: NotificationType
)