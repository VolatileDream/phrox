import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;


public class ZmqTest {

	public static void main( String[] args ) throws InterruptedException{
		Context c = ZMQ.context(1);

		Socket pub = c.socket(ZMQ.PAIR);
		pub.bind("tcp://*:8080");
		System.out.println("bound");
		
		Socket sub = c.socket(ZMQ.PAIR);
		sub.connect("tcp://localhost:8080");
		System.out.println("connect");
		
		Thread.sleep(1000);

		pub.send("herp derp");
		sub.send("herpy derpy");
		System.out.println(sub.recvStr());
		System.out.println(pub.recvStr());
		System.out.println(sub.recv
				(ZMQ.DONTWAIT));
	}

}
