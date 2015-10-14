package com.ddfplus.service.usersettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class UserSettingsServiceTest {

	private UserSettingsServiceImpl service;

	@Before
	public void setUp() throws Exception {
		service = new UserSettingsServiceImpl();
	}

	@Test
	public void getUserSettings() {
		service.setQueryUrl("src/test/resources/usersettings-response.xml");

		UserSettings userSettings = service.getUserSettings("dlucek", "pass");
		assertNotNull(userSettings);

		assertEquals("dlucek", userSettings.getUserName());
		assertEquals("pass", userSettings.getPassword());
		assertEquals("qs-us-e-01.aws.barchart.com", userSettings.getStreamPrimaryServer());
		assertEquals("qs01.aws.ddfplus.com", userSettings.getStreamSecondaryServer());
		assertEquals("qs02.aws.ddfplus.com", userSettings.getRecoveryServer());
		assertEquals("qsws-us-e-01.aws.barchart.com", userSettings.getWssServer());
	}

}
