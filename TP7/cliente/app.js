const API_BASE = "";

class ApiError extends Error {
  constructor(message, details) {
    super(message);
    this.name = "ApiError";
    this.details = details;
  }
}

const $ = (id) => document.getElementById(id);

let productosCache = [];
let ventasCache = [];
let toastTimer = null;

const moneyFormatter = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function money(value) {
  return moneyFormatter.format(Number(value) || 0);
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;",
  })[char]);
}

async function request(path, options = {}) {
  const method = options.method || "GET";
  const url = API_BASE + path;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 8000);

  try {
    const response = await fetch(url, {
      method,
      ...options,
      signal: controller.signal,
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {}),
      },
    });

    const text = await response.text();
    let body = null;
    if (text) {
      try {
        body = JSON.parse(text);
      } catch {
        body = text;
      }
    }

    if (!response.ok) {
      const detail = body && typeof body === "object" ? body.detail : null;
      const message = typeof detail === "string"
        ? detail
        : detail
          ? JSON.stringify(detail)
          : `Error ${response.status}`;
      throw new ApiError(message, { method, url, status: response.status, body });
    }

    return body;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    const message = error.name === "AbortError"
      ? "Tiempo de espera agotado al contactar al servidor"
      : error.message;
    throw new ApiError(message, {
      method,
      url,
      status: "sin respuesta",
      body: { message },
    });
  } finally {
    clearTimeout(timer);
  }
}

function setServerStatus(online, label) {
  const pill = $("serverStatus");
  pill.className = `status-pill ${online ? "online" : "offline"}`;
  pill.innerHTML = `<span class="status-dot"></span>${escapeHtml(label)}`;
}

