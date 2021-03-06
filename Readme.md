SDWN Controller
===============

[ONOS](http://onospoject.org) application for centralized management of [LEDE](https://lede-project.org/) wifi access points (APs).

## Overview

The SDWN controller is structured into a core controller module exposing an API and service modules using that API. The modules can be built separately using maven. The core controller requires a modified ONOS which includes the SDWN protocol driver. You can find it [here](https://github.com/berlin-open-wireless-lab/sdwn-onos) along with instructions on how to build and run ONOS.
ONOS serves as the SDN platform on top of which the SDWN controller is running.
Actually, the SDWN controller is an ONOS applications on top of ONOS' OpenFlow subsystem. It is a bit confusing at first but the way ONOS is modularized is actually pretty clever and makes developing for the platform easy, once you have understood its structure. Check out the ONOS documentation in their [wiki](https://wiki.onosproject.org/display/ONOS/Wiki+Home) and search for tutorials on YouTube if you want to learn more.

## Installation

In order to compile the SDWN controller you have to make sure that the necessary ONOS dependencies are installed in your local maven repository. To do this, clone the ONOS repository and run ```mvn install``` in its root directory. Then proceed as follows:

1. Clone the controller repository: ```git clone https://github.com/berlin-open-wireless-lab/sdwn-controller.git```
2. Compile the SDWN controller: ```mvn clean install```
3. Install the OAR (**O**NOS Application **Ar**chive). ONOS needs to be running for this. ```onos-app <ONOS_IP> install app/target/sdwn-controller-1.0-SNAPSHOT.oar```
4. Activate the controller: ```onos-app <ONOS_IP> activate de.tuberlin.inet.sdwn.sdwn-controller```
5. Verify that the controller is running by looking for the commands in the ```sdwn:``` scope on the ONOS command-line

## Using the Controller's APIs

### Java API
You can write independent ONOS applications that use the SDWN controller and deploy them as OSGi bundles. [This tutorial](https://wiki.onosproject.org/display/ONOS/Template+Application+Tutorial) explains how. 
The SDWN controller's Java API is a self-contained module that can be referenced as a dependency in other ONOS applications by adding this to the project's ```pom.xml```:
```xml
<dependency>
    <groupId>de.tuberlin.inet.sdwn</groupId>
    <artifactId>sdwn-core-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

The module contains interfaces and classes for interacting with the SDWN controller or to subscribe to events. There are callback mechanisms to get notifications of the following events:
1. 802.11 management frame (probe/assoc/auth) reception at an AP
2. client association and dis-association
3. the connection or disconnection of a wireless switch

To request a handle to the SDWN controller from the OSGi environment, reference the ```SdwnCoreService``` in your application's main class:

```java
@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
private SdwnCoreService sdwnService;
```

Here is an example of how the Hearingmap service makes use of this interface to track clients:

The hearing map service contains a private class which implements the ```Sdwn80211MgmtFrameListener``` interface:
```java
private final class InternalFrameListener implements Sdwn80211MgmtFrameListener {
    @Override
    public void receivedProbeRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
        clientHeard(atAP.nic().switchID(), atAP, clientMac, rssi, freq);
    }

    @Override
    public void receivedAuthRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
        clientHeard(atAP.nic().switchID(), atAP, clientMac, rssi, freq);
    }

    @Override
    public void receivedAssocRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
        clientHeard(atAP.nic().switchID(), atAP, clientMac, rssi, freq);
    }
}
```

### Command-Line Interface

The SDWN controller offers the following commands:
1. ```switches``` - to print information about connected wireless switches
2. ```aps``` - to print information about access points
3. ```clients``` - to print information about clients associated with the access points
4. ```client-remove``` - to sever the association of a client with an access point
5. ```set-channel``` - to instruct an access point to send a channel switch announcement and then change the transmitting channel

Details about the commands' arguments are available from the ONOS shell: ```help sdwn:<command>```.

### REST API

The SDWN controller's REST interface covers the same funcitonality as the command-line interface. It is located at ```<onos_url>/sdwn```. You can find documentation generated by the swagger tool along with interactive test calls at ```<onos_url>/onos/v1/docs```.
