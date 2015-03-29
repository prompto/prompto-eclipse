package presto.runner;

import presto.launcher.ContextMap;
import presto.launcher.LaunchContext;
import presto.runtime.Interpreter;

public class RunTarget {

	public static void run(LaunchContext context) {
		try {
			ContextMap cm = context.buildContextMap();
			Interpreter.interpret(cm.getContext(), context.getMethod().getName(), context.getCmdLineArgs());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

}