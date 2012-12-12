import java.security.MessageDigest;

import orb.quantum.phrox.PhroxMessageHandler;
import orb.quantum.phrox.internal.PhroxDeduplicator;


public class DuplicateFilterTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		PhroxDeduplicator filter = new PhroxDeduplicator(MessageDigest.getInstance("MD5"), new PhroxMessageHandler() {
			@Override
			public void close() throws Exception {}
			
			@Override
			public void handleData(byte[] data) {
				System.out.println( new String(data) );
			}
		});
		
		byte[] data = "Hello World".getBytes();
		
		filter.handleData(data);
		Thread.sleep(5_000);
		filter.handleData(data);
		Thread.sleep(62_000);
		filter.handleData(data);
		
		filter.close();
	}

}
