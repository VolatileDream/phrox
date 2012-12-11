package orb.quantum.phrox;


import com.lmax.disruptor.EventFactory;

public interface Phrox extends AutoCloseable {

	public void start();
	
	public void connect( String addr, int port );

	public void disconnect( String addr, int port );

	public void sendMessage(byte[] b);

	public void setHandler( PhroxMessageHandler pmh );

	public final static class PhroxEvent {
		
		private byte[] data;
		public byte[] getData(){
			return data;
		}
		public void setData( byte[] d ){
			data = d;
		}
		
		public final static EventFactory<PhroxEvent> INSTANCE_FACTORY = new EventFactory<PhroxEvent>() {
			@Override
			public PhroxEvent newInstance() {
				return new PhroxEvent();
			}
		};
	}


}
