package ie.gmit.sw;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

//BlockingQueue (int)


public class VigenerHandler implements Runnable
{
	private BlockingQueue<Request> queue;
	private Map<Long, String> out = new ConcurrentHashMap<Long, String>();



	public VigenerHandler(BlockingQueue<Request> q, Map<Long, String> out) 
	{
		this.queue = q;
		this.out = out;
	}
	
	
	public void run() 
	{
		try
		{
			Request req = queue.take();
			VigenereBreaker vb = (VigenereBreaker) Naming.lookup("rmi://127.0.0.1:1099/cypher-service");
			String result = vb.decrypt(req.getCypherText(), req.getMaxKeySize());
			out.put(req.getJobNumber(), result);
		}
		catch (Exception e) 
		{
			
			e.printStackTrace();
		}
	}
	
	
}

	
