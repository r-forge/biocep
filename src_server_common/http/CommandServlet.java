/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
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
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.ConnectException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.SpreadsheetModelDevice;
import model.SpreadsheetModelRemote;

import org.apache.commons.logging.Log;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;

import remoting.GenericCallbackDevice;
import remoting.RKit;
import remoting.RServices;
import server.LocalHttpServer;
import server.LocalRmiRegistry;
import server.ServerManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RPFSessionInfo;
import uk.ac.ebi.microarray.pools.RmiCallInterrupted;
import uk.ac.ebi.microarray.pools.RmiCallTimeout;
import uk.ac.ebi.microarray.pools.SSHTunnelingProxy;
import uk.ac.ebi.microarray.pools.SSHUtils;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.YesSecurityManager;
import uk.ac.ebi.microarray.pools.db.DBLayerInterface;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class CommandServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(CommandServlet.class);

	private static final int RMICALL_TIMEOUT_MILLISEC = 60 * 1000 * 10;
	private static final Integer RMICALL_DONE = new Integer(0);

	RKit _rkit = null;

	public CommandServlet(RKit rkit) {
		super();
		_rkit = rkit;
		PoolUtils.initLog4J();
	}

	public CommandServlet() {
		super();
		PoolUtils.initLog4J();
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

	private void saveSessionAttributes(HttpSession session) {
		((HashMap<String, HashMap<String, Object>>) getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP")).put(session.getId(), cloneAttributes(session));
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
					System.out.println("login :" + login);
					System.out.println("pwd :" + pwd);
					
					if (_rkit == null &&  ( !login.equals(System.getProperty("login")) || !pwd.equals(System.getProperty("pwd"))  ) ) {
						result = new BadLoginPasswordException();
						break;
					}
					
					HashMap<String, Object> options = (HashMap<String, Object>) PoolUtils.hexToObject(request.getParameter("options"));
					if (options == null)
						options = new HashMap<String, Object>();
					System.out.println("options:" + options);

					RPFSessionInfo.get().put("LOGIN", login);
					RPFSessionInfo.get().put("REMOTE_ADDR", request.getRemoteAddr());
					RPFSessionInfo.get().put("REMOTE_HOST", request.getRemoteHost());

					boolean nopool = !options.keySet().contains("nopool") || ((String) options.get("nopool")).equals("")
							|| !((String) options.get("nopool")).equalsIgnoreCase("false");
					boolean save = options.keySet().contains("save") && ((String) options.get("save")).equalsIgnoreCase("true");
					boolean namedAccessMode = login.contains("@@");
					String privateName = (String) options.get("privatename");

					System.out.println("privatename=<" + privateName + ">");

					RServices r = null;
					URL[] codeUrls = null;

					if (_rkit == null) {

						if (namedAccessMode) {

							ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

							if (spFactory == null) {
								result = new NoRegistryAvailableException();
								break;
							}

							Registry registry = spFactory.getServantProvider().getRegistry();
							String sname = login.substring(login.indexOf("@@") + "@@".length());
							login = login.substring(0, login.indexOf("@@"));
							try {
								r = (RServices) registry.lookup(sname);
							} catch (Exception e) {
								e.printStackTrace();
							}

						} else {
							if (nopool) {

								/*								 
								ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

								if (spFactory == null) {
									result = new NoRegistryAvailableException();
									break;
								}

								String nodeName = options.keySet().contains("node") ? (String) options.get("node") : System
										.getProperty("private.servant.node.name");
								Registry registry = spFactory.getServantProvider().getRegistry();
								NodeManager nm = null;
								try {
									nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_" + nodeName);
								} catch (NotBoundException nbe) {
									nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
								} catch (Exception e) {
									result = new NoNodeManagerFound();
									break;
								}
								r = (RServices) nm.createPrivateServant(nodeName);
								 */

								
								
								if (System.getProperty("submit.mode").equals("ssh")) {
							        DBLayerInterface dbLayer =(DBLayerInterface)SSHTunnelingProxy.getDynamicProxy(
					        		System.getProperty("submit.ssh.host") ,Integer.decode(System.getProperty("submit.ssh.port")),System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"), System.getProperty("submit.ssh.biocep.home"),
					                "java -cp "+System.getProperty("submit.ssh.biocep.home")+"/biocep-core.jar uk.ac.ebi.microarray.pools.SSHTunnelingWorker ${file}",
					                "db",new Class<?>[]{DBLayerInterface.class});
									if (privateName != null && !privateName.equals("")) {
										try {
											r = (RServices)dbLayer.lookup(privateName);
										} catch (Exception e) {
											//e.printStackTrace();
										}
									}
							        
									if (r == null) {
										
										
								        final String uid=(privateName != null && !privateName.equals("")) ? privateName : UUID.randomUUID().toString();
								        final String[] jobIdHolder=new String[1];
								        new Thread(new Runnable(){
								                public void run() {
								                    try {
								                    	                    
								                        String command="java -Dlog.file="+System.getProperty("submit.ssh.biocep.home")+"/log/${uid}.log"
								                        				   +" -Drmi.port.start="+System.getProperty("submit.ssh.rmi.port.start")
								                        				   +" -Dname=${uid}"
								                        				   +" -Dnaming.mode=db"
								                        				   +" -Ddb.host="+System.getProperty("submit.ssh.host")
								                        				   +" -Dwait=true"
								                        				   +" -jar "+System.getProperty("submit.ssh.biocep.home")+"/biocep-core.jar";
								                        
								                        jobIdHolder[0]=SSHUtils.execSshBatch(command, uid , System.getProperty("submit.ssh.prefix") ,  System.getProperty("submit.ssh.host"), Integer.decode(System.getProperty("submit.ssh.port")) ,System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"), System.getProperty("submit.ssh.biocep.home") );								                        
								                        System.out.println("jobId:"+jobIdHolder[0]);
								                        
								                    } catch (Exception e) {
								                        e.printStackTrace();
								                    }
								                }
								            }).start();

								        long TIMEOUT=Long.decode(System.getProperty("submit.ssh.timeout"));
								        long tStart=System.currentTimeMillis();
								        while ((System.currentTimeMillis()-tStart)<TIMEOUT) {
								            try {
								                r=(RServices)dbLayer.lookup(uid);
								            } catch (Exception e) {

								            }
								            if (r!=null) break;
								            try {Thread.sleep(10);} catch (Exception e) {}
								        }
								        
								        
								        if (r!=null) {
								        	try {
								        		r.setJobId(jobIdHolder[0]);
								        	} catch (Exception e) {
								        		r=null;
											}
								        }
										
									}
									
									
								} else {
									System.out.println("LocalHttpServer.getLocalHttpServerPort():" + LocalHttpServer.getLocalHttpServerPort());
									System.out.println("LocalRmiRegistry.getLocalRmiRegistryPort():" + LocalHttpServer.getLocalHttpServerPort());
									if (privateName != null && !privateName.equals("")) {
										try {
											r = (RServices) LocalRmiRegistry.getInstance().lookup(privateName);
										} catch (Exception e) {
											//e.printStackTrace();
										}
									}
	
									if (r == null) {
										codeUrls = (URL[]) options.get("urls");
										System.out.println("CODE URL->" + Arrays.toString(codeUrls));
										r = ServerManager.createR(false, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), ServerManager.getRegistryNamingInfo(PoolUtils.getHostIp(), LocalRmiRegistry
												.getLocalRmiRegistryPort()), 256, 256, privateName, false, codeUrls,null);
									}
								}

							} else {

								ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

								if (spFactory == null) {
									result = new NoRegistryAvailableException();
									break;
								}

								boolean wait = options.keySet().contains("wait") && ((String) options.get("wait")).equalsIgnoreCase("true");
								if (wait) {
									r = (RServices) spFactory.getServantProvider().borrowServantProxy();
								} else {
									r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
								}
							}
						}
					} else {
						r = _rkit.getR();
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
					session.setAttribute("PROCESS_ID", r.getProcessId());
					session.setAttribute("JOB_ID", r.getProcessId());
					
					if (privateName != null)
						session.setAttribute("PRIVATE_NAME", privateName);

					if (codeUrls != null && codeUrls.length > 0) {
						session.setAttribute("CODEURLS", codeUrls);
					}

					session.setAttribute("threads", new ThreadsHolder());

					((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).put(session.getId(), session);
					saveSessionAttributes(session);

					Vector<HttpSession> sessionVector = ((HashMap<RServices, Vector<HttpSession>>) getServletContext().getAttribute("R_SESSIONS")).get(r);
					if (sessionVector == null) {
						sessionVector = new Vector<HttpSession>();
						((HashMap<RServices, Vector<HttpSession>>) getServletContext().getAttribute("R_SESSIONS")).put(r, sessionVector);
					}

					sessionVector.add(session);

					if (_rkit == null && save) {
						UserUtils.loadWorkspace((String) session.getAttribute("LOGIN"), r);
					}

					if (sessionVector.size() == 1) {
						try {
							if (_rkit != null)
								_rkit.getRLock().lock();

							GDDevice[] devices = r.listDevices();
							for (int i = 0; i < devices.length; ++i) {
								String deviceName = devices[i].getId();
								System.out.println("??? ---- deviceName=" + deviceName);
								session.setAttribute(deviceName, devices[i]);
							}

						} finally {
							if (_rkit != null)
								_rkit.getRLock().unlock();
						}
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

					if (_rkit != null) {
						Enumeration<String> attributeNames = session.getAttributeNames();
						while (attributeNames.hasMoreElements()) {
							String aname = attributeNames.nextElement();
							if (session.getAttribute(aname) instanceof GDDevice) {
								try {
									_rkit.getRLock().lock();
									((GDDevice) session.getAttribute(aname)).dispose();
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									_rkit.getRLock().unlock();
								}
							}
						}
					}

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

					ClassLoader urlClassLoader = this.getClass().getClassLoader();
					if (session.getAttribute("CODEURLS") != null) {
						urlClassLoader = new URLClassLoader((URL[]) session.getAttribute("CODEURLS"), this.getClass().getClassLoader());
					}

					Class<?>[] methodSignature = (Class[]) PoolUtils.hexToObject(request.getParameter("methodsignature"));

					final Method m = servant.getClass().getMethod(methodName, methodSignature);
					if (m == null) {
						throw new Exception("Bad Method Name :" + methodName);
					}
					final Object[] methodParams = (Object[]) PoolUtils.hexToObject(request.getParameter("methodparameters"), urlClassLoader);
					final Object[] resultHolder = new Object[1];
					Runnable rmiRunnable = new Runnable() {
						public void run() {
							try {
								if (_rkit != null)
									_rkit.getRLock().lock();
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
							} finally {
								if (_rkit != null)
									_rkit.getRLock().unlock();
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
					final Vector<Thread> tvec = (Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads")).getThreads().clone();
					for (int i = 0; i < tvec.size(); ++i) {
						try {
							tvec.elementAt(i).interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					stop[0] = true;
					((Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads")).getThreads()).removeAllElements();
					result = null;
					break;
				} else if (command.equals("saveimage")) {
					UserUtils.saveWorkspace((String) session.getAttribute("LOGIN"), (RServices) session.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("loadimage")) {
					UserUtils.loadWorkspace((String) session.getAttribute("LOGIN"), (RServices) session.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("newdevice")) {
					try {
						if (_rkit != null)
							_rkit.getRLock().lock();
						boolean broadcasted = new Boolean(request.getParameter("broadcasted"));
						GDDevice deviceProxy = null;
						if (broadcasted) {
							deviceProxy = ((RServices) session.getAttribute("R")).newBroadcastedDevice(Integer.decode(request.getParameter("width")), Integer
									.decode(request.getParameter("height")));
						} else {
							deviceProxy = ((RServices) session.getAttribute("R")).newDevice(Integer.decode(request.getParameter("width")), Integer
									.decode(request.getParameter("height")));
						}

						String deviceName = deviceProxy.getId();
						System.out.println("deviceName=" + deviceName);
						session.setAttribute(deviceName, deviceProxy);
						saveSessionAttributes(session);
						result = deviceName;
						break;
					} finally {
						if (_rkit != null)
							_rkit.getRLock().unlock();
					}
				} else if (command.equals("listdevices")) {
					try {
						if (_rkit != null)
							_rkit.getRLock().lock();

						result = new Vector<String>();
						for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
							String attributeName = e.nextElement();
							if (attributeName.startsWith("device_")) {
								((Vector<String>) result).add(attributeName);
							}
						}

						break;

					} finally {
						if (_rkit != null)
							_rkit.getRLock().unlock();
					}
				} else if (command.equals("newgenericcallbackdevice")) {
					try {
						if (_rkit != null)
							_rkit.getRLock().lock();
						GenericCallbackDevice genericCallBackDevice = ((RServices) session.getAttribute("R")).newGenericCallbackDevice();
						String genericCallBackDeviceName = genericCallBackDevice.getId();
						session.setAttribute(genericCallBackDeviceName, genericCallBackDevice);
						saveSessionAttributes(session);

						result = genericCallBackDeviceName;

						break;
					} finally {
						if (_rkit != null)
							_rkit.getRLock().unlock();
					}
				} else if (command.equals("newspreadsheetmodeldevice")) {
						
						String spreadsheetModelDeviceId = request.getParameter("id");
						SpreadsheetModelRemote model=null;
						
						if (spreadsheetModelDeviceId==null || spreadsheetModelDeviceId.equals("")) {
							model=((RServices) session.getAttribute("R")).newSpreadsheetTableModelRemote(Integer.decode(request.getParameter("rowcount")), Integer.decode(request.getParameter("colcount")));
						} else {
							model=((RServices) session.getAttribute("R")).getSpreadsheetTableModelRemote(spreadsheetModelDeviceId);
						}
						
						SpreadsheetModelDevice spreadsheetDevice=model.newSpreadsheetModelDevice();												
						String spreadsheetDeviceId=spreadsheetDevice.getId();
						session.setAttribute(spreadsheetDeviceId, spreadsheetDevice);
						saveSessionAttributes(session);
						result = spreadsheetDeviceId;
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {
		super.init(sConfig);
		log.info("command servlet init");
		if (_rkit == null) {
			PoolUtils.injectSystemProperties(true);
		}
		PoolUtils.initRmiSocketFactory();
		getServletContext().setAttribute("SESSIONS_MAP", new HashMap<String, HttpSession>());
		getServletContext().setAttribute("SESSIONS_ATTRIBUTES_MAP", new HashMap<String, HashMap<String, Object>>());
		getServletContext().setAttribute("R_SESSIONS", new HashMap<RServices, HttpSession>());

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}
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