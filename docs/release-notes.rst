.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


Holmes Rule Management Release Notes
====================================

Holmes provides alarm correlation and analysis for telecom cloud infrastructure
and services, including hosts, vims, VNFs and NSs. Holmes aims to find the root
reason which causes the failure or degradation of services by digging into the
ocean of events collected from different levels of the telecom cloud.


Version: 1.1.0
--------------

:Release Date: 2018-05-24


**New Features**
In the Amsterdam release, Holmes is mainly intended to support the alarm
correlation analysis for the VoLTE scenario. To get us there, Holmes provides
the following features:

- `Rule Management <https://jira.onap.org/browse/HOLMES-4>`_ The feature provides interfaces for the users to create, query, update and delete rules. In this release, they are used along with the DCAE interfaces to accomplish the deployment (creation/update) of the control loop related rules.

- `Engine Management <https://jira.onap.org/browse/HOLMES-5>`_ The feature is not exposed to the end user directly. It's mainly used internally by Holmes as a container for the execution of rules. It provides interface for rule verification and deployment/un-deployment.

**Bug Fixes**

N/A

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

Holmes is following the CII Best Practices Badge program. Most of its vulnerability issues have been fixed in Beijing Release except for the issues brought in by jackson-databind which is introduced indirectly by third-party dependencies (namely Dropwizard). The impact analysis can be found at `Holmes Security/Vulnerability Threat Impact Analysis <https://wiki.onap.org/pages/viewpage.action?pageId=28378012>`_


**Upgrade Notes**

- `HOLMES-99 <https://jira.onap.org/browse/HOLMES-99>`_ Updated the Drools engine to Version 6.5.0.
- `HOLMES-104 <https://jira.onap.org/browse/HOLMES-104>`_ Update all interfaces to enforce the HTTPS protocal.
- `HOLMES-112 <https://jira.onap.org/browse/HOLMES-112>`_ Following the guideline of the Logging Enhancements Project to implement log collection.
- `HOLMES-106 <https://jira.onap.org/browse/HOLMES-106>`_ Implemented horizontal scaling.
- `HOLMES-123 <https://jira.onap.org/browse/HOLMES-123>`_ Fixed some vulnerability issues identified by Nexus IQ.


**Deprecation Notes**

N/A

**Other**

N/A

===========

End of Release Notes
