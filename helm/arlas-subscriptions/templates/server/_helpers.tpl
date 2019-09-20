{{- define "arlas-subscriptions.server.environmentVariables" -}}
    {{- range $key,$value := .Values.server.environmentVariables }}
- name: {{ $key | quote }}
  value: {{ $value | quote }}
    {{- end -}}
{{- end -}}

{{- define "arlas-subscriptions.server.endpoint" -}}
http://{{ include "arlas-subscriptions.server.service.name" . }}:{{ .Values.server.service.port }}
{{- end -}}

{{- define "arlas-subscriptions.server.basePath" -}}
{{ include "arlas-subscriptions.server.endpoint" . }}/{{ trimAll "/" .Values.server.environmentVariables.ARLAS_PREFIX }}
{{- end -}}
