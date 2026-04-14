package com.tapsilat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ValidatorsTest {

    // ── validateInstallments ──────────────────────────────────────────────────

    @Test
    fun `null installments returns single 1`() {
        assertEquals(listOf(1), Validators.validateInstallments(null))
    }

    @Test
    fun `blank installments returns single 1`() {
        assertEquals(listOf(1), Validators.validateInstallments("   "))
    }

    @Test
    fun `single valid installment is parsed`() {
        assertEquals(listOf(3), Validators.validateInstallments("3"))
    }

    @Test
    fun `comma-separated valid installments are parsed`() {
        assertEquals(listOf(1, 2, 6, 12), Validators.validateInstallments("1,2,6,12"))
    }

    @Test
    fun `installment value 13 throws`() {
        assertFailsWith<ValidationError> { Validators.validateInstallments("13") }
    }

    @Test
    fun `installment value 0 throws`() {
        assertFailsWith<ValidationError> { Validators.validateInstallments("0") }
    }

    @Test
    fun `non-integer installment throws`() {
        assertFailsWith<ValidationError> { Validators.validateInstallments("1,abc,3") }
    }

    @Test
    fun `installment list with spaces is trimmed and parsed`() {
        assertEquals(listOf(1, 2, 3), Validators.validateInstallments(" 1 , 2 , 3 "))
    }

    // ── validateGsmNumber ─────────────────────────────────────────────────────

    @Test
    fun `null phone returns null`() {
        assertNull(Validators.validateGsmNumber(null))
    }

    @Test
    fun `blank phone returns blank`() {
        assertEquals("", Validators.validateGsmNumber(""))
    }

    @Test
    fun `international phone with spaces is normalized`() {
        assertEquals("+905551234567", Validators.validateGsmNumber("+90 555 123 45 67"))
    }

    @Test
    fun `phone with dashes and parens is normalized`() {
        assertEquals("+905551234567", Validators.validateGsmNumber("+90-555(123)4567"))
    }

    @Test
    fun `phone with letters throws`() {
        assertFailsWith<ValidationError> { Validators.validateGsmNumber("+90abc12345") }
    }

    @Test
    fun `international phone too short throws`() {
        assertFailsWith<ValidationError> { Validators.validateGsmNumber("+123") }
    }

    @Test
    fun `national phone too short throws`() {
        assertFailsWith<ValidationError> { Validators.validateGsmNumber("012") }
    }

    @Test
    fun `valid national phone is accepted`() {
        assertEquals("0555123456", Validators.validateGsmNumber("0555123456"))
    }
}