function showToast(message, type = "success") {
  const toast = $("toast");
  toast.textContent = message;
  toast.className = `toast visible ${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.className = "toast";
  }, 3200);
}

function clearError() {
  $("errorPanel").hidden = true;
  $("errorHint").hidden = false;
}

function showError(error) {
  const details = error.details || {};
  const body = details.body;
  const path = details.url ? details.url.replace(/^https?:\/\/[^/]+/, "") : "—";
  const detail = body && typeof body === "object" ? body.detail : body;

  $("errorMethod").textContent = details.method || "—";
  $("errorUrl").textContent = details.url || "—";
  $("errorStatus").textContent = details.status || "—";
  $("errorEndpoint").textContent = `${details.method || "—"} ${path}`;
  $("errorDetail").textContent = typeof detail === "string"
    ? detail
    : JSON.stringify(detail || { message: error.message });
  $("errorBody").textContent = typeof body === "string"
    ? body
    : JSON.stringify(body || { message: error.message }, null, 2);

  $("errorPanel").hidden = false;
  $("errorHint").hidden = true;
}

function renderKpis() {
  const total = ventasCache.reduce((sum, venta) => sum + Number(venta.total || 0), 0);
  const unidades = ventasCache.reduce((sum, venta) => sum + Number(venta.cantidad || 0), 0);
  const stockBajo = productosCache.filter((producto) => Number(producto.stock) <= 5).length;

  $("kpiTotal").textContent = money(total);
  $("kpiVentas").textContent = String(ventasCache.length);
  $("kpiUnidades").textContent = String(unidades);
  $("kpiStock").textContent = String(stockBajo);
}

function renderProductSelect() {
  const select = $("productoSelect");
  const previous = select.value;

  select.innerHTML = "";
  productosCache.forEach((producto) => {
    const option = document.createElement("option");
    option.value = String(producto.id);
    option.textContent = `${producto.nombre} · ${money(producto.precio)} · stock ${producto.stock}`;
    select.appendChild(option);
  });

  if (previous && productosCache.some((p) => String(p.id) === previous)) {
    select.value = previous;
  }
}

function renderProducts() {
  const body = $("productosBody");

  body.innerHTML = productosCache.map((producto) => {
    const stock = Number(producto.stock);
    const badge = stock === 0
      ? '<span class="badge badge-out">Sin stock</span>'
      : stock <= 5
        ? '<span class="badge badge-low">Stock bajo</span>'
        : '<span class="badge badge-ok">Disponible</span>';

    return `
      <tr>
        <td>${escapeHtml(producto.id)}</td>
        <td><strong>${escapeHtml(producto.nombre)}</strong></td>
        <td>${money(producto.precio)}</td>
        <td>${escapeHtml(stock)}</td>
        <td>${badge}</td>
      </tr>
    `;
  }).join("");
}

function formatDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString("es-AR", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function renderVentas() {
  const body = $("ventasBody");
  const ordenadas = [...ventasCache].sort((a, b) => b.id - a.id);

  body.innerHTML = ordenadas.map((venta) => `
    <tr>
      <td>#${escapeHtml(venta.id)}</td>
      <td><strong>${escapeHtml(venta.producto_nombre)}</strong></td>
      <td>${escapeHtml(venta.cantidad)}</td>
      <td>${money(venta.total)}</td>
      <td>${escapeHtml(formatDate(venta.fecha))}</td>
    </tr>
  `).join("");

  $("emptyVentas").hidden = ordenadas.length > 0;
}

function renderChart() {
  const chart = $("chartBars");
  const totals = new Map();

  ventasCache.forEach((venta) => {
    const nombre = venta.producto_nombre;
    totals.set(nombre, (totals.get(nombre) || 0) + Number(venta.total || 0));
  });

  if (!totals.size) {
    chart.innerHTML = '<p class="chart-empty">Registrá una venta para ver el gráfico.</p>';
    return;
  }

  const max = Math.max(...totals.values());
  chart.innerHTML = [...totals.entries()].map(([nombre, total]) => {
    const width = Math.max(4, Math.round((total / max) * 100));
    return `
      <div class="chart-row">
        <span class="chart-name" title="${escapeHtml(nombre)}">${escapeHtml(nombre)}</span>
        <div class="chart-track"><div class="chart-fill" style="width:${width}%"></div></div>
        <span class="chart-value">${money(total)}</span>
      </div>
    `;
  }).join("");
}

function previewTotal() {
  const producto = productosCache.find((p) => String(p.id) === $("productoSelect").value);
  const cantidad = Math.max(0, parseInt($("cantidadInput").value, 10) || 0);
  $("totalPreview").textContent = money((producto ? Number(producto.precio) : 0) * cantidad);
}

async function loadDashboard() {
  try {
    const [health, productos, ventas] = await Promise.all([
      request("/health"),
      request("/productos"),
      request("/ventas"),
    ]);

    productosCache = productos;
    ventasCache = ventas;

    const etiquetaProductos = health.productos === 1 ? "producto" : "productos";
    setServerStatus(true, `Servidor OK · ${health.productos} ${etiquetaProductos}`);
    renderKpis();
    renderProductSelect();
    renderProducts();
    renderVentas();
    renderChart();
    previewTotal();
    clearError();
  } catch (error) {
    setServerStatus(false, "Servidor sin conexion");
    showError(error);
    showToast("No se pudo cargar el panel", "error");
  }
}

async function submitForm(button, action) {
  button.disabled = true;
  try {
    await action();
  } finally {
    button.disabled = false;
  }
}

$("ventaForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const button = $("submitVenta");

  submitForm(button, async () => {
    const payload = {
      producto_id: Number($("productoSelect").value),
      cantidad: Number($("cantidadInput").value),
    };

    try {
      await request("/ventas", { method: "POST", body: JSON.stringify(payload) });
      clearError();
      showToast("Venta registrada correctamente", "success");
      $("cantidadInput").value = "1";
      await loadDashboard();
    } catch (error) {
      showError(error);
      showToast(error.message, "error");
    }
  });
});

$("productoForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = form.querySelector("button");

  submitForm(button, async () => {
    const payload = {
      id: Number($("prodId").value),
      nombre: $("prodNombre").value.trim(),
      precio: Number($("prodPrecio").value),
      stock: Number($("prodStock").value),
    };

    try {
      await request("/productos", { method: "POST", body: JSON.stringify(payload) });
      form.reset();
      clearError();
      showToast("Producto agregado al inventario", "success");
      await loadDashboard();
    } catch (error) {
      showError(error);
      showToast(error.message, "error");
    }
  });
});

async function probarError(etiqueta, path, options, esperado) {
  const method = options.method || "GET";
  try {
    await request(path, options);
    showError(new ApiError("El servidor devolvió una respuesta inesperada", {
      method,
      url: API_BASE + path,
      status: "sin error",
      body: null,
    }));
    showToast(`${etiqueta}: se esperaba ${esperado} pero la petición fue OK`, "error");
  } catch (error) {
    showError(error);
    const estado = error.details ? error.details.status : "desconocido";
    if (estado === esperado) {
      showToast(`${etiqueta}: error ${esperado} capturado correctamente`, "success");
    } else {
      showToast(`${etiqueta}: se esperaba ${esperado} y llegó ${estado}`, "error");
    }
  }
}

function primerIdProducto() {
  return productosCache.length ? Number(productosCache[0].id) : 1;
}

$("btnTest400").addEventListener("click", () => probarError(
  "400",
  "/productos",
  { method: "POST", body: JSON.stringify({ id: 0, nombre: "Producto invalido", precio: 10, stock: 1 }) },
  400,
));
$("btnTest404").addEventListener("click", () => probarError("Producto 777", "/productos/777", {}, 404));
$("btnTest409").addEventListener("click", () => probarError(
  "409",
  "/productos",
  { method: "POST", body: JSON.stringify({ id: primerIdProducto(), nombre: "Duplicado", precio: 10, stock: 1 }) },
  409,
));
$("btnTest500").addEventListener("click", () => probarError("500", "/debug/error500", {}, 500));

$("btnRefresh").addEventListener("click", loadDashboard);
$("productoSelect").addEventListener("change", previewTotal);
$("cantidadInput").addEventListener("input", previewTotal);

document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => {
    document.querySelectorAll(".nav-item").forEach((link) => link.classList.remove("active"));
    item.classList.add("active");
  });
});

$("todayLabel").textContent = new Intl.DateTimeFormat("es-AR", {
  dateStyle: "full",
}).format(new Date());

loadDashboard();
