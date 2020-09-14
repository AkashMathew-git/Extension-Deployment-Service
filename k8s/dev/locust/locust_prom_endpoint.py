# Provide a route for a prometheus endpoint on locustio.

import six

from locust import web
from locust import runners
from flask import make_response
from prometheus_client import Metric

STATE_STOPPED = "stopped"

@web.app.route("/metrics")
def metrics():
    is_distributed = isinstance(runners.locust_runner, runners.MasterLocustRunner)
    if is_distributed:
            slave_count = runners.locust_runner.slave_count
    else:
        slave_count = 0

    # TODO look into host
    if runners.locust_runner.host:
            host = runners.locust_runner.host
    elif len(runners.locust_runner.locust_classes) > 0:
        host = runners.locust_runner.locust_classes[0].host
    else:
        host = None

    percentiles = (.05, .25, .5, .75, .90, .95, .99, 1.00)
    percentileLabels = ("5%", "25%", "50%", "75%", "90%", "95%", "99%", "100%")

    metrics = []

    metric = Metric('locust_slave_count', 'Locust Number of slaves', 'gauge')
    metric.add_sample('locust_slave_count', value=slave_count, labels={})
    metrics.append(metric)

    metric = Metric('locust_user_count', 'Swarmed users', 'gauge')
    metric.add_sample('locust_user_count', value=runners.locust_runner.user_count, labels={})
    metrics.append(metric)

    for key in sorted(six.iterkeys(runners.locust_runner.request_stats)):
        statsRow = runners.locust_runner.request_stats[key]
        metric = Metric('locust_num_requests', 'Number of requests', 'counter')
        metric.add_sample('locust_num_requests', value=statsRow.num_requests, labels={'endpoint':statsRow.name, 'method':statsRow.method})
        metrics.append(metric)

        metric = Metric('locust_num_failures', 'Number of failures', 'counter')
        metric.add_sample('locust_num_failures', value=statsRow.num_failures, labels={'endpoint':statsRow.name, 'method':statsRow.method})
        metrics.append(metric)

        metric = Metric('locust_current_requests_per_second', 'Requests Per Second', 'gauge')
        metric.add_sample('locust_current_requests_per_second', value="{0:.1f}".format(statsRow.total_rps), labels={'endpoint':statsRow.name, 'method':statsRow.method})
        metrics.append(metric)

        if statsRow.response_times:
            metric = Metric('locust_latency', 'Latency Percentile', 'gauge')
            for i in range(len(percentiles)):
                metric.add_sample('locust_latency', value=statsRow.get_response_time_percentile(percentiles[i]), labels={'endpoint':statsRow.name, 'method':statsRow.method, 'percentile':percentileLabels[i]})
            metrics.append(metric)

    # Locust does not clear the stats when a test stops, so our metrics continue with the last values.  Clear them.
    if runners.locust_runner.state == STATE_STOPPED:
        runners.locust_runner.stats.clear_all()

    output = []
    for metric in metrics:
        output.append('# HELP {0} {1}'.format(
            metric.name, metric.documentation.replace('\\', r'\\').replace('\n', r'\n')))
        output.append('\n# TYPE {0} {1}\n'.format(metric.name, metric.type))

        # Format the sample.  timestamp & examplar are in the tuple but we don't use them.
        for name, labels, value, timestamp, exemplar in metric.samples:
            if labels:
                labelstr = '{{{0}}}'.format(','.join(
                    ['{0}="{1}"'.format(
                        k, v.replace('\\', r'\\').replace('\n', r'\n').replace('"', r'\"'))
                        for k, v in sorted(labels.items())]))
            else:
                labelstr = ''
            output.append('{0}{1} {2}\n'.format(name, labelstr, value))

    response = make_response("".join(output))
    response.mimetype = "text/plain; charset=utf-8'"
    response.content_type = "text/plain; charset=utf-8'"
    response.headers["Content-Type"] = "text/plain; charset=utf-8'"
    return response
