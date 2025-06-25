FROM docker.io/bitnami/postgresql:15.4.0 AS build

ENV PLUGIN_VERSION=v2.5.0.Beta1
ENV PROTOC_VERSION=1.4

USER root

# Install the packages which will be required to get everything to compile
RUN apt-get update \
    && apt-get install -f -y --no-install-recommends \
        software-properties-common \
        build-essential \
        pkg-config \
        git \
    && add-apt-repository "deb http://ftp.debian.org/debian testing main contrib" \
    && apt-get update && apt-get install -f -y --no-install-recommends \
        libprotobuf-c-dev=$PROTOC_VERSION.* \
    && rm -rf /var/lib/apt/lists/*

# Compile the plugin from sources and install it
RUN git clone https://github.com/debezium/postgres-decoderbufs -b $PLUGIN_VERSION --single-branch \
    && cd /postgres-decoderbufs \
    && make && make install \
    && cd / \
    && rm -rf postgres-decoderbufs

FROM docker.io/bitnami/postgresql:15.4.0

USER root

LABEL maintainer="Şuayb Şimşek"

COPY --from=build /opt/bitnami/postgresql/lib/decoderbufs.so /opt/bitnami/postgresql/lib/
COPY --from=build /opt/bitnami/postgresql/share/extension/decoderbufs.control /opt/bitnami/postgresql/share/extension
# Copy the custom configuration which will be passed down to the server (using a .sample file is the preferred way of doing it by
# the base Docker image)
COPY postgresql.conf /bitnami/postgresql/conf/conf.d/
COPY extension.sql /docker-entrypoint-initdb.d/
USER 1001