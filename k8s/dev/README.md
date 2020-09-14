## Run Thidwick-starter on Kubernetes
###Instrument It With Prometheus and Grafana, Performance Test It With Locustio

This project enables you to create and monitor a load on the thidwick-starter microservice, all running under Kubernetes.

Prerequisites: 
 - Docker, Kubernetes, and Helm are all installed and working on your machine.
 - You can run kubectl.
 - You can run Kubernetes Dashboard.
 - OSvC Kubernetes/Helm code repo downloaded from orahub: osvc-cloud-ms/osvc-helm.
 
### Features
- Run Thidwick-Starter under Kubernetes on your laptop.
- Instrument it with Prometheus and Grafana, also running under Kubernetes.
- Load test it with Locustio, also running under Kubernetes.

### Run Locally On Laptop/Desktop

In this procedure, we will do the following:
- Build a local docker image for thidwick-starter, and install it using a common Helm chart.
- Install Prometheus and Grafana using Helm charts.
- Build a local docker image for locustio, and install it using a common Helm chart.
- Configure all the above to work together, so Prometheus scrapes thidwick-starter and locust, and Grafana makes their metrics visible.
- Access Web UIs for all the above.
- Use Locustio to run load against thidwick-starter.

```
Pro Tip
Working on the Oracle network?  Within your terminal session, you need to set env vars http_proxy & https_proxy.
Setting them both to www-proxy-hqdc.us.oracle.com:80 works.
```

[Build and start Thidwick-Starter](README-thidwick.md)

[Install Prometheus](README-prometheus.md)

[Install and configure Grafana](README-grafana.md)

[Prepare and install Locustio.](README-locustio.md)

##### Project Complete
At this point, you should have Prometheus, Grafana, Thidwick-starter, and Locust all running under Kubernetes, with Locust creating load against Thidwick-starter. 
Requests per second and latency graphs should be visible in Grafana, sourced both internally from withing Thidwick, and externally from Locust.

[Shut it Down](README-shutdown.md)

#####Next:
[Adapt this technique to your thidwick project.](README-adapt.md)

[Run this project on OCI](README-oci.md)
