import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.image.BufferStrategy;
import java.io.File;

import javax.swing.*;
import javax.swing.event.*;
import java.util.Vector;

class SpriteTabPane extends JPanel
{
	//String[] spriteList;
	Diorama dia;
	private JTabbedPane tabs;
	private Font btnFont;
	private ListSelectionListener lsl;
	Vector openSprites;
	//private JList loopList;
	
	class AddTab extends JPanel implements ActionListener
	{
		JButton plus;
		int x;
		int y;
		
		AddTab()
		{
			plus = new JButton("+");
			
			plus.setFont(btnFont);
			//plus.setPreferredSize(new Dimension(24,));
			plus.addActionListener(this);
			add(plus);
			x = plus.getX();
			y = plus.getY();
		}
		
		public void actionPerformed(ActionEvent e)
		{
			//show a list of sprites in the list
			JButton b = (JButton) e.getSource();
			String[] spriteList = getSpriteNames(dia);
			JPopupMenu jpm = new JPopupMenu();
			//jpm.setLocation(x, y);
			for(int i = 0; i < spriteList.length; i++)
				jpm.add("+" + spriteList[i]);
			jpm.show(b, 0, 0);
		}
	}
	
	class FlexTab extends JPanel implements ActionListener
	{
		JButton close;
		JLabel name;
		int index;
		
		FlexTab(String n, int i)
		{
			name = new JLabel(n);
			close = new JButton("X");
			close.setFont(btnFont);
			//close.setPreferredSize(new Dimension(24,16));
			index = i;
			//JPanel label = new JPanel(new FlowLayout());
			add(name, BorderLayout.CENTER);
			add(close, BorderLayout.EAST);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			tabs.remove(index);
		}
	}
	
	SpriteTabPane(Diorama d, ChangeListener chl, ListSelectionListener l)
	{
		dia = d;
		btnFont = new Font("SansSerif", 1, 8);
		tabs = new JTabbedPane();
		tabs.addTab("", null);
		tabs.setTabComponentAt(0, new AddTab());
		add(tabs);
		tabs.addChangeListener(chl);
		lsl = l;
		openSprites = new Vector();
	}
	
	public void addSprite(int index)
	{
		PaintedSprite ps = (PaintedSprite) dia.getAllItemsOfType("PaintedSprite")[index];
		openSprites.add(index);
		JList lst = getStates(ps);
		addTab(ps.name, lst);
	}
	
	public void addTab(String name, Component c)
	{
		int idx = tabs.getTabCount() - 1;
		//int idx = 0;
		FlexTab ft = new FlexTab(name, idx);
		tabs.removeTabAt(idx);
		tabs.addTab("", c);
		tabs.setTabComponentAt(idx, ft);
		tabs.addTab("", null);
		tabs.setTabComponentAt(idx + 1, new AddTab());
	}
	
	private String[] getSpriteNames(Diorama dia)
	{
		PaintedSprite[] allPs = (PaintedSprite[]) dia.getAllItemsOfType("PaintedSprite");
		String[] rtnVal = new String[allPs.length];
		for(int i = 0; i < allPs.length; i++)
		{ //this is not elegant (using PropertyGenerator MIGHT be better)
			PaintedSprite ps = allPs[i];
			rtnVal[i] = ps.name;
		}
		return rtnVal;
	}
	
	private String[] getStateNames(PaintedSprite in)
	{
		String[] rtnVal = new String[in.loopProperties.length];
		for(int i = 0; i < rtnVal.length; i++)
			rtnVal[i] = in.loopProperties[i].stateDescription;
		return rtnVal;
	}
	
	private JList getStates(PaintedSprite in)
	{
		JList lst = new JList(getStateNames(in));
		lst.setPrototypeCellValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		lst.addListSelectionListener(lsl);
		return lst;
	}
	
	private PaintedSprite getSelectedSprite()
	{
		int x = getSelectedIndex();
		int y = (Integer) openSprites.get(x);
		PaintedSprite ps = (PaintedSprite) dia.getAllItemsOfType("PaintedSprite")[y];
		return ps;
	}
	
	public int getSelectedIndex()
	{
		return tabs.getSelectedIndex();
	}
	
	public int getSelectedLoop()
	{
		JList lst = (JList) tabs.getSelectedComponent();
		return lst.getSelectedIndex();
	}
	
