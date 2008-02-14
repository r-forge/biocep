###################################################
#    R character vector to Java 
###################################################
cvtCharArrayToJava <- function(x, ...) {
    ans <- .Call("RCharVector_JavaStringArray", x)
    ans
}

matchCharArrayToJava <- function(x, ...) {
    is.vector(x) && is.character(x) && (length(x)!=1)
}

cvtCharScalarToJava <- function(x, ...) {
    ans <- .Call("RCharScalar_JavaString", x)
    ans
}

matchCharScalarToJava <- function(x, ...) {
    is.vector(x) && is.character(x) && (length(x)==1)
}

###################################################
#    R integer vector to Java 
###################################################
cvtIntegerArrayToJava <- function(x, ...) {
    ans <- .Call("RIntegerVector_JavaIntArray", x)
    ans
}

matchIntegerArrayToJava <- function(x, ...) {
    is.vector(x) && is.integer(x) && (length(x)!=1)
}

cvtIntegerScalarToJava <- function(x, ...) {
    ans <- .Call("RIntegerScalar_JavaInteger", x)
    ans
}

matchIntegerScalarToJava <- function(x, ...) {
    is.vector(x) && is.integer(x) && (length(x)==1)
}

###################################################
#    R integer vector to Java 
###################################################
cvtNumericArrayToJava <- function(x, ...) {
    ans <- .Call("RNumericVector_JavaDoubleArray", x)
    ans
}

matchNumericArrayToJava <- function(x, ...) {
    is.vector(x) && is.double(x) && (length(x)!=1)
}

cvtNumericScalarToJava <- function(x, ...) {
    ans <- .Call("RNumericScalar_JavaDouble", x)
    ans
}

matchNumericScalarToJava <- function(x, ...) {
    is.vector(x) && is.double(x) && (length(x)==1)
}

###################################################
#    R logical vector to Java 
###################################################
cvtLogicalArrayToJava <- function(x, ...) {
    ans <- .Call("RLogicalVector_JavaBooleanArray", x)
    ans
}

matchLogicalArrayToJava <- function(x, ...) {
    is.vector(x) && is.logical(x) && (length(x)!=1)
}

cvtLogicalScalarToJava <- function(x, ...) {
    ans <- .Call("RLogicalScalar_JavaBoolean", x)
    ans
}

matchLogicalScalarToJava <- function(x, ...) {
    is.vector(x) && is.logical(x) && (length(x)==1)
}

regTestCvt <- function(){
    setJavaFunctionConverter(cvtCharArrayToJava, matchCharArrayToJava, 
                             description="R char  to String[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtCharScalarToJava, matchCharScalarToJava, 
                             description="R char  to String",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtIntegerArrayToJava, matchIntegerArrayToJava, 
                             description="R integer  to int[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtIntegerScalarToJava, matchIntegerScalarToJava, 
                             description="R integer  to Integer",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtNumericArrayToJava, matchNumericArrayToJava, 
                             description="R numeric to double[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtNumericScalarToJava, matchNumericScalarToJava, 
                             description="R numeric  to Double",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtLogicalArrayToJava, matchLogicalArrayToJava, 
                             description="R logical to boolean[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtLogicalScalarToJava, matchLogicalScalarToJava, 
                             description="R logical  to Boolean",
                             fromJava=FALSE, position=1)
    ""
}
