const API = "http://localhost:8080/api"

function parsePayload(text) {
  try { return text ? JSON.parse(text) : null } catch (e) { return null }
}

// Supports both "token" and "accessToken" field names (some backends use either)
function extractToken(payload) {
  if (!payload) return null
  return payload.token || payload.accessToken || payload.jwtToken || null
}

async function login() {
  const identity = document.getElementById("username").value.trim()
  const password = document.getElementById("password").value

  if (!identity || !password) {
    alert("Vui lòng nhập tài khoản và mật khẩu")
    return
  }

  let res, text, payload
  try {
    res = await fetch(`${API}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: identity,
        email: identity,
        password
      })
    })
    text = await res.text()
    payload = parsePayload(text)
  } catch (networkErr) {
    alert("Không kết nối được server. Hãy kiểm tra backend đã chạy chưa (localhost:8080).")
    return
  }

  if (!res.ok) {
    alert((payload && payload.message) || text || "Đăng nhập thất bại")
    return
  }

  const token = extractToken(payload)

  if (!token) {
    // Backend may return HTTP 200 with an error message (non-standard but possible)
    const errMsg = (payload && payload.message) || text || "Đăng nhập thất bại"
    alert(errMsg)
    return
  }

  localStorage.setItem("token",    token)
  localStorage.setItem("userId",   payload.userId   ?? payload.id   ?? "")
  localStorage.setItem("username", payload.username ?? payload.name ?? identity)
  localStorage.setItem("role",     payload.role     ?? payload.userRole ?? "ROLE_USER")

  // Signal / to show welcome toast
  sessionStorage.setItem("justLoggedIn", "1")

  const redirectTo = new URLSearchParams(window.location.search).get("redirect")
  window.location = redirectTo || "/"
}

async function register() {
  const username = document.getElementById("username").value.trim()
  const email    = document.getElementById("email").value.trim()
  const password = document.getElementById("password").value

  if (!username || !email || !password) {
    alert("Vui lòng điền đầy đủ thông tin")
    return
  }

  let res, text, payload
  try {
    res = await fetch(`${API}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, email, password })
    })
    text = await res.text()
    payload = parsePayload(text)
  } catch (networkErr) {
    alert("Không kết nối được server. Hãy kiểm tra backend đã chạy chưa (localhost:8080).")
    return
  }

  if (!res.ok) {
    alert((payload && payload.message) || text || "Đăng ký thất bại")
    return
  }

  // If register also returns a token, auto-login directly
  const token = extractToken(payload)
  if (token && payload) {
    localStorage.setItem("token",    token)
    localStorage.setItem("userId",   payload.userId   ?? payload.id   ?? "")
    localStorage.setItem("username", payload.username ?? username)
    localStorage.setItem("role",     payload.role     ?? "ROLE_USER")
    sessionStorage.setItem("justLoggedIn", "1")
    alert("Đăng ký thành công!")
    window.location = "/"
    return
  }

  alert("Đăng ký thành công! Vui lòng đăng nhập.")
  window.location = "/login"
}
