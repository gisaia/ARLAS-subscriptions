# TODO: make this script use all nodes instead of the 1st node. Currently, it fails if 1st node is down.

{{- define "arlas-subscriptions.manager.init.elasticsearchIndex" -}}
set -o errexit -o nounset -o pipefail

# Wait for readiness
while rm --force /tmp/response &&
  curl --dump-header - --show-error --silent --write-out "\n" "$ELASTICSEARCH_FIRST_NODE/_cluster/health" | tee /tmp/response &&
  ! grep --fixed-strings "HTTP/1.1 200" /tmp/response; do

  echo "Waiting for \"$ELASTICSEARCH_FIRST_NODE\""
  sleep 5

done

rm --force /tmp/response

# GET index
curl --dump-header - --show-error --silent --write-out "\n" "$ELASTICSEARCH_FIRST_NODE/$ELASTICSEARCH_INDEX" | tee /tmp/response

# Index does not exist
if grep --fixed-strings "HTTP/1.1 404" /tmp/response; then

  rm --force /tmp/response

  # PUT index
  curl --data @<(cat <<EOF
{
  "mappings": {
    "$ELASTICSEARCH_MAPPING_TYPE":
      {{ toPrettyJson .Values.elasticsearch.mappingType.definition | nindent 6 }}
  }
}
EOF
) --dump-header - --header "content-type: application/json" --request PUT --show-error --silent --write-out "\n" "$ELASTICSEARCH_FIRST_NODE/$ELASTICSEARCH_INDEX" | tee /tmp/response

  # Assert success of PUT request
  grep --fixed-strings "HTTP/1.1 200" /tmp/response

# Index already exists, nothing to do
elif grep --fixed-strings "HTTP/1.1 200" /tmp/response; then
  exit 0

# Error
else
  exit 1

fi
{{- end -}}
