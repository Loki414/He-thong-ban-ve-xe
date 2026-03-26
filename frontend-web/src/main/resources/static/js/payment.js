const API = "http://localhost:8080/api"

function initPaymentSummary() {
  function formatDep(raw) {
    if (!raw) return "—"
    try {
      const v = JSON.parse(raw)
      const d = Array.isArray(v)
        ? new Date(v[0], v[1]-1, v[2], v[3]||0, v[4]||0)
        : new Date(v)
      return d.toLocaleString("vi-VN", {
        day: "2-digit", month: "2-digit", year: "numeric",
        hour: "2-digit", minute: "2-digit"
      })
    } catch { return raw }
  }

  const tripPrice = Number(localStorage.getItem("tripPrice") || 300000)
  const route = `${localStorage.getItem("tripOrigin") || ""} → ${localStorage.getItem("tripDestination") || ""}`
  const departureTime = localStorage.getItem("tripDepartureTime") || ""
  const seatNumber = localStorage.getItem("seatNumber") || "—"
  const bus = `${localStorage.getItem("tripBusNumber") || ""} · ${localStorage.getItem("tripBusType") || ""}`

  const set = (id, val) => { const el = document.getElementById(id); if (el) el.innerText = val }

  set("paymentAmount", tripPrice ? tripPrice.toLocaleString("vi-VN") + " VND" : "—")
  set("paymentRoute", route)
  set("paymentDeparture", formatDep(departureTime))
  set("paymentSeat", seatNumber)
  set("paymentBus", bus)
}

async function pay() {
  const ticketId = localStorage.getItem("ticketId")
  const token =
    typeof AuthState !== "undefined" && AuthState.getToken
      ? AuthState.getToken()
      : sessionStorage.getItem("token") || localStorage.getItem("token") || ""
  const amount = Number(localStorage.getItem("tripPrice") || 300000)

  // Allow override from page-level script
  const method = window._selectedMethod
    || document.querySelector('[name="method"]:checked')?.value
    || "CREDIT_CARD"

  if (!ticketId || !token) {
    alert("Missing ticket or token – please login and book again")
    return
  }

  const res = await fetch(`${API}/payments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({
      ticketId: Number(ticketId),
      amount,
      method
    })
  })

  const text = await res.text()
  let payload = null
  try { payload = text ? JSON.parse(text) : null } catch (e) {}

  if (!res.ok) {
    alert((payload && payload.message) || text || "Payment failed")
    return
  }

  localStorage.setItem("paymentStatus", payload?.status || "SUCCESS")
  localStorage.setItem("paymentMethod", method)
  window.location = "/success"
}

initPaymentSummary()
