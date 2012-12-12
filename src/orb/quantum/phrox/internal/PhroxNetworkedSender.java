package orb.quantum.phrox.internal;

import orb.quantum.phrox.PhroxMessageHandler;

public class PhroxNetworkedSender implements PhroxMessageHandler {

	private final PhroxPublisher _publisher;
	private final PhroxMessageHandler _nextHandler;
	
	public PhroxNetworkedSender( PhroxPublisher pub, PhroxMessageHandler next ){
		_publisher = pub;
		_nextHandler = next;
	}
	
	@Override
	public void close() throws Exception {
		if( _nextHandler != null ) _nextHandler.close();
	}

	@Override
	public void handleData(byte[] data) {
		_publisher.sendMessage(data);
		_nextHandler.handleData(data);
	}

}
