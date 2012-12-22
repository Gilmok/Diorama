import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



class TileMap extends PaintedImage
{
	int[][] map;
	Tile[] tiles;
	boolean xWrapped;
	boolean yWrapped;
	int tileSizeX;
	int tileSizeY;
	double parallax;
	double scale;
	int scaleX;
	int scaleY;
	int nSides;
	//Waypoint[] perTile;
	double rTheta;
	
	class Tile
	{
		PaintedSprite img;
		double rTheta;
		Waypoint[] points;
		LinearTrack[] arcs;
		int nSides;
		
		
		Tile()
		{
			img = null;
			rTheta = 0;
			this.nSides = 4;
			points = new Waypoint[0];
			arcs = new LinearTrack[0];
		}
		
		public void setupTileInfo(Diorama dia)
		{
			points = (Waypoint[]) dia.getAllItemsOfType("Waypoint");
			arcs = (LinearTrack[]) dia.getAllItemsOfType("LinearTrack");
			System.out.println("Saved " + points.length + " waypoint and " + arcs.length + " arcs");
			PaintedRegularShape ps = (PaintedRegularShape) dia.getItemOfType("PaintedRegularShape", 0);
			rTheta = ps.rTheta;
		}
		
		public void setupImage(PaintedSprite sp, int ns)
		{
			img = sp;
			nSides = ns;
		}
		
		public void draw(Graphics2D g2d, Diorama dia, int x, int y, int cx, int cy)
		{
			
		}
		
		public void drawInEditor(Graphics2D g2d, Diorama dia)
		{
			
		}
	}
	
	TileMap()
	{
		map = new int[100][100];
		tileSizeX = tileSizeY = 64;
		tiles = new Tile[0];
		scale = 1.0;
		nSides = 4;
		//perTile = new Waypoint[0];
	}
	
	TileMap(int w, int h)
	{
		this();
		map = new int[h][w];
		resetTiles();
	}
	
	public void setMapSize(int w, int h)
	{
		map = new int[h][w];
		resetTiles();
	}
	
	public void setTile(int x, int y, int n)
	{
		map[y][x] = n;
	}
	
	public void setTileAtLoc(int x, int y, int n)
	{
		//System.out.println("Setting value at " + y/scaleY + "," + x/scaleX);
		map[y / scaleY][x / scaleX] = n;
	}
	
	public void fillTilesBox(int x, int y, int x2, int y2, int n)
	{
		for(int yy = y; yy < y2; yy += scaleY)
		{
			for(int xx = x; xx < x2; xx += scaleX)
			{
				setTileAtLoc(xx, yy, n);
			}
		}
	}
	
	public void drawTilesBox(int x, int y, int x2, int y2, int n)
	{
		for(int yy = y; yy < y2; yy += scaleY)
		{
			setTileAtLoc(x, yy, n);
			setTileAtLoc(x2, yy, n);
		}
		for(int xx = x; xx < x2; xx += scaleX)
		{
			setTileAtLoc(xx, y, n);
			setTileAtLoc(xx, y2, n);
		}
	}
	
