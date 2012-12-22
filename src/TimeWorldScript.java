
public class TimeWorldScript 
{
	public static void main(String args[])
	{
		double war = 0.1;
		int warOut = 0;
		int pointsTotal;
		double attackPerPoint = 0;
		//int command = 16;
		int attackPow = 0;
		
		for(int i = 160; i < 192; i += 8)
		{
			int pointsDiff = 192 - i; 
			war = 1.14;
			for(int j = 0; j < pointsDiff; j += 8)
			{
				war += 0.08;
				warOut = (int) (war * 100);
				attackPow = (int) ((5000 * i) * (1 + war));
				pointsTotal = warOut + i;
				attackPerPoint = attackPow / pointsTotal;
				System.out.println(i + "   " + warOut + "   " + attackPow + ": " + attackPerPoint);
			}
		}
	}
}
