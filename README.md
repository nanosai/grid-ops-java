# Grid Ops - Java
Grid Ops is an open source Java toolkit for creating advanced distributed systems.
Grid Ops consists of a set of core tools for the implementation of
distributed systems in general, and a set of commonly used distributed
systems infrastructure services. We want your distributed systems to
get up and running as quickly as possible.

## IAP
Grid Ops is centered around the general purpose network protocol IAP.
IAP is designed to have a compact message format which can be used
for many different use cases.

IAP can be extended via "semantic protocols". A semantic protocol is a set
of messages and message exchange patterns targeted at a specific distributed
use case.

All semantic protocols use the same core IAP message format: An ION Object
field with nested ION fields. What nested fields a given message contains
 depends on the concrete semantic protocol and message type, but the formatting
 of a message (ION Object field) and its nested fields is the same.
Thus the same message reading / writing tools can be used across all protocols.
This greatly simplifies the implementation of the Java APIs for IAP and the
various semantic protocols.

The IAP core message format is similar to using JSON objects for all
messages across all semantic protocols, but leaving it up to each semantic
to decide what fields each message (JSON object) should contain. IAP
just uses ION objects instead of JSON objects. ION is similar to JSON
except ION is binary and contains more field types than JSON.


## Java vs. Other Programming Languages
Grid Ops is implemented in Java but the use of IAP means that toolkits
in other programming languages can interact with Grid Ops - as long as those
toolkits understand IAP too.


## Grid Ops Road Map
Grid Ops is a living project. The project will address the basics of distributed
systems first, like simple asynchronous message exchange, discovery, relocation
of clients and services, etc.

Once the basics are addressed the project will move on to more advanced
distributed problems like security, authentication, authorization, RSync,
P2P networks and other more complicated distributed problems.

The roadmap isn't fully fixed, but the ambitions are clear:
To make the implementation of advanced distributed systems much simpler.


## Grid Ops by Nanosai.com
Grid Ops is developed by Nanosai.com.

## Grid Ops License
Grid ops is released under an Apache 2.0 License.


