import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.beans.XMLEncoder;
import java.io.*;

import javax.swing.*;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.lang.reflect.*;


class UserObject implements Serializable, Interactive, Updatable
{
	Vector<IOHandler> handlers;
	String name;
	
	@Override
	public IOHandler[] getIOs() 
	{
		// TODO Auto-generated method stub
		IOHandler[] rtnVal = new IOHandler[handlers.size()];
		return handlers.toArray(rtnVal);
	}
	
	@Override
	public boolean containsPoint(int x, int y) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasRollover() 
	{
		// TODO Auto-generated method stub
		Iterator<IOHandler> itio = handlers.iterator();
		while(itio.hasNext())
			if(itio.next().hasRollover)
				return true;
		return false;
	}

	@Override
	public void addHandler(IOHandler io) 
	{
		// TODO Auto-generated method stub
		handlers.add(io);
	}

	@Override
	public void removeHandler(IOHandler io) 
	{
		// TODO Auto-generated method stub
		handlers.remove(io);
	}
	
	public boolean hasHandler(IOHandler io)
	{
		return handlers.contains(io);
	}
	
	public void update(Diorama d)
	{
		
	}
	
	public UserObject()
	{
		handlers = new Vector<IOHandler>();
		name = "";
	}
	
}

class TrackLocation extends UserObject
{
	Track tr;
	double x, y, z;
	
	TrackLocation()
	{
		tr = null;
		x = y = z = 0;
	}
	
	TrackLocation(Track t, double x, double y, double z)
	{
		tr = t; this.x = x; this.y = y; this.z = z;
	}
}

class Waypoint extends UserObject implements Selectable, PropertyGenerator, Placeable
{
	String tag;
	String info;
	double x, y, z;
	Vector<LinearTrack> onTrack;
	Vector<UserObject> itemsHere;
	
	public Waypoint()
	{
		tag = "";
		info = "";
	}
	
	void setXYZ(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void addTrack(LinearTrack t)
	{
		onTrack.add(t);
	}

	//Selectable
	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "Waypoint " + tag;
	}

	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x ,y};
		return rtnVal;
	}

	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		
		return new Rectangle((int) x - 5, (int) y - 5, 10, 10);
	}

	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x, y, 0};
		return rtnVal;
	}

	@Override
	public double[] invPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x,y};
		return rtnVal;
	}

	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		x = in.x + 5;
		y = in.y + 5;
	}

	@Override
	public void setRotation(double[] form) 
	{
		// TODO Auto-generated method stub
	}
	
	public void drawInEditor(Graphics g, Diorama dia)
	{
		g.setColor(Color.MAGENTA);
		if(tag.length() > 0)
		{
			if(tag.equals("open"))
				g.setColor(Color.CYAN);
			if(tag.equals("closed"))
				g.setColor(Color.RED);
			if(tag.equals("path"))
				g.setColor(Color.GREEN);
		}
		g.drawOval((int) x - 5, (int) y - 5, 10, 10);
	}

	//propertyGenerator
	@Override
	public ButtonAction[] getActionNames() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String[] rtnVal = {"Tag","Info","X","Y","Z"};
		return rtnVal;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		String[] rtnVal = {tag, info, String.valueOf(x), String.valueOf(y), String.valueOf(z)};
		return rtnVal;
	}

	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		tag = in[0];
		info = in[1];
		x = Double.parseDouble(in[2]);
		y = Double.parseDouble(in[3]);
		z = Double.parseDouble(in[4]);
	}

	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		x = x2;
		y = y2;
		return true;
	}
	
	public static double euclidDist(double x1, double y1, double x2, double y2)
	{
		if(x1 == x2)
			return Math.abs(y2-y1);
		else if(y1 == y2)
			return Math.abs(x2-x1);
		return Math.sqrt( (x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
	}
	
	Vector<Waypoint> getPathTo(Waypoint dest)
	{
		HashMap<Waypoint, Waypoint> cameFrom = new HashMap<Waypoint, Waypoint>();
		//Vector<Waypoint> path
		Vector<Waypoint> closed = new Vector<Waypoint>();
		Vector<Waypoint> open = new Vector<Waypoint>();
		Vector<Double> dist = new Vector<Double>();  //g-score
		Vector<Double> distLeft = new Vector<Double>(); //f-score
		//Waypoint start = this;
		
		
		//the heuristic is shortest distance overall, (x2-x1)+(y2-y1)
		open.add(this);
		dist.add(0.0);
		distLeft.add(euclidDist(x, dest.x, y, dest.y));
		
		while(open.size() > 0)
		{
			Waypoint current = null;
			int currentIndex = -1;
			for(int i = 0; i < open.size(); i++)
			{
				double longest = Double.MAX_VALUE;
				if(dist.get(i) + distLeft.get(i) < longest)
				{
					current = open.get(i);
					currentIndex = i;
				}
			}
			closed.add(current);
			if(current == dest)
			{
				//final output
				Vector<Waypoint> path = new Vector<Waypoint>();
				Waypoint curr = dest;
				while(curr != this)
				{
					path.add(0, curr);
					curr = cameFrom.get(curr);
				}
				return path;
			}
			Vector<Waypoint> next = getConnectedPoints();
			for(int i = 0; i < next.size(); i++)
			{
				Waypoint newWP = next.get(i);
				if(closed.contains(newWP))
					continue;
				double relativeDist = dist.get(currentIndex) + euclidDist(newWP.x, current.x, newWP.y, current.y);
				double absDist = euclidDist(newWP.x, this.x, newWP.y, this.y);
				int newInOpen = open.indexOf(newWP);
				if(newInOpen == -1 || relativeDist <= absDist)
				{
					if(newInOpen == -1)
					{
						open.add(newWP);
						newInOpen = open.size() - 1;
						dist.add(Double.MAX_VALUE);
						distLeft.add(Double.MAX_VALUE);
						//cameFrom.add(null);
					}
					dist.set(newInOpen, relativeDist);
					distLeft.set(newInOpen, relativeDist + euclidDist(newWP.x, dest.x, newWP.y, dest.y));
					cameFrom.put(newWP, current);
				}
			}
		}
		
		return null;
		
		//return path;
	}
	
	public Vector<Waypoint> depthPathTo(Waypoint dest)
	{
		double totalEstDist = Math.abs((dest.y - x) + (dest.y - y)) / 2;
		Vector<Waypoint> open = new Vector<Waypoint>();
		Vector<Waypoint> closed = new Vector<Waypoint>();
		
		Waypoint current = this;
		
		while(true)
		{
			
			Vector<Waypoint> points = current.getConnectedPoints();
			double[] dists = new double[points.size()];
			int[] order = new int[points.size()];
			int n = 0;
			for(Waypoint wp: points)
			{
				double estDist = Math.abs((dest.y - x) + (dest.y - y)) / 2;
				n++;
			}
			
		}
	}
	
	public Vector<Waypoint> getConnectedPointsQueue()
	{
		Vector<Waypoint> all = new Vector<Waypoint>();
		Vector<Double> dists = new Vector<Double>();
		Waypoint px;
		for(int i = 0; i < onTrack.size(); i++)
		{
			LinearTrack tk = onTrack.get(i);
			//Waypoint px = null;
			if(tk.p1 == this)
				px = tk.p2;
			else
				px = tk.p1;
			double currDist = euclidDist(x, px.x, y, px.y);
			boolean placed = false;
			for(int j = 0; j < dists.size(); j++)
			{
				if(currDist < dists.get(j).doubleValue())
				{
					all.insertElementAt(px, j);
					dists.insertElementAt(currDist, j);
					placed = true;
					break;
				}
			}
			if(!placed)
			{
				all.add(px);
				dists.add(currDist);
			}
		}
		return all;
	}
	
	public Vector<Waypoint> getConnectedPoints()
	{
		Vector<Waypoint> all = new Vector<Waypoint>();
		for(int i = 0; i < onTrack.size(); i++)
		{
			LinearTrack tk = onTrack.get(i);
			//Waypoint px = null;
			if(tk.p1 == this)
				all.add(tk.p2);
			else
				all.add(tk.p1);
		}
		return all;
	}
}

class StandardWaypoint extends Waypoint
{
	boolean passable;
	
	
	
	Vector<StandardWaypoint> getPathTo(StandardWaypoint dest)
	{
		return null;
	}
}

interface TrackTransfer
{
	public TrackLocation getNewLocation(Track srcTrack, double x, double y, double z); 
	public boolean inXferSpace(double x, double y, Track src);
	public Rectangle getBounds();
	public boolean isAuto();
	public Track[] getTracks();
}

class TrackExit extends UserObject implements PropertyGenerator, TrackTransfer
{
	Track srcTrack;
	Track toTrack;
	int atW;
	int atH;
	
	int state;
	
	double newX;
	double newY;
	double newZ;
	
	//int[][] transSpace;
	
	TrackExit()
	{
		
	}
	
	TrackExit(Track fromT, Track toT, int w, int h, double x, double y, double z, int st)
	{
		toTrack = toT;
		atW = w;
		atH = h;
		newX = x;
		newY = y;
		newZ = z;
		state = st;
		srcTrack = fromT;
	}
	
	public TrackLocation getNewLocation(Track srcTrack, double x, double y, double z)
	{
		return new TrackLocation(toTrack, newX, newY, newZ);
	}

	@Override
	public boolean inXferSpace(double x, double y, Track src) {
		// TODO Auto-generated method stub
		int xx = (int) (x - srcTrack.xInit);
		int yy = (int) (y - srcTrack.yInit);
		if(xx == atW && yy == atH)
			return true;
		else
			return false;
	}
	
	public Rectangle getBounds()
	{
		return new Rectangle((int) srcTrack.xInit + atW-5, (int) srcTrack.yInit + atH-5,10,10);
	}

	@Override
	public ButtonAction[] getActionNames() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames() 
	{
		String[] rv = {"toTrack","atW","atH","newX","newY","newZ"};
		// TODO Auto-generated method stub
		return rv;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		double[] rv = {toTrack.index, atW, atH, newX, newY, newZ};
		String[] rv2 = new String[rv.length];
		for(int i = 0; i < rv.length; i++)
			rv2[i] = String.valueOf(rv[i]);
		return rv2;
	}

	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		//toTrack = Integer.parseInt(in[0]);
		atW = Integer.parseInt(in[1]);
		atH = Integer.parseInt(in[2]);
		newX = Double.parseDouble(in[3]);
		newY = Double.parseDouble(in[4]);
		newZ = Double.parseDouble(in[5]);
	}
	
	public boolean isAuto()
	{
		return false;
	}
	
	public Track[] getTracks()
	{
		Track[] rtnVal = {srcTrack, toTrack};
		return rtnVal;
	}
}

class PointBag extends UserObject implements PropertyGenerator, Selectable, Placeable
{
	Vector<Waypoint> points;
	
	int pointRadius;
	
	int x, y, w, h;

	public PointBag()
	{
		pointRadius = 15;
	}

	//PropertyGenerator
	@Override
	public ButtonAction[] getActionNames() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String[] rtnVal = {"pointRadius", "x", "y", "w", "h"};
		return rtnVal;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		int[] vals = {pointRadius, x, y, w, h};
		String[] rtnVal = new String[vals.length];
		for(int i = 0; i < vals.length; i++)
			rtnVal[i] = String.valueOf(vals[i]);
		return rtnVal;
	}

	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
	   pointRadius = Integer.parseInt(in[0]);
	   x = Integer.parseInt(in[1]);
	   y = Integer.parseInt(in[2]);
	   w = Integer.parseInt(in[3]);
	   h = Integer.parseInt(in[4]);
	}

	//Selectable
	@Override
	public void drawInEditor(Graphics g, Diorama dia) 
	{
		// TODO Auto-generated method stub
		Color c = new Color(255,0,255,64);
		g.setColor(c);
		g.fillRect(x, y, w, h);
	}

	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "PointBag";
	}

	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x ,y};
		return rtnVal;
	}

	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		return new Rectangle(x, y, w, h);
	}

	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x, y, 0};
		return rtnVal;
	}

	@Override
	public double[] invPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x, y, 0};
		return rtnVal;
	}

	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		x = in.x;
		y = in.y;
		w = in.width;
		h = in.height;
	}

	@Override
	public void setRotation(double[] form) {
		// TODO Auto-generated method stub
		
	}

	//Placeable
	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		x = x1;
		y = y1;
		w = x2 - x1;
		h = y2 - y1;
		return true;
	}
	
	public void addPoint()
	{
		double px = Math.random() * w + x;
		double py = Math.random() * h + y;
		
		for(int i = 0; i < points.size(); i++)
		{
			Waypoint pi = points.get(i);
			double dx = px - pi.x;
			double dy = py - pi.y;
			double dist = Math.abs(dx) + Math.abs(dy);
			if(dist > pointRadius)
				continue;
			if(dx > 0)
			{
				px += pointRadius;
				if(px > (x + w))
					px = x;
			}
			else
			{
				px -= pointRadius;
				if(px < x)
					px = x + w;
			}
			if(dy > 0)
			{
				py += pointRadius;
				if(py > (y + h))
					py = y;
			}
			else
			{
				py -= pointRadius;
				if(py < y)
					py = y + h;
			}
		}
		
		Waypoint newPoint = new Waypoint();
		newPoint.setXYZ(px, py, 0.0);
		points.add(newPoint);
	}
	
}

class LinearTrack extends UserObject implements PropertyGenerator, Selectable, Placeable
{
	Waypoint p1;
	Waypoint p2;
	int dist;
	int fxProg;
	
	public LinearTrack()
	{
		dist = 10;
		fxProg = 0;
	}
	
	public LinearTrack(Waypoint a, Waypoint b)
	{
		this();
		p1 = a;
		p2 = b;
	}
	
	//Selectable
	@Override
	public void drawInEditor(Graphics g, Diorama dia) 
	{
		// TODO Auto-generated method stub
		g.setColor(Color.MAGENTA);
		if(p1 != null && p2 != null)
			g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
	}
	
	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "Path";
	}
	
	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x ,y};
		return rtnVal;
	}
	
	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		int minX = (int) p1.x;
		int minY = (int) p1.y;
		if(p1.x > p2.x)
			minX = (int) p2.x;
		if(p1.y > p2.y)
			minY = (int) p2.y;
		return new Rectangle(minX, minY, (int) Math.abs(p2.x - p1.x), (int) Math.abs(p2.y - p1.y));
	}
	
	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {0, 0, 0};
		return rtnVal;
	}

	@Override
	public double[] invPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = {x, y, 0};
		return rtnVal;
	}
	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setRotation(double[] form) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void gatherObjInfo() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		if(x2 < x1)
		{
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		if(y2 < y1)
		{
			int t = y1;
			y1 = y2;
			y2 = t;
		}
		Waypoint[] points = dia.getAllItemsOfType("Waypoint");
		Vector<Waypoint> inPoints = new Vector<Waypoint>();
		Waypoint pa = null;
		Waypoint pb = null;
		for(int i = 0; i < points.length; i++)
		{
			Waypoint px = points[i];
			if(px.x >= x1 && px.x <= x2 && px.y >= y1 && px.y <= y2)
			{
				inPoints.add(px);
				/*if(pa == null)
					pa = px;
				else if(pb == null)
				{
					pb = px;
					break;
				}*/
			}
		}
		int dist = 0;
		Waypoint ppa;
		Waypoint ppb;
		for(int i = 0; i < inPoints.size() - 1; i++)
		{
			for(int j = i; j < inPoints.size(); j++)
			{
				ppa = inPoints.get(i);
				ppb = inPoints.get(j);
				int distX = (int) Math.sqrt((ppa.x - ppb.x) * (ppa.x - ppb.x) + (ppa.y - ppb.y) * (ppa.y - ppb.y));
				if(distX > dist)
				{
					pa = ppa;
					pb = ppb;
					dist = distX;
				}
			}
		}
		if(pa != null && pb != null)
		{
			p1 = pa;
			p2 = pb;
			return true;
		}
		else
			return false;
	}
	
	@Override
	public ButtonAction[] getActionNames() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String[] rtnVal = {"dist"};
		return rtnVal;
	}
	
	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		String[] rtnVal = {String.valueOf(dist)};
		return rtnVal;
		
	}
	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		dist = Integer.parseInt(in[0]);
	}
}

class TrackTransition extends UserObject implements TrackTransfer
{
	//int[][] transSpace;
	int x, y;
	int width, height;
	int x3, y3;
	int x4, y4;
	Track[] intersecting;
	boolean auto;
	
	TrackTransition(int x, int y, int w, int h, Track[] ts, boolean a)
	{
		//transSpace = space;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		intersecting = ts;
		auto = a;
	}
	
	
	
