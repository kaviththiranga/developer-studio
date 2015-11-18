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
package org.wso2.developerstudio.eclipse.updater.ui;

import org.wso2.developerstudio.eclipse.updater.core.UpdateManager;
import org.wso2.developerstudio.eclipse.updater.ui.function.GetAvailableUpdatesFunction;
import org.wso2.developerstudio.eclipse.updater.ui.function.SetSelectedUpdatesFunction;
import org.wso2.developerstudio.eclipse.webui.core.window.WebWindow;

public class UpdateWindow extends WebWindow {
	
	protected UpdateManager updateManager;

	public UpdateWindow() throws Exception {
		super("KernelUpdaterUI", "/updater/index.html");
		this.setSize(500, 500);
		updateManager = new UpdateManager();
		updateManager.checkForAvailableUpdates(null);
		//updateManager.checkForAvailableFeatures(null);
		new GetAvailableUpdatesFunction(this);
		new SetSelectedUpdatesFunction(this);
	}

	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
	}

}
