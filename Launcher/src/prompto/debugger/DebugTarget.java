package prompto.debugger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import prompto.debug.IDebugEventListener;
import prompto.debug.JavaDebugEventListener;
import prompto.debug.JavaDebugRequestClient;
import prompto.debug.worker.LeanWorker;
import prompto.ide.code.IEclipseCodeStore;
import prompto.ide.core.CoreConstants;
import prompto.ide.utils.ProjectUtils;
import prompto.ide.utils.ShellUtils;
import prompto.launcher.ILaunchHelper;
import prompto.launcher.LaunchContext;
import prompto.parser.ICodeSection;
import prompto.runner.Runner;

public class DebugTarget extends PlatformObject implements IPromptoDebugTarget  {
	
	public static void run(LaunchContext context) throws CoreException {
		DebugTarget debugger = new DebugTarget(context);
		debugger.debug();
	}

	LaunchContext context;
	JavaDebugEventListener eventServer; 
	DebugEventListener listener;
	JavaDebugRequestClient debugger;
	IProcess process;
	DebugThread thread; // until Prompto supports Workers
	
	public DebugTarget(LaunchContext context) {
		this.context = context;
	}
	
	public DebugThread getThread() {
		return thread;
	}
	
	@Override
	public IProject getProject() {
		return context.getProject();
	}
	
	@Override
	public IDebugEventListener getDebugEventListener() {
		return listener;
	}
	
	@Override
	public JavaDebugRequestClient getDebugger() {
		return debugger;
	}
	
	@Override
	public ILaunch getLaunch() {
		return context.getLaunch();
	}
	
	@Override
	public String getName() {
		ILaunchHelper helper = context.getLaunchHelper();
		StringBuilder sb = new StringBuilder();
		sb.append(context.getConfiguration().getName());
		sb.append(" [");
		sb.append(helper.getRunTypeName());
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "Prompto@localhost:" + eventServer.getPort();
	}

