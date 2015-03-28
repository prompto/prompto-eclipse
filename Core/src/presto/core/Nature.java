package presto.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class Nature implements IProjectNature {
	
	private IProject project;

	@Override
	public void configure() throws CoreException {
		// addToBuildSpec();
	}

	@Override
	public void deconfigure() throws CoreException {
		// removeFromBuildSpec();
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
	
	/*
	protected void addToBuildSpec() throws CoreException {
		IProjectDescription description = this.project.getDescription();
		int scriptCommandIndex = getCommandIndex(description.getBuildSpec());
		if (scriptCommandIndex == -1) {
			// Add a Java command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(Constants.BUILDER_ID);
			setScriptCommand(description, command);
		}
	}

	protected static int getCommandIndex(ICommand[] buildSpec) {
		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(Constants.BUILDER_ID)) {
				return i;
			}
		}
		return -1;
	}

	private void setScriptCommand(IProjectDescription description, ICommand newCommand) throws CoreException {

		ICommand[] oldBuildSpec = description.getBuildSpec();
		int oldScriptCommandIndex = getCommandIndex(oldBuildSpec );
		ICommand[] newCommands;

		if (oldScriptCommandIndex == -1) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldBuildSpec.length + 1];
			System.arraycopy(oldBuildSpec, 0, newCommands, 1, oldBuildSpec.length);
			newCommands[0] = newCommand;
		} else {
			oldBuildSpec[oldScriptCommandIndex] = newCommand;
			newCommands = oldBuildSpec;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		this.project.setDescription(description, null);
	}	
	
	protected void removeFromBuildSpec() throws CoreException {
		IProjectDescription description = this.project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(Constants.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);
				return;
			}
		}
	}
	*/


}
