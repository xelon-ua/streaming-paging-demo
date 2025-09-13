package com.example.streaming_paging_demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform