package orb.quantum.phrox.internal;

import java.util.concurrent.ExecutorService;

import orb.quantum.phrox.PhroxMessageHandler;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class PhroxSubscriber implements AutoCloseable, Runnable {

	private final Socket _clientSocket;
	private final ExecutorService _service;
	private PhroxMessageHandler _handler;

	private volatile boolean _isRunning = true;

	public PhroxSubscriber( Context con, ExecutorService serv, PhroxMessageHandler handler) {
		_clientSocket = con.socket(ZMQ.SUB);
		_handler = handler;
		_service = serv;
	}

	public void start(){
		_service.execute( this );
		_clientSocket.subscribe("".getBytes());
	}

	@Override
	public void run() {
		byte[] data = _clientSocket.recv(ZMQ.DONTWAIT);

		if( _handler != null && data != null ){
			_handler.handleData(data);
		}

		tryContinue();
	}

	private void tryContinue(){
		if( _isRunning ){
			_service.execute(this);
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
		_clientSocket.close();
		_handler.close();
	}

}
