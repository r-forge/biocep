package org.rosuda.ibase.toolkit;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.JFrame;

import org.rosuda.ibase.Common;
import org.rosuda.util.Global;

/** enhanced {@link JFrame} that uses {@link WinTracker} to keep track of
    open frames.
    @version $Id: TJFrame.java 2879 2008-04-11 17:08:03Z helbig $
*/
public class TJFrame extends JFrame implements FrameDevice
{
    WTentry WTmyself;

    public TJFrame(final String tit, final boolean useCommonBg, final int wclass) {
        if (useCommonBg) setBackground(Common.backgroundColor);
	setTitle(tit);
	
	// add myself to WinTracker
	if (WinTracker.current==null) WinTracker.current=new WinTracker();
	WTmyself=new WTentrySwing(WinTracker.current,this,tit,wclass);
        if (Common.cur_arrow!=null) setCursor(Common.cur_arrow);
    };

    //public TFrame(String tit) { this(tit,true,0); }
    public TJFrame(final String tit,final int wclass) { this(tit,true,wclass); }
    
    public TJFrame() { this("<unnamed>",true,0); }

    public void finalize() {
	if (Global.DEBUG>0)
	    System.out.println("Frame \""+getTitle()+"\" removed.");
	WinTracker.current.rm(WTmyself);
    };
	
	public void dispose() {
		if (Global.DEBUG>0)
			System.out.println("Frame \""+getTitle()+"\" removed.");
		WinTracker.current.rm(WTmyself);
		super.dispose();
	}

    static int lastClass=-1;
    static int lastPlaceX=0, lastPlaceY=0;
    static int lastOffset=0;
    
    public void initPlacement() { // initial frame placement
	if (WTmyself==null) return;
        if (lastClass!=WTmyself.wclass) {
            lastClass=WTmyself.wclass;
            lastPlaceX=getWidth()+10; lastPlaceY=0; lastOffset=0;
        } else {
            setLocation(lastPlaceX,lastPlaceY);
            lastPlaceX+=getWidth()+10;
            Common.getScreenRes();
            if (lastPlaceX+100>Common.screenRes.width) {
                lastPlaceY+=getHeight()+20;
                lastPlaceX=0;
                if (lastPlaceY+100>Common.screenRes.height) {
                    lastOffset+=30;
                    lastPlaceY=lastOffset; lastPlaceX=lastOffset;
                }
            }
        }
    };
    
    public Frame getFrame() {
    	return this;
    }
    
    public Component add(Component c) {
    	return getContentPane().add(c);
    }
	/**
	 * @param work true if working, false if idle.
	 */
	public void setWorking(boolean work) {
		if (work)
			cursorWait();
		else
			cursorDefault();
	}
	
	/**
	 * Show waitcursor (speeningwheel or sandglass).
	 */
	private void cursorWait() {
		Component gp = getRootPane().getGlassPane();
		gp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		gp.setVisible(true);
	}

	/**
	 * Show default cursor.
	 * 
	 */
	private void cursorDefault() {
		Component gp = getRootPane().getGlassPane();
		gp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		gp.setVisible(false);
	}
};
