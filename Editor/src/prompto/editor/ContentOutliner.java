package prompto.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import prompto.parser.Dialect;

public class ContentOutliner extends ContentOutlinePage implements IDocumentListener {

	Dialect dialect;
	IFile file;
	IDocument document;
	
	public ContentOutliner(Dialect dialect, IFile file, IDocument document) {
		this.dialect = dialect;
		this.file = file;
		this.document = document;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new ContentProvider(dialect, file));
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);
		viewer.setInput(document);
		document.addDocumentListener(this);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		TreeViewer viewer = getTreeViewer();
		Control control = viewer.getControl();
		if(control!=null && !control.isDisposed()) {
			viewer.setInput(event.getDocument());
			this.getControl().redraw();
		}
	}

	public IDocument getDocument() {
		return document;
	}
	
}
