#### Build and Start Thidwick-starter

##### Configure docker for rncs repository
In your Docker preferences, go to the Daemon|Basic screen and add the following as an insecure registry:
```
rncs-docker-local.docker.oraclecorp.com
```
##### Build thidwick-starter docker image on your local repository.
To pull the base image from rncs-docker-local.docker.oraclecorp.com, you many need to change your Docker configuration from Manual proxy to System proxy, for this step only.
For Manual proxy configuration, use www-proxy-hqdc.us.oracle.com:80 for both http and https proxy URL.

```
$ ./gradlew clean build distDocker
```
***Verify***
You have a new docker repository "localhost:5000/osvc/thidwick-starter", tag "latest".
```
$ docker images
# Verify proper image exists.
```
#### Start thidwick-starter under kubernetes
**Set working directory**
```
$ cd your-path-to/appdev-cloud-rnpd/thidwick-starter/k8s/dev/
```
**Start Thidwick-Starter**
In the following command, we use a standard helm chart with a local values file.
The --namespace clause is optional for the local install.  Without it, Helm will install to the _default_ namespace.
```
$ helm install your-path-to/osvc-cloud-ms/osvc-helm/incubator/thidwick \
--values thidwick-starter-values-local.yaml \
--name thidwick \
--namespace your-namespace
```
**Verify Results** 
Review the installation using Kubernetes Dashboard.
Command line equivalent:
```$xslt
$ kubectl get all [-n your-namespace]
```
Expected results: you should see a deployment, pod, replica set, and service, all named (or starting with) _thidwick-thidwick-starter_.

**Set up access to Thidwick-starter URL**
Follow the instructions under Notes on the output from the _helm install_ command to export the pod name and port-forward Thidwick-starter to port 8080.

To see the output again, run
```$xslt
$ helm status thidwick
$ export POD_NAME=$(kubectl get pods --namespace your-namespace -l "app=thidwick-starter,project=thidwick" -o jsonpath="{.items[0].metadata.name}")
$ kubectl --namespace default port-forward $POD_NAME 8080 &
```

***Best Practice***
It is easy to get confused when forwarding multiple ports from Kubernetes.
When running the _kubectl port-forward_ command, finish the command with an **ampersand** to run it in the background. 
Run all of your port-forward commands this way from a single terminal.  To see which ports are forwarded, run
```$xslt
$ jobs
```
***Verify***

In a browser, open _localhost:8080/hello_.  Expected result: "Hello, my name is starter."

Open _localhost:8080/metrics_.  Expected result: json metrics output.

Open _localhost:8080/metrics/prometheus_.  Expected result: prometheus metrics output.

