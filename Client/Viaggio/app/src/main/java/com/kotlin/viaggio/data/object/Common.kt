package com.kotlin.viaggio.data.`object`


enum class PermissionError {
    CAMERA_PERMISSION, STORAGE_PERMISSION, NECESSARY_PERMISSION
}
enum class TravelingError {
    COUNTRY_EMPTY, THEME_EMPTY, IMAGE_EMPTY
}
enum class SignError {
    EMAIL_NOT_FOUND, WRONG_PW, DELETE_ID, EMAIL_MISMATCH,
    PW_MISMATCH, SAME_PW, INVALID_EMAIL_FORMAT, EXIST_EMAIL
}