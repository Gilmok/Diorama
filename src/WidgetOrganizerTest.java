import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.w3c.dom.Element;

class WidgetOrganizer extends JFrame implements ActionListener
{
	String xmlFile;
	
	XmlProcessor xp;
	
	JPanel categories;
	JButton[] catBtns;
	JScrollPane categoriessp;
	JPanel list;
	JButton[] listBtns;
	JScrollPane listsp;
	JPanel free;
	JScrollPane freesp;
	JPanel addDeleteSave;
	JButton[] addDeleteSaveBtns;
	JTextField jtf;
	
	final int WIDTH = 10;
	
	public WidgetOrganizer(XmlProcessor xp)
	{
		this.xp = xp;
		init(0);
		setSize(700, 700);
		setVisible(true);
	}
	
	public WidgetOrganizer(String xmlFile, String schema)
	{
		XmlProcessor xp = new XmlProcessor(xmlFile, schema);
		xp.initXml();
		if(xp.xml == null)
		{
			//spit out an error and return
			return;
		}
		this.xp = xp;
		init(0);
		
		
		
		setSize(700, 700);
		setVisible(true);
	}
	
	private void init(int catNo)
	{
		String[] cats = loadCategories();
		catBtns = new JButton[cats.length];
		categories = new JPanel(new GridLayout(WIDTH, cats.length / WIDTH));
		for(int i = 0; i < cats.length; i++)
		{
			catBtns[i] = new JButton(cats[i]);
			catBtns[i].addActionListener(this);
			categories.add(catBtns[i]);
		}
		
		String[] items = loadItems(catNo);
		listBtns = new JButton[items.length];
		list = new JPanel(new GridLayout(WIDTH, items.length / WIDTH));
		for(int i = 0; i < items.length; i++)
		{
			listBtns[i] = new JButton(items[i]);
			listBtns[i].addActionListener(this);
			list.add(listBtns[i]);
		}
		
		addDeleteSave = new JPanel(new GridLayout(1, 4));
		jtf = new JTextField();
		addDeleteSave.add(jtf);
		
		String[] btnNames = {"Add Category", "Add Item", "Delete", "Save"};
		addDeleteSaveBtns = new JButton[btnNames.length];
		for(int i = 0; i < btnNames.length; i++)
		{
			addDeleteSaveBtns[i] = new JButton(btnNames[i]);
			addDeleteSaveBtns[i].setActionCommand(btnNames[i]);
			addDeleteSaveBtns[i].addActionListener(this);
			addDeleteSave.add(addDeleteSaveBtns[i]);
		}
		
		getContentPane().add(addDeleteSave, BorderLayout.NORTH);
		
		JPanel temp = new JPanel(new GridLayout(1,2));
		temp.add(categories);
		temp.add(list);
		
		JPanel free2 = new JPanel(new BorderLayout());
		free = new JPanel(new GridLayout(1,50));
		free2.add(new JLabel("Free:"), BorderLayout.WEST);
		free2.add(free, BorderLayout.CENTER);
		
		getContentPane().add(addDeleteSave, BorderLayout.NORTH);
		getContentPane().add(temp, BorderLayout.CENTER);
		getContentPane().add(free2, BorderLayout.SOUTH);
		
		validate();
		repaint();
	}
	
	public String[] loadCategories()
	{
		Element top = xp.getTopElement();
		Element[] all = xp.getSubElements(top);
		String[] rtnVal = new String[all.length];
		for(int i = 0; i < all.length; i++)
			rtnVal[i] = all[i].getAttribute("name");
		return rtnVal;
	}
	
	public String[] loadItems(int catNo)
	{
		Element top = xp.getTopElement();
		Element[] all = xp.getSubElements(top);
		top = all[catNo];
		all = xp.getSubElements(top);
		String[] rtnVal = new String[all.length];
		for(int i = 0; i < all.length; i++)
			rtnVal[i] = all[i].getAttribute("baseclass");
		return rtnVal;
	}
	
	public void categoryAdd(String cToAdd)
	{
		
	}
	
	public void itemAdd(String iToAdd)
	{
		Object obj = DioramaDemo.testObject(iToAdd);
		if(obj instanceof Exception)
		{
			Exception ex = (Exception) obj;
			if(ex instanceof java.lang.NoSuchMethodException)
				JOptionPane.showMessageDialog(this, "Failed to find empty public constructor for " + iToAdd);
			else
				JOptionPane.showMessageDialog(this, "Failed to find class named " + iToAdd);
		}
		else
		{
			free.add(new JButton(iToAdd));
			validate();
			repaint();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("Add Item"))
		{
			itemAdd(this.jtf.getText());
		}
	}
	
}

public class WidgetOrganizerTest 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		XmlProcessor xp = new XmlProcessor("GroupList.xml", "GroupXSD.xsd");
		xp.initXml();
		if(xp.xml != null)
		{
			WidgetOrganizer wo = new WidgetOrganizer(xp);
		}
		
	}

}
