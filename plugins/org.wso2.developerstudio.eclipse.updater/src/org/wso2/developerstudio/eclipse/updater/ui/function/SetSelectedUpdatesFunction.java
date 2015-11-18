package org.wso2.developerstudio.eclipse.updater.ui.function;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.wso2.developerstudio.eclipse.updater.model.DevStudioFeature;
import org.wso2.developerstudio.eclipse.updater.ui.UpdateWindow;
import org.wso2.developerstudio.eclipse.webui.core.util.ScriptFactory;

import com.google.gson.reflect.TypeToken;

public class SetSelectedUpdatesFunction extends AbstractUpdateWindowFunction {

	public SetSelectedUpdatesFunction(UpdateWindow updateWindow) {
		super(updateWindow, FunctionNames.SET_SELECTED_UPDATES);
	}
	
	@Override
	public Object function(Object[] arguments) {
		
		String featureListString = (String) arguments[0];
		Type listType = new TypeToken<ArrayList<DevStudioFeature>>() {
        }.getType();
		List<DevStudioFeature> selectedFeatures = ScriptFactory.jsonToPojo(featureListString, listType);
		updateWindow.getUpdateManager().setSelectedUpdates(selectedFeatures);
		updateWindow.getUpdateManager().installSelectedUpdates();
		return Boolean.TRUE.toString();
	}

}
