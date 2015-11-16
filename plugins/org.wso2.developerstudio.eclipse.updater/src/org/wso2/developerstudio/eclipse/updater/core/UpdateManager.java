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
package org.wso2.developerstudio.eclipse.updater.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.OperationFactory;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.updater.UpdaterPlugin;
import org.wso2.developerstudio.eclipse.updater.model.DevStudioFeature;

public class UpdateManager {

	@Inject
	protected IProvisioningAgentProvider agentProvider;
	protected IProvisioningAgent p2Agent;
	protected UpdateOperation operation;
	protected IArtifactRepositoryManager artifactRepoManager;
	protected IMetadataRepositoryManager metadataRepoManager;
	protected IMetadataRepository metadataRepository;
	protected IArtifactRepository artifactRepository;
	protected Map<String, DevStudioFeature> availableUpdates;
	protected List<DevStudioFeature> availableFeatures;
	
	
	protected static IDeveloperStudioLog log = Logger
			.getLog(UpdaterPlugin.PLUGIN_ID);
	
	public UpdateOperation getOperation() {
		return operation;
	}

	public static final int NO_UPDATES_FOUND = UpdateOperation.STATUS_NOTHING_TO_UPDATE;
	private static final String NOT_RESOLVED_ERROR = "Invalid State: UpdateManager#checkForUpdates should be executed first.";

	public UpdateManager() throws RuntimeException {
		initProvisioningAgent();
	}

	protected void initProvisioningAgent() throws RuntimeException {
		try {
			// Inject references
			BundleContext bundleContext = FrameworkUtil.getBundle(
					UpdateManager.class).getBundleContext();
			IEclipseContext serviceContext = EclipseContextFactory
					.getServiceContext(bundleContext);
			ContextInjectionFactory.inject(this, serviceContext);
			// get p2 agent for current system(Eclipse instance in this
			// case)
			// the location for the currently running system is null (see
			// docs)	
			p2Agent = agentProvider.createAgent(null);
			artifactRepoManager = (IArtifactRepositoryManager) p2Agent.getService(IArtifactRepositoryManager.class.getName());
			metadataRepoManager = (IMetadataRepositoryManager) p2Agent.getService(IMetadataRepositoryManager.class.getName());
			
		} catch (Exception e) {
			throw new RuntimeException("Error while intializing p2 agent.", e);
		}
	}

	protected Map<String, DevStudioFeature> readAvailableFeaturesFromRepo(URI p2Repo,
			IProgressMonitor monitor) throws ProvisionException,
			URISyntaxException, IOException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor progress = SubMonitor.convert(monitor,
				"Checking for DevStudio features.", 5);

		String tmpRoot = System.getProperty("java.io.tmpdir") + File.separator + "DevSUpdaterTmp";
		artifactRepository = artifactRepoManager.loadRepository(p2Repo,
				progress.newChild(1));
		metadataRepository = metadataRepoManager.loadRepository(p2Repo,
				progress.newChild(1));
		
		IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
		IQueryResult<IInstallableUnit> query2 = metadataRepository.query(query,
				progress.newChild(1));
		Collection<IInstallableUnit> wso2features = filterInstallableUnits(
				"org.wso2", "feature.jar", query2, progress.newChild(1));
		
		Map<String, DevStudioFeature> availableFeatures = new HashMap<>();
		
