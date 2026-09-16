package com.example.core.safety

enum class PackageCategory(val label: String, val isSafeToModify: Boolean) {
    SYSTEM_CRITICAL("System Core Critical", false),
    SYSTEM_UI("System UI & Window Manager", false),
    TELEPHONY("Telephony & Emergency Services", false),
    LAUNCHER("Home Launcher", false),
    INPUT("Keyboard & Input Method", false),
    SECURITY("Security & Device Administrator", false),
    SETTINGS("System Settings Provider", false),
    SHIZUKU("Shizuku Privilege Service", false),
    OEM_CRITICAL("OEM Core Service", false),
    GAME("Detected Game Application", true),
    USER_APP("User Installed Application", true),
    OEM_OPTIONAL("OEM Non-Essential Bloat", true),
    UNKNOWN("Unknown / Unclassified Package", false)
}

object ProtectedPackages {
    private val PROTECTED_PREFIXES = setOf(
        "com.android.systemui",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.settings",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.inputmethod",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher",
        "com.miui.home",
        "com.mi.android.globallauncher",
        "com.miui.securitycenter",
        "com.miui.securityadd",
        "moe.shizuku.privileged.api",
        "com.example" // FLUX
    )

    fun classifyPackage(packageName: String, isSystemApp: Boolean, isGame: Boolean): PackageCategory {
        if (packageName == "com.example") return PackageCategory.SYSTEM_CRITICAL
        if (packageName.startsWith("moe.shizuku")) return PackageCategory.SHIZUKU
        if (packageName.contains("systemui", ignoreCase = true)) return PackageCategory.SYSTEM_UI
        if (packageName.contains("telecom", ignoreCase = true) || packageName.contains("telephony", ignoreCase = true) || packageName == "com.android.phone") {
            return PackageCategory.TELEPHONY
        }
        if (packageName.contains("launcher", ignoreCase = true) || packageName == "com.miui.home" || packageName == "com.mi.android.globallauncher") {
            return PackageCategory.LAUNCHER
        }
        if (packageName.contains("inputmethod", ignoreCase = true) || packageName.contains("keyboard", ignoreCase = true)) {
            return PackageCategory.INPUT
        }
        if (packageName == "com.android.settings" || packageName.contains("settings.intelligence")) {
            return PackageCategory.SETTINGS
        }
        if (packageName.contains("securitycenter", ignoreCase = true) || packageName.contains("securityadd", ignoreCase = true)) {
            return PackageCategory.SECURITY
        }
        if (isGame) {
            return PackageCategory.GAME
        }

        if (isSystemApp) {
            // Check if known optional OEM app
            if (isOptionalOemApp(packageName)) {
                return PackageCategory.OEM_OPTIONAL
            }
            // Check if matches protected prefix
            for (prefix in PROTECTED_PREFIXES) {
                if (packageName.startsWith(prefix)) return PackageCategory.SYSTEM_CRITICAL
            }
            return PackageCategory.OEM_CRITICAL
        }

        return PackageCategory.USER_APP
    }

    fun isProtected(packageName: String): Boolean {
        for (prefix in PROTECTED_PREFIXES) {
            if (packageName.startsWith(prefix)) return true
        }
        return false
    }

    private fun isOptionalOemApp(pkg: String): Boolean {
        val optionalTokens = listOf(
            "analytics", "bugreport", "feedback", "mab", "msa", "browser", "compass", "notes", "recorder"
        )
        return optionalTokens.any { pkg.contains(it, ignoreCase = true) }
    }
}
