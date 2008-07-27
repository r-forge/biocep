/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
package server;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface RConst {
	/** xpression type: NULL */
	public static final int XT_NULL = 0;

	/** xpression type: integer */
	public static final int XT_INT = 1;

	/** xpression type: double */
	public static final int XT_DOUBLE = 2;

	/** xpression type: String */
	public static final int XT_STR = 3;

	/** xpression type: language construct (currently content is same as list) */
	public static final int XT_LANG = 4;

	/** xpression type: symbol (content is symbol name: String) */
	public static final int XT_SYM = 5;

	/** xpression type: RBool */
	public static final int XT_BOOL = 6;

	/** xpression type: Vector */
	public static final int XT_VECTOR = 16;

	/** xpression type: RList */
	public static final int XT_LIST = 17;

	/**
	 * xpression type: closure (there is no java class for that type (yet?).
	 * currently the body of the closure is stored in the content part of the
	 * REXP. Please note that this may change in the future!)
	 */
	public static final int XT_CLOS = 18;

	/** xpression type: int[] */
	public static final int XT_ARRAY_INT = 32;

	/** xpression type: double[] */
	public static final int XT_ARRAY_DOUBLE = 33;

	/** xpression type: String[] (currently not used, Vector is used instead) */
	public static final int XT_ARRAY_STR = 34;

	/** internal use only! this constant should never appear in a REXP */
	public static final int XT_ARRAY_BOOL_UA = 35;

	/** xpression type: RBool[] */
	public static final int XT_ARRAY_BOOL = 36;

	/** xpression type: int[] to be interpreted as boolean */
	public static final int XT_ARRAY_BOOL_INT = 37;

	/** xpression type: unknown; no assumptions can be made about the content */
	public static final int XT_UNKNOWN = 48;

	/** xpression type: pure reference, no internal type conversion performed */
	public static final int XT_NONE = -1;

	/**
	 * xpression type: RFactor; this XT is internally generated (ergo is does
	 * not come from Rsrv.h) to support RFactor class which is built from
	 * XT_ARRAY_INT
	 */
	public static final int XT_FACTOR = 127;

	/* internal SEXP types in R - taken directly from Rinternals.h */
	public static final int NILSXP = 0; /* nil = NULL */

	public static final int SYMSXP = 1; /* symbols */

	public static final int LISTSXP = 2; /* lists of dotted pairs */

	public static final int CLOSXP = 3; /* closures */

	public static final int ENVSXP = 4; /* environments */

	public static final int PROMSXP = 5; /*
	 * promises: [un]evaluated closure
	 * arguments
	 */

	public static final int LANGSXP = 6; /*
	 * language constructs (special
	 * lists)
	 */

	public static final int SPECIALSXP = 7; /* special forms */

	public static final int BUILTINSXP = 8; /* builtin non-special forms */

	public static final int CHARSXP = 9; /*
	 * "scalar" string type (internal
	 * only)
	 */

	public static final int LGLSXP = 10; /* logical vectors */

	public static final int INTSXP = 13; /* integer vectors */

	public static final int REALSXP = 14; /* real variables */

	public static final int CPLXSXP = 15; /* complex variables */

	public static final int STRSXP = 16; /* string vectors */

	public static final int DOTSXP = 17; /* dot-dot-dot object */

	public static final int ANYSXP = 18; /* make "any" args work */

	public static final int VECSXP = 19; /* generic vectors */

	public static final int EXPRSXP = 20; /* expressions vectors */

	public static final int BCODESXP = 21; /* byte code */

	public static final int EXTPTRSXP = 22; /* external pointer */

	public static final int WEAKREFSXP = 23; /* weak reference */

	public static final int RAWSXP = 24; /* raw bytes */

	public static final int S4SXP = 25; /* S4 object */

	public static final int FUNSXP = 99; /* Closure or Builtin */

}
