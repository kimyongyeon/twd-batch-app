ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}/infra/openjdk8-utf8:0.1
VOLUME /tmp

ARG VERSION

ADD ./target/app.jar /app.jar

COPY prometheus/jmx_prometheus.yml /prometheus/jmx_prometheus.yml
COPY prometheus/jmx_prometheus_javaagent-0.3.1.jar /prometheus/jmx_prometheus_javaagent-0.3.1.jar

ARG BUILD_ENV
ADD webt/webt-${BUILD_ENV}.properties /webt.properties

ENTRYPOINT java -server -Xmx2048m -Xms2048m -XX:NewSize=768m -XX:MaxNewSize=768m -XX:MaxMetaspaceSize=256m -XX:MetaspaceSize=256m -Dmtrace_spec=${VERSION} \
            -javaagent:/prometheus/jmx_prometheus_javaagent-0.3.1.jar=8090:/prometheus/jmx_prometheus.yml \
            -XX:ParallelGCThreads=2 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Xloggc:/Users/ihwan/logs/applog/gclog/`hostname`_gc_`date "+%Y%m%d-%H%M%S"`.log -Dgclog_file=/applog/gclog/`hostname`_gc_`date "+%Y%m%d-%H%M%S"`.log \
            -XX:+DisableExplicitGC -XX:SurvivorRatio=32 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelOldGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/applog/heapdump