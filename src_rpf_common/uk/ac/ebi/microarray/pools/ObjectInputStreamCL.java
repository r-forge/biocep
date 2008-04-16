package uk.ac.ebi.microarray.pools;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public  class ObjectInputStreamCL extends ObjectInputStream {

	private ClassLoader _classLoader;

	public ObjectInputStreamCL(InputStream in, ClassLoader loader) throws IOException {
		super(in);
		_classLoader = loader;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass v)

	throws IOException, ClassNotFoundException {

		return Class.forName(v.getName(), false, _classLoader);
	}

	@Override
	protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {

		ClassLoader latestLoader = _classLoader;
		ClassLoader nonPublicLoader = null;
		boolean hasNonPublicInterface = false;

		// define proxy in class loader of non-public interface(s), if any
		Class<?>[] classObjs = new Class[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> cl = Class.forName(interfaces[i], false, latestLoader);
			if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
				if (hasNonPublicInterface) {
					if (nonPublicLoader != cl.getClassLoader()) {
						throw new IllegalAccessError("conflicting non-public interface class loaders");
					}
				} else {
					nonPublicLoader = cl.getClassLoader();
					hasNonPublicInterface = true;
				}
			}
			classObjs[i] = cl;
		}
		try {
			return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader : latestLoader, classObjs);
		} catch (IllegalArgumentException e) {
			throw new ClassNotFoundException(null, e);
		}
	}
}