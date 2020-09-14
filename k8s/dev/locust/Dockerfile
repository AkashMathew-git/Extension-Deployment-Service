#   Copyright 2017 Karol Brejna
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
#   Brady Wiseman 4/3/2018  Added installation of prometheus-client
#

FROM python:3.6-alpine

COPY docker-entrypoint.sh /

RUN    apk --no-cache add --virtual=.build-dep build-base \
    && apk --no-cache add libzmq \
    && pip install --no-cache-dir locustio==0.8.1 \
    && pip install --no-cache-dir prometheus-client \
    && apk del .build-dep \
    && chmod +x /docker-entrypoint.sh

RUN  mkdir /locust
COPY locustfile.py /locust
COPY locust_prom_endpoint.py /locust

WORKDIR /locust
EXPOSE 8089 5557 5558

ENTRYPOINT ["/docker-entrypoint.sh"]