#!/bin/bash
JAR_NAME="kafka-monitor-web"

cd $(dirname $0) || exit 1
cd ..
DEPLOY_DIR=$(pwd)
LIB_DIR=$DEPLOY_DIR/lib
JAR_FULL_NAME=
files=$(ls $LIB_DIR)
for filename in $files; do
  if [[ "$filename" =~ ^${JAR_NAME}-.* ]]; then
    JAR_FULL_NAME=$filename
    break
  fi
done

PID=$(ps -ef | grep $JAR_FULL_NAME | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
  echo "ERROR: The $JAR_NAME does not started!"
  exit 1
fi

kill $PID >/dev/null 2>&1

echo "Stopping the $JAR_NAME"

COUNT=0
while [ $COUNT -lt 1 ]; do
  echo -e ".\c"
  sleep 1
  COUNT=1
  PID_EXIST=$(ps --no-heading -p $PID)
  if [ -n "$PID_EXIST" ]; then
    COUNT=0
    break
  fi
done

echo -e "\nStop $JAR_NAME successed with PID=$PID"
