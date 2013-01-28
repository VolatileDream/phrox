package orb.quantum.phrox.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import orb.quantum.phrox.PhroxMessageHandler;

import org.junit.Test;

public class NetworkPropagationTests {

	private final static byte[] data = "hello world".getBytes();

	@Test
	public void ensureDataPropagation() throws Exception{

		PhroxPublisher pub = mock(PhroxPublisher.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);

		PhroxNetworkedSender sender = new PhroxNetworkedSender(pub, handler);

		sender.handleData(data);

		verify(pub, times(1)).sendMessage(data);
		verify(handler, times(1)).handleData(data);

		sender.close();

		verify(handler, times(1)).close();

		verify(pub, times(0)).close();
	}

	@Test
	public void ensureDuplicatePropagation() throws Exception{

		PhroxPublisher pub = mock(PhroxPublisher.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);

		PhroxNetworkedSender sender = new PhroxNetworkedSender(pub, handler);

		sender.handleData(data);
		sender.handleData(data);

		verify(pub, times(2)).sendMessage(data);
		verify(handler, times(2)).handleData(data);

		sender.close();

		verify(handler, times(1)).close();

		verify(pub, times(0)).close();
	}

}
