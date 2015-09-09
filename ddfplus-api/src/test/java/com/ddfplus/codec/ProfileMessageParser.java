package com.ddfplus.codec;

import com.ddfplus.db.DataMaster;
import com.ddfplus.db.MasterType;
import com.ddfplus.db.Quote;
import com.ddfplus.messages.DdfMarketBase;
import com.ddfplus.messages.DdfMessageBase;

public class ProfileMessageParser {

	public static void emptyVoid() {
		;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Test2();
		System.exit(0);

		// 2,0 byte[] ba = "\u00012SF0,0\u00022B1010530,D0Q \u0003".getBytes();
		// 2,0 byte[] ba = "\u00012SF0,0\u00022B1010540,10QR\u0003".getBytes();
		// 2,1 byte[] ba =
		// "\u00012SF0,1\u00022B10,,,,,-,-,,,,,,,,,Q \u0003".getBytes();
		// 2,2 byte[] ba =
		// "\u00012HIG,2\u0002AN15,2445,2604,2404,2582,,,,2481,,,2582,,,12196949,3 \u0003".getBytes();
		// 2,6 byte[] ba =
		// "\u00012$DJUBSSO,6\u0002Ao10,5515,5642,5501,5631,,,,,,,,,,,S \u0003".getBytes();
		// 2,8 byte[] ba =
		// "\u00012HOZ9,8\u0002CJ1020911,5,20919,1,SG\u0003".getBytes();
		// 2,9 byte[] ba = "\u00012FTNT,9\u0002*Q15A,  H \u0003".getBytes();
		// 3,B byte[] ba =
		// "\u00013XIZ9,B\u00028X55,63795K25,63790L5,63780M1000,63775N35,63765O5,63800J20,63815I5,63820H10,63825G5,63830F5\u0003".getBytes();
		// S,0 byte[] ba =
		// "\u0001SSPZ9|11200C,0\u0002AM10GN2ESZ9|11200C,300,00GR\u0003".getBytes();
		// # byte[] ba = "\u0001#20091123185002\u0003".getBytes();
		// 3,S byte[] ba =
		// "\u00013IBM,S\u0002AN>>,10/07/2009,12112,12285,12094,12278,5967600\u0003".getBytes();

		// 2,0 byte[] ba = "\u00012SF0,0\u00022B1010170,00HR\u0003".getBytes();

		// byte[] ba =
		// "\u00012SK0,3\u00022B10,10380,10380,10254,10300,,10280,,10364,,,,,,,H \u0003".getBytes();

		// byte[] ba =
		// "\u00012SK0,3\u00022B10,10380,10380,10254,10300,,10280,,10364,,,,13147,52796,,H \u0003".getBytes();

		byte[] ba = "\u0001SSPZ9|11200C,0\u0002AM10GN2ESZ9|11200C,300,00GR\u0003".getBytes();

		// byte[] ba = "\u00012$TICX,0\u0002AI10-36800,00T \u0003".getBytes();

		byte[] ms = new byte[] { 20, 73, 75, 67, 80, 88, 116, 63, 2 };
		byte[] bx = new byte[ba.length + ms.length];

		System.arraycopy(ba, 0, bx, 0, ba.length);
		System.arraycopy(ms, 0, bx, ba.length, ms.length);

		long l = 0L;
		long runningTime = 0L;
		long overallTime = 0L;

		TestProcessMessage(ba);

		while (true) {
			long begin = System.nanoTime();
			DdfMessageBase m = Codec.parseMessage(ba);
			// ProfileMessageParser.emptyVoid();
			long end = System.nanoTime();

			long difference = end - begin;
			runningTime += difference;
			overallTime += difference;

			if ((l % 100000 == 0) && (l > 0)) {
				System.out.println(l + ": " + difference + " / " + ((double) runningTime / 100000.0) + " / "
						+ (double) ((double) overallTime / (double) l));
				runningTime = 0L;
			}
			l++;
		}
	}

	private static void Test2() {
		byte[] msg1 = "%<QUOTE symbol=\"ESH2\" name=\"E-Mini S&amp;P 500\" exchange=\"GBLX\" basecode=\"A\" pointvalue=\"50.0\" tickincrement=\"25\" ddfexchange=\"M\" lastupdate=\"20120116182024\" bid=\"129025\" bidsize=\"111\" ask=\"129025\" asksize=\"107\" mode=\"R\"><SESSION day=\"G\" session=\"G\" timestamp=\"20120116122036\" open=\"128525\" high=\"129225\" low=\"128125\" last=\"129150\" previous=\"128900\" tradesize=\"6\" volume=\"175626\" numtrades=\"51228\" pricevolume=\"161528936.75\" tradetime=\"20120116102959\" ticks=\"..\" id=\"combined\"/><SESSION day=\"F\" session=\" \" timestamp=\"20120116115052\" last=\"128900\" previous=\"128900\" settlement=\"128900\" openinterest=\"2613516\" volume=\"2115831\" ticks=\"..\" id=\"previous\"/></QUOTE>"
				.getBytes();
		byte[] msg2 = "\u00012ESH2,1\u0002AM10,,,,,,,,128900,,,,2115831,1000,,F \u0003".getBytes();

		DataMaster dm = new DataMaster(MasterType.Realtime);

		DdfMarketBase m1 = Codec.parseMessage(msg1);
		Quote q1 = dm.processMessage(m1).getQuote();

		DdfMarketBase m2 = Codec.parseMessage(msg2);
		Quote q2 = dm.processMessage(m2).getQuote();

	}

	private static void TestProcessMessage(byte[] ba) {
		String s = "%<QUOTE symbol=\"_S_GN_SPZ9|11200C_ESZ9|11200C\" name=\"SOYBEANS (P)\" exchange=\"CBOT\" flag=\"p\" basecode=\"2\" pointvalue=\"50.0\" tickincrement=\"2\" ddfexchange=\"B\" lastupdate=\"20091218181511\" bid=\"10300\" ask=\"10280\">\n"
				+ "<SESSION day=\"H\" timestamp=\"20091218120512\" previous=\"10364\" id=\"combined\"/>\n"
				+ "<SESSION day=\"G\" timestamp=\"20091217000000\" last=\"10364\" previous=\"10730\" id=\"previous\"/>\n"
				+ "</QUOTE>";

		DataMaster dm = new DataMaster(MasterType.Realtime);
		DdfMarketBase m1 = Codec.parseMessage(s.getBytes());

		Quote q1 = dm.processMessage(m1).getQuote();

		DdfMarketBase m2 = Codec.parseMessage(ba);
		Quote q2 = dm.processMessage(m2).getQuote();

	}
}
