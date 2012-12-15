package orb.quantum.phrox;

public interface Phrox extends AutoCloseable {

	public void start();

	/**
	 * Can return null if it was unable to subscribe
	 */
	public Subscription subscribe( String addr, int port );

	public void unsubscribe( String addr, int port );

	public void sendMessage(byte[] b);

	public void setHandler( PhroxMessageHandler pmh );

}
