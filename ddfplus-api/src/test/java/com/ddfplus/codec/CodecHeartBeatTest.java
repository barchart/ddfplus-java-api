package com.ddfplus.codec;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.ddfplus.messages.DdfMarketBase;

public class CodecHeartBeatTest {

	@Test
	public void process20OpenMessage() {
		final byte[] msg = "\u0001!20160918050109\u0003".getBytes();
		DdfMarketBase ddf = Codec.parseMessage(msg);
		assertNull("Should not be parsed.", ddf);
	}

}
