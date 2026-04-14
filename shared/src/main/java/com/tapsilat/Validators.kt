package com.tapsilat

object Validators {
    private val PHONE_STRIP = Regex("""[\s\-()\+]""")
    private val PHONE_VALID_CHARS = Regex("""^[+0-9]+$""")
    private val DIGITS_ONLY = Regex("""^[0-9]+$""")

    fun validateInstallments(installmentsStr: String?): List<Int> {
        if (installmentsStr.isNullOrBlank()) return listOf(1)

        val installments = installmentsStr.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map {
                it.toIntOrNull() ?: throw ValidationError(
                    message = "Enabled installments must be comma-separated integers (e.g., 1,2,3 or 2,4,6)",
                )
            }

        installments.forEach { installment ->
            if (installment !in 1..12) {
                throw ValidationError(
                    message = "Installment value '$installment' is invalid. All installment values must be between 1 and 12 (inclusive).",
                )
            }
        }

        return installments.ifEmpty { listOf(1) }
    }

    fun validateGsmNumber(phone: String?): String? {
        if (phone.isNullOrBlank()) return phone

        val cleanedPhone = phone
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        if (!cleanedPhone.matches(PHONE_VALID_CHARS)) {
            throw ValidationError(message = "Invalid phone number format: $phone")
        }

        val contentForValidation = cleanedPhone.replace("+", "")
        if (contentForValidation.isEmpty() || !contentForValidation.matches(DIGITS_ONLY)) {
            throw ValidationError(message = "Invalid phone number format: $phone")
        }

        when {
            cleanedPhone.startsWith("+") && cleanedPhone.length < 8 -> {
                throw ValidationError(message = "International phone number too short: $phone")
            }

            cleanedPhone.startsWith("00") && cleanedPhone.length < 9 -> {
                throw ValidationError(message = "International phone number (00 format) too short: $phone")
            }

            cleanedPhone.startsWith("0") && cleanedPhone.length < 7 -> {
                throw ValidationError(message = "National phone number too short: $phone")
            }

            !cleanedPhone.startsWith("0") && !cleanedPhone.startsWith("+") && cleanedPhone.length < 6 -> {
                throw ValidationError(message = "Local phone number too short: $phone")
            }
        }

        return cleanedPhone
    }
}