	public void setSelectedLoop(int idx)
	{
		if(idx == -1)  //just update the loop properites
			idx = getSelectedLoop();
		//if(idx == -1)  //if you are still -1 and nothing is selected
			//idx = 0;
		int x = getSelectedIndex();
		JList lst = getStates(getSelectedSprite());
		tabs.setComponentAt(x, lst);
		//System.out.println("I have updated the list " + idx);
		//System.out.println(getSelectedSprite().loopProperties[idx].stateDescription);
		lst.setSelectedIndex(idx);
	}
	
}

class StatesView extends JPanel implements ChangeListener, ListSelectionListener
{
	SpriteTabPane view;
	SpriteEditor local;

	StatesView(Diorama d, int index, SpriteEditor se)
	{
		local = se;
		view = new SpriteTabPane(d, this, this);
		if(index != -1)
		{
			view.addSprite(index);
		}
		add(view);
	}
	
	public void stateChanged(ChangeEvent e)  //new sprite selected
	{
		local.setSelectedSprite(view.getSelectedIndex());
	}
	
	public void valueChanged(ListSelectionEvent e)  //new loop selected
	{
		local.setSelectedLoop(view.getSelectedLoop());
	}
	
	public void setSelectedLoop(int idx)
	{
		view.setSelectedLoop(idx);
	}
	
}

class SpriteView extends JPanel implements ActionListener, Scrollable
{
	Timer tim;
	PaintedSprite[] currSprites;
	
	class KbInput extends KeyAdapter
	{
		KbInput()
		{
			super();
		}
		
		public void keyPressed(KeyEvent e) 
		{
			// TODO Auto-generated method stub
			for(int i = 0; i < currSprites.length; i++)
				currSprites[i].processKeys(e.getKeyCode(), false);
		}

		@Override
		public void keyReleased(KeyEvent e) 
		{
			// TODO Auto-generated method stub
			for(int i = 0; i < currSprites.length; i++)
				currSprites[i].processKeys(e.getKeyCode(), true);
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	SpriteView()
	{
		setFocusable(true);
		setEnabled(true);
		currSprites = new PaintedSprite[0];
		setPreferredSize(new Dimension(320, 256));
		tim = new Timer(30, this);
		addKeyListener(new KbInput());
	}
	
	public void addSprite(PaintedSprite ps)
	{
		ps.currFrame = 0;
		//System.out.println(ps.currFrame);
		//System.out.println(ps.frames[ps.currFrame]);
		PaintedSprite ps2[] = new PaintedSprite[currSprites.length + 1];
		for(int i = 0; i < currSprites.length; i++)
			ps2[i] = currSprites[i];
		ps2[currSprites.length] = ps;
		currSprites = ps2;
	}
	
	public void removeSprite(int index)
	{
		PaintedSprite ps2[] = new PaintedSprite[currSprites.length - 1];
		for(int i = 0; i < index; i++)
			ps2[i] = currSprites[i];
		for(int i = index + 1; i < currSprites.length; i++)
			ps2[i - 1] = currSprites[i];
		currSprites = ps2;
	}
	
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.BLACK);
		Dimension d = this.getSize();
		g.fillRect(0, 0, d.width, d.height);
		for(int i = 0; i < currSprites.length; i++)
		{
			//System.out.println(currSprites[i]);
			//System.out.println(currSprites[i].getImg());
			g.drawImage(currSprites[i].getImg(), 50, 20 + 100 * i,  this);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		for(int i = 0; i < currSprites.length; i++)
		{
			currSprites[i].advanceFrame();
		}
		repaint();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) 
	{
		// TODO Auto-generated method stub
		return 64;
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
		return 64;
	}
	
	
}

class TestView extends JPanel implements ActionListener
{
	JLabel kbTest;
	JLabel currLoop;
	JButton play;
	JButton stop;
	SpriteView spriteView;
	//Timer tim;
	SpriteEditor local;
	
	TestView()
	{
		setLayout(new BorderLayout());
		kbTest = new JLabel("Test");
		currLoop = new JLabel("");
		play = new JButton("Play");
		play.addActionListener(this);
		stop = new JButton("Stop");
		stop.addActionListener(this);
		
		
		JPanel top1 = new JPanel(new GridLayout(1, 3));
		JPanel top2 = new JPanel();
		JPanel topPanel = new JPanel(new GridLayout(2, 1));
		top1.add(kbTest);
		top1.add(play);
		top1.add(stop);
		top2.add(currLoop);
		
		topPanel.add(top1);
		topPanel.add(top2);
		
		spriteView = new SpriteView();
		add(topPanel, BorderLayout.NORTH);
		add(spriteView, BorderLayout.CENTER);
		
		//currSprite = null;
	}
	
