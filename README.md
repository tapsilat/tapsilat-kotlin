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
- `elements/`: Tapsilat Elements sessions for the native card sheet

Service split convention:

- `SharedApiClient`: reusable HTTP transport and error parsing
- `OrderService`: order and order-term APIs
- `OrganizationService`: organization settings and currencies
- `SubmerchantService`: submerchant/suborganization APIs
- `VposService`: VPOS and VPOS-submerchant APIs
- `SubscriptionService`: subscription APIs
- `ElementsService`: Tapsilat Elements session APIs
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

### Tapsilat Elements Sessions

Tapsilat Elements is the native Android and iOS card sheet. Your backend creates a session with its secret key and passes only the session's `clientSecret` to the app, which opens the sheet with it. Card data goes from the sheet to Tapsilat encrypted and never reaches your backend.

```kotlin
import com.tapsilat.ElementsSession
import com.tapsilat.ElementsSessionCreateRequest

val session = api.createElementsSession(
    ElementsSessionCreateRequest(
        purpose = ElementsSession.PURPOSE_PAY,
        orderReferenceId = orderReferenceId,
        saveCard = true,
    )
)
// Return session.id and session.clientSecret to your app. Do not log or store the secret.

// Later, read the outcome on your backend instead of trusting the app:
val outcome = api.getElementsSession(sessionId)
if (outcome.status == ElementsSession.STATUS_SUCCEEDED) {
    // fulfil the order; outcome.card holds brand, last4 and expiry
}
```

A `pay` session needs `orderReferenceId`. A `tokenize` session saves a card for `userId`, with `mode` set to `ElementsSession.MODE_SECURE` (3D Secure), `MODE_HALF_SECURE` or `MODE_NON_SECURE`. `cancelElementsSession(id)` cancels a session that is still waiting for a card; a session that is confirming or has completed comes back unchanged.

Only `createElementsSession` returns the client secret. `ElementsSessionWithSecret.toString()` prints it as `[redacted]`, and JSON serialization keeps it, so you can still send it to the app. Status, purpose and card brand are plain strings, with the known values as `ElementsSession.STATUS_*`, `PURPOSE_*` and `ElementsCardSummary.BRAND_*`, so a value added later still decodes. Elements errors carry a stable `code` (for example `elements_not_enabled`, `session_not_found`) and `retryable` on `ApiError`.

### Webhooks

#### Verify the Signature

Tapsilat signs every callback it sends to your callback URLs, and to an order's `payment_success_url` and `payment_failure_url`, with the webhook secret from the panel's callback settings. The `Tapsilat-Signature` header looks like `t=1726480000,v1=5257a8…`: `v1` is the hex HMAC-SHA256 of the timestamp, a dot and the raw body.

`TapsilatWebhook.verify` checks it in constant time and rejects a timestamp more than 5 minutes from the server clock, so a captured callback cannot be replayed. Pass the raw body (a `String` or `ByteArray`), not re-encoded JSON. For example, in a Spring controller:

```kotlin
import com.tapsilat.TapsilatWebhook
import com.tapsilat.WebhookEvent

@PostMapping("/tapsilat/callback")
fun tapsilatCallback(
    @RequestBody body: String,
    @RequestHeader(TapsilatWebhook.SIGNATURE_HEADER, required = false) signature: String?,
): ResponseEntity<Void> {
    if (!TapsilatWebhook.verify(body, signature, "your_webhook_secret")) {
        return ResponseEntity.badRequest().build()
    }

    val event = WebhookEvent.parse(body)
    event.type           // "order_lite" (the default), "order", "order_payment_detail", ...
    event.referenceId    // the order's reference ID, whatever the callback type
    event.conversationId
    event.status         // e.g. "Paid", when the callback type carries a status

    if (event.isPaid) {
        // fulfil the order
    }
    return ResponseEntity.ok().build()
}
```

A different tolerance in seconds can be passed as `toleranceSeconds` (`0` disables the timestamp check). A bare hex HMAC of the body, with or without a `sha256=` prefix, is also accepted, as the other Tapsilat SDKs accept it.

#### Order Statuses

`OrderStatus` has helpers for the numeric status codes that callbacks carry (`TapsilatConstants.ORDER_STATUS_*`), the names `getOrderStatus` returns, and the groups most integrations need:

```kotlin
import com.tapsilat.OrderStatus

val status = api.getOrderStatus(referenceId).status // e.g. "Waiting for payment"

OrderStatus.isPaid(status)     // Paid, Completed, Sub merchant payment approved
OrderStatus.isFailed(status)   // Cancelled, Fraud, Rejected, Failure, Expired, Terminated
OrderStatus.isRefunded(status) // Refunded, Partially refunded
OrderStatus.code(status)       // 7; also accepts "7" or 7
OrderStatus.name(7)            // "Waiting for payment"
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

### Elements (`api.elements`)

- `createElementsSession`
- `getElementsSession`
- `cancelElementsSession`

### Marketplace (`api.marketplace`)

- Sellers: `listMarketplaceSubmerchants`, `getMarketplaceSubmerchant`, `updateMarketplaceSubmerchant`, `getMarketplaceSellerContracts`, `getMarketplaceSellerAccounts`, `runMarketplaceSellerAccountAction`
- Payouts: `listMarketplacePayouts`, `getMarketplacePayout`, `approveMarketplacePayouts`
- Policies: `list`/`get`/`create`/`update`/`delete` for `MarketplaceApprovalPolicy` and `MarketplaceReleasePolicy`, `getMarketplaceApprovalSettings`, `updateMarketplaceApprovalSettings`
- Payout operations: `runMarketplacePayoutOperation`, `getMarketplacePayoutOperation`, `reconcileMarketplacePayoutOperation`, `retryMarketplacePayoutOperation`

### Webhooks

- `TapsilatWebhook.verify`
- `WebhookEvent.parse`
- `OrderStatus.code`, `name`, `isPaid`, `isFailed`, `isRefunded`

## Error Handling

The SDK throws:

- `ApiError` for non-2xx API responses, carrying `statusCode`, the body `status`, `code`, `messageText`, per-field `errors`, `retryable` and the `rawBody`. Elements routes nest the error as `{"error": {"code": "...", "message": "...", "retryable": true}}`; `code`, `messageText` and `retryable` are read from that object. Most "not found" errors arrive as HTTP 400 and rejected state changes as HTTP 424. A successful status with an HTML body, which a deployment serving the panel UI returns for unknown routes, also throws `ApiError`.
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
