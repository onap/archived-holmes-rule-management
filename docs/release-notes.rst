.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0


Holmes Rule Management Release Notes
====================================

Holmes provides alarm correlation and analysis for telecom cloud infrastructure
and services, including hosts, vims, VNFs and NSs. Holmes aims to find the root
reason which causes the failure or degradation of services by digging into the
ocean of events collected from different levels of the telecom cloud.


Version: 1.0.0
--------------

:Release Date: 2018-06-07


**New Features**
In the Beijing release, Holmes provides no more functionalites than the Amsterdam release. Its main features remains like follows:

- `Rule Management <https://jira.onap.org/browse/HOLMES-4>`_ The feature provides interfaces for the users to create, query, update and delete rules. In this release, they are used along with the DCAE interfaces to accomplish the deployment (creation/update) of the control loop related rules.

- `Engine Management <https://jira.onap.org/browse/HOLMES-5>`_ The feature is not exposed to the end user directly. It's mainly used internally by Holmes as a container for the execution of rules. It provides interface for rule verification and deployment/un-deployment.

Besides, Holmes has been enhanced to meet the platform maturity requirements. The enhancement mainly covers:

- Scaling: Holmes supports horizontal scale-in/scale-out operations in case it is overloaded by too large amounts of data.

- Security: Holmes has updated all its APIs to support the HTTPS protocol. 

**Bug Fixes**

This is the initial release.

**Known Issues**

If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The HOLMES open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=28378012>`_.

Quick Links:
 	- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
 	
 	- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_
 	
 	- `Project Vulnerability Review Table for HOLMES <https://wiki.onap.org/pages/viewpage.action?pageId=28378012>`_


**Upgrade Notes**

This is the inital release.

**Deprecation Notes**

None of the HTTP APIs provided in the Amsterdam release are available in Beijing anymore.


**Other**

N/A

===========

Version: 1.0.0
--------------

:Release Date: 2017-11-16


**New Features**
In the Amsterdam release, Holmes is mainly intended to support the alarm
correlation analysis for the VoLTE scenario. To get us there, Holmes provides
the following features:

- `Rule Management <https://jira.onap.org/browse/HOLMES-4>`_ The feature provides interfaces for the users to create, query, update and delete rules. In this release, they are used along with the DCAE interfaces to accomplish the deployment (creation/update) of the control loop related rules.

- `Engine Management <https://jira.onap.org/browse/HOLMES-5>`_ The feature is not exposed to the end user directly. It's mainly used internally by Holmes as a container for the execution of rules. It provides interface for rule verification and deployment/un-deployment.

**Bug Fixes**

This is the initial release.

**Known Issues**

If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.

**Security Issues**

N/A

**Upgrade Notes**

This is the inital release.
===========


End of Release Notes
