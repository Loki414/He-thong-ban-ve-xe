const API = "http://localhost:8080/api"

async function confirmBooking() {
  const seatId = localStorage.getItem("seatId")
  const userId = localStorage.getItem("userId")
  const token  = localStorage.getItem("token")

  const name  = document.getElementById("name")?.value?.trim() || ""
  const phone = document.getElementById("phone")?.value?.trim() || ""
  const email = document.getElementById("email")?.value?.trim() || ""

  if (!seatId || !userId || !token) {
    alert("Please login and select a seat again")
    return
  }

  // Save passenger info locally (backend does not store it in this demo)
  localStorage.setItem("passengerName",  name)
  localStorage.setItem("passengerPhone", phone)
  localStorage.setItem("passengerEmail", email)

  const res = await fetch(`${API}/tickets`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({
      seatId: Number(seatId),
      userId: Number(userId)
    })
  })

  const text = await res.text()
  let payload = null
  try { payload = text ? JSON.parse(text) : null } catch (e) {}

  if (!res.ok) {
    alert((payload && payload.message) || text || "Booking failed")
    return
  }

  const ticket = payload
  localStorage.setItem("ticketId", ticket.id)
  if (ticket?.seat?.seatNumber) {
    localStorage.setItem("seatNumber", ticket.seat.seatNumber)
  }
  window.location = "/payment"
}
