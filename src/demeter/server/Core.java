package demeter.server;

import demeter.worker.Crawler;

public class Core {
	public static void main(String[] args) throws Exception {
		Crawler crawler = new Crawler();
		crawler.stream(new String[]{"@FAZ_NET","@tagesschau","@dpa","@SZ", "@SPIEGELONLINE"},
				new long[]{114508061,18016521,5734902,40227292,2834511});
    }
}
