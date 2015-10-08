package com.ddfplus.net;

/**
 * Resolves Barchart symbol shortcuts to actual symbol(s).
 * 
 * @see http://www.barchartmarketdata.com/client/protocol_symbol.php
 * 
 *      <pre>
 * 
 * Logic for adjusting symbols, after shortcuts, to Jerq DDF feed symbol.  
 * Verify it's a numeric, then look for month code, then before that is the root.
 * 
 *  RBZ15 - start at back year - 15, month Z - December, therefore root is RB
 *	RBZ5 - start at back year - 5, extrapolate to be 15, month Z - December, therefore root is RB
 *	RBXZ5 - year - 5, extrapolate to be 15, month Z - December, therefore root is RBX
 *	RZ15  - start at back year - 15, month Z - December, therefore root is RB
 *	RZ5 - start at back year - 5, extrapolate to be 15, month Z - December, therefore root is R
 *	When deciding the full year, assuming it's the current year.
 *
 *  For Futures contracts, currently only Natural Gas (NG), that trade out more than
 *  10-years DDF will use alternate contract months
 * 
 *    'A','B','C','D','E','I','L','O','P','R','S','T' = 'Jan' ... 'Dec'
 * 
 * NGZ0 - Natural Gas Dec 2010
 * NGT0 - Natural Gas Dec 2020
 *      </pre>
 */
public interface SymbolShortCuts {
	String[] resolveShortCutSymbols(String symbol);
}