	public void setAllTiles(int n)
	{
		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[i].length; j++)
				map[i][j] = n;
	}
	
	private void floodTiles(int x, int y, int n, int old)
	{
		map[y][x] = n;
		if(y > 0 && map[y - 1][x] == old)
			floodTiles(x, y - 1, n, old);
		if(y < (map.length - 1) && map[y + 1][x] == old)
			floodTiles(x, y + 1, n, old);
		if(x < (map[y].length - 1) && map[y][x + 1] == old)
			floodTiles(x + 1, y, n, old);
		if(x > 0 && map[y][x - 1] == old)
			floodTiles(x - 1, y, n, old);
	}
	
	public void floodTilesAtLoc(int x, int y, int n)
	{
		int sx = x / scaleX;
		int sy = y / scaleY;
		int a = map[sy][sx];
		
		if(a != n)
			floodTiles(sx, sy, n, a);
	}
	
	public void resetTiles()
	{
		setAllTiles(0);
	}
	
	public void printTiles()
	{
		for(int i = 0; i < map.length; i++)
		{
			for(int j = 0; j < map[i].length; j++)
				System.out.print(map[i][j] + "|");
			System.out.println();
		}
	}
	
	void addTiles(String[] in)
	{
		//PaintedSprite[] ps1 = new PaintedSprite[tiles.length + in.length];
		Tile[] allTiles = new Tile[tiles.length + in.length];
		for(int i = 0; i < tiles.length; i++)
			allTiles[i] = tiles[i];
		for(int i = 0; i < in.length; i++)
		{
			String[] arr = new String[1];
			ImageIcon ico = Diorama.getImgIcon(in[i]);
			arr[0] = in[i];
			PaintedSprite ps = new PaintedSprite(arr, 1, 0, 0, ico.getIconWidth(), ico.getIconHeight());
			
			allTiles[i + tiles.length] = new Tile();
			allTiles[i + tiles.length].setupImage(ps, 4);
			tileSizeX = ico.getIconWidth();
			tileSizeY = ico.getIconHeight();
			System.out.println("Loaded tile " + in[i] + " with width " + ico.getIconWidth() + " and height " + ico.getIconHeight());
		}
		tiles = allTiles;
		setScale(1.0);
	}
	
	public void setScale(double input)
	{
		scale = input;
		scaleX = (int) (tileSizeX * scale);
		scaleY = (int) (tileSizeY * scale);
	}
	
	public Rectangle getDrawArea()
	{
		int mx = 0;
		int my = scaleY * map.length;
		for(int i = 0; i < map.length; i++)
		{
			if(map[i].length > mx)
				mx = map[i].length;
		}
		mx *= scaleX;
		
		Rectangle rex = new Rectangle(x,y,mx,my);
		//System.out.println("TileMap drawArea = " + rex.toString());
		return rex;
	}
	
	private void drawArcs(Graphics g, Diorama dia)
	{
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform oldXform = g2d.getTransform();
		if(scale == 1.0)
		{
			//boolean[] excludeMe = new boolean[20];
			for(int i = 0; i < map.length; i++)
			{
				for(int j = 0; j < map[i].length; j++)
				{
					Tile t = tiles[map[i][j]];
					int n = t.nSides * 2;
					//draw tile waypoints
					g2d.setTransform(AffineTransform.getTranslateInstance(tileSizeX * j, tileSizeY * i));
					for(int k = n; k < t.points.length; k++)
					{
						
						t.points[k].drawInEditor(g, dia);
					}
					//draw arcs in nSides * 2 directions
					//int[] excludeMe = new int[n];
					
					//find points to exclude
					/*if(i == 0)
					{
						switch(n)
						{
						case 12: //6 sides
							excludeMe[11] = true;
							excludeMe[12] = true;
						case 8:  //4 sides
							excludeMe[1] = true;
							excludeMe[2] = true;
						case 6:  //3 sides
							excludeMe[0] = true;
							break;
							
						}
					}
					if(j == 0)
					{
						switch(n)
						{
						case 6:
							excludeMe[1] = true;
							excludeMe[2] = true;
							break;
						case 8: //4 sides
							excludeMe[0] = true;
							excludeMe[3] = true;
							excludeMe[5] = true;
							break;
						case 12: //6 sides
							for(int l = 7; l < 11; l++)
								excludeMe[l] = true;
							break;
						}
					}*/
					
					for(int k = 0; k < t.arcs.length; k++)
					{
						t.arcs[k].drawInEditor(g, dia);
					}
					g2d.setTransform(oldXform);
				}
					//g.drawImage(tiles[map[i][j]].img.getImg(), tileSizeX * j, tileSizeY * i, dia);
			}
		}
	}
	
	public void drawInEditor(Graphics g, Diorama dia)
	{
		draw(g, dia);
		
	}
	
	public void draw(Graphics g, Diorama dia)
	{
		//System.out.println("ScaleX " + scaleX + " ScaleY " + scaleY);
		if(scale == 1.0)
		{
			for(int i = 0; i < map.length; i++)
			{
				for(int j = 0; j < map[i].length; j++)
					g.drawImage(tiles[map[i][j]].img.getImg(), tileSizeX * j, tileSizeY * i, dia);
			}
		}
		else
		{
			//int sx = scaleX;
			//int sy = (int) scaleY;
			for(int i = 0; i < map.length; i++)
			{
				int wm = map[i].length;
				int dy = i * scaleY;
				for(int j = 0; j < wm; j++)
				{
					//int dx = j * tileSizeX;
					//System.out.print(j* tileSizeX + "," + dy + ",");
					
					g.drawImage(tiles[map[i][j]].img.getImg(), 
							j * scaleX, dy, j * scaleX + scaleX, dy + scaleY,
							0, 0, tileSizeX, tileSizeY, 
							dia);
				}
			}
		}
		
		//for(int i = 0; i < tiles.length; i++)
			//g.drawImage(tiles[i].getImg(), 0, 50 * i, dia);
	}
	
	
}

