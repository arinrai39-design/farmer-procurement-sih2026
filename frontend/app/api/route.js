export function GET() {
  return Response.json({
    name: "Farmer Procurement API",
    status: "running",
    endpoints: [
      "/api/auth/login",
      "/api/auth/register",
      "/api/centres",
      "/api/crops",
      "/api/slots/available?centreId=1&date=2026-08-27",
      "/api/farmers/1/bookings",
      "/api/admin/queue?centreId=1",
      "/api/admin/dashboard?centreId=1"
    ]
  });
}
