package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.SelfStatement;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREInterruptedException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.natives.interfaces.Mixed;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 *
 */
@core
public class Threading {

	public static String docs() {
		return "This experimental and private API is subject to removal, or incompatible changes, and should not"
				+ " be yet heavily relied on in normal development.";
	}

	public static final Map<String, Thread> THREAD_ID_MAP = new HashMap<>();

	@api
	@noboilerplate
	@seealso({x_run_on_main_thread_later.class, x_run_on_main_thread_now.class})
	public static class x_new_thread extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			final String threadId = args[0].val();
			if(!(args[1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Expected closure for arg 2", t);
			}
			final CClosure closure = (CClosure) args[1];
			Thread th = new Thread("(" + Implementation.GetServerType().getBranding() + ") " + threadId) {
				@Override
				public void run() {
					DaemonManager dm = environment.getEnv(StaticRuntimeEnv.class).GetDaemonManager();
					dm.activateThread(Thread.currentThread());
					try {
						closure.executeCallable();
					} catch (LoopManipulationException ex) {
						ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("Unexpected loop manipulation"
								+ " operation was triggered inside the closure.", t), environment);
					} catch (ConfigRuntimeException ex) {
						ConfigRuntimeException.HandleUncaughtException(ex, environment);
					} catch (CancelCommandException ex) {
						if(ex.getMessage() != null) {
							new Echoes.console().exec(t, environment, new CString(ex.getMessage(), t), CBoolean.FALSE);
						}
					} finally {
						dm.deactivateThread(Thread.currentThread());
						synchronized(THREAD_ID_MAP) {
							if(THREAD_ID_MAP.get(threadId) == this) {
								THREAD_ID_MAP.remove(threadId);
							}
						}
					}
				}
			};
			th.start();
			synchronized(THREAD_ID_MAP) {
				THREAD_ID_MAP.put(threadId, th);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_new_thread";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {id, closure} Creates a new thread, named id, and runs the closure on that thread."
					+ " Note that many operations are not advisable to be run on other threads, and unless otherwise"
					+ " stated, functions are generally not thread safe. You can use " + new x_run_on_main_thread_later().getName()
					+ "() and " + new x_run_on_main_thread_now().getName() + "() to ensure operations will be run"
					+ " correctly, however.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			String get_current_thread = new x_get_current_thread().getName();
			String new_thread = this.getName();
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "msg(" + get_current_thread + "());\n"
				+ new_thread + "('myThread', closure(){\n"
				+ "\tsleep(5); // Sleep here, to allow the main thread to get well past us, for demonstration purposes\n"
				+ "\tmsg(" + get_current_thread + "());\n"
				+ "});\n"
				+ "msg('End of main thread');",
				"MainThread\n"
				+ "End of main thread\n"
				+ "myThread")
			};
		}

	}

	@api
	public static class x_get_current_thread extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CString(Thread.currentThread().getName(), t);
		}

		@Override
		public String getName() {
			return "x_get_current_thread";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "string {} Returns the thread id (thread name) of the currently running thread.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", this.getName() + "()", "MainThread")
			};
		}

	}

	@api
	@noboilerplate
	@seealso({x_run_on_main_thread_now.class})
	public static class x_run_on_main_thread_later extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			final CClosure closure = ArgumentValidation.getObject(args[0], t, CClosure.class);
			StaticLayer.GetConvertor().runOnMainThreadLater(
					environment.getEnv(StaticRuntimeEnv.class).GetDaemonManager(), new Runnable() {

				@Override
				public void run() {
					try {
						closure.executeCallable();
					} catch (ConfigRuntimeException e) {
						ConfigRuntimeException.HandleUncaughtException(e, environment);
					} catch (ProgramFlowManipulationException e) {
						// Ignored
					}
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_run_on_main_thread_later";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {closure} Runs the closure on the main thread later. If the function call is itself being run from the main thread, then"
					+ " the function still will not block, but it is not an error to call this from the main thread. If an exception is thrown"
					+ " from the closure, it is handled using the uncaught exception handler.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso({x_run_on_main_thread_later.class})
	public static class x_run_on_main_thread_now extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			final CClosure closure = ArgumentValidation.getObject(args[0], t, CClosure.class);
			Object ret;
			try {
				ret = StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {

					@Override
					public Object call() throws Exception {
						try {
							return closure.executeCallable();
						} catch (ConfigRuntimeException | ProgramFlowManipulationException e) {
							return e;
						}
					}
				});

			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			if(ret instanceof RuntimeException) {
				throw (RuntimeException) ret;
			} else {
				return (Mixed) ret;
			}
		}

		@Override
		public String getName() {
			return "x_run_on_main_thread_now";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "mixed {closure} Runs the closure on the main thread now, blocking the current thread until it is finished."
					+ " If the function call is itself being run from the main thread, then"
					+ " the function still will block as expected; it is not an error to call this from the main thread. Unlike"
					+ " running on the main thread later, if the underlying code throws an exception, it is thrown as a normal part of"
					+ " the execution. If the closure returns a value, it is returned by " + getName() + ".";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@noboilerplate
	@seealso({x_new_thread.class})
	@SelfStatement
	public static class _synchronized extends AbstractFunction implements VariableScope, BranchStatement {

		private static final Map<Object, Integer> SYNC_OBJECT_MAP = new HashMap<Object, Integer>();

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENullPointerException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {

			// Get the sync object tree and the code to synchronize.
			ParseTree syncObjectTree = nodes[0];
			ParseTree code = nodes[1];

			// Get the sync object (CArray or String value of the Mixed).
			Mixed cSyncObject = parent.seval(syncObjectTree, env);
			if(cSyncObject instanceof CNull) {
				throw new CRENullPointerException("Synchronization object may not be null in " + getName() + "().", t);
			}
			Object syncObject;
			if(cSyncObject.isInstanceOf(CArray.TYPE)) {
				syncObject = cSyncObject;
			} else {
				syncObject = cSyncObject.val();
			}

			// Add String sync objects to the map to be able to synchronize by value.
			if(syncObject instanceof String) {
				synchronized(SYNC_OBJECT_MAP) {
					searchLabel:
					{
						for(Entry<Object, Integer> entry : SYNC_OBJECT_MAP.entrySet()) {
							Object key = entry.getKey();
							if(key instanceof String && key.equals(syncObject)) {
								syncObject = key; // Get reference, value of this assign is the same.
								entry.setValue(entry.getValue() + 1);
								break searchLabel;
							}
						}
						SYNC_OBJECT_MAP.put(syncObject, 1);
					}
				}
			}

			// Evaluate the code, synchronized by the passed sync object.
			try {
				synchronized(syncObject) {
					parent.eval(code, env);
				}
			} catch (RuntimeException e) {
				throw e;
			} finally {

				// Remove 1 from the call count or remove the sync object from the map if it was a sync-by-value.
				if(syncObject instanceof String) {
					synchronized(SYNC_OBJECT_MAP) {
						int count = SYNC_OBJECT_MAP.get(syncObject); // This should never return null.
						if(count <= 1) {
							SYNC_OBJECT_MAP.remove(syncObject);
						} else {
							for(Entry<Object, Integer> entry : SYNC_OBJECT_MAP.entrySet()) {
								if(entry.getKey() == syncObject) { // Equals by reference.
									entry.setValue(count - 1);
									break;
								}
							}
						}
					}
				}
			}
			return CVoid.VOID;
		}

		@Override
		public Mixed exec(final Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "synchronized";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {syncObject, code} Synchronizes access to the code block for all calls (from different"
					+ " threads) with the same syncObject argument."
					+ " This means that if two threads will call " + getName() + "('example', &lt;code&gt;), the second"
					+ " call will hang the thread until the passed code of the first call has finished executing."
					+ " If you call this function from within this function on the same thread using the same"
					+ " syncObject, the code will simply be executed."
					+ " For more information about synchronization, see:"
					+ " https://en.wikipedia.org/wiki/Synchronization_(computer_science)";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Demonstrates two threads possibly overwriting eachother", ""
				+ "export('log', '');\n"
				+ "x_new_thread('Thread1', closure() {\n"
				+ "\t@log = import('log');\n"
				+ "\t@log = @log.'Some new log message from Thread1.\n'\n"
				+ "\texport('log', @log);\n"
				+ "});\n"
				+ "x_new_thread('Thread2', closure() {\n"
				+ "\t@log = import('log');\n"
				+ "\t@log = @log.'Some new log message from Thread2.\n'\n"
				+ "\texport('log', @log);\n"
				+ "});\n"
				+ "sleep(0.1);\n"
				+ "msg(import('log'));",
				"Some new log message from Thread1.\n"
				+ "\nOR\nSome new log message from Thread2.\n"
				+ "\nOR\nSome new log message from Thread1.\nSome new log message from Thread2.\n"
				+ "\nOR\nSome new log message from Thread2.\nSome new log message from Thread1.\n"),
				new ExampleScript("Demonstrates two threads modifying the same variable without the possibility of"
				+ " overwriting eachother because they are synchronized.", ""
				+ "export('log', '');\n"
				+ "x_new_thread('Thread1', closure() {\n"
				+ "\tsynchronized('syncLog') {\n"
				+ "\t\t@log = import('log');\n"
				+ "\t\t@log = @log.'Some new log message from Thread1.\n'\n"
				+ "\t\texport('log', @log);\n"
				+ "\t}\n"
				+ "});\n"
				+ "x_new_thread('Thread2', closure() {\n"
				+ "\tsynchronized('syncLog') {\n"
				+ "\t\t@log = import('log');\n"
				+ "\t\t@log = @log.'Some new log message from Thread2.\n'\n"
				+ "\t\texport('log', @log);\n"
				+ "\t}\n"
				+ "});\n"
				+ "sleep(0.1);\n"
				+ "msg(import('log'));",
				"Some new log message from Thread1.\nSome new log message from Thread2.\n"
				+ "\nOR\nSome new log message from Thread2.\nSome new log message from Thread1.\n")
			};
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(2);
			ret.add(false);
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(2);
			ret.add(false);
			ret.add(true);
			return ret;
		}
	}

	@api
	public static class x_thread_join extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInterruptedException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String threadId = args[0].val();
			Thread th;
			synchronized(THREAD_ID_MAP) {
				th = THREAD_ID_MAP.get(threadId);
			}
			if(th == null) {
				return CVoid.VOID;
			}
			int wait = 0;
			if(args.length > 1) {
				wait = ArgumentValidation.getInt32(args[1], t);
			}
			try {
				th.join(wait);
			} catch (InterruptedException ex) {
				throw new CREInterruptedException(ex, t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "x_thread_join";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {string id, [int maxWait]} Waits for the thread with the given id to exit. By default, we wait"
					+ " potentially forever, but if maxWait is specified, we will only wait that many milliseconds."
					+ " (Sending 0 for this value causes an infinite wait.) If the timeout occurs or thread interrupted, an"
					+ " InterruptedException is thrown. If the id is unknown, this function will directly return";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

	}

	@api
	public static class x_thread_is_alive extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String threadId = args[0].val();
			Thread th;
			synchronized(THREAD_ID_MAP) {
				th = THREAD_ID_MAP.get(threadId);
			}
			if(th == null) {
				return CBoolean.FALSE;
			}
			return CBoolean.get(th.isAlive());
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public String getName() {
			return "x_thread_is_alive";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {string id} Tests if this thread with the given id is alive. A thread is alive if it has been started and has not yet died.";
		}
	}

	@api
	@seealso({x_is_interrupted.class, x_clear_interrupt.class})
	public static class x_interrupt extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Thread th;
			if(args.length == 1) {
				String threadId = args[0].val();
				synchronized(THREAD_ID_MAP) {
					th = THREAD_ID_MAP.get(threadId);
				}
				if(th != null) {
					th.interrupt();
				}
			} else {
				Thread.currentThread().interrupt();
			}
			return CVoid.VOID;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public String getName() {
			return "x_interrupt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[string id]} Interrupts the thread with the given id, or the current thread if no id is given."
					+ " When a thread is interrupted, its interrupt status is set to true."
					+ " This status can be checked using the " + new x_is_interrupted().getName() + " and " + new x_clear_interrupt().getName() + " function."
					+ " Note that some blocking functions will throw an InterruptedException when the thread on which they are executed is interrupted.";
		}
	}

	@api
	@seealso({x_interrupt.class, x_clear_interrupt.class})
	public static class x_is_interrupted extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args.length == 1) {
				String threadId = args[0].val();
				Thread th;
				synchronized(THREAD_ID_MAP) {
					th = THREAD_ID_MAP.get(threadId);
				}
				if(th != null) {
					return CBoolean.get(th.isInterrupted());
				} else {
					return CBoolean.FALSE;
				}
			} else {
				return CBoolean.get(Thread.currentThread().isInterrupted());
			}
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public String getName() {
			return "x_is_interrupted";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "boolean {[string id]} Tests whether the thread with the given id (or the current thread if none is provided)"
					+ " is interrupted via " + new x_interrupt().getName() + ". If the thread doesn't exist, or is not alive, false is returned.";
		}
	}

	@api
	@seealso({x_interrupt.class, x_is_interrupted.class})
	public static class x_clear_interrupt extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(Thread.interrupted());
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public String getName() {
			return "x_clear_interrupt";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		@Override
		public String docs() {
			return "boolean {} Clears the interrupt status of the current thread. If this call actually cleared the interrupt status,"
					+ " true is returned, if the thread was already not interrupted, false is returned.";
		}
	}
}