	@Override
	public boolean inXferSpace(double x, double y, Track src) 
	{
		if(src.rTheta != 0)
		{
			double[] xy = src.getPointRotation(x, y);
			x = xy[0];
			y = xy[1];
		}
		if(x >= this.x && y >= this.y)
		{
			if(x < this.x + this.width && y < this.height + this.y)
			{
				System.out.println("TTCheck: " + x + "," + y + ": in TT");
				return true;
			}
		}
		System.out.println("TTCheck: " + x + "," + y + ": not in TT");
		return false;
	}



	@Override
	public TrackLocation getNewLocation(Track srcTrack, double x, double y, double z) 
	{
		/*if(srcTrack.rTheta != 0)
		{
			double[] xy = srcTrack.getPointRotation(x, y);
			x = xy[0];
			y = xy[1];
		}*/
		System.out.println("does T" + srcTrack.index + " contain @" + x + "," + y + "?");
		//if x,y is on the source track
		if(srcTrack.moveOK(x, y))
			return new TrackLocation(srcTrack, x, y, z);
		else
		{
			//get the absolute point
			double[] abs = srcTrack.getPointRotation(x, y);
			x = abs[0];
			y = abs[1];
			//and see if any other track contains it
			for(int j = 0; j < intersecting.length; j++)
			{
				Track t = intersecting[j];
				System.out.println("does T" + t.index + " contain @" + abs[0] + "," + abs[1] + "?");
				if(t.moveOK(x, y))
				{
					double[] xy = t.invPointRotation(x, y);  //if it does, map it to the new track
					x = xy[0];
					y = xy[1];
					double tx = x - t.xInit;
					double ty = y - t.yInit;
					System.out.println("Moving to Track #" + t.index + " @" + x + "," + y);
					//try{Thread.sleep(1000);}catch(Exception ex){}
					return new TrackLocation(t, x, y, t.getZ(tx, ty));
				}
			}	
		}
		return null;
		/*int xx = (int) (x - this.x);
		int yy = (int) (y - this.y);
		System.out.println("Checking TT location:" + xx +"," + yy + ":" + transSpace[yy][xx]);
		if(transSpace[yy][xx] == -2)
			return null;
		if((transSpace[yy][xx] == srcTrack.index) || transSpace[yy][xx] < 0)
			return new TrackLocation(srcTrack.index, x, y, z);
		else
		{
			for(int j = 0; j < intersecting.length; j++)
			{
				Track t = intersecting[j];
				if(t.index == transSpace[yy][xx])
				{
					if(t.rTheta != 0)
					{
						double[] xy = t.invPointRotation(x, y);
						x = xy[0];
						y = xy[1];
					}
					double tx = x - t.xInit;
					double ty = y - t.yInit;
					return new TrackLocation(t.index, x, y, t.getZ(tx, ty));
				}
			}
		}*/
		//return new TrackLocation(srcTrack, x, y, z); // this line should never be reached
	}

	public Rectangle getBounds()
	{
		return new Rectangle(x, y, width, height);
	}
	
	public boolean isAuto()
	{
		return auto;
	}
	
	public Track[] getTracks()
	{
		return intersecting;
	}
	
}

class Track extends UserObject implements PropertyGenerator, Selectable, Placeable
{
	int index;
	
	double xInit;
	double yInit;
	double zInit;
	
	double xVert;
	double yVert;
	double zVert;
	
	double xHoriz;
	double yHoriz;
	double zHoriz;
	
	double rTheta;
	double rOriginX;
	double rOriginY;
	
	int width;
	int height;
	
	boolean freeSpace;
	boolean move[][];
	
	Diorama dRef;
	
	//TrackExit[] exits;
	TrackTransfer[] exits;
	
	Track(int w, int h)
	{
		this();
		width = w;
		height = h;
	}
	
	public Track()
	{
		exits = new TrackExit[0];
		setupFreeSpace(true);
		this.setHorizMovement(1,0,0);
		this.setVertMovement(0,1,0);
		rTheta = 0;
	}
	
	private void setupFreeSpace(boolean free)
	{
		freeSpace = free;
		if(!free)
		{
			move = new boolean[height][width];
			for(int i = 0; i < height; i++)
				for(int j = 0; j < width; j++)
					move[i][j] = true;
		}
		else
			move = null;
	}
	
	Track(int w, int h, boolean free)
	{
		this(w, h);
		setupFreeSpace(free);
	}
	
	void setIndex(int in)
	{
		index = in;
	}
	
	void setDioramaRef(Diorama in)
	{
		dRef = in;
	}
	
	void setHorizMovement(double x, double y, double z)
	{
		xHoriz = x;
		yHoriz = y;
		zHoriz = z;
	}
	
	void setVertMovement(double x, double y, double z)
	{
		xVert = x;
		yVert = y;
		zVert = z;
	}
	
	void setInit(double x, double y, double z)
	{
		xInit = x;
		yInit = y;
		zInit = z;
	}
	
	void setRotation(double x, double y, double theta)
	{
		rOriginX = x;
		rOriginY = y;
		rTheta = theta;
	}
	
	double[] incrMoveHoriz(double x, double y, double z, int dir)
	{
		double[] rtnVal = {index, x, y, z}; 
		double xp = x + xHoriz * dir * z;
		double yp = y + yHoriz * dir;
		double zp = z + zHoriz * dir;
		int w = (int) (xp - xInit); 
		int h = (int) (yp - yInit);
		
		for(int i = 0; i < exits.length; i++)
		{	
			TrackTransfer tt = exits[i];
			if(tt.inXferSpace(x, y, this))
			{
				System.out.println("In xfer space " + i);
				TrackLocation nl = tt.getNewLocation(this, xp, yp, zp);
				if(nl != null)
				{
					System.out.print("H:Moving to track #" + nl.tr.index);
					System.out.println(" x:" + nl.x + ", y:" + nl.y + ", z:" + nl.z);
					rtnVal[0] = nl.tr.index;
					rtnVal[1] = nl.x;
					rtnVal[2] = nl.y;
					rtnVal[3] = nl.z;
					return rtnVal;
				}
			}
		}
		if(w < 0 || w >= width)
		{
			System.out.println("w " + w + " is out of bounds - aborting move");
			return rtnVal;
		}
		/*double[] temp = getTrackXY(x, y);
		int w = (int) temp[0];
		int h = (int) temp[1];
		x = temp[2];
		y = temp[3];*/
		if(freeSpace || move[h][w])
		{
			rtnVal[0] = index;
			rtnVal[1] = xp;
			rtnVal[2] = yp;
			rtnVal[3] = zp;
		}
		System.out.println("SpMovedTo:" + xp + "," + yp + "," + zp);
		
		System.out.println(getPos(rtnVal));
		return rtnVal;
	}
	
	private double[] getTrackXY(double inX, double inY)
	{
		double w = inX - xInit;
		double h = inY - yInit;
		if(w <= 0)
		{
			w = 0;
			inX = xInit;
		}
		if(h <= 0)
		{
			h = 0;
			inY = yInit;
		}
		if(w >= width)
		{
			w = width - 1;
			inX = xInit + width - 1;
		}
		if(h >= height)
		{
			h = height - 1;
			inY = yInit + height - 1;
		}
		double[] rtnVal = {w, h, inX, inY};
		return rtnVal;	
	}
	
	double[] incrMoveVert(double x, double y, double z, int dir)
	{
		double[] rtnVal = {index, x, y, z}; 
		double xp = x + xVert * dir;
		double yp = y + yVert * dir;
		double zp = z + zVert * dir;
		//double[] temp = getTrackXY(x, y);
		int w = (int) (xp - xInit); 
		int h = (int) (yp - yInit);
		
		for(int i = 0; i < exits.length; i++)
		{	
			TrackTransfer tt = exits[i];
			if(tt.inXferSpace(x, y, this))
			{
				System.out.println("In xfer space " + i);
				TrackLocation nl = tt.getNewLocation(this, xp, yp, z);
				if(nl != null)
				{
					System.out.print("H:Moving to track #" + nl.tr.index);
					System.out.println(" x:" + nl.x + ", y:" + nl.y + ", z:" + nl.z);
					rtnVal[0] = nl.tr.index;
					rtnVal[1] = nl.x;
					rtnVal[2] = nl.y;
					rtnVal[3] = nl.z;
					return rtnVal;
				}
			}
		}
		if(h < 0 || h >= height)
		{
			//System.out.println("y " + y + " - yi " + yInit + " = h " + h); 
			System.out.println("h " + h + " is out of bounds - aborting move");
			return rtnVal;
		}
		/*int w = (int) temp[0];
		int h = (int) temp[1];
		x = temp[2];
		y = temp[3];*/
		if(w >= width)
			System.out.println(x + "," + xInit + "," + (int) (x - xInit));
		if(freeSpace || move[h][w])
		{
			rtnVal[0] = index;
			rtnVal[1] = xp;
			rtnVal[2] = yp;
			rtnVal[3] = zp;
		}
		System.out.println(getPos(rtnVal));
		return rtnVal;
	}
	
	public String getPos(double[] in)
	{
		String s = "T:" + in[0];
		s += " x:" + in[1];
		s += " y:" + in[2];
		s += " z:" + in[3];
		return s;
	}
	
	public void view(Graphics g, Track[] list)
	{
		Color c = getTrackColor(list);
		g.setColor(c);
		int xi = (int) xInit;
		int yi = (int) yInit;
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				if(move[i][j])
					g.fillRect(i + xi, j + yi, 1, 1);
	}
	
	private Color getTrackColor(Track[] list)
	{
		switch(index % 3)
		{
		case 0:
			return Color.RED;
		case 1:
			return Color.BLUE;
		case 2:
			return Color.GREEN;
		}
		return null;
	}
	
	public void addXfer(TrackTransfer ex)
	{
		TrackTransfer[] exits2 = new TrackTransfer[exits.length + 1];
		for(int i = 0; i < exits.length; i++)
			exits2[i] = exits[i];
		exits2[exits.length] = ex;
		exits = exits2;
	}
	
	public void removeXfer(TrackTransfer ex)
	{
		TrackTransfer[] exits2 = new TrackTransfer[exits.length - 1];
		int plus = 0;
		for(int i = 0; i < exits.length; i++)
		{
			if(exits[i] == ex)
				plus = 1;
			else
				exits2[i - plus] = exits[i];
		}
		exits = exits2;
	}
	
	public void removeAutoXfers()
	{
		int c = 0;
		System.out.println("Exits:" + exits.length);
		for(int i = 0; i < exits.length; i++)
		{
			System.out.println(exits[i]);
			if(exits[i].isAuto())
				c++;
		}
		System.out.println("C is " + c);
		TrackTransfer[] exits2 = new TrackTransfer[exits.length - c];
		int plus = 0;
		for(int i = 0; i < exits.length; i++)
		{
			if(exits[i].isAuto())
			{
				TrackTransition tt = (TrackTransition) exits[i];
				Track other = tt.intersecting[0];
				if(other == this)
					other = tt.intersecting[1];
				TrackTransfer[] otherExits = other.exits;
				Rectangle r1 = tt.getBounds();
				for(int j = 0; j < otherExits.length; j++)
				{
					if(exits[i] == otherExits[j])
					{
					
						other.removeXfer(otherExits[j]);
						System.out.println("Removed other exit #" + j);
					}
				}
				System.out.println("Removed exit #" + i);
				plus++;
			}
			else
				exits2[i - plus] = exits[i];
		}
		exits = exits2;
	}
	
	public double getZ(double inX, double inY)
	{
		return zInit + (inX * zHoriz) + (inY * zVert);
	}
	
	public boolean moveOK(double inX, double inY)
	{
		double[] dd = {inX, inY};
		if(rTheta != 0)
			dd = invPointRotation(inX, inY);
		int xx = (int) (dd[0] - xInit);
		int yy = (int) (dd[1] - yInit);
		
		if(xx < 0 || xx >= width)
			return false;
		if(yy < 0 || yy >= height)
			return false;
		if(freeSpace)
			return true;
		else
			return move[yy][xx];
		
	}

	//PropertyGenerator
	@Override
	public ButtonAction[] getActionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String[] rv = {"width","height","xInit","yInit","zInit","xVert","yVert","zVert","xHoriz","yHoriz","zHoriz"};
		return rv;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		double[] rv = {width,height,xInit,yInit,zInit,xVert,yVert,zVert,xHoriz,yHoriz,zHoriz};
		String[] rv2 = new String[rv.length];
		for(int i = 0; i < rv.length; i++)
			rv2[i] = String.valueOf(rv[i]);
		return rv2; 
	}

	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		width = Integer.parseInt(in[0]);
		height = Integer.parseInt(in[1]);
		xInit = Double.parseDouble(in[2]);
		yInit = Double.parseDouble(in[3]);
		zInit = Double.parseDouble(in[4]);
		xVert = Double.parseDouble(in[5]);
		yVert = Double.parseDouble(in[6]);
		zVert = Double.parseDouble(in[7]);
		xHoriz = Double.parseDouble(in[8]);
		yHoriz = Double.parseDouble(in[9]);
		zHoriz = Double.parseDouble(in[10]);
	}

	
	/*public boolean contains(int x, int y) 
	{
		if(x >= xInit && x <= (xInit + width))
			if(y >= yInit && y <= (yInit + height))
				return true;
		// TODO Auto-generated method stub
		return false;
	}*/

	//Selectable
	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "Track #" + index;
	}

	@Override
	public Rectangle getRect() 
	{
		Rectangle r = new Rectangle((int)xInit,(int) yInit, width, height);
		// TODO Auto-generated method stub
		return r;
	}
	
	@Override
	public void setRect(Rectangle in)
	{
		xInit = in.x;
		yInit = in.y;
		width = in.width;
		height = in.height;
		
		setupFreeSpace(freeSpace);
		
		removeAutoXfers();
		dRef.addTrackTransitions(this);
	}

	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		double[] rtnVal = new double[3];
		rtnVal[0] = rOriginX;
		rtnVal[1] = rOriginY;
		rtnVal[2] = rTheta;
		return rtnVal;
	}

	@Override
	public void setRotation(double[] form) 
	{
		// TODO Auto-generated method stub
		rOriginX = form[0];
		rOriginY = form[1];
		rTheta = form[2];
		
		setupFreeSpace(freeSpace);
		
		removeAutoXfers();
		dRef.addTrackTransitions(this);
	}
	
	public double[] getPointRotation(double x, double y)
	{
		double[] rtnVal = {x,y};
		if(rTheta != 0)
		{
			AffineTransform xf = AffineTransform.getRotateInstance(rTheta, rOriginX, rOriginY);
			Point2D p = xf.transform(new Point((int) x, (int) y), null);
			rtnVal[0] = p.getX();
			rtnVal[1] = p.getY();
		}
		return rtnVal;
	}
	
	public double[] invPointRotation(double x, double y)
	{
		double[] rtnVal = {x,y};
		if(rTheta != 0)
		{
			AffineTransform xf = AffineTransform.getRotateInstance(rTheta * -1, rOriginX, rOriginY);
			Point2D p = xf.transform(new Point((int) x, (int) y), null);
			rtnVal[0] = p.getX();
			rtnVal[1] = p.getY();
		}
		return rtnVal;
	}
	
	public void drawInEditor(Graphics g, Diorama dia)
	{
		int initX = (int) xInit;
		int initY = (int) yInit;
		g.setColor(new Color(0,0,255,48));
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform orig = null;
		if(getXform()[2] != 0)
		{
			double[] rot = getXform();
			AffineTransform xForm = AffineTransform.getRotateInstance(rot[2], rot[0], rot[1]);
			orig = g2.getTransform();
			g2.transform(xForm);
		}
		g2.fillRect(initX, initY, width, height);
		if(orig != null)
			g2.setTransform(orig);
		
		for(int i = 0; i < exits.length; i++)
		{
			TrackTransfer xfer = exits[i];
			Track[] toTracks = xfer.getTracks();
			boolean drawMe = true;
			for(int j = 0; j < toTracks.length; j++)
			{
				Track tx = toTracks[j];
				if(tx.index < this.index)
				{
					drawMe = false;
					break;
				}
			}
			if(drawMe)
			{
				Rectangle r = exits[i].getBounds();
				//System.out.println("TE filling:" + r.x + "," + r.y + "," + r.width + "," + r.height);
				g.setColor(new Color(0,255,0,96));
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}
	}
	
	//Placeble
	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		xInit = x1;
		yInit = y1;
		width = (x2 - x1);
		height = (y2 - y1);
		
		
		setIndex(dia.countItemOfType("Track"));
		setDioramaRef(dia);
		System.out.println("Adding tts:");
		dia.addTrackTransitions(this);
		return true;
	}
	
	
	
	public static Track[] getTracks(Diorama d)
	{
		UserObject[] ats = d.getAllItemsOfType("Track");
		Track[] tracks = new Track[ats.length];
		for(int i = 0; i < ats.length; i++)
			tracks[i] = (Track) ats[i];
		return tracks;
	}
}

