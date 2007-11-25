package org.rosuda.javaGD;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;

public class GDLinePar extends GDObject {
	double lwd;
	int lty;
	volatile BasicStroke bs;

	public GDLinePar(double lwd, int lty) {
		//System.out.println();
		this.lwd = lwd;
		this.lty = lty;
		//System.out.println(">> LINE TYPE: width="+lwd+", type="+Integer.toString(lty,16));
		bs = null;
		if (lty == 0)
			bs = new BasicStroke((float) lwd);
		else if (lty == -1)
			bs = new BasicStroke(0f);
		else {
			int l = 0;
			int dt = lty;
			while (dt > 0) {
				dt >>= 4;
				l++;
			}
			float[] dash = new float[l];
			dt = lty;
			l = 0;
			while (dt > 0) {
				int rl = dt & 15;
				dash[l++] = (float) rl;
				dt >>= 4;
			}
			bs = new BasicStroke((float) lwd, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, dash, 0f);
		}
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (bs != null)
			((Graphics2D) g).setStroke(bs);
	}

	public static Stroke readStroke(ObjectInputStream stream) throws IOException, ClassNotFoundException {

		Stroke result = null;
		boolean isNull = stream.readBoolean();
		if (!isNull) {
			Class<?> c = (Class<?>) stream.readObject();
			if (c.equals(BasicStroke.class)) {
				float width = stream.readFloat();
				int cap = stream.readInt();
				int join = stream.readInt();
				float miterLimit = stream.readFloat();
				float[] dash = (float[]) stream.readObject();
				float dashPhase = stream.readFloat();
				result = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
			} else {
				result = (Stroke) stream.readObject();
			}
		}
		return result;

	}

	public static void writeStroke(Stroke stroke, ObjectOutputStream stream) throws IOException {
		if (stroke != null) {
			stream.writeBoolean(false);
			if (stroke instanceof BasicStroke) {
				BasicStroke s = (BasicStroke) stroke;
				stream.writeObject(BasicStroke.class);
				stream.writeFloat(s.getLineWidth());
				stream.writeInt(s.getEndCap());
				stream.writeInt(s.getLineJoin());
				stream.writeFloat(s.getMiterLimit());
				stream.writeObject(s.getDashArray());
				stream.writeFloat(s.getDashPhase());
			} else {
				stream.writeObject(stroke.getClass());
				stream.writeObject(stroke);
			}
		} else {
			stream.writeBoolean(true);
		}
		stream.flush();
	}

	private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
		GetField gf = aStream.readFields();
		byte[] bs_ba = (byte[]) gf.get("bs", null);
		bs = (BasicStroke) readStroke(new ObjectInputStream(new ByteArrayInputStream(bs_ba)));
	}

	private void writeObject(ObjectOutputStream aStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeStroke(bs, new ObjectOutputStream(baos));
		PutField pf = aStream.putFields();
		pf.put("bs", baos.toByteArray());
		aStream.writeFields();

	}

}