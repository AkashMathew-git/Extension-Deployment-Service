#### Install and configure Grafana

We use a custom OSvC Helm chart to install Grafana, to overcome various issues with OCI.
The --namespace clause is optional for the local install.  Without it, Helm will install to the _default_ namespace.
```
$ helm install your-path-to/osvc-cloud-ms/osvc-helm/incubator/grafana \
--name graf4thidwick \
--values grafana-values-local.yaml \
--namespace your-namespace
```
Follow the instructions in the _NOTES_ at the end of the output to get the admin password, and port-forward the Grafana browser endpoint.

Use ampersand at the end of the port-forward command to run it in the background.

Open the UI on your browser, URL localhost:3000.  Log in using admin/your_secret.  This login will remain active until you uninstall Grafana.

Hook up Prometheus as a data source in Grafana
Get the DNS name for the _Prometheus_ server.  It's listed in NOTES at the end of the output.
```
$ helm status prom4thidwick
# In our case, it is prom4thidwick-prometheus-server.default.svc.cluster.local
```

In the Grafana UI, go to Add Data Source.
Name: prom4thidwick  <sp><sp>Default: True (checked)
<br>Type: prometheus
URL:  http://your_dns_name
<br>Access:  proxy
<br> Set it as the default data source.

**Verify:** Grafana responds with "Data source is working"

##### In Grafana, install the thidwick-starter dashboard
On Grafana's left-hand logo, select +|Import.
Copy thidwick-starter/k8s/dev/thidwick-starter-dashboard.json, and paste it into the json window.
<br>Click Load.

**Verify:** Graphs on the dashboard are showing data, which indicates that Prometheus is scraping and Grafana is graphing.
<br>Note: some of the Prometheus Stats may have no data points yet.

##### On the thidwick-starter endpoint, run some REST queries.

In a browser, hit the /hello endpoint about 10 times:
```
localhost:8080/hello
#
# You can also examine the thidwick's Prometheus metrics:
localhost:8080/metrics/prometheus
```

**Verify:** your /hello requests cause the graphs on Thidwick Starter Dashboard to register new data.  Since we don't have Locust installed yet, the bottom panel will not have any data.

**At this point, we have thidwick-starter running under kubernetes, with its metrics graphing to Grafana via Prometheus.**
