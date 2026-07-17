{{/*
SPDX-FileCopyrightText: Contributors to the GXF project

SPDX-License-Identifier: Apache-2.0
*/}}

{{- define "gxf.labels" -}}
app.kubernetes.io/part-of: gxf
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end -}}

{{/*
Full image reference for a platform image name.
Usage: {{ include "gxf.image" (dict "root" $ "image" "osgp-core") }}
*/}}
{{- define "gxf.image" -}}
{{- $root := .root -}}
{{ $root.Values.image.registry }}/{{ .image }}:{{ $root.Values.image.tag }}
{{- end -}}