class SpriteSelector extends PaintedImage
{
	TileMap.Tile[] allTiles;
	int w;
	BeveledRectangle selectRect;
	int selectedSprite;
	
	SpriteSelector(TileMap.Tile[] tiles, int width)
	{
		allTiles = tiles;
		w = width;
		selectRect = new BeveledRectangle();
		selectRect.setColor(0, 0, 0, 0);
		selectRect.setBevelWidth(3);
		setSelectedSprite(0);
		//selectedSprite = -1;
	}
	
	public Rectangle getDrawArea()
	{
		int width = allTiles[0].img.getDrawWid();
		int hgt = allTiles[0].img.getDrawHgt();
		//System.out.println("SpSelector width = " + width + " hgt = " + hgt);
		Rectangle r = new Rectangle(0, 0, width * w, hgt * ((allTiles.length / w) + 1));
		//System.out.println("DA Rect = " + r.toString());
		return r;
	}
	
	public Rectangle getSpriteArea(int num)
	{
		int width = allTiles[num].img.getDrawWid();
		int hgt = allTiles[num].img.getDrawHgt();
		
		int x = (num % w) * allTiles[0].img.getDrawWid();
		int y = (num / w) * allTiles[0].img.getDrawHgt();
		
		return new Rectangle(x, y, width, hgt);
		
	}
	
	public void setSelectedSprite(int n)
	{
		selectedSprite = n;
		//BeveledRectangle br = (BeveledRectangle) spriteViewer.getItemOfType("BeveledRectangle", 0);
		Rectangle r2 = getSpriteArea(n);
		selectRect.setLoc(r2.x, r2.y, r2.x + r2.width, r2.y + r2.height, null);
	}
	
	public int getSpriteAt(int x, int y)
	{
		Rectangle r = getSpriteArea(0);
		int xx = x / r.width;
		int yy = y / r.height;
		int rtnVal = (yy * w + xx);
		if(rtnVal >= allTiles.length)
			return -1;
		else
			return rtnVal;
	}
	
	public int getSelectedSprite()
	{
		return selectedSprite;
	}
	
	public void draw(Graphics g, Diorama dia)
	{
		int x = 0;
		int y = 0;
		for(int i = 0; i < allTiles.length; i++)
		{
			//int w = map[i].length;
			/*int sx = 0;
			int sy = 0;
			int sx2 = tileSizeX;
			int sy2 = tileSizeY;*/
			//int dy = i * tileSizeY;
			g.drawImage(allTiles[i].img.getImg(), x, y, dia);
			
			if(i % w == (w - 1))
			{
				x = 0; 
				y += allTiles[0].img.getDrawHgt();
			}
			else
				x += allTiles[0].img.getDrawWid();
		}
		
		selectRect.draw(g, dia);
	}
}

class CheckerBox extends TileMap
{
	CheckerBox()
	{
		super(150,150);
		//Diorama d = DioramaDemo.getTestDiorama();
		String[] strs = {"src\\Box0.jpg","src\\Box1.jpg", "src\\Box2.jpg", "src\\Box3.jpg"};
		addTiles(strs);
		for(int i = 0; i < 150; i++)
		{
			for(int j = 0; j < 150; j++)
			{
				if((i + j) % 10 == 0)
					setTile(i,j,1);
				else
					setTile(i,j,0);
				//setTile(i, j, ((i + j) & 3));
			}
		}
	}
}

