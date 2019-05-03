package com.kotlin.viaggio.android

sealed class ComponentArg

enum class ArgName {
    OCR_IMAGE_URI, EXTRA_TRANSITION_NAME, TRAVEL_TYPE, TRAVEL_OPTION,
    TRAVEL_CARD_LOCATION
}

enum class WorkerName {
    TRAVELING_OF_DAY_CHECK, COMPRESS_IMAGE, TRAVEL
}
