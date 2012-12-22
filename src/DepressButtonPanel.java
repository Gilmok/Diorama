import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.UIManager.*;

import org.w3c.dom.Element;


class PlacerSelection extends JPanel implements ActionListener, MouseListener
{
	JToggleButton[] tbs;
	int[] indeces;
	String[][] menuItems;
	ButtonGroup grp;
	JPopupMenu menu;
	String activeOption;
	int currButton;
	//String[] buttonMenuItems;
	
	PlacerSelection()
	{
		tbs = new JToggleButton[0];
		menuItems = null;
		menu = new JPopupMenu();
		grp = new ButtonGroup();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		//Integer.parseInt(e.getActionCommand());
		System.out.println("AC:|" + e.getActionCommand() + "|");
		System.out.println(e.getSource());
		
		String act = e.getActionCommand();
		String[] parts = act.split(":");
		if(parts.length == 1)
		{
			JToggleButton jtb = (JToggleButton) e.getSource();
			currButton = Integer.parseInt(act);
			if(currButton != -1)
			{	
				activeOption = menuItems[currButton][indeces[currButton]];
				System.out.println("AO:" + activeOption);
			}
			else
			{
				activeOption = "-1";
				System.out.println("AO:" + activeOption);
			}
		}
		else
		{
			
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			//String btnNewName = x + ":" + parts[1];
			//objToPlace = parts[1];  //add this part in
			//btnNewName = e.getActionCommand();
			tbs[x].setText(parts[2]);
			activeOption = parts[2];
			indeces[currButton] = y;
			System.out.println("AO: " + activeOption);
			repaint();
		}
	}
	
	public void setButtonList(String[][] buttons, String[] toolTips, ImageIcon[] imgData, JToggleButton selectBtn)
	{
		for(int i = 0; i < tbs.length; i++)
		{
			tbs[i].removeActionListener(this);
			grp.remove(tbs[i]);
			
		}
		
		removeAll();
		tbs = null;
		menuItems = buttons;
		int n = buttons.length;
		//nButtons = n;
		tbs = new JToggleButton[n + 1];
		tbs[n] = selectBtn;
		indeces = new int[n];
		setLayout(new GridLayout(2, (n>>1) + 1));
		//JMenu[] allMenus;
		//menuItems = new String[n][];
		for(int i = 0; i < n; i++)
		{
			String[] items = buttons[i];
			//String buttonName = null;
			//JPopupMenu aMenu = new JPopupMenu();
			//menuItems[i] = new String[items.length];
			
			/*for(int j = 0; j < items.length; j++)
			{
				String[] names = items[j].split(":");
				
				
				buttonName = names[1] + ":" + names[2];
			}*/
			
			tbs[i] = new JToggleButton(items[0]);
			
			if(imgData != null)
			{
				tbs[i].setIcon(imgData[i]);
			}
			//tbs[i].add(aMenu);
			tbs[i].setToolTipText(toolTips[i]);
			
			
			tbs[i].addActionListener(this);
			tbs[i].addMouseListener(this);
			tbs[i].setActionCommand(String.valueOf(i));
			
			grp.add(tbs[i]);
			add(tbs[i]);
			
			
		}
		
		grp.add(selectBtn);
		selectBtn.addActionListener(this);
		selectBtn.setActionCommand("-1");
		currButton = -1;
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
	public void mousePressed(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		menu.removeAll();
		//System.out.println(e.getButton());
		if(e.getButton() == MouseEvent.BUTTON3)
		{
			JToggleButton jtb = (JToggleButton) e.getSource();
			for(int i = 0; i < tbs.length; i++)
			{
				if(jtb == tbs[i])
				{
					currButton = i;
					//System.out.println(i + ":" + menuItems[i].length);
					for(int j = 0; j < menuItems[i].length; j++)
					{
						String name = menuItems[i][j];
						JMenuItem itm = new JMenuItem(name);
						itm.addActionListener(this);
						String actCommand = i + ":" + j + ":" + name;
						itm.setActionCommand(actCommand);
						menu.add(itm);
					}
					break;
				}
			}
			menu.show(jtb, e.getX(), e.getY());
		}
	}
	
	

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}

class PlacerWindow extends JFrame implements ActionListener//, WindowFocusListener
{
	JComboBox bx;
	PlacerSelection placer;
	String[] groupNames;
	JToggleButton selector;
	JPanel westPane;
	
	String[][][] groupItems;
	String[][] toolTips;
	ImageIcon[][] icons;
	
	
	//int lastIndex;
	
	
	PlacerWindow()
	{
		String[] myMenus = {"MenuA","MenuB","MenuC"};
		groupNames = myMenus;
		groupItems = null;
		toolTips = null;
		icons = null;
		setupStrings();
		bx = new JComboBox(groupNames);
		bx.addActionListener(this);
		setSize(500,100);
		
		westPane = new JPanel(new GridLayout(1,2));
		selector = new JToggleButton("Select");
		westPane.add(selector);
		westPane.add(bx);
		getContentPane().add(westPane, BorderLayout.WEST);
		
		placer = new PlacerSelection();
		placer.setButtonList(getButtonList(0), toolTips[0], null, selector);
		getContentPane().add(placer, BorderLayout.CENTER);
		//lastIndex = 0;
		setVisible(true);
	}
	