class PaintedImage extends UserObject implements PropertyGenerator, Placeable, Drawable, Selectable
{
	transient BufferedImage img;
	String fromFile;
	int layer;
	int x;
	int y;
	int w;
	int h;
	
	boolean sizeLocked;
	
	PaintedImage(ImageIcon ico, int l, int x, int y, int w, int h)
	{
		super();
		if(ico != null)
			img = Diorama.bufferImage(ico);
		layer = l;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public PaintedImage()  //empty constructor used for other components
	{
		super();
		layer = 0;
	}
	
	int getX()
	{
		return x;
	}
	
	int getY()
	{
		return y;
	}
	
	int getDrawX()
	{
		return x;	
	}
	
	int getDrawY()
	{
		return y;
	}
	
	int[] getDrawXY()
	{
		int[] rtnVal = {x,y};
		return rtnVal;
	}
	
	int getDrawWid()
	{
		return w;
	}
	
	int getDrawHgt()
	{
		return h;
	}
	
	public boolean isSprite()
	{
		return false;
	}

	//PropertyGenerator
	@Override
	public ButtonAction[] getActionNames() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String rv[] = {"fromFile","layer","x","y","width","height","sizeLocked"};
		return rv;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		double[] rv = {layer, x, y, w, h};
		String[] rv2 = new String[rv.length + 2];
		
		for(int i = 1; i < rv.length; i++)
			rv2[i + 1] = String.valueOf(rv[i]);
		rv2[0] = fromFile;
		rv2[rv2.length - 1] = String.valueOf(sizeLocked);
		return rv2;
		
	}

	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		String oldFrom = fromFile;
		fromFile = in[0];
		if(!oldFrom.equals(fromFile))
			reload();
		layer = Integer.parseInt(in[1]);
		x = Integer.parseInt(in[2]);
		y = Integer.parseInt(in[3]);
		w = Integer.parseInt(in[4]);
		h = Integer.parseInt(in[5]);
		sizeLocked = Boolean.parseBoolean(in[6]);
	}
	
	public void reload()
	{
		ImageIcon icn = Diorama.getImgIcon(fromFile);
		img = Diorama.bufferImage(icn);
	}

	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		PictureLoader pl = new PictureLoader(false);
		
		fromFile = pl.getImgFile();
		System.out.println("PaintedImage: loading " + fromFile);
		if(fromFile != null)
			reload();
	}

	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		if(fromFile == null)
			return false;
		x = x1;
		y = y1;
		w = (x2 - x1);
		h = (y2 - y1);
		return true;
	}
	
	//Drawable
	@Override
	public void draw(Graphics g, Diorama d) 
	{
		// TODO Auto-generated method stub
		if(sizeLocked)
			g.drawImage(img, x, y, d);
		else
			g.drawImage(img, x, y, w, h, d);
	}

	public double getLayer()
	{
		return layer;
	}
	
	public BufferedImage getImg()
	{
		return img;
	}
	
	
	
   @Override
	public Rectangle getDrawArea() 
   {
		// TODO Auto-generated method stub
		return new Rectangle(x, y, w, h);
	}

	//Selectable
	@Override
	public void drawInEditor(Graphics g, Diorama dia) 
	{
		// TODO Auto-generated method stub
		draw(g, dia);
	}

	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "PaintedImg";
	}

	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return new double[3];
	}

	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		return new Rectangle(x, y, w, h);
	}

	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		return new double[3];
	}

	@Override
	public double[] invPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return new double[3];
	}

	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		x = in.x;
		y = in.y;
		w = in.width;
		h = in.height;
	}

	@Override
	public void setRotation(double[] form) 
	{
		// TODO Auto-generated method stub
		
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();
		ImageIcon ico = Diorama.getImgIcon(fromFile);
		img = Diorama.bufferImage(ico);
	}
	
}

// a sprite frame is a painted image with a transform
class SpriteFrame implements Serializable
{
	AffineTransform af;
	transient BufferedImage img;
	String fromFile;
	
	public SpriteFrame()
	{
		
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException 
	{
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();
		ImageIcon ico = Diorama.getImgIcon(fromFile);
		img = Diorama.bufferImage(ico);
	}
}

class PaintedSprite extends UserObject implements Selectable, PropertyGenerator, Drawable, Placeable
{
	transient SpriteFrame[] frames;
	int currFrame;
	String name;
	int currLoop;
	double xx;
	double yy;
	double zz;
	
	int x, y, w, h;
	
	int trackNo;
	Track onTrack;
	
	class FrameProperties implements PropertyGenerator, Serializable
	{
		double deltaX;
		double deltaY;
		double deltaZ;
		int deltaAlpha;
		transient CollisionSpace cs;
		
		FrameProperties()
		{
			deltaX = 0;
			deltaY = 0;
			deltaZ = 0;
			deltaAlpha = 0;
			
			//CollisionSpace cs;
		}
		
		FrameProperties(int frameNo)
		{
			this();
			//cs = new CollisionSpace(frames[frameNo]);
		}
		
		public ButtonAction[] getActionNames() 
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		public String[] getPropertyNames() 
		{
			String[] rtnVal = {"deltaX", "deltaY", "deltaZ", "deltaAlpha"};
			return rtnVal;
		}
		
		public String[] getPropertyValues() 
		{
			String[] rtnVal = {String.valueOf(deltaX), String.valueOf(deltaY), String.valueOf(deltaZ), String.valueOf(deltaAlpha)};
			return rtnVal;
		}
		
		public void setPropertyValues(String[] in) 
		{
			deltaX = Double.parseDouble(in[0]);
			deltaY = Double.parseDouble(in[1]);
			deltaZ = Double.parseDouble(in[2]);
			deltaAlpha = Integer.parseInt(in[3]);			
		}
	}
	
	class LoopProperties implements PropertyGenerator, Serializable
	{
		boolean looped;
		int startFrame;
		int endFrame;
		int exitToFrame;
		int exitToLoop;
		String stateDescription;
		
		public LoopProperties()
		{
			looped = true;
			startFrame = 0;
			endFrame = 0;
			exitToFrame = 0;
			exitToLoop = -1;
			stateDescription = "";
		}
		
		public LoopProperties(int s, int e, String desc)
		{
			this();
			startFrame = s;
			endFrame = e;
			stateDescription = desc;
		}
		@Override
		public ButtonAction[] getActionNames() 
		{
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public String[] getPropertyNames() 
		{
			String[] rtnVal = {"looped","startFrame","endFrame","exitToFrame","exitToLoop","stateDescription"};
			return rtnVal;
		}
		@Override
		public String[] getPropertyValues() 
		{
			String[] rtnVal = {String.valueOf(looped), String.valueOf(startFrame), String.valueOf(endFrame), String.valueOf(exitToFrame), String.valueOf(exitToLoop), stateDescription};
			return rtnVal;
		}
		@Override
		public void setPropertyValues(String[] in) 
		{
			looped = Boolean.parseBoolean(in[0]);
			startFrame = Integer.parseInt(in[1]);
			endFrame = Integer.parseInt(in[2]);
			exitToFrame = Integer.parseInt(in[3]);
			exitToLoop = Integer.parseInt(in[4]);
			stateDescription = in[5];
			
		}
		
		
	}
	
	class CollisionSpace
	{
		long[] space;
		//boolean enabled;
		
		CollisionSpace(BufferedImage fr)
		{
			for(int i = 0; i < fr.getHeight(); i++)
			{
				for(int j = 0; j < fr.getWidth(); j++)
				{
					//if the pixel is not transparent
					int c = fr.getRGB(i, j);
					int alpha = c & 255;
					if(alpha > 0)
						space[i] += (1L << (fr.getWidth() - j));
				}
			}
		}
		
		
		//TODO: test this
		private boolean detectCollision(CollisionSpace s1, CollisionSpace s2, int x1, int y1, int x2, int y2)
		{
			int deltaX = x2 - x1;
			int deltaY = y2 - y1;
			
			for(int i = y1; i < deltaY; i++)
			{
				if((s1.space[i] & (s2.space[i] >> (64-deltaX))) > 0)
					return true;
			}
			return false;
		}
	}
	
	class KBResponse implements PropertyGenerator, Serializable
	{
		Integer[] kbChange;
		
		public KBResponse()
		{
			kbChange = new Integer[10];
			for(int i = 0; i < 10; i++)
				kbChange[i] = null;
			kbChange[0] = new Integer(KeyEvent.VK_LEFT);
			kbChange[5] = new Integer(KeyEvent.VK_RIGHT);
		}
		
		public void addKbResponse(Integer[] data)
		{
			//check to see if it's already in the list
			for(int i = 0; i < kbChange.length; i+=5)
			{
				if(data[0] == kbChange[i])
					return;
			}
			
			//if not, add it
			Integer[] x = new Integer[kbChange.length + 5];
			for(int i = 0; i < kbChange.length; i++)
				x[i] = kbChange[i];
			for(int i = 0; i < 5; i++)
				x[kbChange.length + i] = data[i];
			kbChange = x;
		}

		@Override
		public ButtonAction[] getActionNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getPropertyNames() 
		{
			String[] s = new String[kbChange.length / 5];
			int[] dirs = {KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_DOWN};
			String[] dirStr = {"Right", "Left", "Up", "Down"};
			int[] ctrl = {KeyEvent.VK_ESCAPE, KeyEvent.VK_SPACE, KeyEvent.VK_TAB, KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL};
			String[] ctrlStr = {"Esc", "Space", "Tab", "Shift"};
			int[] alpha = {KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_W};
			String[] alphaStr = {"A", "S", "D", "Z", "X", "C", "W"};
			int j = 0;
			boolean found = false;
			for(int i = 0; i < kbChange.length; i+=5)
			{
				for(int k = 0; k < dirs.length; k++)
				{
					if(kbChange[i] == dirs[k])
					{
						s[j] = dirStr[k];
						found = true;
						break;
					}
				}
				if(!found)
				{
					for(int k = 0; k < ctrl.length; k++)
					{
						if(kbChange[i] == dirs[k])
						{
							s[j] = ctrlStr[k];
							found = true;
							break;
						}
					}
				}
				if(!found)
				{
					for(int k = 0; k < alpha.length; k++)
					{
						if(kbChange[i] == dirs[k])
						{
							s[j] = alphaStr[k];
							found = true;
							break;
						}
					}
				}
				if(!found)
					s[j] = String.valueOf(kbChange[i]);
				j++;
			}
			return s;
		}

		@Override
		public String[] getPropertyValues() 
		{
			String[] s = new String[kbChange.length / 5];
			int k = 0;
			for(int i = 0; i < kbChange.length; i += 5)
			{
				String x = getKbDelta(kbChange[i + 1]);
				
				for(int j = 2; j < 5; j++)
				{
					x += "," + getKbDelta(kbChange[i + j]);
				}
				s[k] = x;
				k++;
				
			}
			return s;
		}
		
		private String getKbDelta(Integer in)
		{
			if(in == null)
				return "";
			if(in < 1)
			{
				return String.valueOf(in);
			}
			if((in & (1 << 31)) > 0)
			{
				String r = "+";
				int x = in & ((1 << 31) - 1);
				return r + String.valueOf(x);
			}
			else
				return String.valueOf(in);
		}
		
		private Integer setKbDelta(String in)
		{
			if(in.equals(""))
				return null;
			Integer rtnVal = Integer.parseInt(in);
			if(rtnVal < 0)
				return rtnVal;
			if(in.charAt(0) == '+')
			{
				rtnVal += (1 << 31);
			}
			return rtnVal;
		}

		@Override
		public void setPropertyValues(String[] in) 
		{
			// TODO Auto-generated method stub
			for(int i = 0; i < in.length; i++)
			{
				String[] x = in[i].split(",");  //this function will not always get 4 elements
				String[] y = new String[4];
				for(int j = 0; j < x.length; j++)
					y[j] = x[j];
				for(int j = x.length; j < 4; j++)
					y[j] = new String("");
				
				for(int j = 0; j < 4; j++)
				{
					//System.out.println(x[j]);
					kbChange[(i*5) + j + 1] = setKbDelta(y[j]);
				}
			}
		}
		
		public Integer[] getPressInfo(int code)
		{
			for(int i = 0; i < kbChange.length; i += 5)
			{
				if(kbChange[i] == code)
				{
					Integer[] rtnVal = {kbChange[i+1], kbChange[i+2]};
					return rtnVal;
				}
			}
			return null;
		}
		
		public Integer[] getReleaseInfo(int code)
		{
			for(int i = 0; i < kbChange.length; i += 5)
			{
				if(kbChange[i] == code)
				{
					Integer[] rtnVal = {kbChange[i+3], kbChange[i+4]};
					return rtnVal;
				}
			}
			return null;
		}
		
	}
	
	FrameProperties[] frameProperties;
	LoopProperties[] loopProperties;
	KBResponse kbr;
	
	public PaintedSprite()
	{
		super();
	}
	
	PaintedSprite(String fr[], double z, double x, double y, int w, int h)
	{
		//super(null, -1, -1, -1, w, h);
		this();
		this.w = w;
		this.h = h;
		frames = new SpriteFrame[fr.length];
		frameProperties = new FrameProperties[fr.length];
		for(int i = 0; i < fr.length; i++)
		{
			frames[i] = new SpriteFrame();
			frames[i].img = Diorama.loadPicture(fr[i]);
			frameProperties[i] = new FrameProperties(i);
			
		}
		loopProperties = new LoopProperties[1];
		loopProperties[0] = new LoopProperties();
		kbr = new KBResponse();
		zz = z;
		xx = x;
		yy = y;
		
		currFrame = 0;
		currLoop = -1;
	}
	
	int getX()
	{
		return (int) xx;
	}
	
	int getY()
	{
		return (int) yy;
	}
	
	/*int getDrawX() // deprecated
	{
		double absx = (w * zz - w) / 2 + xx;
		if(onTrack.rTheta != 0)
			return (int) 0;
		else
			return (int) (absx);
	}
	
	int getDrawY() //deprecated
	{
		double newx = (h * zz - h) / 2;
		return (int) (yy + newx);
	}*/
	
	int[] getDrawXY()
	{
		double newX = (w * zz - w) / 2 + xx;
		double newY = (h * zz - h) / 2 + yy;
		int[] rtnVal = new int[2];
		if(onTrack != null && onTrack.rTheta != 0)
		{
			double[] xy = onTrack.getPointRotation(newX, newY);
			rtnVal[0] = (int) xy[0];
			rtnVal[1] = (int) xy[1];
		}
		else
		{
			rtnVal[0] = (int) newX;
			rtnVal[1] = (int) newY;
		}
		//System.out.println("Drawing sprite @" + rtnVal[0] + "," + rtnVal[1]);
		return rtnVal;
	}
	
	int getDrawWid()
	{
		return (int) (w * zz);
	}
	
	int getDrawHgt()
	{
		return (int) (h * zz);
	}
	
	public double getLayer()
	{
		return zz;
	}
	
	public BufferedImage getImg()
	{
		return frames[currFrame].img;
	}

	@Override
	public ButtonAction[] getActionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyNames() 
	{
		// TODO Auto-generated method stub
		String[] rv = {"fromFile", "x","y","z","trackNo"};
		return rv;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		double[] rv = {xx, yy, zz, trackNo};
		String[] rv2 = new String[rv.length + 1];
		
		for(int i = 1; i < rv.length; i++)
			rv2[i + 1] = String.valueOf(rv[i]);
		return rv2;
		
	}

	@Override
	public void setPropertyValues(String[] in) 
	{
		// TODO Auto-generated method stub
		//fromFile = in[0];
		xx = Double.parseDouble(in[1]);
		yy = Double.parseDouble(in[2]);
		zz = Double.parseDouble(in[3]);
		trackNo = Integer.parseInt(in[4]);
	}
	
	public boolean isSprite()
	{
		return true;
	}
	
	public void processKeys(int code, boolean isRelease)
	{
		Integer[] x;
		
		if(isRelease)
			x = kbr.getReleaseInfo(code);
		else
			x = kbr.getPressInfo(code);
		
		//frame
		if(x[0] != null)
		{
			if(x[0] < 0)
				currFrame += x[0];
			else if((x[0] & (1 << 31)) > 0)
			{
				int y = x[0] & ((1 << 31) - 1);
				currFrame += y;
			}
			else
			{
				currFrame = x[0];
				currLoop = -1;
			}
		}
		//loop
		if(x[1] != null)
		{
			int z = currLoop;
			if(x[1] < 0)
				currLoop += x[1];
			else if((x[1] & (1 << 31)) > 0)
			{
				int y = x[1] & ((1 << 31) - 1);
				currLoop += y;
			}
			else
				currLoop = x[1];
			if(currLoop != z)
				changeLoop(currLoop);
		}
		//else
			//changeLoop(-1);
	}
	
	private void changeLoop(int n)
	{
		currFrame = loopProperties[n].startFrame;
	}
	
