/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.guiSWT;

import javax.swing.*;
import javax.swing.text.html.*;
import java.io.*;
import java.awt.*;
import javax.swing.event.*;

/**
 * The HelpFrame is used to display game help.
 * It reads and shows HTML documents.
 */
public class HelpFrame extends JFrame implements HyperlinkListener
{
	/**
	 * Constructor
	 * @param helpFile name of HTML help file to open.
	 * @throws Exception
	 */
	public HelpFrame(String helpFile) throws Exception
	{
		JEditorPane  textPane = new JEditorPane();
		File f = new File(helpFile);
		java.net.URL url = f.toURL();
		textPane.setPage(url);
	    JScrollPane editorScrollPane = new JScrollPane(textPane);
        editorScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(250, 145));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));
		
		textPane.setEditable(false);
		textPane.addHyperlinkListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(editorScrollPane, BorderLayout.CENTER);
		setTitle("Checkers - Help");
		setSize(600, 400);		
		setVisible(true);
	}	
	

	/**
	 * Responses to Hyperlink activation.
	 * @param e Hyperlink event
	 */
    public void hyperlinkUpdate(HyperlinkEvent e) 
    {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
        {
        	JEditorPane pane = (JEditorPane) e.getSource();
        	if (e instanceof HTMLFrameHyperlinkEvent) 
        	{
        		HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
        		HTMLDocument doc = (HTMLDocument)pane.getDocument();
        		doc.processHTMLFrameHyperlinkEvent(evt);
        	}
        	else 
        	{
        		try 
        		{
        			pane.setPage(e.getURL());
        		}
        		catch (Throwable t) 
        		{
        			t.printStackTrace();
        		}
        	}
        }
     }
	
}
