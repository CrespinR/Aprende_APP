package com.example.aprendeapp.util

object PasswordValidator {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(password: String): ValidationResult {
        if (password.length < 12) {
            return ValidationResult.Invalid("La contraseña debe tener al menos 12 caracteres.")
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult.Invalid("La contraseña debe contener al menos una letra mayúscula.")
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult.Invalid("La contraseña debe contener al menos una letra minúscula.")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult.Invalid("La contraseña debe contener al menos un número.")
        }
        val specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?\\"
        if (!password.any { it in specialChars }) {
            return ValidationResult.Invalid("La contraseña debe contener al menos un carácter especial (ej. !@#$%^&*).")
        }
        return ValidationResult.Valid
    }

    fun isValid(password: String): Boolean {
        return validate(password) is ValidationResult.Valid
    }
}
