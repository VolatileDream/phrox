package orb.quantum.phrox.internal;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * 
 * This class handles sending and receiving messages
 * 
 */
public class PhroxPublisher implements AutoCloseable {

	private final int _pubPort;
	private final Socket _serverSocket;
	
	public PhroxPublisher( Context con, int pubPort ){
		_serverSocket = con.socket(ZMQ.PUB);
		_pubPort = pubPort;
	}
	
	public void start(){
		_serverSocket.bind("tcp://*:" + _pubPort );
	}
	
	public void sendMessage(byte[] b) {
		_serverSocket.send(b, 0);
	}

	@Override
	public void close() throws Exception {
		_serverSocket.close();
	}

}
