const centres = [
  { id: 1, name: "Lucknow Procurement Centre", location: "Lucknow", address: "Mandi Parishad Road, Lucknow", workingHours: "09:00 AM - 05:00 PM", dailyCapacity: 100 },
  { id: 2, name: "Kanpur Procurement Centre", location: "Kanpur", address: "GT Road, Kanpur Nagar", workingHours: "09:00 AM - 05:00 PM", dailyCapacity: 90 },
  { id: 3, name: "Barabanki Procurement Centre", location: "Barabanki", address: "Nawabganj Mandi Campus", workingHours: "08:30 AM - 04:30 PM", dailyCapacity: 75 }
];

const crops = [
  { id: 1, name: "Wheat", ratePerKg: 21 },
  { id: 2, name: "Rice", ratePerKg: 23.5 },
  { id: 3, name: "Paddy", ratePerKg: 22 },
  { id: 4, name: "Maize", ratePerKg: 19.25 }
];

const slots = centres.flatMap((centre) => (
  ["09:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM", "12:00 PM - 01:00 PM"]
    .map((timeRange, index) => ({
      id: (centre.id - 1) * 4 + index + 1,
      centreId: centre.id,
      slotDate: "2026-08-27",
      timeRange,
      capacity: 10
    }))
));

const names = [
  ["Rahul Kumar", "9876501001", "FARM1001", "Gosainganj", "Lucknow"],
  ["Sunita Devi", "9876501002", "FARM1002", "Malihabad", "Lucknow"],
  ["Amit Singh", "9876501003", "FARM1003", "Bithoor", "Kanpur"],
  ["Pooja Yadav", "9876501004", "FARM1004", "Safedabad", "Barabanki"],
  ["Vikram Patel", "9876501005", "FARM1005", "Chinhat", "Lucknow"],
  ["Neha Verma", "9876501006", "FARM1006", "Rura", "Kanpur"],
  ["Arjun Pal", "9876501007", "FARM1007", "Dewa", "Barabanki"],
  ["Meera Chauhan", "9876501008", "FARM1008", "Mohanlalganj", "Lucknow"],
  ["Kiran Nishad", "9876501009", "FARM1009", "Akbarpur", "Kanpur"],
  ["Ramesh Gupta", "9876501010", "FARM1010", "Haidergarh", "Barabanki"],
  ["Sanjay Maurya", "9876501011", "FARM1011", "Kakori", "Lucknow"],
  ["Anita Shukla", "9876501012", "FARM1012", "Jajmau", "Kanpur"]
];

function seedState() {
  const users = [
    { id: 1, username: "admin", displayName: "Centre Officer", role: "ADMIN", password: "admin123" }
  ];
  const farmers = [];
  const bookings = [];
  const notifications = [];
  const statuses = ["COMPLETED", "COMPLETED", "PROCUREMENT", "VERIFICATION"];
  const payments = ["PAID", "PROCESSING", "PROCESSING", "PROCESSING"];

  names.forEach(([fullName, mobile, farmerCode, village, district], index) => {
    const user = { id: users.length + 1, username: mobile, displayName: fullName, role: "FARMER", password: "farmer123" };
    users.push(user);
    const farmer = {
      id: farmers.length + 1,
      userId: user.id,
      mobile,
      farmerCode,
      address: `${village}, ${district}`,
      village,
      district,
      state: "Uttar Pradesh"
    };
    farmers.push(farmer);
    const crop = crops[index % crops.length];
    const status = statuses[index] || "WAITING";
    const paymentStatus = payments[index] || "PENDING";
    bookings.push({
      id: bookings.length + 1,
      farmerId: farmer.id,
      centreId: centres[index % centres.length].id,
      cropId: crop.id,
      slotId: slots[index % slots.length].id,
      quantityKg: 1200 + index * 175,
      tokenNumber: `A${101 + index}`,
      status,
      paymentStatus,
      amount: status === "COMPLETED" ? Number((crop.ratePerKg * (1200 + index * 175)).toFixed(2)) : null,
      createdAt: Date.now() - (70 - index * 4) * 60 * 1000
    });
    notifications.push({
      id: notifications.length + 1,
      userId: user.id,
      message: `Your token is A${101 + index}. Track queue status from your dashboard.`,
      createdAt: new Date(Date.now() - 20 * 60 * 1000).toISOString()
    });
  });

  return { users, farmers, bookings, notifications };
}

const store = globalThis.__farmerProcurementStore || seedState();
globalThis.__farmerProcurementStore = store;

function json(data, status = 200) {
  return Response.json(data, { status });
}

function error(message, status = 400) {
  return json({ message }, status);
}

function parts(params) {
  return params.path || [];
}

