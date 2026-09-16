package com.example.core.executor

sealed class ExecutionResult {
    data class Success(
        val message: String,
        val verified: Boolean,
        val oldValue: String? = null,
        val newValue: String? = null
    ) : ExecutionResult()

    data class Failure(
        val error: String,
        val exitCode: Int = -1,
        val details: String? = null
    ) : ExecutionResult()

    data class Unsupported(
        val reason: String
    ) : ExecutionResult()

    data class PermissionDenied(
        val requiredPermission: String
    ) : ExecutionResult()

    data class Timeout(
        val durationMs: Long
    ) : ExecutionResult()

    data class VerificationFailed(
        val expected: String,
        val actual: String,
        val message: String
    ) : ExecutionResult()
}

data class OptimizationResult(
    val id: String,
    val success: Boolean,
    val verified: Boolean,
    val oldValue: String?,
    val newValue: String?,
    val message: String,
    val error: String?,
    val timestamp: Long = System.currentTimeMillis()
)
