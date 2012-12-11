import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import orb.quantum.phrox.PhroxMessageHandler;
import orb.quantum.phrox.internal.PhroxPublisher;
import orb.quantum.phrox.internal.PhroxSubscriber;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;


public class PubSubTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Context con = ZMQ.context(1);
		
		ExecutorService serv = Executors.newSingleThreadExecutor();
		
		PhroxPublisher pub = new PhroxPublisher(con, 8080);
		PhroxSubscriber sub = new PhroxSubscriber(con, serv, new PhroxMessageHandler() {
			@Override
			public void close() throws Exception {
			}
			
			@Override
			public void handleData(byte[] data) {
				System.out.println("Recieve:");
				System.out.println(new String(data));
			}
		});
		
		pub.start();
		sub.start();
		
		sub.connect("localhost", 8080);
		
		
		pub.sendMessage("hello world".getBytes());
	}

}
