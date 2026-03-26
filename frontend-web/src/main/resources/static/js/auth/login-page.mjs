import { z } from "https://cdn.jsdelivr.net/npm/zod@3.23.8/+esm";
import { apiJson, apiMessage } from "./auth-http.mjs";

const schema = z.object({
  identity: z.string().trim().min(1, "Nhập tài khoản hoặc email"),
  password: z.string().min(8, "Mật khẩu tối thiểu 8 ký tự")
});

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
      btn.setAttribute("aria-label", show ? "Ẩn mật khẩu" : "Hiện mật khẩu");
      const eye = btn.querySelector("[data-eye-open]");
      const eyeOff = btn.querySelector("[data-eye-off]");
      if (eye && eyeOff) {
        eye.classList.toggle("hidden", show);
        eyeOff.classList.toggle("hidden", !show);
      }
    });
  });
}

async function doLogin(nf) {
  const identity = document.getElementById("identity").value.trim();
  const password = document.getElementById("password").value;
  const remember = document.getElementById("rememberMe").checked;

  const parsed = schema.safeParse({ identity, password });
  if (!parsed.success) {
    const first = parsed.error.flatten().fieldErrors;
    const msg = first.identity?.[0] || first.password?.[0] || "Dữ liệu không hợp lệ";
    nf.error(msg);
    return;
  }

  const api = window.__API_BASE__ || "http://localhost:8080/api";
  const btn = document.getElementById("loginSubmit");
  btn.disabled = true;
  btn.textContent = "Đang đăng nhập...";

  let result;
  try {
    result = await apiJson(api + "/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: identity,
        email: identity,
        password: password,
        rememberMe: remember
      })
    });
  } catch (e) {
    nf.error("Không kết nối được server. Kiểm tra backend (port 8080).");
    btn.disabled = false;
    btn.textContent = "Đăng nhập";
    return;
  }

  btn.disabled = false;
  btn.textContent = "Đăng nhập";

  if (!result.ok) {
    nf.error(apiMessage(result));
    return;
  }

  const token = extractToken(result.payload);
  if (!token) {
    nf.error(apiMessage(result));
    return;
  }

  window.AuthState.setSession(
    {
      token: token,
      userId: result.payload.userId ?? result.payload.id ?? "",
      username: result.payload.username ?? result.payload.name ?? identity,
      role: result.payload.role ?? result.payload.userRole ?? "ROLE_USER"
    },
    remember
  );
  sessionStorage.setItem("justLoggedIn", "1");
  nf.success("Đăng nhập thành công");
  const redirect = new URLSearchParams(window.location.search).get("redirect");
  setTimeout(function () {
    window.location.href = redirect || "/";
  }, 400);
}

function initGoogle(nf) {
  const cid = window.__GOOGLE_CLIENT_ID__;
  const holder = document.getElementById("googleBtnWrap");
  if (!cid || !holder) {
    if (holder) holder.classList.add("hidden");
    return;
  }

  window.handleGoogleCredential = function (resp) {
    if (!resp || !resp.credential) {
      nf.error("Đăng nhập Google thất bại.");
      return;
    }
    const api = window.__API_BASE__ || "http://localhost:8080/api";
    apiJson(api + "/auth/google", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ idToken: resp.credential })
    }).then(function (result) {
      if (!result.ok) {
        nf.error(apiMessage(result));
        return;
      }
      const token = extractToken(result.payload);
      if (!token) {
        nf.error(apiMessage(result));
        return;
      }
      window.AuthState.setSession(
        {
          token: token,
          userId: result.payload.userId ?? "",
          username: result.payload.username ?? "",
          role: result.payload.role ?? "ROLE_USER"
        },
        true
      );
      sessionStorage.setItem("justLoggedIn", "1");
      nf.success("Đăng nhập Google thành công");
      setTimeout(function () {
        window.location.href = "/";
      }, 400);
    });
  };

  const tryRender = function () {
    if (!window.google || !google.accounts || !google.accounts.id) {
      setTimeout(tryRender, 80);
      return;
    }
    google.accounts.id.initialize({
      client_id: cid,
      callback: window.handleGoogleCredential,
      auto_select: false
    });
    var w = Math.min(360, Math.max(260, holder.clientWidth || 300));
    google.accounts.id.renderButton(holder, {
      type: "standard",
      theme: "outline",
      size: "large",
      text: "continue_with",
      width: w
    });
  };
  tryRender();
}

document.addEventListener("DOMContentLoaded", function () {
  const nf = notyf();
  wirePasswordToggle(document);

  document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();
    doLogin(nf);
  });

  initGoogle(nf);
});
