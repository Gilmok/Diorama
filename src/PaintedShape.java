import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

class PaintedRectangle extends UserObject implements Drawable, Selectable, PropertyGenerator, Placeable
{
	int x1, y1, x2, y2;
	int r, g, b, a;
	double rotOriginX, rotOriginY;
	double rTheta;
	int type;
	private Color color;
	
	public PaintedRectangle()
	{
		rTheta = rotOriginX = rotOriginY = 0;
		type = 0;
		r = 255; g = 0; b = 0; a = 255;
	}
	
	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		rotOriginX = x1 + ((x2 - x1) / 2);
		rotOriginY = y1 + ((y2 - y1) / 2);
		return true;
		//type = 0;
	}
	
	public void setColor(int red, int green, int blue, int alpha)
	{
		r = red; g = green; b = blue; a = alpha;
		color = new Color(r, g, b, a);
	}
	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String all = "x1,y1,x2,y2,r,g,b,a,rotOriginX,rotOriginY,rTheta,type"; 
		String[] names = all.split(",");
		return names;
	}
	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		/*Field[] allFields = this.getClass().getFields();
		allFields[i].*/
		String[] rtnVal = new String[12];
		int[] all1 = {x1,y1,x2,y2,r,g,b,a};
		for(int i = 0; i < all1.length; i++)
			rtnVal[i] = String.valueOf(all1[i]);
		double[] all2 = {rotOriginX, rotOriginY, rTheta};
		for(int i = 0; i < all2.length; i++)
			rtnVal[i + all1.length] = String.valueOf(all2[i]);
		rtnVal[11] = String.valueOf(type);
		return rtnVal;
	}
	@Override
	public ButtonAction[] getActionNames() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		int[] a = new int[8];
		for(int i = 0; i < a.length; i++)
			a[i] = Integer.parseInt(in[i]);
		x1 = a[0];
		y1 = a[1];
		x2 = a[2];
		y2 = a[3];
		for(int i = 4; i < 8; i++)
		{
			if(a[i] > 255)
				a[i] = 255;
			if(a[i] < 0)
				a[i] = 0;
		}
		r = a[4];
		g = a[5];
		b = a[6];
		this.a = a[7];
		double[] b = new double[3];
		for(int i = 0; i < b.length; i++)
			b[i] = Double.parseDouble(in[a.length + i]);
		setRotation(b);
		type = Integer.parseInt(in[in.length - 1]);
		//return rtnVal;
	}
	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "PaintedRectangle";
	}
	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.getXform(rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.getPointRotation(x,  y, rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public double[] invPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.invPointRotation(x, y, rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		x1 = in.x;
		y1 = in.y;
		x2 = in.x + in.width;
		y2 = in.y + in.height;
	}
	@Override
	public void setRotation(double[] xForm) 
	{
		// TODO Auto-generated method stub
		rTheta = xForm[2];
		rotOriginX = xForm[0];
		rotOriginY = xForm[1];
	}
	@Override
	public void drawInEditor(Graphics g, Diorama dia) 
	{
		// TODO Auto-generated method stub
		//Graphics2D g2d = (Graphics2D) g;
		//AffineTransform orig = g2d.getTransform();
		//double tempRTheta = rTheta;
		//rTheta = 0;
		draw(g, dia);
		//rTheta = tempRTheta;
		
	}
	@Override
	public void draw(Graphics g, Diorama d) 
	{
		// TODO Auto-generated method stub
		g.setColor(color);
		if(rTheta == 0)
		{
			switch(type)
			{
			case 0: //rectangle
				g.fillRect(x1, y1, x2 - x1, y2 - y1);
				break;
			case 1:  //rounded rectangle
				g.fillRoundRect(x1, y1, x2 - x1, y2 - y1, (x2 - x1) / 10, (y2 - y1) / 10); 
				break;
			case 2:
				g.fillOval(x1, y1, x2 - x1, y2 - y1);
				break;
			}
		}
			
		else
		{
			Graphics2D g2d = (Graphics2D) g;
			AffineTransform orig = g2d.getTransform();
			AffineTransform tx = AffineTransform.getRotateInstance(rTheta, rotOriginX, rotOriginY);
			g2d.transform(tx);
			System.out.println("Shape xform:" + rTheta + "," + rotOriginX + "," + rotOriginY);
			switch(type)
			{
			case 0: //rectangle
				g2d.fillRect(x1, y1, x2 - x1, y2 - y1);
				break;
			case 1:  //rounded rectangle
				g2d.fillRoundRect(x1, y1, x2 - x1, y2 - y1, (x2 - x1) / 10, (y2 - y1) / 10); 
				break;
			case 2:
				g2d.fillOval(x1, y1, x2 - x1, y2 - y1);
				break;
			}
			if(orig != null)
			{
				g2d.setTransform(orig);
			}
		}
	}
	@Override
	public double getLayer() 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public BufferedImage getImg() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Rectangle getDrawArea() 
	{
		// TODO Auto-generated method stub
		return getRect();
	}
}

class BeveledRectangle extends PaintedRectangle
{
	int bevelWidth;
	int lr, lg, lb, la;
	int dr, dg, db, da;
	boolean raised;
	Color cDark;
	Color cLight;
	
	public BeveledRectangle()
	{
		bevelWidth = 5;
		lr = 255; lg = 128; lb = 128; la = 255;
		dr = 128; dg = 0; db = 0; da = 255;
		cDark = new Color(dr, dg, db, da);
		cLight = new Color(lr, lg, lb, la);
		raised = true;
	}
	
	public void depress()
	{
		raised = false;
	}
	
	public void raise()
	{
		raised = true;
	}
	
	public void setBevelWidth(int n)
	{
		bevelWidth = n;
	}
	
	public void setLightColor(Color lc)
	{
		cLight = lc;
	}
	
	public void setDarkColor(Color dc)
	{
		cDark = dc;
	}
	
	private void drawLines(Color c1, Color c2, Graphics2D g2d)
	{
		for(int i = 0; i < bevelWidth; i++)
		{
			g2d.setColor(c1);  //bottom half
			g2d.drawLine(x1 + i, y2 - i, x2, y2 - i);
			g2d.drawLine(x2 - i, y1 + i, x2 - i, y2);
			g2d.setColor(c2);  //top half
			g2d.drawLine(x1, y1 + i, x2 - i, y1 + i);
			g2d.drawLine(x1 + i, y1, x1 + i, y2 - i);
		}
	}
	
	public void draw(Graphics g, Diorama dia)
	{
		super.draw(g, dia);
		
		/*if(raised)
		{
			col1 = new Color(dr, dg, db, da);
			col2 = new Color(lr, lg, lb, la);
		}
		else
		{
			col2 = new Color(dr, dg, db, da);
			col1 = new Color(lr, lg, lb, la);
		}*/
		
		if(rTheta != 0)
		{
			Graphics2D g2d = (Graphics2D) g;
			AffineTransform orig = g2d.getTransform();
			g2d.transform(AffineTransform.getRotateInstance(rTheta, rotOriginX, rotOriginY));
			if(raised)
				drawLines(cDark, cLight, g2d);
			else
				drawLines(cLight, cDark, g2d);
			if(orig != null)
				g2d.setTransform(orig);
		}
		else
		{
			if(raised)
				drawLines(cDark, cLight, (Graphics2D) g);
			else
				drawLines(cLight, cDark, (Graphics2D) g);
		}
		
	}
}

class PaintedLabel extends UserObject implements Drawable, Selectable, PropertyGenerator, Placeable
{
	String text;
	Font font;
	int x1, y1, x2, y2;
	int r, g, b, a;
	private Color c;
	double rTheta, rotOriginX, rotOriginY;
	
	public PaintedLabel()
	{
		text = "HappyLabel";
		r = g = b = a = 255;
		c = new Color(r,g,b,a);
		rTheta = rotOriginX = rotOriginY = 0;
		font = new Font("Serif", Font.BOLD, 12);
	}

	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		rotOriginX = x1 + ((x2 - x1) / 2);
		rotOriginY = y1 + ((y2 - y1) / 2);
		return true;
	}

	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getPropertyNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyValues()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ButtonAction[] getActionNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPropertyValues(String[] in)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getItemName()
	{
		// TODO Auto-generated method stub
		return "PaintedLabel";
	}

	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.getXform(rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.getPointRotation(x,  y, rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public double[] invPointRotation(double x, double y) 
	{
		return DefaultSelectable.invPointRotation(x, y, rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public void setRect(Rectangle in) 
	{
		x1 = in.x;
		y1 = in.y;
		x2 = in.x + in.width;
		y2 = in.y + in.height;
	}
	@Override
	public void setRotation(double[] xForm) 
	{
		
		rotOriginX = xForm[0];
		rotOriginY = xForm[1];
		rTheta = xForm[2];
	}
	@Override
	public void drawInEditor(Graphics g, Diorama dia) 
	{
		draw(g, dia);
	}

	@Override
	public void draw(Graphics g, Diorama d)
	{
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform orig = null;
		if(rTheta != 0)
		{
			orig = g2d.getTransform();
			g2d.transform(AffineTransform.getRotateInstance(rTheta, rotOriginX, rotOriginY));
		}
		g2d.setFont(font);
		g2d.setColor(c);
		g2d.drawString(text, (float) x1, (float) y2);
		//g2d.drawString
		if(orig != null)
			g2d.setTransform(orig);
	}

	@Override
	public double getLayer()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BufferedImage getImg()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getDrawArea() 
	{
		return getRect();
	}
	
	
}

class PaintedRegularShape extends UserObject implements Drawable, Selectable, PropertyGenerator, Placeable
{
	int nVerteces; // 0 for circle
	int sideLen;  //radius for circle
	int centerX;
	int centerY;
	
	
	
	int rotOriginX;
	int rotOriginY;
	double rTheta;
	
	private int[] xPoints;
	private int[] yPoints;
	
	int r, g, b, a;
	private Color color;
	
	private Rectangle drawArea;
	
	
	public PaintedRegularShape()
	{
		color = new Color(0,0,0,0);
		setColor(0,255,0,255);
		nVerteces = 3;
		rTheta = 0;
	}
	
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		centerX = (x2 + x1) / 2;
		centerY = (y2 + y1) / 2;
		sideLen = x2 - x1;
		if(y2 - y1 > sideLen)
			sideLen = y2 - y1;
		sideLen /= 2;
		rotOriginX = centerX;
		rotOriginY = centerY;
		
		drawArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);
		setPoints();
		return true;
	}
	
	private void setPoints()
	{
		
		xPoints = new int[nVerteces];
		yPoints = new int[nVerteces];
		double centralAngle = (Math.PI * 2.0) / nVerteces;
		double angle = (Math.PI / 2);
		int diff = sideLen;
		if(nVerteces == 4) //to rotate it to the square position
		{
			angle += Math.PI / 4;
			//diff = 50;
		}
		for(int i = 0; i < nVerteces; i++)
		{
			
			xPoints[i] = (int) (Math.cos(angle) * diff) + centerX;
			yPoints[i] = (int) (Math.sin(angle) * -1 * diff) + centerY;
			System.out.println(Math.toDegrees(angle) + ":" + xPoints[i] + "," + yPoints[i]);
			angle += centralAngle;
			/*int x2 = (int) (Math.cos(angle) * 45) + diff;
			int y2 = (int) (Math.sin(angle) * -45) + diff;
			System.out.println(Math.toDegrees(angle) + ":" + x2 + "," + y2);*/
		}
	}
	
	public void setNPoints(int n)
	{
		nVerteces = n;
		setPoints();
	}
	
	public void setColor(int red, int green, int blue, int alpha)
	{
		r = red;
		b = blue;
		g = green;
		a = alpha;
		color = new Color(r, g, b, a);
	}
	
	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		//nVerteces = 4;
	}
	
	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String[] props = {"nVerteces", "centerX", "centerY", "sideLen", "rotOriginX", "rotOriginY", "r", "g", "b", "a", "rTheta"};
		return props;
	}
	
	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		
		int[] intVals = {nVerteces, centerX, centerY, sideLen, rotOriginX, rotOriginY, r, g, b, a};
		String[] vals = new String[intVals.length + 1];
		for(int i = 0; i < intVals.length; i++)
			vals[i] = String.valueOf(intVals[i]);
		vals[intVals.length] = String.valueOf(rTheta);
		return vals;
	}
	@Override
	public ButtonAction[] getActionNames() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		int[] n = new int[10];
		for(int i = 0; i < n.length; i++)
			n[i] = Integer.parseInt(in[i]);
		for(int i = 6; i < 10; i++)
		{
			if(n[i] > 255)
				n[i] = 255;
			if(n[i] < 0)
				n[i] = 0;
		}
		if(n[0] < 0)
			n[0] = 0;
		
		centerX = n[1];
		centerY = n[2];
		setNPoints(n[0]);
		if(n[3] < 1)
			n[3] = 1;
		sideLen = n[3];
		rotOriginX = n[4];
		rotOriginY = n[5];
		r = n[6];
		g = n[7];
		b = n[8];
		a = n[9];
		
		rTheta = Double.parseDouble(in[n.length]);
	}
	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "RegularShape";
	}
	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		return drawArea;
	}
	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.getXform(rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.getPointRotation(x, y, rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public double[] invPointRotation(double x, double y)
	{
		// TODO Auto-generated method stub
		return DefaultSelectable.invPointRotation(x, y, rTheta, rotOriginX, rotOriginY);
	}
	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		setLoc(in.x, in.y, in.x + in.width, in.y + in.height, null);
	}
	@Override
	public void setRotation(double[] xForm) 
	{
		// TODO Auto-generated method stub
		rotOriginX = (int) xForm[0];
		rotOriginY = (int) xForm[1];
		rTheta = xForm[2];
	}
	@Override
	public void drawInEditor(Graphics g, Diorama dia) 
	{
		// TODO Auto-generated method stub
		draw(g, dia);
	}
	@Override
	public void draw(Graphics g, Diorama d) 
	{
		// TODO Auto-generated method stub
		g.setColor(color);
		
		switch(nVerteces)
		{
		case 0:
			g.drawOval(centerX - sideLen, centerY - sideLen, sideLen * 2, sideLen * 2);
			break;
		case 1: case 2:
			break;
		default:
			Graphics2D g2d = (Graphics2D) g;
			if(rTheta == 0)
				g2d.fillPolygon(xPoints, yPoints, nVerteces);
			else
			{
				AffineTransform orig = g2d.getTransform();
				g2d.transform(AffineTransform.getRotateInstance(rTheta, rotOriginX, rotOriginY));
				g2d.fillPolygon(xPoints, yPoints, nVerteces);
				if(orig != null)
					g2d.setTransform(orig);
			}
		}
	}
	@Override
	public double getLayer() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public BufferedImage getImg() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Rectangle getDrawArea() {
		// TODO Auto-generated method stub
		return drawArea;
	}
	
	
	
}
