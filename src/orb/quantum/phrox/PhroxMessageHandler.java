package orb.quantum.phrox;

public interface PhroxMessageHandler extends AutoCloseable{

	public void handleData( byte[] data );
	
}
