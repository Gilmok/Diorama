/*import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
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
			System.out.print("C");
			//try{Thread.sleep(10);}catch(Exception ex){}
			try
			{
				out.println(String.valueOf(msgs));
				System.out.println(in.readLine());
			}
			catch(IOException ex){}
		}
		out.println("END");	
	}
}

public class DioramaClientTest {*/

	/**
	 * @param args
	 */
/*	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

}*/
