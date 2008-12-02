package org.kchine.r.server.spreadsheet;

import java.util.Vector;

import org.kchine.r.RChar;
import org.kchine.r.RDataFrame;
import org.kchine.r.RList;
import org.kchine.r.RMatrix;
import org.kchine.r.RObject;

public class ExportInfo {

	public ExportInfo(RObject object, String conversionCommand, int commandsNbr) {
		super();
		this.rObject = object;
		this.conversionCommand = conversionCommand;
		this.commandsNbr = commandsNbr;
	}

	RObject rObject;
	String conversionCommand;
	int commandsNbr;

	public RObject getRObject() {
		return rObject;
	}

	public void setRObject(RObject object) {
		rObject = object;
	}

	public String getConversionCommand() {
		return conversionCommand;
	}

	public void setConversionCommand(String conversionCommand) {
		this.conversionCommand = conversionCommand;
	}

	public static boolean isDataFrameType(int dataType) {
		return dataType == ImportInfo.R_DATAFRAME || dataType == ImportInfo.R_DATAFRAME_AUTO_ROW || dataType == ImportInfo.R_DATAFRAME_AUTO_COL
				|| dataType == ImportInfo.R_DATAFRAME_AUTO_ROW_COL;
	}

	public static ExportInfo getExportInfo(CellRange range, int dataType, SpreadsheetTableModelClipboardInterface model) {
		String[] conversionCommandHolder = new String[] { "" };
		RObject result = null;
		int commandsCounter = 0;
		if (!isDataFrameType(dataType) && range.getWidth() == 1) {

			result = new RChar();
			String[] value = new String[range.getHeight()];
			Vector<Integer> na = new Vector<Integer>();
			for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
				if (model.isEmptyCell(i, range.getStartCol())) {
					na.add(i - range.getStartRow());
				} else {
					value[i - range.getStartRow()] = model.getCellAt(i, range.getStartCol()).getValue().toString();
				}
			}

			int[] naTab = new int[na.size()];
			for (int k = 0; k < na.size(); ++k)
				naTab[k] = na.elementAt(k);
			((RChar) result).setIndexNA(naTab);
			((RChar) result).setValue(value);

			switch (dataType) {
			case ImportInfo.R_NUMERIC:
				conversionCommandHolder[0] = "${VAR}" + "=as.numeric(" + "${VAR}" + ");";
				commandsCounter = 1;
				break;
			case ImportInfo.R_CHARACTER:
				conversionCommandHolder[0] = "";
				commandsCounter = 0;
				break;
			case ImportInfo.R_INTEGER:
				conversionCommandHolder[0] = "${VAR}" + "=as.integer(" + "${VAR}" + ");";
				commandsCounter = 1;
				break;
			case ImportInfo.R_LOGICAL:
				for (int i = 0; i < value.length; ++i) {
					if (value[i] != null) {
						if (value[i].equals("true") || value[i].equals("TRUE")) {
							((RChar) result).getValue()[i] = "1";
						} else if (value[i].equals("false") || value[i].equals("FALSE")) {
							((RChar) result).getValue()[i] = "0";
						}
					}
				}
				conversionCommandHolder[0] = "${VAR}" + "=as.logical(as.numeric(" + "${VAR}" + "));";
				commandsCounter = 1;
				break;
			case ImportInfo.R_COMPLEX:
				conversionCommandHolder[0] = "${VAR}" + "=as.complex(" + "${VAR}" + ");";
				commandsCounter = 1;
				break;
			case ImportInfo.R_FACTOR:
				conversionCommandHolder[0] = "${VAR}" + "=as.factor(" + "${VAR}" + ");";
				commandsCounter = 1;
				break;
			default:
				break;
			}

		} else if (!isDataFrameType(dataType) && range.getWidth() > 1) {
			result = new RMatrix();
			int[] dims = new int[] { range.getHeight(), range.getWidth() };
			String[] value = new String[range.getHeight() * range.getWidth()];
			Vector<Integer> na = new Vector<Integer>();
			for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
				for (int j = range.getStartCol(); j <= range.getEndCol(); ++j) {
					int offset = (j - range.getStartCol()) * dims[0] + (i - range.getStartRow());
					if (model.isEmptyCell(i, j)) {
						na.add(offset);
					} else {
						value[offset] = model.getCellAt(i, j).getValue().toString();
					}
				}
			}
			int[] naTab = new int[na.size()];
			for (int k = 0; k < na.size(); ++k)
				naTab[k] = na.elementAt(k);
			((RMatrix) result).setValue(new RChar(value, naTab, null));
			((RMatrix) result).setDim(dims);

