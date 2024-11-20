# Setup Guide: Flink with Pumba, Prometheus, and Grafana
## Step 1: Install Docker
```sh
sudo apt-get update
sudo apt-get install docker.io
```
## Step 2: Create Docker Network
```sh
docker network create flink-network
```
## Step 3: Start Flink JobManager and TaskManagers
```sh
docker run -d --name jobmanager --network flink-network --publish 8081:8081 --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:latest jobmanager
docker run -d --name taskmanager1 --network flink-network --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:latest taskmanager
docker run -d --name taskmanager2 --network flink-network --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:latest taskmanager
docker run -d --name taskmanager3 --network flink-network --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" flink:latest taskmanager
```
## Step 4: Configure Flink Metrics
Add the following lines to the Flink configuration file (flink-conf.yaml):

```yaml
metrics.reporters: prom
metrics.reporter.prom.class: org.apache.flink.metrics.prometheus.PrometheusReporter
metrics.reporter.prom.port: 9250
metrics.scope.jm: jobmanager
metrics.scope.tm: taskmanager
```
## Step 5: Set Up Prometheus
Create Prometheus Configuration (prometheus.yml):

```yaml
global:
scrape_interval: 15s

scrape_configs:
- job_name: 'flink'
  static_configs:
    - targets: ['jobmanager:9250', 'taskmanager1:9250', 'taskmanager2:9250', 'taskmanager3:9250']
```
Run Prometheus Docker Container:

```sh
docker run -d --name prometheus --network flink-network -p 9090:9090 -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```
## Step 6: Set Up Grafana
Run Grafana Docker Container:

```sh
docker run -d --name=grafana --network=flink-network -p 3000:3000 grafana/grafana
```
Configure Grafana:

Open Grafana at http://localhost:3000 (default credentials: admin/admin).

Add Prometheus as a data source (URL: http://prometheus:9090).

Create dashboards to visualize Flink metrics.

## Step 7: Install Pumba
```sh
curl -fsSL https://get.pumba.io | sh
```
## Step 8: Write Chaos Experiment Script
Create a script (chaos_experiment.sh) to kill and restart Flink TaskManagers:

```sh
#!/bin/bash

# Define the container names
CONTAINER_NAMES=("taskmanager1" "taskmanager2" "taskmanager3")

# Function to kill a random container
kill_container() {
    CONTAINER=$(echo ${CONTAINER_NAMES[@]} | tr ' ' '\n' | shuf -n 1)
    pumba kill --signal SIGKILL $CONTAINER
}

# Function to restart a container
restart_container() {
    CONTAINER=$(echo ${CONTAINER_NAMES[@]} | tr ' ' '\n' | shuf -n 1)
    docker start $CONTAINER
}

# Main loop
while true; do
    kill_container
    sleep 120  # Wait for 2 minutes
    restart_container
    sleep 60  # Wait for 1 minute to stabilize
done
```
## Step 9: Run Chaos Experiment
Execute the chaos experiment script:

```sh
nohup ./chaos_experiment.sh &
```
## Step 10: Monitor and Analyze
Monitor Flink Web UI: Access at http://localhost:8081.

Use Grafana: Access at http://localhost:3000 to monitor performance metrics.

This setup integrates Flink, Pumba, Prometheus, and Grafana, providing a comprehensive environment for benchmarking and chaos testing. You're all set!