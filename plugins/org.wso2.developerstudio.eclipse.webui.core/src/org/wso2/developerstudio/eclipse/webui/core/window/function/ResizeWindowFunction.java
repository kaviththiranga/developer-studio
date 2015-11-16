package org.wso2.developerstudio.eclipse.webui.core.window.function;

import org.wso2.developerstudio.eclipse.webui.core.window.WebWindow;

public class ResizeWindowFunction extends AbstractWebWindowFunction  {

	public ResizeWindowFunction(WebWindow window) {
		super(window, FunctionNames.RESIZE_SHELL_CALLBACK);
	}
	
	@Override
	public Object function(Object[] arguments) {
		int width = Integer.parseInt((String) arguments[0]);
		int height = Integer.parseInt((String) arguments[1]);
		window.getShell().layout(true);
		window.getShell().setSize(width, height);
		window.getShell().redraw();
		return Boolean.TRUE.toString();
	}

}
