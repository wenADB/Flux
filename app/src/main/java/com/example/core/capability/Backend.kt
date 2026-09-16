package com.example.core.capability

enum class Backend(val displayName: String) {
    NATIVE_API("Native Android API"),
    SHIZUKU("Shizuku Privileged API"),
    ADB("ADB Shell API"),
    UNSUPPORTED("Unsupported")
}

enum class RiskLevel(val label: String) {
    SAFE("Safe"),
    LOW("Low Risk"),
    MODERATE("Moderate Risk"),
    HIGH("High Risk"),
    UNSUPPORTED("Unsupported")
}
