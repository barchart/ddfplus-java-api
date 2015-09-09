/**
 * Copyright (C) 2004 - 2015 by Barchart.com, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Barchart.com, Inc.
 * Use is subject to license terms.
 */
package com.ddfplus.service.usersettings;

/**
 * Retrieves a user's Barchart profile information from the Barchart servers.
 *
 */
public interface UserSettingsService {

	UserSettings getUserSettings(String userName, String password);
}