class TileMapViewer extends JFrame implements ActionListener, MouseListener, MouseMotionListener
{
	TileMap tm;
	Diorama tmDia;
	DioramaV tmViewer;
	SpriteSelector sv;
	Diorama spriteViewer;
	JScrollPane tScroller;
	JScrollPane sScroller;
	JToggleButton[] buttons;
	ButtonGroup mapEditorGroup;
	JButton[] buttons2;
	int selectedSprite;
	
	JPanel inner;
	
	JComboBox nSides;
	JLabel nSidesLabel;
	Diorama tile;
	DioramaV tileView;
	AbstractButton[] tileViewButtons;
	ButtonGroup tileEditorGroup;
	JLabel tileInfo;
	
	JLabel parallax;
	
	int actionNo = -1;
	int tileAction = -1;
	
	double zoom;
	
	DrawBox drawBox;
	SelectionOverlay selectionOverlay;
	
	DrawBox dBoxTm;
	
	JPopupMenu multiComponent;
	
	JPanel top;
	
	JTextField tileWidth;
	JTextField tileHeight;
	JButton resetMapSize;
	
	TileMapViewer()
	{
		
		tm = new CheckerBox();
		//tm.setScale(0.05);
		sv = new SpriteSelector(tm.tiles, 3);
		
		String[] toggleButtonStrings = {"Pencil", "Box Fill", "Box Edge", "Flood"};
		String[] normalButtonStrings = {"Zoom in", "Zoom out", "Reset"};
		//gravityTrack settings, linkedWaypoint settings
		buttons = new JToggleButton[toggleButtonStrings.length];
		mapEditorGroup = new ButtonGroup();
		JPanel innerTop = new JPanel(new GridLayout(1, toggleButtonStrings.length + normalButtonStrings.length));
		for(int i = 0; i < toggleButtonStrings.length; i++)
		{
			buttons[i] = new JToggleButton(toggleButtonStrings[i]);
			buttons[i].addActionListener(this);
			mapEditorGroup.add(buttons[i]);
			innerTop.add(buttons[i]);
		}
		buttons2 = new JButton[normalButtonStrings.length];
		for(int i = 0; i < normalButtonStrings.length; i++)
		{
			buttons2[i] = new JButton(normalButtonStrings[i]);
			buttons2[i].addActionListener(this);
			innerTop.add(buttons2[i]);
		}
		
		tmDia = new Diorama();
		tmDia.initSize(0, 0);
		tmDia.suppressMouse();
		tmDia.addExistingObject(tm);
		
		dBoxTm = new DrawBox();
		tmViewer = new DioramaV(dBoxTm, tmDia, null);
		tmViewer.addMouseMotionListener(this);
		tmViewer.addMouseListener(this);
		//System.out.println("TMViewer = " + tmViewer.getPreferredSize());
		tScroller = new JScrollPane(tmViewer);
		
		spriteViewer = new Diorama();
		spriteViewer.initSize(0, 0);
		spriteViewer.suppressMouse();
		spriteViewer.addExistingObject(sv);
		
		spriteViewer.addMouseListener(this);
		///selectSprite(0);
		
		sScroller = new JScrollPane(spriteViewer);
		
		
		
		
		//tm.printTiles();
		System.out.println(tmViewer.getPreferredSize());
		System.out.println(spriteViewer.getPreferredSize());
		
		
		
		//FlowLayout flow = new FlowLayout();
		
		top = new JPanel(new GridLayout(1,2));
		String[] numberList = {"3", "4", "6"};
		nSides = new JComboBox(numberList);
		nSides.setSelectedIndex(1);
		nSides.addActionListener(this);
		nSidesLabel = new JLabel("Sides:");
		
		int sides = Integer.parseInt((String) nSides.getSelectedItem());
		tile = makeWaypointDia(sides);
		tm.tiles[0].setupTileInfo(tile);
		
		drawBox = new DrawBox();
		selectionOverlay = new SelectionOverlay();
		tileView = new DioramaV(drawBox, tile, selectionOverlay);
		tileView.addMouseListener(this);
		tileView.addMouseMotionListener(this);
		
		tileInfo = new JLabel("Tile Waypoints:");
		
		String[] editorStrings = {"S", "+W", "+L", "StdPsg", "StdObs"};
		tileViewButtons = new AbstractButton[editorStrings.length];
		JPanel tileEditorPanel = new JPanel(new GridLayout(1, tileViewButtons.length));
		JPanel tileEdit = new JPanel(new BorderLayout());
		tileEditorGroup = new ButtonGroup();
		for(int i = 0; i < editorStrings.length; i++)
		{
			if(i <= 2)
			{
				tileViewButtons[i] = new JToggleButton(editorStrings[i]);
				tileEditorGroup.add(tileViewButtons[i]);
			}
			else
			{
				tileViewButtons[i] = new JButton(editorStrings[i]);
			}
			tileViewButtons[i].addActionListener(this);
			tileEditorPanel.add(tileViewButtons[i]);
		}
		FlowLayout flow = new FlowLayout();
		flow.setAlignment(FlowLayout.CENTER);
		JPanel topLeft = new JPanel(new GridLayout(4,2));
		topLeft.add(nSidesLabel);
		topLeft.add(nSides);
		
		tileWidth = new JTextField(5);
		tileWidth.setText(String.valueOf(tm.map.length));
		tileHeight = new JTextField(5);
		tileHeight.setText(String.valueOf(tm.map[0].length));
		topLeft.add(new JLabel("Map width:"));
		topLeft.add(tileWidth);
		topLeft.add(new JLabel("Map height:"));
		topLeft.add(tileHeight);
		topLeft.add(new JLabel(""));
		
		resetMapSize = new JButton("Reset");
		resetMapSize.addActionListener(this);
		topLeft.add(resetMapSize);
		
		//JPanel topLeftB = new JPanel(flow);
		
		//flow.setHgap(100);
		JPanel topRight = new JPanel(new BorderLayout());
		
		topRight.add(tileInfo, BorderLayout.NORTH);
		tileEdit.add(tileEditorPanel, BorderLayout.NORTH);
		tileEdit.add(tileView);
		//tileEdit.pack();
		System.out.println(tileView.getPreferredSize());
		topRight.add(tileEdit);
		top.add(topLeft);
		top.add(topRight);
		
		//validate();
		zoom = 1.0;
		
		inner = new JPanel(new BorderLayout());
		//inner.set
		inner.add(innerTop, BorderLayout.NORTH);
		inner.add(tScroller, BorderLayout.CENTER);
		//JScrollPane jspI = new JScrollPane(inner);
		
		setSize(800, 600);
		getContentPane().add(top, BorderLayout.NORTH);
		getContentPane().add(inner, BorderLayout.CENTER);
		getContentPane().add(sScroller, BorderLayout.WEST);
		
		setVisible(true);
		tmDia.repaint();
		//spriteViewer.repaint();
	}
	
