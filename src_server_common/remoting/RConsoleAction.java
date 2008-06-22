package remoting;

import java.util.HashMap;

public class RConsoleAction extends RAction {
	public RConsoleAction() {
	}

	public RConsoleAction(String actionName) {
		super(actionName);
	}

	public RConsoleAction(String actionName, HashMap<String, Object> attributes) {
		super(actionName, attributes);
	}
}
