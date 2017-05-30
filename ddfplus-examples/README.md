
# DDF Plus Java API

 The Barchart API provides support for three operational modes:

 1. Pull by Symbol      - Subscribes by individual symbol
 2. Pull by Exchange    - Subscribes for all symbols on an exchange
 3. Server Push         - Pushes all symbols for an exchange to a customer's server


## Pull Mode Provisioning Requirements
  The pull modes require a user login and password, please contact Barchart for credentials.  The pull by exchange mode requires a refresh/snapshot user and password, so the API can refresh the symbol state via a web service.

## Server Push Provisioning Requirements
 The server push mode requires Barchart to push market data to the customer's server and requires the following:

 * Static IP
 * Static Port

 The server push mode does not require a login or password.  Please contact Barchart for provisioning.

## API Requirements
  The API requires Java 8 or higher.

## Contents

* `ddplus-examples-<version>-exe.jar`    - Runnable jar for example code
* `src/**/*.java`                           - Example source code
* `doc/api`                                  - JavaDoc for the API
* `lib/ddplus-api-<version>.jar`          - API jar
* `lib/slf4j-api-1.7.6.jar`                - Required dependency
* `lib/joda-time-2.3.jar`                  - Required dependency
* `lib/logback*.jar`                        - SL4J implementation, optional, required if you want to see internal API logging, will default to nop logging


## How to run the Client example
  This client will connect up to a JERQ middleware server and display market data.

The source code is in `src/**/DdfClientExample.java`.  

To get help

  `java -jar  ddf-examples-<version>-exe.jar -h`

Run in pull by symbol mode for symbols IBM,ESU5

  `java -jar ddf-examples-<version>-exe.jar -u <user> -p <pass>  -sym IBM,ESU5  -l a`
* Uses JERQ "GO XXX=YY;SsV" command
* The pull by symbol mode provides the snapshot refresh automatically
* "-l a" will log all messages

Run in pull by exchange mode for exchange NYSE (N) with all logging enabled and background refresh enabled

  `java -jar ddf-examples-<version>-exe.jar -u <feed user> -p <pass> -e N -l a  -su <snaphot user> -sp <snapshot password>`
* Uses JERQ "STREAM LISTEN XXX" command
* The push by exchange mode will call back the FeedHandler interface for every DDF message received.
* If the snapshot credentials are provided then a snapshot/refresh request will be sent in the background which will refresh the local caches and then call back the QuoteHandler interface


## How to run the Server example
  The server example API is used by clients who will receive market data directly from Barchart's internal servers.
The source code is in `src/**/ServerListenExample.java`.  

  `java -cp ddf-examples-<version>-exe.jar com.ddfplus.api.examples.ServerListenExample LISTEN_TCP|LISTEN_UDP address port [interface] [-su user] [-sp password]`

where:
* LISTEN_TCP|LISTEN_UDP - Specify whether the inbound packets are TCP or UDP
* address               - Specify the Local Address to bind to. Use 0.0.0.0 for any
* port                  - Specify the port to bind to
* interface             -(Optional) Only if address is a multicast address.\n" + "")
* -su user              - Snapshot User Name
* -sp password          - Snapshot Password

  This command will listen on port 10110 for inbound TCP market feed messages on any interface:

   `java -cp ddfplus-examples-<version>-exe.jar com.ddfplus.api.examples.ServerExample LISTEN_TCP  0.0.0.0  10110`

   If the refresh/snapshot user and password are set then in the background a refresh request will be sent to obtain the statistics such as (hi,low, etc..).

## How to Build the Example Code

* Create a Java project in your favorite IDE
* Add src/main/java to the source directories
* Add all the jars in lib to the project classpath
* If you want to see the API internal logging, add a SLF7 implementation such as `logback-classic-<version>.jar`
* The project should now build and run.

  If you do not have a SL4J implementation jar you will see an error similar to the one below.  The API will operate correctly just without internal logging.

> SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
> SLF4J: Defaulting to no-operation (NOP) logger implementation
>  SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.


## How to build from source

* Clone the Github project
* Load JDK 1.8
* Load Maven 3.2.5++
* mvn clean install

# Using the API

Please see DdfClientExample.java for a running example of using the API.

## Getting Started
 Please look at the DdfClientExample constructor for initialization.
 
 1. Create a ClientConfig class
   * Set the user name and password
   * Set the snapshot/refresh user name and password if using the Pull by Exchange mode.
 2. Create the DdfClientImpl
 3. Call the DdfClientImpl.init() method
 4. Add response handlers as required, normally you would add the following:
  * ConnectionEventHandler 
  * TimestampHandler
  * FeedHandlerImpl
  * MarketEventHandlerImpl
 5. Call the DdfClientImpl.connect() method
 6. On the connection event handler LOGIN_SUCESS callback add response handlers for each symbol or exchange.
   * Create a new ClientQuoteHandler()
   * Add the handler to the client using DdfClientImpl.addQuoteHandler(symbol,quoteHandler)
 7. The handlers will be called back as data arrives. 


```Java

ClientConfig config = new ClientConfig();
config.setUserName("username");
config.setPassword("password");

// Use TCP
DdfClient client = new DdfClientImpl(config);

// Init
client.init();

// Add Handlers, assuming enclosing class implements the handlers
client.addConnectionEventHandler(this);
client.addTimestampHandler(this);
client.addFeedHandler(this);
client.addMarketEventHandler(this);

// Connection
client.connect();


/* On the ConnectionEventHandler.onEvent() LOGIN_SUCESS subscribe
to symbols as desired
*/
QuoteHandler handler = new ClientQuoteHandler();
// This will request quotes if client does not have a subscription
// to the symbol
client.addQuoteHandler(symbol, handler);

```

## Available Response Handlers

  1. ConnectionEventHandler - Connection Events, see ConnectonEvent.java
  2. TimestampHandler - Callback on time stamps sent by the server
  3. FeedHandler - Callback on each DDF message
  4. QuoteHandler - Callback with Quote which contains BBO plus other statistics
  5. MarketEventHandler - Callback for market events such as Open, High, etc.. See MarketEvent.java
  6. BookQuoteHandler - Callback for Depth messages.
  7. MinuteBarHandler - Callback for OHLC minute bars

## Subscribing to market data by symbol

	1. Create a ClientQuoteHandler()
	2. Add to client by calling DdfClientImpl.addQuoteHandler(symbol, handler)


## Subscribing to market data by exchange

	1. Create a ClientQuoteExchangeHandler(String exchangeCode)
	2. Add to client by calling DdfClientImpl.addQuoteExchangeHandler(exchangeCode, handler)


