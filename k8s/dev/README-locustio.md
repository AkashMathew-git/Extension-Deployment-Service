#### Prepare and install Locustio.
We have a build of locustio that is configured to make requests against thidwick-starter's /hello endpoint.
We'll install it locally, configured for our thidwick pod.


**Build locustio in your local Docker repository.**
Navigate to thidwick-starter/k8s/dev/locust.
```$xslt
$ docker build --tag locust-thidwick-starter:0.8.1-py3.6 .
```

**Deploy Locustio under Kubernetes.**
The --namespace clause is optional for the local install.  Without it, Helm will install to the _default_ namespace.
```
$ helm install your-path-to/osvc-cloud-ms/osvc-helm/incubator/locustio4thidwick-starter \
--values locust4thidwick-values-local.yaml \
--name locust4thidwick \
--set master.config.target-host="http://thidwick-thidwick-starter:8080" \
--namespace your-namespace
```

**Set up the Locustio browser UI**
```$xslt
$ export POD_NAME=$(kubectl get pods --namespace default -l "app=locust4thidwick-locust,component=master" -o jsonpath="{.items[0].metadata.name}")
$ kubectl port-forward $POD_NAME 8089 &
# Use ampersand at the end of the port-forward command to run it in the background.
# Open the UI on your browser, URL localhost:8089 
```
**Bring up Locust control page in a browser.**
```
localhost:8089
```
**Verify:** Locust is aimed at host http://thidwick-starter:8080 

##### Start a load test using the Locust web UI
On the Locust browser page, start a new Locust swarm with 50 users, with hatch rate of 5.
Start swarming!

**Verify:** 
- The Locust Statistics show the #requests column consistently growing, with no #fails.
- On the Grafana Thidwick-Starter dashboard, the Requests/Second and Latency graphs start showing data.
- On the Grafana Thidwick-Starter dashboard, the Locust latency graphs on the bottom panel of the Thidwick Starter Dashboard start showing data.  Since we need to wait for one or more 20-second Prometheus scrape cycles, this may take a minute.

**Stop the load test**
After a few thousand requests, click Stop on the Locust web UI.

**Try slamming Thidwick.**
Start a new test, but raise the load.  Add more Locust users (100?  200?  1,000?), raising the Requests per Second to the point that thidwick-starter performance deteriorates.  You will see this in the latency graphs.
