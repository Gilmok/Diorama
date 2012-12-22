import java.io.Serializable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class MouseIO
{
	public static final int CLICK = 1;
	public static final int PRESS = 2;
	public static final int RELEASE = 3;
	public static final int DRAGTO = 4;
	public static final int ROLLON = 5;
	public static final int ROLLOFF = 6;
	public static final int RBUTTON = 256;
}

abstract class IOHandler implements Serializable
{	
	Interactive iv;
	boolean hasRollover;
	String name;
	
	public IOHandler()
	{
		
	}
	
	public void setInteractive(Interactive in)
	{
		iv = in;
	}
	
	public abstract void handleMouse(int code, int x, int y, Diorama d);
	
	public abstract void kbPress(int code, Diorama d);
	
	public abstract void kbRelease(int code, Diorama d);
}

class IOEditor extends JFrame implements ActionListener
{
	JTabbedPane mainPane;
	JTabbedPane mouseLeft;
	JTabbedPane mouseRight;
	JTabbedPane kbPress;
	JTabbedPane kbRelease;
	JButton saveButton;
	JTextField ioName;
	JTextArea[] ioText;
	JScrollPane[] textScroll;
	//String[] ioCode;
	
	public IOEditor()
	{
		//ioCode = new String[15];
		ioText = new JTextArea[14];
		
		textScroll = new JScrollPane[14];
		for(int i = 0; i < ioText.length; i++)
		{
			ioText[i] = new JTextArea();
			ioText[i].setTabSize(3);
			ioText[i].setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			textScroll[i] = new JScrollPane(ioText[i]);
		}
		ioName = new JTextField(30);
		mainPane = new JTabbedPane();
		mouseLeft = new JTabbedPane();
		mouseRight = new JTabbedPane();
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		//kbPress = new JTabbedPane();
		//kbRelease = new JTabbedPane();
		
		String[] a1 = {"Mosue - Left", "Mouse - Right", "Key Press", "Key Release"};
		mainPane.add(mouseLeft, a1[0]);
		mainPane.add(mouseRight, a1[1]);
		mainPane.add(textScroll[12], a1[2]);
		mainPane.add(textScroll[13], a1[3]);
		
		String[] a2 = {"Click", "Press" , "Release", "Drag", "RollOn", "RollOff"};
		for(int i = 0; i < a2.length; i++)
		{
			mouseLeft.add(textScroll[i], a2[i]);
			mouseRight.add(textScroll[i + a2.length], a2[i]);
		}
		
		JPanel top = new JPanel(new FlowLayout());
		top.add(new Label("Handler name:"));
		top.add(ioName);
		top.add(saveButton);
		
		setSize(1000,400);
		getContentPane().add(top, BorderLayout.NORTH);
		getContentPane().add(mainPane, BorderLayout.CENTER);
		
		setVisible(true);
		//mainPane.add()
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String s = getSaveContents();
	}
	
	private String getSaveContents()
	{
		String fname = "IO_" + ioName.getText();
		String output = "class " + fname + "extends IOHandler\n{\n   ";
		boolean hmouseDumped = false;
		//boolean hkbDumped = false;
		for(int i = 0; i < 12; i++)
		{
			String txt = ioText[i].getText();
			if(txt != "")
			{
				if(!hmouseDumped)
				{
					output += "   public void handleMouse(int code, int x, int y, Diorama d)\n   {\n      switch(code)\n";
					hmouseDumped = true;
				}
				
				//int caseNo = i % 6;
				
				String[] mouses = {"MouseIO.CLICK", "MouseIO.PRESS", "MouseIO.RELEASE", "MouseIO.DRAGTO", "MouseIO.ROLLON", "MouseIO.ROLLOFF"};
				output += "      case " + mouses[i % mouses.length];
				if(i > mouses.length)
					output += " + MouseIO.RBUTTON";
				output += ":\n";
				
				String[] textParts = txt.split("\n");
				for(int j = 0; j < textParts.length; j++)
					output += "         " + textParts[j] + "\n";
			}
		}
		if(hmouseDumped)
			output += "   }\n";
		if(ioText[12].getText() != "")
		{
			output += "   kbPress(int code, Diorama d)\n   {\n";
			String txt = ioText[12].getText();
			String[] textParts = txt.split("\n");
			for(int j = 0; j < textParts.length; j++)
				output += "         " + textParts[j] + "\n";
			output += "   }\n";
		}
		if(ioText[13].getText() != "")
		{
			output += "   kbRelease(int code, Diorama d)\n   {\n";
			String txt = ioText[13].getText();
			String[] textParts = txt.split("\n");
			for(int j = 0; j < textParts.length; j++)
				output += "         " + textParts[j] + "\n";
			output += "   }\n";
		}
		return output + "}\n";
	}
	
}

public class IOHandlerTest 
{

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		IOEditor ioe = new IOEditor();
	}

}
