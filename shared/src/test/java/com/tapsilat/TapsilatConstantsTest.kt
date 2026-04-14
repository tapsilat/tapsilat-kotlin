package com.tapsilat

import kotlin.test.Test
import kotlin.test.assertEquals

class TapsilatConstantsTest {

    @Test
    fun `known status names map to correct codes`() {
        assertEquals(1, TapsilatConstants.getOrderStatusByString("Received"))
        assertEquals(3, TapsilatConstants.getOrderStatusByString("Paid"))
        assertEquals(8, TapsilatConstants.getOrderStatusByString("Cancelled"))
        assertEquals(10, TapsilatConstants.getOrderStatusByString("Refunded"))
        assertEquals(15, TapsilatConstants.getOrderStatusByString("Partially refunded"))
        assertEquals(29, TapsilatConstants.getOrderStatusByString("Suspect"))
    }

    @Test
    fun `unknown status name returns 0`() {
        assertEquals(0, TapsilatConstants.getOrderStatusByString("NonExistent"))
        assertEquals(0, TapsilatConstants.getOrderStatusByString(""))
    }

    @Test
    fun `order status constants have expected values`() {
        assertEquals(1, TapsilatConstants.ORDER_STATUS_RECEIVED)
        assertEquals(3, TapsilatConstants.ORDER_STATUS_PAID)
        assertEquals(21, TapsilatConstants.ORDER_STATUS_EXPIRED)
        assertEquals(29, TapsilatConstants.ORDER_STATUS_SUSPECT)
    }

    @Test
    fun `order type constants have expected values`() {
        assertEquals(1, TapsilatConstants.ORDER_TYPE_PHYSICAL)
        assertEquals(2, TapsilatConstants.ORDER_TYPE_VIRTUAL)
        assertEquals(3, TapsilatConstants.ORDER_TYPE_MARKETPLACE)
    }

    @Test
    fun `subscription status constants are correct`() {
        assertEquals("success", TapsilatConstants.SUBSCRIPTION_STATUS_SUCCESS)
        assertEquals("failure", TapsilatConstants.SUBSCRIPTION_STATUS_FAILURE)
        assertEquals("pending", TapsilatConstants.SUBSCRIPTION_STATUS_PENDING)
    }

    @Test
    fun `default endpoint is set`() {
        assertEquals("https://panel.tapsilat.dev/api/v1", TapsilatConstants.DEFAULT_ENDPOINT)
    }
}
