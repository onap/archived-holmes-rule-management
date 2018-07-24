FROM openresty/openresty:alpine

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

EXPOSE 9101 9104 9201

ENV HOSTNAME holmes-rule-mgmt

ENV LANG C.UTF-8

#install java-1.8-openjdk

# add a simple script that can auto-detect the appropriate JAVA_HOME value
# based on whether the JDK or only the JRE is installed
RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin

ENV JAVA_ALPINE_VERSION 8.171.11-r0

RUN set -x \
	&& apk add --no-cache \
		openjdk8="$JAVA_ALPINE_VERSION" \
	&& [ "$JAVA_HOME" = "$(docker-java-home)" ]

#install neccessary tools
RUN apk upgrade \
    && apk update \
    && apk add --no-cache curl \
    && apk add --no-cache postgresql-client=10.4-r0

#add the frontend pacakge to the docker images
RUN rm /etc/nginx/conf.d/default.conf
ADD holmes-rulemgt-frontend-*.tar.gz /usr/local/openresty/nginx/html
ADD nginx.conf /usr/local/openresty/nginx/conf

#add the backend package to the docker image
RUN mkdir /home/holmes
WORKDIR /home/holmes
ADD holmes-rulemgt-standalone-*-linux64.tar.gz /home/holmes/
RUN chmod 755 /home/holmes/bin/*.sh

CMD ["sh", "/home/holmes/bin/run.sh"]

