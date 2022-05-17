.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. _release_notes:


Holmes Rule Management Release Notes
====================================

Holmes provides alarm correlation and analysis for telecom cloud infrastructure
and services, including hosts, vims, VNFs and NSs. Holmes aims to find the root
reason which causes the failure or degradation of services by digging into the
ocean of events collected from different levels of the telecom cloud.

Version: 10.0.0
---------------

:Release Date: 2022-06-02


**New Features**

- `Added support to 7.1 VES data-stream in parallel to 5.4. <https://jira.onap.org/browse/HOLMES-308>`_
- `Removed CBS/consul dependency for application config management to finish DEAE tranformation to support Helm. <https://jira.onap.org/browse/HOLMES-488>`_

**Bug Fixes**

- `Engine instance recreating ALARM_INFO table on every restart of pod. <https://jira.onap.org/browse/HOLMES-491>`_
- `Make EN lang as default for other than ZH locale in Holmes UI. <https://jira.onap.org/browse/HOLMES-499>`_
- `Date parsing exceptions in logs after rule-management restart. <https://jira.onap.org/browse/HOLMES-492>`_

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.
- `Unable to perform CRUD operations on rules when multiple engine instances running <https://jira.onap.org/browse/HOLMES-493>`_ (a potential problem, not reproduced)
- `Inconsistency of alarm processing when multiple engine instances running <https://jira.onap.org/browse/HOLMES-494>`_ (a potential problem, not reproduced)

**Security Issues**

HOLMES code has been formally scanned during build time using NexusCloud and all critical vulnerabilities have been addressed.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_


**Upgrade Notes**

N/A


**Deprecation Notes**

N/A


**Other**

N/A


Version: 9.0.0
--------------

:Release Date: 2021-11-04


**New Features**

N/A

**Bug Fixes**

- `Fixed Vulnerability issues by removing httpclient. <https://jira.onap.org/browse/HOLMES-441>`_
- `Failed to update rules if the package name does not end with a semicolon. <https://jira.onap.org/browse/HOLMES-482>`_
- `Database instantiation failed due to holmesdb password with single quote. <https://jira.onap.org/browse/HOLMES-463>`_
- `Existing rules are not able to sync when engine get restarted/redeployed. <https://jira.onap.org/browse/HOLMES-462>`_
- `RESTful APIs of the Engine Management Module are not Accessible. <https://jira.onap.org/browse/HOLMES-454>`_

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusCloud and all critical vulnerabilities have been addressed.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_


**Upgrade Notes**

N/A


**Deprecation Notes**

N/A


**Other**

N/A

Version: 8.0.1
--------------

:Release Date: 2021-04-22


**New Features**

- `HOLMES-380 <https://jira.onap.org/browse/HOLMES-380>`_ Migrate Holmes deployment to Helm

**Bug Fixes**

N/A

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusCloud and all critical vulnerabilities have been addressed.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_


**Upgrade Notes**

N/A


**Deprecation Notes**

N/A


**Other**

N/A


Version: 7.0.1
--------------

:Release Date: 2020-11-19


**New Features**

- `HOLMES-312 <https://jira.onap.org/browse/HOLMES-312>`_ Alarm Correlation Support in MDONS Close Loop

**Bug Fixes**

- `HOLMES-357 <https://jira.onap.org/browse/HOLMES-357>`_ Some holmes pods have no limit
- `HOLMES-367 <https://jira.onap.org/browse/HOLMES-367>`_ Holmes certificates are expired

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusCloud and all critical vulnerabilities have been addressed.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_


**Upgrade Notes**

- Upgraded the Java environment from Java 8 to Java 11.
- Changed the base images to onap/integration-java11:7.0.0.


**Deprecation Notes**

N/A


**Other**

N/A


Version: 5.0.1
--------------

:Release Date: 2019-10-17


**New Features**
N/A

**Bug Fixes**
N/A

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, some of the items that remain open have been assessed for risk and determined to be false positive and the rest are planned to be resolved in the next release.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_


