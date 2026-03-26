import { z } from "https://cdn.jsdelivr.net/npm/zod@3.23.8/+esm";
import { apiJson, apiMessage } from "./auth-http.mjs";

const schema = z.object({
  email: z.string().trim().email("Email không hợp lệ")
});

function notyf() {
  return new window.Notyf({
    duration: 5000,
    position: { x: "right", y: "top" },
    dismissible: true
  });
}

document.addEventListener("DOMContentLoaded", function () {
  const nf = notyf();

  document.getElementById("forgotForm").addEventListener("submit", function (e) {
    e.preventDefault();
    const email = document.getElementById("email").value.trim();
    const parsed = schema.safeParse({ email });
    if (!parsed.success) {
      const fe = parsed.error.flatten().fieldErrors.email;
      nf.error(fe && fe[0] ? fe[0] : "Email không hợp lệ");
      return;
    }

    const api = window.__API_BASE__ || "http://localhost:8080/api";
    const btn = document.getElementById("forgotSubmit");
    btn.disabled = true;
    btn.textContent = "Đang gửi...";

    apiJson(api + "/auth/forgot-password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email })
    })
      .then(function (result) {
        btn.disabled = false;
        btn.textContent = "Gửi mã OTP";

        if (!result.ok) {
          nf.error(apiMessage(result));
          return;
        }

        const msg = result.payload && result.payload.message ? result.payload.message : "Đã xử lý yêu cầu.";
        nf.success(msg);
        sessionStorage.setItem("passwordResetEmail", email);
        setTimeout(function () {
          window.location.href = "/reset-password";
        }, 800);
      })
      .catch(function () {
        btn.disabled = false;
        btn.textContent = "Gửi mã OTP";
        nf.error("Không kết nối được server.");
      });
  });
});
