# TODO: make this script use all nodes instead of the 1st node. Currently, it fails if 1st node is down.

{{- define "arlas-subscriptions.matcher.init.serverCollection" -}}
set -o errexit -o nounset -o pipefail

if [[ -v DEBUG ]] && [[ "$DEBUG" == true ]]; then
  set -x
fi

# Wait for elasticsearch index to have been created
while rm --force /tmp/response &&
  curl --dump-header - --show-error --silent --write-out "\n" "$ELASTICSEARCH_FIRST_NODE/$ELASTICSEARCH_INDEX" | tee /tmp/response &&
  ! grep --fixed-strings "HTTP/1.1 200" /tmp/response; do

  echo "Waiting for \"$ELASTICSEARCH_FIRST_NODE/$ELASTICSEARCH_INDEX\""
  sleep 5

done

# Wait for server
while rm --force /tmp/response &&
  curl --dump-header - --show-error --silent --write-out "\n" "$ARLAS_SUBSCRIPTIONS_ENDPOINT/admin/healthcheck" | tee /tmp/response &&
  ! grep --fixed-strings "HTTP/1.1 200" /tmp/response; do

  echo "Waiting for \"$ARLAS_SUBSCRIPTIONS_ENDPOINT/admin/healthcheck\""
  sleep 5

done

rm --force /tmp/response

# GET collection
curl \
  --dump-header - \
  --show-error \
  --silent \
  --write-out "\n" \
  "$ARLAS_SUBSCRIPTIONS_BASE_PATH/collections/$ARLAS_SUBSCRIPTIONS_COLLECTION_NAME" \
  | tee /tmp/response

# Collection does not exist
if grep --fixed-strings "HTTP/1.1 404" /tmp/response; then
  
  rm --force /tmp/response

  # TODO: variabilize
  # PUT collection
  curl \
    --data @<(cat <<EOF
{
  "index_name": "$ELASTICSEARCH_INDEX",
  "type_name": "$ELASTICSEARCH_MAPPING_TYPE",
  "id_path": "$ARLAS_SUBSCRIPTIONS_COLLECTION_ID_PATH",
  "geometry_path": "$ARLAS_SUBSCRIPTIONS_COLLECTION_GEOMETRY_PATH",
  "centroid_path": "$ARLAS_SUBSCRIPTIONS_COLLECTION_CENTROID_PATH",
  "timestamp_path": "$ARLAS_SUBSCRIPTIONS_COLLECTION_TIMESTAMP_PATH"
}
EOF
    ) \
    --dump-header - \
    --header "content-type: application/json" \
    --request PUT \
    --show-error \
    --silent \
    --write-out "\n" "$ARLAS_SUBSCRIPTIONS_BASE_PATH/collections/$ARLAS_SUBSCRIPTIONS_COLLECTION_NAME" \
    | tee /tmp/response

  # Assert success of PUT request
  grep --fixed-strings "HTTP/1.1 200" /tmp/response

# Collection already exists
elif grep --fixed-strings "HTTP/1.1 200" /tmp/response; then
  exit 0

# Error
else
  exit 1

fi

{{- end -}}
