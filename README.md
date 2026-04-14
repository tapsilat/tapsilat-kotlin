# tapsilat-kotlin

Tapsilat Kotlin is a Kotlin client library for accessing the Tapsilat API.

This SDK is implemented to match the feature set in the Go SDK (`tapsilat-go`) and includes support for order operations, terms, organization currencies, submerchant/suborganization APIs, VPOS management, VPOS-submerchant mappings, and subscription APIs.

[![Build & Publish](https://github.com/tapsilat/tapsilat-kotlin/actions/workflows/publish.yml/badge.svg)](https://github.com/tapsilat/tapsilat-kotlin/actions/workflows/publish.yml)

## Project Layout

This repository uses a single-module layout so feature domains can share one API client and common infrastructure:

- `shared/`: common client, validators, and shared errors
- `order/`: order domain DTOs and services
- `checkout/`: checkout service module (placeholder methods included)
- `credit/`: credit service module (placeholder methods included)

Service split convention:

- `SharedApiClient`: reusable HTTP transport and error parsing
- `OrderService`: order and order-term APIs
- `OrganizationService`: organization settings and currencies
- `SubmerchantService`: submerchant/suborganization APIs
- `VposService`: VPOS and VPOS-submerchant APIs
- `SubscriptionService`: subscription APIs
- `CheckoutService` / `CreditService`: placeholders for upcoming features

## Installation

### Gradle

Add JitPack repository:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

```kotlin
dependencies {
    implementation("com.github.tapsilat:tapsilat-kotlin:v1.0.0")
}
```

Use a Git tag for the version (`v1.0.0`, `v1.1.0`, etc.). JitPack builds directly from GitHub tags.


## Configuration

```kotlin
import com.tapsilat.TapsilatApi

fun main() {
    // Default endpoint
    val api = TapsilatApi.newApi("your_token_here")

    // Custom endpoint
    val customApi = TapsilatApi.newCustomApi(
        endpoint = "https://custom.endpoint.com/api/v1",
        token = "your_token_here"
    )

    // Feature services (recommended)
    val orderApi = api.order
    val checkoutApi = api.checkout
    val creditApi = api.credit
}
```

`TapsilatApi` still includes backward-compatible facade methods (`createOrder`, `getOrder`, etc.) that delegate to these services.

## Usage Examples

### Basic Order Creation

```kotlin
import com.tapsilat.Order
import com.tapsilat.OrderBasketItem
import com.tapsilat.OrderBillingAddress
import com.tapsilat.OrderBuyer
import com.tapsilat.OrderShippingAddress
import com.tapsilat.TapsilatApi

fun main() {
    val api = TapsilatApi.newApi("TOKEN")

    val order = Order(
        locale = "tr",
        currency = "TRY",
        amount = 5.0,
        buyer = OrderBuyer(
            id = "123456789",
            name = "John",
            surname = "Doe",
            email = "john@doe.com",
            gsmNumber = "5555555555",
            identityNumber = "12345678901"
        ),
        shippingAddress = OrderShippingAddress(
            address = "Istanbul",
            zipCode = "34000",
            city = "Istanbul",
            country = "Turkiye",
            contactName = "John Doe"
        ),
        billingAddress = OrderBillingAddress(
            address = "Istanbul",
            zipCode = "34000",
            city = "Istanbul",
            country = "Turkiye",
            contactName = "John Doe"
        ),
        basketItems = listOf(
            OrderBasketItem(
                id = "1",
                name = "Product 1",
                price = 5.0,
                category1 = "Category 1",
                category2 = "Category 2",
                itemType = "VIRTUAL"
            )
        )
    )

    val response = api.createOrder(order)
    println("Order ID: ${response.orderId}")
    println("Reference ID: ${response.referenceId}")
    println("Checkout URL: ${response.checkoutUrl}")
}
```

### Validation Helpers

```kotlin
import com.tapsilat.Validators

fun main() {
    val cleanPhone = Validators.validateGsmNumber("+90 555 123-45-67")
    println(cleanPhone) // +905551234567

    val installments = Validators.validateInstallments("1,2,3,6")
    println(installments) // [1, 2, 3, 6]
}
```

### Submerchant + VPOS Currency Normalization

`createSubmerchant`, `updateSubmerchant`, `createVpos`, and `updateVpos` accept currency references in either format:

- currency UUID
- organization currency unit (`TRY`, `USD`, `EUR`, etc.)

The SDK resolves units to IDs by calling organization currency endpoints and caches currency mappings internally.

### Placeholder Methods

For modules planned later, placeholder methods are included now:

- `CheckoutService.createCheckoutSessionPlaceholder()`
- `CreditService.createCreditApplicationPlaceholder()`

They currently throw `UnsupportedOperationException` until feature implementation is added.

## Implemented API Surface

### Orders

- `createOrder`
- `getOrder`
- `getOrderByConversationID`
- `getOrders`
- `getOrderList`
- `getOrderSubmerchants`
- `getCheckoutUrl`
- `getOrderStatus`
- `getOrderPaymentDetails`
- `getOrderTransactions`
- `cancelOrder`
- `refundOrder`
- `refundAllOrder`

### Order Terms

- `getOrderTerm`
- `createOrderTerm`
- `deleteOrderTerm`
- `updateOrderTerm`
- `refundOrderTerm`
- `orderTerminate`
- `orderManualCallback`
- `orderRelatedUpdate`

### Organization

- `getOrganizationSettings`
- `getOrganizationCurrencies`
- `listOrganizationCurrencyPresets`
- `createOrganizationCurrency`
- `resolveCurrencyId`

### Submerchant and Suborganization

- `createSubmerchant`
- `getSubmerchant`
- `listSubmerchants`
- `updateSubmerchant`
- `deleteSubmerchant`
- `getSuborganizations`
- `getSuborganization`
- `getSuborganizationDetail`
- `getSuborganizationBySubmerchant`
- `getSubmerchantBySuborganization`

### VPOS

- `listVpos`
- `listVposWithFilter`
- `createVpos`
- `getVpos`
- `updateVpos`
- `deleteVpos`
- `listVposAcquirers`
- `listCardSchemes`
- `listVposAcquirerTemplates`

### VPOS-Submerchant Mapping

- `listVposSubmerchants`
- `createVposSubmerchant`
- `getVposSubmerchant`
- `updateVposSubmerchant`
- `deleteVposSubmerchant`

### Subscription

- `getSubscription`
- `cancelSubscription`
- `createSubscription`
- `listSubscriptions`
- `redirectSubscription`

## Error Handling

The SDK throws:

- `ApiError` for non-2xx API responses
- `ValidationError` for local validation failures (phone, installments, currency references)

Example:

```kotlin
try {
    api.getOrder("invalid-id")
} catch (e: com.tapsilat.ApiError) {
    println("status=${e.statusCode}, message=${e.messageText}")
}
```

## Build

```bash
./gradlew clean build
```
