/*
 * Copyright (C) 2007 EMBL-EBI
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package http;

import graphics.pop.GDDevice;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.NodeManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RmiCallInterrupted;
import uk.ac.ebi.microarray.pools.RmiCallTimeout;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.db.RPFSessionInfo;
import util.Utils;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class CommandServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private static final int RMICALL_TIMEOUT_MILLISEC = 60 * 1000 * 10;
	private static final Integer RMICALL_DONE = new Integer(0);

	public CommandServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	class ThreadsHolder {
		private transient Vector<Thread> _threads = new Vector<Thread>();

		Vector<Thread> getThreads() {
			return _threads;
		}
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {

		HttpSession session = null;
		Object result = null;

		try {
			final String command = request.getParameter("method");
			do {

				if (command.equals("logon")) {

					session = request.getSession(false);
					if (session != null) {
						result = session.getId();
						break;
					}

					String login = (String) PoolUtils.hexToObject(request.getParameter("login"));
					String pwd = (String) PoolUtils.hexToObject(request.getParameter("pwd"));
					HashMap<String, Object> options = (HashMap<String, Object>) PoolUtils.hexToObject(request
							.getParameter("options"));
					if (options == null)
						options = new HashMap<String, Object>();
					System.out.println("options:" + options);

					RPFSessionInfo.get().put("LOGIN", login);
					RPFSessionInfo.get().put("REMOTE_ADDR", request.getRemoteAddr());
					RPFSessionInfo.get().put("REMOTE_HOST", request.getRemoteHost());

					RServices r = null;

					boolean nopool = options.keySet().contains("nopool")
							&& ((String) options.get("nopool")).equalsIgnoreCase("true");
					boolean save = options.keySet().contains("save")
							&& ((String) options.get("save")).equalsIgnoreCase("true");

					ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

					if (spFactory == null) {
						result = new NoRegistryAvailableException();
						break;
					}

					boolean namedAccessMode = login.contains("@@");

					if (!namedAccessMode) {
						if (nopool) {

							String nodeName = options.keySet().contains("node") ? (String) options.get("node") : System
									.getProperty("private.servant.node.name");
							Registry registry = spFactory.getServantProvider().getRegistry();
							NodeManager nm = null;
							try {
								nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_"
										+ nodeName);
							} catch (NotBoundException nbe) {
								nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
							} catch (Exception e) {
								result = new NoNodeManagerFound();
								break;
							}
							r = (RServices) nm.createPrivateServant(nodeName);
						} else {
							boolean wait = options.keySet().contains("wait")
									&& ((String) options.get("wait")).equalsIgnoreCase("true");
							if (wait) {
								r = (RServices) spFactory.getServantProvider().borrowServantProxy();
							} else {
								r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
							}
						}
					} else {

						Registry registry = spFactory.getServantProvider().getRegistry();
						String sname = login.substring(login.indexOf("@@") + "@@".length());
						login = login.substring(0, login.indexOf("@@"));
						try {
							r = (RServices) registry.lookup(sname);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (r == null) {
						result = new NoServantAvailableException();
						break;
					}

					session = request.getSession(true);
					session.setAttribute("R", r);
					session.setAttribute("NOPOOL", nopool);
					session.setAttribute("SAVE", save);
					session.setAttribute("LOGIN", login);
					session.setAttribute("NAMED_ACCESS_MODE", namedAccessMode);					
					
					session.setAttribute("threads", new ThreadsHolder());

					((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).put(session
							.getId(), session);
					((HashMap<String, HashMap<String, Object>>) getServletContext().getAttribute(
							"SESSIONS_ATTRIBUTES_MAP")).put(session.getId(), cloneAttributes(session));

					if (save) {
						UserUtils.loadWorkspace((String) session.getAttribute("LOGIN"), r);
					}

					result = session.getId();
					break;

				}

				session = request.getSession(false);
				if (session == null) {
					result = new NotLoggedInException();
					break;
				}

				if (command.equals("logoff")) {
					try {
						session.invalidate();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					result = null;
					break;
				}

				final boolean[] stop = { false };
				final HttpSession currentSession = session;

				if (command.equals("invoke")) {

					String servantName = (String) PoolUtils.hexToObject(request.getParameter("servantname"));
					final Object servant = session.getAttribute(servantName);
					if (servant == null) {
						throw new Exception("Bad Servant Name :" + servantName);
					}

					String methodName = (String) PoolUtils.hexToObject(request.getParameter("methodname"));
					Class<?>[] methodSignature = (Class[]) PoolUtils.hexToObject(request
							.getParameter("methodsignature"));
					final Method m = servant.getClass().getMethod(methodName, methodSignature);
					if (m == null) {
						throw new Exception("Bad Method Name :" + methodName);
					}

					final Object[] methodParams = (Object[]) PoolUtils.hexToObject(request
							.getParameter("methodparameters"));

					final Object[] resultHolder = new Object[1];
					Runnable rmiRunnable = new Runnable() {
						public void run() {
							try {
								resultHolder[0] = m.invoke(servant, methodParams);
								if (resultHolder[0] == null)
									resultHolder[0] = RMICALL_DONE;
							} catch (InvocationTargetException ite) {
								if (ite.getCause() instanceof ConnectException) {
									currentSession.invalidate();
									resultHolder[0] = new NotLoggedInException();
								} else {
									resultHolder[0] = ite.getCause();
								}
							} catch (Exception e) {
								final boolean wasInterrupted = Thread.interrupted();
								if (wasInterrupted) {
									resultHolder[0] = new RmiCallInterrupted();
								} else {
									resultHolder[0] = e;
								}
							}
						}
					};

					Thread rmiThread = InterruptibleRMIThreadFactory.getInstance().newThread(rmiRunnable);
					((ThreadsHolder) session.getAttribute("threads")).getThreads().add(rmiThread);
					rmiThread.start();

					long t1 = System.currentTimeMillis();

					while (resultHolder[0] == null) {

						if ((System.currentTimeMillis() - t1) > RMICALL_TIMEOUT_MILLISEC || stop[0]) {
							rmiThread.interrupt();
							resultHolder[0] = new RmiCallTimeout();
							break;
						}

						try {
							Thread.sleep(10);
						} catch (Exception e) {
						}
					}
					try {
						((ThreadsHolder) session.getAttribute("threads")).getThreads().remove(rmiThread);
					} catch (IllegalStateException e) {
					}

					if (resultHolder[0] instanceof Throwable) {
						throw (Throwable) resultHolder[0];
					}

					if (resultHolder[0] == RMICALL_DONE) {
						result = null;
					} else {
						result = resultHolder[0];
					}

					break;

				}

				if (command.equals("interrupt")) {
					final Vector<Thread> tvec = (Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads"))
							.getThreads().clone();
					for (int i = 0; i < tvec.size(); ++i) {
						try {
							tvec.elementAt(i).interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					stop[0] = true;
					((Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads")).getThreads())
							.removeAllElements();
					result = null;
					break;
				} else if (command.equals("saveimage")) {
					UserUtils.saveWorkspace((String) session.getAttribute("LOGIN"), (RServices) session
							.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("loadimage")) {
					UserUtils.loadWorkspace((String) session.getAttribute("LOGIN"), (RServices) session
							.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("newdevice")) {
					GDDevice deviceProxy=((RServices) session	.getAttribute("R")).newDevice(Integer.decode(request.getParameter("width")), Integer.decode(request.getParameter("height")));
					String deviceName="device"+"_"+deviceProxy.getDeviceNumber();
					System.out.println("deviceName="+deviceName);
					session.setAttribute(deviceName, deviceProxy);
					result = deviceName;
					break;
				}
				

			} while (true);

		} catch (TunnelingException te) {
			result = te;
			te.printStackTrace();
		} catch (Throwable e) {
			result = new TunnelingException("Server Side", e);
			e.printStackTrace();
		}
		response.setContentType("application/x-java-serialized-object");
		new ObjectOutputStream(response.getOutputStream()).writeObject(result);
		response.flushBuffer();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {
		super.init(sConfig);
		PoolUtils.injectSystemProperties(true);
		Utils.initLog();
		PoolUtils.initRmiSocketFactory();
		getServletContext().setAttribute("SESSIONS_MAP", new HashMap<String, HttpSession>());
		getServletContext().setAttribute("SESSIONS_ATTRIBUTES_MAP", new HashMap<String, HashMap<String, Object>>());
	}

	HashMap<String, Object> cloneAttributes(HttpSession session) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result.put(name, session.getAttribute(name));
		}

		return result;
	}

}