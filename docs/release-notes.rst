.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


Holmes Rule Management Release Notes
====================================

Holmes provides alarm correlation and analysis for telecom cloud infrastructure and services,
including hosts, vims, VNFs and NSs. Holmes aims to find the root reason which causes the failure
or degradation of services by digging into the ocean of events collected from different levels of
the telecom cloud.


Version: 1.0.0
--------------


:Release Date: 2017-11-16



**New Features**
In the Amsterdam release, Holmes is mainly intended to support the alarm correlation analysis for the VoLTE scenario. To get us there, Holmes provides the following features:
	- `Rule Management <https://jira.onap.org/browse/HOLMES-4>`_ The feature provides interfaces for the users to create, query, update and delete rules. In this release, they are used along with the DCAE interfaces to accomplish the deployment (creation/update) of the control loop related rules. 
	- `Engine Management <https://jira.onap.org/browse/HOLMES-5>`_ The feature is not exposed to the end user directly. It's mainly used internally by Holmes as a container for the execution of rules. It provides interface for rule verification and deployment/un-deployment.

**Bug Fixes**

**Known Issues**

**Security Issues**

**Upgrade Notes**

**Deprecation Notes**

**Other**

===========

End of Release Notes
