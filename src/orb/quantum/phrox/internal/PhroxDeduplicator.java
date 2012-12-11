package orb.quantum.phrox.internal;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import orb.quantum.phrox.PhroxMessageHandler;

public class PhroxDeduplicator implements PhroxMessageHandler {

	private static final long TIMEOUT = 60_000;

	private final MessageDigest _digest;
	private final PhroxMessageHandler _subHandler;

	private final Map<byte[], Long> hashTimeMap = new HashMap<>();

	public PhroxDeduplicator( PhroxDeduplicator dedup, PhroxMessageHandler handler){
		this._digest = dedup._digest;
		this._subHandler = handler;
		this.hashTimeMap.putAll( dedup.hashTimeMap );
	}
	
	public PhroxDeduplicator( MessageDigest dig, PhroxMessageHandler handler) {
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

		Long previous = hashTimeMap.get(hash);
		
		// put the current thing in.
		hashTimeMap.put(hash, now);

		return previous != null && now <= previous + TIMEOUT;
	}

	@Override
	public void close() throws Exception {
		hashTimeMap.clear();
	}

}
