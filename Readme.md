SDWN Controller
===============

[ONOS](http://onospoject.org) application for centralized management of [LEDE](https://lede-project.org/) wifi access points (APs).

## Overview

The SDWN controller is structured into a core controller module exposing an API and service modules using that API. The modules can be built separately using maven. The core controller requires a modified ONOS which includes the SDWN protocol driver. You can find it [here](https://github.com/berlin-open-wireless-lab/sdwn-onos) along with instructions on how to build and run ONOS.
ONOS serves as the SDN platform on top of which the SDWN controller is running.
Actually, the SDWN controller is an ONOS applications on top of ONOS' OpenFlow subsystem. It is a bit confusing at first but the way ONOS is modularized is actually pretty clever and makes developing for the platform easy, once you have understood its structure. Check out the ONOS documentation in their [wiki](https://wiki.onosproject.org/display/ONOS/Wiki+Home) and search for tutorials on YouTube if you want to learn more.

## The SDWN Core

The core is responsible for the communication with and the representation of the network elements (LEDE access points in this case). It uses ONOS' OpenFlow subsystem and the SDWN protocol driver to learn about LEDE switches, their APs, and associated clients. All of this information is stored and made available through a Java API, a REST API, and on the ONOS command-line.

### SDWN Entities

Relevant building blocks of a wireless network are represented by the SDWN controller as so-called _SDWN entities_. These are things such as APs, clients or frequency bands. The SDWN core maintains these entities and makes them accessible through APIs and callback mechanisms.

### Java API

The SDWN controller's Java API is a self-contained module that can be referenced as a dependency in other ONOS applications by adding this to the project's ```pom.xml```:
```xml
<dependency>
    <groupId>de.tuberlin.inet.sdwn</groupId>
    <artifactId>sdwn-core-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

It contains interfaces and classes for interacting with the SDWN controller or to subscribe to events.
