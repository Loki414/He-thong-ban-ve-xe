/**
 * Route Autocomplete Module
 * Loads routes from the backend and provides smart suggestions
 * for origin / destination inputs.
 */

const AC_API = "http://localhost:8080/api"

let _routes    = []   // [{ id, origin, destination, distance }]
let _origins   = []   // unique origin strings
let _allDests  = []   // unique destination strings (all)

/** Remove Vietnamese diacritics for fuzzy matching */
function acNorm(str) {
  return (str || "").toLowerCase()
    .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
}

/** Fetch routes once and cache */
async function acLoadRoutes() {
  if (_routes.length > 0) return _routes
  try {
    const res = await fetch(`${AC_API}/routes`)
    if (res.ok) {
      _routes   = await res.json()
      _origins  = [...new Set(_routes.map(r => r.origin))].sort()
      _allDests = [...new Set(_routes.map(r => r.destination))].sort()
    }
  } catch (e) { console.warn("autocomplete: could not load routes", e) }
  return _routes
}

/**
 * Get destinations available from a given origin.
 * If origin is blank, returns all destinations.
 */
function acDestinations(origin) {
  if (!origin || !origin.trim()) return _allDests
  const filtered = _routes
    .filter(r => r.origin === origin)
    .map(r => r.destination)
  return [...new Set(filtered)].sort()
}

// ── Dropdown DOM management ─────────────────────────────────────────────────

let _activeDropdown = null

function acCloseAll() {
  document.querySelectorAll(".ac-dropdown").forEach(d => d.remove())
  _activeDropdown = null
}

function acCreateDropdown(input, items, onSelect) {
  acCloseAll()
  if (!items.length) return

  const rect = input.getBoundingClientRect()
  const box  = input.closest(".ac-wrap") || input.parentElement

  const dd = document.createElement("div")
  dd.className = "ac-dropdown"
  Object.assign(dd.style, {
    position:    "absolute",
    top:         input.offsetHeight + 4 + "px",
    left:        "0",
    right:       "0",
    zIndex:      "9999",
    background:  "#fff",
    border:      "1.5px solid #d0deff",
    borderRadius:"12px",
    boxShadow:   "0 8px 28px rgba(30,111,255,0.12)",
    maxHeight:   "240px",
    overflowY:   "auto",
    padding:     "6px 0"
  })

  items.forEach(text => {
    const item = document.createElement("div")
    item.className = "ac-item"
    item.innerText = text
    Object.assign(item.style, {
      padding:    "9px 16px",
      fontSize:   "0.87rem",
      cursor:     "pointer",
      color:      "#2d3748",
      transition: "background 0.1s"
    })
    item.addEventListener("mouseenter", () => item.style.background = "#f0f4ff")
    item.addEventListener("mouseleave", () => item.style.background = "")
    item.addEventListener("mousedown", e => {
      e.preventDefault()
      input.value = text
      acCloseAll()
      onSelect && onSelect(text)
    })
    dd.appendChild(item)
  })

  const wrap = input.parentElement
  if (getComputedStyle(wrap).position === "static") wrap.style.position = "relative"
  wrap.appendChild(dd)
  _activeDropdown = dd
}

// ── Public: init autocomplete on a pair of inputs ──────────────────────────

/**
 * @param {string} originId       - ID of the origin input
 * @param {string} destinationId  - ID of the destination input
 * @param {Function} [onChange]   - optional callback when either value changes
 */
async function initRouteAutocomplete(originId, destinationId, onChange) {
  await acLoadRoutes()

  const originEl = document.getElementById(originId)
  const destEl   = document.getElementById(destinationId)
  if (!originEl || !destEl) return

  // ---- Origin input ----
  function showOrigins() {
    const q   = acNorm(originEl.value)
    const list = q
      ? _origins.filter(o => acNorm(o).includes(q))
      : _origins
    acCreateDropdown(originEl, list, selected => {
      originEl.value = selected
      onChange && onChange()
      destEl.focus()
    })
  }

  originEl.addEventListener("focus", showOrigins)
  originEl.addEventListener("input", showOrigins)

  // ---- Destination input ----
  function showDests() {
    const q     = acNorm(destEl.value)
    const pool  = acDestinations(originEl.value.trim())
    const list  = q ? pool.filter(d => acNorm(d).includes(q)) : pool
    acCreateDropdown(destEl, list, selected => {
      destEl.value = selected
      onChange && onChange()
    })
  }

  destEl.addEventListener("focus", showDests)
  destEl.addEventListener("input", showDests)

  // Close dropdown on outside click
  document.addEventListener("click", e => {
    if (!e.target.closest(".ac-dropdown") && e.target !== originEl && e.target !== destEl) {
      acCloseAll()
    }
  })
}

/**
 * Build popular-route chips from loaded routes.
 * Returns an array of { label, origin, destination } for the top routes.
 */
function acPopularRoutes() {
  // Fixed popular routes (shown only if they exist in DB)
  const popular = [
    { origin: "TP. Hồ Chí Minh", destination: "Đà Lạt",    label: "HCM → Đà Lạt" },
    { origin: "TP. Hồ Chí Minh", destination: "Nha Trang",  label: "HCM → Nha Trang" },
    { origin: "TP. Hồ Chí Minh", destination: "Đà Nẵng",   label: "HCM → Đà Nẵng" },
    { origin: "TP. Hồ Chí Minh", destination: "Vũng Tàu",  label: "HCM → Vũng Tàu" },
    { origin: "TP. Hồ Chí Minh", destination: "Cần Thơ",   label: "HCM → Cần Thơ" },
    { origin: "Hà Nội",          destination: "Đà Nẵng",   label: "HN → Đà Nẵng" },
    { origin: "Hà Nội",          destination: "Hải Phòng", label: "HN → Hải Phòng" },
    { origin: "Hà Nội",          destination: "Sapa",      label: "HN → Sapa" },
    { origin: "Đà Nẵng",        destination: "Hội An",    label: "Đà Nẵng → Hội An" },
    { origin: "Đà Nẵng",        destination: "Huế",       label: "Đà Nẵng → Huế" },
  ]
  return popular.filter(p =>
    _routes.some(r => r.origin === p.origin && r.destination === p.destination)
  )
}
