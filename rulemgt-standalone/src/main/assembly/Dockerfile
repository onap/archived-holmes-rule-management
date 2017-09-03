FROM ubuntu:16.04

MAINTAINER "Guangrong Fu" <fu.guangrong@zte.com.cn>

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

#add holmes related resources to the docker image
RUN mkdir /home/holmes
WORKDIR /home/holmes
ADD holmes-rulemgt-standalone-*-linux64.tar.gz /home/holmes/
RUN chmod 755 /home/holmes/bin/*.sh

#RUN apt-get install -y mysql-client

ENTRYPOINT /home/holmes/bin/run.sh

