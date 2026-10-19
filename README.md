# tapsilat-kotlin

Tapsilat Kotlin is a Kotlin client library for accessing the Tapsilat API.

This SDK is implemented to match the feature set in the Go SDK (`tapsilat-go`) and includes support for order operations, terms, organization currencies, submerchant/suborganization APIs, VPOS management, VPOS-submerchant mappings, and subscription APIs.

[![Build & Publish](https://github.com/tapsilat/tapsilat-kotlin/actions/workflows/publish.yml/badge.svg)](https://github.com/tapsilat/tapsilat-kotlin/actions/workflows/publish.yml)

[![](https://jitpack.io/v/tapsilat/tapsilat-kotlin.svg)](https://jitpack.io/#tapsilat/tapsilat-kotlin)

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
    implementation("com.github.tapsilat:tapsilat-kotlin:latest.release")
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

### Currency References

Currency fields on submerchants, marketplace sellers, seller contracts and VPOS accept a currency UUID or an organization currency unit (`TRY`, `USD`, ...); the server resolves units, so the SDK sends them unchanged. Marketplace approval policies are the exception: their `currencyId` must be a UUID, so `marketplace.createMarketplaceApprovalPolicy` and `updateMarketplaceApprovalPolicy` resolve a unit first. `resolveCurrencyId` exposes the same cached lookup.

### Replacing Updates

`updateVpos`, `updateSubmerchant`, `updateOrderTerm` and `marketplace.updateMarketplaceSubmerchant` replace the stored record. Read it first and send every field back; for example, leaving out `failover` or `tls` on a VPOS stores `false`.

### Card Installments and Charging

```kotlin
val options = api.getCardInstallments(CardInstallmentsRequest(bin = "54066200", amount = 100.0, currency = "TRY"))
val table = api.getInstallmentTable(InstallmentTableRequest(amount = 100.0, currency = "TRY"))

val charge = api.chargeOrder(OrderChargeRequest(orderReferenceId = "order-reference-id"))
if (charge.isSuccess != true) {
    println("charge failed: ${charge.errorCode} ${charge.message}")
}
```

### Marketplace

`api.marketplace` uses the API version 2 routes derived from your endpoint. Revisions are opaque strings: pass them back exactly as received.

```kotlin
val payout = api.marketplace.getMarketplacePayout(payoutId)
if (payout.eligibilityStatus == "eligible" && payout.releaseStatus == "awaiting_approval") {
    api.marketplace.approveMarketplacePayouts(MarketplacePayoutApproval(ids = listOf(payoutId)))
}
```

### Placeholder Methods

`CreditService.createCreditApplicationPlaceholder()` is a placeholder for a module planned later and throws `UnsupportedOperationException`.

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
- `getOrderPayments`
- `cancelOrder`
- `refundOrder`
- `refundAllOrder`
- `chargeOrder`
- `approveSubmerchantPayment` (deprecated), `disapproveSubmerchantPayment`, `updateSubmerchantPaymentItem`

### Card Installments

- `getCardInstallments`
- `getInstallmentTable`

### Order Terms

`createOrderTerm` and `deleteOrderTerm` take the order's ID (not its reference ID); term dates use `YYYY-MM-DD`. `refundOrderTerm` needs the order's reference ID and the term's ID.

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
- `createOrganizationUser`
- `createOrganizationUserToken`
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
- `recordSubmerchantPayoutEvent`
- `createMarketplaceSubmerchant`

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

### Tokenization

- `tokenizeCard`
- `listSavedCards`
- `deleteSavedCard`

### Marketplace (`api.marketplace`)

- Sellers: `listMarketplaceSubmerchants`, `getMarketplaceSubmerchant`, `updateMarketplaceSubmerchant`, `getMarketplaceSellerContracts`, `getMarketplaceSellerAccounts`, `runMarketplaceSellerAccountAction`
- Payouts: `listMarketplacePayouts`, `getMarketplacePayout`, `approveMarketplacePayouts`
- Policies: `list`/`get`/`create`/`update`/`delete` for `MarketplaceApprovalPolicy` and `MarketplaceReleasePolicy`, `getMarketplaceApprovalSettings`, `updateMarketplaceApprovalSettings`
- Payout operations: `runMarketplacePayoutOperation`, `getMarketplacePayoutOperation`, `reconcileMarketplacePayoutOperation`, `retryMarketplacePayoutOperation`

## Error Handling

The SDK throws:

- `ApiError` for non-2xx API responses, carrying `statusCode`, the body `status`, `code`, `messageText`, per-field `errors` and the `rawBody`. Most "not found" errors arrive as HTTP 400 and rejected state changes as HTTP 424. A successful status with an HTML body, which a deployment serving the panel UI returns for unknown routes, also throws `ApiError`.
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
