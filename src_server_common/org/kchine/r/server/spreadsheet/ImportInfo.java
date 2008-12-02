package org.kchine.r.server.spreadsheet;


import java.util.HashSet;


import org.kchine.r.RChar;
import org.kchine.r.RComplex;
import org.kchine.r.RDataFrame;
import org.kchine.r.RFactor;
import org.kchine.r.RInteger;
import org.kchine.r.RList;
import org.kchine.r.RLogical;
import org.kchine.r.RMatrix;
import org.kchine.r.RNumeric;
import org.kchine.r.RObject;
import org.kchine.r.RVector;

public class ImportInfo {

	public ImportInfo(int nrow, int ncol, int dtype, String tabString) {
		super();
		this.nrow = nrow;
		this.ncol = ncol;
		this.dtype = dtype;
		this.tabString = tabString;
	}

	int nrow = 0;
	int ncol = 0;
	int dtype = 0;
	String tabString = null;
	public static final String[] R_TYPES_NAMES = { "numeric", "character", "integer", "logical", "complex", "factor", "data.frame", "data.frame.auto.row", "data.frame.auto.col", "data.frame.auto.row.col" };
	public static final int R_DATAFRAME_AUTO_ROW_COL = 9;
	public static final int R_DATAFRAME_AUTO_COL = 8;
	public static final int R_DATAFRAME_AUTO_ROW = 7;
	public static final int R_DATAFRAME = 6;
	public static final int R_FACTOR = 5;
	public static final int R_COMPLEX = 4;
	public static final int R_LOGICAL = 3;
	public static final int R_INTEGER = 2;
	public static final int R_CHARACTER = 1;
	public static final int R_NUMERIC = 0;

	public int getNrow() {
		return nrow;
	}

	public void setNrow(int nrow) {
		this.nrow = nrow;
	}

	public int getNcol() {
		return ncol;
	}

	public void setNcol(int ncol) {
		this.ncol = ncol;
	}

	public int getDtype() {
		return dtype;
	}

	public void setDtype(int dtype) {
		this.dtype = dtype;
	}

	public String getTabString() {
		return tabString;
	}

	public void setTabString(String tabString) {
		this.tabString = tabString;
	}

	public static int getRow(String c) throws Exception {
		int i = 0;
		while (Character.isLetter(c.charAt(i)))
			++i;
		return Node.translateRow(c.substring(i));
	}

	public static int getCol(String c) throws Exception {
		int i = 0;
		while (Character.isLetter(c.charAt(i)))
			++i;
		return Node.translateColumn(c.substring(0, i));
	}

	public static CellRange getRange(String s) throws Exception {
		try {
			int idx = s.indexOf(":");
			String c1 = s.substring(0, idx);
			String c2 = s.substring(idx + 1);
			return new CellRange(getRow(c1), getRow(c2), getCol(c1), getCol(c2));
		} catch (Exception e) {
			throw new Exception("Bad Cell Range");
		}
	}

	public static ImportInfo getImportInfo(RObject robj) {

		StringBuffer sb = new StringBuffer();
		int nrow = 0;
		int ncol = 0;
		int dtype = 0;

		if (robj instanceof RNumeric) {
			RNumeric rnum = (RNumeric) robj;
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rnum.getIndexNA() != null)
				for (int i = 0; i < rnum.getIndexNA().length; ++i)
					NASet.add(rnum.getIndexNA()[i]);
			for (int i = 0; i < rnum.length(); ++i) {
				if (NASet.contains(i)) {
					sb.append("");
				} else {
					sb.append(rnum.getValue()[i]);
				}
				sb.append("\n");
			}

			nrow = rnum.length();
			ncol = 1;
			dtype = ImportInfo.R_NUMERIC;

		} else if (robj instanceof RInteger) {
			RInteger rint = (RInteger) robj;
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rint.getIndexNA() != null)
				for (int i = 0; i < rint.getIndexNA().length; ++i)
					NASet.add(rint.getIndexNA()[i]);

