/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.playground.nestor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.3
 *
 */
public class ShowAsProjectAction extends Action {

	private CommonViewer viewer;
	private IProject project;
	private IFolder targetFolder;
	private Object parent;

	public ShowAsProjectAction(IProject project, IFolder targetFolder, CommonViewer viewer) {
		super(NLS.bind(Messages.ShowProjectHere, project.getName()));
		this.project = project;
		this.parent = ((ITreeContentProvider)viewer.getContentProvider()).getParent(project);
		this.targetFolder = targetFolder;
		this.viewer = viewer;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(SharedImages.IMG_OBJ_PROJECT));
	}
	
	public void run() {
		NestedProjectManager.registerProjectShownInFolder(targetFolder, project);
		viewer.refresh(parent);
		viewer.refresh(targetFolder);
		viewer.setSelection(new StructuredSelection(project));
	}
}
