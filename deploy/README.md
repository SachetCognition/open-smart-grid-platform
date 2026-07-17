<!--
SPDX-FileCopyrightText: Contributors to the GXF project

SPDX-License-Identifier: Apache-2.0
-->

# In-repo deployment manifests (WS6)

This directory contains declarative, in-repo deployment manifests for the GXF / OSGP platform, so
that orchestration is versioned together with the code. It mirrors the layout used by the external
[`OSGP/gxf-gitops`](https://github.com/OSGP/gxf-gitops) repository (Postgres, an Artemis broker,
`osgp-core`, ws/domain adapters and a protocol adapter + simulator).

## Layout

```
deploy/
  helm/gxf/
    Chart.yaml
    values.yaml
    templates/
      _helpers.tpl
      config.yaml              # ConfigMap (global.properties) + Secret (DB / Artemis credentials)
      postgres.yaml            # PostgreSQL Deployment, Service, PVC, DB init
      artemis.yaml             # ActiveMQ Artemis broker Deployment + Service
      platform-services.yaml   # WS6 Spring Boot services: osgp-core, ws + domain adapters
      protocol-adapter.yaml    # OSLP protocol adapter + web device simulator
```

## Configuration: from Tomcat JNDI to ConfigMap + Secret

The WS6 Spring Boot conversion removes the per-module Tomcat `context.xml` files whose JNDI
`<Environment>` entries used to bind the external config files (e.g. `/etc/osgp/global.properties`).
Instead:

- Each converted app declares `spring.config.import` for `optional:file:/etc/osgp/global.properties`
  (and its module-specific properties) in `src/main/resources/application.yml`.
- `config.yaml` renders a **ConfigMap** into `global.properties` and mounts it at `/etc/osgp` in every
  app pod — replacing the JNDI binding.
- Database and broker passwords live in a Kubernetes **Secret** and are injected as the `DB_PASSWORD`
  / `ARTEMIS_PASSWORD` environment variables. Spring relaxed binding resolves `${db.password}` from
  `DB_PASSWORD`, so no secret value is ever committed or baked into an image.

Provide a real password at install time (do **not** use the demo default) via
`--set database.password=...`, or point the chart at an externally managed Secret.

## Health probes

The converted services expose Spring Boot Actuator Kubernetes probes, which the Deployments wire up:

- liveness: `GET /actuator/health/liveness`
- readiness: `GET /actuator/health/readiness`

For `osgp-adapter-ws-tariffswitching` these run on the dedicated management port `8081` (its SOAP
`MessageDispatcherServlet` is mapped to `/*` on `8080`). The non-converted protocol adapter and
simulator fall back to a TCP readiness check.

## Usage

```bash
# Render manifests
helm template gxf deploy/helm/gxf

# Install (set a real DB password)
helm install gxf deploy/helm/gxf --set database.password="$DB_PASSWORD"
```

Images default to `ghcr.io/osgp/<module>:latest`; override `image.registry` / `image.tag` to point at
your own registry (for local k3d, load the images you `docker build` from the converted modules).
