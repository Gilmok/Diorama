import java.awt.*;
import javax.swing.*;
import java.util.*;

class TerrainSquare
{
	int elev;
	int trackNo;
	
	TerrainSquare(int e, int t)
	{
		elev = e;
		trackNo = t;
	}
}

class TerrainMap 
{
	TerrainSquare[][] map;
	int width;
	int height;
	LinkedList tks;
	
	TerrainMap(int w, int h)
	{
		width = w;
		height = h;
		map = new TerrainSquare[w][h];
		for(int i = 0; i < w; i++)
		{
			for(int j = 0; j < h; j++)
			{
				map[i][j] = new TerrainSquare(0,0);
			}
		}	
		tks = new LinkedList();
	}
	
	void elevate(int x, int y, int n)
	{
		//elevate the square
		map[x][y].elev += n;
		int newTrack = tks.size();
		//map[x][y].trackNo = newTrack;
		boolean addedTrack = true;
		//elevate the surrounding cross
		if(x > 0)
		{
			if(map[x-1][y].elev + 1 < map[x][y].elev)
				elevate(x-1, y, 1);
			if(Math.abs(map[x-1][y].elev - map[x][y].elev) == 0)
			{
				map[x][y].trackNo = map[x-1][y].trackNo;
				addedTrack = false;
			}
			
		}
		if(x < width - 1)
		{
			if(map[x+1][y].elev + 1 < map[x][y].elev)
				elevate(x+1, y, 1);
			else if(Math.abs(map[x+1][y].elev - map[x][y].elev) == 0)
			{
				map[x][y].trackNo = map[x+1][y].trackNo;
				addedTrack = false;
			}
			
		}
		if(y > 0)
		{
			if(map[x][y-1].elev + 1 < map[x][y].elev)
				elevate(x, y-1 , 1);
			else if(Math.abs(map[x][y-1].elev - map[x][y].elev) == 0)
			{
				map[x][y].trackNo = map[x][y-1].trackNo;
				addedTrack = false;
			}
			
		}
		if(y < height - 1)
		{
			if(map[x][y+1].elev + 1 < map[x][y].elev)
				elevate(x,y+1, 1);
			else if(Math.abs(map[x][y+1].elev - map[x][y].elev) == 0)
			{
				map[x][y].trackNo = map[x][y+1].trackNo;
				addedTrack = false;
			}
			
		}
		//add a track if necessary
		if(addedTrack)
		{
			map[x][y].trackNo = newTrack;
			addTrack(x, y);
		}
		//add track connections to this new track at the "cross"
		/*if(x > 0)
		{
			if(Math.abs(map[x-1][y].elev - map[x][y].elev) == 1)
				addTrackConnection(x-1, y, map[x-1][y].trackNo, map[x][y].trackNo);
		}
		if(x < width - 1)
		{
			if(Math.abs(map[x+1][y].elev - map[x][y].elev) == 1)
				addTrackConnection(x+1, y, map[x+1][y].trackNo, map[x][y].trackNo);
		}	
		if(y > 0)
		{
			if(Math.abs(map[x][y-1].elev - map[x][y].elev) == 1)
				addTrackConnection(x, y-1, map[x][y-1].trackNo, map[x][y].trackNo);
		}
		if(y < height - 1)
		{
			if(Math.abs(map[x][y+1].elev - map[x][y].elev) == 1)
				addTrackConnection(x, y+1, map[x][y+1].trackNo, map[x][y].trackNo);
		}*/
	}
	
	void addTrack(int x, int y)
	{
		//add the track to the list
		Track t = new Track(1, 1);
		t.setIndex(tks.size());
		t.setHorizMovement(1,1,0);
		t.setVertMovement(1,1,0);
		t.setInit(x, y, 0);
		tks.add(t);
	}
	
	void addTrackConnection(int x, int y, int t1, Track t2)
	{
		Track ta = (Track) tks.get(t1);
		//Track tb = (Track) tks.get(t2);
		TrackExit te = new TrackExit(ta, t2, x, y, 0, 0, 0, 0);
		ta.addXfer(te);
	}
	
	//adjust track waypoints
	void adjTrackWaypoints(int x, int y, int newTrack)
	{
		//for each object with this as a waypoint
		//change the track to this new track
	}
	
	void drawMap(Graphics g)
	{
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				int e = map[i][j].elev;
				g.setColor(new Color(32 * e, 32 * e, 40));
				g.fillRect(i * 24, j * 24, 24, 24);
				
				int t = map[i][j].trackNo;
				g.setColor(new Color((t % 2) * 192, (t % 4) * 64, (t % 8) * 32));
				g.drawString("E:" + map[i][j].elev, i * 24 + 5, j * 24 + 10);
				g.drawString("T:" + map[i][j].trackNo, i * 24 + 5, j * 24 + 20);
			}
		}
	}
}

class TerrainMapViewer extends JPanel
{
	TerrainMap tm;
	
	TerrainMapViewer(TerrainMap in)
	{
		tm = in;
	}
	
	public void paintComponent(Graphics g)
	{
		tm.drawMap(g);
	}
}

class TrackViewer extends JFrame
{
	TerrainMap tm;
	JTextArea ta;
	
	TrackViewer(TerrainMap in)
	{
		tm = in;
		ta = new JTextArea();
		getContentPane().add(ta);
		setLocation(700, 50);
		setSize(100, 400);
		setVisible(true);
	}
	
	void update()
	{
		String str = "";
		for(int i = 0; i < tm.tks.size(); i++)
		{
			Track t = (Track) tm.tks.get(i);
			/*for(int j = 0; j < t.exits.length; j++)
			{
				TrackExit ti = t.exits[j];
				str += "Exit from track " + t.index + " to " + ti.toTrack + "\n";
			}*/
		}
		ta.setText(str);
	}
}

class TerrainEditor extends JFrame
{
	TerrainMapViewer tmv;
	
	TerrainEditor(TerrainMap in)
	{
		tmv = new TerrainMapViewer(in);
		setSize(600,500);
		getContentPane().add(tmv, BorderLayout.CENTER);
		setVisible(true);
	}
	
	void elevate(int x, int y)
	{
		tmv.tm.elevate(x, y, 1);
		repaint();
	}
}

public class TerrainEditorDemo
{
	public static void main(String args[])
	{
		TerrainMap mp = new TerrainMap(100,80);
		TerrainEditor te = new TerrainEditor(mp);
		mp.addTrack(0,0);
		te.elevate(5,5);
		te.elevate(5,5);
		/*te.elevate(5,5);
		te.elevate(5,6);
		te.elevate(5,7);
		te.elevate(6,5);*/
		TrackViewer tv = new TrackViewer(mp);
		tv.update();
	}
}



