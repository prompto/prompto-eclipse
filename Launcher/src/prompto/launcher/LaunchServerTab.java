package prompto.launcher;

import prompto.ide.core.RunType;

public class LaunchServerTab extends LaunchTabBase {
	
	@Override
	protected RunType getRunType() {
		return RunType.SERVER;
	}
	
	@Override
	protected boolean supportsMethodSelector() {
		return true;
	}
	
	@Override
	protected boolean requiresSelectedMethod() {
		return false;
	}
	
	@Override
	protected boolean supportsServerPort() {
		return true;
	}
}
