#!/bin/bash

#
# Copyright 2017 ZTE Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

DIRNAME=`dirname $0`
RUNHOME=`cd $DIRNAME/; pwd`
echo @RUNHOME@ $RUNHOME

echo @JAVA_HOME@ $JAVA_HOME
JAVA="$JAVA_HOME/bin/java"
echo @JAVA@ $JAVA
main_path=$RUNHOME/..
cd $main_path
JAVA_OPTS="-Xms50m -Xmx128m"
port=8312
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$port,server=y,suspend=n"
echo @JAVA_OPTS@ $JAVA_OPTS

class_path="$main_path/:$main_path/holmes-rulemgt.jar"
echo @class_path@ $class_path

sed -i "s|url:.*|url: jdbc:postgresql://$URL_JDBC/holmes|" "$main_path/conf/rulemgt.yml"
sed -i "s|msbServerAddr:.*|msbServerAddr: http://$MSB_ADDR|" "$main_path/conf/rulemgt.yml"

export SERVICE_IP=`hostname -i`
echo SERVICE_IP=${SERVICE_IP}

if [ ! -z ${TESTING} ] && [ ${TESTING} == 1 ]; then
    if [ ! -z ${HOST_IP} ]; then
        export HOSTNAME=${HOST_IP}:9101
    else
        export HOSTNAME=${SERVICE_IP}:9101
    fi
fi


./bin/initDB.sh holmes holmespwd 5432 "${URL_JDBC%:*}"

"$JAVA" $JAVA_OPTS -classpath "$class_path" org.onap.holmes.rulemgt.RuleActiveApp server "$main_path/conf/rulemgt.yml"

