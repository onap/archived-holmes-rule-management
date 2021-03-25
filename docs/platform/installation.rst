.. This work is licensed under a Creative Commons Attribution 4.0 International License.


Installation
------------

In the Honolulu release, Holmes is deployed as an analytic application by the DCAE controller. So the users do not have to install it on their own.

In case the users want to deploy Holmes independently, the steps for the installation is as follows.

Prerequisites
^^^^^^^^^^^^^

#. MSB must be installed and started. The user knows the IP address of the MSB API gateway service.
#. PostgreSQL must be installed and started. For the guidance on how to run a PostgreSQL, please refer to `Offical Repository of PostgreSQL <https://hub.docker.com/_/postgres/>`_.
   
   **While setting up PostgreSQL, a database and a user named 'holmes' must be created. The corresponding password shuold be set to 'holmespwd'. Otherwise, Holmes could not be started up successfully.**

Steps
^^^^^

#. Start the rule management module of Holmes using the command below:

   ``sudo docker run --name holmes-rule-management -p 9101:9101 -p 9201:9201 -p 9104:9104 -p 9105:9105 -d -e URL_JDBC=$DB_IP -e MSB_ADDR=$MSB_IP -e TESTING=1 -e HOST_IP=$HOST_IP -e ENABLE_ENCRYPT=false nexus3.onap.org:10001/onap/holmes/rule-management``   

#. Start the engine manamgement module of Holmes using the command below:

   ``sudo docker run --name holmes-engine-management -p 9102:9102 -d -e URL_JDBC=$DB_IP -e MSB_ADDR=$MSB_IP -e TESTING=1 -e HOST_IP=$HOST_IP -e ENABLE_ENCRYPT=false nexus3.onap.org:10001/onap/holmes/engine-management``

When the environment variable ``TESTING`` is set to ``1``, it means Holmes is running in the standalone mode. All the interactions between Holmes and other ONAP components are routed by MSB. In order to register Holmes itself to MSB, the users have to specify the IP address of the host using the ``HOST_IP`` variable. Please note that the ``HOST_IP`` should be the IP address of the host, rather than the IP address of the containers (of which the IP address is allocated by the docker daemon).
``ENABLE_ENCRYPT`` specifies whether HTTPS is enabled. When it is set to "false", only the HTTP schema is allowed. Otherwise, only HTTPS is allowed.

Check the Status of Holmes
^^^^^^^^^^^^^^^^^^^^^^^^^^

After the installation, you have to check whether Holmes is alive or not using the health-check API.

#. Use ``curl http://${msb-ip}:${msb-port}/api/holmes-rule-mgmt/v1/healthcheck`` or any other tools (e.g. Postman) to check whether the rule management module of Holmes has been spun up and registered to MSB successfully. 

#. Use ``curl http://${msb-ip}:${msb-port}/api/holmes-engine-mgmt/v1/healthcheck`` or any other tools (e.g. Postman) to check whether the engine management module of Holmes has been spun up and registered to MSB successfully.

If the response code is ``200`` and the response body is ``true``, it's telling the user that everything is fine. Otherwise, you have to take a look at the logs to check whether there are any errors and contact the Holmes team for help.

