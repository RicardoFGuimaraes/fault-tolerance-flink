1. Stop the current Minikube cluster (optional, if running):
```bash
   minikube stop
```
2. Delete the Minikube cluster:
   This will remove all data and settings from the current Minikube environment.
```bash
   minikube delete
```
3. You can specify additional configurations like memory, CPUs, or Kubernetes version, if needed. For example:
```bash
   minikube start --cpus=4 --memory=8192
```
4. To list all running pods:
```bash
   kubectl get pods --all-namespaces
```
5. Apply the config/create de pods from `flink-deploy.yaml`:
```bash
   kubectl apply -f flink-pv.yaml
   kubectl apply -f flink-pvc.yaml
   kubectl apply -f flink-deploy.yaml
```
6. Port-forwarding, so you can access Flink UI via `localhost:8081`:
```bash
   kubectl port-forward svc/jobmanager 8081:8081
```
7. To send the load, you must log in the jobmanager pod:
```bash
   kubectl exec -it < pod-complete-name > -- /bin/bash
   flink run -d -m localhost:8081 -p 6 ./examples/streaming/TopSpeedWindowing.jar
```

7 To send the load, you must log in the jobmanager pod:
```bash
  --app wordcount --config metrics.onlySink=false,wc.splitter.threads=2,wc.source.path=/opt/flink/book.dat,wc.parser.threads=1,metrics.enabled=true,wc.counter.threads=1,metrics.output=/metrics/stream/WC/,wc.kafka.source.topic=books,wc.kafka.zookeeper.host=10.32.45.44:9092,wc.sink.threads=1,wc.source.class=flink.source.FileSource,wc.source.threads=1,wc.sink.class=flink.sink.ConsoleSink,metrics.interval.unit=seconds,wc.runtime_sec=60
```
