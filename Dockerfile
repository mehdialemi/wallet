FROM java:8-jdk-alpine
COPY build/install/wallet/ /opt/wallet
EXPOSE 8080
ENTRYPOINT ["/opt/wallet/bin/wallet-server"]
