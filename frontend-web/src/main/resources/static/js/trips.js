const API = "http://localhost:8080/api"

// ── Parse departure time (Jackson: ISO string, hoặc mảng [y,m,d,h,m], hoặc object Java time) ──
function parseDep(raw) {
  if (raw == null || raw === "") return null
  if (typeof raw === "string") {
    const d = new Date(raw)
    return isNaN(d.getTime()) ? null : d
  }
  if (Array.isArray(raw)) {
    const d = new Date(raw[0], (raw[1] || 1) - 1, raw[2] || 1, raw[3] || 0, raw[4] || 0, raw[5] || 0)
    return isNaN(d.getTime()) ? null : d
  }
  if (typeof raw === "object") {
    if (raw.year != null && raw.monthValue != null)
      return new Date(raw.year, raw.monthValue - 1, raw.dayOfMonth, raw.hour || 0, raw.minute || 0, raw.second || 0)
    if (raw.year != null && raw.month != null)
      return new Date(raw.year, raw.month - 1, raw.day || 1, raw.hour || 0, raw.minute || 0, raw.second || 0)
  }
  const d = new Date(raw)
  return isNaN(d.getTime()) ? null : d
}

function formatDateTime(value) {
  const d = parseDep(value)
  if (!d || isNaN(d)) return "—"
  return d.toLocaleString("vi-VN", {
    day: "2-digit", month: "2-digit", year: "numeric",
    hour: "2-digit", minute: "2-digit"
  })
}

// ── Trip status helpers ────────────────────────────────────────────────────
function getTripStatus(departureTime) {
  const dep    = parseDep(departureTime)
  if (!dep) return "unknown"
  const diffH  = (dep - Date.now()) / 3_600_000
  if (Number.isNaN(diffH)) return "unknown"
  if (diffH < 0)   return "departed"
  if (diffH < 1)   return "imminent"   // < 1h
  if (diffH < 6)   return "today"      // 1–6h
  return "scheduled"
}

/** Giong backend: origin/destination la chuoi con (khong phan biet hoa thuong). Ca hai rong = lay het. */
function tripMatchesSearch(trip, origin, destination) {
  const o = (origin || "").trim()
  const d = (destination || "").trim()
  const ro = (trip.route && trip.route.origin) || ""
  const rd = (trip.route && trip.route.destination) || ""
  if (!o && !d) return true
  if (o && !ro.toLowerCase().includes(o.toLowerCase())) return false
  if (d && !rd.toLowerCase().includes(d.toLowerCase())) return false
  return true
}

function statusBadgeHTML(status) {
  switch (status) {
    case "departed":  return '<span class="trip-status-badge departed">🔴 Đã khởi hành</span>'
    case "imminent":  return '<span class="trip-status-badge imminent">🟠 Sắp khởi hành</span>'
    case "today":     return '<span class="trip-status-badge today">🟡 Hôm nay</span>'
    default:          return '<span class="trip-status-badge scheduled">🟢 Còn chỗ</span>'
  }
}

// ── Countdown label for trips within 3 hours ─────────────────────────────
function countdownLabel(departureTime) {
  const dep   = parseDep(departureTime)
  if (!dep) return ""
  const diff  = dep - Date.now()
  if (diff <= 0) return ""
  const h     = Math.floor(diff / 3_600_000)
  const m     = Math.floor((diff % 3_600_000) / 60_000)
  if (h >= 3)  return ""
  if (h > 0)   return `⏱ Còn <strong>${h}h${m < 10 ? "0" + m : m}p</strong>`
  return `⏱ Còn <strong>${m} phút</strong>`
}

// ── Seat availability bar ─────────────────────────────────────────────────
function seatBarHTML(available, total) {
  if (total === 0) return '<span class="text-muted small">— ghế</span>'
  const pct  = Math.round((available / total) * 100)
  const color = available === 0 ? "#ef4444"
              : available <= 5  ? "#f97316"
              : available <= 10 ? "#eab308"
              : "#22c55e"
  return `
    <div class="seat-bar-wrap">
      <div class="seat-bar-track">
        <div class="seat-bar-fill" style="width:${pct}%;background:${color}"></div>
      </div>
      <span class="seat-bar-label" style="color:${color}">
        ${available === 0 ? "Hết chỗ" : `Còn <strong>${available}</strong>/${total} ghế`}
      </span>
    </div>`
}

