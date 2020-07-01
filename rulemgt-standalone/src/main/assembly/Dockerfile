FROM onap/integration-java11:7.0.0

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

USER root

# 9101 - service port
# 9104 - GUI port
# 9201 - debugging port
EXPOSE 9101 9104 9201

ENV HOSTNAME=holmes-rule-mgmt \
    LANG=C.UTF-8

RUN apt-get upgrade \
    && apt-get update \
    && apt-get install -y curl postgresql-client-11 nginx


ADD holmes-rulemgt-standalone-*-linux64.tar.gz /opt/onap/
ADD holmes-rulemgt-frontend-*.tar.gz /usr/share/nginx/html/
ADD nginx-https.conf /etc/nginx/conf.d/
ADD nginx-http.conf /etc/nginx/conf.d/
ADD holmes-frontend.key /etc/ssl/private/
ADD holmes-frontend-selfsigned.crt /etc/ssl/certs/
ADD dhparam.pem /etc/ssl/certs/

RUN mkdir -p /usr/share/nginx/logs \
    && chmod -R 777 /usr/share/nginx/ \
    && chmod -R 777 /var/lib/nginx/ \
    && chmod -R 755 /etc/ssl/private/ \
    && chmod -R 777 /etc/nginx/conf.d/ \
    && chmod -R a+rw /opt/onap/ \
    && chmod -R a+rw /var/log/ \
    && chmod 755 /opt/onap/bin/*.sh

USER onap
ENTRYPOINT ["sh", "/opt/onap/bin/run.sh"]

