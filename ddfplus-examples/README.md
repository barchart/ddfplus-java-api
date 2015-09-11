
DDF PLUS API
-----------
  This API provides access to the Barchart streaming market data services. 

Contents
-------- 
ddplus-examples-<version>-exe.jar - Runnable jar for example code
src/**/*.java                     - Example source code
doc/api                           - JavaDoc for the API
lib/ddplus-api-<version>.jar      - API jar
lib/slf4j-api-1.7.6.jar           - Required dependency
lib/joda-time-2.3.jar             - Required dependency
lib/logback*.jar                  - SL4J implementation, optional, required if you want to see internal API logging, will default to nop logging

API Requirements
----------------
 The Barchart API requires a user login and password.  When using "push" mode a snaphot user and password are required.
This API requires a Java 7 runtime.
 
How to run the Client example
-----------------------------
  This client will connect up to a JERQ middleware server and display market data.
The source code is in src/**/ClientExample.java.  

To get help
  java -jar  ddf-examples-<version>-exe.jar -h
  
Run in "pull" mode for symbols IBM,ESU5
  java -jar ddf-examples-<version>-exe.jar -u <user> -p <pass>  -sym IBM,ESU5  -l a
	- Uses JERQ "GO XXX=YY;SsV" command
    - The "pull" mode provides the snapshot refresh automatically
    - "-l a" will log all messages
    
Run in "push" mode for exchange NYSE (N) 
  java -jar ddf-examples-<version>-exe.jar -u <feed user>  -p <pass>  -su <snapshot user> -sp <pass> -e N  -l a

   - Uses JERQ "STREAM LISTEN XXX" command
   - The push mode requires the snapshot credentials for the background snapshot refresh/statistics
   -su/-sp is the Snapshot User and password


How to run the Server example
-----------------------------
  The server example API is used by clients who will receive market data directly from Barchart's internal servers.
The source code is in src/**/ServerListenExample.java.  

  java -cp ddf-examples-<version>-exe.jar com.ddfplus.api.examples.ServerListenExample LISTEN_TCP|LISTEN_UDP address port [interface] [-su user] [-sp password]

where:
  LISTEN_TCP|LISTEN_UDP - Specify whether the inbound packets are TCP or UDP
  address               - Specify the Local Address to bind to. Use 0.0.0.0 for any
  port                  - Specify the port to bind to
  interface             -(Optional) Only if address is a multicast address.\n" + "")
  -su user              - Snapshot User Name
  -sp password          - Snapshot Password
    
  This command will listen on port 10110 for inbound TCP market feed messages on any interface:
  
   java -cp ddfplus-examples-<version>-exe.jar com.ddfplus.api.examples.ServerExample LISTEN_TCP  0.0.0.0  10110
   
   If the snapshot user and password are set then in the background a refresh request will be sent to obtain the statistics such as (hi,low, etc..).
   
How to build the example code
-----------------------------
 This version of the API jar is currently not in the Maven public repositories.
 
- Create a Java project in your favorite IDE
- Add src/main/java to the source directories
- Add all the jars in lib to the project classpath
- If you want to see the API internal logging, add a SLF7 implementation such as logback-classic-<version>.jar
- The project should now build and run.

  If you do not have a SL4J implementation jar you will see an error similar to the one below.  The API will operate correctly just without internal logging.
  
  SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
  SLF4J: Defaulting to no-operation (NOP) logger implementation
  SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.


  