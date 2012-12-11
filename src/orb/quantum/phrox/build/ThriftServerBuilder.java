package orb.quantum.phrox.build;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.AbstractServerArgs;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;


public class ThriftServerBuilder {

	protected int _port = 8080;
	protected ThriftProtocol _proto = ThriftProtocol.Compact;
	
	protected boolean _https = true;
	
	public ThriftServerBuilder(){}
	
	public ThriftServerBuilder port( int port ){
		_port = port;
		return this;
	}
	
	public ThriftServerBuilder https( boolean use ){
		_https = use;
		return this;
	}
	
	public ThriftServerBuilder protocol( ThriftProtocol proto ){
		_proto = proto;
		return this;
	}
	
	public ThriftServerBuilder copy( ThriftClientBuilder pcb ){
		_port = pcb._port;
		_proto = pcb._proto;
		_https = pcb._https;
		return this;
	}
	
	public TServer build( TProcessor proc ) throws TTransportException{
		
		TServer serv;
		
		if( _https ){
			TServerSocket socket = TSSLTransportFactory.getServerSocket(_port);
			TServer.Args args = new TServer.Args(socket);
			setupArgs(args, proc);
			serv = new TSimpleServer(args);
		}else{
			//TNonblockingServerSocket socket = new TNonblockingServerSocket(_port);
			//ThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(socket);
			TServerSocket socket = new TServerSocket(_port);
			TServer.Args args = new TServer.Args(socket);
			
			setupArgs(args, proc);
			//serv = new TThreadedSelectorServer(args);
			serv = new TSimpleServer(args);
		}
		
		return serv;
	}
	
	private void setupArgs( AbstractServerArgs<?> args, TProcessor proc ){
		
		TProtocolFactory protocol;
		switch( _proto ){
			default:
				System.err.println("Unable to determine protocol type: " + _proto);
			case Binary:
				protocol = new TBinaryProtocol.Factory(true,true);
				break;
			case Compact:
				protocol = new TCompactProtocol.Factory();
				break;
			case Json:
				protocol = new TJSONProtocol.Factory();
				break;
		}
		args.protocolFactory(protocol);
		
		args.processor(proc);
		
		args.transportFactory( new TFramedTransport.Factory() );
	}
	
}
