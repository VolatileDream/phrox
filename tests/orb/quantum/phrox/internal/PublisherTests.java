package orb.quantum.phrox.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class PublisherTests {

	private final static byte[] data = "hello world".getBytes();
	private final static int defaultPort = 8080;
	
	@Test
	public void createAndClosePublisher() throws Exception{
		
		Context context = mock(Context.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.PUB)).thenReturn(soc);
		
		PhroxPublisher publisher = new PhroxPublisher(context, defaultPort);
		
		publisher.close();
		
		verify(context, times(1)).socket(ZMQ.PUB);
		verify(soc, times(1)).close();
	}
	
	@Test
	public void bind() throws Exception{
		
		int port = 8090;
		
		Context context = mock(Context.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.PUB)).thenReturn(soc);
		
		PhroxPublisher publisher = new PhroxPublisher(context, port);
		
		publisher.start();
		
		verify(context, times(1)).socket(ZMQ.PUB);
		verify(soc, times(1)).bind("tcp://*:" + port);
	}
	
	@Test
	public void publishTest() throws Exception{
		
		Context context = mock(Context.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.PUB)).thenReturn(soc);
		
		PhroxPublisher publisher = new PhroxPublisher(context, defaultPort);
		
		publisher.sendMessage(data);
		
		verify(context, times(1)).socket(ZMQ.PUB);
		verify(soc, times(1)).send(data, 0);
	}
	
	@Test
	public void createUseAndClose() throws Exception{
		
		Context context = mock(Context.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.PUB)).thenReturn(soc);
		
		PhroxPublisher publisher = new PhroxPublisher(context, defaultPort);
		
		publisher.start();
		
		publisher.sendMessage(data);
		
		publisher.close();
		
		verify(context, times(1)).socket(ZMQ.PUB);
		verify(soc, times(1)).bind("tcp://*:" + defaultPort);
		verify(soc, times(1)).send(data, 0);
		verify(soc, times(1)).close();
	}
	
}
