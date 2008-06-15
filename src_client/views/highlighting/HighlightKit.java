package views.highlighting;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;

public  class HighlightKit extends StyledEditorKit {
	
    public Document createDefaultDocument()
    {
            return new HighlightDocument(true);
    }
}