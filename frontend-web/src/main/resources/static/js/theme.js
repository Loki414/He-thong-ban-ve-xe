/**
 * BusBooking – Light/Dark theme
 * Usage: include <script src="js/theme.js"></script> and add <span id="themeToggleContainer"></span> in navbar
 */
(function () {
  const STORAGE_KEY = "busbooking-theme";

  function getTheme() {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === "dark" || saved === "light") return saved;
    return "light";
  }

  function setTheme(value) {
    document.documentElement.setAttribute("data-theme", value);
    localStorage.setItem(STORAGE_KEY, value);
  }

  function toggleTheme() {
    const next = getTheme() === "dark" ? "light" : "dark";
    setTheme(next);
    updateToggleButton();
  }

  function updateToggleButton() {
    const btn = document.getElementById("themeToggleBtn");
    if (!btn) return;
    const isDark = getTheme() === "dark";
    btn.innerHTML = isDark ? "&#9728;" : "&#9790;"; // sun : moon
    btn.title = isDark ? "Chuyển sang chế độ sáng" : "Chuyển sang chế độ tối";
  }

  // Init on load
  setTheme(getTheme());

  // Inject toggle button into container if present
  const container = document.getElementById("themeToggleContainer");
  if (container) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "theme-toggle-btn";
    btn.id = "themeToggleBtn";
    btn.setAttribute("aria-label", "Đổi chế độ sáng / tối");
    btn.onclick = toggleTheme;
    container.appendChild(btn);
    updateToggleButton();
  }

  // Expose for manual use
  window.toggleTheme = toggleTheme;
  window.getTheme = getTheme;
  window.setTheme = setTheme;
})();
