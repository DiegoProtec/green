{{/*
DEPLOYMENT< */}}

{{- define "recreate" -}}
strategy:
  type: Recreate
{{- end }}

{{- define "rollingUpdate" -}}
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: {{ default 1 .surge }}
    maxUnavailable: {{ default 0 .unavailable }}
{{- end }}

{{/*
deployment-strategy */}}
{{- define "strategy" -}}
{{- if and .Values.app.strategy.type }}
{{- $strategy := .Values.app.strategy }}
{{- if eq ($strategy.type | upper) "RECREATE" }}
{{- include "recreate" . }}
{{- end }}
{{- if hasPrefix ($strategy.type | upper) "ROLLING" }}
{{- include "rollingUpdate" $strategy }}
{{- end }}
{{- end }}
{{- end }}

{{/*
deployment-replicas */}}
{{- define "replicas" -}}
{{ default 1 .Values.app.replicas }}
{{- if and .Values.app.strategy.type (ne .Values.app.strategy.type "none") }}
{{- include "strategy" . | nindent 2 }}
{{- end }}
{{- end }}

{{/*
>DEPLOYMENT */}}