	TestView(PaintedSprite ps)
	{
		this();
		addSprite(ps);
	}
	
	public void addSprite(PaintedSprite ps)
	{
		spriteView.addSprite(ps);
	}
	
	public void removeSprite(int index)
	{
		spriteView.removeSprite(index);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == play)
		{
			spriteView.requestFocusInWindow();
			spriteView.tim.start();
		}
		else
		{
			spriteView.tim.stop();
		}
	}
}

class KbInputView extends JPanel implements ActionListener
{
	PaintedSprite.KBResponse localKbr;
	PropertyEditor localPe;
	ActionListener localAl;
	JButton helpBtn;
	KbInputWin win;
	
	
	KbInputView(PaintedSprite.KBResponse kbr, ActionListener other)
	{
		localKbr = kbr;
		localAl = other;
		
		JLabel help = new JLabel("KB Press Frame, KB Press Loop");
		JLabel help2 = new JLabel(" KB Release Frame, KB Release Loop");
		JPanel n = new JPanel(new GridLayout(2, 1));
		n.add(help);
		n.add(help2);
		
		
		localPe = new PropertyEditor(kbr, other);
		
		helpBtn = new JButton("Add key...");
		helpBtn.addActionListener(this);
		
		//addKeyListener(new KbInput());
		setLayout(new BorderLayout(0,0));
		add(n, BorderLayout.NORTH);
		add(helpBtn, BorderLayout.SOUTH);
		add(localPe, BorderLayout.CENTER);
		win = null;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(win == null)
			win = new KbInputWin(this);
	}
	
	public void updateWithKey(int in)
	{
		Integer[] data = {in, null, null, null, null}; 
		int l = localKbr.kbChange.length;
		localKbr.addKbResponse(data);
		if(localKbr.kbChange.length > l)
		{
			remove(localPe);
			localPe = new PropertyEditor(localKbr, localAl);
			add(localPe, BorderLayout.CENTER);
			validate();
		}
		win = null;
	}
}

class KbInputWin extends JFrame
{
	KbInputView kbv;
	
	class KbInput extends KeyAdapter
	{
		KbInput()
		{
			super();
		}
		
		public void keyPressed(KeyEvent e) 
		{
			// TODO Auto-generated method stub
			setVisible(false);
			kbv.updateWithKey(e.getKeyCode());
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	KbInputWin(KbInputView in)
	{
		kbv = in;
		JLabel pressKey = new JLabel("Press a key");
		addKeyListener(new KbInput());
		getContentPane().add(pressKey);
		setSize(200, 70);
		setLocation(500, 400);
		setVisible(true);
	}
}

class LoopEditor extends JPanel implements ActionListener
{
	JTabbedPane view;
	int spriteIndex;
	int loopIndex;
	int frameIndex;
	PaintedSprite currSprite;
	PropertyEditor peLoop;
	PropertyEditor peFrame;
	KbInputView peKb;
	
	SpriteEditor local;
	
	LoopEditor()
	{
		view = new JTabbedPane();
		view.addTab("Loop", null);
		view.addTab("Frame", null);
		view.addTab("KB Input", null);
		add(view);
	}
	
	LoopEditor(PaintedSprite ps, SpriteEditor se)
	{
		this();
		local = se;
		setSprite(ps);
	}
	
	public void setSprite(PaintedSprite ps)
	{
		currSprite = ps;
		setFrame(0);
		setLoop(0);
		setKB();
	}
	
	public void setFrame(int frameNo)
	{
		peFrame = new PropertyEditor(currSprite.frameProperties[frameNo], this);
		view.setComponentAt(1, peFrame);
	}
	
	public void setLoop(int loopNo)
	{
		peLoop = new PropertyEditor(currSprite.loopProperties[loopNo], this);
		view.setComponentAt(0, peLoop);
	}
	
	public void setKB()
	{
		peKb = new KbInputView(currSprite.kbr, this);
		view.setComponentAt(2, peKb);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		//collect notification of changes
		switch(view.getSelectedIndex())
		{
		case 0:
			local.setSelectedLoop(-1);
		case 1:
		}
		//local.update();
	}
}

class CellView extends JPanel 
{
	boolean selected;
	boolean loopSelected;
	Image spriteFrame;
	
