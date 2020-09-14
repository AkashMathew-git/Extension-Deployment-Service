
##### Shut it down
Get a list of releases running under kubernetes.
The --namespace clause is optional for the local install. 
```aidl
$ helm list [--namespace your-namespace]
```

Run _helm delete_ for each one.  Purge them so you can reinstall them again later.
```$xslt
$ helm delete --purge locust4thidwick
$ helm delete --purge graf4thidwick
$ helm delete --purge prom4thidwick
$ helm delete --purge thidwick
```
