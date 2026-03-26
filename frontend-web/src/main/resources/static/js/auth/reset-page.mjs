import { z } from "https://cdn.jsdelivr.net/npm/zod@3.23.8/+esm";
import { apiJson, apiMessage } from "./auth-http.mjs";

const schema = z
  .object({
    email: z.string().trim().email("Email không hợp lệ"),
    code: z.string().trim().min(4, "Nhập mã OTP").max(10),
    password: z.string().min(8, "Mật khẩu tối thiểu 8 ký tự"),
    confirm: z.string()
  })
  .refine(function (d) {
    return d.password === d.confirm;
  }, { message: "Mật khẩu nhập lại không khớp", path: ["confirm"] });

function notyf() {
  return new window.Notyf({
    duration: 4500,
    position: { x: "right", y: "top" },
    dismissible: true
  });
}

function wirePasswordToggle(root) {
  root.querySelectorAll("[data-toggle-pwd]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const id = btn.getAttribute("data-target");
      const input = id ? document.getElementById(id) : null;
      if (!input) return;
      const show = input.type === "password";
      input.type = show ? "text" : "password";
      const eye = btn.querySelector("[data-eye-open]");
      const eyeOff = btn.querySelector("[data-eye-off]");
      if (eye && eyeOff) {
        eye.classList.toggle("hidden", show);
        eyeOff.classList.toggle("hidden", !show);
      }
    });
  });
}

document.addEventListener("DOMContentLoaded", function () {
  const nf = notyf();
  wirePasswordToggle(document);

  var saved = sessionStorage.getItem("passwordResetEmail");
  if (saved) {
    document.getElementById("email").value = saved;
  }

  document.getElementById("resetForm").addEventListener("submit", function (e) {
    e.preventDefault();
    const email = document.getElementById("email").value.trim();
    const code = document.getElementById("code").value.trim();
    const password = document.getElementById("password").value;
    const confirm = document.getElementById("confirmPassword").value;

    const parsed = schema.safeParse({ email, code, password, confirm });
    if (!parsed.success) {
      const fe = parsed.error.flatten().fieldErrors;
      const msg =
        fe.email?.[0] || fe.code?.[0] || fe.password?.[0] || fe.confirm?.[0] || "Dữ liệu không hợp lệ";
      nf.error(msg);
      return;
    }

    const api = window.__API_BASE__ || "http://localhost:8080/api";
    const btn = document.getElementById("resetSubmit");
    btn.disabled = true;
    btn.textContent = "Đang cập nhật...";

    apiJson(api + "/auth/reset-password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, code, newPassword: password })
    })
      .then(function (result) {
        btn.disabled = false;
        btn.textContent = "Đặt mật khẩu mới";

        if (!result.ok) {
          nf.error(apiMessage(result));
          return;
        }

        sessionStorage.removeItem("passwordResetEmail");
        nf.success((result.payload && result.payload.message) || "Đổi mật khẩu thành công.");
        setTimeout(function () {
          window.location.href = "/login";
        }, 900);
      })
      .catch(function () {
        btn.disabled = false;
        btn.textContent = "Đặt mật khẩu mới";
        nf.error("Không kết nối được server.");
      });
  });
});