	CellView(Image img)
	{
		selected = false;
		loopSelected = false;
		spriteFrame = img;
		setPreferredSize(new Dimension(64,64));
	}
	
	public void paintComponent(Graphics g)
	{
		if(selected)
		{
			g.setColor(new Color(255,0,0,128));
			g.fillRect(0,0,63,63);
		}
		if(loopSelected)
		{
			g.setColor(new Color(0,0,255,128));
			g.fillRect(0,0,63,63);
		}
		g.setColor(new Color(0,0,0));
		g.drawRect(4, 4, 56, 56);
		if(spriteFrame != null)
		{
			int w = spriteFrame.getWidth(this);
			int h = spriteFrame.getHeight(this);
			if(w > 48)
				w = 48;
			if(h > 48)
				h = 48;
			g.drawImage(spriteFrame, 32 - (w/2), 32 - (h/2), w, h, this);
		}
	}
	
	
}

class JScrollPanel extends JPanel implements Scrollable
{
	//the things I have to do to appease the compiler....
	JScrollPanel(LayoutManager layout)
	{
		super(layout);
	}
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 64;
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
		return 64;
	}
	
}

//Zone 4
class FrameEditor extends JPanel implements ActionListener, MouseListener, FramePopupUser
{
	CellView[] cells;
	int selectedCell;
	int startLoop;
	int endLoop;
	PaintedSprite storedSprite;
	JPopupMenu rightClickMenu;
	JMenuItem[] menuItems;
	JFrame openFrame;
	int operation;
	
	SpriteEditor local;
	
	FrameEditor(PaintedSprite ps, SpriteEditor se)
	{
		local = se;
		
		String[] options = {"Copy Frames", "Add Loop", "Add Frame", "Remove Loop", "Remove Frame"};
		menuItems = new JMenuItem[options.length];
		rightClickMenu = new JPopupMenu();
		
		//jpm.setLocation(m.getX(), m.getY());
		for(int i = 0; i < options.length; i++)
		{
			menuItems[i] = new JMenuItem(options[i]);
			menuItems[i].addActionListener(this);
			rightClickMenu.add(menuItems[i]);
		}
		add(new JPanel()); //just a dummy for when 
		//setSprite removes the current cellsView 
		setSprite(ps);
		
	}
	
	public void select(int index)
	{
		cells[selectedCell].selected = false;
		cells[index].selected = true;
		selectedCell = index;
	}
	
	public void selectLoop(int s, int e)
	{
		System.out.println("CALLED SELECT LOOP: " + s + " " + e);
		for(int i = startLoop; i <= endLoop; i++)
		{
			cells[i].loopSelected = false;
		}
		for(int i = s; i <= e; i++)
		{
			cells[i].loopSelected = true;
		}
		startLoop = s;
		endLoop = e;
		repaint();
	}
	
	public void setSprite(PaintedSprite sp)
	{
		storedSprite = sp;
		System.out.println("Removing " + this.getComponent(0).toString());
		remove(0);
		int nFrames = sp.frames.length;
		System.out.println("Sprite has " + nFrames + " frames");
		
		JPanel cellsView = new JPanel(new GridLayout((nFrames / 5) + 1, 5));
		cells = new CellView[nFrames];
		int j = 0;
		for(int i = 0; i < nFrames; i++)
		{
			cells[i] = new CellView(sp.frames[i].img);
			cellsView.add(cells[i]);
			j++;
			j %= 5;
		}
		for(int i = j; i < 5; i++)
		{
			CellView empty = new CellView(null);
			cellsView.add(empty);
		}
		cellsView.addMouseListener(this);
		
		JScrollPane jsp = new JScrollPane(cellsView);
		//int sbw = jsp.getVerticalScrollBar().getWidth();
		jsp.setPreferredSize(new Dimension(64*5 + 22, 64*4));
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.getVerticalScrollBar().setUnitIncrement(64);
		//jsp.add(cellsView);
		add(jsp);
		//cellsView.repaint()
		validate();
		
		/*try{
			throw new Exception();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}*/
		//local.update();
	}
	
