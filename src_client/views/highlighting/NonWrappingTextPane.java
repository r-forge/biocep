package views.highlighting;

import java.awt.Component;
import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;

public class NonWrappingTextPane extends JTextPane {
      // The method below is coutesy of Core Swing Advanced Programming by Kim Topley
      //
      // Override getScrollableTracksViewportWidth
      // to preserve the full width of the text
      public boolean getScrollableTracksViewportWidth() {
                Component parent = getParent();
                ComponentUI ui = getUI();
                return parent != null ? (ui.getPreferredSize(this).width <= parent.getSize().width) : true;
    }
}

