package prompto.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

import prompto.core.CoreConstants;

public class ApplicationProjectWizard extends PromptoProjectWizard {
 

	@Override
	protected String getTitle() {
		return CoreConstants.NEW_APPLICATION_PROJECT;
	};
	
	@Override
	protected String getDescription() {
		return CoreConstants.APPLICATION_PROJECT_DESCRIPTION;
	}

	@Override
	protected String getNatureId() {
		return CoreConstants.APPLICATION_NATURE_ID;
	}
	
	@Override
	protected IRunnableWithProgress buildCreateProjectOperation(final IProject project, final IProjectDescription description) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					CreateProjectOperation cpo = new CreateProjectOperation(description, CoreConstants.CREATING_PROJECT);
					cpo.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
					IFile file = project.getFile("SampleApplication.ped");
					CreateFileOperation cfo = new CreateFileOperation(file, null, null /*contents*/, CoreConstants.CREATING_SAMPLE_APPLICATION);
					cfo.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (ExecutionException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
	}

}