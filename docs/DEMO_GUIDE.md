# Cuponomia Demo Guide

This guide is the fastest way to evaluate Cuponomia from a browser using Swagger UI.

Published demo hub:

```text
https://cuponomia.maxsueleinstein.dev
```

## 1. Start The Stack

```bash
docker compose up --build
```

Wait until both services and both PostgreSQL containers are healthy.

## 2. Open Swagger

Management service:

- Root URL: `http://localhost:8081/`
- Swagger URL: `http://localhost:8081/swagger-ui.html`

Validation service:

- Root URL: `http://localhost:8082/`
- Swagger URL: `http://localhost:8082/swagger-ui.html`

The root URL of each service redirects to its Swagger UI, so reviewers can open the service base URL without memorizing the Swagger path.

## 3. Inspect Seed Coupons

Open the management Swagger and run:

```http
GET /api/v1/coupons
```

Useful seeded coupons:

| Code | Scenario |
| --- | --- |
| `MAX50` | Valid recruiter demo coupon, 50% discount, minimum order `16000.00`. |
| `BEMVINDO25` | Fixed discount coupon. |
| `EXPIRADO20` | Expired coupon scenario. |
| `INATIVO30` | Inactive coupon scenario. |

## 4. Create A Coupon

In the management Swagger, run:

```http
POST /api/v1/coupons
```

Use the request example already available in Swagger:

```json
{
  "code": "RECRUITER50",
  "description": "50% discount for a recruiter demo checkout.",
  "discountType": "PERCENTAGE",
  "discountValue": 50,
  "rules": {
    "minimumOrderValue": 16000.00,
    "expiresAt": "2027-12-31T23:59:59",
    "singleUsePerClient": true,
    "maxUsages": 10
  }
}
```

If you run this request more than once, change `code` to a new value because coupon codes are unique.

## 5. Apply A Coupon At Checkout

Open the validation Swagger and run:

```http
POST /api/v1/checkout/apply-coupon
```

Use the Swagger example:

```json
{
  "couponCode": "MAX50",
  "clientId": "recruiter-demo",
  "orderTotal": 16000.00
}
```

Expected result:

- `valid`: `true`
- `discountApplied`: `8000.00`
- `finalTotal`: `8000.00`

## 6. Try Failure Scenarios

Use the same checkout endpoint with these variations:

| Payload Change | Expected Result |
| --- | --- |
| `"orderTotal": 100.00` with `MAX50` | Invalid because the order total is below the minimum value. |
| `"couponCode": "EXPIRADO20"` | Invalid because the coupon is expired. |
| `"couponCode": "INATIVO30"` | Invalid because the coupon is inactive. |
| Reuse `MAX50` with the same `clientId` | Invalid if the coupon is configured as single-use and the first use was persisted. |

## 7. What To Discuss In An Interview

- Why the management service does not call the validation service directly.
- How eventual consistency affects a newly created coupon.
- Why each service owns its database.
- How coupon rules are modeled as isolated domain rules.
- What happens if checkout validation receives traffic while the management service is unavailable.
