package orb.quantum.phrox.internal;

import orb.quantum.phrox.build.ThriftClientBuilder;
import orb.quantum.phrox.build.ThriftProtocol;
import orb.quantum.phrox.build.ThriftServerBuilder;
import orb.quantum.phrox.internal.thrift.Authorization;
import orb.quantum.phrox.internal.thrift.NotAuthorized;
import orb.quantum.phrox.internal.thrift.PhroxConnectionHandler;
import orb.quantum.phrox.internal.thrift.PhroxLocation;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransportException;

public class PhroxConnector implements PhroxConnectionHandler.Iface, AutoCloseable {

	private static final boolean https = false;
	private static final ThriftProtocol protocol = ThriftProtocol.Binary;
	
	private final String _localAddress;
	private final int _pubPort;
	private final PhroxSubscriber _subscriber;
	private final TServer _server;
	
	public PhroxConnector(String localAddress, int replyPort, int pubPort, PhroxSubscriber msg) throws TTransportException{
		_localAddress = localAddress;
		_pubPort = pubPort;
		_subscriber = msg;
		
		ThriftServerBuilder builder = new ThriftServerBuilder();
		builder
			.https(https)
			.port(replyPort)
			.protocol(protocol);
		_server = builder.build( new PhroxConnectionHandler.Processor<PhroxConnectionHandler.Iface>( this ) );
	}

	public void start(){
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				_server.serve();
			}
		});
		t.start();
	}
	
	public void connect( String host, int port ) throws TException{
		ThriftClientBuilder builder = new ThriftClientBuilder();
		builder
			.address(host)
			.https(https)
			.port(port)
			.protocol(protocol);
		TProtocol protocol = builder.build();
		
		PhroxConnectionHandler.Client client =
				new PhroxConnectionHandler.Client.Factory().getClient(protocol);
		
		try {
			PhroxLocation local = new PhroxLocation(_localAddress, _pubPort);
			PhroxLocation remote = client.connect(new Authorization(), local);
			System.out.println("PhroxConnector:" + remote +" - " + local);
			_subscriber.connect(remote.host, remote.port);
		} catch (NotAuthorized e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public PhroxLocation connect(Authorization auth, PhroxLocation location) throws NotAuthorized, TException {
		//TODO check auth
		boolean authorized = true;
		
		if( authorized ){
			_subscriber.connect(location.host, location.port);
			return new PhroxLocation(_localAddress, _pubPort);
		}
		
		return null;
	}

	@Override
	public void close() throws Exception {
		_server.stop();
	}
}
