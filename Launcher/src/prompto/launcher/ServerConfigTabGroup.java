package prompto.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class ServerConfigTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs( new ILaunchConfigurationTab[] {
			new LaunchServerTab(),
			new ArgumentsTab(),
			new RuntimeTab(),
			new CommonTab()
		} );
	}

}