function viewBooking(booking) {
  const farmer = store.farmers.find((item) => item.id === booking.farmerId);
  const user = store.users.find((item) => item.id === farmer.userId);
  const centre = centres.find((item) => item.id === booking.centreId);
  const crop = crops.find((item) => item.id === booking.cropId);
  const slot = slots.find((item) => item.id === booking.slotId);
  const active = store.bookings
    .filter((item) => item.centreId === booking.centreId && !["COMPLETED", "CANCELLED", "SKIPPED"].includes(item.status))
    .sort((a, b) => a.createdAt - b.createdAt);
  const index = Math.max(0, active.findIndex((item) => item.id === booking.id));
  const peopleAhead = Math.max(0, index);

  return {
    id: booking.id,
    farmerName: user.displayName,
    farmerCode: farmer.farmerCode,
    mobile: farmer.mobile,
    centre: centre.name,
    crop: crop.name,
    quantityKg: booking.quantityKg,
    date: slot.slotDate,
    slot: slot.timeRange,
    tokenNumber: booking.tokenNumber,
    status: booking.status,
    paymentStatus: booking.paymentStatus,
    amount: booking.amount,
    queuePosition: peopleAhead + 1,
    peopleAhead,
    estimatedWait: `${peopleAhead * 5 + 5} minutes`
  };
}

function notify(userId, message) {
  store.notifications.unshift({
    id: store.notifications.length + 1,
    userId,
    message,
    createdAt: new Date().toISOString()
  });
}

async function readBody(request) {
  return request.json().catch(() => ({}));
}

export async function GET(request, context) {
  const path = parts(await context.params);
  const search = new URL(request.url).searchParams;

  if (path[0] === "centres" && path.length === 1) return json(centres);
  if (path[0] === "crops") return json(crops);
  if (path[0] === "slots" && path[1] === "available") {
    const centreId = Number(search.get("centreId") || 1);
    const date = search.get("date");
    return json(slots.filter((slot) => slot.centreId === centreId && slot.slotDate === date).map((slot) => {
      const booked = store.bookings.filter((booking) => booking.slotId === slot.id).length;
      return { id: slot.id, date: slot.slotDate, timeRange: slot.timeRange, capacity: slot.capacity, booked, available: slot.capacity - booked, full: booked >= slot.capacity };
    }));
  }
  if (path[0] === "farmers" && path[2] === "bookings") {
    const farmerId = Number(path[1]);
    return json(store.bookings.filter((booking) => booking.farmerId === farmerId).sort((a, b) => b.createdAt - a.createdAt).map(viewBooking));
  }
  if (path[0] === "queue" && path[1]) {
    const booking = store.bookings.find((item) => item.id === Number(path[1]));
    return booking ? json(viewBooking(booking)) : error("Invalid booking.", 404);
  }
  if (path[0] === "admin" && path[1] === "queue") {
    const centreId = Number(search.get("centreId") || 1);
    return json(store.bookings.filter((booking) => booking.centreId === centreId).sort((a, b) => a.createdAt - b.createdAt).map(viewBooking));
  }
  if (path[0] === "admin" && path[1] === "dashboard") {
    const centreId = Number(search.get("centreId") || 1);
    const all = store.bookings.filter((booking) => booking.centreId === centreId);
    const pendingPayments = all
      .filter((booking) => booking.paymentStatus !== "PAID")
      .reduce((sum, booking) => sum + Number(booking.amount || 0), 0);
    return json({
      todaysFarmers: all.length,
      waiting: all.filter((booking) => ["WAITING", "CALLED"].includes(booking.status)).length,
      processing: all.filter((booking) => ["VERIFICATION", "PROCUREMENT"].includes(booking.status)).length,
      completed: all.filter((booking) => booking.status === "COMPLETED").length,
      pendingPayments,
      farmersPerDay: [31, 42, 47, 39, all.length],
      paymentStatus: {
        pending: all.filter((booking) => booking.paymentStatus === "PENDING").length,
        processing: all.filter((booking) => booking.paymentStatus === "PROCESSING").length,
        paid: all.filter((booking) => booking.paymentStatus === "PAID").length
      },
      cropWiseQuantity: Object.fromEntries(crops.map((crop) => [
        crop.name,
        all.filter((booking) => booking.cropId === crop.id).reduce((sum, booking) => sum + booking.quantityKg, 0)
      ]))
    });
  }
  if (path[0] === "notifications" && path[1]) {
    const userId = Number(path[1]);
    return json(store.notifications.filter((note) => note.userId === userId));
  }

  return error("Endpoint not found.", 404);
}

