
public class EuclidDistTest 
{
	public static double edSqrt(int x1, int x2, int y1, int y2)
	{
		if(x1 == x2)
			return Math.abs(y2-y1);
		else if(y1 == y2)
			return Math.abs(x2-x1);
		return Math.sqrt( (x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
	}
	
	public static double edHyp(int x1, int x2, int y1, int y2)
	{
		if(x1 == x2)
			return Math.abs(y2-y1);
		else if(y1 == y2)
			return Math.abs(x2-x1);
		//else
			//return 0;
		return Math.hypot( (x2-x1), (y2-y1));
	}
	
	public static double edTan(int x1, int x2, int y1, int y2)
	{
		if(x1 == x2)
			return Math.abs(y2-y1);
		else if(y1 == y2)
			return Math.abs(x2-x1);
		//double dist = ;
		//System.out.println(dist);
		double angle = Math.atan((double) (y2-y1) / (double) (x2-x1));
		//System.out.println(Math.toDegrees(angle));
		return Math.abs((y2-y1) / Math.sin(angle));
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		long time1 = System.currentTimeMillis();
		for(int i = 0; i < 10000000; i++)
		{
			int x = (int) (Math.random() * 1000);
			int y = (int) (Math.random() * 1000);
			double z = edSqrt(0, x, 0, y);
			//System.out.println( x + "," + y + "," + z);
		}
		time1 = System.currentTimeMillis() - time1;
		
		System.out.println("=============================");
		long time3 = System.currentTimeMillis();
		for(int i = 0; i < 10000000; i++)
		{
			int x = (int) (Math.random() * 1000);
			int y = (int) (Math.random() * 1000);
			double z = edHyp(0, x, 0, y);
			//System.out.println( x + "," + y + "," + z);
		}
		time3 = System.currentTimeMillis() - time3;
		long time2 = System.currentTimeMillis();
		for(int i = 0; i < 10000000; i++)
		{
			int x = (int) (Math.random() * 1000);
			int y = (int) (Math.random() * 1000);
			double z = edTan(0, x, 0, y);
			//System.out.println( x + "," + y + "," + z);
		}
		time2 = System.currentTimeMillis() - time2;
		System.out.println("Square root ED * 1M:" + time1);
		System.out.println("Tan ED * 1M:" + time2);
		System.out.println("Hyp ED * 1M:" + time3);
	}

}