	private int find(int x, int y)
	{
		return 5 * (y / 64) + (x / 64);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
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

	@Override
	public void mousePressed(MouseEvent m) 
	{
		// TODO Auto-generated method stub
		if(m.getButton() == MouseEvent.BUTTON1)
		{
			//deselect everything previously selected
			
			//select the cell
			int cellNum = find(m.getX(), m.getY());
			select(cellNum);
			
			//loop select the loop it's in
			PaintedSprite.LoopProperties[] lps = storedSprite.loopProperties;
			for(int i = 0; i < lps.length; i++)
			{
				if(lps[i].startFrame >= cellNum && lps[i].endFrame <= cellNum)
				{
					selectLoop(lps[i].startFrame, lps[i].endFrame);
				}
					
			}
		}
		else if(m.getButton() == MouseEvent.BUTTON3)
		{
			
			rightClickMenu.show(this, m.getX(), m.getY());
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(openFrame != null)
			openFrame.setVisible(false);
		for(int i = 0; i < menuItems.length; i++)
		{
			if(e.getSource() == menuItems[i])
			{
				operation = i;
				switch(i)
				{
				case 0:  //copy frames (Select Frames)
					openFrame = new OkCancelFrame("Copy Frames", 
							new FrameSelectionPanel(selectedCell, startLoop, endLoop, storedSprite.frames.length - 1), this, null);
					openFrame.setLocation(400,400);
					openFrame.setVisible(true);
					break;
				case 1:  //add loop    (New Loop)
					openFrame = new OkCancelFrame("New Loop", storedSprite.new LoopProperties(), this, this);
					openFrame.setLocation(400,400);
					openFrame.setVisible(true);
					break;
				case 2:  //add frame   (Add Frame)
					openFrame = new OkCancelFrame("Add Frame", storedSprite.new FrameProperties(), this, this);
					openFrame.setLocation(400,400);
					openFrame.setVisible(true);
					break;
				case 3:  //remove loop
					
					break;
				case 4:  //remove frame
					break;
				}
				return;
			}
		}
		//othewise the actionPerformed is by a PropertyGenerator somewhere
		local.update();
	}
	
	public void processData(String[] data)
	{
		switch(operation)
		{
		case 0:  //copy frames
			int a = Integer.parseInt(data[0]);
			int b = Integer.parseInt(data[1]);
			System.out.println("Copying frames " + a + " to " + b);
			System.out.println("SS Frame count = " + storedSprite.frames.length);
			Image[] imgs = new Image[b - a + 1];
			PaintedSprite.FrameProperties[] fps = new PaintedSprite.FrameProperties[b - a + 1];
			for(int i = 0; i <= (b - a); i++)
			{
				imgs[i] = storedSprite.frames[a + i].img;
				fps[i] = storedSprite.new FrameProperties();
			}
			Vector v = Diorama.arrayInsert(storedSprite.frames, imgs, -1);
			storedSprite.frames = (SpriteFrame[]) v.toArray(new SpriteFrame[v.size()]);
			v =  Diorama.arrayInsert(storedSprite.frameProperties, fps, -1);
			storedSprite.frameProperties = (PaintedSprite.FrameProperties[]) 
				v.toArray(new PaintedSprite.FrameProperties[v.size()]);
			
			setSprite(storedSprite);
			//System.out.println("SS Frame count = " + storedSprite.frames.length);
			repaint();
			break;
		case 1:  //add loop
			PaintedSprite.LoopProperties newLoop = storedSprite.new LoopProperties();
			newLoop.setPropertyValues(data);
			v = Diorama.arrayInsert(storedSprite.loopProperties, newLoop, -1);
			storedSprite.loopProperties = (PaintedSprite.LoopProperties[])
				v.toArray(new PaintedSprite.LoopProperties[v.size()]);
			//System.out.println("SS Loop count = " + storedSprite.loopProperties.length);
			this.selectLoop(newLoop.startFrame, newLoop.endFrame);
			System.out.println("LOOP:" + startLoop + " " + endLoop);
			local.setSelectedLoop(storedSprite.loopProperties.length - 1);
			//setSprite(storedSprite);
			//System.out.println("REPAINT NOW!");
			repaint();
			validate();
			//repaint();
			//validate();
			break;
		case 2:  //add frame
			String fname = data[0];
			Image img = Diorama.loadImage(fname);
			PaintedSprite.FrameProperties fp = storedSprite.new FrameProperties();
			v = Diorama.arrayInsert(storedSprite.frames, img, -1);
			storedSprite.frames = (SpriteFrame[]) v.toArray(new SpriteFrame[v.size()]);
			v = Diorama.arrayInsert(storedSprite.frameProperties, fp, -1);
			storedSprite.frameProperties = (PaintedSprite.FrameProperties[])
				v.toArray(new PaintedSprite.FrameProperties[v.size()]);
			setSprite(storedSprite);
			repaint();
			break;
		case 3:
			break;
		case 4:
			break;
		}
		
	}
}

class FrameSelectionPanel extends JPanel implements PropertyGenerator, ActionListener, ChangeListener
{
	JRadioButton[] threes;
	JSpinner start;
	JSpinner end;
	int[] selection;
	ButtonGroup bg;
	boolean otherSelected;
	int btnSelected;
	
	FrameSelectionPanel(int sel, int loopStart, int loopEnd, int maxFrame)
	{
		String[] x = {"Selected Frame", "Selected Loop", "Other"};
		threes = new JRadioButton[3];
		selection = new int[5];
		bg = new ButtonGroup();
		for(int i = 0; i < 3; i++)
		{
			threes[i] = new JRadioButton(x[i]);
			threes[i].addActionListener(this);
			bg.add(threes[i]);
		}
		threes[0].setSelected(true);
		selection[0] = sel;
		selection[1] = loopStart;
		selection[2] = loopEnd;
		selection[3] = loopStart;
		selection[4] = loopEnd;
		
		SpinnerNumberModel nm = new SpinnerNumberModel(sel, 0, maxFrame, 1);
		SpinnerNumberModel nm2 = new SpinnerNumberModel(sel, 0, maxFrame, 1);
		
		start = new JSpinner(nm);
		start.addChangeListener(this);
		//start.setEnabled(false);
		end = new JSpinner(nm2);
		end.addChangeListener(this);
		//end.setEnabled(false);
		
		otherSelected = false;
		
		JPanel last = new JPanel(new GridLayout(1,3));
		last.add(threes[2]);
		last.add(start);
		//last.add(new JLabel("to"));
		last.add(end);
		
		setLayout(new GridLayout(3,1));
		add(threes[0]);
		add(threes[1]);
		add(last);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		otherSelected = false;
		for(int i = 0; i < 3; i++)
		{
			if(e.getSource() == threes[i])
			{
				btnSelected = i;
				switch(i)
				{
				case 0:
					start.setValue(new Integer(selection[0]));
					end.setValue(new Integer(selection[0]));
					break;
				case 1:
					start.setValue(new Integer(selection[1]));
					end.setValue(new Integer(selection[2]));
					break;
				case 2:
					start.setValue(new Integer(selection[3]));
					end.setValue(new Integer(selection[4]));
					otherSelected = true;
					break;
				}
			}
		}
		//start.setEnabled(otherSelected);
		//end.setEnabled(otherSelected);
		
	}
	
	

	@Override
	public void stateChanged(ChangeEvent ev) 
	{
		// TODO Auto-generated method stub
		if(otherSelected)
		{
			selection[3] = (Integer) start.getValue();
			//System.out.println(start.getValue());
			selection[4] = (Integer) end.getValue();
			//System.out.println(end.getValue());
		}
		else
		{
			switch(btnSelected)
			{
			case 0:
				start.setValue(new Integer(selection[0]));
				end.setValue(new Integer(selection[0]));
				break;
			case 1:
				start.setValue(new Integer(selection[1]));
				end.setValue(new Integer(selection[2]));
				break;
			}
		}
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyValues() 
	{
		// TODO Auto-generated method stub
		//System.out.println("Got property values");
		String[] rtnVal = new String[2];
		switch(btnSelected)
		{
		case 0:
			rtnVal[0] = String.valueOf(selection[0]);
			rtnVal[1] = String.valueOf(selection[0]);
			break;
		case 1:
			rtnVal[0] = String.valueOf(selection[1]);
			rtnVal[1] = String.valueOf(selection[2]);
			break;
		case 2:
		   if(selection[3] > selection[4])
		   {
		   	rtnVal[0] = String.valueOf(selection[4]);
		   	rtnVal[1] = String.valueOf(selection[3]);
		   }
		   else
		   {
		   	rtnVal[0] = String.valueOf(selection[3]);
		   	rtnVal[1] = String.valueOf(selection[4]);
		   }
			break;
		}
		
		return rtnVal;
	}

	@Override
	public void setPropertyValues(String[] in) {
		// TODO Auto-generated method stub
		
	}
	
	
}

class OkCancelFrame extends JFrame implements ActionListener
{
	JButton ok;
	JButton cancel;
	FramePopupUser fpu;
	PropertyGenerator local;
	
	OkCancelFrame(String title, PropertyGenerator pg, FramePopupUser fpu, ActionListener al)
	{
		JPanel south = new JPanel(new FlowLayout());
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		south.add(ok);
		south.add(cancel);
		
		this.fpu = fpu;
		
		local = pg;
		if(pg instanceof JPanel)
		{
			JPanel pan = (JPanel) pg;
			getContentPane().add(pan, BorderLayout.CENTER);
		}
		else
		{
			PropertyEditor pe = new PropertyEditor(pg);
			getContentPane().add(pe, BorderLayout.CENTER);
		}
		getContentPane().add(south, BorderLayout.SOUTH);
		setSize(300,200);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		//System.out.println("AA");
		if(e.getSource() == ok)
		{
			//System.out.println("AAAA");
			fpu.processData(local.getPropertyValues());
		}
		setVisible(false);
	}
}

interface FramePopupUser
{
	public void processData(String[] data);
}


class SpriteEditor extends JFrame 
{
	Diorama dia;
	
	StatesView statesView;
	LoopEditor loopView;
	FrameEditor frameView;
	TestView testView;
	
	int currSelection;
	boolean loopLocked;
	boolean spriteLocked;
	
	SpriteEditor(Diorama d, int spriteIndex)
	{
		dia = d;
		//int idx = spriteIndex;
		
		/*if(d.images[idx].isSprite() == false)
		{
			int[] sprites = d.getSpriteIndeces();
			spriteIndex = sprites[0];
		}*/
		spriteLocked = true;
		PaintedSprite ps = (PaintedSprite) dia.getAllItemsOfType("PaintedSprite")[spriteIndex];
		currSelection = spriteIndex;
		statesView = new StatesView(d, spriteIndex, this);
		loopView = new LoopEditor(ps, this);
		testView = new TestView(ps);
		frameView = new FrameEditor(ps, this);
		
		JPanel main = new JPanel(new GridLayout(2,2));
		main.add(statesView);
		main.add(testView);
		main.add(loopView);
		main.add(frameView);
		
		getContentPane().add(main);
		setSize(800, 600);
		setLocation(100, 50);
		
		setVisible(true);
		spriteLocked = false;
	}
	
	public void getSelectedSprite()
	{
		
	}
	
	public void setSelectedSprite(int idx)
	{
		if(spriteLocked)  //do not call this during the constructor
			return;
		System.out.println("Calling setSelectedSprite");
		PaintedSprite[] lst = dia.getAllItemsOfType("PaintedSprite");
		PaintedSprite ps = lst[idx];
		currSelection = idx;
		loopView.setSprite(ps);
		frameView.setSprite(ps);
		update();
	}
	
	public void setSelectedLoop(int idx)  //loop selection will do this
	{
		if(loopLocked) //do not call this recursively
			return;
		loopLocked = true;
		System.out.println("Updating loop #" + idx);
		//note that if loop is -1 we are just updating the loop's properties
		statesView.setSelectedLoop(idx);
		if(idx != -1)
		{
			loopView.setLoop(idx);
			PaintedSprite ps = (PaintedSprite) dia.getAllItemsOfType("PaintedSprite")[currSelection];
			PaintedSprite.LoopProperties sLoop = ps.loopProperties[idx];
			System.out.println(sLoop.startFrame);
			System.out.println(sLoop.endFrame);
			int s = sLoop.startFrame;
			int e = sLoop.endFrame;
			
			frameView.selectLoop(s, e);
		}
		update();
		loopLocked = false;
	}
	
	public void setSelectedFrame(int idx)  //frame selection will do this
	{
		loopView.setFrame(idx);
		update();
	}
	
	public void update()
	{
		System.out.println("Now updataing sprite editor");
		//statesView.validate();
		//loopView.validate();
		//testView.validate();
		//frameView.validate();
		validate();
	}
}

public class SpriteEditorDemo
{
	public static void main(String[] args)
	{
		System.out.println(System.getProperty("user.dir"));
		Diorama d = DioramaDemo.getTestDiorama();
		String[] img = new String[10];
		for(int i = 0; i < 10; i++)
			img[i] = "C:\\Users\\Aaron\\Desktop\\Wizard1\\Txt_" + i + ".gif";
		d.addSprite(img, 0, 0, 0, 0, 0, false);
		
		SpriteEditor se = new SpriteEditor(d, 0);
	}
}