#!/bin/bash
JAR_NAME="kafka-monitor-web"
Xms=4G
Xmx=4G
Xmn=1G
COMMON_LOG_DIR="/wyyt/logs/dubbo/$JAR_NAME"
if [ ! -d $COMMON_LOG_DIR ]; then
  mkdir -p $COMMON_LOG_DIR
fi
LOGS_HEAPDUMP=$COMMON_LOG_DIR/heapdump
if [ ! -d $LOGS_HEAPDUMP ]; then
  mkdir -p $LOGS_HEAPDUMP
fi
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
jarName=${JAR_FULL_NAME/.jar/}
VERSION=${jarName##*-}
JAR_DIR=$LIB_DIR/$JAR_FULL_NAME

PID=$(ps -ef | grep $JAR_FULL_NAME | grep -v grep | awk '{print $2}')

if [ "$1" = "status" ]; then
  if [ -n "$PID" ]; then
    echo "The $JAR_NAME is running...! PID: $PID"
    exit 0
  else
    echo "The $JAR_NAME is stopped"
    exit 0
  fi
fi

if [ -n "${PID}" ]; then
  echo "ERROR: The $JAR_NAME already started! PID: $PID"
  exit 1
fi

JAVA_OPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_GC="-Xloggc:$COMMON_LOG_DIR/$JAR_NAME-gc.log -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=10M "

JAVA_MEM_OPTS="-server -Xms${Xms} -Xmx${Xmx} -Xmn${Xmn} -XX:NewRatio=1 -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
JAVA_MEM_OPTS="$JAVA_MEM_OPTS -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:MaxDirectMemorySize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$LOGS_HEAPDUMP/ "
JAVA_PARAMETER_OPTS="-Dversion=$VERSION -Dwork.dir=$DEPLOY_DIR"

SKYWALKING_COMMON_LOG_DIR="$COMMON_LOG_DIR/skywalking/"
SKYWALKING_SERVICE_NAME="-Dskywalking.agent.service_name=$JAR_NAME"
SKYWALKING_AGENT_HOME="/wyyt/app/skywalking/agent"
SKYWALKING_AGENT="-javaagent:$SKYWALKING_AGENT_HOME/skywalking-agent.jar "
SKYWALKING_LOGGING_DIR="-Dskywalking.logging.dir=$SKYWALKING_COMMON_LOG_DIR"
SKYWALKING_AGENT_CONFIG=" -Dskywalking_config=$SKYWALKING_AGENT_HOME/config/agent.config "
SKYWALKING_AGENT_OPTS="$SKYWALKING_AGENT $SKYWALKING_LOGGING_DIR $SKYWALKING_SERVICE_NAME $SKYWALKING_AGENT_CONFIG "
SKYFOLDER="/wyyt/app/skywalking/agent"
if [ -d "$SKYFOLDER" ]; then
  echo "java $JAVA_OPTS $JAVA_GC $SKYWALKING_AGENT_OPTS $JAVA_MEM_OPTS $JAVA_PARAMETER_OPTS -jar $JAR_DIR"
  nohup java $JAVA_OPTS $JAVA_GC $SKYWALKING_AGENT_OPTS $JAVA_MEM_OPTS $JAVA_PARAMETER_OPTS -jar $JAR_DIR >/dev/null 2>&1 &
else
  echo "java $JAVA_OPTS $JAVA_GC $JAVA_MEM_OPTS $JAVA_PARAMETER_OPTS -jar $JAR_DIR"
  nohup java $JAVA_OPTS $JAVA_GC $JAVA_MEM_OPTS $JAVA_PARAMETER_OPTS -jar $JAR_DIR >/dev/null 2>&1 &
fi

echo "Starting the $JAR_NAME"

COUNT=0
while [ $COUNT -lt 1 ]; do
  echo -e ".\c"
  sleep 1
  COUNT=$(ps -ef | grep $JAR_FULL_NAME | grep -v grep | awk '{print $2}' | wc -l)
  if [ $COUNT -gt 0 ]; then
    break
  fi
done

echo -e "\nStart $JAR_NAME successed with PID=$!"
