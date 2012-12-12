package orb.quantum.phrox.internal;

import java.security.MessageDigest;

import orb.quantum.phrox.Phrox;
import orb.quantum.phrox.PhroxMessageHandler;

import org.zeromq.ZMQ.Context;

public class PhroxImpl implements Phrox, AutoCloseable {
	
	private final PhroxConnector _connector;
	private final PhroxPublisher _publisher;
	private final PhroxSubscriber _subscriber;
	private final MessageDigest _digest;
	
	public PhroxImpl( Context con, PhroxConnector connector, PhroxPublisher messenger, PhroxSubscriber sub, MessageDigest digest ){
		_publisher = messenger;
		_subscriber = sub;
		_connector = connector;
		_digest = digest;
	}
	
	@Override
	public void start(){
		_publisher.start();
		_subscriber.start();
		if( _connector != null ) _connector.start();
	}
	
	@Override
	public void connect( String addr, int port ) {
		try {
			if( _connector != null ) {
				_connector.connect(addr, port);
			}else{
				_subscriber.connect(addr, port);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void disconnect( String addr, int port ){
		_subscriber.disconnect(addr, port);
	}
	
	@Override
	public void sendMessage(byte[] b){
		_publisher.sendMessage(b);
	}
	
	@Override
	public void setHandler( PhroxMessageHandler pmh ){
		
		// we want to publish it to everyone we know, and then handle it.
		pmh = new PhroxNetworkedSender(_publisher, pmh);
		
		// we want to make sure we filter out duplicate messages
		pmh = new PhroxDeduplicator(_digest,pmh);
		
		// set it all up
		_subscriber.setHandler(pmh);
	}
	
	@Override
	public void close() throws Exception {
		if( _connector != null ) _connector.close();
		_publisher.close();
		_subscriber.close();
	}
}