	public void advanceFrame()
	{
		if(currLoop != -1)
		{
			if(loopProperties[currLoop].endFrame > currFrame)
				currFrame++;
			else
			{
				if(loopProperties[currLoop].looped)
					currFrame = loopProperties[currLoop].startFrame;
				else
				{
					currFrame = loopProperties[currLoop].exitToFrame;
					if(loopProperties[currLoop].exitToLoop != -1)
						changeLoop(loopProperties[currLoop].exitToLoop);
				}
			}
		}
	}

	@Override
	public String getItemName() 
	{
		// TODO Auto-generated method stub
		return "Sprite:" + name;
	}

	@Override
	public Rectangle getRect() 
	{
		// TODO Auto-generated method stub
		return new Rectangle(x, y, w, h);
	}

	@Override
	public void setRect(Rectangle in) 
	{
		// TODO Auto-generated method stub
		x = in.x;
		y = in.y;
		w = in.width;
		h = in.height;
	}

	@Override
	public double[] getXform() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRotation(double[] form) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] getPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] invPointRotation(double x, double y) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void drawInEditor(Graphics g, Diorama dia)
	{
		draw(g, dia);
	}

	@Override
	public void gatherObjInfo() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia) 
	{
		// TODO Auto-generated method stub
		x = x1;
		y = y1;
		w = x2 - x1;
		h = y2 - y1;
		return true;
	}

	@Override
	public void draw(Graphics g, Diorama d) 
	{
		int[] xy = getDrawXY();
		g.drawImage(frames[currFrame].img, xy[0], xy[1], w, h, d);
		
	}

	@Override
	public Rectangle getDrawArea() 
	{
		// TODO Auto-generated method stub
		return getRect();
	}
}

/*interface Drawable
{
	public void draw(Graphics g, Diorama dia);
}*/

class Diorama extends JPanel implements MouseListener, MouseMotionListener
{
	//an image
	//PaintedImage[] images;
	PaintedSprite plrActor;
	//a group of tracks
	//Track[] tracks;
	int moveHoriz = 0;
	int moveVert = 0;
	int maxLayer;
	
	Vector<Serializable> objectList;
	Vector<Drawable> paintables;
	Vector<Updatable> updatables;
	
	int rollovers;
	Vector<Interactive> interactives;
	
	//Timer tim;
	
	
	int w, h;
	/*Vector<Drawable> paintables;
	/*Vector<Movable> movables;
	Vector<Collidable> collidables;*/
	String filename;
	
	//use an Initializer to reset a diorama that has been loaded from a file
	class Initializer implements Serializable
	{
		void init(){}
		
		void initWithObject(Object obj){}
		
		void initWithObjectList(Object[] list){}
		
		Initializer(){}
	}
	
	public void setInitializer(Initializer init)
	{
		initializer = init;
	}
	
	Initializer initializer;
	
	class KBInput extends KeyAdapter
	{
		Diorama dia;
		
		KBInput(Diorama d)
		{
			super();
			dia = d;
		}
		
		public void keyPressed(KeyEvent arg0) 
		{
			//System.out.println("I pressed " + arg0.getKeyCode());
			/*switch(arg0.getKeyCode())
			{
			case KeyEvent.VK_UP:
				moveVert = -1; 
				break;
			case KeyEvent.VK_DOWN:
				moveVert = 1;
				break;
			case KeyEvent.VK_LEFT:
				moveHoriz = -1;
				break;
			case KeyEvent.VK_RIGHT:
				moveHoriz = 1;
				break;
			}*/
			
			Iterator<Interactive> kit = interactives.iterator();
			while(kit.hasNext())
			{
				IOHandler[] ios = kit.next().getIOs();
				for(int i = 0; i < ios.length; i++)
					ios[i].kbPress(arg0.getKeyCode(), dia);
			}
		}
		
		public void keyReleased(KeyEvent arg0) 
		{
			//System.out.println("I released " + arg0.getKeyCode());
			/*switch(arg0.getKeyCode())
			{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				moveVert = 0;
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
				moveHoriz = 0;
				break;
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			}*/
			
			Iterator<Interactive> kit = interactives.iterator();
			while(kit.hasNext())
			{
				IOHandler[] ios = kit.next().getIOs();
				for(int i = 0; i < ios.length; i++)
					ios[i].kbRelease(arg0.getKeyCode(), dia);
			}
		}
		
		public void keyTyped(KeyEvent arg0) 
		{
			//System.out.println("I typed " + arg0.getKeyCode());
			//System.out.println("Comparing " + arg0.getKeyCode() + " with " + KeyEvent.VK_ESCAPE);
			if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
				System.exit(0);
		}
	}
	
	Diorama()
	{
		
		plrActor = null;
		//tracks = new Track[0];
		objectList = new Vector<Serializable>();
		paintables = new Vector<Drawable>();
		interactives = new Vector<Interactive>();
		updatables = new Vector<Updatable>();
		enableMouse();
		//tim = new Timer(25, this);
		addKeyListener(new KBInput(this));
		initializer = new Initializer();
		w = 800;
		h = 600;
	}
	
	
	
	public void suppressMouse()
	{
		removeMouseListener(this);
	}
	
	public void enableMouse()
	{
		addMouseListener(this);
	}
	
	private void mouse(boolean rollover, int code, MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		int btn = e.getButton();
		if(btn == MouseEvent.BUTTON3)
			code += MouseIO.RBUTTON;
		int n = interactives.size();
		Iterator<Interactive> iit = interactives.iterator();
		if(rollover)
			n = rollovers;
		for(int i = 0; i < n; i++)
		{
			Interactive iv = iit.next();
			if(iv.containsPoint(x, y))
			{
				IOHandler[] ios = iv.getIOs();
				for(int j = 0; j < ios.length; j++)
				{
					IOHandler io = ios[j];
					io.handleMouse(code, x, y, this);
				}
			}
			else if(rollover)
			{
				IOHandler[] ios = iv.getIOs();
				for(int j = 0; j < ios.length; j++)
				{
					IOHandler io = ios[j];
					io.handleMouse(MouseIO.ROLLOFF, x, y, this);
				}
			}
		}
	}
	
	public void addHandler(IOHandler io, Interactive iv)
	{
		iv.addHandler(io);
		int n = interactives.indexOf(iv);
		if(io.hasRollover && n < rollovers)
		{
			interactives.remove(iv);
			interactives.add(0, iv);
			rollovers++;
		}
	}
	
