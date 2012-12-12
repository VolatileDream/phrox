import java.security.MessageDigest;

import orb.quantum.phrox.PhroxBuilder;
import orb.quantum.phrox.PhroxMessageHandler;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;


public class PhroxTest {

	public static void main(String[] args) throws Exception{
		
		PhroxBuilder builder = new PhroxBuilder()
			
			.filterOutDuplicatesWith( MessageDigest.getInstance("MD5"))
			
			.useMessageHandler(new PhroxMessageHandler() {
				@Override
				public void close() throws Exception {}
				
				@Override
				public void handleData(byte[] data) {
					System.out.println(new String(data));
				}
			});
		
		Context context = ZMQ.context(1);
	
		orb.quantum.phrox.Phrox ph = builder.useConnectorOnPort(8080).publishMessagesOnPort(8081).build(context);
	
		orb.quantum.phrox.Phrox ph2 = builder.useConnectorOnPort(8083).publishMessagesOnPort(8084).build(context);
		
		ph.start();
		ph2.start();
		
		System.out.println("Started");
		
		ph2.connect("192.168.1.5", 8080);

		System.out.println("Connected");
		
		ph.sendMessage("hello world".getBytes());
	
		Thread.sleep(1000);
		
		ph.close();
		System.out.println("CLOSED");
	}
	
}
