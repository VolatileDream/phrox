package orb.quantum.phrox.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.security.MessageDigest;

import orb.quantum.phrox.PhroxMessageHandler;

import org.junit.Test;

public class DuplicationFilterTest {

	private TimeProvider buildProvider(final long[] times ){
		return new TimeProvider() {
			private final long[] timeArray = times;
			private int index = 0;
			@Override
			public long getCurrentTime() {
				return timeArray[index++];
			}
		};
	}

	private static final byte[] data = "hello world".getBytes();

	@Test
	public void filtersDuplicateUnderTimeout() throws Exception{

		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		MessageDigest digest = mock(MessageDigest.class);

		when(digest.digest(data)).thenReturn(data);

		TimeProvider provider = buildProvider(new long[]{ 1000, 2000 });

		PhroxDeduplicator dedup = new PhroxDeduplicator(provider,digest,handler);

		dedup.handleData(data);
		dedup.handleData(data);

		verify(handler, times(1)).handleData(data);
	}

	@Test
	public void allowDuplicateAfterTimeout() throws Exception{

		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		MessageDigest digest = mock(MessageDigest.class);

		when(digest.digest(data)).thenReturn(data);

		TimeProvider provider = buildProvider(new long[]{ 1000, 2000+PhroxDeduplicator.TIMEOUT });

		PhroxDeduplicator dedup = new PhroxDeduplicator(provider,digest,handler);

		dedup.handleData(data);
		dedup.handleData(data);

		verify(handler, times(2)).handleData(data);
	}

	@Test
	public void allowDifferentMessages() throws Exception{

		byte[] otherdata = "stuff".getBytes();
		
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		MessageDigest digest = mock(MessageDigest.class);
		TimeProvider provider = mock(TimeProvider.class);

		when(digest.digest(otherdata)).thenReturn(otherdata);
		when(digest.digest(data)).thenReturn(data);
		
		PhroxDeduplicator dedup = new PhroxDeduplicator(provider,digest,handler);

		dedup.handleData(otherdata);
		dedup.handleData(data);

		verify(handler, times(1)).handleData(data);
		verify(handler, times(1)).handleData(otherdata);
	}
	
	@Test
	public void ensureProperHandlerClose() throws Exception{

		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		MessageDigest digest = mock(MessageDigest.class);
		TimeProvider provider = mock(TimeProvider.class);

		PhroxDeduplicator dedup = new PhroxDeduplicator(provider,digest,handler);

		dedup.close();

		verify(handler, times(1)).close();
	}

}
