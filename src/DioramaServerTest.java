import java.net.*;
import java.io.*;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

class DioramaClient extends Thread
{
	Socket client;
	String sentStuff;
	
	BufferedReader in;
	PrintWriter out;
	
	DioramaClient(Socket in)
	{
		client = in;
		
		try
		{
			//System.out.println("Client:");
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			//System.out.println(client.getInputStream().toString());
			out = new PrintWriter(client.getOutputStream(), true);
			//System.out.println(client.getOutputStream().toString());
			//run();
		}
		catch(IOException ex)
		{
			System.err.println("Failed to start client");
		}
	}
	
	public void run()
	{
		int msgs = 50;
		
		for(int i = 0; i < msgs; i++)
		{
			//System.out.print("C");
			//try{Thread.sleep(10);}catch(Exception ex){}
			try
			{
				out.println(String.valueOf(i));
				//System.out.println(in.readLine());
			}
			catch(Exception ex){}
		}
		out.println("END");	
	}
}

class DioramaServer extends Thread
{
	Socket cl;
	MessageManager ms;
	BufferedReader in;
	PrintWriter out;
	String msg;
	boolean clearMsg;
	int index;
	int messagesSent;
	int messagesProcessed;
	
	DioramaServer(Socket client, MessageManager msmg, int idx)
	{
		cl = client;
		ms = msmg;
		msg = "";
		index = idx;
		messagesSent = 0;
		messagesProcessed = 0;
		try
		{
			System.out.println("Server #" + idx);
			in = new BufferedReader(new InputStreamReader(cl.getInputStream()));
			//System.out.println(cl.getInputStream().toString());
			out = new PrintWriter(cl.getOutputStream(), true);
			//System.out.println(cl.getOutputStream().toString());
			//run();
		}
		catch(IOException ex)
		{
			System.err.println("Failed to start client");
		}
	}
	
	public void run()
	{
		System.out.println("Starting server");
		String temp;
		try
		{
			while((temp = in.readLine()) != null)
			{
				//System.out.print("S");
				/*if(clearMsg)
				{
					msg = "";
					clearMsg = false;
				}*/
				msg += temp + ";";
				messagesSent++;
				ms.messagesFrom.add(this);
				ms.processMessage();  //this call might fail
				/*int x = messagesProcessed;
				messagesSent -= messagesProcessed;
				if(x == messagesProcessed)
				{
					messagesProcessed = 0;
					for(int i = 0; i < x; i++)
					{
						msg = msg.substring(msg.indexOf(";") + 1);
					}
				}*/
			}
		}
		catch(IOException ex)
		{
			System.err.println("Server disconnected from client");
		}
	}
}

class DServerHub extends Thread
{
	int maxClients;
	int numClients;
	MessageManager msmg;
	ServerSocket ds;
	
	DServerHub(int max)
	{
		maxClients = max;
		
		numClients = 0;
		msmg = new MessageManager(this);
		try 
		{             
			ds = new ServerSocket(4444);
			start();
			//msmg.start();
		} catch (IOException e) 
		{             
			System.err.println("Could not listen on port: 4444.");             
			System.exit(-1);         
		}
		
		
	}
	
	public void run()
	{
		Socket dcNext = null;
		while(true)
		{
			try
			{
				dcNext = ds.accept();  //blocks here
				System.out.println("Added client " + dcNext.toString());
				addServer(dcNext);
			}
			catch(Exception ex)
			{
				System.err.println("Failed to add client #" + msmg.clientList.size());
			}
		}
	}
	
	public synchronized void addServer(Socket dc)
	{
		if(msmg.serverList.size() < maxClients)
		{
			//DioramaClient client = new DioramaClient(dc);
			DioramaServer server = new DioramaServer(dc, msmg, msmg.serverList.size());
			//msmg.clientList.add(client);
			msmg.serverList.add(server);
			server.start();
			//client.start();
		}
	}
	
	public void addClient(Socket dc) //only for local testing
	{
		if(msmg.clientList.size() < maxClients)
		{
			DioramaClient client = new DioramaClient(dc);
			//DioramaServer server = new DioramaServer(dc, msmg, msmg.clientList.size());
			msmg.clientList.add(client);
			//msmg.serverList.add(server);
			//server.start();
			client.start();
		}
	}
}

class MessageManager extends Thread
{
	Vector<DioramaClient> clientList;
	Vector<DioramaServer> serverList;
	DServerHub dsHub;
	BufferedReader pm;
	String msgLeft;
	String[] testMsg;
	Vector<DioramaServer> messagesFrom;
	
	MessageManager(DServerHub dsHub)
	{
		clientList = new Vector<DioramaClient>();
		serverList = new Vector<DioramaServer>();
		msgLeft = new String();
		messagesFrom = new Vector<DioramaServer>();
		
		testMsg = new String[12];
		for(int i = 0; i < testMsg.length; i++)
			testMsg[i] = "\n";
		try
		{
			InputStream is = new ByteArrayInputStream(msgLeft.getBytes("UTF-8"));
			pm = new BufferedReader(new InputStreamReader(is));
		}catch(Exception ex){}
		
	}
	
	public synchronized void processMessage()
	{
		//System.out.print("M");
		if(messagesFrom.size() == 0)
			return;
		//int serverNum = messagesFrom.remove(0).intValue();
		DioramaServer ds = messagesFrom.remove(0);
		String mm = ds.msg.substring(0, ds.msg.indexOf(";"));
		ds.msg = ds.msg.substring(ds.msg.indexOf(";") + 1);
		testMsg[ds.index] += "#" + ds.index + ":" + mm;
			
			if(testMsg[ds.index].endsWith("END"))
			{
				System.out.println(testMsg[ds.index]);
				testMsg[ds.index] = "";
			}
		
		if(messagesFrom.size() > 0)
			processMessage();
	}
	
}


public class DioramaServerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		DServerHub dsh = new DServerHub(100000);
		try
		{
			for(int i = 0; i < 12; i++)
			{
				Socket local = new Socket("localhost", 4444);
				DioramaClient client = new DioramaClient(local);
				client.start();
				//dsh.addClient(local);
			}
		}
		catch(Exception ex)
		{
			System.out.println("Failed to make socket");
		}

	}

}