		for (IInstallableUnit iu : wso2features) {
			SubMonitor downloadProgress = SubMonitor.convert(monitor,
					"Downloading feature jars.", wso2features.size() * 2);
			Collection<IArtifactKey> artifacts = iu.getArtifacts();
			// ideally there should be only one artifact in feature.jar iu
			for (IArtifactKey iArtifactKey : artifacts) {
				IArtifactDescriptor[] artifactDescriptors = artifactRepository
						.getArtifactDescriptors(iArtifactKey);
				File cachedFeatureRoot = new File(tmpRoot, iu.getId()
						+ File.separator + iu.getVersion().toString());
				File cachedFeatureDir = new File(cachedFeatureRoot, "extracted");

				if (cachedFeatureDir.exists() && cachedFeatureDir.isDirectory()) {
					downloadProgress.worked(2);
				} else {
					cachedFeatureRoot.mkdirs();
					File jarFile = new File(cachedFeatureRoot, iu.getId());
					try {
						if (jarFile.exists()) {
							// something is wrong with the file
							// if it is there without the cache dir.
							jarFile.delete();
						}
						FileOutputStream fop = new FileOutputStream(jarFile);
						// jar iu only contains a single artifact. hence [0]
						artifactRepository.getArtifact(artifactDescriptors[0],
								fop, downloadProgress.newChild(1));
						cachedFeatureDir.mkdirs();
						extractJar(jarFile, cachedFeatureDir);
					} catch (IOException e) {
						throw new IOException("Error while downloading feature jar.", e);
					}
				}
				DevStudioFeature feature = readDevStudioFeature(iu, cachedFeatureDir);
				availableFeatures.put(feature.getId(), feature);
			}
		}
		return availableFeatures;
	}

	private DevStudioFeature readDevStudioFeature(IInstallableUnit iu,
			File cachedFeatureDir) {
		DevStudioFeature feature = new DevStudioFeature(iu);
		feature.setIconURL(cachedFeatureDir + File.separator + "icon.png");
		try {
			File updateProperties = new File(cachedFeatureDir, "update.properties");
			Properties prop = new Properties();
			InputStream input = new FileInputStream(updateProperties);
			prop.load(input);
			feature.setWhatIsNew(prop.getProperty("whatIsNew"));
			feature.setBugFixes(prop.getProperty("bugFixes"));
		} catch (Exception e) {
			//ignore
			//log.error(e);
		}
		return feature;
	}

	/**
	 * Extracts a jar file to a given directory.
	 * 
	 * @param jar Source jar file.
	 * @param extractpath Destination to extract.
	 * 
	 * @throws IOException
	 */
	private void extractJar(File jar, File extractpath) throws IOException {
		ZipFile zipFile = new ZipFile(jar);
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(extractpath, entry.getName());
				if (entry.isDirectory())
					entryDestination.mkdirs();
				else {
					entryDestination.getParentFile().mkdirs();
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					out.close();
				}
			}
		} finally {
			zipFile.close();
		}
	}
	
	public void checkupdates(){
		try {
			availableUpdates =  readAvailableFeaturesFromRepo(getDevStudioUpdateSite(), new NullProgressMonitor());
		} catch (ProvisionException | URISyntaxException | IOException e) {
			log.error("Error while featching updates", e);
		}
	}

	public void checkForUpdates(IProgressMonitor monitor)
			throws OperationCanceledException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		SubMonitor progress = SubMonitor.convert(monitor,
				"Checking for DevStudio updates.", 2);
		ProvisioningSession session = new ProvisioningSession(p2Agent);
		Collection<IInstallableUnit> installedWSO2Features = getInstalledWSO2Features(progress
				.newChild(1));
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
		progress.subTask("Preparing update operation.");
		URI[] updateSites = new URI[] { getDevStudioUpdateSite() };
		operation = new UpdateOperation(session, installedWSO2Features);
		operation.getProvisioningContext().setArtifactRepositories(updateSites);
		operation.getProvisioningContext().setMetadataRepositories(updateSites);
		operation.resolveModal(progress.newChild(1));
		if (getResolutionResult().getSeverity() == IStatus.CANCEL
				|| progress.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	private URI getDevStudioUpdateSite() {
		URI updateSite = null;
		try {
			updateSite = new URI(
					"http://builder1.us1.wso2.org/~developerstudio/developer-studio-kernel/4.0.0/M1/p2/");
		} catch (URISyntaxException e) {
			log.error(e);
		}
		return updateSite;
	}

	private URI getDevStudioReleaseSite() {
		URI updateSite = null;
		try {
			updateSite = new URI(
					"http://builder1.us1.wso2.org/~developerstudio/developer-studio-kernel/4.0.0/M1/p2/");
		} catch (URISyntaxException e) {
			log.error(e);
		}
		return updateSite;
	}

	public IStatus update(IProgressMonitor monitor)
			throws OperationCanceledException {
		monitor.beginTask("Updating Developer Studio Features", 2);
		ProvisioningJob job = operation
				.getProvisioningJob(new SubProgressMonitor(monitor, 1));
		IStatus status = job.runModal(new SubProgressMonitor(monitor, 1));
		if (status.getSeverity() == IStatus.CANCEL) {
			throw new OperationCanceledException();
		}
		monitor.done();
		return status;
	}

	public Collection<IInstallableUnit> getInstalledWSO2Features(
			IProgressMonitor monitor) throws OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(monitor,"Searching installed WSO2 features.", 2);
//		progress.subTask("Loading current p2 profile.");
//		IProfileRegistry profileRegistry = (IProfileRegistry) p2Agent
//				.getService(IProfileRegistry.SERVICE_NAME);
//		IProfile p2Profile = profileRegistry.getProfile(IProfileRegistry.SELF);
//		progress.worked(1);
//		if (progress.isCanceled()) {
//			throw new OperationCanceledException();
//		}
//		progress.subTask("Fetching the list of installed features.");
//		IQuery<IInstallableUnit> query = QueryUtil.createIUGroupQuery();
//		IQueryResult<IInstallableUnit> queryResult = p2Profile.query(query,
//				progress.newChild(1));
//		if (progress.isCanceled()) {
//			throw new OperationCanceledException();
//		}
//		progress.subTask("Filtering features provided by WSO2.");
		OperationFactory operationFactory = new OperationFactory();
		IQueryResult<IInstallableUnit> queryResult = operationFactory.listInstalledElements(true, progress.newChild(1));
		return filterInstallableUnits("org.wso2", "feature.group", queryResult, progress.newChild(1));
	}

	protected Collection<IInstallableUnit> filterInstallableUnits(String idPrefix, String idSuffix,
			IQueryResult<IInstallableUnit> queryResult, IProgressMonitor monitor)
			throws OperationCanceledException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		Collection<IInstallableUnit> wso2IUs = new ArrayList<IInstallableUnit>();
		Iterator<IInstallableUnit> iterator = queryResult.iterator();
		SubMonitor progress = SubMonitor.convert(monitor,
				"Filtering IUs.", queryResult.toSet()
						.size());
		;
		while (iterator.hasNext()) {
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
			IInstallableUnit iu = iterator.next();
			String versionedID = iu.getId();
			progress.subTask("Analyzing feature: " + versionedID);
			if (versionedID != null && versionedID.startsWith(idPrefix)
					&& versionedID.endsWith(idSuffix)) {
				if (!iu.getArtifacts().isEmpty()) {
					wso2IUs.add(iu);
				}
			}
			progress.worked(1);
		}
		return wso2IUs;
	}
	

	/**
	 * Get the list of all possible updates.  This list may include multiple versions
	 * of updates for the same Feature, as well as patches to the Feature.
	 * 
	 * @return an array of all possible updates
	 */
	public Update[] getPossibleUpdates() throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		}
		return operation.getPossibleUpdates();
	}

	/**
	 * Get the list of latest updates.  This list only includes the latest
	 * update available for each feature.
	 * 
	 * @return the latest updates that are chosen from all of the available updates
	 */
	public Update[] getLatestUpdates(){
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		}
		return operation.getSelectedUpdates();
	}

	public String getResolutionDetails() throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		}
		return operation.getResolutionDetails();
	}

	public String getResolutionDetails(IInstallableUnit iu)
			throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		}
		return operation.getResolutionDetails(iu);
	}

	public boolean hasNothingToUpdate() throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		} else if (getResolutionResult().getCode() == NO_UPDATES_FOUND) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasResolutionErrors() throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		} else if (getResolutionResult().getSeverity() == IStatus.ERROR){
			return true;
		}
		return false;
	}

	public boolean hasResolutionWarnings() throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		} else if (getResolutionResult().getSeverity() == IStatus.WARNING){
			return true;
		}
		return false;
	}

	public IStatus getResolutionResult() throws IllegalStateException {
		if (!operation.hasResolved()) {
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		}
		return operation.getResolutionResult();
	}

	public Map<String, DevStudioFeature> getPossibleUpdatesMap() throws IllegalStateException {
		if(availableUpdates == null){
			throw new IllegalStateException(NOT_RESOLVED_ERROR);
		}
		return availableUpdates;
	}
}
