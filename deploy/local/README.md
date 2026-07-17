<!--
SPDX-FileCopyrightText: Copyright Contributors to the GXF project
SPDX-License-Identifier: Apache-2.0
-->
# GXF / Open Smart Grid Platform — Local Deployment & Validation

This directory documents how to build, deploy and validate the **modernized**
Open Smart Grid Platform (GXF) locally, and ships a small **journeys UI**
(`ui/`) that exercises the main flows against the running stack.

It captures the reconvergence of five modernization workstreams:

| WS  | Theme | Summary |
| --- | ----- | ------- |
| WS1 | JMS security + broker | `jms.default.trust.all.packages` now defaults to **false**; the explicit trusted-package whitelist is authoritative. **Artemis** is the default broker (`jms.default.broker.type=ARTEMIS`); ActiveMQ Classic remains available for rollback. |
| WS3 | REST/JSON facade | Additive springdoc-based REST facade on smart metering (`/rest/...`), running side-by-side with the unchanged SOAP endpoints. |
| WS4 | Build/release + config cleanup | Removed the abandoned `jgitflow-maven-plugin`; migrated `applicationContext.xml` (ws-core) to Java config; recorded a dependency/EOL audit. |
| WS5 | Shared SOAP config | Extracted repeated marshaller/interceptor wiring into `osgp-adapter-ws-shared`. |
| WS6 | Cloud-native | Representative modules converted to self-contained Spring Boot executables with Actuator liveness/readiness; JNDI/`context.xml` replaced by env/ConfigMap-driven config. |

> WS2 (Joda-Time → `java.time`) is tracked **separately**, outside this migration.

## 1. Prerequisites

| Tool | Version used |
| ---- | ------------ |
| JDK  | Temurin/OpenJDK **17** (`JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`) |
| Maven | wrapper `./mvnw` (3.9.9) |
| Docker | 27.x |
| k3d | 5.9.0 (k3s v1.31.5) |
| kubectl | 1.3x |
| Helm | 3.x |
| Python | 3.11+ (for the UI) |

Deployment orchestration lives in the external repo **`OSGP/gxf-gitops`**
(Helm charts + `setup.sh`). Clone it next to this repo.

## 2. Build the project

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./mvnw -T1C -B -Ppl,sm install -DskipTestJarWithDependenciesAssembly=false
```

## 3. Build the Docker images

Each deployable module has a `Dockerfile`. Build and tag them (here with tag
`recon`) and import them into the k3d cluster:

```bash
# example for one module; repeat for every image in .github/workflows/build.yml
docker build -t ghcr.io/osgp/osgp-core:recon \
  osgp/platform/osgp-core
...
k3d image import ghcr.io/osgp/<image>:recon -c test
```

A helper that builds every image used by this deployment is kept at
`build-recon-images.sh` (outside the repo tree, environment-specific).

The WS6 modules (`osgp-core`, `osgp-adapter-domain-tariffswitching`,
`osgp-adapter-ws-tariffswitching`) now run **fat jars / executable WARs** on
`eclipse-temurin:17-jre` with an Actuator-based `HEALTHCHECK`, replacing the
previous `tomcat:10.1` + `probe.txt` pattern.

## 4. Create the k3d cluster

In a nested/virtualized environment the default flannel VXLAN backend fails
(`operation not supported`). Use the **host-gw** backend:

```bash
k3d cluster create test \
  --image rancher/k3s:v1.31.5-k3s1 \
  --volume /tmp/k3dvolume:/k3d/pv \
  --k3s-arg "--flannel-backend=host-gw@server:*"

export KUBECONFIG=$(k3d kubeconfig write test)
```

## 5. Generate secrets and deploy the platform

```bash
cd gxf-gitops
bash setup.sh                      # generates certs/keys as k8s secrets
# (if the API server is briefly unready, re-run the secret step)

./charts/gxf-platform/template-apply.sh --imageTag recon --valuesFile values-recon.yaml
```

`values-recon.yaml` (added for this deployment) pins `imageTag: recon`,
`imagePullPolicy: IfNotPresent`, WS6 readiness settings, per-service memory
(e.g. `osgp-protocol-adapter-iec61850: 1024Mi`) and the smart-metering WS
service + RSA key mount.

Broker URLs in `charts/gxf-platform/templates/config.yaml` use the
**Artemis** form `tcp://activemq:61617` (not the ActiveMQ-Classic
`failover:(ssl://...)` form).

Wait for all platform pods to be `1/1 Ready`:

```bash
kubectl get pods
```

## 6. Run the Cucumber validation

The CI `cucumber` flow is reproduced with the test-suite chart:

```bash
./charts/gxf-cucumber-tests/template-apply.sh --valuesFile ci/common-values.yaml
# then
./charts/gxf-cucumber-tests/template-apply.sh --valuesFile ci/publiclighting-values.yaml
```

