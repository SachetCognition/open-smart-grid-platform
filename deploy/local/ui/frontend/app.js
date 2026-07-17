// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
// SPDX-License-Identifier: Apache-2.0
"use strict";

const $ = (id) => document.getElementById(id);

function setOut(el, data, cls) {
  el.className = "out" + (cls ? " " + cls : "");
  el.textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2);
}

async function callJson(method, url, body) {
  const opts = { method, headers: { "Content-Type": "application/json" } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(url, opts);
  let payload;
  try { payload = await res.json(); } catch { payload = { raw: await res.text() }; }
  return { status: res.status, payload };
}

let lastCid = null;

// --- REST journey ---
$("rest-enqueue").addEventListener("click", async () => {
  const device = $("rest-device").value.trim();
  setOut($("rest-out"), "POST /rest/smartmetering/monitoring/actual-meter-reads …", "pending");
  const { status, payload } = await callJson("POST", "/api/rest/actual-meter-reads", { deviceIdentification: device });
  if (status === 202 && payload.correlationUid) {
    lastCid = payload.correlationUid;
    $("rest-cid").textContent = lastCid;
    $("rest-poll").disabled = false;
    setOut($("rest-out"), { httpStatus: status, ...payload }, "ok");
  } else {
    setOut($("rest-out"), { httpStatus: status, ...payload }, "err");
  }
});

$("rest-poll").addEventListener("click", async () => {
  if (!lastCid) return;
  setOut($("rest-out"), `GET …/actual-meter-reads/${lastCid} …`, "pending");
  const { status, payload } = await callJson("GET", `/api/rest/actual-meter-reads/${lastCid}`);
  const cls = status === 200 ? "ok" : status === 202 ? "pending" : "err";
  setOut($("rest-out"), { httpStatus: status, ...payload }, cls);
});

// --- SOAP SetLight ---
$("pl-send").addEventListener("click", async () => {
  const body = {
    deviceIdentification: $("pl-device").value.trim(),
    index: parseInt($("pl-index").value || "1", 10),
    on: $("pl-on").value === "true",
  };
  const dim = $("pl-dim").value.trim();
  if (dim !== "") body.dimValue = parseInt(dim, 10);
  setOut($("pl-out"), "POST SOAP SetLightRequest …", "pending");
  const { status, payload } = await callJson("POST", "/api/soap/set-light", body);
  render($("pl-out"), status, payload);
});

// --- SOAP GetActualMeterReads ---
$("sm-send").addEventListener("click", async () => {
  setOut($("sm-out"), "POST SOAP ActualMeterReadsRequest …", "pending");
  const { status, payload } = await callJson("POST", "/api/soap/actual-meter-reads", {
    deviceIdentification: $("sm-device").value.trim(),
  });
  render($("sm-out"), status, payload);
});

function render(el, status, payload) {
  const soap = payload.soap || "";
  const ok = (payload.httpStatus || status) < 400 && !/Fault/i.test(soap);
  setOut(el, payload.soap ? `HTTP ${payload.httpStatus}\n\n${soap}` : payload, ok ? "ok" : "err");
}

// --- Health + config ---
async function refreshHealth() {
  try {
    const res = await fetch("/api/health");
    const data = await res.json();
    const pills = $("health-pills");
    const table = $("health-table");
    pills.innerHTML = "";
    table.innerHTML = "";
    for (const [name, h] of Object.entries(data)) {
      const up = h.status === "UP";
      const short = name.replace("osgp-adapter-", "").replace("osgp-", "");
      const pill = document.createElement("div");
      pill.className = "pill " + (up ? "up" : "down");
      pill.innerHTML = `<span class="dot"></span>${short}`;
      pills.appendChild(pill);

      const row = document.createElement("div");
      row.className = "hrow";
      row.innerHTML = `<span class="name">${name}</span>
        <span class="hstat">
          <span class="tag ${up ? "up" : "down"}">health ${h.status}</span>
          <span class="tag ${h.liveness === "UP" ? "up" : "down"}">live ${h.liveness || "?"}</span>
          <span class="tag ${h.readiness === "UP" ? "up" : "down"}">ready ${h.readiness || "?"}</span>
        </span>`;
      table.appendChild(row);
    }
  } catch (e) {
    $("health-pills").innerHTML = `<div class="pill down"><span class="dot"></span>health unavailable</div>`;
  }
}

async function loadConfig() {
  try {
    const cfg = await (await fetch("/api/config")).json();
    $("cfg").textContent = `organisation: ${cfg.organisation}  ·  REST base: ${cfg.restBase}`;
    const base = cfg.restBase;
    $("swagger-link").href = base + "/swagger-ui/index.html";
    $("apidocs-link").href = base + "/v3/api-docs";
  } catch {}
}

loadConfig();
refreshHealth();
setInterval(refreshHealth, 5000);