	void setupStrings()
	{
		int[] randoms = {3,6,5};
		groupItems = new String[randoms.length][][];
		toolTips = new String[randoms.length][];
		for(int i = 0; i < groupItems.length; i++)
		{
			groupItems[i] = new String[randoms[i]][];
			toolTips[i] = new String[randoms[i]];
			for(int j = 0; j < groupItems[i].length; j++)
			{
				int random2 = (int) (Math.random() * 5)  + 1;
				groupItems[i][j] = new String[random2];
				for(int k = 0; k < groupItems[i][j].length; k++)
				{
					String str = groupNames[i] + ":";
					str += String.valueOf(j) + ":";
					str += String.valueOf(k);
					groupItems[i][j][k] = str;
				}
				toolTips[i][j] = groupNames[i] + ":" + String.valueOf(j);
			}
		}
	}
	
	public void receiveConfig(String xmlFile, String schemaFile)
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
		
		int n1 = allGroups.length;
		groupNames = new String[n1];
		toolTips = new String[n1][];
		icons = new ImageIcon[n1][];
		groupItems = new String[n1][][];
		
		for(int i = 0; i < allGroups.length; i++)
		{
			//foreach oper
			System.out.println("  " + xp.getAttribute(allGroups[i], "name"));
			
			groupNames[i] = xp.getAttribute(allGroups[i], "name");
			
			Element[] allOps = xp.getSubElements(allGroups[i]);
			int n2 = allOps.length;
			groupItems[i] = new String[n2][];
			icons[i] = new ImageIcon[n2];
			toolTips[i] = new String[n2]; 
			
			
			for(int j = 0; j < allOps.length; j++)
			{
				//try to instantiate the base class
				String baseClass = xp.getAttribute(allOps[j], "baseclass");
				
				
				String imgFile = xp.getAttribute(allOps[j], "imgfile");
				icons[i][j] = new ImageIcon(imgFile);
				
				Object obj = DioramaDemo.testObject(baseClass);
				if(obj == null)
					failures.add(baseClass);
				
				//foreach option
				Element[] allOptions = xp.getSubElements(allOps[j]);
				groupItems[i][j] = new String[allOptions.length];
				toolTips[i][j] = baseClass + " Types (" + allOptions.length + ")";
				
				for(int k = 0; k < allOptions.length; k++)
				{
					String myClass = xp.getAttribute(allOptions[k], "classname");
					groupItems[i][j][k] = myClass;
					
					Object obj2 = DioramaDemo.testObject(myClass);
					if(obj2 == null)
						failures.add(myClass);
				}
			}
		}
		int idx = 0;
		String[][] btns = getButtonList(idx);
		placer.setButtonList(btns, toolTips[idx], icons[idx], selector);
		
		setGroupNames(groupNames);
		
		repaint();
		validate();
		
		//try to instantiate the class
		//if failure, add the class to the list of failures
		int fails = failures.size();
		for(int i = 0; i < fails; i++)
		{
			String badClass = failures.get(i);
			System.out.println("Failed to instantiate a " + badClass);
		}
	}
	
	
	
	public void setGroupNames(String[] in)
	{
		groupNames = in;
		westPane.remove(bx);
		bx = new JComboBox(groupNames);
		bx.addActionListener(this);
		
		//getContentPane().remove(westPane);
		//westPane = new JPanel(new GridLayout(1,2));
		//westPane.add(selector);
		westPane.add(bx);
		//getContentPane().add(westPane, BorderLayout.WEST);
	}
	
	public void setGroupItems(String[][][] in)
	{
		groupItems = in;
	}
	
	public void setToolTips(String[][] in)
	{
		toolTips = in;
	}
	
	public void setIcons(ImageIcon[][] in)
	{
		icons = in;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		//System.out.println("AAA");
		int idx = bx.getSelectedIndex();
		//if(idx != lastIndex)
		//{
		String[][] btns = getButtonList(idx);
		placer.setButtonList(btns, toolTips[idx], icons[idx], selector);
		repaint();
		validate();
			//lastIndex = idx;
		//}
	}
	
	private String[][] getButtonList(int idx)
	{
		/*String[] btns = new String[groupItems[idx].length];
		for(int i = 0; i < groupItems[idx].length; i++)
		{
			btns[i] = String.valueOf(i);
		}
		return btns;*/
		
		return groupItems[idx];
	}
	
	public String getActiveOption()
	{
		return placer.activeOption;
	}

}

public class DepressButtonPanel 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			LookAndFeelInfo[] laffs = UIManager.getInstalledLookAndFeels();
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			for(int i = 0; i < laffs.length; i++)
			{
				System.out.println(laffs[i].getClassName());
				
			}
			System.out.println("---------------");
			System.out.println(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.out.println("Failed to set system L&F");
		}
		PlacerWindow bf = new PlacerWindow();
		PlacerWindow bf2 = new PlacerWindow();
	}

}
