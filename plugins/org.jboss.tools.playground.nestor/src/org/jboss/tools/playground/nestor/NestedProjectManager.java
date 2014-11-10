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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;

/**
 * @since 3.3
 *
 */
public class NestedProjectManager implements BundleListener, IPreferenceChangeListener {

	/** The singleton. */
	private static NestedProjectManager INSTANCE;

	/**
	 * @return the lazily created singleton
	 */
	public static NestedProjectManager getDefault() {
		if (INSTANCE == null) {
			INSTANCE = new NestedProjectManager();
		}
		return INSTANCE;
	}

	/** The default value of the preference {@value #NESTING_ENABLED_PREFERENCE_KEY} */
	public static final boolean NESTING_ENABLED_DEFAULT_VALUE = false;

	/** The preference key {@value #NESTING_ENABLED_PREFERENCE_KEY} */
	public static final String NESTING_ENABLED_PREFERENCE_KEY = Activator.PLUGIN_ID + ".nestingEnabled"; //$NON-NLS-1$

	/** @see #isNestingEnabled() */
	private boolean nestingEnabled;

	/** Use {@link #getDefault()} */
	private NestedProjectManager() {
		super();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		this.nestingEnabled = node.getBoolean(NESTING_ENABLED_PREFERENCE_KEY, NESTING_ENABLED_DEFAULT_VALUE);
		node.addPreferenceChangeListener(this);
		BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		bundleContext.addBundleListener(this);
	}

	/**
	 *
	 * (1) If {@link #nestingEnabled} is {@code true}, returns an {@link IProject} having the same location as the given {@code folder}
	 * or {@code null} if no such exists.
	 * <p>
	 * (2) If {@link #nestingEnabled} is {@code false}, returns {@code null}.

	 * @param folder a folder to decide about
	 * @return an {@link IProject} or {@code null}
	 */
	public IProject getProject(IFolder folder) {
		if (folder == null) {
			return null;
		}
		if (nestingEnabled) {
			IPath folderLocation = folder.getLocation();
			// FIXME: performance: this is probably called often enough to cache the folder -> project mapping?
			for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				if (project.getLocation().equals(folderLocation)) {
					return project;
				}
			}
		}
		return null;
	}

	/**
	 * A shorthand for {@code getProject(folder) != null}.
	 *
	 * @param folder
	 * @return {@code true} if project having the same location as {@code folder} exists and nesting is enabled, {@code false} otherwise
	 */
	public boolean isShownAsProject(IFolder folder) {
		return getProject(folder) != null;
	}

	/**
	 * Returns {@code true} if the given {@code project} should be shown nested under its parent folder
	 * in Project Explorer rather than as a top level workspace element.
	 *
	 * @param project
	 * @return
	 */
	public boolean isShownAsNested(IProject project) {
		if (nestingEnabled) {
			IPath queriedLocation = project.getLocation();
			// FIXME: performance: this is probably called often enough to cache the project -> parentProject mapping?
			for (IProject otherProject : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				IPath otherLocation = otherProject.getLocation();
				if (queriedLocation.segmentCount() - otherLocation.segmentCount() >= 1 && otherLocation.isPrefixOf(queriedLocation)) {
					/* otherLocation is ancestor of queriedLocation (but not equal) */
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Does the usual dispose work: on {@link BundleEvent#STOPPING}, removes {@code this} from {@link BundleContext}
	 * listeners and from preference change listeners on node {@link Activator#PLUGIN_ID}.
	 *
	 * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
	 */
	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STOPPING) {
			event.getBundle().getBundleContext().removeBundleListener(this);
			IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			node.removePreferenceChangeListener(this);
		}
	}

	/**
	 * Updates the state of {@link #nestingEnabled} if the related preference was changed.
	 *
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (NESTING_ENABLED_PREFERENCE_KEY.equals(event.getKey())) {
			Object newValue = event.getNewValue();
			if (newValue instanceof String) {
				nestingEnabled = Boolean.parseBoolean((String)newValue);
			} else if (newValue == null) {
				nestingEnabled = NESTING_ENABLED_DEFAULT_VALUE;
			} else {
				throw new IllegalStateException(NESTING_ENABLED_PREFERENCE_KEY +" expected to be String or null; found "+ newValue.getClass().getName()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @return {@code true} if hierarchical rendering of nested projects in Project explorer is enabled and {@code false} otherwise.
	 */
	public boolean isNestingEnabled() {
		return nestingEnabled;
	}
}
