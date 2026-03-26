/**
 * Global auth session (sessionStorage vs localStorage for "Remember me").
 * Other pages should use AuthState.getToken() after including this script.
 */
(function (global) {
  var KEYS = ["token", "userId", "username", "role"];

  function clearKeys(store) {
    KEYS.forEach(function (k) {
      store.removeItem(k);
    });
  }

  global.AuthState = {
    getToken: function () {
      return sessionStorage.getItem("token") || localStorage.getItem("token") || "";
    },
    getUserId: function () {
      return sessionStorage.getItem("userId") || localStorage.getItem("userId") || "";
    },
    getUsername: function () {
      return sessionStorage.getItem("username") || localStorage.getItem("username") || "";
    },
    getRole: function () {
      return sessionStorage.getItem("role") || localStorage.getItem("role") || "";
    },
    isAuthed: function () {
      return !!this.getToken();
    },
    /**
     * @param {{ token: string, userId?: string|number, username?: string, role?: string }} data
     * @param {boolean} rememberMe
     */
    setSession: function (data, rememberMe) {
      var primary = rememberMe ? localStorage : sessionStorage;
      var secondary = rememberMe ? sessionStorage : localStorage;
      clearKeys(secondary);
      if (data.token != null) primary.setItem("token", data.token);
      if (data.userId != null) primary.setItem("userId", String(data.userId));
      if (data.username != null) primary.setItem("username", data.username);
      if (data.role != null) primary.setItem("role", data.role);
      global.dispatchEvent(new CustomEvent("bus-auth-change", { detail: { authed: true } }));
    },
    clear: function () {
      clearKeys(localStorage);
      clearKeys(sessionStorage);
      global.dispatchEvent(new CustomEvent("bus-auth-change", { detail: { authed: false } }));
    },
    subscribe: function (fn) {
      global.addEventListener("bus-auth-change", fn);
      return function () {
        global.removeEventListener("bus-auth-change", fn);
      };
    }
  };
})(window);
