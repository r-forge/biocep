package http.local;

import graphics.pop.GDDevice;
import graphics.rmi.RGui;
import http.NoServantAvailableException;
import http.NotLoggedInException;
import http.RHttpProxy;
import http.TunnelingException;
import http.UserUtils;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ConnectException;
import java.util.HashMap;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RmiCallInterrupted;
import uk.ac.ebi.microarray.pools.RmiCallTimeout;

public class LocalCommandServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	private static final int RMICALL_TIMEOUT_MILLISEC = 60 * 1000 * 10;
	private static final Integer RMICALL_DONE = new Integer(0);

	RGui _rgui = null;

	public LocalCommandServlet(RGui rgui) {
		super();
		_rgui = rgui;
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

	HashMap<String, Object> _sessionAttributes = null;

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = null;
		Object result = null;

		try {
			final String command = request.getParameter("method");
			do {

				if (command.equals("logon")) {

					String login = (String) PoolUtils.hexToObject(request.getParameter("login"));
					String pwd = (String) PoolUtils.hexToObject(request.getParameter("pwd"));

					RServices r = _rgui.getR();
					if (r == null) {
						result = new NoServantAvailableException();
						break;
					}

					_sessionAttributes = new HashMap<String, Object>();
					_sessionAttributes.put("R", r);
					_sessionAttributes.put("LOGIN", login);
					_sessionAttributes.put("threads", new ThreadsHolder());

					result = RHttpProxy.FAKE_SESSION;
					break;

				}

				if (_sessionAttributes == null) {
					result = new NotLoggedInException();
					break;
				}

				if (command.equals("logoff")) {

					for (Object k : _sessionAttributes.values()) {
						if (k instanceof GDDevice) {
							try {
								_rgui.getRLock().lock();
								((GDDevice) k).dispose();
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								_rgui.getRLock().unlock();
							}
						}
					}

					_sessionAttributes = null;
					result = null;
					break;
				}

				final boolean[] stop = { false };

				if (command.equals("invoke")) {

					String servantName = (String) PoolUtils.hexToObject(request.getParameter("servantname"));
					final Object servant = _sessionAttributes.get(servantName);
					String methodName = (String) PoolUtils.hexToObject(request.getParameter("methodname"));
					Class<?>[] methodSignature = (Class[]) PoolUtils.hexToObject(request.getParameter("methodsignature"));
					final Method m = servant.getClass().getMethod(methodName, methodSignature);
					if (m == null) {
						throw new Exception("Bad Method Name :" + methodName);
					}
					final Object[] methodParams = (Object[]) PoolUtils.hexToObject(request.getParameter("methodparameters"));

					final Object[] resultHolder = new Object[1];
					Runnable rmiRunnable = new Runnable() {
						public void run() {
							try {
								_rgui.getRLock().lock();
								resultHolder[0] = m.invoke(servant, methodParams);

								if (resultHolder[0] == null)
									resultHolder[0] = RMICALL_DONE;
							} catch (InvocationTargetException ite) {
								if (ite.getCause() instanceof ConnectException) {
									_sessionAttributes = null;
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
							} finally {
								_rgui.getRLock().unlock();
							}
						}
					};

					Thread rmiThread = InterruptibleRMIThreadFactory.getInstance().newThread(rmiRunnable);
					((ThreadsHolder) _sessionAttributes.get("threads")).getThreads().add(rmiThread);
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
						((ThreadsHolder) _sessionAttributes.get("threads")).getThreads().remove(rmiThread);
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
					final Vector<Thread> tvec = (Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads")).getThreads().clone();
					for (int i = 0; i < tvec.size(); ++i) {
						try {
							tvec.elementAt(i).interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					stop[0] = true;
					((Vector<Thread>) ((ThreadsHolder) _sessionAttributes.get("threads")).getThreads()).removeAllElements();
					result = null;
					break;
				} else if (command.equals("saveimage")) {
					UserUtils.saveWorkspace((String) _sessionAttributes.get("LOGIN"), (RServices) session.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("loadimage")) {
					UserUtils.loadWorkspace((String) _sessionAttributes.get("LOGIN"), (RServices) session.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("newdevice")) {
					try {
						_rgui.getRLock().lock();
						GDDevice deviceProxy = ((RServices) _sessionAttributes.get("R")).newDevice(Integer.decode(request.getParameter("width")), Integer
								.decode(request.getParameter("height")));
						String deviceName = "device" + "_" + deviceProxy.getDeviceNumber();
						System.out.println("deviceName=" + deviceName);
						_sessionAttributes.put(deviceName, deviceProxy);
						result = deviceName;
						break;
					} finally {
						_rgui.getRLock().unlock();
					}
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
		response.getOutputStream().flush();
		response.getOutputStream().close();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

}