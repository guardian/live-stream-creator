#!/bin/bash

# Debug option
DEBUG_PARAMS=""
for arg in "$@"
do
    if [ "$arg" == "--debug" ]; then
      echo "setting java process as debuggable"
      DEBUG_PORT="9100"
      DEBUG_PARAMS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT}"
      shift
    fi
done

SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"

java ${SBT_OPTS} \
  ${DEBUG_PARAMS} \
  -jar `dirname $0`/sbt-launcher.jar "$@"
