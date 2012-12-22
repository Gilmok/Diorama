import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

class DTrackEditor extends JPanel implements MouseListener, MouseMotionListener, Scrollable
{
	
	Diorama dia;
	Track t;
	int pixW, pixH;
	final int BOX_W = 500;
	final int BOX_H = 500;
	int xpad, ypad;
	int oper;
	Rectangle editRect;
	boolean drawRect;
	boolean drawLine; 
	
	DTrackEditor(Diorama d, Track tk)
	{
		//draw all pics in 
		dia = d;
		update(tk);
		addMouseListener(this);
		addMouseMotionListener(this);
		
	}
	
	public void update(Track tk)
	{
		t = tk;
		setPreferredSize(new Dimension(BOX_W, BOX_H));
		pixW = BOX_W / tk.width;
		pixH = BOX_H / tk.height;
		//xpad = tk.width / 3;
		//ypad = tk.height / 3;
	}
	
	public void paintComponent(Graphics g)
	{
		dia.drawMap(g, (int) t.xInit, (int) t.yInit, t.width, t.height, BOX_W, BOX_H);
		g.setColor(new Color(255, 0, 0, 127));
		//System.out.println(t.height + "," + t.width);
		for(int i = 0; i < t.height; i++)
		{
			for(int j = 0; j < t.width; j++)
			{
				if(t.move[i][j])
					g.fillRect(pixW * j, pixH * i, pixW, pixH);
					//g.fillRect(j*4, i*4, 4, 4);
			}
		}
		if(drawRect)
			g.fillRect(editRect.x, editRect.y, editRect.width, editRect.height);
		if(drawLine)
			g.drawLine(editRect.x, editRect.y, editRect.width, editRect.height);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	// 
	public void mousePressed(MouseEvent arg0) 
	{
		int x = arg0.getX();
		int y = arg0.getY();
		//System.out.println("MP:" + x + "," + y);
		switch(oper)
		{
		case 0:
			t.move[y / pixH][x / pixW] = !(t.move[y/pixH][x/pixW]);
			break;
		case 1: case 2: case 3: case 4:
			editRect.x = x;
			editRect.y = y;
			editRect.width = 0;
			editRect.height = 0;
			
			if(oper == 1)
				drawLine = true;
			else
				drawRect = true;
			break;
			
			
		}
		
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		// TODO Auto-generated method stub
		switch(oper)
		{
		case 0: //point - do nothing
			break;
		case 1: //scissors
			drawLine = false;
			break;
		case 2:  //fill box
			//process the rectangle
			for(int i = editRect.y; i < editRect.height; i += pixH)
				for(int j = editRect.x; j < editRect.width; j+= pixW)
					t.move[i/pixH][j/pixW] = true;
			
			drawRect = false;
			//turn off the edit rect
			break;
		case 3:  //empty box
			for(int i = editRect.y; i < editRect.height; i += pixH)
				for(int j = editRect.x; j < editRect.width; j+= pixW)
					t.move[i/pixH][j/pixW] = false;
			
			drawRect = false;
			break;
		case 4:  //toggle box
			for(int i = editRect.y; i < editRect.height; i += pixH)
				for(int j = editRect.x; j < editRect.width; j+= pixW)
					t.move[i/pixH][j/pixW] = !t.move[i/pixH][j/pixW];
			
			drawRect = false;
			break;
		}
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		int x = arg0.getX();
		int y = arg0.getY();
		//System.out.println("MD:" + x + "," + y);
		switch(oper)
		{
		case 0:
			t.move[y / pixH][x / pixW] = !(t.move[y/pixH][x/pixW]);
			break;
		case 1: case 2: case 3: case 4:
			if(y < editRect.y)
			{
				editRect.height = editRect.y - y;
				editRect.y = y;
			}
			else
				editRect.height = y - editRect.y;
			if(x < editRect.x)
			{
				editRect.width = editRect.x - x;
				editRect.x = x;
			}
			else
				editRect.width = x - editRect.x;
		}	
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

class TrackEditor extends JFrame implements ActionListener
{
	TextField[] deltaGrid;
	TextField[] size;
	JLabel lbls[];
	JLabel lbls2[];
	JCheckBox gravity;
	DTrackEditor dt;
	JButton[] editOps;
	
	
	TrackEditor(Diorama d, int index)
	{
		deltaGrid = new TextField[12];
		lbls = new JLabel[10];
		
		Track[] tracks = Track.getTracks(d);
		
		dt = new DTrackEditor(d, tracks[index]);
		
		JPanel deltaPan = new JPanel(new GridLayout(4,6));
		deltaGrid = new TextField[15];
		for(int i = 0; i < 15; i++)
			deltaGrid[i] = new TextField();
		String[] x = {"", "None", "Up", "Down", "Left", "Right", "deltaX", "deltaY", "deltaZ"};
		lbls = new JLabel[x.length];
		for(int i = 0; i < x.length; i++)
			lbls[i] = new JLabel(x[i]);
		for(int i = 0; i < 6; i++)
			deltaPan.add(lbls[i]);
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 6; j++)
			{
				if(j == 0)
					deltaPan.add(lbls[i + 6]);
				else
					deltaPan.add(deltaGrid[i * 5 + j - 1]);
			}
		}
		
		editOps = new JButton[5];
		String[] xx = {"Point", "Scissors", "FillBox", "EmptyBox", "ToggleBox"};
		JPanel innerTop = new JPanel(new GridLayout(1, editOps.length));
		for(int i = 0; i < editOps.length; i++)
		{
			editOps[i] = new JButton(xx[i]);
			editOps[i].addActionListener(this);
			innerTop.add(editOps[i]);
		}
		
		JPanel inner = new JPanel();
		inner.add(innerTop, BorderLayout.NORTH);
		inner.add(dt, BorderLayout.CENTER);
		
		JPanel leftPan = new JPanel(new GridLayout(3, 1));
		size = new TextField[2];
		size[0] = new TextField();
		size[1] = new TextField();
		lbls2 = new JLabel[2];
		lbls2[0] = new JLabel("Width"); 
		lbls2[1] = new JLabel("Height");
		gravity = new JCheckBox();
		for(int i = 0; i < 2; i++)
		{
			JPanel paan = new JPanel(new BorderLayout());
			paan.add(lbls2[i], BorderLayout.WEST);
			paan.add(size[i], BorderLayout.CENTER);
			leftPan.add(paan);
		}
		leftPan.add(gravity);
		
		getContentPane().add(leftPan, BorderLayout.WEST);
		getContentPane().add(deltaPan, BorderLayout.SOUTH);
		getContentPane().add(inner, BorderLayout.CENTER);
		
		setSize(500, 500);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		for(int i = 0; i < 5; i++)
		{
			if(e.getSource() == editOps[i])
			{
				dt.oper = i;
				break;
			}
		}
	}
}

public class TrackEditorDemo 
{
	public static void main(String[] args)
	{
		System.out.println(System.getProperty("user.dir"));
		Diorama d = DioramaDemo.getTestDiorama();
		//ImageIcon[] img = new ImageIcon[10];
		//for(int i = 0; i < 10; i++)
			//img[i] = Diorama.getImgIcon("C:\\Users\\Aaron\\Desktop\\Wizard1\\Txt_" + i + ".gif");
		//d.addSprite(img, 0, 0, 0, 0, 0, false);
		
		TrackEditor te = new TrackEditor(d, 0);
	}
}
