/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Ivica Loncar - Projects open from inside parent inherit working sets
 ******************************************************************************/
package org.jboss.tools.playground.nestor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkingSet;
import org.jboss.tools.playground.nestor.internal.WorkingSets;

/**
 * @since 3.3
 *
 */
public class NestedProjectManager {

	private static Map<IFolder, IProject> shownAsProject;
	private static Map<IProject, Set<IFolder>> projectToFolders;

	public static void registerProjectShownInFolder(IFolder folder, IProject project) {
		if (shownAsProject == null) {
			init(folder.getWorkspace());
		}
		shownAsProject.put(folder, project);
		if (!projectToFolders.containsKey(project)) {
			projectToFolders.put(project, new HashSet<IFolder>());
		}
		projectToFolders.get(project).add(folder);

		IContainer parent = folder.getParent();
		IProject parentProject = parent.getProject();
		Set<IWorkingSet> parentWorkingSets = WorkingSets.projectWorkingSets(parentProject);
		WorkingSets.addToWorkingSets(project, parentWorkingSets);
	}

	/**
	 * @param workspace
	 */
	private static void init(IWorkspace workspace) {
		shownAsProject = new HashMap<IFolder, IProject>();
		projectToFolders = new HashMap<IProject, Set<IFolder>>();
		workspace.addResourceChangeListener(new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				IProject closedProject = (IProject) event.getResource();
				// projectToFolders.get(closedProject).stream().forEach( p -> unregistedProjectShownInFolder(p) );
				for (IFolder relatedFolder : projectToFolders.get(closedProject)) {
					unregisterProjectShownInFolder(relatedFolder);
				}
			}
		}, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
	}

	public static void unregisterProjectShownInFolder(IFolder targetFolder) {
		if (shownAsProject != null) {
			shownAsProject.remove(targetFolder);
		}
	}

	public static boolean isShownAsProject(IFolder folder) {
		return isOpenProject(folder);
		//return shownAsProject != null && shownAsProject.containsKey(folder);
	}

	public static boolean isShownAsNested(IProject project) {
		return hasOpenParentProject(project);
		//return shownAsProject != null && shownAsProject.containsValue(project);
	}

	/**
	 * Returns {@code true} if and only if there is an open project in the
	 * current workspace having the same location as the given {@code folder}.
	 * <p>
	 * Note that this method loops over all projects in the workspace and
	 * therfore, it is not very performant.
	 *
	 * @param folder
	 *            the folder to decide about
	 * @return {@code true} if the given {@code folder} is an open project
	 */
	private static boolean isOpenProject(IFolder folder) {
		for (IProject project : folder.getWorkspace().getRoot().getProjects()) {
			if (project.getLocation().equals(folder.getLocation()) && project.isOpen()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns {@code true} if and only if there is an open project in the
	 * current workspace at the parent location of the given {@code project}.
	 *
	 * @param project
	 *            the project to decide about
	 * @return {@code true} if and only if there is an open project in the
	 *         current workspace at the parent location of the given
	 *         {@code project}
	 */
	private static boolean hasOpenParentProject(IProject project) {
		IPath location = project.getLocation();
		if (location.segmentCount() > 1) {
			IPath parentPath = location.removeLastSegments(1);
			IContainer parent = project.getWorkspace().getRoot().getContainerForLocation(parentPath);
			if (parent instanceof IFolder) {
				return isOpenProject((IFolder) parent);
			} else if (parent instanceof IProject) {
				return ((IProject) parent).isOpen();
			}
		}
		return false;
	}

}
