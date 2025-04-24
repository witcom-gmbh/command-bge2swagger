ARG VARIANT=17-bookworm
FROM mcr.microsoft.com/vscode/devcontainers/java:1-${VARIANT}

# Brute force around certificate-inspection
ARG SKIP_APT_PEER_VERIFY="false"
RUN if [ "${SKIP_APT_PEER_VERIFY}" = "true" ]; then \
    touch /etc/apt/apt.conf.d/99verify-peer.conf && echo >>/etc/apt/apt.conf.d/99verify-peer.conf "Acquire { https::Verify-Peer false }"; \
    fi

# Custom CA integration for Java
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && \
    apt-get -y install ca-certificates-java
    
# Add custom CAs
COPY ./ca-trust/*.crt /usr/local/share/ca-certificates/
RUN update-ca-certificates && \
    # if adding additional JDKs -> repeat
    ln -sf /etc/ssl/certs/java/cacerts /usr/lib/jvm/msopenjdk-current/lib/security/cacerts

# This is needed for extension-installing in vscode
ENV NODE_EXTRA_CA_CERTS=/usr/local/share/ca-certificates/wit-ca-crt-19f321a4661f40884bb8af5146932975.crt
