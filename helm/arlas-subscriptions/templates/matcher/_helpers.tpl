{{- define "arlas-subscriptions.matcher.environmentVariables" -}}
    {{- range $key,$value := .Values.matcher.environmentVariables }}
- name: {{ $key | quote }}
  value: {{ $value | quote }}
    {{- end -}}
{{- end -}}

{{- define "arlas-subscriptions.matcher.init.serverCollection.container.environmentVariables" -}}
    {{- range $key,$value := .Values.matcher.init.serverCollection.container.environmentVariables }}
- name: {{ $key | quote }}
  value: {{ $value | quote }}
    {{- end -}}
{{- end -}}