// ── Save selection & go to seats page ────────────────────────────────────
function saveTripSelection(trip) {
  localStorage.setItem("tripId",            trip.id)
  localStorage.setItem("tripPrice",         trip.price)
  localStorage.setItem("tripOrigin",        trip.route.origin)
  localStorage.setItem("tripDestination",   trip.route.destination)
  localStorage.setItem("tripDepartureTime", JSON.stringify(trip.departureTime))
  localStorage.setItem("tripBusNumber",     trip.bus.busNumber)
  localStorage.setItem("tripBusType",       trip.bus.busType)
  window.location = `/seats?trip=${trip.id}`
}

// ── Current filter ("all" | "upcoming" | "today" | "departed") ───────────
// Mặc định "all" để vẫn thấy chuyến đã khởi hành; có thể chọn "Sắp khởi hành" để chỉ còn chuyến sắp tới
let _currentFilter = "all"
let _lastTrips     = []
let _refreshTimer  = null

function setFilter(f) {
  _currentFilter = f
  document.querySelectorAll(".filter-tab").forEach(el => {
    el.classList.toggle("active", el.dataset.filter === f)
  })
  renderTripCards(_lastTrips)
}

// ── Main load ─────────────────────────────────────────────────────────────
async function loadTrips(silent = false) {
  const origin      = localStorage.getItem("origin")      || ""
  const destination = localStorage.getItem("destination") || ""
  const date        = localStorage.getItem("date")        || ""

  // Update search info label
  const info = document.getElementById("tripSearchInfo")
  if (info) {
    const parts = []
    if (origin || destination) parts.push(`${origin || "?"} → ${destination || "?"}`)
    if (date)                  parts.push(`Ngày: ${date}`)
    info.innerText = parts.join(" · ") || "Tất cả chuyến xe"
  }

  const div = document.getElementById("tripList")

  if (!silent) {
    div.innerHTML = `
      <div class="text-center py-5">
        <div class="spinner-border text-primary mb-3" role="status"></div>
        <p class="text-muted small">Đang tải danh sách chuyến...</p>
      </div>`
  }

  try {
    // Lay tat ca chuyen nhu admin (GET /api/trips), loc tren trinh duyet -> khong lech /search vs du lieu that
    const res = await fetch(`${API}/trips`)
    if (!res.ok) {
      div.innerHTML = `<div class="alert alert-danger">Không tải được danh sách chuyến. Kiểm tra backend.</div>`
      return
    }

    let trips = await res.json()
    if (!Array.isArray(trips)) trips = []

    trips = trips.filter(t => tripMatchesSearch(t, origin, destination))

    // Filter by date on client if date chosen
    if (date) {
      trips = trips.filter(t => {
        const dep = parseDep(t.departureTime)
        return dep && dep.toISOString().startsWith(date)
      })
    }

    // Sort: upcoming by time ASC, departed at bottom by time DESC
    trips.sort((a, b) => {
      const da = parseDep(a.departureTime), db = parseDep(b.departureTime)
      const now = Date.now()
      const aDep = da < now, bDep = db < now
      if (aDep !== bDep) return aDep ? 1 : -1   // departed goes last
      return aDep ? db - da : da - db             // upcoming: ASC, departed: DESC
    })

    _lastTrips = trips
    renderTripCards(trips)
    updateRefreshIndicator()

    // Auto-refresh every 60s
    clearInterval(_refreshTimer)
    _refreshTimer = setInterval(() => loadTrips(true), 60_000)

  } catch (err) {
    div.innerHTML = `<div class="alert alert-danger">Lỗi kết nối server. Kiểm tra backend đã chạy chưa.</div>`
  }
}

