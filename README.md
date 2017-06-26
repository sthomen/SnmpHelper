SnmpHelper
==========

Version 1.2

This is a small class that makes working with SNMP4J much simpler for
non-complicated queries.

It completely encapsulates the many SNMP4J classes and provides
chainable methods for setting up get, set and walk queries to SNMPv1,
v2c and v3 Agents.

This version of SnmpHelper is built on SNMP4J 2.5 (2.5.6 specifically).

Building
--------

There's a small build.bat script for windows hosts. Make sure that you
have the JDK installed and then just drop the snmp4j jar in the base
directory of the source and run the script.

If your environment is correctly configured (i.e it has *JAVA_HOME*
properly set), everything should work and you'll end up with a
SnmpHelper.jar. It contains a manifest with a dependency for the version
of snmp4j jar you build it with.

Usage
-----

Usage of the class is fairly straightforward, the class defaults to
SNMPv2c with the community name _public_, so if that's your setup
the great! All you have to do is...

```
SnmpHelper helper = new SnmpHelper();
helper.setAddress("udp:192.168.0.1/161");
```

To set it up and you can then run...

```
String uptime=helper.get("1.3.6.1.2.1.1.1.0");
```

...to get the uptime of the server on 192.168.0.1. Note that this is a
very simple library and thus does _NOT_ do any MIB loading. SNMP4J does
not seem to include these in the jar and thus you can't--for instance
--say _SNMPv2-MIB::sysDescr.0_.

There are two more "end point" methods in the SnmpHelper class, those
are _set_ and  _walk_. As you might imagine, they let you set a single
OID and walk a tree based at a given OID.

The syntax is deliberately simple, all you need to do is feed it a base
OID (as a string) and for the _set_ method, an SNMP type (since that is
required when setting values) and the value as a string or integer.


```
// update SNMPv2-MIB::sysLocation.0
helper.set("1.3.6.1.2.1.1.6.0", "string", "Server room 2, rack A1");
```

For numeric types like _INTEGER_ or _GAUGE32_, you can use a string
or an int as the value argument.

Walk is a litte more complicated, you still only give it an OID where to
start the walk, but it returns a SnmpHelperTree object instead, from
which you can retreive a Map with a string key and a string value (all
return values are strings, even those of gauges or integers)

To walk a tree and get the values, do:

```
SnmpHelperTree tree = helper.walk("1.3.6");
Map<String,String> contents = tree.getContents();
```

So what if I don't want to use the defaults?
--------------------------------------------

That's also possible, for v1 and v2c, there isn't much to do, just use
helper.setCommunity("private") to change the community to use to
_private_ and to use SNMPv1, just say helper.setVersion("1").

But what about SNMPv3?
----------------------

SNMPv3 is a bit more complicated, for that you'll need to know what
Authentication mechanism and Privacy mechanism your server uses
as well as the username and all the passwords used. The defaults for
v3 are no authentication, no privacy and no passwords, so in order
to set up (for instance) password authentication using the SHA AuthType,
and the user _xkcd _with the password _correcthorsebatterystaple_ but no
privacy (encryption) you would do:


```
SnmpHelper helper = new SnmpHelper();
helper.setAuthHash("sha")
	.setAuthPassword("correcthorsebatterystaple")
	.setUsername("xkcd")
	.setVersion("3")
	.setAddress("udp:192.168.0.1/161");

String location = helper.get("1.3.6.1.2.1.1.6.0");
```

As you can see, all but the methods that return values are chainable
to make it easier to use. 

SnmpHelper automatically sets the security level depending on what
values you've filled in, if no Privacy is set, then it will not
attempt to use it.

What if there's no values or something goes wrong?
--------------------------------------------------

This is curretly the weak point of this library, I haven't really
built any error checking into it.

As for now, any missing values when doing a get will return null, set
does not do error checking. Walk might even break if no values are
returned, so be careful.

This may be addressed in future versions.
