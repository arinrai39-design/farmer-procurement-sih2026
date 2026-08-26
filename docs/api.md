# API Documentation

Base URL: `http://localhost:8080/api`

## Authentication

`POST /auth/register`

```json
{
  "fullName": "Rahul Kumar",
  "mobile": "9876501001",
  "farmerId": "FARM1001",
  "address": "Gosainganj, Lucknow",
  "village": "Gosainganj",
  "district": "Lucknow",
  "state": "Uttar Pradesh",
  "password": "farmer123"
}
```

`POST /auth/login`

```json
{ "identifier": "9876501001", "password": "farmer123" }
```

## Booking

`GET /slots/available?centreId=1&date=2026-08-27`

`POST /slots/book`

```json
{ "farmerId": 1, "centreId": 1, "cropId": 1, "slotId": 1, "quantityKg": 2500 }
```

## Queue and procurement

`PUT /queue/next?centreId=1`

`PUT /procurements/{bookingId}/status`

```json
{ "status": "VERIFICATION" }
```

Valid statuses: `WAITING`, `CALLED`, `ARRIVED`, `VERIFICATION`, `PROCUREMENT`, `COMPLETED`, `SKIPPED`, `CANCELLED`.

## Payments

`PUT /payments/{bookingId}/status`

```json
{ "status": "PAID" }
```

Valid payment statuses: `PENDING`, `PROCESSING`, `PAID`.
