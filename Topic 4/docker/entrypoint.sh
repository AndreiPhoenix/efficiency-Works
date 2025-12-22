#!/bin/sh
# entrypoint.sh

echo "=== Starting Currency Converter ==="
echo "Java version:"
java -version

echo -e "\n=== JVM Settings ==="
echo "Max RAM percentage: ${MAX_RAM_PERCENTAGE:-75}"
echo "GC Type: ${GC_TYPE:-G1}"
echo "Max GC Pause: ${MAX_GC_PAUSE:-200}"

# Собираем JVM опции
JAVA_OPTS="${JAVA_OPTS} \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=${MAX_RAM_PERCENTAGE:-75} \
    -XX:+Use${GC_TYPE:-G1}GC \
    -XX:MaxGCPauseMillis=${MAX_GC_PAUSE:-200} \
    -XX:ParallelGCThreads=${PARALLEL_GC_THREADS:-2} \
    -XX:ConcGCThreads=${CONC_GC_THREADS:-2} \
    -Xlog:gc*,gc+age=trace,safepoint:file=/tmp/gc.log:time,level,tags:filecount=5,filesize=10m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker}"

# JMX только если явно включено
if [ "$ENABLE_JMX" = "true" ]; then
    JAVA_OPTS="${JAVA_OPTS} \
        -Dcom.sun.management.jmxremote \
        -Dcom.sun.management.jmxremote.port=9090 \
        -Dcom.sun.management.jmxremote.rmi.port=9090 \
        -Dcom.sun.management.jmxremote.ssl=false \
        -Dcom.sun.management.jmxremote.authenticate=false \
        -Djava.rmi.server.hostname=0.0.0.0"
fi

echo -e "\n=== Final JVM Options ==="
echo "${JAVA_OPTS}"

echo -e "\n=== Starting Application ==="
exec java ${JAVA_OPTS} -jar app.jar