export async function apiJson(url, init) {
  const res = await fetch(url, init);
  const text = await res.text();
  let payload = null;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch (_) {
    /* ignore */
  }
  return { ok: res.ok, status: res.status, text, payload };
}

export function apiMessage(result) {
  if (result.payload && result.payload.message) return result.payload.message;
  if (result.text && result.text.length < 500) return result.text;
  return "Request failed (" + result.status + ")";
}
