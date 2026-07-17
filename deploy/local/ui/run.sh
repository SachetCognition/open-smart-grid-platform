#!/usr/bin/env bash
# SPDX-FileCopyrightText: Copyright Contributors to the GXF project
# SPDX-License-Identifier: Apache-2.0
#
# Starts port-forwards to the locally deployed GXF stack and launches the
# journeys UI (backend-for-frontend + single-page app) on http://localhost:8500.
#
# Prerequisites:
#   * A running k3d cluster with the GXF platform deployed (see this repo's
#     deploy/local/README.md).
#   * KUBECONFIG pointing at that cluster.
#   * Organisation client cert extracted to ${UI_CERTS_DIR} (test-org-cert.pem,
#     test-org-key.pem) — see README "UI credentials".
#   * Python 3.11+ with the packages in requirements.txt installed.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UI_CERTS_DIR="${UI_CERTS_DIR:-/home/ubuntu/ui-certs}"
PORT="${UI_PORT:-8500}"

echo "Starting port-forwards…"
kubectl port-forward deploy/osgp-adapter-ws-smartmetering 18080:8080 >/tmp/pf-sm-rest.log 2>&1 &
kubectl port-forward svc/httpd 18443:443 >/tmp/pf-httpd.log 2>&1 &
kubectl port-forward deploy/osgp-core 18091:8080 >/tmp/pf-core.log 2>&1 &
kubectl port-forward deploy/osgp-adapter-domain-tariffswitching 18092:8080 >/tmp/pf-domain-ts.log 2>&1 &
kubectl port-forward deploy/osgp-adapter-ws-tariffswitching 18093:8081 >/tmp/pf-ws-ts.log 2>&1 &
sleep 5

export CLIENT_CERT="${UI_CERTS_DIR}/test-org-cert.pem"
export CLIENT_KEY="${UI_CERTS_DIR}/test-org-key.pem"

echo "UI available at http://localhost:${PORT}"
cd "${HERE}"
exec uvicorn backend.app:app --host 0.0.0.0 --port "${PORT}"