Each run is a Kubernetes Job; `STATUS=Complete` means the failsafe build
exited 0 (all scenarios passed). Reports are written to the `report-volume`
PVC (Cucumber HTML/JSON/JUnit XML).

Alternatively, from a machine with network access to the stack and the org
client certs configured:

```bash
cd integration-tests
mvn verify -DskipITs=false
```

## 7. Runtime verification checklist

```bash
# Actuator liveness/readiness (WS6 services)
kubectl exec deploy/osgp-core -- \
  curl -s localhost:8080/actuator/health/readiness    # {"status":"UP"}

# Broker = Artemis, TLS
kubectl logs deploy/osgp-core | grep JmsBrokerArtemis  # tcp://activemq:61617

# trustAllPackages default (code)
grep trust.all.packages \
  ../open-smart-grid-platform/osgp/shared/shared/src/main/java/org/opensmartgridplatform/shared/application/config/messaging/DefaultJmsConfiguration.java
# -> ${jms.default.trust.all.packages:false}
```

## 8. The journeys UI

```bash
cd deploy/local/ui
pip install -r requirements.txt
UI_CERTS_DIR=/path/to/org-certs ./run.sh      # http://localhost:8500
```

The UI drives three journeys and a live Actuator health dashboard:

1. **Smart Metering GetActualMeterReads (REST/JSON, WS3)** — POST returns
   HTTP 202 + `correlationUid`; GET returns 200 with the result or 202
   `PENDING`.
2. **Public Lighting SetLight (SOAP, mTLS)** — the preserved SOAP endpoint.
3. **Smart Metering GetActualMeterReads (SOAP, mTLS)** — the preserved SOAP
   endpoint, side-by-side with REST.

### UI credentials

The SOAP journeys use the organisation client certificate (mTLS via `httpd`).
Extract `test-org`'s cert from the k8s secret created by `setup.sh`:

```bash
kubectl get secret test-org-cert -o jsonpath='{.data.test-org\.pfx}' | base64 -d > test-org.pfx
PW=$(kubectl get secret organisations-ws-client-certs -o jsonpath='{.data.keystore-password}' | base64 -d)
openssl pkcs12 -in test-org.pfx -clcerts -nokeys -passin pass:"$PW" -out test-org-cert.pem
openssl pkcs12 -in test-org.pfx -nocerts  -nodes  -passin pass:"$PW" -out test-org-key.pem
```

Point `run.sh` at that directory with `UI_CERTS_DIR`.

## 9. Before / after modernization

| Area | Before | After |
| ---- | ------ | ----- |
| JMS deserialization | `trustAllPackages=true` by default (accepts any class) | **`false`** by default; explicit trusted-package whitelist authoritative |
| Message broker default | ActiveMQ (classic-style config) | **Artemis** default; Classic retained behind config for rollback |
| Adapter redelivery config | IEC60870/IEC61850 called the ActiveMQ-Classic `RedeliveryPolicy` (unsupported by Artemis) | Broker-agnostic `getMaxRedeliveries()` — works under Artemis |
| Smart-metering API | SOAP only | SOAP **+** REST/JSON with OpenAPI (`/rest/v3/api-docs`, Swagger UI) |
| Release plugin | abandoned `jgitflow-maven-plugin:1.0-m5.1` | removed; GitHub Actions release flow + `RELEASE.md` guidance |
| ws-core XML config | `applicationContext.xml` | Java `@Configuration` (`WebServiceEndpointConfig`) |
| SOAP wiring | marshaller/interceptors duplicated across 5 adapters | shared `osgp-adapter-ws-shared` config |
| Packaging | WAR-in-Tomcat + `probe.txt` health file | self-contained Spring Boot executables + Actuator liveness/readiness |
| App config | Tomcat `context.xml` JNDI | env vars / ConfigMaps / Secrets |

## 10. Known limitations

* **Smart-metering DLMS chain.** `GetActualMeterReads` over SOAP/REST completes
  end-to-end only when the full DLMS chain (domain-smartmetering +
  protocol-adapter-dlms + DLMS device simulator) is deployed and a smart-meter
  device is provisioned. The recon deployment focuses on the messaging/broker,
  REST facade, public-lighting `SetLight` (validated end-to-end via Cucumber)
  and Actuator/health surfaces; where the DLMS chain is not deployed the REST
  GET correctly returns `202 PENDING`.
* **WS6 scope.** Three representative modules were converted to Spring Boot
  executables. `httpd` still proxies SOAP to the non-converted adapters over
  AJP; the converted `osgp-adapter-ws-tariffswitching` exposes HTTP/Actuator
  directly.
* **UI SOAP TLS.** The UI backend presents the org client cert for mTLS but
  does not verify the `httpd` server certificate hostname when reached through
  a `localhost` port-forward.
