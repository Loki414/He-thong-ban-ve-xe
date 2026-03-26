import { z } from "https://cdn.jsdelivr.net/npm/zod@3.23.8/+esm";
import { apiJson, apiMessage } from "./auth-http.mjs";

const schema = z
  .object({
    username: z.string().trim().min(3, "Tài khoản tối thiểu 3 ký tự").max(50),
    email: z.string().trim().email("Email không hợp lệ"),
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

function extractToken(payload) {
  if (!payload) return null;
  return payload.token || payload.accessToken || payload.jwtToken || null;
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

  document.getElementById("registerForm").addEventListener("submit", function (e) {
    e.preventDefault();
    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirm = document.getElementById("confirmPassword").value;

    const parsed = schema.safeParse({ username, email, password, confirm });
    if (!parsed.success) {
      const fe = parsed.error.flatten().fieldErrors;
      const msg =
        fe.username?.[0] ||
        fe.email?.[0] ||
        fe.password?.[0] ||
        fe.confirm?.[0] ||
        "Dữ liệu không hợp lệ";
      nf.error(msg);
      return;
    }

    const api = window.__API_BASE__ || "http://localhost:8080/api";
    const btn = document.getElementById("regSubmit");
    btn.disabled = true;
    btn.textContent = "Đang đăng ký...";

    apiJson(api + "/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, email, password })
    })
      .then(function (result) {
        btn.disabled = false;
        btn.textContent = "Đăng ký";

        if (!result.ok) {
          nf.error(apiMessage(result));
          return;
        }

        const token = extractToken(result.payload);
        if (token && result.payload) {
          window.AuthState.setSession(
            {
              token: token,
              userId: result.payload.userId ?? "",
              username: result.payload.username ?? username,
              role: result.payload.role ?? "ROLE_USER"
            },
            true
          );
          sessionStorage.setItem("justLoggedIn", "1");
          nf.success("Đăng ký thành công!");
          setTimeout(function () {
            window.location.href = "/";
          }, 500);
          return;
        }

        nf.success("Đăng ký thành công! Vui lòng đăng nhập.");
        setTimeout(function () {
          window.location.href = "/login";
        }, 600);
      })
      .catch(function () {
        btn.disabled = false;
        btn.textContent = "Đăng ký";
        nf.error("Không kết nối được server.");
      });
  });
});
