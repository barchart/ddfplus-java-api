package com.ddfplus.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.ddfplus.messages.CtrlTimestamp;
import com.ddfplus.messages.DdfMarketBase;

public class DataMasterTest {

	private DataMaster dataMaster;

	@Before
	public void setUp() throws Exception {
		dataMaster = new DataMaster(MasterType.Realtime);
	}

	@Test
	public void processMessageEmptyBytes() {
		byte[] data = null;
		FeedEvent fe = dataMaster.processMessage(data);
		assertNull(fe);
		data = new byte[1];
		fe = dataMaster.processMessage(data);
		assertNull(fe);
	}

	@Test
	public void processMessageDdfMessageIsAlwaysSaved() {

		DdfMarketBase msg = null;
		FeedEvent fe = dataMaster.processMessage(msg);
		assertNull(fe);
		// Set EOD
		dataMaster = new DataMaster(MasterType.EndOfDay);
		msg = new CtrlTimestamp(null);
		fe = dataMaster.processMessage(msg);
		assertNotNull(fe);
		assertTrue(fe.isDdfMessage());
		assertEquals(msg, fe.getDdfMessage());

	}

}