			for (int i = 0; i < rint.length(); ++i) {
				if (NASet.contains(i)) {
					sb.append("");
				} else {
					sb.append(rint.getValue()[i]);
				}
				sb.append("\n");
			}
			nrow = rint.length();
			ncol = 1;
			dtype = ImportInfo.R_INTEGER;
		} else if (robj instanceof RLogical) {
			RLogical rlogical = (RLogical) robj;
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rlogical.getIndexNA() != null)
				for (int i = 0; i < rlogical.getIndexNA().length; ++i)
					NASet.add(rlogical.getIndexNA()[i]);
			for (int i = 0; i < rlogical.length(); ++i) {
				if (NASet.contains(i)) {
					sb.append("");
				} else {
					sb.append(rlogical.getValue()[i]);
				}
				sb.append("\n");
			}
			nrow = rlogical.length();
			ncol = 1;
			dtype = ImportInfo.R_LOGICAL;

		} else if (robj instanceof RChar) {
			RChar rchar = (RChar) robj;
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rchar.getIndexNA() != null)
				for (int i = 0; i < rchar.getIndexNA().length; ++i)
					NASet.add(rchar.getIndexNA()[i]);
			for (int i = 0; i < rchar.length(); ++i) {
				if (NASet.contains(i)) {
					sb.append("");
				} else {
					sb.append(rchar.getValue()[i]);
				}
				sb.append("\n");
			}
			nrow = rchar.length();
			ncol = 1;
			dtype = ImportInfo.R_CHARACTER;
		} else if (robj instanceof RFactor) {
			String[] data = ((RFactor) robj).asData();
			HashSet<Integer> NASet = new HashSet<Integer>();
			for (int i = 0; i < data.length; ++i) {
				if (data[i].equals("NA")) {
					sb.append("");
				} else {
					sb.append(data[i]);
				}
				sb.append("\n");
			}
			nrow = data.length;
			ncol = 1;
			dtype = ImportInfo.R_FACTOR;
		} else if (robj instanceof RComplex) {
			RComplex rcomplex = (RComplex) robj;
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rcomplex.getIndexNA() != null)
				for (int i = 0; i < rcomplex.getIndexNA().length; ++i)
					NASet.add(rcomplex.getIndexNA()[i]);
			for (int i = 0; i < rcomplex.length(); ++i) {
				if (NASet.contains(i)) {
					sb.append("");
				} else {
					sb.append(rcomplex.getReal()[i] + "+" + rcomplex.getImaginary()[i] + "i");
				}
				sb.append("\n");
			}
			nrow = rcomplex.length();
			ncol = 1;
			dtype = ImportInfo.R_COMPLEX;
		} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RNumeric) {
			int[] dims = ((RMatrix) robj).getDim();
			RNumeric rnum = (RNumeric) ((RMatrix) robj).getValue();
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rnum.getIndexNA() != null)
				for (int i = 0; i < rnum.getIndexNA().length; ++i)
					NASet.add(rnum.getIndexNA()[i]);
			for (int i = 0; i < dims[0]; ++i) {
				for (int j = 0; j < dims[1]; ++j) {
					int offset = j * dims[0] + i;
					if (NASet.contains(offset)) {
						sb.append("");
					} else {
						sb.append(rnum.getValue()[offset]);
					}
					if (j == (dims[1] - 1)) {
					} else
						sb.append('\t');
				}
				sb.append('\n');
			}
			nrow = dims[0];
			ncol = dims[1];
			dtype = ImportInfo.R_NUMERIC;
		} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RInteger) {
			int[] dims = ((RMatrix) robj).getDim();
			RInteger rint = (RInteger) ((RMatrix) robj).getValue();
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rint.getIndexNA() != null)
				for (int i = 0; i < rint.getIndexNA().length; ++i)
					NASet.add(rint.getIndexNA()[i]);
			for (int i = 0; i < dims[0]; ++i) {
				for (int j = 0; j < dims[1]; ++j) {
					int offset = j * dims[0] + i;
					if (NASet.contains(offset)) {
						sb.append("");
					} else {
						sb.append(rint.getValue()[offset]);
					}
					if (j == (dims[1] - 1)) {
					} else
						sb.append('\t');
				}
				sb.append('\n');
			}
			nrow = dims[0];
			ncol = dims[1];
			dtype = ImportInfo.R_INTEGER;
		} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RChar) {
			int[] dims = ((RMatrix) robj).getDim();
			RChar rchar = (RChar) ((RMatrix) robj).getValue();
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rchar.getIndexNA() != null)
				for (int i = 0; i < rchar.getIndexNA().length; ++i)
					NASet.add(rchar.getIndexNA()[i]);
			for (int i = 0; i < dims[0]; ++i) {
				for (int j = 0; j < dims[1]; ++j) {
					int offset = j * dims[0] + i;
					if (NASet.contains(offset)) {
						sb.append("");
					} else {
						sb.append(rchar.getValue()[offset]);
					}
					if (j == (dims[1] - 1)) {
					} else
						sb.append('\t');
				}
				sb.append('\n');
			}
			nrow = dims[0];
			ncol = dims[1];
			dtype = ImportInfo.R_CHARACTER;
		} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RLogical) {
			int[] dims = ((RMatrix) robj).getDim();
			RLogical rlogical = (RLogical) ((RMatrix) robj).getValue();
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rlogical.getIndexNA() != null)
				for (int i = 0; i < rlogical.getIndexNA().length; ++i)
					NASet.add(rlogical.getIndexNA()[i]);
			for (int i = 0; i < dims[0]; ++i) {
				for (int j = 0; j < dims[1]; ++j) {
					int offset = j * dims[0] + i;
					if (NASet.contains(offset)) {
						sb.append("");
					} else {
						sb.append(rlogical.getValue()[offset]);
					}
					if (j == (dims[1] - 1)) {
					} else
						sb.append('\t');
				}
				sb.append('\n');
			}
			nrow = dims[0];
			ncol = dims[1];
			dtype = ImportInfo.R_LOGICAL;
		} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RComplex) {
			int[] dims = ((RMatrix) robj).getDim();
			RComplex rcomplex = (RComplex) ((RMatrix) robj).getValue();
			HashSet<Integer> NASet = new HashSet<Integer>();
			if (rcomplex.getIndexNA() != null)
				for (int i = 0; i < rcomplex.getIndexNA().length; ++i)
					NASet.add(rcomplex.getIndexNA()[i]);
			for (int i = 0; i < dims[0]; ++i) {
				for (int j = 0; j < dims[1]; ++j) {
					int offset = j * dims[0] + i;
					if (NASet.contains(offset)) {
						sb.append("");
					} else {
						sb.append(rcomplex.getReal()[offset] + "+" + rcomplex.getImaginary()[offset] + "i");
					}
					if (j == (dims[1] - 1)) {
					} else
						sb.append('\t');
				}
				sb.append('\n');
			}
			nrow = dims[0];
			ncol = dims[1];
			dtype = ImportInfo.R_COMPLEX;
		} else if (robj instanceof RDataFrame) {

			System.out.println(robj);

			RList list = ((RDataFrame) robj).getData();
			sb.append("\t");
			for (int i = 0; i < list.getValue().length; ++i) {
				if (list.getNames() != null) {

					//sb.append("'" + list.getNames()[i] + "'");
					sb.append( list.getNames()[i] );
					
					sb.append(" ");

					Class<?> elementClass = list.getValue()[i].getClass();
					if (elementClass == RNumeric.class) {
						sb.append("(numeric)");
					} else if (elementClass == RInteger.class) {
						sb.append("(integer)");
					} else if (elementClass == RChar.class) {
						sb.append("(character)");
					} else if (elementClass == RLogical.class) {
						sb.append("(logical)");
					} else if (elementClass == RComplex.class) {
						sb.append("(complex)");
					} else if (elementClass == RFactor.class) {
						sb.append("(factor)");
					}

					if (i == (list.getValue().length - 1)) {

					} else {
						sb.append("\t");
					}

				}
			}
			sb.append("\n");

			int nrow0 = -1;
			RObject robj0 = list.getValue()[0];

			if (robj0 instanceof RNumeric) {
				nrow0 = ((RNumeric) robj0).getValue().length;
			} else if (robj0 instanceof RInteger) {
				nrow0 = ((RInteger) robj0).getValue().length;
			} else if (robj0 instanceof RChar) {
				nrow0 = ((RChar) robj0).getValue().length;
			} else if (robj0 instanceof RLogical) {
				nrow0 = ((RLogical) robj0).getValue().length;
			} else if (robj0 instanceof RComplex) {
				nrow0 = ((RComplex) robj0).getReal().length;
			} else if (robj0 instanceof RFactor) {
				nrow0 = ((RFactor) robj0).asData().length;
			}

			String[] rownames = ((RDataFrame) robj).getRowNames();
			HashSet<Integer>[] NASet = new HashSet[list.getValue().length];
			for (int i = 0; i < list.getValue().length; ++i) {
				NASet[i] = new HashSet<Integer>();

				int[] indexNA = null;
				try {
					indexNA = (int[]) list.getValue()[i].getClass().getMethod("getIndexNA").invoke(list.getValue()[i]);
				} catch (Exception e) {
				}

				if (indexNA != null)
					for (int j = 0; j < indexNA.length; ++j)
						NASet[i].add(indexNA[j]);
			}

			for (int i = 0; i < nrow0; ++i) {
				
				//sb.append("'" + rownames[i] + "'" + "\t");
				sb.append(rownames[i] + "\t");
				
				for (int j = 0; j < list.getValue().length; ++j) {

					if (NASet[j].contains(i)) {
						sb.append("");
					} else {
						if (list.getValue()[j] instanceof RFactor) {
							sb.append(((RFactor) list.getValue()[j]).asData()[i]);
						} else {
							RVector v = (RVector) list.getValue()[j];
							if (v instanceof RNumeric) {
								sb.append(((RNumeric) v).getValue()[i]);
							} else if (v instanceof RInteger) {
								sb.append(((RInteger) v).getValue()[i]);
							} else if (v instanceof RChar) {
								sb.append(((RChar) v).getValue()[i]);
							} else if (v instanceof RLogical) {
								sb.append(((RLogical) v).getValue()[i]);
							} else if (v instanceof RComplex) {
								sb.append(((RComplex) v).getReal()[i] + "+" + ((RComplex) v).getImaginary()[i] + "i");
							}
						}
					}

					if (j == (list.getValue().length - 1)) {
					} else {
						sb.append("\t");
					}
				}

				sb.append("\n");
			}

			nrow = nrow0 + 1;
			ncol = list.getValue().length + 1;
			dtype = ImportInfo.R_DATAFRAME;
		}

		return new ImportInfo(nrow, ncol, dtype, sb.toString());
	}

}