	@Override
	public void debug() throws CoreException {
		try {
			listener = new DebugEventListener(this);
			eventServer = new JavaDebugEventListener(listener);
			int port = eventServer.startListening();
			String[] commands = buildCommands(port);
			ProcessBuilder builder = new ProcessBuilder(commands)
				.directory(new File(context.getDistribution().getDirectory()))
				.inheritIO();
			Process remote = builder.start();
			debugger = new JavaDebugRequestClient.Process(remote, eventServer);
			String processName = getProcessName(remote);
			process = DebugPlugin.newProcess(context.getLaunch(), remote, processName);
			thread = new DebugThread(this, new LeanWorker()); 
			context.getLaunch().addDebugTarget(this);
			listener.waitConnected();
			connectBreakpoints();
			DebuggerUtils.startListeningToBreakpoints(this);
			if(context.isStopInMain())
				DebuggerUtils.fireSuspendEvent(this, DebugEvent.STEP_INTO);
			else
				debugger.resume(null);
		} catch(IOException | InterruptedException e) {
			e.printStackTrace(System.err);
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}
	
	private String getProcessName(Process remote) {
		StringBuilder sb = new StringBuilder();
		sb.append(context.getDistribution().getDirectory());
		sb.append("/");
		sb.append(context.getLaunchHelper().getTargetJar(context));
		return sb.toString();
	}

	private String[] buildCommands(int debugPort) throws CoreException {
		ILaunchHelper helper = context.getLaunchHelper();
		List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-jar");
		commands.add(helper.getTargetJar(context));
		commands.add("-debug_port");
		commands.add(String.valueOf(debugPort));
		commands.addAll(helper.getTargetSpecifiers(context));
		commands.add("-resources");
		commands.add(Runner.getResourcesAsString(context));
		if(!ProjectUtils.hasRuntime(context.getProject())) {
			commands.add("-loadRuntime");
			commands.add("false");
		}
		Collection<String> args = Runner.getCommandLineArgs(context);
		if(args!=null && !args.isEmpty())
			commands.addAll(args);
		return commands.toArray(new String[commands.size()]);
	}
	
	@Override
	public IFile resolveFile(String filePath) throws CoreException {
		try {
			URI location = new URI(filePath);
			IWorkspaceRoot root = ShellUtils.getRoot();
			IFile[] files = root.findFilesForLocationURI(location);
			return files.length==0 ? null : files[0];
		} catch (URISyntaxException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}


	public void connectBreakpoints() throws CoreException {
		IBreakpoint[] breakpoints = DebuggerUtils.getBreakpoints();
		for(IBreakpoint breakpoint : breakpoints)
			connectBreakpoint(breakpoint);
	}


	public void connectBreakpoint(IBreakpoint breakpoint) throws CoreException {
		if(breakpoint instanceof LineBreakpoint)
			connectLineBreakpoint((LineBreakpoint)breakpoint);
		else
			throw new RuntimeException("Unsupported");
		
	}

	public void connectLineBreakpoint(LineBreakpoint breakpoint) throws CoreException {
		IEclipseCodeStore store = context.getCodeStore();
		ICodeSection section = store.findSection(breakpoint.getResource(), breakpoint.getLineNumber());
		breakpoint.setEnabled(section!=null);
		if(section!=null)
			section.getSection().setAsBreakpoint(breakpoint.isEnabled());
	}
	

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return false;
	}


	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		try {
			connectBreakpoint(breakpoint);
		} catch (CoreException e) {
			e.printStackTrace(System.err);
		}
	}
	
	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		try {
			disconnectBreakpoint(breakpoint);
		} catch (CoreException e) {
			e.printStackTrace(System.err);
		}
	}

	public void disconnectBreakpoint(IBreakpoint breakpoint) throws CoreException {
		if(breakpoint instanceof LineBreakpoint)
			disconnectLineBreakpoint((LineBreakpoint)breakpoint);
		else
			throw new RuntimeException("Unsupported");
		
	}

	public void disconnectLineBreakpoint(LineBreakpoint breakpoint) throws CoreException {
		IEclipseCodeStore store = context.getCodeStore();
		ICodeSection section = store.findSection(breakpoint.getResource(), breakpoint.getLineNumber());
		if(section!=null)
			section.getSection().setAsBreakpoint(false);
	}
	
	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}


	@Override
	public IProcess getProcess() {
		return process;
	}

	
	@Override
	public boolean hasThreads() throws DebugException {
		return !isTerminated();
	}
	
	
	@Override
	public IThread[] getThreads() throws DebugException {
		return isTerminated() ? new IThread[0] : new IThread[] { thread };
	}
	
	@Override
	public String getModelIdentifier() {
		return CoreConstants.DEBUG_MODEL_IDENTIFIER;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IDebugElement.class)
			return (T)this;
		else if(adapter==ILaunch.class)
			return (T)getLaunch();
		else
			return super.getAdapter(adapter);
	}

	@Override
	public boolean canTerminate() {
		return debugger==null || debugger.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return debugger==null || debugger.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		debugger.terminate();
	}

	@Override
	public boolean canDisconnect() {
		return process!=null;
	}

	@Override
	public void disconnect() throws DebugException {
		process = null;
	}

	@Override
	public boolean isDisconnected() {
		return process==null;
	}

	@Override
	public boolean canSuspend() {
		return debugger.canSuspend(null);
	}

	@Override
	public boolean isSuspended() {
		return debugger.isSuspended(null);
	}

	@Override
	public void suspend() throws DebugException {
		debugger.suspend(null);
	}
	
	@Override
	public boolean canResume() {
		return debugger.canResume(null);
	}

	@Override
	public void resume() throws DebugException {
		debugger.resume(null);
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	public void notifyTerminated() {
		debugger.notifyTerminated();
		process = null;
	}
	
}
