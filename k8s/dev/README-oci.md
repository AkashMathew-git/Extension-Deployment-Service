## Run Thidwick-starter on OCI
###Instrument It With Prometheus and Grafana, Performance Test It With Locustio

This project enables you to create and monitor a load on the thidwick-starter microservice, all running under Kubernetes on OCI.

Prerequisites: 
- You can run kubectl on the OSvC OCI Kubernetes clusters.
- You can run kubectl.
- You can run Kubernetes Dashboard.
- You have a namespace in the OCI Kubernetes cluster in which you are authorized to deploy Helm charts.
- Your namespace has the ocirsecret installed.
- OSvC Kubernetes/Helm code repo downloaded from orahub: osvc-cloud-ms/osvc-helm.


#### Start thidwick-starter under kubernetes
**Set working directory**
```
$ cd your-path-to/appdev-cloud-rnpd/thidwick-starter/k8s/dev/
```
**Start Thidwick-Starter**
In the following command, we use a standard helm chart with a local values file.
```
$ helm install your-path-to/osvc-cloud-ms/osvc-helm/incubator/thidwick \
--name thidwick \
--namespace your-namespace
``` 

**Verify Results** 
Review the installation using Kubernetes Dashboard.
Command line equivalent:
```$xslt
$ kubectl get all -n your-namespace
```
Expected results: you should see a deployment, pod, replica set, and service, all named (or starting with) _thidwick-thidwick-starter_.

**Set up access to Thidwick-starter URL**
Follow the instructions under Notes on the output from the _helm install_ command to export the pod name and port-forward Thidwick-starter to port 8080.

To see the output again, run
```$xslt
$ helm status thidwick
$ export POD_NAME=$(kubectl get pods --namespace your-namespace -l "app=thidwick-starter,project=thidwick" -o jsonpath="{.items[0].metadata.name}")
$ kubectl --namespace your-namespace port-forward $POD_NAME 8080 &
```

***Verify***

In a browser, open _localhost:8080/hello_.  Expected result: "Hello, my name is starter."

Open _localhost:8080/metrics_.  Expected result: json metrics output.

Open _localhost:8080/metrics/prometheus_.  Expected result: prometheus metrics output.

#### Install Prometheus

Use the given prometheus4thidwick_vaues.yaml file to install a lighter-weight Prometheus.   

Without intervention, the Helm chart for Prometheus will install components that you don't need to get started.  We turn off alertmanager and pushgateway. They will run in pods and consume resources for no purpose.  
Additionally, we override the default value for Prometheus scrape interval.  The default is 60 seconds; we set it to 20.

We set a specific name for the release under Kubernetes. Grafana will use this name to find Prometheus.
The --namespace clause is optional for the local install.  Without it, Helm will install to the _default_ namespace.
```$xslt
$ helm install stable/prometheus \
--values=prometheus4thidwick-values.yaml \
--name prom4thidwick \
--set server.persistentVolume.size=50Gi \
--namespace your-namespace
```

**Set up the Prometheus browser UI**
```$xslt
# Follow the instructions at the end of the output to port-forward the Prometheus browser endpoint.
$ export POD_NAME=$(kubectl get pods --namespace your-namespace -l "app=prometheus,component=server" -o jsonpath="{.items[0].metadata.name}")
$ kubectl --namespace your-namespace port-forward $POD_NAME 9090 &
# Use ampersand at the end of the port-forward command to run it in the background.
# Open the UI on your browser, URL localhost:9090 
```
***Verify***

In a browser, open _localhost:9090_.  Expected result: Prometheus UI, open to the _graph_ page.

Click on the pulldown labelled _insert metric at cursor_.  Expected result:  scroll down to see a bunch of items that start with _HelloWorld_.  These are the Thidwick metrics, scraped from Thidwick-Starter.  (You may need to refresh the page after a 20-second scrape interval has elapsed.)

#### Install Grafana
We use a custom OSvC Helm chart to install Grafana, to overcome various issues with OCI. 
```
$ helm install your-path-to/osvc-cloud-ms/osvc-helm/incubator/grafana \
--name graf4thidwick \
--values grafana-values-oci.yaml \
--namespace your-namespace
```
Follow the instructions in the _NOTES_ at the end of the output to get the admin password, and port-forward the Grafana browser endpoint.

Use ampersand at the end of the port-forward command to run it in the background.

Open the UI on your browser, URL localhost:3000.  Log in using admin/your_secret.  This login will remain active until you uninstall Grafana.

Refer to the instructions in the [main README file](README.md) to configure Grafana and install the Thidwick-Starter dashboard.

#### Prepare and install Locustio.
We have a build of locustio that is configured to make requests against thidwick-starter's /hello endpoint.

**Deploy pre-built Locustio under Kubernetes.**
```
$ helm install your-path-to/osvc-cloud-ms/osvc-helm/incubator/locustio4thidwick-starter \
--name locust4thidwick \
--set master.config.target-host="http://thidwick-thidwick-service:8080" \
--namespace your-namespace
```

**Set up the Locustio browser UI**
```$xslt
$ export POD_NAME=$(kubectl get pods --namespace your-namespace -l "app=locust4thidwick-locust,component=master" -o jsonpath="{.items[0].metadata.name}")
$ kubectl port-forward $POD_NAME 8089 &
# Use ampersand at the end of the port-forward command to run it in the background.
# Open the UI on your browser, URL localhost:8089 
```
**Bring up Locust control page in a browser.**
```
localhost:8089
```
**Verify:** Locust is aimed at host http://thidwick-starter:8080 

Refer to the instructions in the [main README file](README.md) to start and run Locustio.
