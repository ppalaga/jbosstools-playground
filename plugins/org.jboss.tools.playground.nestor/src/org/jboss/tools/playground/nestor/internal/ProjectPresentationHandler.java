package org.jboss.tools.playground.nestor.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.jboss.tools.playground.nestor.Activator;
import org.jboss.tools.playground.nestor.NestedProjectManager;

public class ProjectPresentationHandler extends AbstractHandler {

	private static final String NEST_PARAMETER = "org.jboss.tools.playground.nestor.projectPresentation.nest"; //$NON-NLS-1$
	private boolean nest = NestedProjectManager.getDefault().isNestingEnabled();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String newNestParam = event.getParameter(NEST_PARAMETER);
		boolean newNest = false;
		if (newNestParam != null) {
			newNest = Boolean.parseBoolean(newNestParam);
		}
		if (newNest != nest) {
			IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			node.putBoolean(NestedProjectManager.NESTING_ENABLED_PREFERENCE_KEY, newNest);
			((ProjectExplorer)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()).getCommonViewer().refresh();
		}
		// TODO refresh selection
		return Boolean.valueOf(nest);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
