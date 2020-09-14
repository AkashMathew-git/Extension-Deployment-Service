#### Install Prometheus

Use the given prometheus4thidwick_values.yaml file to install a lighter-weight Prometheus.   

Without intervention, the Helm chart for Prometheus will install components that you don't need to get started.  We turn off alertmanager and pushgateway. They will run in pods and consume resources for no purpose.  
Additionally, we override the default value for Prometheus scrape interval.  The default is 60 seconds; we set it to 20.

We set a specific name for the release under Kubernetes. Grafana will use this name to find Prometheus.
The --namespace clause is optional for the local install.  Without it, Helm will install to the _default_ namespace.
```$xslt
$ helm install stable/prometheus \
--values=prometheus4thidwick-values.yaml \
--name prom4thidwick \
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
