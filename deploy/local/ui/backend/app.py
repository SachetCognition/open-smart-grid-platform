# SPDX-FileCopyrightText: Copyright Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0
"""Backend-for-frontend for the GXF local journeys UI.

Exposes a small JSON API that the single-page UI calls to exercise the three
headline journeys against a locally deployed GXF stack:

* Smart metering GetActualMeterReads over the new REST/JSON facade (WS3).
* Public lighting SetLight over the existing SOAP endpoint (mTLS).
* Smart metering GetActualMeterReads over the existing SOAP endpoint (mTLS).

It also aggregates Spring Boot Actuator health for the WS6 cloud-native
services. All upstream URLs and credentials are supplied via environment
variables so the same UI runs against k3d port-forwards or any other host.
"""
from __future__ import annotations

import os
import urllib.parse
from pathlib import Path

import httpx
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"

# Upstream endpoints (override via env for different environments).
SM_REST_BASE = os.environ.get(
    "SM_REST_BASE",
    "http://localhost:18080/osgp-adapter-ws-smartmetering/rest",
)
SOAP_PL_URL = os.environ.get(
    "SOAP_PL_URL",
    "https://localhost:18443/osgp-adapter-ws-publiclighting/publiclighting/adHocManagementService/",
)
SOAP_SM_URL = os.environ.get(
    "SOAP_SM_URL",
    "https://localhost:18443/osgp-adapter-ws-smartmetering/smartmetering/monitoringService/",
)
ORG_ID = os.environ.get("ORG_ID", "test-org")
USER_NAME = os.environ.get("USER_NAME", "test-org")
APPLICATION_NAME = os.environ.get("APPLICATION_NAME", "GXF-Journeys-UI")
CLIENT_CERT = os.environ.get("CLIENT_CERT", "/home/ubuntu/ui-certs/test-org-cert.pem")
CLIENT_KEY = os.environ.get("CLIENT_KEY", "/home/ubuntu/ui-certs/test-org-key.pem")

# name -> actuator base url
ACTUATOR_TARGETS = {
    "osgp-core": os.environ.get("ACT_CORE", "http://localhost:18091/actuator"),
    "osgp-adapter-domain-tariffswitching": os.environ.get(
        "ACT_DOMAIN_TS", "http://localhost:18092/actuator"
    ),
    "osgp-adapter-ws-tariffswitching": os.environ.get(
        "ACT_WS_TS", "http://localhost:18093/actuator"
    ),
}

PL_NS = "http://www.opensmartgridplatform.org/schemas/publiclighting/adhocmanagement/2014/10"
SM_NS = "http://www.opensmartgridplatform.org/schemas/smartmetering/sm-monitoring/2014/10"
COMMON_NS = "http://www.opensmartgridplatform.org/schemas/common"


def _soap_header() -> str:
    """OSGP common SOAP header carrying the organisation/user/application identity.

    The endpoint interceptors read these three elements from the common schema
    namespace; mTLS authenticates the organisation, these headers authorise it.
    """
    return (
        f'<common:OrganisationIdentification xmlns:common="{COMMON_NS}">{ORG_ID}'
        "</common:OrganisationIdentification>"
        f'<common:UserName xmlns:common="{COMMON_NS}">{USER_NAME}</common:UserName>'
        f'<common:ApplicationName xmlns:common="{COMMON_NS}">{APPLICATION_NAME}'
        "</common:ApplicationName>"
    )


app = FastAPI(title="GXF Journeys UI", version="1.0")


def _soap_client() -> httpx.Client:
    return httpx.Client(cert=(CLIENT_CERT, CLIENT_KEY), verify=False, timeout=30.0)


# --------------------------------------------------------------------------- #
# Smart metering — REST facade (WS3)
# --------------------------------------------------------------------------- #
class MeterReadRequest(BaseModel):
    deviceIdentification: str


@app.post("/api/rest/actual-meter-reads")
def rest_enqueue(req: MeterReadRequest):
    with httpx.Client(timeout=30.0) as client:
        r = client.post(
            f"{SM_REST_BASE}/smartmetering/monitoring/actual-meter-reads",
            headers={"OrganisationIdentification": ORG_ID, "Content-Type": "application/json"},
            json={"deviceIdentification": req.deviceIdentification},
        )
    return JSONResponse(status_code=r.status_code, content=_safe_json(r))


