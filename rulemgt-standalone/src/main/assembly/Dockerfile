FROM ubuntu:16.04

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

EXPOSE 9101 9104 9201

#install openjdk-1.8
#RUN sed -i 's#http://archive.ubuntu.com#http://mirrors.163.com#g' /etc/apt/sources.list
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk

#configure the JDK
RUN sed -i 's|#networkaddress.cache.ttl=-1|networkaddress.cache.ttl=10|' /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $PATH:/usr/lib/jvm/java-8-openjdk-amd64/jre/bin:/usr/lib/jvm/java-8-openjdk-amd64/bin
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib
ENV JRE_HOME ${JAVA_HOME}/jre

#install neccessary tools
RUN apt-get install -y bash curl vim

#install nginx
RUN apt-get install -y wget build-essential libtool libpcre3 libpcre3-dev zlib1g-dev openssl
RUN wget http://nginx.org/download/nginx-1.14.0.tar.gz
RUN tar -zxvf nginx-1.14.0.tar.gz
RUN rm nginx-1.14.0.tar.gz
WORKDIR /nginx-1.14.0
RUN ./configure --prefix=/usr/local/nginx
RUN make install
RUN ln -s /usr/local/nginx/sbin/nginx /usr/bin/nginx
RUN rm -rf /nginx-1.14.0
ADD holmes-rulemgt-frontend-*.tar.gz /usr/local/nginx/html
ADD holmes.nginx.conf /usr/local/nginx/conf


ENV HOSTNAME holmes-rule-mgmt

#add holmes related resources to the docker image
RUN mkdir /home/holmes
WORKDIR /home/holmes
ADD holmes-rulemgt-standalone-*-linux64.tar.gz /home/holmes/
RUN chmod 755 /home/holmes/bin/*.sh

#install the postgres client
RUN apt-get install -y postgresql-client-9.5 postgresql-contrib-9.5

ENTRYPOINT /home/holmes/bin/run.sh