	public void removeHandler(IOHandler io, Interactive iv)
	{
		if(!iv.hasHandler(io))
			return;
		int n = interactives.indexOf(iv);
		iv.removeHandler(io);
		boolean stillRollover = false;
		IOHandler hs[] = iv.getIOs();
		for(int i = 0; i < hs.length; i++)
		{
			IOHandler ix = hs[i];
			if(ix.hasRollover)
			{
				stillRollover = true;
				break;
			}
		}
		if(n < rollovers && !stillRollover)
		{
			interactives.remove(iv);
			interactives.add(iv);
			rollovers--;
		}
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		mouse(false, MouseIO.DRAGTO, e);
	}



	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		mouse(true, MouseIO.ROLLON, e);
	}



	@Override
	public void mouseClicked(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		mouse(false, MouseIO.CLICK, e);
	}



	@Override
	public void mouseEntered(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void mouseExited(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void mousePressed(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		mouse(false, MouseIO.PRESS, e);
	}



	@Override
	public void mouseReleased(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		mouse(false, MouseIO.RELEASE, e);
	}



	public <U> U getItemOfType(String objType, int index)
	{
		int n = objectList.size();
		Iterator<Serializable> sit = objectList.iterator();
		for(int i = 0; i < n; i++)
		{
			Serializable uo = sit.next();
			String s = uo.getClass().getName();
			if(s.equals(objType))
			{
				if(index == 0)
				{
					//Class cls = Class.forName(s);
					return (U) uo;
				}
				index--;
				
			}
		}
		return null;
	}
	
	public int countItemOfType(String objType)
	{
		int cnt = 0;
		int n = objectList.size();
		Iterator<Serializable> sit = objectList.iterator();
		for(int i = 0; i < n; i++)
		{
			Serializable uo = sit.next();
			String s = uo.getClass().getName();
			if(s.equals(objType))
				cnt++;
		}
		return cnt;
	}
	
	/*public Vector<? extends UserObject> getAllItemsOfType(String objType)
	{
		Vector<? extends UserObject> uos = new Vector<? extends UserObject>(); 
		int n = objectList.size();
		for(int i = 0; i < n; i++)
		{
			UserObject uo = objectList.get(i);
			String s = uo.getClass().getName();
			if(s.equals(objType))
				uos.add(uo);
		}
		
		//UserObject[] uos2 = new UserObject[uos.size()];
		//uos2 = uos.toArray(uos2);
		return uos;
	}*/
	
	public <U> U[] getAllItemsOfType(String objType)
	{
		U obj = this.getType(objType);
		if(obj == null)
		{
			return null;
		}
		Vector<U> rtn1 = this.loadUp(obj, objectList);
		U[] rtn2 = (U[]) Array.newInstance(obj.getClass(), rtn1.size());
		rtn1.toArray(rtn2);
		return rtn2;
	}
	
	public void removeItem(Object obj)
	{
		objectList.remove(obj);
		if(obj instanceof Drawable)
			recalcSize();
	}
	
	private <T> T getType(String in)
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
	
	private <U> Vector<U> loadUp(U in, Vector<Serializable> all)
	{
		Vector<U> x = new Vector<U>();
		
		int n = all.size();
		Iterator<Serializable> sit = all.iterator();
		for(int i = 0; i < n; i++)
		{
			Serializable ca = sit.next();
			
			if(in.getClass().getName().equals(ca.getClass().getName()))
			{
				U ub = (U) ca;
				x.add(ub);
			}
		}
		return x;
	}
	
	/*private void insertPImage(PaintedImage nimg)
	{
		int newLen = images.length + 1;
		PaintedImage[] newImages = new PaintedImage[newLen];
		if(newLen == 1)
		{
			newImages[0] = nimg;
			images = newImages;
			return;
		}
		int i = 0;
		for(i = 0; i < images.length; i++)
		{
			//System.out.println(i + ":" + images[i].toString());
			if(images[i].getLayer() < nimg.getLayer())
				newImages[i] = images[i];
			else
			{
				newImages[i] = nimg;
				break;
			}
		}
		if(i == images.length)
			newImages[i] = nimg;
		else
		{
			for(i = i; i < images.length; i++)
				newImages[i + 1] = images[i];
		}
		images = newImages;
		//for(i = 0; i < images.length; i++)
			//System.out.println(i + ":" + images[i].toString());
	}*/
	
	/*public void addImage(ImageIcon img, int lr, int x, int y, int w, int  h)
	{
		PaintedImage nimg = new PaintedImage(img, lr, x, y, w, h);
		insertPImage(nimg);
	}*/
	
	/*public int[] getSpriteIndeces()
	{
		int[] x = new int[images.length];
		int j = 0;
		for(int i = 0; i < images.length; i++)
		{
			if(images[i].isSprite())
			{
				x[j] = i;
				j++;
			}
		}
		int[] y = new int[j];
		for(int i = 0; i < j; i++)
		{
			y[i] = x[i];
		}
		return y;
	}*/
	
	public void addSprite(String imgs[], double lr, double x, double y, int w, int h, boolean asActor)
	{
		PaintedSprite nimg = new PaintedSprite(imgs, lr, x, y, w, h);
		//insertPImage(nimg);
		if(asActor)
			plrActor = nimg;
		nameSprite(nimg);
	}
	
	public void addSpriteToTrack(String imgs[], int trackNo, double x, double y, double z, int w, int h, boolean asActor)
	{
		Serializable uo = this.getItemOfType("Track", trackNo);
		Track t = (Track) uo;
		/*double sx = tracks[trackNo].xInit + x;
		double sy = tracks[trackNo].yInit + y;
		double sz = tracks[trackNo].zInit + z;*/
		
		double sx = t.xInit + x;
		double sy = t.yInit + y;
		double sz = t.zInit + z;
		
		PaintedSprite nimg = new PaintedSprite(imgs, sz, sx, sy, w, h);
		nimg.trackNo = trackNo;
		//insertPImage(nimg);
		if(asActor)
			plrActor = nimg;
		nameSprite(nimg);
	}
	
	private void nameSprite(PaintedSprite ps)
	{
		int x = getAllItemsOfType("PaintedSprite").length;
		ps.name = "Sprite[" + (x - 1) + "]";
	}
	
	public void addTileMap(TileMap mp)
	{
		
		//insertPImage(mp);
	}
	
	/*public void addTrack(Track tr)  //deprecated
	{
		//tr.setIndex(tracks.length);
		tr.setIndex(this.countItemOfType("Track"));
		tr.setDioramaRef(this);
		System.out.println("Adding tts:");
		addTrackTransitions(tr);
		
		//objectList.add(tr);
		/*Track[] tracks2 = new Track[tracks.length  + 1];
		for(int i = 0; i < tracks.length; i++)
			tracks2[i] = tracks[i];
		tracks2[tracks.length] = tr;
		tracks = tracks2;
	}*/
	
	public static Image loadImage(String fname)
	{
		ImageIcon ico = new ImageIcon(fname);
		if(ico.getIconWidth() == 0)
			return null;
		return ico.getImage();
	}
	
	public static ImageIcon getImgIcon(String fname)
	{
		ImageIcon ico = new ImageIcon(fname);
		if(ico.getIconWidth() == -1)
			return null;
		else
			return ico;
	}
	
	public static BufferedImage bufferImage(ImageIcon ico)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		
		int w = ico.getIconWidth();
		int h = ico.getIconHeight();
		
		BufferedImage rtnVal = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		
		Graphics2D g2d  = rtnVal.createGraphics();
		g2d.drawImage(ico.getImage(), 0, 0, null);
		
		return rtnVal;
		//Image img = ico.getImage();
		//return new BufferedImage(img);
	}
	
	public static BufferedImage loadPicture(String filename)
	{
		ImageIcon icn = Diorama.getImgIcon(filename);
		return Diorama.bufferImage(icn);
	}
	
	public static Vector arrayInsert(Object[] src, Object[] more, int loc)
	{
		Object[] rtnVal = new Object[src.length + more.length];
		int insertLoc = loc;
		if(loc == -1)
			insertLoc = src.length;
		int j = 0;
		for(int i = 0; i < insertLoc; i++)
		{
			rtnVal[j] = src[i];
			j++;
		}
		for(int i = 0; i < more.length; i++)
		{
			rtnVal[j] = more[i];
			j++;
		}
		for(int i = insertLoc; i < src.length; i++)
		{
			rtnVal[j] = src[i];
			j++;
		}
		Vector v = new Vector();
		for(int i = 0; i < rtnVal.length; i++)
			v.add(rtnVal[i]);
		return v;
	}
	
	public static Vector arrayInsert(Object[] src, Object add, int loc)
	{
		Object[] more = new Object[1];
		more[0] = add;
		return arrayInsert(src, more, loc);
	}
	
	public void moveActor()
	{
		if(moveHoriz != 0)
		{
			Track tr = (Track) this.getItemOfType("Track", plrActor.trackNo);
			//double[] mv = tracks[plrActor.trackNo].incrMoveHoriz(plrActor.xx, plrActor.yy, plrActor.zz, moveHoriz);
			double[] mv = tr.incrMoveHoriz(plrActor.xx, plrActor.yy, plrActor.zz, moveHoriz);
			plrActor.trackNo = (int) mv[0];
			//plrActor.onTrack = tracks[plrActor.trackNo];
			plrActor.onTrack = (Track) this.getItemOfType("Track", plrActor.trackNo);
			plrActor.xx = mv[1];
			plrActor.yy = mv[2];
			plrActor.zz = mv[3];
			System.out.println("Sprite @T" + plrActor.trackNo + " x" + plrActor.xx + " y" + plrActor.yy + " z" + plrActor.zz);
		}
		if(moveVert != 0)
		{
			Track tr = (Track) this.getItemOfType("Track", plrActor.trackNo);
			//double[] mv = tracks[plrActor.trackNo].incrMoveVert(plrActor.xx, plrActor.yy, plrActor.zz, moveVert);
			double[] mv = tr.incrMoveVert(plrActor.xx, plrActor.yy, plrActor.zz, moveVert);
			
			plrActor.trackNo = (int) mv[0];
			//plrActor.onTrack = tracks[plrActor.trackNo];
			plrActor.onTrack = (Track) this.getItemOfType("Track", plrActor.trackNo);
			plrActor.xx = mv[1];
			plrActor.yy = mv[2];
			plrActor.zz = mv[3];
			System.out.println("Sprite @T" + plrActor.trackNo + " x" + plrActor.xx + " y" + plrActor.yy + " z" + plrActor.zz);
		}
		
	}
	
	public void save(String filename)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void saveAsXml(String filename)
	{
		try
		{
			XMLEncoder e = new XMLEncoder(
                    new BufferedOutputStream(
                        new FileOutputStream(filename)));
			e.writeObject(this);
			e.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public Diorama load(String filename)
	{
		try
		{
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			Diorama dia = (Diorama) in.readObject();
			dia.initializer.init();
			for(int i = 0; i < dia.objectList.size(); i++)
				System.out.println(dia.objectList.get(i));
			return dia;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	public Diorama load(String filename, Object obj)
	{
		
		Diorama dia = load(filename);
		dia.initializer.initWithObject(obj);
		return dia;
		
	}
	
	public Diorama load(String filename, Object[] objs)
	{
		Diorama dia = load(filename);
		dia.initializer.initWithObjectList(objs);
		return dia;
	}
	
	public void paintComponent(Graphics g)
	{
		//System.out.println("painting d");
		drawMap(g);
	}
	
	public void updateState()
	{
		Iterator<Updatable> upd = updatables.iterator();
		while(upd.hasNext())
			upd.next().update(this);
	}
	
	private void gameLoop(Graphics g)
	{
		/*for(int i = 0; i < objectList.size(); i++)
			objectList.get(i).processKBInput(0, this);
		for(int i = 0; i < objectList.size(); i++)
			objectList.get(i).processMouseInput(0, 0, 0, this);
		for(int i = 0; i < objectList.size(); i++)
			objectList.get(i).processLogic(this);*/
	}
	
	private void drawMap(Graphics g)
	{
		//1 time bubble sort the images layers into place
		int n = paintables.size();
		Iterator<Drawable> dit = paintables.iterator();
		if(n >= 2)
		{
			Drawable d1 = dit.next();
			Drawable d2 = dit.next();
			for(int i = 1; i < n; i++)
			{
				if(d1.getLayer() > d2.getLayer())
				{
					paintables.set(i - 1, d2);
					paintables.set(i, d1);
				}
				d1 = d2;
				d2 = dit.next();
			}
		}
		dit = paintables.iterator();
		for(int i = 0; i < n; i++)
		{
			Drawable d = dit.next();
			d.draw(g, this);
		}
	}
	
	public void drawMap(Graphics g, int x, int y, int w1, int h1, int w, int h)
	{
		int n = paintables.size();
		Iterator<Drawable> dit = paintables.iterator();
		for(int i = 0; i < n; i++)
		{
			Drawable dr = dit.next();
			g.drawImage(dr.getImg(),
					0, 0, w, h,
					x, y, x+w1, y+h1, this);
		}
	}
	
	private Double getSlope(double[] in1, double[] in2)
	{
		if(in1[0] == in2[0])
			return null;
		else
			return new Double((in2[1] - in1[1]) / (in2[0] - in1[0]));
	}
	
	private String printTp(double[][] tp, int a, int b)
	{
		return tp[a][0] + ", " + tp[a][1] + ", " + tp[b][0] + ", " + tp[b][1];
	}
	
	private Double[] getEdgeLines(double[][] tp)
	{
		Double[] slopes = new Double[4];
		
		slopes[0] = getSlope(tp[0], tp[1]);
		System.out.println("Line " + printTp(tp, 0, 1) + " has slope " + slopes[0]);
		slopes[1] = getSlope(tp[0], tp[2]);
		System.out.println("Line " + printTp(tp, 0, 2) + " has slope " + slopes[1]);
		slopes[2] = getSlope(tp[1], tp[3]);
		System.out.println("Line " + printTp(tp, 1, 3) + " has slope " + slopes[2]);
		slopes[3] = getSlope(tp[2], tp[3]);
		System.out.println("Line " + printTp(tp, 2, 3) + " has slope " + slopes[3]);
		return slopes;
	}
	
	private boolean inBetween(double m, double a, double b)
	{
		double d1 = b - a;
		double d2 = d1 - (b - m);
		if(Math.abs(d2) > Math.abs(d1))
			return false;
		if(Math.abs(d1-d2) > Math.abs(d1))
			return false;
		return true;
	}
	
	//private int threeMedian()
	
	private void inBetweenTest()
	{
		double a,b,m;
		a = 5; b = 10; m = 7;
		if(!inBetween(m,a,b)) System.out.println("wrong");
		a = 5; b = 10; m = 12;
		if(inBetween(m,a,b)) System.out.println("wrong");
		a = 5; b = 0; m = 2;
		if(!inBetween(m,a,b)) System.out.println("wrong");
		a = 5; b = 0; m = 12;
		if(inBetween(m,a,b)) System.out.println("wrong");
		a = 5; b = 0; m = -5;
		if(inBetween(m,a,b)) System.out.println("wrong");
		a = 467.45; b = 445.75; m = 449.47;
		if(!inBetween(m,a,b)) System.out.println("wrong");
	}
	
	private double[] getExtremes(Track t, double[][] tp)
	{
		double quad = Math.ceil(t.rTheta / (Math.PI / 2));
		double[] e = new double[6];
		e[4] = quad;
		e[5] = Math.toDegrees(t.rTheta);
		if(quad == 1)
		{
			e[0] = tp[2][0];
			e[1] = tp[0][1];
			e[2] = tp[1][0];
			e[3] = tp[3][1];
		}
		else if(quad == 2)
		{
			e[0] = tp[3][0];
			e[1] = tp[2][1];
			e[2] = tp[0][0];
			e[3] = tp[1][1];
		}
		else if(quad == -1)
		{
			e[0] = tp[1][0];
			e[1] = tp[3][1];
			e[2] = tp[2][0];
			e[3] = tp[0][1];
		}
		else if(quad == -0)
		{
			e[0] = tp[0][0];
			e[1] = tp[1][1];
			e[2] = tp[3][0];
			e[3] = tp[2][1];
		}
		return e;
	}
	
	private void planeIntersectTest()
	{
		double x1, x2, y1, y2;
		double x3, x4, y3, y4;
		Double s1, s2;
		
		x1=0;y1=0;x2=200;y2=100;
		x3=0;y3=100;x4=100;y4=0;
		
		double[] sa = {x1, y1};
		double[] sb = {x2,y2};
		
		s1 = getSlope(sa, sb);
		
		double[] sc = {x3, y3};
		double[] sd = {x4,y4};
		
		s2 = getSlope(sc, sd);
		
		System.out.println("y3 " + y3 + " y1 " + y1 + " x1 " + x1);
		System.out.println("s1 " + s1 + " s2 " + s2);
		double xp = (y3 - y1) / (s1 - s2) + x1;
		System.out.println("no verticals, is xp " + xp + " inbetween " + x3 + " and " + x4);
		//if this xpoint is on the other line
		if(!inBetween(xp,x3,x4))
			return;
		System.out.println("no verticals, is xp " + xp + " inbetween " + x1 + " and " + x2);
		if(!inBetween(xp,x1,x2))
			return;
		
		int xx = (int) xp;
		
		//System.out.println("m " + slopes1[i] + " * x " + (xp-x1) + " + b " + y1);
		int yy = (int) ((s1 * (xp-x1)) + y1);
		System.out.println("no verticals, cross found @" + xx + "," + yy);
	}
	
	public int[] rotatedPlaneIntersection(Track t1, Track t2)
	{
		//planeIntersectTest();
		//xa,ya,xb,yb is the final bounding box of the intersecting plane
		int xa = Integer.MAX_VALUE;
		int ya = Integer.MAX_VALUE; 
		int xb = -1;
		int yb = -1;
		
		//tp[i] is a corner of the track; 0 is top-left, 1 is top-right, 2 is bottom-left, 3 is bottom-right
		//tp[n][0] is an x value, tp[n][1] is a y value
		double[][] tp = new double[4][];
		System.out.println("Track #" + t1.index);
		tp[0] = t1.getPointRotation(t1.xInit, t1.yInit);
		tp[1] = t1.getPointRotation(t1.xInit + t1.width, t1.yInit);
		tp[2] = t1.getPointRotation(t1.xInit, t1.yInit + t1.height);
		tp[3] = t1.getPointRotation(t1.xInit + t1.width, t1.yInit + t1.height);
		Double[] slopes1 = getEdgeLines(tp);
		
		System.out.println("-----------------");
		
		//see above comment
		double[][] tp2 = new double[4][];
		System.out.println("Track #" + t2.index);
		tp2[0] = t2.getPointRotation(t2.xInit, t2.yInit);
		tp2[1] = t2.getPointRotation(t2.xInit + t2.width, t2.yInit);
		tp2[2] = t2.getPointRotation(t2.xInit, t2.yInit + t2.height);
		tp2[3] = t2.getPointRotation(t2.xInit + t2.width, t2.yInit + t2.height);
		Double[] slopes2 = getEdgeLines(tp2);
		
		//double t1e[] = getExtremes(t1, tp);
		//double t2e[] = getExtremes(t2, tp2);
		
		//xa = (int) Math.max(t1e[0], t2e[0]);
		//xb = (int) Math.min(t1e[2], t2e[2]);
		//ya = (int) Math.max(t1e[1], t2e[1]);
		//yb = (int) Math.min(t1e[3], t2e[3]);
		
		//these are useful for identifying lines
		//use lpts1 for x1,y1 values and lpts2 for x2,y2 values
		int[] lpts1 = {0,0,1,2};
		int[] lpts2 = {1,2,3,3};
		
		Double b1[] = new Double[4];
		for(int i = 0; i < 4; i++)
		{
			if(slopes1[i] != null)
				//b = y1 - x1*m1
				b1[i] = tp[lpts1[i]][1] - (slopes1[i] * tp[lpts1[i]][0]);
			else
				b1[i] = null;
		}
		
		Double b2[] = new Double[4];
		for(int i = 0; i < 4; i++)
		{
			if(slopes2[i] != null)
				//b = y1 - x1*m1
				b2[i] = tp2[lpts1[i]][1] - (slopes2[i] * tp2[lpts1[i]][0]);
			else
				b2[i] = null;
		}
		
		//plane intersection is a 2-step process
		//first check each line to capture intersection points
		//then 
		//first do line checking
		
		for(int i = 0; i < 4; i++)
		{
			double x1 = tp[lpts1[i]][0];
			double x2 = tp[lpts2[i]][0];
			double y1 = tp[lpts1[i]][1];
			double y2 = tp[lpts2[i]][1];
			
			for(int j = 0; j < 4; j++) 
			{
				double x3 = tp2[lpts1[j]][0];
				double x4 = tp2[lpts2[j]][0];
				double y3 = tp2[lpts1[j]][1];
				double y4 = tp2[lpts2[j]][1];
				
				System.out.println("L" + i + " and L" + j + " xranges(" + x1 + " to " + x2 + ") and (" + x3 + "to " + x4 + ")");
				
				
				if(slopes1[i] == null && slopes2[j] == null)
					continue;
				else if(slopes1[i] == null) 
				{
					//l1 is vertical
					//is l1.x in between l2.x1 and l2.x2?
					if(!inBetween(x1, x3, x4))
						continue;
					
					int yy = (int) (slopes2[j] * (x3 - x1) + y3);
					
					//is yy in between l1.y1 and l1.y2? 
					if(!inBetween(yy, y1, y2))
						continue;
					
					//good to go
					int xx = (int) x1;
					//System.out.println("L1 " + i + " L2 " + j + " Point " + xx + ", " + yy + " is within x bounds " + x3 + " to " + x4);
					if(xx < xa)
						xa = xx;
					if(xx > xb)
						xb = xx;
					if(yy < ya)
						ya = yy;
					if(yy > yb)
						yb = yy;
					
					//olap = true;
					
				}
				else if(slopes2[j] == null)
				{
					
					//l2 is vertical
					//is l2.x in between l1.x1 and l1.x2?
					System.out.println("is " + x3 + " between " + x1 + " and " + x2 + "?");
					if(!inBetween(x3, x1, x2))
						continue;
					
					System.out.println("y = m " + slopes1[i] + " * dx " + (x3-x1) + " + b " + y1);
					int yy = (int) (slopes1[i] * (x3 - x1) + y1);
					System.out.println("YY is " + yy);
					System.out.println("is " + yy + " between " + y3 + " and " + y4 + "?");
					//is yy in between l2.y1 and l2.y2?
					//System.out.println("Comparing d(y4-y3) and d(y4-yy):" + Math.abs(y4-y3) +" >= " + Math.abs(y4-yy) + " ?");
					if(!inBetween(yy, y3, y4))
						continue;
					
					//good to go
					int xx = (int) x3;
					System.out.println("Found point intersection:" + xx + "," + yy);
					if(xx < xa)
						xa = xx;
					if(xx > xb)
						xb = xx;
					if(yy < ya)
						ya = yy;
					if(yy > yb)
						yb = yy;
					
					//olap = true;
				}
				else if(slopes2[j].equals(slopes1[i])) //parallel lines - forget it
					continue;
				else
				{
					System.out.println("y3 " + y3 + " y1 " + y1 + " x1 " + x1);
					System.out.println("s1 " + slopes1[i] + " s2 " + slopes2[j]);
					double xp = (b2[j] - b1[i]) / (slopes1[i] - slopes2[j]);
					System.out.println("no verticals, is xp " + xp + " inbetween " + x3 + " and " + x4);
					//if this xpoint is on the other line
					if(!inBetween(xp,x3,x4))
						continue;
					System.out.println("no verticals, is xp " + xp + " inbetween " + x1 + " and " + x2);
					if(!inBetween(xp,x1,x2))
						continue;
					
					int xx = (int) xp;
					
					//System.out.println("m " + slopes1[i] + " * x " + (xp-x1) + " + b " + y1);
					int yy = (int) ((slopes1[i] * (xp-x1)) + y1);
					
					System.out.println("no verticals, cross found @" + xx + "," + yy);
					if(xx < xa)
						xa = xx;
					if(xx > xb)
						xb = xx;
					if(yy < ya)
						ya = yy;
					if(yy > yb)
						yb = yy;
					
					//olap = true;
					//System.out.println("no vertical: x1 " + x1 + " x2 " + x2 + " b2-b1 " + (b2-b1) + " slope diff " + (slopes1[i]-slopes2[j]) + " crossPoint " + crossPoint);
					//double x3 = tp2[lpts1[j]][0];
					//double x4 = tp2[lpts2[j]][0];
					
				}
			}
		}
		
		
		//now do vertex checking
		for(int i = 0; i < 4; i++)
		{
			if(t1.moveOK(tp2[i][0], tp2[i][1]))
			{
				int xx = (int) tp2[i][0];
				int yy = (int) tp2[i][1];
				if(xx < xa)
					xa = xx;
				if(xx > xb)
					xb = xx;
				if(yy < ya)
					ya = yy;
				if(yy > yb)
					yb = yy;
			}
		}
		for(int i = 0; i < 4; i++)
		{
			if(t2.moveOK(tp[i][0], tp[i][1]))
			{
				int xx = (int) tp[i][0];
				int yy = (int) tp[i][1];
				if(xx < xa)
					xa = xx;
				if(xx > xb)
					xb = xx;
				if(yy < ya)
					ya = yy;
				if(yy > yb)
					yb = yy;
			}
		}
		//}
		/*double xMin = 0;
		double xMax = 0;
		double yMin = 0;
		double yMax = 0;*/
		
		
		int[] rtnVal = {xa, xb, ya, yb};
		return rtnVal;
	}
	
	public void addTrackTransitions(Track t)
	{
		Track[] tracks = Track.getTracks(this);
		
		Track t1 = t;
		for(int j = 0; j < tracks.length; j++)
		{
			//find intersection points
			Track t2 = tracks[j];
			if(t1 == t2)
				continue;
			int x1 = -1;
			int x2 = -1;
			int y1 = -1;
			int y2 = -1;
			//Track readX = null;
			//Track readY = null;
			
			System.out.println("T1:" + (int) t1.xInit + "," + (int) t1.yInit + "," + t1.width + "," + t1.height);
			System.out.println("T2:" + (int) t2.xInit + "," + (int) t2.yInit + "," + t2.width + "," + t2.height);
			
			//boolean yAtStart = false;
			if(t1.rTheta == 0 && t2.rTheta == 0) //do it faster
			{
				//get the bounding rect
				if(t2.xInit < t1.xInit)
				{
					
					x1 = (int) t1.xInit;
					x2 = (int) t2.xInit + t2.width;
					if(x2 > t1.xInit + t1.width)
						x2 = (int) t1.xInit + t1.width;
					//readX = t1;
				}
				else if(t2.xInit < t1.xInit + t1.width)
				{
					x1 = (int) t2.xInit;
					x2 = (int) t1.xInit + t1.width;
					if(x2 > t2.xInit + t2.width)
						x2 = (int) t2.xInit + t2.width;
					//readX = t2;
				}
				if(t2.yInit < t1.yInit)
				{
					y1 = (int) t1.yInit;
					y2 = (int) t2.yInit + t2.height;
					if(y2 > t1.yInit + t1.height)
						y2 = (int) t1.yInit + t1.height;
					//readY = t1;
				}
				else if(t2.yInit < t1.yInit + t1.height)
				{
					y1 = (int) t2.yInit;
					y2 = (int) t1.yInit + t1.height;
					if(y2 > t2.yInit + t2.height)
						y2 = (int) t2.yInit + t2.height;
					//readY = t2;
				}
			}
			else
			{
				System.out.println("doing the rotated plane intersect");
				int[] aa = rotatedPlaneIntersection(t1, t2);
				x1 = aa[0];
				x2 = aa[1];
				y1 = aa[2];
				y2 = aa[3];
			}
			
			System.out.println("TT created:" + x1 + "," + y1 + "," + x2 + "," + y2);
			
			
			
			
			if(x1 == -1 || y1 == -1)
				continue;
			if((x2 < x1) || (y2 < y1))
				continue;
			
			
			
			System.out.println("Making tt");
			int[][] field = new int[y2 - y1 + 2][x2 - x1 + 2]; 
			System.out.println("Field size:" + (x2-x1+2) + "," + (y2-y1+2));
			
			/*for(int k = y1 - 1; k < y2 + 1; k++)
			{
				for(int l = x1 - 1; l < x2 + 1; l++)
				{
					int fx = l - x1 + 1;
					int fy = k - y1 + 1;
					//if(cx == -1 || cy == -1)
						//continue;
					//System.out.println("Checking:" + l + "," + k);
					if(t1.moveOK(l, k) && t2.moveOK(l, k))
						//(t2.freeSpace || t2.move[k - (int) t2.yInit][l - (int) t2.xInit] == true))
						field[fy][fx] = -1;
					else if(t1.moveOK(l,k))
						field[fy][fx] = t1.index;
					else if(t2.moveOK(l,k))
						field[fy][fx] = t2.index;
					else
						field[fy][fx] = -2;
					//System.out.println("Wrote:" + field[fy][fx]);
				}
			}*/
			Track[] tks = new Track[2];
			tks[0] = t1; tks[1] = t2;
			//System.out.println("Made tt:" + x1 + "," + y1);
			TrackTransition tt = new TrackTransition(x1 - 1, y1 - 1, (x2 - x1) + 2, (y2 - y1) + 2, tks, true);
			t1.addXfer(tt);
			t2.addXfer(tt);
		}
	}
	
	public void initSize(int x, int y)
	{
		//setPreferredSize(new Dimension(x, y));
		w = x;
		h = y;
	}
	
	public Dimension getPreferredSize()
	{
		recalcSize();
		System.out.println("D size is: " + w + "," + h);
		return new Dimension(w, h);
	}
	
	public void recalcSize()
	{
		Rectangle rect;// = new Rectangle();
		w = 0;
		h = 0;
		for(int i = 0; i < paintables.size(); i++)
		{
			Drawable dr = (Drawable) paintables.get(i);
			rect = dr.getDrawArea();
			if(rect.x + rect.width > w)
				w = rect.x + rect.width;
			if(rect.y + rect.height > h)
				h = rect.y + rect.height;
		}
	}
	
	private void addDrawable(Drawable dr)
	{
		//objectList.add(uo);
		paintables.add(dr);
		Rectangle r = dr.getDrawArea();
		int newW = r.x + r.width;
		int newH = r.y + r.height;
		if(newW > w)
			w = newW;
		if(newH > h)
			h = newH;
		//setPreferredSize(new Dimension(w, h));
	}
	
	private void addInteractive(Interactive in)
	{
		if(in.hasRollover())
		{
			rollovers++;
			interactives.add(0, in);
		}
		else
			interactives.add(in);
	}
	
	public void addExistingObject(Serializable sz)
	{
		if(sz instanceof Drawable)
			addDrawable((Drawable) sz);
		if(sz instanceof Interactive)
			addInteractive((Interactive) sz);
		if(sz instanceof Updatable)
			updatables.add((Updatable) sz);
		objectList.add(sz);
	}
	
	public Object makeAndAddObject(String in, int x1, int y1, int x2, int y2)
	{
		try
		{
			Class cls = Class.forName(in);
			Constructor con = cls.getConstructor(null);
			Serializable obj = (Serializable) con.newInstance(null);
			//Class[] ifaces = cls.getInterfaces();
			if(obj instanceof Placeable)
			{
				Placeable pl = (Placeable) obj;
				pl.gatherObjInfo();
				if(pl.setLoc(x1, y1, x2, y2, this) == false)  //failed to place the object
					return null;
				if(x2 > w)
					w = x2;
				if(y2 > h)
					h = y2;
			}
			addExistingObject(obj);
			return obj;
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	
	public void printObjectList()
	{
		System.out.println("-----Object List------");
		for(int i = 0; i < objectList.size(); i++)
		{
			Object uo = objectList.get(i);
			System.out.println(i + ":" + uo.toString());
		}
		System.out.println("---------------");
	}
}

class DioramaFrame extends JFrame implements ActionListener
{
	int width;
	int height;
	Diorama dia;
	Timer tim;
	
	DioramaFrame(int w, int h, Diorama d)
	{
		super();
		dia = d;
		
		setSize(w, h);
		getContentPane().add(dia);
		tim = new Timer(30, this);
		tim.start();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addKeyListener(dia.new KBInput(dia));
		setVisible(true);
	}
	
	public Diorama getDiorama()
	{
		return dia;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		//move the actor
		dia.updateState();
		//move all other actors
		//draw the map
		//System.out.print("T");
		repaint();
	}
}

class DioramaScreen implements ActionListener
{
	Frame screen;
	Diorama dia;
	Timer tim;
	
	DioramaScreen(int w, int h, Diorama d)
	{
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
   	GraphicsDevice gd = env.getDefaultScreenDevice(); 
   	DisplayMode mode = new DisplayMode(w, h, 32, 0);
   	dia = d;
   	tim = new Timer(30, this);
   	try
		{
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			screen = new Frame(gc);
			screen.setUndecorated(true);
			screen.setIgnoreRepaint(true);
			
			gd.setFullScreenWindow(screen);
			if(gd.isDisplayChangeSupported())
				gd.setDisplayMode(mode);
			Rectangle bounds = screen.getBounds();
			System.out.println("Screen Width:" + bounds.width);
			System.out.println("Screen Height:" + bounds.height);
			screen.createBufferStrategy(2);
			screen.addKeyListener(dia.new KBInput(dia));
			tim.start();
		}
   	catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			gd.setFullScreenWindow(null);
		}
	}
	
	public void redrawScreen()
	{
		BufferStrategy strategy = screen.getBufferStrategy();
		Graphics g = strategy.getDrawGraphics();
		if(!strategy.contentsLost())
		{
			dia.paintComponent(g);
			strategy.show();
			g.dispose();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		//move the actor
		dia.moveActor();
		//move all other actors
		//draw the map
		redrawScreen();
		//System.out.print("T");
	}

	/*@Override
	public void keyPressed(KeyEvent arg0) 
	{
		// TODO Auto-generated method stub
		System.out.println("I pressed:" + arg0.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) 
	{
		// TODO Auto-generated method stub
		System.out.println("I released:" + arg0.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent arg0) 
	{
		System.out.println("I typed:" + arg0.getKeyCode());
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}*/
}

abstract class ButtonAction
{
	int index;
	String name;
	abstract void doAction();
}

class TinyButton extends JButton
{
	Image storedIcon;
	//String storedText;
	
	TinyButton(Image icon)
	{
		storedIcon = icon;
		//int w = icon.getWidth(this);
		//int h = icon.getHeight(this);
		//addNotify();
		//int w = 10;
		//int h = 10;
		//setPreferredSize(new Dimension(w, h));
		//System.out.println("CON:TB Preferred Size is W" + w + " H" + h);
	}
	
	TinyButton(String txt)
	{
		//I'm not using this right now
	}
	
	public void paintComponent(Graphics g)
	{
		//int w = getPreferredSize().width;
		//int h = getPreferredSize().height;
		//System.out.println("TB Preferred Size is W" + w + " H" + h);
		//g.setColor(Color.GRAY);
		//g.fillRect(0, 0, w, h);
		//System.out.println(storedIcon.toString());
		g.drawImage(storedIcon, 1,1, this);
	}
}

interface PropertyGenerator
{
	String[] getPropertyNames();
	String[] getPropertyValues();
	ButtonAction[] getActionNames();
	void setPropertyValues(String[] in);
	//void resize(int dir, int x, int y);
}

interface Selectable
{
	String getItemName();
	//boolean contains(int x, int y);
	//void resize(int dir, int x, int y);
	Rectangle getRect();
	double[] getXform();  //may change these to use affineTransforms 
	double[] getPointRotation(double x, double y);
	double[] invPointRotation(double x, double y);
	void setRect(Rectangle in);
	void setRotation(double[] xForm);
	
	void drawInEditor(Graphics g, Diorama dia);
}

class DefaultSelectable
{
	static double[] getPointRotation(double x, double y, double rTheta, double rox, double roy)
	{
		double[] rtnVal = {x,y};
		if(rTheta != 0)
		{
			AffineTransform xf = AffineTransform.getRotateInstance(rTheta, rox, roy);
			Point2D p = xf.transform(new Point((int) x, (int) y), null);
			rtnVal[0] = p.getX();
			rtnVal[1] = p.getY();
		}
		return rtnVal;
	}
	
	static double[] invPointRotation(double x, double y, double rTheta, double rox, double roy)
	{
		double[] rtnVal = {x,y};
		if(rTheta != 0)
		{
			AffineTransform xf = AffineTransform.getRotateInstance(rTheta * -1, rox, roy);
			Point2D p = xf.transform(new Point((int) x, (int) y), null);
			rtnVal[0] = p.getX();
			rtnVal[1] = p.getY();
		}
		return rtnVal;
	}
	
	static double[] getXform(double rTheta, double rox, double roy)
	{
		double[] rtnVal = {rox, roy, rTheta};
		return rtnVal;
	}
	
	
}

class PropertyEditor extends JPanel implements ActionListener, MouseListener
{
	String[] props;
	JTextField[] vals;
	String title;
	TinyButton cancel;
	TinyButton checkOK;
	JPanel allProps;
	int selItem;
	PropertyGenerator gen;
	String oldValue;
	int txtHeight;
	int txtWidth;
	
	public PropertyEditor()
	{
		ImageIcon ico = Diorama.getImgIcon("bin\\Cancel.gif");
		System.out.println("Icon width: " + ico.getIconWidth());
		Image img = Diorama.loadImage("bin\\Cancel.gif");
		
		Image img2 = Diorama.loadImage("bin\\CheckOK.gif");
		checkOK = new TinyButton(img2);
		checkOK.addActionListener(this);
		cancel = new TinyButton(img);
		cancel.addActionListener(this);
		selItem = -1;
	}
	
	public PropertyEditor(PropertyGenerator pg)
	{
		this();
		gen = pg;
		add(new JPanel());
		refresh(pg);
	}
	
	public PropertyEditor(PropertyGenerator pg, ActionListener al)
	{
		this(pg);
		
		//TODO
		//Note: this may need to be fixed, but for now this portion of the code
		//relies on the *undocumented* behavior that actionListeners are called in
		//reverse order
		checkOK.removeActionListener(this);
		checkOK.addActionListener(al);
		checkOK.addActionListener(this);
	}
	
	public void refresh(PropertyGenerator pg)
	{
		remove(0);
		//int width = 200;
		props = pg.getPropertyNames();
		String[] vs = pg.getPropertyValues();
		if(vs == null)
			return;
		vals = new JTextField[vs.length];
		//int height = 20 * vals.length;
		//setPreferredSize(new Dimension(width, height));
		//setTitle(ttl);
		allProps = new JPanel(new GridLayout(vals.length, 2));
		for(int i = 0; i < vals.length; i++)
		{
			JLabel lbl = new JLabel(props[i]);
			vals[i] = new JTextField(vs[i], 10);
			vals[i].addMouseListener(this);
			vals[i].addActionListener(this);
			vals[i].setEditable(false);
			allProps.add(lbl);
			allProps.add(vals[i]);
			
		}
		add(allProps);
		validate();
		if(vals.length > 0)
		{
			txtHeight = vals[0].getPreferredSize().height;
			txtWidth =  vals[0].getPreferredSize().width;
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		System.out.println("Action performed on PropertyEditor");
		boolean clearPanel = false;
		if(e.getSource() == checkOK || e.getSource() == vals[selItem])
		{
			//reset the properties
			resetValues();
			clearPanel = true;
		}
		else if(e.getSource() == cancel)
		{
			vals[selItem].setText(oldValue);
			clearPanel = true;
		}
		if(clearPanel)
		{
			int compNo = (selItem + 1) * 2 - 1;
			allProps.remove(compNo);
			vals[selItem].setEditable(false);
			allProps.add(vals[selItem], compNo);
			selItem = -1;
			validate();
		}
		
	}
	
	//we need to implement MouseListener for the text components
	public void mousePressed(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		if(selItem != -1)
			return;
		for(int i = 0; i < vals.length; i++)
		{
			if(e.getSource() == vals[i])
			{
				selItem = i;
				oldValue = vals[i].getText();
				GridLayout btnGl = new GridLayout(1, 2);
				btnGl.setHgap(2);
				JPanel pLeft = new JPanel(btnGl);
				pLeft.setPreferredSize(new Dimension(40, txtHeight));
				pLeft.add(cancel);
				pLeft.add(checkOK);
				JPanel pMain = new JPanel(new BorderLayout(2,0));
				pMain.add(vals[i], BorderLayout.CENTER);
				pMain.add(pLeft, BorderLayout.EAST);
				int compNo = (i + 1) * 2 - 1;
				allProps.remove(vals[i]);
				vals[i].setEditable(true);
				int w = vals[i].getPreferredSize().width;
				pMain.setPreferredSize(new Dimension(txtWidth, txtHeight));
				
				allProps.add(pMain, compNo);
				
			}
		}
		//code to validate components
		validate();
		
	}

	
	private void resetValues()
	{
		String[] input = new String[vals.length];
		for(int i = 0; i < vals.length; i++)
		{
			input[i] = vals[i].getText();
		}
		gen.setPropertyValues(input);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

class DrawBox
{
	int x1, y1, x2, y2;
	boolean visible;
	int initX, initY;
	
	DrawBox()
	{
		visible = false;
	}
	
	public void setBoxDims(int x, int y, int xa, int ya)
	{
		x1 = x; y1 = y; x2 = xa; y2 = ya;
		if(x2 < x1)
		{
			int t = x1; x1 = x2; x2 = t;
		}
		if(y2 < y1)
		{
			int t = y1; y1 = y2; y2 = t;
		}
	}
	
	public void setBoxDims(int x, int y)
	{
		setBoxDims(initX, initY, x, y);
	}
	
	public void init(int x, int y)
	{
		initX = x;
		initY = y;
		x1 = x2 = initX;
		y1 = y2 = initY;
	}
	
	public void setVisible(boolean in)
	{
		visible = in;
	}
	
	public void drawBox(Graphics g)
	{
		if(visible)
		{
			g.setColor(new Color(196, 196, 196, 64));
			g.fillRect(x1, y1, x2 - x1, y2 - y1);
		}
	}
}

class SelectionOverlay
{
	Rectangle[] rects;
	//Image[] lrRotation;
	boolean visible;
	//int xa, ya, xb, yb;
	int selIndex;
	Selectable selItem;
	
	SelectionOverlay()
	{
		visible = false;
		selIndex = -1;
		selItem = null;
	}
	
	/*SelectionOverlay(int x1, int y1, int x2, int y2)
	{
		this();
		
		reset(x1, y1, x2, y2);
		
	}*/
	
	public void selectItem(Selectable in)
	{
		selItem = in;
		Rectangle r = in.getRect();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = r.x + r.width;
		int y2 = r.y + r.height;
		
		double[] rot = in.getXform();
		//Automatically setup the rotation hammer if your 
		//rotation has not been set up
		if(rot[0] == 0 && rot[1] == 0 && rot[2] == 0)
		{
			rot[0] = (x2 + x1) / 2;
			rot[1] = y2 - (y2 - y1) / 4;
			selItem.setRotation(rot);
		}
		reset();
	}
	
	public void reset()
	{
		Rectangle r = selItem.getRect();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = r.x + r.width;
		int y2 = r.y + r.height;
		
		double[] rot = selItem.getXform();
		
		rects = new Rectangle[12];
		//selIndex = -1;
		for(int i = 0; i < 12; i++)
		{
			rects[i] = new Rectangle();
			//setup xs
			switch(i)
			{
			case 0: case 1: case 2:
				rects[i].x = x1;
				break;
			case 3: case 4: case 8:  
				rects[i].x = (x2+x1)/2;
				break;
			case 5: case 6: case 7:
				rects[i].x = x2;
				break;
			case 9:
				rects[i].x = (int) rot[0];
				break;
			case 10:
				rects[i].x = rects[9].x - 8;
				break;
			case 11:
				rects[i].x = rects[9].x + 14;
				break;
			}
			
			//setup ys
			switch(i)
			{
			case 0: case 3: case 5:
				rects[i].y = y1;
				break;
			case 1: case 6: case 8:
				rects[i].y = (y2+y1)/2;
				break;
			case 2: case 4: case 7:
				rects[i].y = y2;
				break;
			case 9:
				rects[i].y = y1 + (y2 - (int) rot[1]);
				break;
			case 10:  case 11:
				rects[i].y = y1 + (y2 - (int) rot[1]);
				break;
			}
			
			if(i < 10)
			{
				rects[i].x -= 3;
				rects[i].y -= 3;
				rects[i].width = 6;
				rects[i].height = 6;
			}
			else
			{
				rects[i].x -= 5;
				rects[i].y -= 5;
				rects[i].width = 10;
				rects[i].height = 10;
			}
			
		}
		
			
		if(rot[2] != 0)
		{
			AffineTransform xform = AffineTransform.getRotateInstance(rot[2], rot[0], rot[1]);
			for(int i = 0; i < rects.length; i++)
			{
				Point2D init = new Point(rects[i].x, rects[i].y);
				Point2D dest = xform.transform(init, null);
				rects[i].x = (int) dest.getX();
				rects[i].y = (int) dest.getY();
			}
		}
		
	}
	
	
	
	public void setVisible(boolean in)
	{
		visible = in;
	}
	
	public int getSelectedIndex(int x, int y, boolean reset)
	{
		if(!reset)
		{
			//System.out.println("SI:"+selIndex);
			return selIndex;
		}
		if(selIndex < 0)
			selIndex = getPressedIndex(x, y);
		return selIndex;
	}
	
	/*public void setSelectedItem(Selectable in)
	{
		selItem = in;
	}*/
	
	
	
	public void resizeSelItem(int x, int y)
	{
		if(selItem == null)
			return;
		int dir = selIndex;
		if(dir >= 0)
		{
			if(dir < 9)
			{
				Rectangle r = resize(dir, x, y, selItem.getRect());
				selItem.setRect(r);
				
			}
			else
			{
				selItem.setRotation(adjRotation(x, y, dir, selItem.getXform(), selItem.getRect()));
			}
			reset();
			
			
		}
	}
	
	private Rectangle resize(int dir, int x, int y, Rectangle orig)
	{
		int rx = orig.x;
		int ry = orig.y;
		int rw = orig.width;
		int rh = orig.height;
		// TODO Auto-generated method stub
		switch(dir)
		{
		//process y movement
		case 0: case 3: case 5:
			rh = rh - (y - ry);
			ry = y;
			break;
		case 2: case 4: case 7:
			rh = y - ry;
			break;
		case 8:
			ry = y - (rh/2);
			break;
		}	
		//process x movement
		switch(dir)
		{
		case 0: case 1: case 2:
			rw = rw - (x - rx);
			rx = x;
			break;
		case 5: case 6: case 7:
			rw = x - rx;
			break;
		case 8:
			rx = x - (rw/2);
			break;
		}
		return new Rectangle(rx, ry, rw, rh);
	}
	
	private double[] adjRotation(int x, int y, int dir, double[] rotData, Rectangle orig)
	{
		int rx = orig.x;
		int ry = orig.y;
		int rw = orig.width;
		int rh = orig.height;
		
		double[] rtnVal = {rotData[0], rotData[1], rotData[2]};
		
		switch(dir)
		{
		case 9:  //rotation origin repositioning
			rtnVal[0] = rx + rw - (x - rx);
			rtnVal[1] = ry + rh - (y - ry);
			break;
		case 10:  case 11: //free rotate
			double dx = rotData[0] - x;
			double dy = rotData[1] - y;
			if(dx == 0)
			{
				if(dy >= 0)
					rtnVal[2] = 0;
				else
					rtnVal[2] = Math.PI;
				//rtnVal[2] = 0; 
			}
			else
			{
				if(x < rotData[0])
				//rtnVal[2] = Math.atan(dy/dx) - (Math.PI / 2);
					rtnVal[2] = Math.atan(dy/dx) - (Math.PI / 2);
				else
					rtnVal[2] = Math.atan(dy/dx) + (Math.PI / 2);
				System.out.println("dx = " + dx + " dy = " + dy + " ratio = " + (dy/dx) + "  theta = " + Math.toDegrees(rtnVal[2]));
			}
			break;
		}
		return rtnVal;
	}
	
	private int getPressedIndex(int x, int y)
	{
		System.out.println("SOverlay resetting pressed index");
		if(visible)
		{
			if(rects[8].contains(x,y))
				return 8;
			for(int i = 0; i < rects.length; i++)
			{
				if(rects[i].contains(x,y))
					return i;
			}
		}
		return -1;
			
	/*		if(x >= (xa-3) && x <= (xa+3))
			{
				if(y >= ya-3 && y <= ya+3) return 0;
				if(y >= yb-3 && y <= yb+3) return 2;
				if(y >= ((ya+yb)/2 - 3) && y <= ((ya+yb)/2 + 3)) return 1;
			}
			else if(x >= (xb-3) && x <= (xb+3))
			{
				if(y >= ya-3 && y <= ya+3) return 5;
				if(y >= yb-3 && y <= yb+3) return 7;
				if(y >= ((ya+yb)/2 - 3) && y <= ((ya+yb)/2 + 3)) return 6;
			}
			else if(x >= ((xb+xa)/2 - 3) && x <= ((xb+xa)/2 + 3))
			{
				if(y >= ya-3 && y <= ya+3) return 3;
				if(y >= yb-3 && y <= yb+3) return 4;
				if(y >= ((ya+yb)/2 - 3) && y <= ((ya+yb)/2 + 3)) return 8;
			}
		}
		return -1;*/
	}
	
	public void drawSelOverlay(Graphics g)
	{
		if(!visible)
			return;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color(255,255,0));
		//boolean rotationSet = false;
		AffineTransform orig = null;
		int rx = (int) selItem.getXform()[0];
		int ry = (int) selItem.getXform()[1];
		if(selItem.getXform()[2] != 0)
		{
			double[] rot = selItem.getXform();
			AffineTransform xForm = AffineTransform.getRotateInstance(rot[2], rot[0], rot[1]);
			System.out.println("SO xform: " + rot[2] + "," + rot[0] + "," + rot[1]);
			
			//rx = (int) rot[0];
			//ry = (int) rot[1];
			orig = g2.getTransform();
			g2.transform(xForm);
		}
		Rectangle r = selItem.getRect();
		g2.drawRect(r.x, r.y, r.width, r.height);
		if(orig != null)
			g2.setTransform(orig);
		for(int i = 0; i < rects.length; i++)
			g2.fillRect(rects[i].x, rects[i].y, rects[i].width, rects[i].height);
		 
		int a = rects[9].x + 3;
		int b = rects[9].y + 3;
		//if(orig != null)
			//g2.drawLine(a, b, a, b+12);
		//else
		//{
			g2.drawLine(a, b, rx, ry);
			g2.fillOval(rx - 2, ry - 2, 4, 4);
		//}
		a = rects[10].x + rects[10].width;
		b = rects[10].y + (rects[10].height / 2);
		g2.drawLine(a, b, a+12, b);
	
		g2.setColor(new Color(255,0,255));
		a = rects[10].x + rects[10].width;
		b = rects[10].y + rects[10].height;
		int c = rects[10].y + (rects[10].height / 2);
		
		int[] xps = {a, rects[10].x, a};
		int[] yps = {rects[10].y, c, b};
		g2.fillPolygon(xps, yps, 3);
		
		a = rects[11].x + rects[11].width;
		b = rects[11].y + rects[11].height;
		c = rects[11].y + (rects[11].height / 2);
		
		int[] xps2 = {rects[11].x, a, rects[11].x};
		int[] yps2 = {rects[11].y, c, b};
		g2.fillPolygon(xps2, yps2, 3);
		
		
	}
	
}

class DioramaV extends JPanel implements Scrollable
{
	private DrawBox dbox;
	private Placeable dummyP;
	private Diorama dia;
	private SelectionOverlay selO;
	private int ew;
	private int eh;
	//private boolean drawBlankBox;
	
	DioramaV(DrawBox db, Diorama d, SelectionOverlay so)
	{
		dia = d;
		dbox = db;
		selO = so;
		ew = 0;
		eh = 0;
	}
	
	private void printDiorama(Diorama dx)
	{
		System.out.println("Viewing diorama in editor");
		for(int i = 0; i < dx.objectList.size(); i++)
			System.out.println(dx.objectList.get(i));
	}
	
	public void setDiorama(Diorama d)
	{
		printDiorama(dia);
		printDiorama(d);
		dia = d;
		printDiorama(dia);
		resetSize(d.w, d.h, false);
		revalidate();
		repaint();
		
	}
	
	public void resetSize(int w, int h, boolean hard)
	{
		if(hard || w > ew)
			ew = w;
		if(hard || h > eh)
			eh = h;
		setPreferredSize(new Dimension(ew, eh));
	}
	
	public void paintComponent(Graphics g)
	{
		//System.out.println("in diav paint component, rect is " + ew + ", " + eh);
		//System.out.println("SelO: visible is " + selO.visible);
		//dia.paintComponent(g);
		
		//if(dia.w < ew || dia.h < eh)
		//{
			g.setColor(Color.DARK_GRAY);
			g.drawRect(0, 0, ew, eh);
		//}
		
		int n = dia.objectList.size();
		//dia.printObjectList();
		for(int i = 0; i < n; i++)
		{
			Object obj = dia.objectList.get(i);
			if(obj instanceof Selectable)
			{
				//System.out.println("Painting " + obj.toString());
				Selectable sel = (Selectable) obj;
				sel.drawInEditor(g, dia);
			}
		}
		//if(dummyP != null)
			//dummyP.drawPlacerBox(dbox, dia, g);
		//if(dbox != null)
			//dbox.drawBox(g);
		if(selO != null)
			selO.drawSelOverlay(g);
	}
	
	public Dimension getPreferredSize()
	{
		return dia.getPreferredSize();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() 
	{
		// TODO Auto-generated method stub
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) 
	{
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) 
	{
		// TODO Auto-generated method stub
		return 200;
	}
	
	public void setPlacable(String className)
	{
		try
		{
			Class cls = Class.forName(className);
			Constructor con = cls.getConstructor(null);
			Serializable obj = (Serializable) con.newInstance(null);
			
			if(obj instanceof Placeable)
				dummyP = (Placeable) obj;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	
	/*public void mouseDragged(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}*/	
	
	
	
	
	
	
}

class PropertyWindow extends JFrame
{
	PropertyEditor pe;
	
	PropertyWindow(PropertyGenerator pg)
	{
		layout(pg);
		
		setSize(200, 600);
		setLocation(800, 100);
		setVisible(true);
	}
	
	private void layout(PropertyGenerator pg)
	{
		if(pg != null)
		{
			pe = new PropertyEditor(pg);
			
			getContentPane().add(pe, BorderLayout.CENTER);
		}
	}
	
	public void refresh(PropertyGenerator pg)
	{
		layout(pg);
		validate();
	}
}

class DioramaEditor extends JFrame implements MouseListener, MouseMotionListener, ActionListener
{
	Diorama dia;
	DioramaV diav;
	JScrollPane scrollPane;
	
	int currentAction;
	int selTrack;
	int selImage;
	//PropertyEditor pe;
	//int initX;
	//int initY;
	//int endX;
	//int endY;
	DrawBox dbox;
	SelectionOverlay so;
	PropertyWindow propWin;
	PlacerWindow placeWin;
	Selectable selItem;
	JButton[] modes;
	JPanel all;
	String imgFile;
	JMenuBar topMenu;
	JMenu[] menus;
	String objectToPlace;
	
	//boolean drawBlankBox;
	
	DioramaEditor(Diorama d)
	{
		
		initialize(d);
		
	}
	
	private void resetDiorama(Diorama d)
	{
		
		int nMouseLs = diav.getListeners(MouseListener.class).length;
		int nMMLs = diav.getListeners(MouseMotionListener.class).length;
		System.out.println(nMouseLs + ", " + nMMLs);
		
		dia = d;
		dia.suppressMouse();
		diav.resetSize(800, 600, true);
		
		//diav.removeMouseListener(this);
		//diav.removeMouseMotionListener(this);
		nMouseLs = diav.getListeners(MouseListener.class).length;
		nMMLs = diav.getListeners(MouseMotionListener.class).length;
		System.out.println(nMouseLs + ", " + nMMLs);
		
		diav.setDiorama(dia);
		diav.revalidate();
		//diav.repaint();
		nMouseLs = diav.getListeners(MouseListener.class).length;
		nMMLs = diav.getListeners(MouseMotionListener.class).length;
		System.out.println(nMouseLs + ", " + nMMLs);
		//diav.addMouseListener(this);
		//diav.addMouseMotionListener(this);
		//getContentPane().repaint();
		//validate();
	}
	
	private void initialize(Diorama d)
	{
		//setVisible(false);
		/*if(diav != null)
		{
			
			diav.removeMouseListener(this);
			diav.removeMouseMotionListener(this);
		}*/
		//getContentPane().removeAll();
		//if(diav != null)
			//System.out.println(diav.dia.toString());
		dia = d;
		//d.printObjectList();
		dia.suppressMouse();
		dbox = new DrawBox();
		so = new SelectionOverlay();
		//drawBlankBox = false;
		//System.out.println("PS: W" + dia.w + ", H" + dia.h);
		//int dvw = dia.w;
		/*if(dia.w == 0)
		{
			dia.w = 799;
			//drawBlankBox = true;
		}
		//int dvh = dia.h;
		if(dia.h == 0)
		{
			dia.h = 599;
			//drawBlankBox = true;
		}*/
		
		
		diav = new DioramaV(dbox, d, so);
		diav.resetSize(800, 600, true);
		diav.setDiorama(d);
		//diav.dia.printObjectList();
		//diav.repaint();
		//diav.setPreferredSize(new Dimension(dia.w, dia.h));
		
		//JTextArea test = new JTextArea(40,50);
		//test.setText("Boo\nBoo\nBoo");
		
		scrollPane = new JScrollPane(diav);
		//scrollPane.addMouseListener(this);
		//scrollPane.addMouseMotionListener(this);
		
		int nMouseLs = diav.getListeners(MouseListener.class).length;
		int nMMLs = diav.getListeners(MouseMotionListener.class).length;
		System.out.println(nMouseLs + ", " + nMMLs);
		
		diav.addMouseListener(this);
		diav.addMouseMotionListener(this);
		
		nMouseLs = diav.getListeners(MouseListener.class).length;
		nMMLs = diav.getListeners(MouseMotionListener.class).length;
		System.out.println(nMouseLs + ", " + nMMLs);
		
		//diav.dia.printObjectList();
		topMenu = setupMenus();
		setJMenuBar(topMenu);
		
		
		//getContentPane().add(diav, BorderLayout.CENTER);
		
		
		
		setSize(800, 600);
		//diav.setSize(800, 600);
		setLocation(100, 100);
		getContentPane().add(scrollPane);
		setVisible(true);
		
		if(propWin == null)
			propWin = new PropertyWindow(null);
		
		if(placeWin == null)
			placeWin = new PlacerWindow();
		
		objectToPlace = null;
		/*System.out.println("End of new");
		diav.dia.printObjectList();
		dia.printObjectList();
		System.out.println(diav.dia.toString());*/
		this.addMouseListener(this);
	}
	
	private JPanel setupButtons(String allItems)
	{
		String[] x = allItems.split(",");
		int nItems = x.length + 1;
		JPanel pan = new JPanel(new GridLayout(2, nItems / 2));
		
		for(int i = 0; i < x.length; i++)
		{
			try
			{
				Class cls = Class.forName(x[i]);
				Method m = cls.getMethod("getUIButton", null);
				JButton jb = (JButton) m.invoke(cls, null);
				pan.add(jb);
			}
			catch(Exception ex)
			{
				
			}
		}
		return pan;
		
	}
	
	private JMenuBar setupMenus()
	{
		menus = new JMenu[2];
		//menu 1: File
		menus[0] = new JMenu("File");
		String[] x = {"New", "Save", "Save As XML", "Load"};
		for(int i = 0; i < x.length; i++)
		{
			JMenuItem newFile = new JMenuItem(x[i]);
			menus[0].add(newFile);
			newFile.addActionListener(this);
		}
		menus[1] = new JMenu("Run");
		String[] y = {"Run"};
		for(int i = 0; i < y.length; i++)
		{	
			JMenuItem newFile = new JMenuItem(y[i]);
			menus[1].add(newFile);
			newFile.addActionListener(this);
		}
		JMenuBar rtnVal = new JMenuBar();
		for(int i = 0; i < menus.length; i++)
			rtnVal.add(menus[i]);
		return rtnVal;
	}
	
	private void menuAction(Vector pathTo)
	{
		printV(pathTo);
		int menuAct = 0;
		for(int i = 0; i < pathTo.size(); i+=2)
		{
			menuAct *= 16;
			menuAct +=  ((Integer) pathTo.get(i)).intValue() + 1;
		}
		System.out.println("MA:" + menuAct);
		switch( menuAct)
		{
		case 0x11:  //new
			askForSave("creating a new file");
			resetDiorama(new Diorama());
			break;
		case 0x12:  //save
			saveDialog(false);
			break;
		case 0x13:  //save
			saveDialog(true);
			break;
		case 0x14:  //load
			askForSave("loading a file");
			String path = loadDialog();
			if(path != null)
			{
				dia = dia.load(path);
				resetDiorama(dia);
			}
			break;
		case 0x21: //run
			askForSave("running");
			Diorama diax = dia;
			if(dia.filename != null)
				diax = dia.load(dia.filename);
			DioramaFrame drmf = new DioramaFrame(800, 800, diax);
			
			
		}
	}
	
	private void saveDialog(boolean asXml)
	{
		if(dia.filename == null)
		{
			JFileChooser jfc = new JFileChooser();
			int code = jfc.showSaveDialog(this);
			if(code == JFileChooser.APPROVE_OPTION)
			{
				File fl = jfc.getSelectedFile();
				dia.filename = fl.getName();
			}
			else
				return;
		}
		dia.save(dia.filename);
	}
	
	private String loadDialog()
	{
		JFileChooser jfc = new JFileChooser();
		int code = jfc.showOpenDialog(this);
		if(code == JFileChooser.APPROVE_OPTION)
		{
			File fl = jfc.getSelectedFile();
			return fl.getAbsolutePath();
		}
		else
			return null;
	}
	
	private void askForSave(String nextAction)
	{
		Object opts[] = {"Yes", "No", "Cancel"};
		int choice = JOptionPane.showOptionDialog(this, "Save your work before " + nextAction + "?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
		if(choice == 2)
			return;
		if(choice == 0)
		{
			saveDialog(false);
		}
	}
	
	private void printV(Vector v)
	{
		for(int i = 0; i < v.size(); i++)
			System.out.print(v.get(i) + ",");
		System.out.println("");
	}
	
	private int getMenuSelection(Vector v, MenuElement[] ms, ActionEvent e)
	{
		System.out.println("MSLen:" + ms.length);
		for(int i = 0; i < ms.length; i++)
		{
			MenuElement m = ms[i];
			v.add(new Integer(i));
			//printV(v);
			//System.out.println(m);
			//System.out.println(e.getSource());
			if(e.getSource() == m)
			{
				//System.out.println("Match");
				return 1;
			}
			else if(m.getSubElements().length > 0)
			{
				//System.out.println("stepping into menu");
				if(getMenuSelection(v, m.getSubElements(), e) == 1)
					return 1;
			}
			v.remove(v.lastElement());
		}
		return 0;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		System.out.println(e.getSource());
		Vector lst = new Vector();
		if(getMenuSelection(lst, topMenu.getSubElements(), e) == 1)
		{
			menuAction(lst);
			return;
		}
		for(int i = 0; i < modes.length; i++)
		{
			if(modes[i] == e.getSource())
			{
				if(currentAction != i)
				{
					modes[i].setSelected(true);
					//modes[i].setBackground(new Color(0,255,0));
					//modes[currentAction].setForeground(Color.BLACK);
					//modes[currentAction].setBackground(Color.BLACK);
					modes[currentAction].setSelected(false);
				}
				System.out.println("Action is " + i);
				currentAction = i;
				modes[i].getModel().setPressed(true);
			}
			else
				modes[i].getModel().setPressed(false);
		}
	}

	public void mouseDragged(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		System.out.println("Dragged mouse");
		System.out.println("SI:" + so.selIndex);
		System.out.println(e.getX() + "," + e.getY());
		dbox.setBoxDims(e.getX(), e.getY());
		
		if(so.getSelectedIndex(e.getX(), e.getY(), false) > -1)
			so.resizeSelItem(e.getX(), e.getY());
		repaint();
		//System.out.println("SI:" + so.selIndex);
	}

	public void mouseMoved(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		//if(currentAction == 0)
			//getTrack(e.getX(), e.getY());
	}

	public void mouseEntered(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource() == this)
		{
			System.out.println("FUBAR!");
			return;
		}
		System.out.println("Pressed at (" + e.getX() + "," + e.getY() + ")");
		int x = e.getX();
		int y = e.getY();
		
		if(so.getSelectedIndex(x, y, true) >= 0)
		{
			return;
		}
		else
		{
			int initX = e.getX();
			int initY = e.getY();
			dbox.init(initX, initY);
			dbox.setVisible(true);
		}
		repaint();
	}
	
	private boolean rotBounds(DrawBox in, Selectable sel)
	{
		Rectangle r = sel.getRect();
		
		double[] pt1 = sel.getPointRotation(r.x, r.y);
		double[] pt2 = sel.getPointRotation(r.x + r.width, r.y);
		double[] pt3 = sel.getPointRotation(r.x, r.y + r.height);
		double[] pt4 = sel.getPointRotation(r.x + r.width, r.y + r.height);
		
		double[] points1 = {pt1[0], pt2[0], pt3[0], pt4[0]};
		if(lineTest(in.x1, in.x2, points1) == false)
		{
			//System.out.println("failed line test 1");
			return false;
		}
		double[] points2 = {pt1[1], pt2[1], pt3[1], pt4[1]};
		if(lineTest(in.y1, in.y2, points2) == false)
		{
			//System.out.println("failed line test 2");
			return false;
		}
		pt1 = sel.invPointRotation(in.x1, in.y1);
		pt2 = sel.invPointRotation(in.x2, in.y1);
		pt3 = sel.invPointRotation(in.x1, in.y2);
		pt4 = sel.invPointRotation(in.x2, in.y2);
		
		double[] points3 = {pt1[0], pt2[0], pt3[0], pt4[0]};
		if(lineTest(r.x, r.x + r.width, points3) == false)
		{
			//System.out.println("failed line test 3");
			return false;
		}
		double[] points4 = {pt1[1], pt2[1], pt3[1], pt4[1]};
		if(lineTest(r.y, r.y + r.height, points4) == false)
		{
			//System.out.println("failed line test 4");
			return false;
		}
		return true;
	}
	
	private boolean lineTest(int a, int b, double[] points)
	{
		/*System.out.print(a + ", " + b + ": ");
		for(int i = 0; i < points.length; i++)
			System.out.print((int) points[i] + ", ");
		System.out.println("");*/
		double min = points[0];
		double max = points[0];
		for(int i = 0; i < points.length; i++)
		{
			if(points[i] > a && points[i] < b)
				return true;
			if(points[i] > max)
				max = points[i];
			if(points[i] < min)
				min = points[i];
		}
		if(a > min && a < max)
			return true;
		if(b > min && b < max)
			return true;
		return false;
	}
	
	public int[] getObjects(DrawBox in)
	{
		Vector<Integer> sels = new Vector<Integer>();
		Rectangle r2 = new Rectangle(in.x1, in.y1, (in.x2 - in.x1), (in.y2 - in.y1));
		r2.grow(1, 1);
		//System.out.println("IN size:" + r2.width + "," + r2.height);
		int x = dia.objectList.size();
		for(int i = 0; i < x; i++)
		{
			Object o = dia.objectList.get(i);
			if(o instanceof Selectable)
			{
				Selectable sel = (Selectable) o;
				if(sel.getXform()[2] == 0)
				{
					Rectangle rect = sel.getRect();
					//System.out.println(o.toString() + rect.toString());
					if(rect.intersects(r2))
						sels.add(new Integer(i));
				}
				else
				{
					if(rotBounds(in, sel) == true)
						sels.add(new Integer(i));
				}
			}
		}
		int[] rtnVal = new int[sels.size()];
		for(int i = 0; i < sels.size(); i++)
		{
			Integer val = sels.get(i);
			rtnVal[i] = val.intValue();
		}
		return rtnVal;
	}
	
	private int getTopSelectedObject(DrawBox in)
	{
		int[] as = getObjects(in);
		if(as.length == 0)
			return -1;
		return as[as.length - 1];
	}

	public void mouseReleased(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		dbox.setVisible(false);
		System.out.println("released mouse with action #" + currentAction);
		System.out.println("Current action is " + placeWin.getActiveOption());
		System.out.println("dbox dims are " + dbox.x1 + "," + dbox.y1 + "," + dbox.x2 + "," + dbox.y2) ;
		String myOpt = placeWin.getActiveOption();
		
		if(myOpt == null)
			return;
		
		if(myOpt.equals("-1"))
		{
			//int[] selections = getObjects(dbox);
			int sel = getTopSelectedObject(dbox);
			if(sel == -1)
				return;
			//System.out.println("You selected " + selections.length + " items.");
			//int[] selections = getImage(e.getX(), e.getY(), (currentAction == 1));
			//if(selections.length == 1)
			//{
				Object uo = dia.objectList.get(sel);
				if(uo instanceof Selectable)
				{
					Selectable selc = (Selectable) uo;
					so.selectItem(selc);
					so.setVisible(true);
				}
				if(uo instanceof PropertyGenerator)
				{
					PropertyGenerator pg = (PropertyGenerator) uo;
					propWin.refresh(pg);
				}
				/*Track t = dia.tracks[selections[0]];
				//so.reset((int) t.xInit, (int) t.yInit, (int) t.xInit + t.width, (int) t.yInit + t.height);
				so.selectItem(t);
				so.setVisible(true);
				propWin.refresh(t);*/
				System.out.println("Now painting the diav");
				System.out.println("SO visible is " + so.visible);
				repaint();
				//validate();
				System.out.println("Finished repainting the diav");
			//}
		}
		else
		{
			Object obj = dia.makeAndAddObject(myOpt, dbox.x1, dbox.y1, dbox.x2, dbox.y2);
			
			if(obj != null && obj instanceof Selectable)
			{		
				//System.out.println("repainting the diav now");
				repaint();
				
				//validate();
			}
		}

		//clear the selected object's selected index
		so.selIndex = -1;
	}
	
	public ImageIcon[] loadSpriteFrames(ImageIcon img)
	{
		ImageIcon[] imgs = new ImageIcon[1];
		imgs[0] = img;
		return imgs;
	}
		
}

interface Placeable
{
	public boolean setLoc(int x1, int y1, int x2, int y2, Diorama dia);
	//public void drawPlacerBox(DrawBox bx, Diorama dia, Graphics g);
	public void gatherObjInfo();
}

class DefaultPlaceable
{
	public static void drawPlacerBox(DrawBox bx, Diorama dia, Graphics g)
	{
		bx.drawBox(g);
	}
}

interface Drawable
{
	public void draw(Graphics g, Diorama d);
	public double getLayer();
	public BufferedImage getImg();
	public Rectangle getDrawArea();
}

interface Interactive
{
	public IOHandler[] getIOs();
	public boolean containsPoint(int x, int y);
	public boolean hasRollover();
	public void addHandler(IOHandler io);
	public void removeHandler(IOHandler io);
	public boolean hasHandler(IOHandler io);
}

interface Updatable
{
	public void update(Diorama dia);
}

class PictureLoader extends JFrame implements ActionListener
{
	//filer out *.gif, *.jpeg
	//JFileChooser jfc;
	//JButton okBtn;
	//JButton cancelBtn;
	boolean asSprite;
	String imgFile;
	
	PictureLoader(boolean asSprite)
	{
		this.asSprite = asSprite;
		FileChooserDemo2 jfc = new FileChooserDemo2(this);
	}
	
	private void passFile(File f)
	{
		//File f = jfc.getSelectedFile();
		String s = f.getAbsolutePath();
	   imgFile = s;
		//de.addImage(asSprite);
		
	}
	
	public String getImgFile()
	{
		return imgFile;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		/*if(e.getSource() == )
		{
			File f = jfc.getSelectedFile();
			String s = f.getAbsolutePath() + f.getName();
			de.imgFile = s;
			de.addImage(asSprite);
		}
		else
		{
			de.imgFile = null;
		}
		setVisible(false);*/
	}
	
	public void close(File selectedFile)
	{
		//setVisible(false);
		if(selectedFile != null)
		{
			passFile(selectedFile);
		}
		else
		{
			imgFile = null;
		}
	}
}

public class DioramaDemo
{
	public static void main(String args[])
	{
		Diorama d = getTestDiorama();
		//Image[] spr = new Image[]
		
		/*for(int i = 0; i < 100; i++)
		{
			double randX = Math.random() * 1000;
			double randY = Math.random() * 300;
			double randZ = Math.random() * 2 + 0.5;
			d.addSprite(imgs, randZ, randX, randY, 64, 44, true);
		}*/
		
		//DioramaFrame df = new DioramaFrame(1024, 768, d);
		//DioramaScreen ds = new DioramaScreen(1280, 800, d);
		//verifyClasses("GroupList.xml", "GroupXSD.xsd");
		DioramaEditor de = new DioramaEditor(d);
		de.placeWin.receiveConfig("GroupList.xml", "GroupXSD.xsd");
	}
	
	public static Diorama getTestDiorama()
	{
		Diorama d = new Diorama();
		String[] files = new String[1];
		ImageIcon[] imgs = new ImageIcon[1];
		PaintedImage pi = new PaintedImage();
		pi.fromFile = "TestBkg1.jpg";
		pi.reload();
		pi.setLoc(0, 0, 1280, 1000, d);
		pi.layer = -1;
		
		files[0] = "PWSprite1.jpg";
		
		d.addExistingObject(pi);
		
		Track t1 = (Track) d.makeAndAddObject("Track", 200, 500, 700, 700);
		t1.setInit(200.0, 500.0, 0.5);
		t1.setHorizMovement(2.0, 0.0, 0.0);
		t1.setVertMovement(0.0, 2.0, (2.0 / 150.0));
		
		
		
		Track t2 = (Track) d.makeAndAddObject("Track", 150, 200, 200, 500);
		t2.setInit(150.0, 200.0, 0.5);
		t2.setHorizMovement(0.0, 0.0, 0.0);
		t2.setVertMovement(0.5, 3.0, 0.0);
		
		
		TrackExit e1a = new TrackExit(t1, t2, 0, 0, 199.5, 497.0, 0.5, 0);
		TrackExit e2a = new TrackExit(t2, t1, 49, 297, 200.0, 500.0, 0.5, 0);
		t1.addXfer(e1a);
		t2.addXfer(e2a);
		
		d.printObjectList();
		
		
		
		d.addSpriteToTrack(files, 0, 50, 0, 0, 64, 44, true);
		d.printObjectList();
		return d;
	}
	
	public static void verifyClasses(String xmlFile, String schemaFile)
	{
		Vector<String> failures = new Vector<String>();
		//load the xml file
		XmlProcessor xp = new XmlProcessor(xmlFile, schemaFile);
		xp.initXml();
		//validate the xml
		if(xp.xml == null)
		{
			System.out.println("Cannot verify classes due to xml validation failure.");
			return;
		}
		//foreach group
		Element groupElement = xp.getTopElement();
		Element[] allGroups = xp.getSubElements(groupElement);
		
		for(int i = 0; i < allGroups.length; i++)
		{
			//foreach oper
			System.out.println("  " + xp.getAttribute(allGroups[i], "name"));
			Element[] allOps = xp.getSubElements(allGroups[i]);
			for(int j = 0; j < allOps.length; j++)
			{
				//try to instantiate the base class
				String baseClass = xp.getAttribute(allOps[j], "baseclass");
				Object obj = testObject(baseClass);
				if(obj == null)
					failures.add(baseClass);
				//foreach option
				Element[] allOptions = xp.getSubElements(allOps[j]);
				for(int k = 0; k < allOptions.length; k++)
				{
					String myClass = xp.getAttribute(allOptions[k], "classname");
					Object obj2 = testObject(myClass);
					if(obj2 == null)
						failures.add(myClass);
				}
			}
		}
		//try to instantiate the class
		//if failure, add the class to the list of failures
		int fails = failures.size();
		for(int i = 0; i < fails; i++)
		{
			String badClass = failures.get(i);
			System.out.println("Failed to instantiate a " + badClass);
		}
	}
	
	public static Object testObject(String objName)
	{
		try
		{
			Class cls = Class.forName(objName);
			Constructor con = cls.getConstructor(null);
			Object obj = (Object) con.newInstance(null);
			return obj;
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
			System.out.println(ex.getMessage());
			
			return ex;
		}
	}
}

