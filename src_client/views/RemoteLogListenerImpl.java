package views;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.SwingUtilities;

import uk.ac.ebi.microarray.pools.RemoteLogListener;

public class RemoteLogListenerImpl extends UnicastRemoteObject implements RemoteLogListener {
	private ServerLogView _serverLogView;

	public RemoteLogListenerImpl(ServerLogView serverLogView) throws RemoteException {
		super();
		_serverLogView = serverLogView;
	}

	public void scrollToEnd() {
		_serverLogView.getScrollPane().getVerticalScrollBar().setValue(_serverLogView.getScrollPane().getVerticalScrollBar().getMaximum());
	}

	public void flush() throws RemoteException {
	}

	public void write(final byte[] b) throws RemoteException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_serverLogView.getArea().append(new String(b));
				scrollToEnd();
				_serverLogView.getArea().repaint();
			}
		});
	}

	public void write(final byte[] b, final int off, final int len) throws RemoteException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_serverLogView.getArea().append(new String(b, off, len));
				scrollToEnd();
				_serverLogView.getArea().repaint();
			}
		});
	}

	public void write(final int b) throws RemoteException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_serverLogView.getArea().append(new String(new byte[] { (byte) b, (byte) (b >> 8) }));
				scrollToEnd();
				_serverLogView.getArea().repaint();
			}
		});
	}

}