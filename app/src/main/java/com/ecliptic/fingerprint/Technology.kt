package com.ecliptic.fingerprint

data class Technology(
    val name: String,
    val categories: List<String>,
    val icon: String?,
    val website: String?,
    val version: String? = null,
    val confidence: Int = 100,
    val detectionMethod: String? = null,
    val matchedValue: String? = null,
)
