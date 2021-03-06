package prompto.ide.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;

import prompto.code.ICodeStore;
import prompto.code.ImmutableCodeStore;
import prompto.code.QueryableCodeStore;
import prompto.intrinsic.PromptoVersion;
import prompto.ide.addon.AddOn;
import prompto.ide.code.ApplicationCodeStore;
import prompto.ide.code.IEclipseCodeStore;
import prompto.ide.code.LibraryCodeStore;
import prompto.ide.code.ScriptCodeStore;
import prompto.ide.code.ServerCodeStore;
import prompto.ide.core.CoreConstants;
import prompto.ide.distribution.Distribution;
import prompto.store.IStore;
import prompto.utils.ResourceUtils;

public abstract class StoreUtils {

	public static IEclipseCodeStore setStoreFor(IFile file) throws CoreException {
		IEclipseCodeStore store = fetchStoreFor(file);
		ICodeStore.instance.set(store);	
		return store;
	}

	public static synchronized IEclipseCodeStore fetchStoreFor(IFile file) throws CoreException {
		return fetchStoreFor(file.getProject());
	}
	
	public static synchronized IEclipseCodeStore fetchStoreFor(IProject project) throws CoreException {
		if(project.hasNature(CoreConstants.SCRIPTS_NATURE_ID))
			return new ScriptCodeStore(getRuntimeCodeStore(project));
		else {
			QualifiedName key = new QualifiedName(CoreConstants.CORE_PLUGIN_ID, "code_store");
			Object obj = project.getSessionProperty(key);
			if(obj instanceof IEclipseCodeStore)
				return (IEclipseCodeStore)obj;
			if(project.hasNature(CoreConstants.LIBRARY_NATURE_ID))
				obj = new LibraryCodeStore(getRuntimeCodeStore(project));
			else if(project.hasNature(CoreConstants.SERVER_NATURE_ID))
				obj = new ServerCodeStore(getRuntimeCodeStore(project));
			else if(project.hasNature(CoreConstants.APPLICATION_NATURE_ID))
				obj = new ApplicationCodeStore(getRuntimeCodeStore(project));
			else
				throw new CoreException(Status.CANCEL_STATUS);
			project.setSessionProperty(key, obj);
			((IEclipseCodeStore)obj).setProject(project);
			return (IEclipseCodeStore)obj;
		}
	}
	private static ICodeStore runtimeCodeStore = null;
	
	public static ICodeStore getRuntimeCodeStore(IProject project) throws CoreException {
		if(!ProjectUtils.hasRuntime(project))
			return null;
		if(runtimeCodeStore==null) {
			ICodeStore runtime = ImmutableCodeStore.bootstrapRuntime(()->getLibraryEntries());
			URL[] addOns = AddOn.allURLs();
			runtimeCodeStore = new QueryableCodeStore(getNullStore(), runtime, project.getName(), PromptoVersion.LATEST, addOns);
		}
		return runtimeCodeStore;
	}
	
	private static Collection<URL> getLibraryEntries() {
		Distribution dist = Distribution.getDefaultDistribution();
		if(dist==null)
			return Collections.emptyList();
		List<URL> urls = new ArrayList<URL>();
		File jarFile = Paths.get(dist.getDirectory(), "Runtime-0.0.1-SNAPSHOT.jar").toFile();
		addLibraryEntries(urls, jarFile);
		jarFile = Paths.get(dist.getDirectory(), "Server-0.0.1-SNAPSHOT.jar").toFile();
		addLibraryEntries(urls, jarFile);
		return urls;
	}
	
	private static void addLibraryEntries(List<URL> urls, File jarFile) {
		if(jarFile.exists()) try(InputStream input = new FileInputStream(jarFile)) {
			try(ZipInputStream zip = new ZipInputStream(input)) {
				ZipEntry entry = zip.getNextEntry();
				while(entry!=null) {
					if(entry.getName().startsWith("libraries/") && ResourceUtils.isPromptoLibrary(entry.getName()))
						urls.add(new URL("jar:" + jarFile.toURI().toURL() + "!/" + entry.getName()));
					entry = zip.getNextEntry();
				}
			}
		} catch(IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	private static IStore getNullStore() throws CoreException {
		return null;
	}


}
