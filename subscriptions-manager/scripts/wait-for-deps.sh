#!/bin/bash

################################################################################
# Functions
################################################################################

wait_for_hosts () {
  for host in ${@} ; do

  # Split "host" into "hostname" & "port" using bash parameter expansion
  hostname=${host%%:*}
  port=${host##*:}

  i=1; until nc -w 2 $hostname $port; do if [ $i -lt 100 ]; then sleep 1; else break; fi; i=$(($i + 1)); echo `date +%FT%T`' Trying to connect to '$hostname:$port; done
  done
}


################################################################################
# Script
################################################################################

set -e -o pipefail

IFS=',' read -r -a mongo_hosts <<< $1
IFS=',' read -r -a es_hosts <<< $2

wait_for_hosts ${mongo_hosts[@]} ${es_hosts[@]}

java -jar /opt/app/subscriptions-manager.jar server /opt/app/configuration-manager.yaml