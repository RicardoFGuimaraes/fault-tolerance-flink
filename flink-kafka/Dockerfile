# Dockerfile
FROM flink:1.19.1

# Run as root for setting up directory and permissions
USER root

# Create directory and set permissions
RUN mkdir -p /metrics/stream/WC && chmod 777 /metrics/stream/WC

# Switch back to the default user if needed
USER flink

