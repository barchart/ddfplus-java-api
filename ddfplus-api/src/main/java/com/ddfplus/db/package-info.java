/**
 * The ddfplus.db package contains the classes necessary to keep an
 * active, in-memory database. The database itself is handled by the
 * <code>DataMaster</code> class. Messages from the codec
 * package are then passed into processMessage(Message),
 * and these are in turn interpreted, and processed. Further, the method
 * passes back a corresponding Quote, BookQuote, etc. object.
 */
package com.ddfplus.db;

