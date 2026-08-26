Deploy Link 
frontened :http://localhost:3000
backened :http://localhost:8080



# Farmer Procurement Scheduling and Queue Management System

College-level SIH 2026 prototype for PS ID `SIH26032`.

## What is included

- Next.js farmer/admin frontend in `frontend/`
- Spring Boot REST API in `backend/`
- MySQL-compatible schema in `database/schema.sql`
- Demo seed data loaded automatically on backend startup
- Farmer registration/login, slot booking, token generation, live queue, procurement status, payment status, notifications, and admin analytics

## Demo credentials

- Farmer: `9876501001` / `farmer123`
- Farmer ID login also works: `FARM1001` / `farmer123`
- Admin: `admin` / `admin123`

## Run backend

```bash
cd backend
mvn spring-boot:run
```

By default the backend uses in-memory H2 so the prototype runs immediately.

For MySQL:

```bash
export DB_URL='jdbc:mysql://localhost:3306/procurement_db'
export DB_USERNAME='root'
export DB_PASSWORD='your_password'
export DB_DRIVER='com.mysql.cj.jdbc.Driver'
mvn spring-boot:run
```

Create the database first:

```sql
CREATE DATABASE procurement_db;
```

## Run frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

## Main API endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/centres`
- `GET /api/crops`
- `GET /api/slots/available?centreId=1&date=2026-08-27`
- `POST /api/slots/book`
- `GET /api/farmers/{farmerId}/bookings`
- `GET /api/queue/{bookingId}`
- `GET /api/admin/queue?centreId=1`
- `PUT /api/queue/next?centreId=1`
- `PUT /api/procurements/{id}/status`
- `PUT /api/payments/{id}/status`
- `GET /api/notifications/{userId}`
- `GET /api/admin/dashboard?centreId=1`

## SIH demo flow

1. Open the landing page and explain the problem: long waits and low visibility at procurement centres.
2. Login as farmer `9876501001`.
3. Open the farmer dashboard to show token, queue position, timeline, and notifications.
4. Book a new slot with another registered or newly created farmer.
5. Login as admin.
6. Click `Call Next Farmer` to move the live queue.
7. Move a token through `Arrived`, `Verify`, `Procure`, and `Complete`.
8. Mark payment as `Paid`.
9. Return to farmer dashboard and show updated status, amount, payment badge, and notifications.
