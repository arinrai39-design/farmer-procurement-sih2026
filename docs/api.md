# API Documentation

Base URL: `http://localhost:8080/api`

All non-auth, non-public lookup endpoints require `Authorization: Bearer <jwt>`.

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

The response includes a signed JWT access token, `userId`, `farmerId`, display name, and role.

## Public Lookups

- `GET /centres`
- `GET /centres/{id}`
- `GET /crops`
- `GET /slots/available?centreId=1&date=<today>`

Slot availability is calculated from live database bookings, excludes cancelled/skipped/no-show bookings, and includes congestion, ETA, and a recommended slot flag.

## Farmer Booking

- `GET /bookings/my`
- `GET /farmers/{farmerId}/bookings`
- `POST /slots/book`
- `POST /bookings`
- `PUT /bookings/{id}/cancel`
- `PUT /bookings/{id}/reschedule`

```json
{ "farmerId": 1, "centreId": 1, "cropId": 1, "slotId": 1, "quantityKg": 2500 }
```

Server-side validation checks farmer ownership, centre/crop/slot existence, slot-centre match, slot open state, capacity, duplicate active bookings, and positive quantity.

## Officer Queue And Procurement

- `GET /admin/queue?centreId=1`
- `PUT /queue/next?centreId=1`
- `PUT /officer/bookings/{id}/call`
- `PUT /officer/bookings/{id}/arrive`
- `PUT /officer/bookings/{id}/verification`
- `PUT /officer/bookings/{id}/procurement`
- `PUT /officer/bookings/{id}/complete`

```json
{ "weighedQuantityKg": 2380, "acceptedQuantityKg": 2380 }
```

Valid queue state flow is enforced by `StateTransitionService`:
`WAITING -> CALLED -> ARRIVED -> VERIFICATION -> PROCUREMENT -> COMPLETED -> PAYMENT_PROCESSING -> PAID`.

Allowed alternates are cancellation, skip, and no-show from early queue states.

## Payments

`PUT /payments/{bookingId}/status`

```json
{ "status": "PAID" }
```

Valid payment statuses: `PENDING`, `PROCESSING`, `PAID`, `FAILED`.

Payment amount is calculated server-side from accepted quantity and crop rate.

## Notifications, Analytics, Audit

- `GET /notifications/{userId}`
- `GET /admin/dashboard?centreId=1`
- `GET /admin/audit`

Farmer notification access is owner-restricted. Admin dashboard metrics come from database data for the selected centre and current business date.
