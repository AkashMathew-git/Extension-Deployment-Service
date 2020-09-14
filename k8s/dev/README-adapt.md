#### Adapt this to your Thidwick project.

You will need all the code in thidwick-starter/k8s/dev.

**Plan your metrics.**  Run your thidwick service.  Using browser or curl, hit the metrics endpoint at localhost:8080/metrics/prometheus.  You will need to have traffic (requests per second) and latency metrics for your endpoints.  You will need metrics for anything else you want to monitor.

**Build your own Thidwick container.**  In your thidwick project, run the gradle distDocker command.

**Adapt thidwick-starter-values-local.yaml.**  You will need to adjust the imagename to match your docker image name. 


**Adapt the Thidwick Starter dashboard.**  For each Thidwick graph, the data query will need to change to indicate your endpoints.  Do you need to look at other data elements?  See above to view all the Thidwick metric data directly in a browser.
If you change the _thidwick-starter-values-local.yaml/application_ value from "thidwick-starter", you will need to adjust the queries behind the graphs for 
several of the Thidwick-Starter dashboard items.  
You will need to change the queries for the Hello World latency graphs to match the name of your endpoint.

You can run the system at this point, and exercise your endpoint from a browser or with curl.  You should see some activity on the Thidwick-Starter dashboard.

**Adapt the Locust configuration.**  This is the biggest effort.
- Edit locustfile.py to exercise your endpoints.  You will want to refer to the Locustio documentation at https://docs.locust.io/en/stable/
- Rebuild the locustio docker image, using an appropriate tag.
- Edit grafana-values-local.yaml, setting the imagename to your docker tag.

**Pro Tip:** Once you have your Grafana dashboard configured for your thidwick endpoint, export its json and put it in the code repository.

