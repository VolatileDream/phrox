package orb.quantum.phrox;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import orb.quantum.phrox.internal.PhroxConnector;
import orb.quantum.phrox.internal.PhroxDeduplicator;
import orb.quantum.phrox.internal.PhroxImpl;
import orb.quantum.phrox.internal.PhroxNetworkedSender;
import orb.quantum.phrox.internal.PhroxPublisher;
import orb.quantum.phrox.internal.PhroxSubscriber;
import orb.quantum.phrox.internal.TimeProvider;

import org.apache.thrift.transport.TTransportException;
import org.zeromq.ZMQ.Context;


public class PhroxBuilder {

	public static final TimeProvider SYSTEM_TIME_PROVIDER = new TimeProvider() {
		@Override
		public long getCurrentTime() {
			return System.currentTimeMillis();
		}
	};
	
	private MessageDigest digest = null;

	private boolean useConnector;
	private int connectorPort;

	private int messengerPort = -1;
	
	private PhroxMessageHandler messageHandler;

	public Phrox build( Context context ) {
		
		MessageDigest hashAlgo = digest;
		
		if( messengerPort <= 0 ){
			throw new IllegalArgumentException("Messenger port must be set");
		}
		
		try{
			String address = getLocalIP();

			PhroxPublisher publisher = new PhroxPublisher(context, messengerPort);
			
			PhroxMessageHandler handler = null;

			if( hashAlgo == null ){
				System.err.println("No hashing algorithm set, defaulting to MD5");
				hashAlgo = MessageDigest.getInstance("MD5");
			}

			// no point to build it if no one will handle the messages...
			if( messageHandler != null ){
				// send to everyone we know
				handler = new PhroxNetworkedSender(publisher, messageHandler);
				// after having filtered out duplicates
				handler = new PhroxDeduplicator(SYSTEM_TIME_PROVIDER, hashAlgo, handler);
			}
			
			PhroxSubscriber subscriber = new PhroxSubscriber(context, handler);

			PhroxConnector connect = null;

			if( useConnector ){
				connect = new PhroxConnector(address, connectorPort, messengerPort, subscriber);
			}

			return new PhroxImpl(context, connect, publisher, subscriber, hashAlgo);
			
		} catch (SocketException | TTransportException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the Hashing Algorithm to be used for
	 * filtering out duplicate messages
	 * @param md The Hashing Algorithm to use
	 */
	public PhroxBuilder filterOutDuplicatesWith( MessageDigest md ){
		digest = md;
		return this;
	}

	public PhroxBuilder noConnector(){
		useConnector = false;
		return this;
	}

	/**
	 * Sets the port to bind to when listening for
	 * new subscriptions. This is used to create
	 * a connected graph, and ensure that we 
	 * subscribe to all the people that are
	 * subscribed to us.
	 * @param port To use for the "Mutual Subscription Protocol"
	 */
	public PhroxBuilder useConnectorOnPort( int port ){
		connectorPort = port;
		useConnector = true;
		return this;
	}

	public PhroxBuilder useMessageHandler( PhroxMessageHandler handler ){
		messageHandler = handler;
		return this;
	}
	
	public PhroxBuilder publishMessagesOnPort( int port ){
		messengerPort = port;
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
