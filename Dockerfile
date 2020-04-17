FROM osvc-docker-local.dockerhub-den.oraclecorp.com/dev-environments/thidwick-base-256:latest
MAINTAINER OSvC Architecture Team "brajohns_org_ww@oracle.com"
EXPOSE 8080
ADD build/distributions/thidwick-starter.tar /
ENTRYPOINT ["/thidwick-starter/bin/thidwick-starter"]
