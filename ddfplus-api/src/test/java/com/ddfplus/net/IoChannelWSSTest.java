package com.ddfplus.net;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ddfplus.enums.ConnectionType;

public class IoChannelWSSTest {

	private IoChannelWSS channel;
	private Connection connection;
	private SymbolProvider symbolProvider;

	@Before
	public void setUp() throws Exception {
		symbolProvider = new SymbolProviderImpl();
		InetAddress address = InetAddress.getByName("qsws-us-e-01.aws.barchart.com");
		connection = new Connection(ConnectionType.WSS, "ddftest", "pass", address, 80, null, symbolProvider, null);
		connection.setVersion(NetConstants.JERQ_VERSION_DEFAULT);
		channel = new IoChannelWSS(connection);
	}

	@Test
	@Ignore
	public void login() throws InterruptedException {
		channel.start();
		Thread.currentThread().join();
	}
}
