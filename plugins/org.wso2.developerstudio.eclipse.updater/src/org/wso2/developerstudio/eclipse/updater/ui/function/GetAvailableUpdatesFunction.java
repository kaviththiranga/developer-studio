/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.developerstudio.eclipse.updater.ui.function;

import org.wso2.developerstudio.eclipse.updater.ui.UpdateWindow;
import org.wso2.developerstudio.eclipse.webui.core.util.ScriptFactory;

public class GetAvailableUpdatesFunction extends AbstractUpdateWindowFunction {
	
	public GetAvailableUpdatesFunction(UpdateWindow updateWindow) {
		super(updateWindow, FunctionNames.GET_AVAILABLE_UPDATES_FUNCTION);
	}
	
	@Override
	public Object function(Object[] arguments) {
		String updatesList = null;
		try {
			updatesList = ScriptFactory.pojoToJson(updateWindow.getUpdateManager()
					.getPossibleUpdatesMap());
		} catch (IllegalStateException e) {
			log.error("Error while fetching the list of updates.", e);
			return Boolean.FALSE.toString();
		}
		return updatesList;
	}
}
