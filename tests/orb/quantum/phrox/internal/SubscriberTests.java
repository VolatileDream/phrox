package orb.quantum.phrox.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import orb.quantum.phrox.PhroxMessageHandler;
import orb.quantum.phrox.Subscription;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class SubscriberTests {

	private final static byte[] data = "hello world".getBytes();
	private final static int defaultPort = 8080;
	private final static String defaultHost = "yer_host";
	
	@Test
	public void createAndClose() throws Exception{
		
		Context context = mock(Context.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.SUB)).thenReturn(soc);
		
		PhroxSubscriber sub = new PhroxSubscriber(context, handler);
		
		sub.close();
		
		verify(context).socket(ZMQ.SUB);
		verify(handler).close();
	}

	@Test
	public void start() throws Exception{
		
		Context context = mock(Context.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.SUB)).thenReturn(soc);
		when(soc.recv(0)).thenReturn(null);
		
		PhroxSubscriber sub = new PhroxSubscriber(context, handler);
		
		sub.start();
		Thread.sleep(10);
		
		verify(context).socket(ZMQ.SUB);
		
		verify(handler, times(0)).handleData( Mockito.any(byte[].class) );
		
		verify(soc).subscribe(new byte[0]);
		verify(soc, atLeastOnce()).recv(0);
	}
	
	@Test
	public void startAndRun() throws Exception{
		
		Context context = mock(Context.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.SUB)).thenReturn(soc);
		when(soc.recv(0)).then(new Answer<byte[]>(){
			private boolean sent = false;
			@Override
			public byte[] answer(InvocationOnMock invocation) throws Throwable {
				if( ! sent ){
					sent = true;
					return data;
				}
				return null;
			}
		});
		
		PhroxSubscriber sub = new PhroxSubscriber(context, handler);
		
		sub.start();
		
		Thread.sleep(100);
		
		verify(context).socket(ZMQ.SUB);
		
		verify(soc).subscribe(new byte[0]);
		verify(soc, atLeastOnce()).recv(0);
		
		verify(handler).handleData( data );
	}
	
	@Test
	public void swapHandler() throws Exception{
		
		Context context = mock(Context.class);
		PhroxMessageHandler handler1 = mock(PhroxMessageHandler.class);
		PhroxMessageHandler handler2 = mock(PhroxMessageHandler.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.SUB)).thenReturn(soc);
		
		MyAnswer ans = new MyAnswer();
		
		when(soc.recv(0)).then(ans);
		
		PhroxSubscriber sub = new PhroxSubscriber(context, handler1);
		
		sub.start();
		
		Thread.sleep(100);
		sub.setHandler(handler2);
		ans.sent = false;
		Thread.sleep(100);
		
		verify(context).socket(ZMQ.SUB);
		
		verify(soc).subscribe(new byte[0]);
		verify(soc, atLeastOnce()).recv(0);
		
		verify(handler1).handleData( data );
		verify(handler2).handleData( data );
	}
	
	@Test
	public void subscribeAndCloseSubscription() throws Exception{
		
		Context context = mock(Context.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.SUB)).thenReturn(soc);
		when(soc.recv(0)).then(new MyAnswer());
		
		PhroxSubscriber sub = new PhroxSubscriber(context, handler);
		
		Subscription s = sub.connect(defaultHost,defaultPort);
		s.close();
		
		verify(context).socket(ZMQ.SUB);
		
		verify(soc).connect("tcp://" + defaultHost +":" + defaultPort );
		verify(soc).disconnect("tcp://" + defaultHost +":" + defaultPort );
	}
	
	@Test
	public void subscribeAndDisconnect() throws Exception{
		
		Context context = mock(Context.class);
		PhroxMessageHandler handler = mock(PhroxMessageHandler.class);
		Socket soc = mock(Socket.class);
		
		when(context.socket(ZMQ.SUB)).thenReturn(soc);
		when(soc.recv(0)).then(new MyAnswer());
		
		PhroxSubscriber sub = new PhroxSubscriber(context, handler);
		
		sub.connect(defaultHost,defaultPort);
		sub.disconnect(defaultHost, defaultPort);
		
		verify(context).socket(ZMQ.SUB);
		
		verify(soc).connect("tcp://" + defaultHost +":" + defaultPort );
		verify(soc).disconnect("tcp://" + defaultHost +":" + defaultPort );
	}
	
	private static class MyAnswer implements Answer<byte[]> {
		boolean sent = false;
		@Override
		public byte[] answer(InvocationOnMock invocation) throws Throwable {
			if( ! sent ){
				sent = true;
				return data;
			}
			return null;
		}
	}
	
}
