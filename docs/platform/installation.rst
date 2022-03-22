.. This work is licensed under a Creative Commons Attribution 4.0 International License.


Installation
------------

In the Jakarta release, Holmes could either be deployed as an analytics application of DCAE along with the whole ONAP or be deployed in a standalone mode for the sake of saving resources.

If a user wants to deploy Holmes using Helm, please refer to the OOM manual for help.

In case a user want to deploy Holmes independently, the steps for the installation is as follows.

Prerequisites
^^^^^^^^^^^^^

#. MSB must be installed and started. The user knows the IP address of the MSB API gateway service.
#. PostgreSQL must be installed and started. For the guidance on how to run a PostgreSQL, please refer to `Offical Repository of PostgreSQL <https://hub.docker.com/_/postgres/>`_.

   **While setting up PostgreSQL, a database and a user named 'holmes' must be created. The corresponding password shuold be set to 'holmespwd'. Otherwise, Holmes could not be started up successfully.**

Steps
^^^^^

#. Start the rule management module of Holmes using the command below:

   ``sudo docker run --name holmes-rule-management -p 9101:9101 -p 9104:9104 -p 9201:9201 -d -e URL_JDBC=$DB_IP -e MSB_IAG_SERVICE_HOST=$MSB_IAG_IP -e MSB_IAG_SERVICE_PORT=$MSB_IAG_PORT -e HOST_IP=$HOST_IP -e ENABLE_ENCRYPT=false -v $LOCAL_PATH_THAT_STORES_THE_CONFIG_FILES:/opt/hrmrules nexus3.onap.org:10001/onap/holmes/rule-management:10.0.2``

#. Start the engine manamgement module of Holmes using the command below:

   ``sudo docker run --name holmes-engine-management -p 9102:9102 -d -e URL_JDBC=$DB_IP -e MSB_IAG_SERVICE_HOST=$MSB_IAG_IP -e MSB_IAG_SERVICE_PORT=MSB_IAG_PORT -e HOST_IP=$HOST_IP -e ENABLE_ENCRYPT=false -v $LOCAL_PATH_THAT_STORES_THE_CONFIG_FILES:/opt/hemtopics  nexus3.onap.org:10001/onap/holmes/engine-management:10.0.2``

All the interactions between Holmes and other ONAP components are routed by MSB. In order to register Holmes itself to MSB, the users have to specify the IP address of the host using the ``HOST_IP`` variable. Please note that the ``HOST_IP`` should be the IP address of the host, rather than the IP address of the containers (of which the IP address is allocated by the docker daemon).
``ENABLE_ENCRYPT`` specifies whether HTTPS is enabled. When it is set to "false", only the HTTP schema is allowed. Otherwise, only HTTPS is allowed. ``LOCAL_PATH_THAT_STORES_THE_CONFIG_FILES`` specifies the place where corresponding configuration files are stored. The configuration files should be organized as `files for the rule management module <https://gerrit.onap.org/r/gitweb?p=oom.git;a=tree;f=kubernetes/holmes/components/holmes-rule-mgmt/resources/rules;h=e3071f02c0143bd5774967bd7148d73afeb8a17c;hb=HEAD>`_ and `files for the engine management module <https://gerrit.onap.org/r/gitweb?p=oom.git;a=tree;f=kubernetes/holmes/components/holmes-engine-mgmt/resources/config;h=6a43bc35fa56731379956da08f766aa8d0abdd53;hb=HEAD>`_ (only *cfy.json* is needed in the standalone mode).

Check the Status of Holmes
^^^^^^^^^^^^^^^^^^^^^^^^^^

After the installation, you have to check whether Holmes is alive or not using the health-check API.

#. Use ``curl http://${msb-ip}:${msb-port}/api/holmes-rule-mgmt/v1/healthcheck`` or any other tools (e.g. Postman) to check whether the rule management module of Holmes has been spun up and registered to MSB successfully.

#. Use ``curl http://${msb-ip}:${msb-port}/api/holmes-engine-mgmt/v1/healthcheck`` or any other tools (e.g. Postman) to check whether the engine management module of Holmes has been spun up and registered to MSB successfully.

If the response code is ``200`` and the response body is ``true``, it's telling the user that everything is fine. Otherwise, you have to take a look at the logs to check whether there are any errors and contact the Holmes team for help.