@app.get("/api/rest/actual-meter-reads/{correlation_uid}")
def rest_result(correlation_uid: str):
    with httpx.Client(timeout=30.0) as client:
        r = client.get(
            f"{SM_REST_BASE}/smartmetering/monitoring/actual-meter-reads/"
            f"{urllib.parse.quote(correlation_uid, safe='')}",
            headers={"OrganisationIdentification": ORG_ID},
        )
    return JSONResponse(status_code=r.status_code, content=_safe_json(r))


# --------------------------------------------------------------------------- #
# Public lighting — SetLight over SOAP
# --------------------------------------------------------------------------- #
class SetLightRequest(BaseModel):
    deviceIdentification: str
    index: int = 1
    on: bool = True
    dimValue: int | None = None


@app.post("/api/soap/set-light")
def soap_set_light(req: SetLightRequest):
    dim = f"<ns:DimValue>{req.dimValue}</ns:DimValue>" if req.dimValue is not None else ""
    body = f"""<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="{PL_NS}">
  <soap:Header>{_soap_header()}</soap:Header>
  <soap:Body>
    <ns:SetLightRequest>
      <ns:DeviceIdentification>{req.deviceIdentification}</ns:DeviceIdentification>
      <ns:LightValue>
        <ns:Index>{req.index}</ns:Index>
        <ns:On>{str(req.on).lower()}</ns:On>
        {dim}
      </ns:LightValue>
    </ns:SetLightRequest>
  </soap:Body>
</soap:Envelope>"""
    return _post_soap(SOAP_PL_URL, body)


# --------------------------------------------------------------------------- #
# Smart metering — GetActualMeterReads over SOAP
# --------------------------------------------------------------------------- #
class SoapMeterReadRequest(BaseModel):
    deviceIdentification: str


@app.post("/api/soap/actual-meter-reads")
def soap_actual_meter_reads(req: SoapMeterReadRequest):
    body = f"""<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="{SM_NS}">
  <soap:Header>{_soap_header()}</soap:Header>
  <soap:Body>
    <ns:ActualMeterReadsRequest>
      <ns:DeviceIdentification>{req.deviceIdentification}</ns:DeviceIdentification>
    </ns:ActualMeterReadsRequest>
  </soap:Body>
</soap:Envelope>"""
    return _post_soap(SOAP_SM_URL, body)


def _post_soap(url: str, body: str):
    try:
        with _soap_client() as client:
            r = client.post(
                url,
                content=body.encode("utf-8"),
                headers={"Content-Type": "text/xml; charset=utf-8", "SOAPAction": ""},
            )
        return JSONResponse(
            status_code=200 if r.status_code < 400 else r.status_code,
            content={"httpStatus": r.status_code, "soap": r.text},
        )
    except Exception as exc:  # noqa: BLE001 - surface upstream errors to the UI
        raise HTTPException(status_code=502, detail=f"SOAP call failed: {exc}") from exc


# --------------------------------------------------------------------------- #
# Actuator health dashboard (WS6)
# --------------------------------------------------------------------------- #
@app.get("/api/health")
def health():
    out = {}
    with httpx.Client(timeout=5.0) as client:
        for name, base in ACTUATOR_TARGETS.items():
            entry = {"status": "DOWN", "liveness": None, "readiness": None}
            for probe in ("health", "health/liveness", "health/readiness"):
                try:
                    resp = client.get(f"{base}/{probe}")
                    data = resp.json()
                    if probe == "health":
                        entry["status"] = data.get("status", "UNKNOWN")
                    elif probe == "health/liveness":
                        entry["liveness"] = data.get("status")
                    else:
                        entry["readiness"] = data.get("status")
                except Exception:  # noqa: BLE001 - service may be unreachable
                    pass
            out[name] = entry
    return out


@app.get("/api/config")
def config():
    return {
        "organisation": ORG_ID,
        "restBase": SM_REST_BASE,
        "userName": USER_NAME,
        "applicationName": APPLICATION_NAME,
        "soapPublicLighting": SOAP_PL_URL,
        "soapSmartMetering": SOAP_SM_URL,
        "actuatorTargets": list(ACTUATOR_TARGETS.keys()),
    }


def _safe_json(resp: httpx.Response):
    try:
        return resp.json()
    except Exception:  # noqa: BLE001
        return {"raw": resp.text}


@app.get("/")
def index():
    return FileResponse(FRONTEND_DIR / "index.html")


app.mount("/", StaticFiles(directory=str(FRONTEND_DIR), html=True), name="static")
