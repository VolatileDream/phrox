import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import orb.quantum.phrox.PhroxBuilder;
import orb.quantum.phrox.PhroxMessageHandler;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;


public class PhroxTest {

	public static void main(String[] args) throws Exception{
		
		ExecutorService service = Executors.newFixedThreadPool(1);
		
		PhroxBuilder builder = new PhroxBuilder()
			.connectionPort(8080)
			.messagePort(8081)
			.executorService( service )
			.messageDigest( MessageDigest.getInstance("MD5"));
		
		Context context = ZMQ.context(1);
	
		orb.quantum.phrox.Phrox ph = builder.build(context);
	
		orb.quantum.phrox.Phrox ph2 = builder.connectionPort(8083).messagePort(8084).build(context);
		
		ph2.setHandler(new PhroxMessageHandler() {
			@Override
			public void close() throws Exception {}
			
			@Override
			public void handleData(byte[] data) {
				System.out.println("PH recv");
				System.out.println(new String(data));
			}
		});
	
		ph.start();
		ph2.start();
		
		System.out.println("Started");
		
		ph2.connect("192.168.1.5", 8080);

		System.out.println("Connected");
		
		for (int i = 0; i < 15; i++) {
			ph.sendMessage("hello world".getBytes());
			System.out.println("SENT " + i );
			Thread.sleep(1000);
		}
		
		ph.close();
		System.out.println("CLOSED");
	}
	
}
