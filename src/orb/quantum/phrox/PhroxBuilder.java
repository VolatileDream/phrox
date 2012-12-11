package orb.quantum.phrox;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;

import orb.quantum.phrox.internal.PhroxConnector;
import orb.quantum.phrox.internal.PhroxDeduplicator;
import orb.quantum.phrox.internal.PhroxImpl;
import orb.quantum.phrox.internal.PhroxPublisher;
import orb.quantum.phrox.internal.PhroxSubscriber;

import org.apache.thrift.transport.TTransportException;
import org.zeromq.ZMQ.Context;


public class PhroxBuilder {

	private MessageDigest digest;
	private ExecutorService exec;
	private int connectorPort;
	private int messengerPort;
	
	public Phrox build( Context context ) {
		String address;
		try {
			address = getLocalIP();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		
		PhroxDeduplicator handler = new PhroxDeduplicator(digest, null);
		
		PhroxSubscriber sub = new PhroxSubscriber(context, exec, handler);
		
		PhroxConnector connect;
		try {
			connect = new PhroxConnector(address, connectorPort, messengerPort, sub);
		} catch (TTransportException e) {
			throw new RuntimeException(e);
		}
		
		PhroxPublisher msg = new PhroxPublisher(context, messengerPort);
		
		return new PhroxImpl(context, connect, msg, sub, digest);
	}
	
	public PhroxBuilder messageDigest( MessageDigest md ){
		digest = md;
		return this;
	}
	
	public PhroxBuilder connectionPort( int port ){
		connectorPort = port;
		return this;
	}
	
	public PhroxBuilder messagePort( int port ){
		messengerPort = port;
		return this;
	}
	
	public PhroxBuilder executorService( ExecutorService service ){
		exec = service;
		return this;
	}
	
    private static String getLocalIP() throws SocketException
    {
        Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
        if (nifs == null) throw new RuntimeException("No local network interfaces");

        while (nifs.hasMoreElements())
        {
            NetworkInterface nif = nifs.nextElement();
            // We ignore subinterfaces - as not yet needed.

            Enumeration<InetAddress> adrs = nif.getInetAddresses();
            while (adrs.hasMoreElements())
            {
                InetAddress adr = adrs.nextElement();
                if (adr != null && !adr.isLoopbackAddress() && (nif.isPointToPoint() || !adr.isLinkLocalAddress()))
                {
                    return adr.getHostAddress();
                }
            }
        }
        throw new RuntimeException("Not connected to any network");
    }
	
}
