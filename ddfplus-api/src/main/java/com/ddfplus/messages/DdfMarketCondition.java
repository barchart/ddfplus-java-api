/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.messages;

import com.ddfplus.enums.MarketConditionType;

/**
 * DDF Market Condition/Trading Status
 */
public interface DdfMarketCondition extends DdfMarketBase {

	/**
	 * Gets the market condition.
	 * 
	 * @return The Market Condition.
	 */

	MarketConditionType getMarketCondition();

}