export async function POST(request, context) {
  const path = parts(await context.params);
  const body = await readBody(request);

  if (path[0] === "auth" && path[1] === "login") {
    const user = store.users.find((item) => item.username === body.identifier || store.farmers.find((farmer) => farmer.userId === item.id)?.farmerCode === body.identifier);
    if (!user || user.password !== body.password) return error("Invalid credentials.", 401);
    const farmer = store.farmers.find((item) => item.userId === user.id);
    return json({ userId: user.id, farmerId: farmer?.id || null, name: user.displayName, role: user.role, token: `demo-token-${user.id}` });
  }

  if (path[0] === "auth" && path[1] === "register") {
    if (store.users.some((user) => user.username === body.mobile) || store.farmers.some((farmer) => farmer.farmerCode === body.farmerId)) {
      return error("Farmer already exists.");
    }
    const user = { id: store.users.length + 1, username: body.mobile, displayName: body.fullName, role: "FARMER", password: body.password };
    store.users.push(user);
    const farmer = {
      id: store.farmers.length + 1,
      userId: user.id,
      mobile: body.mobile,
      farmerCode: body.farmerId,
      address: body.address,
      village: body.village,
      district: body.district,
      state: body.state
    };
    store.farmers.push(farmer);
    notify(user.id, "Your farmer profile has been created successfully.");
    return json({ userId: user.id, farmerId: farmer.id, name: user.displayName, role: user.role, token: `demo-token-${user.id}` });
  }

  if ((path[0] === "slots" && path[1] === "book") || path[0] === "bookings") {
    const active = store.bookings.find((booking) => booking.farmerId === Number(body.farmerId) && !["COMPLETED", "CANCELLED", "SKIPPED"].includes(booking.status));
    if (active) return error("You already have an active booking.");
    const slot = slots.find((item) => item.id === Number(body.slotId));
    const farmer = store.farmers.find((item) => item.id === Number(body.farmerId));
    if (!slot || !farmer) return error("Invalid booking details.");
    if (store.bookings.filter((booking) => booking.slotId === slot.id).length >= slot.capacity) return error("This slot is full. Please select another time.");
    const booking = {
      id: store.bookings.length + 1,
      farmerId: farmer.id,
      centreId: Number(body.centreId),
      cropId: Number(body.cropId),
      slotId: slot.id,
      quantityKg: Number(body.quantityKg),
      tokenNumber: `A${store.bookings.filter((item) => item.centreId === Number(body.centreId)).length + 101}`,
      status: "WAITING",
      paymentStatus: "PENDING",
      amount: null,
      createdAt: Date.now()
    };
    store.bookings.push(booking);
    notify(farmer.userId, `Your slot has been booked successfully. Token: ${booking.tokenNumber}.`);
    return json(viewBooking(booking));
  }

  return error("Endpoint not found.", 404);
}

export async function PUT(request, context) {
  const path = parts(await context.params);
  const body = await readBody(request);

  if (path[0] === "queue" && path[1] === "next") {
    const centreId = Number(new URL(request.url).searchParams.get("centreId") || 1);
    const next = store.bookings
      .filter((booking) => booking.centreId === centreId && booking.status === "WAITING")
      .sort((a, b) => a.createdAt - b.createdAt)[0];
    if (!next) return error("No waiting farmers in this queue.");
    next.status = "CALLED";
    notify(store.farmers.find((farmer) => farmer.id === next.farmerId).userId, "Your turn is now. Please proceed to the procurement counter.");
    return json(viewBooking(next));
  }

  if (path[0] === "procurements" && path[2] === "status") {
    const booking = store.bookings.find((item) => item.id === Number(path[1]));
    if (!booking) return error("Invalid token.", 404);
    booking.status = body.status;
    const crop = crops.find((item) => item.id === booking.cropId);
    if (body.status === "COMPLETED") {
      booking.amount = Number((crop.ratePerKg * booking.quantityKg).toFixed(2));
      booking.paymentStatus = "PROCESSING";
    }
    notify(store.farmers.find((farmer) => farmer.id === booking.farmerId).userId, `Your procurement status is now ${body.status}.`);
    return json(viewBooking(booking));
  }

  if (path[0] === "payments" && path[2] === "status") {
    const booking = store.bookings.find((item) => item.id === Number(path[1]));
    if (!booking) return error("Payment update failed: invalid booking.", 404);
    const crop = crops.find((item) => item.id === booking.cropId);
    booking.paymentStatus = body.status;
    booking.amount = booking.amount || Number((crop.ratePerKg * booking.quantityKg).toFixed(2));
    notify(store.farmers.find((farmer) => farmer.id === booking.farmerId).userId, `Your payment of Rs ${booking.amount} has been marked as ${body.status}.`);
    return json(viewBooking(booking));
  }

  return error("Endpoint not found.", 404);
}
