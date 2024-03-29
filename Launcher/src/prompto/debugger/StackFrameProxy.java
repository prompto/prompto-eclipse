package prompto.debugger;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import prompto.ide.core.CoreConstants;

public class StackFrameProxy extends PlatformObject implements IStackFrame {

	DebugThread thread;
	prompto.debug.stack.IStackFrame frame;
	IResource resource = null;

	
	public StackFrameProxy(DebugThread thread, prompto.debug.stack.IStackFrame frame) {
		this.thread = thread;
		this.frame = frame;
	}

	@Override
	public String getModelIdentifier() {
		return CoreConstants.DEBUG_MODEL_IDENTIFIER;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return thread.getDebugTarget();
	}

	@Override
	public ILaunch getLaunch() {
		return thread.getLaunch();
	}

	@Override
	public IThread getThread() {
		return thread;
	}
	
	public IPromptoDebugTarget getTarget() {
		return thread.getTarget();
	}
	
	public prompto.debug.stack.IStackFrame getStackFrame() {
		return frame;
	}


	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IDebugElement.class)
			return (T)this;
		else if(adapter==ILaunch.class)
			return (T)getLaunch();
		else if(adapter==IResource.class)
			return (T)getResource();
		else
			return super.getAdapter(adapter);
	}
	
	public IResource getResource() {
		if(resource==null) try {
			resource = thread.getTarget().resolveFile(frame.getFilePath());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return resource;
	}
	

	@Override
	public boolean canStepInto() {
		return thread.canStepInto();
	}

	@Override
	public boolean canStepOver() {
		return thread.canStepOver();
	}

	@Override
	public boolean canStepReturn() {
		return thread.canStepReturn();
	}

	@Override
	public boolean isStepping() {
		return thread.isStepping();
	}

	@Override
	public void stepInto() throws DebugException {
		thread.stepInto();
	}

	@Override
	public void stepOver() throws DebugException {
		thread.stepOver();
	}

	@Override
	public void stepReturn() throws DebugException {
		thread.stepReturn();
	}

	@Override
	public boolean canResume() {
		return thread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return thread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return thread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		thread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		thread.suspend();
	}

	@Override
	public boolean canTerminate() {
		return thread.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return thread.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		thread.terminate();
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		List<VariableProxy> variables = frame.getVariables().stream()
				.map((v)->new VariableProxy(this, v))
				.collect(Collectors.toList());
		return variables.toArray(new IVariable[variables.size()]);
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return frame.getVariables().size() > 0;
	}

	@Override
	public int getLineNumber() throws DebugException {
		return frame.getStatementLine();
	}

	@Override
	public int getCharStart() throws DebugException {
		return frame.getStartCharIndex();
	}

	@Override
	public int getCharEnd() throws DebugException {
		return frame.getEndCharIndex();
	}

	@Override
	public String getName() {
		return frame.toString();
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this)
			return true;
		if(!(obj instanceof StackFrameProxy))
			return false;
		StackFrameProxy sfp = (StackFrameProxy)obj;
		return this.thread.equals(sfp.thread)
				&& frame.getIndex()==sfp.frame.getIndex();
	}



}
