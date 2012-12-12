package orb.quantum.phrox.internal;

import orb.quantum.phrox.PhroxMessageHandler;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class PhroxSubscriber implements AutoCloseable, Runnable {

	private final Socket _clientSocket;
	private Thread _thread;
	private PhroxMessageHandler _handler;

	private volatile boolean _isRunning = true;

	public PhroxSubscriber( Context con, PhroxMessageHandler handler) {
		_clientSocket = con.socket(ZMQ.SUB);
		_handler = handler;
	}

	public void start(){
		_thread = new Thread(this);
		_thread.setDaemon(true);
		_thread.start();
		_clientSocket.subscribe("".getBytes());
		
		// on close this is how long (ms) we wait before trashing the socket + waiting messages
		_clientSocket.setLinger(100);
		
		_clientSocket.setReceiveTimeOut(1000);
	}

	@Override
	public void run() {
		while( _isRunning ){
			// this blocks nicely so we don't waste CPU
			byte[] data = _clientSocket.recv(0);
	
			if( _handler != null && data != null ){
				_handler.handleData(data);
			}
		}
	}

	public void setHandler( PhroxMessageHandler pmh ){
		_handler = pmh;
	}

	public void connect(String host, int port) {
		_clientSocket.connect("tcp://" + host + ":" + port);
	}

	public void disconnect(String host, int port) {
		_clientSocket.disconnect("tcp://" + host + ":" + port);
	}

	@Override
	public void close() throws Exception {
		_isRunning = false;
		if( _thread != null ) _thread.interrupt();
		_clientSocket.close();
		if( _handler != null ) _handler.close();
	}

}
