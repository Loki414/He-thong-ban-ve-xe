const API = "http://localhost:8080/api"

let selectedSeat = null
let lockExpiresAt = null
let countdownInterval = null

function getTripId() {
  return new URLSearchParams(window.location.search).get("trip")
}

async function loadSeats() {
  const tripId = getTripId()
  const div = document.getElementById("seatList")
  if (!tripId) {
    div.innerHTML = `<div class="alert alert-warning small">Không tìm thấy ID chuyến.</div>`
    return
  }

  try {
    const res = await fetch(`${API}/seats/trip/${tripId}`)
    if (!res.ok) {
      const errText = await res.text().catch(() => "")
      div.innerHTML = `<div class="alert alert-danger small">Không tải được danh sách ghế (HTTP ${res.status}). Kiểm tra backend đã chạy và thử lại. ${errText ? "<br><small>" + errText.slice(0, 200) + "</small>" : ""}</div>`
      return
    }

    const seats = await res.json()
    if (!Array.isArray(seats) || seats.length === 0) {
      div.innerHTML = `<div class="alert alert-warning small">Chuyến này chưa có ghế trong hệ thống (admin tạo chuyến qua Dashboard sẽ tự sinh ghế). Hoặc chuyến được thêm thủ công vào DB mà chưa có bản ghi trong bảng <code>seats</code>.</div>`
      return
    }
    div.innerHTML = ""
    let row = ""

    seats.forEach((seat, i) => {
      const cls = seat.booked ? "seat booked" : "seat available"
      row += `
        <div class="${cls}" id="seat-${seat.id}"
             onclick="selectSeat(${seat.id}, ${seat.booked})">
          ${seat.seatNumber}
        </div>
      `
      // 2 ghế, 1 aisle, 2 ghế layout
      const pos = i % 4
      if (pos === 1) row += `<div class="aisle"></div>`
      if (pos === 3) {
        div.innerHTML += `<div class="row-seat">${row}</div>`
        row = ""
      }
    })
    if (row) {
      div.innerHTML += `<div class="row-seat">${row}</div>`
    }

  } catch (err) {
    div.innerHTML = `<div class="alert alert-danger small">Lỗi kết nối server.</div>`
  }
}

async function selectSeat(id, booked) {
  if (booked) {
    alert("Ghế này đã được đặt hoặc đang bị giữ.")
    return
  }

  const token = (typeof AuthState !== "undefined" && AuthState.getToken)
    ? AuthState.getToken()
    : (sessionStorage.getItem("token") || localStorage.getItem("token") || "")
  const userId = localStorage.getItem("userId")

  if (!token || !userId) {
    document.getElementById("loginWarning") && (document.getElementById("loginWarning").style.display = "block")
    return
  }

  // Deselect previous
  if (selectedSeat) {
    const prev = document.getElementById("seat-" + selectedSeat)
    if (prev) {
      prev.classList.remove("selected")
      prev.classList.add("available")
    }
  }

  // Optimistic UI
  const seatEl = document.getElementById("seat-" + id)
  if (seatEl) seatEl.classList.add("selected")

  try {
    const res = await fetch(`${API}/seats/lock`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ seatId: Number(id), userId: Number(userId) })
    })

    const text = await res.text()
    let payload = null
    try { payload = text ? JSON.parse(text) : null } catch (e) {}

    if (!res.ok) {
      if (seatEl) {
        seatEl.classList.remove("selected")
        seatEl.classList.add("available")
      }
      alert((payload && payload.message) || text || "Không thể giữ ghế.")
      return
    }

    selectedSeat = id
    localStorage.setItem("seatId", id)
    if (seatEl) {
      localStorage.setItem("seatNumber", seatEl.innerText.trim())
    }

    if (payload && payload.expiresAt) {
      localStorage.setItem("seatLockExpiresAt", JSON.stringify(payload.expiresAt))
      lockExpiresAt = payload.expiresAt
      startCountdown(lockExpiresAt)
    }

    // Update summary
    const sumSeat = document.getElementById("sumSeat")
    if (sumSeat) sumSeat.innerText = seatEl ? seatEl.innerText.trim() : id

  } catch (err) {
    if (seatEl) { seatEl.classList.remove("selected"); seatEl.classList.add("available") }
    alert("Lỗi kết nối khi giữ ghế.")
  }
}

function startCountdown(expiresAt) {
  const el = document.getElementById("lockCountdown")
  const timer = document.getElementById("countdownTimer")
  if (!el || !timer) return

  el.classList.add("visible")

  if (countdownInterval) clearInterval(countdownInterval)

  function getExpireMs() {
    if (!expiresAt) return 0
    const raw = Array.isArray(expiresAt)
      ? new Date(expiresAt[0], expiresAt[1]-1, expiresAt[2], expiresAt[3]||0, expiresAt[4]||0, expiresAt[5]||0)
      : new Date(expiresAt)
    return raw.getTime() - Date.now()
  }

  countdownInterval = setInterval(() => {
    const remaining = getExpireMs()
    if (remaining <= 0) {
      clearInterval(countdownInterval)
      el.classList.remove("visible")
      timer.innerText = "0:00"
      // Reset selected seat
      if (selectedSeat) {
        const el2 = document.getElementById("seat-" + selectedSeat)
        if (el2) { el2.classList.remove("selected"); el2.classList.add("available") }
        selectedSeat = null
      }
      const sumSeat = document.getElementById("sumSeat")
      if (sumSeat) sumSeat.innerText = "Chưa chọn (hết giờ)"
      return
    }
    const m = Math.floor(remaining / 60000)
    const s = Math.floor((remaining % 60000) / 1000)
    timer.innerText = `${m}:${s.toString().padStart(2, "0")}`
  }, 500)
}

function goBooking() {
  const warn = document.getElementById("noSeatWarning")
  if (!selectedSeat) {
    if (warn) warn.style.display = "block"
    return
  }
  if (warn) warn.style.display = "none"
  window.location = "/booking"
}

loadSeats()