// ── Render cards with current filter ─────────────────────────────────────
function renderTripCards(trips) {
  const div = document.getElementById("tripList")
  if (!div) return

  // Apply filter
  let filtered = trips
  if (_currentFilter === "upcoming") filtered = trips.filter(t => getTripStatus(t.departureTime) !== "departed")
  if (_currentFilter === "today")    filtered = trips.filter(t => ["imminent","today"].includes(getTripStatus(t.departureTime)))
  if (_currentFilter === "departed") filtered = trips.filter(t => getTripStatus(t.departureTime) === "departed")

  // Count badges
  const upcoming  = trips.filter(t => getTripStatus(t.departureTime) !== "departed").length
  const todayOnly = trips.filter(t => ["imminent","today"].includes(getTripStatus(t.departureTime))).length
  const departed  = trips.filter(t => getTripStatus(t.departureTime) === "departed").length
  document.querySelectorAll(".filter-tab").forEach(el => {
    const f = el.dataset.filter
    if (f === "all")      el.querySelector(".tab-count").innerText = trips.length
    if (f === "upcoming") el.querySelector(".tab-count").innerText = upcoming
    if (f === "today")    el.querySelector(".tab-count").innerText = todayOnly
    if (f === "departed") el.querySelector(".tab-count").innerText = departed
  })

  const countEl = document.getElementById("tripCount")
  if (countEl) {
    countEl.style.display = "inline"
    countEl.innerText = `${filtered.length} chuyến`
  }

  if (!filtered.length) {
    div.innerHTML = `
      <div class="text-center py-5">
        <div style="font-size:3rem">🚌</div>
        <h6 class="mt-3 text-muted">Không có chuyến nào</h6>
        <p class="text-muted small">Thử bộ lọc khác hoặc thay đổi điểm đi / đến.</p>
      </div>`
    return
  }

  div.innerHTML = ""
  filtered.forEach((trip, idx) => {
    const status      = getTripStatus(trip.departureTime)
    const isDeparted  = status === "departed"
    const countdown   = countdownLabel(trip.departureTime)
    const available   = trip.availableSeats ?? "—"
    const total       = trip.totalSeats     ?? "—"
    const noSeats     = available === 0

    const card = document.createElement("div")
    card.className = `trip-card${isDeparted ? " departed-card" : ""}${status === "imminent" ? " imminent-card" : ""}`
    card.dataset.tripIdx = idx

    card.innerHTML = `
      <div class="row align-items-center g-3">
        <!-- Route & info -->
        <div class="col-md-4">
          <div class="d-flex align-items-center gap-2 mb-1">
            ${statusBadgeHTML(status)}
            ${countdown ? `<span class="countdown-label">${countdown}</span>` : ""}
          </div>
          <div class="route-label">${trip.route.origin} <span class="text-muted mx-1">→</span> ${trip.route.destination}</div>
          <div class="meta-text mt-1">
            <span class="badge-bus me-2">🚌 ${trip.bus.busNumber}</span>
            <span class="badge-type">${trip.bus.busType}</span>
          </div>
        </div>

        <!-- Time -->
        <div class="col-md-3">
          <div class="small text-muted mb-1">Khởi hành</div>
          <div class="fw-bold" style="font-size:.95rem">⏰ ${formatDateTime(trip.departureTime)}</div>
          ${trip.route.distance ? `<div class="meta-text mt-1">📍 ${trip.route.distance} km</div>` : ""}
        </div>

        <!-- Seats -->
        <div class="col-md-2">
          <div class="small text-muted mb-1">Chỗ trống</div>
          ${seatBarHTML(available, total)}
        </div>

        <!-- Price + action -->
        <div class="col-md-3 text-md-end">
          <div class="price mb-2">${Number(trip.price).toLocaleString("vi-VN")} <span class="small fw-normal text-muted">đ</span></div>
          ${isDeparted
            ? `<button class="btn btn-secondary fw-semibold w-100" disabled>Đã khởi hành</button>`
            : noSeats
            ? `<button class="btn btn-outline-danger fw-semibold w-100" disabled>Hết chỗ</button>`
            : `<button class="btn btn-warning fw-semibold w-100 btn-select" data-idx="${idx}">Chọn ghế →</button>`
          }
        </div>
      </div>`

    div.appendChild(card)
  })

  // Bind select buttons safely
  div.querySelectorAll(".btn-select[data-idx]").forEach(btn => {
    btn.onclick = () => saveTripSelection(filtered[Number(btn.dataset.idx)])
  })
}

// ── Last-updated indicator ────────────────────────────────────────────────
function updateRefreshIndicator() {
  const el = document.getElementById("lastUpdated")
  if (!el) return
  const now = new Date()
  el.innerText = `Cập nhật lúc ${now.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}`
}

// ── Initial call ──────────────────────────────────────────────────────────
loadTrips()
