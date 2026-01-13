package com.shibler.transferfiles

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform