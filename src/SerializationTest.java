import java.io.*;
import java.util.*;

class Keeper implements Serializable
{
	int x;
	int y;
	
	public Keeper()
	{
		
	}
	
	public Keeper(int in)
	{
		x = in;
		y = 0;
	}
}

class SaveMe implements Serializable
{
	Vector<Keeper> keeps;
	
	public SaveMe()
	{
		keeps = new Vector<Keeper>();
	}
	
	public SaveMe(int nKeeps)
	{
		this();
		for(int i = 0; i < nKeeps; i++)
		{
			keeps.add(new Keeper(i));
		}
	}
	
	public void print()
	{
		for(int i = 0; i < keeps.size(); i++)
		{
			Keeper k = keeps.get(i);
			System.out.println("Keeper " + i + ":" + k.x + "," + k.y);
		}
		System.out.println("------------");
	}
}


public class SerializationTest {

	/**
	 * @param args
	 */
	
	public static void saveObject(Object obj, String filename)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static Object loadObject(String filename)
	{
		try
		{
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			return in.readObject();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		SaveMe sm = new SaveMe(6);
		sm.print();
		String filename = "Save_Me.ser";
		try
		{
			saveObject(sm, filename);
			SaveMe sm2 = (SaveMe) loadObject(filename);
			sm2.print();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

}