			switch (dataType) {
			case ImportInfo.R_NUMERIC:
				conversionCommandHolder[0] = "${VAR}" + "=as.numeric(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				commandsCounter = 2;
				break;
			case ImportInfo.R_CHARACTER:
				conversionCommandHolder[0] = "";
				commandsCounter = 0;
				break;
			case ImportInfo.R_INTEGER:
				conversionCommandHolder[0] = "${VAR}" + "=as.integer(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				commandsCounter = 2;
				break;
			case ImportInfo.R_LOGICAL:
				for (int i = 0; i < value.length; ++i) {
					if (value[i] != null) {
						if (value[i].equals("true") || value[i].equals("TRUE")) {
							((RChar) ((RMatrix) result).getValue()).getValue()[i] = "1";
						} else if (value[i].equals("false") || value[i].equals("FALSE")) {
							((RChar) ((RMatrix) result).getValue()).getValue()[i] = "0";
						}
					}
				}
				conversionCommandHolder[0] = "${VAR}" + "=as.logical(as.numeric(" + "${VAR}" + "));" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				commandsCounter = 2;
				break;
			case ImportInfo.R_COMPLEX:
				conversionCommandHolder[0] = "${VAR}" + "=as.complex(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				commandsCounter = 2;
				break;
			case ImportInfo.R_FACTOR:
				conversionCommandHolder[0] = "${VAR}" + "=as.factor(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				commandsCounter = 2;
				break;
			default:
				break;
			}

		} else if (isDataFrameType(dataType)) {
			
			int colShift = (dataType == ImportInfo.R_DATAFRAME_AUTO_ROW || dataType == ImportInfo.R_DATAFRAME_AUTO_ROW_COL ? 0 : 1);
			int rowShift = (dataType == ImportInfo.R_DATAFRAME_AUTO_COL || dataType == ImportInfo.R_DATAFRAME_AUTO_ROW_COL ? 0 : 1);
			System.out.println("colShift=" + colShift);
			System.out.println("rowShift=" + rowShift);
			
			String[] rownames = new String[range.getHeight() - rowShift];
			if (dataType == ImportInfo.R_DATAFRAME || dataType == ImportInfo.R_DATAFRAME_AUTO_COL) {
				for (int i = range.getStartRow() + rowShift; i <= range.getEndRow(); ++i) {
					String name = model.getCellAt(i, range.getStartCol()).getValue().toString();
					if (name.startsWith("'") && name.endsWith("'")) {
						name = name.substring(1, name.length() - 1);
					}
					rownames[i - (range.getStartRow() + rowShift)] = name;
				}
			} else {
				for (int i = 0; i < rownames.length; ++i) {
					rownames[i] = "<" + (i+1) + ">";
				}
			}

			String[] colnames = new String[range.getWidth() - colShift];
			String[] classnames = new String[range.getWidth() - colShift];

			if (dataType != ImportInfo.R_DATAFRAME_AUTO_COL && dataType != ImportInfo.R_DATAFRAME_AUTO_ROW_COL) {
				for (int j = range.getStartCol() + colShift; j <= range.getEndCol(); ++j) {
					String name = model.getCellAt(range.getStartRow(), j).getValue().toString().trim();
					if (name.endsWith("(numeric)")) {
						classnames[j - (range.getStartCol() + colShift)] = "numeric";
						name = name.substring(0, name.indexOf("(numeric)")).trim();
					} else if (name.endsWith("(logical)")) {
						classnames[j - (range.getStartCol() + colShift)] = "logical";
						name = name.substring(0, name.indexOf("(logical)")).trim();
					} else if (name.endsWith("(integer)")) {
						classnames[j - (range.getStartCol() + colShift)] = "integer";
						name = name.substring(0, name.indexOf("(integer)")).trim();
					} else if (name.endsWith("(complex)")) {
						classnames[j - (range.getStartCol() + colShift)] = "complex";
						name = name.substring(0, name.indexOf("(complex)")).trim();
					} else if (name.endsWith("(factor)")) {
						classnames[j - (range.getStartCol() + colShift)] = "factor";
						name = name.substring(0, name.indexOf("(factor)")).trim();
					} else if (name.endsWith("(character)")) {
						classnames[j - (range.getStartCol() + colShift)] = "character";
						name = name.substring(0, name.indexOf("(character)")).trim();
					}
					if (name.startsWith("'") && name.endsWith("'")) {
						name = name.substring(1, name.length() - 1);
					}
					colnames[j - (range.getStartCol() + colShift)] = name;
				}
			}

			for (int j = 0; j < colnames.length; ++j) {
				if (colnames[j] == null || colnames[j].equals("")) {
					colnames[j] = "column" + (j + 1) ;
				}
				if (classnames[j] == null || classnames[j].equals("")) {
					classnames[j] = "numeric";
				}
			}

			Vector<String> rowNamesVector = new Vector<String>();
			for (int i = 0; i < rownames.length; ++i) {
				if (rownames[i].equals("") || rowNamesVector.contains(rownames[i])) {
					rowNamesVector.add(rownames[i] + "<" + (i + 1) + ">");
				} else {
					rowNamesVector.add(rownames[i]);
				}
			}

			RObject[] elements = new RObject[range.getWidth() - colShift];

			for (int j = range.getStartCol() + colShift; j <= range.getEndCol(); ++j) {
				String[] value = new String[range.getHeight() - rowShift];
				Vector<Integer> na = new Vector<Integer>();
				for (int i = range.getStartRow() + rowShift; i <= range.getEndRow(); i++) {
					if (model.isEmptyCell(i, j)) {
						na.add(i - (range.getStartRow() + rowShift));
					} else {
						value[i - (range.getStartRow() + rowShift)] = model.getCellAt(i, j).getValue().toString();
					}
				}
				int[] naTab = new int[na.size()];
				for (int k = 0; k < na.size(); ++k)
					naTab[k] = na.elementAt(k);
				elements[j - (range.getStartCol() + colShift)] = new RChar(value, naTab, null);
			}

			for (int i = 0; i < colnames.length; ++i) {
				if (!classnames[i].equals("character")) {
					if (classnames[i].equals("logical")) {
						for (int l = 0; l < rownames.length; l++) {
							String lstr = ((RChar) elements[i]).getValue()[l];
							if (lstr != null) {
								if (lstr.equals("false") || lstr.equals("FALSE")) {
									((RChar) elements[i]).getValue()[l] = "0";
								} else if (lstr.equals("true") || lstr.equals("TRUE")) {
									((RChar) elements[i]).getValue()[l] = "1";
								}
							}
						}
						conversionCommandHolder[0] += "${VAR}$" + colnames[i] + "=as.logical(as.numeric(" + "${VAR}$" + colnames[i] + "));";
						++commandsCounter;
					} else {
						conversionCommandHolder[0] += "${VAR}$" + colnames[i] + "=as." + classnames[i] + "(" + "${VAR}$" + colnames[i] + ");";
						++commandsCounter;
					}

				}
			}

			result = new RDataFrame(new RList(elements, colnames), rowNamesVector.toArray(new String[0]));
		} else {
			result = null;
		}
		return new ExportInfo(result, conversionCommandHolder[0], commandsCounter);
	}

	public int getCommandsNbr() {
		return commandsNbr;
	}

	public void setCommandsNbr(int commandsNbr) {
		this.commandsNbr = commandsNbr;
	}

}
