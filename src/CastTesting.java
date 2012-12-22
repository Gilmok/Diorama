import java.util.Vector;
import java.lang.reflect.*;
import java.awt.event.*;


class classA implements java.io.Serializable
{
	int x;
	public String toString()
	{
		return "A" + x;
	}
	
	public classA(){x = 1;}
}

class classB extends classA
{
	public String toString()
	{
		return "B" + x;
	}
	
	public classB(){x = 5;}
}

class classC extends classA
{
	public String toString()
	{
		return "C" + x;
	}
	
	public classC(){x = 10;}
}

class classD implements ActionListener, java.io.Serializable
{
	
	classD()
	{
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		
	}
	
}

class classE extends classD
{
	
	
	classE()
	{
		
	}
	
	
}



public class CastTesting 
{

	public static <U> U[] fillUp(String in, Vector<java.io.Serializable> all)
	{
		U obj = getType(in);
		if(obj == null)
		{
			return null;
		}
		Vector<U> rtn1 = loadUp(obj, all);
		U[] rtn2 = (U[]) Array.newInstance(obj.getClass(), rtn1.size());
		rtn1.toArray(rtn2);
		return rtn2;
	}
	
	public static <T> T getType(String in)
	{
		try
		{
			Class cls = Class.forName(in);
			Object obj = cls.newInstance();
			return (T) obj;
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	public static <U> Vector<U> loadUp(U in, Vector<java.io.Serializable> all)
	{
		Vector<U> x = new Vector<U>();
		
		int n = all.size();
		for(int i = 0; i < n; i++)
		{
			java.io.Serializable ca = all.get(i);
			
			if(in.getClass().getName().equals(ca.getClass().getName()))
			{
				U ub = (U) ca;
				x.add(ub);
			}
		}
		return x;
	}
	
	/**
	 * @param args
	 * 
	 * 
	 */
	public static void main(String[] args) 
	{
		String x = "classB";
		Vector<java.io.Serializable> cavs = new Vector<java.io.Serializable>();
		cavs.add(new classA());
		cavs.add(new classB());
		cavs.add(new classC());
		cavs.add(new classB());
		
		
		//Vector<classB> bx = fillUp("classB", cavs);
		classB[] bx = fillUp("classB", cavs);
		
		for(int i = 0; i< bx.length; i++)
			System.out.println(bx[i]);
		
		classC[] cx = fillUp("classC", cavs);
		
		for(int i = 0; i< cx.length; i++)
			System.out.println(cx[i]);
		
		classE ce = new classE();
		
		ce.actionPerformed(null);
		
	}

}
