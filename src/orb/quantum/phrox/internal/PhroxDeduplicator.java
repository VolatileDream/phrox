package orb.quantum.phrox.internal;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import orb.quantum.phrox.PhroxMessageHandler;

public class PhroxDeduplicator implements PhroxMessageHandler {

	public static final long TIMEOUT = 60_000;

	private final TimeProvider _provider;
	private final MessageDigest _digest;
	private final PhroxMessageHandler _subHandler;

	private final Map<HashCode, Long> hashTimeMap = new HashMap<>();

	public PhroxDeduplicator( PhroxDeduplicator dedup, PhroxMessageHandler handler){
		this._provider = dedup._provider;
		this._digest = dedup._digest;
		this._subHandler = handler;
		this.hashTimeMap.putAll( dedup.hashTimeMap );
	}

	public PhroxDeduplicator(TimeProvider provider, MessageDigest dig, PhroxMessageHandler handler) {
		_provider = provider;
		_digest = dig;
		_subHandler = handler;
	}

	public void handleData( byte[] data ){

		if( _subHandler != null && ! isDuplicate(data) ){
			_subHandler.handleData(data);
		}
	}

	private boolean isDuplicate( byte[] data ){

		long now = System.currentTimeMillis();

		_digest.reset();
		byte[] hash = _digest.digest(data);

		HashCode key = new HashCode(hash);

		Long previous = hashTimeMap.get(key);

		// put the current thing in.
		hashTimeMap.put(key, now);

		return previous != null && now <= previous + TIMEOUT;
	}

	@Override
	public void close() throws Exception {
		//maybe these two operations clear memory?
		hashTimeMap.clear();
		_digest.reset();
		if( _subHandler != null ) _subHandler.close();
	}

	private static class HashCode {
		private final byte[] data;
		protected HashCode(byte[] data){
			this.data = data;
		}
		@Override
		public int hashCode(){
			int hashcode=0;
			for( int wrap=0, i=0; i < data.length ; i++, wrap=(wrap+1)%4 ){
				hashcode ^= (data[i] << (4*wrap));
			}
			return hashcode;
		}
		public boolean equals(Object o){
			if( o instanceof HashCode){
				HashCode other = (HashCode) o;
				if( other.data.length == data.length )
					for( int i=0; i < data.length; i++ ){
						if( other.data[i] != data[i] ){
							return false;
						}
					}
				return true;
			}
			return false;
		}
	}

}
