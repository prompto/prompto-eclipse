package prompto.content;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import prompto.core.CoreConstants;
import prompto.core.Plugin;
import prompto.utils.ImageUtils;

public class LibrariesRootElement extends LibraryElement {

	public LibrariesRootElement(IProject parent) {
		super(parent);
	}
	
	@Override
	public IProject getProject() {
		return (IProject)getParent();
	}
	
	@Override
	public String getText() {
		return "Prompto Libraries";
	}	
	
	@Override
	public Image getImage() {
		return ImageUtils.load(Plugin.getDefault().getBundle(), "images/library.gif");
	}
	
	@Override
	public boolean hasChildren() throws CoreException {
		IProject project = getProject();
		if(project.hasNature(CoreConstants.LIBRARY_NATURE_ID))
			return project.getReferencedProjects().length>0;
		else
			return true;
	}

	@Override
	public Object[] getChildren() throws CoreException {
		IProject project = getProject();
		if(project.hasNature(CoreConstants.LIBRARY_NATURE_ID))
			return getReferencedLibraries(project);
		else
			return getPromptoLibraries();
	}
	
	private Object[] getPromptoLibraries() {
		// TODO Auto-generated method stub
		return new Object[0];
	}

	private Object[] getReferencedLibraries(IProject project) throws CoreException {
		// double check that referenced projects are Prompto libraries
		List<ILibraryElement> libraries = new ArrayList<ILibraryElement>();
		for(IProject ref : project.getReferencedProjects()) {
			if(ref.hasNature(CoreConstants.LIBRARY_NATURE_ID))
				libraries.add(new LibraryProjectElement(this, ref));
		}
		return libraries.toArray(new ILibraryElement[libraries.size()]);
	}
}