	private Diorama makeWaypointDia(int nSides)
	{
		Diorama dia = new Diorama();
		dia.initSize(100, 100);
		
		dia.makeAndAddObject("PaintedRectangle", 0, 0, 99, 99);
		dia.makeAndAddObject("PaintedRegularShape", 25, 25, 75, 75);
		PaintedRectangle pr = (PaintedRectangle) dia.getAllItemsOfType("PaintedRectangle")[0];
		pr.setColor(255, 255, 255, 255);
		PaintedRegularShape prs = (PaintedRegularShape) dia.getAllItemsOfType("PaintedRegularShape")[0];
		prs.setNPoints(nSides);
		double centralAngle = (Math.PI * 2) / nSides;
		double angle = (Math.PI / 2);
		int diff = 50;
		if(nSides == 4)
		{
			angle += Math.PI / 4;
			diff = 50;
		}
		for(int i = 0; i < nSides; i++)
		{
			
			int x1 = (int) (Math.cos(angle) * 45) + diff;
			int y1 = (int) (Math.sin(angle) * -45) + diff;
			//System.out.println(Math.toDegrees(angle) + ":" + x1 + "," + y1);
			angle += centralAngle;
			int x2 = (int) (Math.cos(angle) * 45) + diff;
			int y2 = (int) (Math.sin(angle) * -45) + diff;
			//System.out.println(Math.toDegrees(angle) + ":" + x2 + "," + y2);
			
			dia.makeAndAddObject("Waypoint", x1, y1, x1, y1);
			dia.makeAndAddObject("Waypoint", 0,0, Math.abs(x2 + x1) / 2, Math.abs(y2 + y1) / 2);
			
		}
		return dia;
	}
	