**Upgrade Notes**

N/A


**Deprecation Notes**

N/A


**Other**

N/A

Version: 1.2.6
--------------

:Release Date: 2019-06-13


**New Features**
Added some tools to support the extended CCVPN use case:

- `Rules for CCVEN Extension <https://jira.onap.org/browse/HOLMES-193>`_ This feature provides some rules for the extended CCVPN usecase. Users could use the rules as templates to develop their own rules.
- `Updated AAI Assistant Tools for CCVPN Extension <https://jira.onap.org/browse/HOLMES-194>`_ Some tools related to the CCVPN usecase have been enhanced in order to support the extended CCVPN usecase.

**Bug Fixes**

- `HOLMES-204 <https://jira.onap.org/browse/HOLMES-204>`_ Alarms can not be deleted from the DB when they get cleared.
- `HOLMES-223 <https://jira.onap.org/browse/HOLMES-223>`_ The "ABATED" messages can not be generated in the control loop.

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, some of the items that remain open have been assessed for risk and determined to be false positive and the rest are planned to be resolved in the next release.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_


**Upgrade Notes**

N/A


**Deprecation Notes**

N/A


**Other**

N/A

===========

Version: 1.2.0
--------------

:Release Date: 2018-11-30


**New Features**
In the Casablanca release, Holmes provides its own GUI for rule management tasks:

- `UI Deployment <https://jira.onap.org/browse/HOLMES-96>`_ This feature provides a graphic user interface for the sake of easiness of rule management. It mainly provides a rule list view and a rule editing page. Users could get an overview of all rules that have been added to Holmes and create/modify them easily by using the GUI provided by this feature.

Besides, Holmes has been enhanced to be CCVPN use case supportive. CCVPN related assistant tools are added to the common library of Holmes.

**Bug Fixes**

- `HOLMES-156 <https://jira.onap.org/browse/HOLMES-156>`_ Rules can not be deployed after they've been added/removed from the engine.
- `HOLMES-133 <https://jira.onap.org/browse/HOLMES-133>`_ Don't rely on key word 'import' when extracting package name from rule.
- `HOLMES-130 <https://jira.onap.org/browse/HOLMES-130>`_ Holmes can not be successfully registered to MSB when trying to register itself with a health check parameter.

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The HOLMES open Critical security vulnerabilities and their risk assessment have been documented as part of the `project`_.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_
- `Project Vulnerability Review Table for HOLMES`_


**Upgrade Notes**

N/A


**Deprecation Notes**

N/A


**Other**

N/A

===========


Version: 1.1.0
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

N/A

**Known Issues**

- If the database is not stable, there may be data/status inconsistency between the rule management module and the engine management module.
- The current horizontal scaling mechanism can not efficiently balance the load. Optimization is needed in the future.

**Security Issues**

HOLMES code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The HOLMES open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=28378012>`_.

Quick Links:

- `HOLMES project page <https://wiki.onap.org/display/DW/Holmes+Project>`_
- `Passing Badge information for HOLMES <https://bestpractices.coreinfrastructure.org/en/projects/1602>`_
- `Project Vulnerability Review Table for HOLMES <https://wiki.onap.org/pages/viewpage.action?pageId=28378012>`_


**Upgrade Notes**

- `HOLMES-99 <https://jira.onap.org/browse/HOLMES-99>`_ Updated the Drools engine to Version 6.5.0.
- `HOLMES-104 <https://jira.onap.org/browse/HOLMES-104>`_ Update all interfaces to enforce the HTTPS protocal.
- `HOLMES-112 <https://jira.onap.org/browse/HOLMES-112>`_ Following the guideline of the Logging Enhancements Project to implement log collection.
- `HOLMES-106 <https://jira.onap.org/browse/HOLMES-106>`_ Implemented horizontal scaling.
- `HOLMES-123 <https://jira.onap.org/browse/HOLMES-123>`_ Fixed some vulnerability issues identified by Nexus IQ.


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

N/A


End of Release Notes
