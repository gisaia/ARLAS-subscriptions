{{- define "arlas-subscriptions.manager.environmentVariables" -}}
    {{- range $key,$value := .Values.manager.environmentVariables }}
- name: {{ $key | quote }}
  value: {{ $value | quote }}
    {{- end -}}
{{- end -}}

{{- define "arlas-subscriptions.manager.imagePullSecrets" -}}
    {{- range $imagePullSecret := .Values.manager.imagePullSecrets }}
- name: {{ $imagePullSecret }}
    {{- end -}}
{{- end -}}

{{- define "arlas-subscriptions.manager.triggerSchema.configMap" -}}
{{ include "arlas-subscriptions.fullname" . }}-manager-trigger-schema
{{- end -}}
