package ie.gmit.sw;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class VigenereRequestManager 
{
	private BlockingQueue<Request> queue = new ArrayBlockingQueue<Request>(10);
	private Map<Long, String> out = new ConcurrentHashMap<Long, String>();
	
	public void add(final Request r)
	{
		try
		{
			//queue.put(r)//blocks if queue full
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						queue.put(r);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Request take() throws InterruptedException
	{
		return queue.take();
	}
	public  void putToOutMap(long jobnumber,String text) throws InterruptedException
	{
		out.put(jobnumber,text);
		System.out.println(out);
	}

	
	public String getResult(long jobnumber) 
	{
		if(out.containsKey(jobnumber))
		{
			return out.get(jobnumber);
		}
		else
		{
			return null; //no result

		}
	}
}



