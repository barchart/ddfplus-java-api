# DDF Plus Java API

  This API provides access to the Barchart streaming market data services.  

Please see [ddfplus-examples/README.md](ddfplus-examples/README.md)


## Latest API Release

Releases are available on maven central.

#### Core Library

<dependency>
   <groupId>com.ddfplus.jaws</groupId>
   <artifactId>ddfplus-api</artifactId>
   <version>version_string</version>
</dependency>


#### Examples Distribution

<dependency>
   <groupId>com.ddfplus.jaws</groupId>
   <artifactId>ddfplus-examples</artifactId>
   <version>version_string</version>
</dependency>


## Version History 

### 1.0.9
* Added support for .F symbols as FOREX

### 1.0.8
* Changed MarketCondition from 1 to 9 for NASDAQ

### 1.0.7
* Reset market condition on the quote when settlement or open occurs
* Added MarketConditions for NASDAQ

### 1.0.6
* Fix for settlement field on Session.clone()
* Add read timeout on Web Socket connection

### 1.0.5
* Add Market Events for Trading Halt and Suspension

### 1.0.4
* Additional web socket logging
* Added uncaught exception handler

### 1.0.3
* TCP read timeouts correction to properly handle a failing connection.
* Updated Web Socket library.

### 1.0.2
* Updated documentation.

### 1.0.1
* Adds support for JERQ symbol short cuts.
* Improves Web Socket re-connection on failure.

### 1.0.0
* Initial version
