package views.highlighting;

import java.awt.Color;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import views.Highlighter;

public class HighlightDocument extends DefaultStyledDocument
{
    private Element rootElement;
    
    //private HashMap<String,Color> keywords;
    
    private MutableAttributeSet style;

    private Color commentColor = Color.gray;
    private Pattern singleLineCommentDelimter = Pattern.compile("#");
    private Pattern multiLineCommentDelimiterStart = Pattern.compile("/\\*");
    private Pattern multiLineCommentDelimiterEnd = Pattern.compile("\\*/");

    private boolean _autoUpdate;
    public HighlightDocument(boolean autoUpdate) {
    		_autoUpdate=autoUpdate;
            putProperty( DefaultEditorKit.EndOfLineStringProperty, "\n" );

            rootElement = getDefaultRootElement();

            style = new SimpleAttributeSet();
            
            
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
    {
            super.insertString(offset, str, attr);
            highlightString(Color.black, offset, str.length() , true, false);
            if (_autoUpdate) processChangedLines(offset, str.length());
    }

    public void remove(int offset, int length) throws BadLocationException
    {
            super.remove(offset, length);            
            if (_autoUpdate) processChangedLines(offset, length);
    }

    public void highlightKeywords(String text, Vector<String> v, Color c) {
        for (String keyword : v) {
                Pattern p = Pattern.compile("\\b" + keyword + "\\b");
                Matcher m = p.matcher(text);
                while(m.find()) {
                        highlightString(c, m.start(), keyword.length(), true, true);
                }
        }
    }
    
    public void processChangedLines(int offset, int length) throws BadLocationException
    {
    	
            String text = getText(0, getLength());
            highlightString(Color.black, 0, getLength(), true, false);

            highlightKeywords(text, Highlighter.getInstance().getKeyword1Vector(), Color.red);
            highlightKeywords(text, Highlighter.getInstance().getKeyword2Vector(), Color.blue);
            highlightKeywords(text, Highlighter.getInstance().getKeyword3Vector(), new Color(0,200,0));
                        
            /*
            Matcher mlcStart = multiLineCommentDelimiterStart.matcher(text);
            Matcher mlcEnd = multiLineCommentDelimiterEnd.matcher(text);
            while(mlcStart.find()) {
                    if(mlcEnd.find( mlcStart.end() ))
                            highlightString(commentColor, mlcStart.start(), (mlcEnd.end()-mlcStart.start()), true, true);
                    else
                            highlightString(commentColor, mlcStart.start(), getLength(), true, true);
            }
            */
            
             Matcher slc = singleLineCommentDelimter.matcher(text);
    
            while(slc.find()) {
                    int line = rootElement.getElementIndex(slc.start());
                    int endOffset = rootElement.getElement(line).getEndOffset() - 1;

                    highlightString(commentColor, slc.start(), (endOffset-slc.start()), true, true);
            }
    }
    
    public void highlightString(final Color col,final int begin,final int length,final boolean flag,final boolean bold)
    {
            StyleConstants.setForeground(style, col);
            StyleConstants.setBold(style, bold);            
            if (SwingUtilities.isEventDispatchThread()) {
            	setCharacterAttributes(begin, length, style, flag);
            } else {
            	SwingUtilities.invokeLater(new Runnable(){
            		public void run() {
            			setCharacterAttributes(begin, length, style, flag);            			
            		}
            	});
            }
    }

    //public String getLine(String content, int line)
    //{
    //        Element lineElement = rootElement.getElement( line );
    //        return content.substring(lineElement.getStartOffset(), lineElement.getEndOffset() - 1);
    //}
}


/*
new Thread(new Runnable(){
	public void run() {
		while(true) {
			try {Thread.sleep(1000);} catch (Exception e) {}
			Vector<RAction> actions=popRActions();
			if (actions!=null) {
				try {
					processChangedLines();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}).start();

}


static public Vector<RAction> _modelActions = new Vector<RAction>();
public Vector<RAction> popRActions() {
if (_modelActions.size() == 0)
return null;
Vector<RAction> result = (Vector<RAction>) _modelActions.clone();
for (int i = 0; i < result.size(); ++i)
_modelActions.remove(0);
return result;
}


public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
{
super.insertString(offset, str, attr);
HashMap<String, Object> attributes=new HashMap<String, Object>();
attributes.put("offset", offset);attributes.put("str", str);attributes.put("attr", attr);
_modelActions.add(new RAction("insertString"));
}

public void remove(int offset, int length) throws BadLocationException
{
super.remove(offset, length);            
HashMap<String, Object> attributes=new HashMap<String, Object>();
attributes.put("offset", offset);attributes.put("length", length);
_modelActions.add(new RAction("remove",attributes));
}

public void processChangedLines() throws BadLocationException

*/