	private Diorama makeTileInfo(TileMap.Tile in)
	{
		Diorama d = makeWaypointDia(in.nSides);
		Waypoint[] points = in.points;
		if(points.length >= in.nSides * 2)
		{
		//clear the newly created waypoints
			Waypoint[] old = (Waypoint[]) d.getAllItemsOfType("Waypoint");
			for(int i = 0; i < old.length; i++)
				d.removeItem(old[i]);
		}
		
		LinearTrack[] arcs = in.arcs;
		PaintedRegularShape ps = (PaintedRegularShape) d.getItemOfType("PaintedRegularShape", 0);
		double[] ds = {ps.rotOriginX, ps.rotOriginY, in.rTheta};
		ps.setRotation(ds);
		for(int i = 0; i < points.length; i++)
			d.addExistingObject(points[i]);
		
		for(int i = 0; i < arcs.length; i++)
			d.addExistingObject(arcs[i]);
		//rTheta = ps.rTheta;
		return d;
	}
	
	private void execAction(int num)
	{
		switch(num)
		{
		case 0: // zoom in
			//System.out.println(zoom);
			int exp = (int) Math.floor(Math.log10(zoom));
			if(exp == Math.log10(zoom))
				zoom *= 2.0;
			else
				zoom += Math.pow(10, exp);
			//System.out.println(zoom);
			tm.setScale(zoom);
			tmViewer.revalidate();
			//tScroller.revalidate();
			break;
		case 1:  //zoom out
			//System.out.println(zoom);
			exp = (int) Math.floor(Math.log10(zoom));
			double zz = Math.pow(10, exp);
			zoom -= Math.pow(10, exp);
			if(zoom < zz)
				zoom = zz * 0.9;
			//System.out.println(zoom);
			tm.setScale(zoom);
			tmViewer.revalidate();
			break;
		case 2:  //reset
			break;
		}
		//validate();
		repaint();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		System.out.println(e.getSource());
		for(int i = 0; i < buttons.length; i++)
		{
			if(e.getSource() == buttons[i])
			{
				actionNo = i;
			}
		}
		
		for(int i = 0; i < buttons2.length; i++)
		{
			if(e.getSource() == buttons2[i])
			{
				execAction(i);
			}
		}
		
		for(int i = 0; i < tileViewButtons.length; i++)
		{
			if(e.getSource() == tileViewButtons[i])
				tileAction = i;
			if(tileAction == 3) //stdPassage
			{
				Diorama dx = this.makeWaypointDia(tm.nSides);
				Dimension dxDim = dx.getPreferredSize();
				int aa = dxDim.height / 2 + 1;
				int bb = dxDim.width / 2 + 1;
				Waypoint wp = (Waypoint) dx.makeAndAddObject("Waypoint", aa, bb, aa, bb);
				//int[] destPoints;// = null;// = new int[8];
				if(tm.nSides == 3)
				{
					int[] destPoints = {1, 3, 5};
					for(int j = 0; j < destPoints.length; j++)
					{
						Waypoint wpx = (Waypoint) dx.getItemOfType("Waypoint", destPoints[j]);
						LinearTrack lt = new LinearTrack(wp, wpx);
						dx.addExistingObject(lt);
					}
				}
				if(tm.nSides == 4)
				{
					int[] destPoints = {0,1,2,3,4,5,6,7};
					for(int j = 0; j < destPoints.length; j++)
					{
						Waypoint wpx = (Waypoint) dx.getItemOfType("Waypoint", destPoints[j]);
						LinearTrack lt = new LinearTrack(wp, wpx);
						dx.addExistingObject(lt);
					}
				}
				if(tm.nSides == 6)
				{
					int[] destPoints = {1,3,5,7,9,11};
					for(int j = 0; j < destPoints.length; j++)
					{
						Waypoint wpx = (Waypoint) dx.getItemOfType("Waypoint", destPoints[j]);
						LinearTrack lt = new LinearTrack(wp, wpx);
						dx.addExistingObject(lt);
					}
				}
				//tm.tiles[sv.getSelectedSprite()].setupTileInfo(dx);
				tileAction = -1;
				tile = dx;
				tileView.setDiorama(dx);
				repaint();
			}
			if(tileAction == 4) //stdObstacle
			{
				Diorama dx = this.makeWaypointDia(tm.nSides);
				tm.tiles[sv.getSelectedSprite()].setupTileInfo(dx);
				tileAction = -1;
				tileView.setDiorama(dx);
				repaint();
			}
		}
		
		if(multiComponent != null)
		{
			MenuElement[] elements = multiComponent.getSubElements();
			for(int i = 0; i < elements.length; i++)
			{
				JMenuItem jmi = (JMenuItem) elements[i];
				if(e.getSource() == jmi)
				{
					selectObject(null, jmi);
				}
			}
		}
		
		else if(e.getSource() == nSides)
		{
			String val = (String) nSides.getSelectedItem();
			System.out.println("Making " + val + " sides");
			int numSides = Integer.parseInt(val);
			tm.nSides = numSides;
			tile = makeWaypointDia(numSides);
			tileView.setDiorama(tile);
			repaint();
		}
		
		else if(e.getSource() == resetMapSize)
		{
			try
			{
				int newXSize = Integer.parseInt(tileWidth.getText());
				int newYSize = Integer.parseInt(tileHeight.getText());
			
				if(newXSize != tm.map[0].length || newYSize != tm.map.length)
				{
					tm.setMapSize(newXSize, newYSize);
					tmViewer.revalidate();
					repaint();
				}
			}
			catch(NumberFormatException ex){}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource() == tmViewer)
		{
			if(actionNo == 0)
			{
				tm.setTileAtLoc(e.getX(), e.getY(), sv.getSelectedSprite());
				repaint();
			}
			if(actionNo == 1 || actionNo == 2)
			{
				dBoxTm.setBoxDims(e.getX(), e.getY());
				repaint();
			}
		}
		else if(e.getSource() == tileView)
		{
			drawBox.setBoxDims(e.getX(), e.getY());
			
			if(selectionOverlay.getSelectedIndex(e.getX(), e.getY(), false) > -1)
			{
				selectionOverlay.resizeSelItem(e.getX(), e.getY());
				tm.tiles[sv.getSelectedSprite()].setupTileInfo(tile);
			}
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		//System.out.println("Mouse Clicked" + e.getSource());
		// TODO Auto-generated method stub
		if(e.getSource() == spriteViewer)
		{
			int ss = sv.getSpriteAt(e.getX(), e.getY());
			if(ss == -1)
				return;
			tm.tiles[sv.getSelectedSprite()].setupTileInfo(tile);
			sv.setSelectedSprite(ss);
			if(selectionOverlay.visible)
				selectionOverlay.setVisible(false);
			tile = makeTileInfo(tm.tiles[sv.selectedSprite]);
			tileView.setDiorama(tile);
			repaint();
		}
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
	public void mousePressed(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource() == tmViewer)
		{
			if(actionNo == 0)
			{
				tm.setTileAtLoc(e.getX(), e.getY(), sv.getSelectedSprite());
				repaint();
			}
			if(actionNo == 1)
			{
				dBoxTm.init(e.getX(), e.getY());
				dBoxTm.setVisible(true);
				repaint();
			}
			if(actionNo == 2)
			{
				dBoxTm.init(e.getX(), e.getY());
				dBoxTm.setVisible(true);
				repaint();
			}
			if(actionNo == 3)
			{
				tm.floodTilesAtLoc(e.getX(), e.getY(), sv.getSelectedSprite());
				repaint();
			}	
		}
		
		if(e.getSource() == tileView)
		{
			int x = e.getX();
			int y = e.getY();
			
			if(selectionOverlay.getSelectedIndex(x, y, true) >= 0)
			{
				return;
			}
			else
			{
				int initX = e.getX();
				int initY = e.getY();
				drawBox.init(initX, initY);
				drawBox.setVisible(true);
				selectionOverlay.setVisible(false);
			}
			repaint();
		}
	}
	
	private int[] getObjects(DrawBox in)
	{
		Vector<Integer> sels = new Vector<Integer>();
		Rectangle r2 = new Rectangle(in.x1, in.y1, (in.x2 - in.x1), (in.y2 - in.y1));
		r2.grow(1, 1);
		//System.out.println("IN size:" + r2.width + "," + r2.height);
		int x = tile.objectList.size();
		for(int i = 0; i < x; i++)
		{
			Object o = tile.objectList.get(i);
			//System.out.println("Looking at " + o.toString());
			if(o instanceof Selectable)
			{
				Selectable sel = (Selectable) o;
				//if(sel.getXform()[2] == 0)
				//{//No need to rotation test because nothing is roatatable in this viewer
				Rectangle rect = sel.getRect();
				if(rect.intersects(r2))
				{
					sels.add(new Integer(i));
					System.out.println("Added component #" + i + ": " + sel.toString());
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
	
	private int getTopSelectedObject(DrawBox in, Diorama dia)
	{
		int[] as = getObjects(in);
		if(as.length == 0)
			return -1;
		else if(as.length == 1)
			return as[0];
		else
		{
			multiComponent = new JPopupMenu();
			for(int i = 0; i < as.length; i++)
			{
				Selectable sel = (Selectable) dia.objectList.get(as[i]);
				System.out.println("I selected #" + as[i] + sel);
				String iName = sel.getItemName();
				JMenuItem jmi = new JMenuItem(iName);
				jmi.setActionCommand(String.valueOf(as[i]));
				jmi.addActionListener(this);
				multiComponent.add(jmi);
			}
			//tile.setVisible(true);
			//multiComponent.sh
			multiComponent.show(tileView, in.x2, in.y2);
			return -1;
		}
		//return as[as.length - 1];
	}
	
	private void selectObject(DrawBox in, JMenuItem jmi)
	{
		int sel = -1;
		if(in != null)
		{
			sel = getTopSelectedObject(drawBox, tile);
			if(sel == -1)
				return;
		}
		else if(jmi != null)
			sel = Integer.parseInt(jmi.getActionCommand());
		
		Object uo = tile.objectList.get(sel);
		if(uo instanceof Selectable)
		{
			Selectable selc = (Selectable) uo;
			selectionOverlay.selectItem(selc);
			selectionOverlay.setVisible(true);
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource() == tmViewer)
		{
			dBoxTm.setVisible(false);
			if(actionNo == 1)  //fill
			{
				tm.fillTilesBox(dBoxTm.x1, dBoxTm.y1, dBoxTm.x2, dBoxTm.y2, sv.getSelectedSprite());
				//repaint();
			}
			else if(actionNo == 2)  //edge
			{
				tm.drawTilesBox(dBoxTm.x1, dBoxTm.y1, dBoxTm.x2, dBoxTm.y2, sv.getSelectedSprite());
				//repaint();
			}
			repaint();
		}
		
		else if(e.getSource() == tileView)
		{
			drawBox.setVisible(false);
			
			//String myOpt = tileEditorGroup.
			
			if(tileAction == -1)
				return;
			
			if(tileAction == 0)
			{
				//int[] selections = getObjects(dbox);
				if(selectionOverlay.visible == false)
					selectObject(drawBox, null);
			}
			else
			{
				String myOpt;
				if(tileAction == 1)
					myOpt = "Waypoint";
				else
					myOpt = "LinearTrack";
				System.out.println(myOpt + ":" + drawBox.x1 + "," + drawBox.y1);
				Object obj = tile.makeAndAddObject(myOpt, drawBox.x1, drawBox.y1, drawBox.x2, drawBox.y2);
				tm.tiles[sv.getSelectedSprite()].setupTileInfo(tile);
				if(obj != null && obj instanceof Selectable)
				{		
					//System.out.println("repainting the diav now");
					repaint();
					
					//validate();
				}
			}
			//clear the selected object's selected index
			selectionOverlay.selIndex = -1;
			//repaint();
		}
	}

	
	
	
	
}

public class TileEditorDemo
{
	public static void main(String[] args)
	{
		TileMapViewer mtv = new TileMapViewer();
		
		
		
		
	}
}
