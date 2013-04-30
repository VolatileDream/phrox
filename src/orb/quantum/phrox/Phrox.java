package orb.quantum.phrox;

import orb.quantum.phrox.internal.thrift.Authorization;
import orb.quantum.phrox.internal.thrift.NotAuthorized;

public interface Phrox extends AutoCloseable {

	public void start();

	/**
	 * @see #subscribe(Authorization,String,int)
	 */
	public Subscription subscribe( String addr, int port ) throws NotAuthorized;

	/**
	 * Attempts to connect to the given address and port, by either:
	 * <ol>
	 * 	<li>Connecting directly</li>
	 *  <li>Using a Connector to see where it should be connecting</li>
	 * </ol>
	 * These options are configured via the {@link PhroxBuilder}
	 * @param addr The address to subscribe to
	 * @param port The port to subscribe to
	 * @return 
	 * @throws NotAuthorized If a connector is being used, and
	 * requires authorization.
	 */
	public Subscription subscribe( Authorization auth, String addr, int port ) throws NotAuthorized;
	
	public void unsubscribe( String addr, int port );

	public void sendMessage(byte[] b);

	public void setHandler( PhroxMessageHandler pmh );

}
