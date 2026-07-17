# GXF Modernization — Local Deploy & Journey Validation Report

**Date:** 2026-07-17
**Stack:** reconverged branch `devin/1784289039-reconvergence` (WS1 + WS3 + WS4 + WS5 + WS6) deployed to a local **k3d** cluster (k3s v1.31.5, flannel `host-gw`), images built from this repo (`:recon` tag), broker = **ActiveMQ Artemis**, `jms.*.trust.all.packages=false`.

## Summary

| Journey | Transport | Result |
|---|---|---|
| Public-lighting `SetLight` | SOAP / mTLS | ✅ HTTP 200 `SetLightAsyncResponse` + correlationUid |
| Smart-metering `GetActualMeterReads` | SOAP / mTLS | ✅ HTTP 200 `ActualMeterReadsAsyncResponse` + correlationUid |
| Smart-metering `GetActualMeterReads` enqueue | REST/JSON (WS3) | ✅ HTTP 202 + correlationUid |
| Smart-metering REST poll by correlationUid | REST/JSON (WS3) | ✅ returns async domain result (see DLMS note) |
| OpenAPI / Swagger UI (WS3) | HTTP | ✅ OAS 3.1 document renders |
| Cucumber `platform/publiclighting` | k3d job | ✅ **456 / 456 scenarios, 0 failed** (2217 steps) |
| Actuator health (WS6 services) | HTTP | ✅ health / liveness / readiness = UP |
| Cluster | k3d | ✅ **20 / 20 pods Ready** |

## 1. Journey UI (SOAP + REST + health)

All three headline journeys driven through the local journey UI (`deploy/local/ui`), plus the WS6 Actuator health panel (all UP). The public-lighting SetLight and smart-metering ActualMeterReads SOAP calls return HTTP 200 async responses with correlation UIDs; the REST/JSON facade (WS3) returns HTTP 202 + correlationUid.

![Journey UI](https://partner-workshops.devinenterprise.com/attachments/ce026ca2-e401-46dc-8aa7-8353f26b2228/ui-journeys.png)

## 2. WS3 REST/OpenAPI facade (springdoc)

Swagger UI renders the OpenAPI 3.1 document for the new smart-metering REST facade: `POST /smartmetering/monitoring/actual-meter-reads` (enqueue → correlationUid) and `GET /smartmetering/monitoring/actual-meter-reads/{correlationUid}` (fetch result), additive to the existing SOAP endpoints.

![Swagger UI — WS3](https://partner-workshops.devinenterprise.com/attachments/7f44bf36-4875-48ea-8f0e-b59cb2fb4bbd/swagger-post-expanded.png)

## 3. Cucumber — platform / publiclighting: 456 / 456

Authoritative tally computed from the job's `cucumber.json` (not just Kubernetes Job completion). Covers `SetLight`, `SetLightSchedule`, `GetTariffStatus`, `Set/ReverseTariffSchedule`, `AuthorizeDeviceFunctions`, OSLP registration/events, and core/firmware/config management.

![Cucumber 456/456](https://partner-workshops.devinenterprise.com/attachments/e2ef82a5-6cdf-4642-8a8d-9fdb03e915c2/cucumber-summary.png)

## Known limitations (honest)

- **Smart-meter REST/SOAP result value:** the enqueue → correlationUid → poll contract is fully exercised, but the resolved async result is `NOT_OK` because the **DLMS device simulator is not deployed** in this `gxf-gitops` local variant (its protocol adapter requires a DLMS-specific datasource/secret + a provisioned simulated meter the chart does not package). A completed meter-read *value* is therefore not demonstrated locally. Public-lighting completes end-to-end via the OSLP web-device-simulator (proven by Cucumber 456/456).
- **Swagger UI config URL:** springdoc's auto-generated `swagger-config` omits the `/rest` servlet prefix, so `/rest/swagger-ui/index.html` must be pointed at `/osgp-adapter-ws-smartmetering/rest/v3/api-docs` (the raw OpenAPI doc is served correctly). Minor WS3 follow-up.
- **Integration-tests via `mvn verify`:** validation was run through the CI-equivalent GitOps Cucumber job against the live k3d stack (same test suite the `cucumber` CI job runs), and inspected from `cucumber.json`.

## Evidence artifacts

- `deploy/local/README.md` — full build/deploy/validate runbook
- Cucumber report: `cucumber.json` + HTML (extracted from the report volume) — 456/456
- Screenshots: journey UI, Swagger UI, Cucumber summary (above)
- Screen recording of the journeys (attached to the delivery message)
