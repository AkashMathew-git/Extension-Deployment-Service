# locustio/locustfile configured to test a thidwick-starter endpoint.

from locust import HttpLocust, TaskSet

import locust_prom_endpoint

def hello(l):
    l.client.get("/hello")

def notThere(l):
    l.client.get("/no_such_page")

class UserBehavior(TaskSet):
    #tasks = {hello: 2, notThere: 1}
    tasks = {hello: 2}

    def on_start(self):
        #login(self)
        pass

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 500
    max_wait = 5000