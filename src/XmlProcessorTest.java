import java.io.*;
import javax.xml.*;
import javax.xml.validation.*;
import javax.xml.transform.dom.*;
import javax.xml.parsers.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.dom.*;


class XmlProcessor
{
	String srcXml;
	String srcSchema;
	Document xml;
	
	XmlProcessor(String src, String schema)
	{
		srcXml = src;
		srcSchema = schema;
		xml = null;
	}
	
	public void initXml()
	{
	// parse an XML document into a DOM tree
		int error = 0;
		try
	   {
			DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			DocumentBuilder parser = parserFactory.newDocumentBuilder();
			Document document = parser.parse(new File(srcXml));

			// create a SchemaFactory capable of understanding WXS schemas
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// load a WXS schema, represented by a Schema instance
			File f = new File(srcSchema);
	    	
	   	Schema schema = factory.newSchema(f);
	   	error++;
	   	// create a Validator instance, which can be used to validate an instance document
	   	Validator validator = schema.newValidator();

	   	// validate the DOM tree
	      validator.validate(new DOMSource(document));
	      xml = document;
	   } 
		catch (ParserConfigurationException pce)
		{
			System.out.println("Failed to configure the parser.");
			//return null;
		}
	   catch (SAXException e) 
	   {
	   	if(error == 0)
	   	{
	   		System.out.println("Source schema is not a valid schema file");
	   		System.out.println(e.getMessage());
	   	}
	   	else
	   	{
	   		System.out.println(srcXml + " failed to validate according to " + srcSchema);
	   		System.out.println(e.getMessage());
	   	}
	        // instance document is invalid!
	   	//return null;
	   } 
	   catch (IOException ioe) 
	   {
	   	System.out.println(ioe.getMessage());
	   	//return null;
	   }

	}
	
	public Element getTopElement()
	{
		return xml.getDocumentElement();
	}
	
	public Element[] getSubElements(Element el)
	{
		return getSubElements(el, "*");
	}
	
	public Element[] getSubElements(Element el, String withName)
	{
		Element[] rtnVal;
		Vector<Element> subNodes = new Vector<Element>();
		NodeList nl = el.getElementsByTagName(withName);
		int len = nl.getLength();
		//
		//System.out.println("gsels:" + el.getNodeName());
		for(int i = 0; i < len; i++)
		{
			//System.out.println("   gsels:" + nl.item(i).getNodeName());
			//Note that the nodeList contains ALL elements underneath the node, not just ones
			//immediately underneath it, so we must filter out nodes whose parent is not the passed
			//in element
			if(nl.item(i).getParentNode() == el)
			{
				subNodes.add((Element) nl.item(i));
				//System.out.println("   gsels added element " + i);
			}
		}
		rtnVal = new Element[subNodes.size()];
		rtnVal = subNodes.toArray(rtnVal);
		//System.out.println("gsels returned " + rtnVal.length + " items");
		return rtnVal;
	}
	
	public String getAttribute(Element el, String attrib)
	{
		return el.getAttribute(attrib);
	}
	
	public void moveElement(Element el, Element oldParent, Element newParent)
	{
		Element[] eList = getSubElements(oldParent);
		for(int i = 0; i < eList.length; i++)
		{
			if(eList[i] == el)
			{
				oldParent.removeChild(el);
				newParent.appendChild(el);
				break;
			}
		}
		System.out.println("Failed to move " + el.getNodeName() + " element");
	}
	
	public void setAttrib(Element el, String attrib, String newVal)
	{
		el.setAttribute(attrib, newVal);
	}
}


public class XmlProcessorTest 
{
	public static void main(String args[])
	{
		XmlProcessor xp = new XmlProcessor("GroupList.xml", "GroupXSD.xsd");
		xp.initXml();
		if(xp.xml == null)
			return;
		
		//Element topElement = 
		Element groupElement = xp.getTopElement();
		Element[] allGroups = xp.getSubElements(groupElement);
		System.out.println("Groups (" + allGroups.length + "):");
		for(int i = 0; i < allGroups.length; i++)
		{
			System.out.println("  " + xp.getAttribute(allGroups[i], "name"));
			Element[] allOps = xp.getSubElements(allGroups[i]);
			for(int j = 0; j < allOps.length; j++)
			{
				System.out.println("    Button (" + xp.getAttribute(allOps[j], "baseclass") + ","  +
						             xp.getAttribute(allOps[j], "buttonimg") + ")");
				Element[] allOptions = xp.getSubElements(allOps[j]);
				for(int k = 0; k < allOptions.length; k++)
				{
					System.out.println("      " + xp.getAttribute(allOptions[k], "classname"));
				}
			}
		}
	}
}
