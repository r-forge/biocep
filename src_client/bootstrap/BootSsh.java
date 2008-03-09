package bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

public class BootSsh {
	public static final String STUB_BEGIN_MARKER="#STUBBEGIN#";
	public static final String STUB_END_MARKER="#STUBEND#";
	
	public static final String PROCESS_ID_BEGIN_MARKER="#PROCESSIDBEGIN#";
	public static final String PROCESS_ID_END_MARKER="#PROCESSIDEND#";	

	public static final String R_PROCESS_ID_BEGIN_MARKER="#RPROCESSIDBEGIN#";
	public static final String R_PROCESS_ID_END_MARKER="#RPROCESSIDEND#";	

	public static void main(String[] args) {
		try {
			URLClassLoader cl = new URLClassLoader(new URL[] { new URL("http://" + args[1] + ":" + args[2] + "/classes/") }, BootSsh.class.getClassLoader());
			cl.loadClass("uk.ac.ebi.microarray.pools.PoolUtils").getMethod("startPortInUseDogwatcher",
					new Class<?>[] { String.class, int.class, int.class, int.class }).invoke(null, args[1], Integer.decode(args[2]), 3, 3);
			Class<?> ServerLauncherClass = cl.loadClass("graphics.rmi.ServerLauncher");
			Remote r = (Remote) ServerLauncherClass.getMethod("createR",
					new Class<?>[] { boolean.class, String.class, int.class, String.class, int.class, int.class, int.class, boolean.class }).invoke(
					null,
					new Object[] { new Boolean(args[0]).booleanValue(), args[1], Integer.decode(args[2]).intValue(), args[3],
							Integer.decode(args[4]).intValue(), Integer.decode(args[5]).intValue(), Integer.decode(args[6]).intValue(), false });
			
			System.out.println(STUB_BEGIN_MARKER+ stubToHex(r)+ STUB_END_MARKER);	
			
			Class<?> poolUtilsClass = cl.loadClass("uk.ac.ebi.microarray.pools.PoolUtils");			
			String processId=(String)poolUtilsClass.getMethod("getProcessId", new Class<?>[0]).invoke(null, new Object[0]);
			System.out.println(PROCESS_ID_BEGIN_MARKER+processId+ PROCESS_ID_END_MARKER);
			
			System.out.println(R_PROCESS_ID_BEGIN_MARKER+(String)r.getClass().getMethod("getProcessId", new Class<?>[0]).invoke(r, new Object[0])+ R_PROCESS_ID_END_MARKER);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static String stubToHex(Remote obj) throws NoSuchObjectException {
		if (obj instanceof UnicastRemoteObject) {
			obj = java.rmi.server.RemoteObject.toStub(obj);
		}
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baoStream).writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String stub_hex = bytesToHex(baoStream.toByteArray());
		return stub_hex;
	}

	public static String bytesToHex(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;
		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);
		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(pseudo[(int) ch]);
			ch = (byte) (in[i] & 0x0F);
			out.append(pseudo[(int) ch]);
			i++;
		}
		String rslt = new String(out);
		return rslt;
	}

}
