package net.java.dev.jspreadsheet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.bioconductor.packages.rservices.RInteger;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.rservices.RObject;

class FunctionR extends Function {
	private String rFucntionName;

	public FunctionR(String funcName) {
		super();
		rFucntionName = funcName;
	}

	public String getDescription() {
		return "Executes the R function <" + rFucntionName + "> on a set of values.";
	}

	public String getUsage() {
		return rFucntionName + "(value1,value2,...)";
	}

	public Number evaluate(SpreadsheetTableModelInterface table, Node node, int row, int col) throws ParserException {
		// requires parameters
		checkParamsExist(node);

		LinkedList params = node.getParams();
		RObject v=null;
		if (params != null) {
			// go over the parameters
			Iterator it = params.iterator();

			Vector<Double> input = new Vector<Double>();
			Vector<Integer> NA = new Vector<Integer>();

			int counter = 0;
			while (it.hasNext()) {
				// get this parameter
				Node exp = (Node) it.next();

				// if it's a range of cells
				if (isRange(exp)) {
					CellPoint[] addr = getFirst(exp).getAddressRange(row, col);

					// for a range, go over the whole range
					for (int j = addr[0].getCol(); j <= addr[1].getCol(); j++)
						for (int i = addr[0].getRow(); i <= addr[1].getRow(); i++)
						{
							// get the numeric value of that cell
							if (table.isEmptyCell(i, j)) {
								NA.add(counter);
								input.add((double) 0);
							} else {
								input.add((double) table.getNumericValueAt(i, j).floatValue());
							}

							++counter;
						}
					
				} else {
					// evaluate this parameter's expression (sub-formula)
					if (table.isEmptyCell(row, col)) {
						NA.add(counter);
						input.add((double) 0);
					} else {
						input.add((double) Formula.evaluate(table, exp.getExp(), row, col).floatValue());
					}
					++counter;
				}

			}
			double[] values = new double[input.size()];
			for (int i = 0; i < input.size(); ++i)
				values[i] = input.elementAt(i);
			int[] na = new int[NA.size()];
			for (int i = 0; i < NA.size(); ++i)
				na[i] = NA.elementAt(i);
			v = new RNumeric(values, na, null);

		} else {
			v=null;
		}

		try {
			System.out.println("!!!!!! v="+v);
			RObject result= (v!=null?(RObject)table.getR().call(rFucntionName, v):(RObject)table.getR().call(rFucntionName)) ;
			System.out.println("!!!!!! result="+result);
			
			if (table.getR().getStatus().contains("ERROR")) {
				JOptionPane.showMessageDialog(null, table.getR().getStatus());
				return null;
			}
			
			if (result instanceof RNumeric && ((RNumeric)result).getValue().length==1 ) {
				return new Double(((RNumeric)result).getValue()[0]);
			} else if (result instanceof RInteger && ((RInteger)result).getValue().length==1 ) {
				return new Integer(((RInteger)result).getValue()[0]);
			} else  {
				JOptionPane.showMessageDialog(null, "Bad result Format :"+result.toString() );
				return null;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return null;
		}

	}
}
