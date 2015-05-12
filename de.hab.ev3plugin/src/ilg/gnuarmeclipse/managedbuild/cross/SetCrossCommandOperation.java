/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *     Marc-Andre Laperle - Moved to an operation for a custom wizard page
 *     Liviu Ionescu - ARM version
 *******************************************************************************/

package ilg.gnuarmeclipse.managedbuild.cross;
import de.hab.ev3plugin.util.Gui;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * An operation that runs when the new project wizard finishes for the Cross GCC
 * toolchain. It reuses the information from {@link SetCrossCommandWizardPage}
 * to store options (index and path) in persistent storage.
 * 
 */
public class SetCrossCommandOperation implements IRunnableWithProgress {

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {

		// System.out.println("SetCrossCommandOperation.run() begin");

		// get local properties
		String projectName = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_PROJECT_NAME);

		String toolchainName = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_TOOLCHAIN_NAME);
		String path = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_TOOLCHAIN_PATH);
/* EV3 SPECIFIC */		
		String uploader = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_TOOLCHAIN_UPLOADER);
/* END EV3 SPECIFIC */
		// store them on the permanent storage in
		// workspace/.plugins/org.eclipse.cdt.core/shareddefaults.xml

		SharedStorage.putToolchainPath(toolchainName, path);
		SharedStorage.putToolchainUploader(toolchainName, uploader);
		SharedStorage.putToolchainName(toolchainName);
		SharedStorage.update();
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		if (!project.exists())
			return;

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null)
			return;

		IConfiguration[] configs = buildInfo.getManagedProject()
				.getConfigurations();

		for (IConfiguration config : configs) {

			try {
				updateOptions(config);
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ManagedBuildManager.saveBuildInfo(project, true);

		if (true) {
			for (IConfiguration config : configs) {
				ScannerConfigBuilder.build(config,
						ScannerConfigBuilder.PERFORM_CORE_UPDATE,
						new NullProgressMonitor());
			}
		}

		// System.out.println("SetCrossCommandOperation.run() end");

	}

	private void updateOptions(IConfiguration config) throws BuildException {

		String sToolchainName = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_TOOLCHAIN_NAME);

		int toolchainIndex;
		try {
			toolchainIndex = ToolchainDefinition
					.findToolchainByName(sToolchainName);
		} catch (IndexOutOfBoundsException e) {
			toolchainIndex = ToolchainDefinition.getDefault();
		}
	IToolChain toolchain = config.getToolChain();
		{
		IOption option;
		ToolchainSettingsTab.setOptionsForToolchain(config, toolchainIndex);

		String path = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_TOOLCHAIN_PATH);
		option = toolchain
				.getOptionBySuperClassId(Option.OPTION_TOOLCHAIN_PATH); //$NON-NLS-1$
		// Do not use config.setOption() to DO NOT save it on .cproject...
		option.setValue(path);

		// ... instead save it to the workspace project storage
		ProjectStorage.putPath(config, path);
		}

		/* EV3/uploader Specific */
		{
		IOption option;
		ToolchainSettingsTab.setOptionsForToolchain(config, toolchainIndex);

		String path = (String) MBSCustomPageManager.getPageProperty(
				SetCrossCommandWizardPage.PAGE_ID,
				SetCrossCommandWizardPage.CROSS_TOOLCHAIN_UPLOADER);
		option = toolchain
				.getOptionBySuperClassId(Option.OPTION_TOOLCHAIN_UPLOADER); //$NON-NLS-1$
		// Do not use config.setOption() to DO NOT save it on .cproject...
		option.setValue(path);
		// ... instead save it to the workspace project storage
		ProjectStorage.putValue(config, "uploader", path);
		}
		
	}


}
