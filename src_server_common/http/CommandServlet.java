/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package http;

import static org.kchine.rpf.PoolUtils.DEFAULT_MEMORY_MAX;
import static org.kchine.rpf.PoolUtils.DEFAULT_MEMORY_MIN;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.ConnectException;
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
import org.apache.commons.logging.Log;
import org.kchine.r.server.GenericCallbackDevice;
import org.kchine.r.server.RKit;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.spreadsheet.SpreadsheetModelDevice;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.rpf.LocalRmiRegistry;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RPFSessionInfo;
import org.kchine.rpf.RmiCallInterrupted;
import org.kchine.rpf.RmiCallTimeout;
import org.kchine.rpf.SSHTunnelingProxy;
import org.kchine.rpf.SSHUtils;
import org.kchine.rpf.ServantProvider;
import org.kchine.rpf.ServantProviderFactory;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.YesSecurityManager;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.monitor.SupervisorUtils;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;
import server.ExtendedReentrantLock;
import server.LocalHttpServer;
import server.ServerManager;

/**
 * @author Karim Chine karim.chine@m4x.org
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

				if (command.equals("ping")) {
					result="pong";
					break;					
				} else  if (command.equals("logon")) {

					session = request.getSession(false);
					if (session != null) {
						result = session.getId();
						break;
					}

					String login = (String) PoolUtils.hexToObject(request.getParameter("login"));
					String pwd = (String) PoolUtils.hexToObject(request.getParameter("pwd"));
					boolean namedAccessMode = login.contains("@@");
					String sname = null;
					if (namedAccessMode) {
						sname=login.substring(login.indexOf("@@") + "@@".length());
						login = login.substring(0, login.indexOf("@@"));
					}
					
					System.out.println("login :" + login);
					System.out.println("pwd :" + pwd);
					
					
					if (_rkit == null &&  ( !login.equals(System.getProperty("login")) || !pwd.equals(System.getProperty("pwd"))  ) ) {
						result = new BadLoginPasswordException();
						break;
					}
					
					HashMap<String, Object> options = (HashMap<String, Object>) PoolUtils.hexToObject(request.getParameter("options"));
					if (options == null) options = new HashMap<String, Object>();
					System.out.println("options:" + options);

					RPFSessionInfo.get().put("LOGIN", login);
					RPFSessionInfo.get().put("REMOTE_ADDR", request.getRemoteAddr());
					RPFSessionInfo.get().put("REMOTE_HOST", request.getRemoteHost());

					boolean nopool = !options.keySet().contains("nopool") || ((String) options.get("nopool")).equals("")
							|| !((String) options.get("nopool")).equalsIgnoreCase("false");
					boolean save = options.keySet().contains("save") && ((String) options.get("save")).equalsIgnoreCase("true");
					boolean selfish=options.keySet().contains("selfish") && ((String) options.get("selfish")).equalsIgnoreCase("true");
					
					String privateName = (String) options.get("privatename");
					
					int memoryMin = DEFAULT_MEMORY_MIN; 
					int memoryMax = DEFAULT_MEMORY_MAX;
					try {
						if (options.get("memorymin")!=null) memoryMin = Integer.decode((String)options.get("memorymin"));
						if (options.get("memorymax")!=null) memoryMax = Integer.decode((String)options.get("memorymax"));
					} catch (Exception e) {		
						e.printStackTrace();
					}
					

					RServices r = null;
					URL[] codeUrls = null;

					if (_rkit == null) {

						if (namedAccessMode) {
							
							try {
								if (System.getProperty("submit.mode") != null  && System.getProperty("submit.mode").equals("ssh")) {
									
									if (PoolUtils.isStubCandidate(sname)) {
										r=(RServices)PoolUtils.hexToStub(sname, PoolUtils.class.getClassLoader());
									} else {
										r = (RServices) ((DBLayerInterface)SSHTunnelingProxy.getDynamicProxy(
								        		System.getProperty("submit.ssh.host") ,Integer.decode(System.getProperty("submit.ssh.port")),System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"), System.getProperty("submit.ssh.biocep.home"),
								                "java -Dpools.provider.factory=org.kchine.rpf.db.ServantsProviderFactoryDB -Dpools.dbmode.defaultpoolname=R -Dpools.dbmode.shutdownhook.enabled=false -cp %{install.dir}/biocep-core.jar org.kchine.rpf.SSHTunnelingWorker %{file}",
								                "db",new Class<?>[]{DBLayerInterface.class})).lookup(sname);
									}
									
								} else {
									
									if (PoolUtils.isStubCandidate(sname)) {
										r=(RServices)PoolUtils.hexToStub(sname, PoolUtils.class.getClassLoader());
									} else {
										ServantProviderFactory spFactory = ServantProviderFactory.getFactory();
										if (spFactory == null) {
											result = new NoRegistryAvailableException();
											break;
										}
										r = (RServices) spFactory.getServantProvider().getRegistry().lookup(sname);
									}
									
								}
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

								
								
								if (System.getProperty("submit.mode")!=null && System.getProperty("submit.mode").equals("ssh")) {
									
									
							        DBLayerInterface dbLayer =(DBLayerInterface)SSHTunnelingProxy.getDynamicProxy(
					        		System.getProperty("submit.ssh.host") ,Integer.decode(System.getProperty("submit.ssh.port")),System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"), System.getProperty("submit.ssh.biocep.home"),
					                "java -Dpools.provider.factory=org.kchine.rpf.db.ServantsProviderFactoryDB -Dpools.dbmode.defaultpoolname=R -Dpools.dbmode.shutdownhook.enabled=false -cp %{install.dir}/biocep-core.jar org.kchine.rpf.SSHTunnelingWorker %{file}",
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
								                    	                    
								                        String command="java -Dlog.file="+System.getProperty("submit.ssh.biocep.home")+"/log/%{uid}.log"
								                        				   +" -Drmi.port.start="+System.getProperty("submit.ssh.rmi.port.start")
								                        				   +" -Dname=%{uid}"
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
										r = ServerManager.createR(null, false, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), ServerManager.getRegistryNamingInfo(PoolUtils.getHostIp(), LocalRmiRegistry
												.getLocalRmiRegistryPort()), memoryMin, memoryMax, privateName, false, codeUrls,null);
									}
								}

							} else {

								if (System.getProperty("submit.mode").equals("ssh")) {
									
									
									ServantProvider servantProvider =(ServantProvider)SSHTunnelingProxy.getDynamicProxy(
							        		System.getProperty("submit.ssh.host") ,Integer.decode(System.getProperty("submit.ssh.port")),System.getProperty("submit.ssh.user") ,System.getProperty("submit.ssh.password"), System.getProperty("submit.ssh.biocep.home"),
							                "java -Dpools.provider.factory=org.kchine.rpf.db.ServantsProviderFactoryDB -Dpools.dbmode.defaultpoolname=R -Dpools.dbmode.shutdownhook.enabled=false -cp %{install.dir}/biocep-core.jar org.kchine.rpf.SSHTunnelingWorker %{file}",
							                "servant.provider",new Class<?>[]{ServantProvider.class});									
									boolean wait = options.keySet().contains("wait") && ((String) options.get("wait")).equalsIgnoreCase("true");									
									String poolname=((String) options.get("poolname"));
									if (wait) {
										r = (RServices)(poolname==null || poolname.trim().equals("") ? servantProvider.borrowServantProxy() : servantProvider.borrowServantProxy(poolname));
									} else {
										r = (RServices)(poolname==null || poolname.trim().equals("") ? servantProvider.borrowServantProxyNoWait(): servantProvider.borrowServantProxyNoWait(poolname));
									}
									
									System.out.println("---> borrowed : "+r);
									
								} else {
									ServantProviderFactory spFactory = ServantProviderFactory.getFactory();
	
									if (spFactory == null) {
										result = new NoRegistryAvailableException();
										break;
									}
	
									boolean wait = options.keySet().contains("wait") && ((String) options.get("wait")).equalsIgnoreCase("true");
									String poolname=((String) options.get("poolname"));
									if (wait) {
										r = (RServices) (poolname==null || poolname.trim().equals("")? spFactory.getServantProvider().borrowServantProxy() : spFactory.getServantProvider().borrowServantProxy(poolname));
									} else {
										r = (RServices) (poolname==null || poolname.trim().equals("")? spFactory.getServantProvider().borrowServantProxyNoWait(): spFactory.getServantProvider().borrowServantProxyNoWait(poolname));
									}
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
					
					Integer sessionTimeOut=null;
					try {
						if (options.get("sessiontimeout")!=null) sessionTimeOut = Integer.decode((String)options.get("sessiontimeout"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (sessionTimeOut!=null) {
						session.setMaxInactiveInterval(sessionTimeOut);
					}
					
					session.setAttribute("TYPE", "RS");
					session.setAttribute("R", r);		
					session.setAttribute("NOPOOL", nopool);
					session.setAttribute("SAVE", save);
					session.setAttribute("LOGIN", login);
					session.setAttribute("NAMED_ACCESS_MODE", namedAccessMode);
					session.setAttribute("PROCESS_ID", r.getProcessId());
					session.setAttribute("JOB_ID", r.getJobId());
					session.setAttribute("SELFISH", selfish);
					session.setAttribute("IS_RELAY", _rkit!=null);
															
					if (privateName != null)
						session.setAttribute("PRIVATE_NAME", privateName);

					if (codeUrls != null && codeUrls.length > 0) {
						session.setAttribute("CODEURLS", codeUrls);
					}

					session.setAttribute("THREADS", new ThreadsHolder());

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

					System.out.println("---> Has Collaboration Listeners:"+r.hasRCollaborationListeners());
					if (selfish || !r.hasRCollaborationListeners()) {
						try {
							if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawLock();

							GDDevice[] devices = r.listDevices();
							for (int i = 0; i < devices.length; ++i) {
								String deviceName = devices[i].getId();
								System.out.println("??? ---- deviceName=" + deviceName);
								session.setAttribute(deviceName, devices[i]);
							}

						} finally {
							if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawUnlock();
						}
					}

					result = session.getId();

					break;

				} else if (command.equals("logondb")) {

					String login = (String) PoolUtils.hexToObject(request.getParameter("login"));
					String pwd = (String) PoolUtils.hexToObject(request.getParameter("pwd"));
					HashMap<String, Object> options = (HashMap<String, Object>) PoolUtils.hexToObject(request.getParameter("options"));
					if (options == null) options = new HashMap<String, Object>();
					System.out.println("options:" + options);
					
					session = request.getSession(true);
					
					Integer sessionTimeOut=null;
					try {
						if (options.get("sessiontimeout")!=null) sessionTimeOut = Integer.decode((String)options.get("sessiontimeout"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (sessionTimeOut!=null) {
						session.setMaxInactiveInterval(sessionTimeOut);
					}
					
					session.setAttribute("TYPE", "DBS");
					session.setAttribute("REGISTRY", (DBLayer) ServerDefaults.getRmiRegistry() );
					session.setAttribute("SUPERVISOR", new SupervisorUtils((DBLayer) ServerDefaults.getRmiRegistry()) );
					session.setAttribute("THREADS", new ThreadsHolder());
					((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).put(session.getId(), session);
					saveSessionAttributes(session);
					
					result = session.getId();

					break;

					
				}

				
				session = request.getSession(false);
				if (session == null) {
					result = new NotLoggedInException();
					break;
				}

				if (command.equals("logoff")) {

					if (session.getAttribute("TYPE").equals("RS")) {
						if (_rkit != null) {
							/*
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
							*/
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
								if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawLock();
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
								if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawUnlock();
							}
						}
					};

					Thread rmiThread = InterruptibleRMIThreadFactory.getInstance().newThread(rmiRunnable);
					((ThreadsHolder) session.getAttribute("THREADS")).getThreads().add(rmiThread);
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
						((ThreadsHolder) session.getAttribute("THREADS")).getThreads().remove(rmiThread);
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
					final Vector<Thread> tvec = (Vector<Thread>) ((ThreadsHolder) session.getAttribute("THREADS")).getThreads().clone();
					for (int i = 0; i < tvec.size(); ++i) {
						try {
							tvec.elementAt(i).interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					stop[0] = true;
					((Vector<Thread>) ((ThreadsHolder) session.getAttribute("THREADS")).getThreads()).removeAllElements();
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
						if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawLock();
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
						if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawUnlock();
					}
				} else if (command.equals("listdevices")) {
					try {
						if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawLock();

						result = new Vector<String>();
						for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements();) {
							String attributeName = e.nextElement();
							if (attributeName.startsWith("device_")) {
								((Vector<String>) result).add(attributeName);
							}
						}

						break;

					} finally {
						if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawUnlock();
					}
				} else if (command.equals("newgenericcallbackdevice")) {
					try {
						if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawLock();
						GenericCallbackDevice genericCallBackDevice = ((RServices) session.getAttribute("R")).newGenericCallbackDevice();
						String genericCallBackDeviceName = genericCallBackDevice.getId();
						session.setAttribute(genericCallBackDeviceName, genericCallBackDevice);
						saveSessionAttributes(session);

						result = genericCallBackDeviceName;

						break;
					} finally {
						if (_rkit != null) ((ExtendedReentrantLock)_rkit.getRLock()).rawUnlock();
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
			PoolUtils.injectSystemProperties(true);ServerDefaults.init();
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