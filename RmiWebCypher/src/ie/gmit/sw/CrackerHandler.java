package ie.gmit.sw;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
//import VigenereBreaker.*;
//import java.rmi.Naming;

public class CrackerHandler extends HttpServlet 
{
	private String remoteHost = null;
	private static long jobNumber = 0;
	private String decryptedCypherText;
	int number = 0;
	
	private VigenereRequestManager vRManager = new VigenereRequestManager();

	public void init() throws ServletException 
	{
		ServletContext ctx = getServletContext();
		remoteHost = ctx.getInitParameter("RMI_SERVER"); //Reads the value from the <context-param> in web.xml
		Consumer consumer = new Consumer();
		Thread thread = new Thread(consumer);
		thread.start();
	}
	
	
	public class Consumer implements Runnable
	{
		public void run() 
		{
			while(true)
			{
				try
				{
					Request req = vRManager.take();
			
					VigenereBreaker vb = (VigenereBreaker) Naming.lookup("rmi://127.0.0.1:1099/cypher-service");
					String result = vb.decrypt(req.getCypherText(), req.getMaxKeySize());
					vRManager.putToOutMap(req.getJobNumber(), result);
					
				}
				catch (Exception e) 
				{
			
					e.printStackTrace();
				}
			}
		}
	}
	
	
	

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, MalformedURLException, RemoteException  
	{
		ready(req,resp);
		
	}

	private void ready(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		int maxKeyLength = Integer.parseInt(req.getParameter("frmMaxKeyLength"));
		String cypherText = req.getParameter("frmCypherText");
		//String testword = "HHESKCDOZNLLSCDRTYYDHLTKUCDW";
		
//		VigenereBreaker vb;
//		try 
//		{
//			vb = (VigenereBreaker) Naming.lookup("rmi://127.0.0.1:1099/cypher-service");
//			decryptedCypherText = vb.decrypt(cypherText, maxKeyLength);
//			//out.print(result);
//			
//			
//		} catch (NotBoundException e) 
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String taskNumber = req.getParameter("frmStatus");


		out.print("<html><head><title>Distributed Systems Assignment</title>");		
		out.print("</head>");		
		out.print("<body>");
		
		if (taskNumber == null)
		{
			jobNumber++;
			taskNumber = new String("T" + jobNumber);
			
			Request request = new Request(cypherText,maxKeyLength,jobNumber);
			
			vRManager.add(request);
			out.print("<H1>Processing request for Job#: " + taskNumber + "</H1>");
			out.print("<div id=\"r\"></div>");
			//number++;
			//out.print(number);
			
			out.print("RMI Server is located at " + remoteHost);
			out.print("<P>Maximum Key Length: " + maxKeyLength);		
			out.print("<P>CypherText: " + cypherText);
		}
		else
		{
			
			String result;
			result = vRManager.getResult(jobNumber);
			System.out.println(jobNumber);
			if(result!=null)
			{
				decryptedCypherText = result;
				
				//out.print(decryptedCypherText);
				
				out.print("<H1>Processing Finished for Job#: " + taskNumber + "</H1>");
				out.print("<div id=\"r\"></div>");		
				out.print("<P>DecryptedCypherText: " + decryptedCypherText);
			}
			
			//Check out-queue for finished job
		}
		
		
		
		
		out.print("<form name=\"frmCracker\">");
		out.print("<input name=\"frmMaxKeyLength\" type=\"hidden\" value=\"" + maxKeyLength + "\">");
		out.print("<input name=\"frmCypherText\" type=\"hidden\" value=\"" + cypherText + "\">");
		out.print("<input name=\"frmStatus\" type=\"hidden\" value=\"" + taskNumber + "\">");
		out.print("</form>");								
		out.print("</body>");	
		out.print("</html>");	
		
		out.print("<script>");
		out.print("var wait=setTimeout(\"document.frmCracker.submit();\", 2000);");
		out.print("</script>");

		
		
		
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		doGet(req, resp);
		
 	}
	
	private void waiting(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		int maxKeyLength = Integer.parseInt(req.getParameter("frmMaxKeyLength"));
		String cypherText = req.getParameter("frmCypherText");
		
		
		
		String taskNumber = req.getParameter("frmStatus");


		out.print("<html><head><title>Distributed Systems Assignment</title>");		
		out.print("</head>");		
		out.print("<body>");
		
		if (taskNumber == null)
		{
			taskNumber = new String("T" + jobNumber);
			jobNumber++;
			//Add job to in-queue
			
		}
		else
		{
			//Check out-queue for finished job
		}
		
		
		
		out.print("<H1>Processing request for Job#: " + taskNumber + "</H1>");
		out.print("<div id=\"r\"></div>");
		
		
		out.print("RMI Server is located at " + remoteHost);
		out.print("<P>Maximum Key Length: " + maxKeyLength);		
		out.print("<P>CypherText: " + cypherText);
		out.print("<P>This servlet should only be responsible for handling client request and returning responses. Everything else should be handled by different objects.");
		out.print("<P>Note that any variables declared inside this doGet() method are thread safe. Anything defined at a class level is shared between HTTP requests.");				


		out.print("<P> Next Steps:");	
		out.print("<OL>");
		out.print("<LI>Generate a big random number to use a a job number, or just increment a static long variable declared at a class level, e.g. jobNumber");	
		out.print("<LI>Create some type of a message request object from the maxKeyLength, cypherText and jobNumber.");	
		out.print("<LI>Add the message request object to a LinkedList or BlockingQueue (the IN-queue)");			
		out.print("<LI>Return the jobNumber to the client web browser with a wait interval using <meta http-equiv=\"refresh\" content=\"10\">. The content=\"10\" will wait for 10s.");	
		out.print("<LI>Have some process check the LinkedList or BlockingQueue for message requests ");	
		out.print("<LI>Poll a message request from the front of the queue and make an RMI call to the Vigenere Cypher Service");			
		out.print("<LI>Get the result and add to a Map (the OUT-queue) using the jobNumber and the key and the result as a value");	
		out.print("<LI>Return the cyphertext to the client next time a request for the jobNumber is received and delete the key / value pair from the Map.");	
		out.print("</OL>");	
		
		out.print("<form name=\"frmCracker\">");
		out.print("<input name=\"frmMaxKeyLength\" type=\"hidden\" value=\"" + maxKeyLength + "\">");
		out.print("<input name=\"frmCypherText\" type=\"hidden\" value=\"" + cypherText + "\">");
		out.print("<input name=\"frmStatus\" type=\"hidden\" value=\"" + taskNumber + "\">");
		out.print("</form>");								
		out.print("</body>");	
		out.print("</html>");	
		
		out.print("<script>");
		out.print("var wait=setTimeout(\"document.frmCracker.submit();\", 3000);");
		out.print("</script>");

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
