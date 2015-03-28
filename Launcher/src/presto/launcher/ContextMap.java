package presto.launcher;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import presto.declaration.ConcreteMethodDeclaration;
import presto.declaration.IDeclaration;
import presto.grammar.DeclarationList;
import presto.parser.ISection;
import presto.runtime.Context;
import presto.statement.IStatement;

public class ContextMap {
	
	private Context context;
	
	Map<IFile,DeclarationList> map;
	
	public Context getContext() {
		return context;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public ISection findSection(IResource path, int lineNumber) {
		DeclarationList list = map.get(path);
		if(list==null)
			return null;
		return findSection(list, lineNumber);
	}
	
	
	public ISection findSection(DeclarationList list, int lineNumber) {
		for(IDeclaration decl : list) {
			if(decl.getStart().getLine()>lineNumber)
				continue;
			if(decl.getEnd().getLine()<lineNumber)
				continue;
			return findSection(decl, lineNumber);
		}
		return null;
	}

	private ISection findSection(IDeclaration decl, int lineNumber) {
		if(decl instanceof ConcreteMethodDeclaration)
			return findSection((ConcreteMethodDeclaration)decl, lineNumber);
		else
			return decl;
	}
	
	private ISection findSection(ConcreteMethodDeclaration decl, int lineNumber) {
		for(IStatement stmt : decl.getStatements()) {
			if(stmt.getStart().getLine()>lineNumber)
				continue;
			if(stmt.getEnd().getLine()<lineNumber)
				continue;
			return stmt;
		}
		return decl;
	}

}