const API = "http://localhost:8080/api"

function authToken() {
  if (typeof AuthState !== "undefined" && AuthState.getToken) return AuthState.getToken()
  return sessionStorage.getItem("token") || localStorage.getItem("token") || ""
}

function authHeader() {
  return { "Authorization": `Bearer ${authToken()}` }
}

async function apiFetch(path) {
  const res = await fetch(`${API}${path}`, { headers: authHeader() })
  if (!res.ok) throw new Error(`${path} failed: ${res.status}`)
  return res.json()
}

function asUserArray(payload) {
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.content)) return payload.content
  return []
}

// ---- Renderers (can be overridden by dashboard.html) ----

function renderUsers(users) {
  const tbody = document.getElementById("usersTable")
  if (!tbody) return
  tbody.innerHTML = users.map(u => `
    <tr>
      <td class="text-muted">${u.id}</td>
      <td>${u.username}</td>
      <td>${u.email}</td>
      <td><span class="badge ${u.role === 'ROLE_ADMIN' ? 'bg-danger' : 'bg-secondary'}">${u.role}</span></td>
    </tr>
  `).join("")
}

function renderBuses(buses) {
  const tbody = document.getElementById("busesTable")
  if (!tbody) return
  tbody.innerHTML = buses.map(b => `
    <tr>
      <td class="text-muted">${b.id}</td>
      <td>${b.busNumber}</td>
      <td>${b.busType}</td>
      <td>${b.totalSeats}</td>
    </tr>
  `).join("")
}

function renderTrips(trips) {
  const tbody = document.getElementById("tripsTable")
  if (!tbody) return
  tbody.innerHTML = trips.map(t => `
    <tr>
      <td class="text-muted">${t.id}</td>
      <td>${t.route.origin} → ${t.route.destination}</td>
      <td>${t.bus.busNumber}</td>
      <td>—</td>
      <td>${Number(t.price).toLocaleString("vi-VN")} VND</td>
    </tr>
  `).join("")
}

function renderTickets(tickets) {
  const tbody = document.getElementById("ticketsTable")
  if (!tbody) return
  tbody.innerHTML = tickets.map(t => `
    <tr>
      <td class="text-muted">${t.id}</td>
      <td>${t.seat?.seatNumber || "—"}</td>
      <td>${t.user?.username || "—"}</td>
      <td>—</td>
      <td><span class="badge bg-secondary">${t.status}</span></td>
    </tr>
  `).join("")
}

function renderPayments(payments) {
  const tbody = document.getElementById("paymentsTable")
  if (!tbody) return
  tbody.innerHTML = payments.map(p => `
    <tr>
      <td class="text-muted">${p.id}</td>
      <td>#${p.ticketId}</td>
      <td>${p.paymentMethod}</td>
      <td>${p.status}</td>
      <td>${Number(p.amount).toLocaleString("vi-VN")} VND</td>
      <td>—</td>
    </tr>
  `).join("")
}

function renderBookingsChart(data) {
  const ctx = document.getElementById("bookingsChart")
  if (!ctx) return
  new Chart(ctx, {
    type: "line",
    data: {
      labels: data.map(d => d.date),
      datasets: [{
        label: "Vé đặt",
        data: data.map(d => d.count),
        fill: true,
        borderColor: "#1e6fff",
        backgroundColor: "rgba(30,111,255,0.08)",
        tension: 0.4,
        pointBackgroundColor: "#1e6fff",
        pointRadius: 4
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { display: false } },
        y: { beginAtZero: true, ticks: { stepSize: 1 } }
      }
    }
  })
}

function renderTripsChart(data) {
  const ctx = document.getElementById("tripsChart")
  if (!ctx) return
  const colors = ["#1e6fff","#0f4edb","#ff6b00","#2ecc71","#e74c3c","#f1c40f","#9b59b6"]
  new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: data.map(d => `${d.origin} → ${d.destination}`),
      datasets: [{
        data: data.map(d => d.tripCount),
        backgroundColor: colors.slice(0, data.length),
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: "bottom", labels: { boxWidth: 12, font: { size: 11 } } }
      }
    }
  })
}

// ---- Main load ----

async function loadDashboard() {
  const token =
    typeof AuthState !== "undefined" && AuthState.getToken ? AuthState.getToken() : authToken()
  const role =
    typeof AuthState !== "undefined" && AuthState.getRole
      ? AuthState.getRole()
      : sessionStorage.getItem("role") || localStorage.getItem("role") || ""

  if (!token || role !== "ROLE_ADMIN") {
    alert("Bạn không có quyền truy cập trang này.")
    window.location = "/login"
    return
  }

  try {
    const [users, userCountRes, buses, trips, tickets, payments, revenueData, bookings30, tripStats] =
      await Promise.allSettled([
        apiFetch("/admin/accounts"),
        apiFetch("/admin/users/count"),
        apiFetch("/buses"),
        apiFetch("/trips"),
        apiFetch("/admin/tickets"),
        apiFetch("/payments"),
        apiFetch("/admin/revenue/today"),
        apiFetch("/admin/bookings/last30days"),
        apiFetch("/admin/trips/statistics")
      ])

    const ok = p => p.status === "fulfilled" ? p.value : []

    let usersData = asUserArray(ok(users))
    const userKpi = userCountRes.status === "fulfilled" && userCountRes.value != null && typeof userCountRes.value.total === "number"
      ? userCountRes.value.total
      : null
    if (users.status === "rejected" || (userKpi != null && userKpi > 0 && usersData.length === 0)) {
      for (const p of ["/admin/users", "/users"]) {
        try {
          const r = await fetch(`${API}${p}`, { headers: authHeader() })
          if (!r.ok) continue
          const next = asUserArray(await r.json())
          if (next.length > 0) {
            usersData = next
            break
          }
        } catch (e) { console.warn("Fallback GET " + p + ":", e) }
      }
    }
    const busesData    = ok(buses)
    const tripsData    = ok(trips)
    const ticketsData  = ok(tickets)
    const paymentsData = ok(payments)

    // KPI
    const setEl = (id, val) => { const el = document.getElementById(id); if (el) el.innerText = val }
    setEl("userCount",   userKpi != null ? userKpi : usersData.length)
    setEl("busCount",    busesData.length)
    setEl("tripCount",   tripsData.length)

    const rev = revenueData.status === "fulfilled" ? revenueData.value : null
    setEl("revenueToday", rev?.revenue != null
      ? Number(rev.revenue).toLocaleString("vi-VN") + " VND"
      : "—")

    // Tables
    renderUsers(usersData)
    renderBuses(busesData)
    renderTrips(tripsData)
    renderTickets(ticketsData)
    renderPayments(paymentsData)

    // Charts
    if (bookings30.status === "fulfilled") renderBookingsChart(bookings30.value)
    if (tripStats.status === "fulfilled")  renderTripsChart(tripStats.value)

  } catch (err) {
    console.error("Dashboard load error:", err)
    const area = document.getElementById("contentArea")
    if (area) area.innerHTML = `<div class="alert alert-danger">Không tải được dữ liệu. Hãy kiểm tra backend đã chạy chưa.</div>`
  }
}
