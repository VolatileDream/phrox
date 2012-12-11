package orb.quantum.phrox.build;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ThriftClientBuilder {

	// set reasonable defaults
	
	protected int _port = 8080;
	protected String _address;
	protected ThriftProtocol _proto = ThriftProtocol.Compact;
	
	protected boolean _https = true;
		
	public ThriftClientBuilder(){}
	
	public ThriftClientBuilder port( int port ){
		_port = port;
		return this;
	}
	
	public ThriftClientBuilder https( boolean use ){
		_https = use;
		return this;
	}
	
	public ThriftClientBuilder address( String address ){
		_address = address;
		return this;
	}
	
	public ThriftClientBuilder protocol( ThriftProtocol protocol ){
		_proto = protocol;
		return this;
	} 
	
	public ThriftClientBuilder reset(){
		_port = 8080;
		_address = null;
		_proto = ThriftProtocol.Compact;
		_https = true;
		return this;
	}

	public ThriftClientBuilder copy( ThriftServerBuilder server ){
		_port = server._port;
		_proto = server._proto;
		_https = server._https;
		return this;
	}
	
	public TProtocol build() throws TTransportException{
		TTransport trans;
		if( ! _https ){
			trans = new TSocket(_address, _port);
		}else{
			trans = TSSLTransportFactory.getClientSocket(_address, _port);
		}
	
		trans = new TFramedTransport(trans); 
		
		if( !_https ){
			trans.open();
		}
		
		switch( _proto ){
			default:
				System.err.println("Unable to determine protocol type: " + _proto);
			case Binary:
				return new TBinaryProtocol(trans,true,true);
			case Compact:
				return new TCompactProtocol(trans);
			case Json:
				return new TJSONProtocol(trans);
		}
	}
	
}
