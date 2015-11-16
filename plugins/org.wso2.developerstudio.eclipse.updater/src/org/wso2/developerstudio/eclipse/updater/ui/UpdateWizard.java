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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.wso2.developerstudio.eclipse.updater.core.UpdateManager;

public class UpdateWizard {

	protected UpdateManager updateManager;
	
	public UpdateWizard() {
	}
	
	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	public void checkForUpdates(){
		IProgressService progressService = PlatformUI.getWorkbench()
				.getProgressService();
		try {
			progressService.runInUI(progressService,
					new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							try {
								updateManager = new UpdateManager();
//								try {
//									//updateManager.downloadWSO2FeatureJars();
//								} catch (ProvisionException
//										| URISyntaxException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
								
								updateManager.checkForUpdates(monitor);	
								if (updateManager.hasNothingToUpdate()) {
									MessageDialog
											.openInformation(
													Display.getDefault()
															.getActiveShell(),
													"WSO2 Developer Studio Update Manager",
													"No updates found");
								}
								showAvailableUpdates();
							} catch (OperationCanceledException e) {
								showOperationCancelledError();
							}

						}

					}, ResourcesPlugin.getWorkspace().getRoot());
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void showAvailableUpdates(){
		LoadMetadataRepositoryJob job = new LoadMetadataRepositoryJob(ProvisioningUI.getDefaultUI());
		//ProvisioningUI.getDefaultUI().
		ProvisioningUI.getDefaultUI().openUpdateWizard(false, updateManager.getOperation(), job);
		
	}
	
	protected void showOperationCancelledError() {
		MessageDialog
		.openError(
				Display.getDefault()
						.getActiveShell(),
				"WSO2 Developer Studio Update Manager",
				"Error while updating WSO2 Developer Studio. User cancelled the operation.");
	}
}
