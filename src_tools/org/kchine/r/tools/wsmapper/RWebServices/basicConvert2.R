########################################
# Authors : Nianhua Li, MT Morgan
# License : caBIG 
########################################

###################################################
#    Java class org.kchine.r.RJComplex to R complex   
###################################################
cvtComplexFromJava2 <- function(x, thisClassName) {
   real <- .Java(x, "getReal") 
   imaginary <- .Java(x, "getImaginary")
   ans <- complex(real=real, imaginary=imaginary)
   ans
}
  
matchComplexFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJComplex"
}

###################################################
#    R character vector to Java String[]
###################################################
cvtCharacterToJava2 <- function(x, ...) {
    if (any(is.na(x)))
	stop("Can't convert NA in R character to Java.")
    ans <- .Call("RCharVector_JavaStringArray", x)
    ans
}

matchCharacterToJava2 <- function(x, ...) {
    is.vector(x) && is.character(x)
}

###################################################
#    R integer vector to Java int[]
###################################################
cvtIntegerToJava2 <- function(x, ...) {
    if (any(is.na(x)))
	stop("Can't convert NA in R integer to Java.")
    ans <- .Call("RIntegerVector_JavaIntArray", x)
    ans
}

matchIntegerToJava2 <- function(x, ...) {
    is.vector(x) && is.integer(x)
}

###################################################
#    R numeric vector to Java double[]
###################################################
cvtNumericToJava2 <- function(x, ...) {
    if (any(is.na(x[!is.nan(x)])))
	stop("Can't convert NA in R double to Java.")
    ans <- .Call("RNumericVector_JavaDoubleArray", x)
    ans
}

matchNumericToJava2 <- function(x, ...) {
    is.vector(x) && is.double(x)
}
    
###################################################
#    R logical vector to Java boolean[]
###################################################
cvtLogicalToJava2 <- function(x, ...) {
    if (any(is.na(x)))
	stop("Can't convert NA in R logical to Java.")
    ans <- .Call("RLogicalVector_JavaBooleanArray", x)
    ans
}

matchLogicalToJava2 <- function(x, ...) {
    is.vector(x) && is.logical(x)
}

###################################################
#    R raw vector to Java byte[]
###################################################
cvtRawToJava2 <- function(x, ...) {
    ans <- .Call("RRawVector_JavaByteArray", x)
    ans
}

matchRawToJava2 <- function(x, ...) {
    is.vector(x) && (typeof(x)=="raw")
}
    
###################################################
#    R complex vector to Java org.kchine.r.RJComplex                                       
###################################################
cvtComplexToJava2 <- function(x, ...) {
    real <- .Call("RNumericVector_JavaDoubleArray", Re(x))
    imaginary <- .Call("RNumericVector_JavaDoubleArray", Im(x))
    ans <- .JNew("org.kchine.r.RJComplex", real, imaginary, .convert=FALSE)
    ans
}

matchComplexToJava2 <- function(x, ...) {
    is.vector(x) && is.complex(x)
}

###################################################
#    R list to Java Object[]
###################################################
cvtListToJava2 <- function(x, ...) {
    ans <- .Call("RList_JavaObjectArray", x)
    ans
}

matchListToJava2 <- function(x, ...) {
    inherits(x,"list") && (!is.array(x)) && (!is.data.frame(x))
}

###################################################
#    Java class org.kchine.r.RJFactor to R factor  
###################################################
cvtFactorFromJava2 <- function(x, thisClassName) {
    theCode <- x$getCode()
    theCode[theCode==-1] <- NA
    theCode <- theCode +1
    theLevels <- x$getLevels()
    if(is.null(theLevels))
        ans <- factor(theCode)
    else
        ans <- factor(theCode, labels=theLevels)
    ans
}

matchFactorFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJFactor"
}
    
###################################################
#    R factor to Java class org.kchine.r.RJFactor
###################################################
cvtFactorToJava2 <- function(x, ...) {
    code <- as.integer(x)-1
    code[is.na(code)] <- -1
    code <- .Call("RIntegerVector_JavaIntArray", as.integer(code))
    level <- .Call("RCharVector_JavaStringArray", levels(x))
    v <- .JNew("org.kchine.r.RJFactor", .convert=FALSE)
    .Java(v, "setLevels", level, .convert=FALSE)
    .Java(v, "setCode", code, .convert=FALSE)
    v
}

matchFactorToJava2 <- function(x, ...) {
    is.factor(x)
}
    
###################################################
#    Java class org.kchine.r.RJDataFrame to R data.frame  
###################################################
cvtDataFrameFromJava2 <- function(x, thisClassName) {
    theData <- .Java(x, "getData")
    theRowNames <- x$getRowNames()
    ans <- data.frame(theData, row.names=theRowNames, stringsAsFactors=FALSE)
    ans
}

matchDataFrameFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJDataFrame"
}
    
###################################################
#    R data.frame to Java class org.kchine.r.RJDataFrame
###################################################
cvtDataFrameToJava2 <- function(x, ...) {
    v <- .JNew("org.kchine.r.RJDataFrame", .convert=FALSE)
    dfdata <- cvtListToJava2(as.list(x))
    .Java(v, "setData", dfdata, .convert=FALSE)
    rowname <- .Call("RCharVector_JavaStringArray", row.names(x))
    .Java(v, "setRowNames", rowname, .convert=FALSE)
    v
}

matchDataFrameToJava2 <- function(x, ...) {
    is.data.frame(x)
}

###################################################
#    helper class
###################################################
cvtArrayFromJavaUtil2 <- function(x) {
    value <- .Java(x, "getValue")
    ans <- array(value)
    theDim <- .Java(x, "getDim")
    theDimnames <- .Java(x, "getDimnames")
    if (!is.null(theDim))
        dim(ans) <- theDim
    dimnames(ans) <- theDimnames
    ans
}

cvtArrayToJavaUtil2 <- function(x, ans) {
    theDim <- cvtIntegerToJava2(dim(x))
    .Java(ans, "setDim", theDim, .convert=FALSE)
    theDimnames <- dimnames(x)
    if (!is.null(theDimnames)) {
        theDimnames <- cvtListToJava2(theDimnames)
        .Java(ans, "setDimnames", theDimnames, .convert=FALSE)
    }
    ans
}
###################################################
#    Java class org.kchine.r.RJCharArray to R CharArray 
###################################################
cvtCharArrayFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("CharArray", ans)
}

matchCharArrayFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJCharArray"
}
    
###################################################
#    R CharArray to Java class org.kchine.r.RJCharArray
###################################################
cvtCharArrayToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJCharArray", .convert=FALSE)
    value <- cvtCharacterToJava2(as.character(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchCharArrayToJava2 <- function(x, ...) {
    class(x)=="CharArray"
    ##is.array(x) && (!is.matrix(x)) && identical(typeof(x), "character")
}

###################################################
#    Java class org.kchine.r.RJCharMatrix to R CharMatrix 
###################################################
cvtCharMatrixFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("CharMatrix", ans)
}

matchCharMatrixFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJCharMatrix"
}
    
###################################################
#    R CharMatrix to Java class org.kchine.r.RJCharMatrix
###################################################
cvtCharMatrixToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJCharMatrix", .convert=FALSE)
    value <- cvtCharacterToJava2(as.character(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchCharMatrixToJava2 <- function(x, ...) {
    class(x)=="CharMatrix"
    ##is.array(x) && (!is.matrix(x)) && identical(typeof(x), "character")
}

###################################################
# Java class org.kchine.r.RJIntegerArray to R IntegerArray 
###################################################
cvtIntegerArrayFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("IntegerArray", ans)
}

matchIntegerArrayFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJIntegerArray"
}
    
###################################################
# R IntegerArray to Java class org.kchine.r.RJIntegerArray
###################################################
cvtIntegerArrayToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJIntegerArray", .convert=FALSE)
    value <- cvtIntegerToJava2(as.integer(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchIntegerArrayToJava2 <- function(x, ...) {
    class(x)== "IntegerArray"
    #is.array(x) && (!is.matrix(x)) && identical(typeof(x), "integer")
}

###################################################
# Java class org.kchine.r.RJIntegerMatrix to R IntegerMatrix 
###################################################
cvtIntegerMatrixFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("IntegerMatrix", ans)
}

matchIntegerMatrixFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJIntegerMatrix"
}
    
###################################################
# R IntegerMatrix to Java class org.kchine.r.RJIntegerMatrix
###################################################
cvtIntegerMatrixToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJIntegerMatrix", .convert=FALSE)
    value <- cvtIntegerToJava2(as.integer(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchIntegerMatrixToJava2 <- function(x, ...) {
    class(x)== "IntegerMatrix"
    #is.array(x) && (!is.matrix(x)) && identical(typeof(x), "integer")
}

###################################################
# Java class org.kchine.r.RJNumericArray to R NumericArray 
###################################################
cvtNumericArrayFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("NumericArray", ans)
}

matchNumericArrayFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJNumericArray"
}
    
###################################################
# R NumericArray to Java class org.kchine.r.RJNumericArray
###################################################
cvtNumericArrayToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJNumericArray", .convert=FALSE)
    value <- cvtNumericToJava2(as.double(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchNumericArrayToJava2 <- function(x, ...) {
    #is.array(x) && (!is.matrix(x)) && identical(typeof(x), "double")
    class(x)=="NumericArray"
}

###################################################
# Java class org.kchine.r.RJNumericMatrix to R NumericMatrix 
###################################################
cvtNumericMatrixFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("NumericMatrix", ans)
}

matchNumericMatrixFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJNumericMatrix"
}
    
###################################################
# R NumericMatrix to Java class org.kchine.r.RJNumericMatrix
###################################################
cvtNumericMatrixToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJNumericMatrix", .convert=FALSE)
    value <- cvtNumericToJava2(as.double(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchNumericMatrixToJava2 <- function(x, ...) {
    #is.array(x) && (!is.matrix(x)) && identical(typeof(x), "double")
    class(x)=="NumericMatrix"
}

###################################################
# Java class org.kchine.r.RJLogicalArray to R LogicalArray 
###################################################
cvtLogicalArrayFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("LogicalArray", ans)
}

matchLogicalArrayFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJLogicalArray"
}
    
###################################################
#  R LogicalArray to Java class org.kchine.r.RJLogicalArray
###################################################
cvtLogicalArrayToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJLogicalArray", .convert=FALSE)
    value <- cvtLogicalToJava2(as.logical(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchLogicalArrayToJava2 <- function(x, ...) {
    class(x)=="LogicalArray"
    ##is.array(x) && (!is.matrix(x)) && identical(typeof(x), "logical")
}

###################################################
# Java class org.kchine.r.RJLogicalMatrix to R LogicalMatrix 
###################################################
cvtLogicalMatrixFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("LogicalMatrix", ans)
}

matchLogicalMatrixFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJLogicalMatrix"
}
    
###################################################
#  R LogicalMatrix to Java class org.kchine.r.RJLogicalMatrix
###################################################
cvtLogicalMatrixToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJLogicalMatrix", .convert=FALSE)
    value <- cvtLogicalToJava2(as.logical(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchLogicalMatrixToJava2 <- function(x, ...) {
    class(x)=="LogicalMatrix"
    ##is.array(x) && (!is.matrix(x)) && identical(typeof(x), "logical")
}

###################################################
#    Java class org.kchine.r.RJRawArray to R RawArray 
###################################################
cvtRawArrayFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("RawArray", ans)
}

matchRawArrayFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJRawArray"
}
    
###################################################
#    R RawArray to Java class org.kchine.r.RJRawArray
###################################################
cvtRawArrayToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJRawArray", .convert=FALSE)
    value <- cvtRawToJava2(as.raw(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchRawArrayToJava2 <- function(x, ...) {
    class(x)=="RawArray"
    ## is.array(x) && (!is.matrix(x)) && identical(typeof(x), "raw")
}

###################################################
#    Java class org.kchine.r.RJRawMatrix to R RawMatrix 
###################################################
cvtRawMatrixFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("RawMatrix", ans)
}

matchRawMatrixFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJRawMatrix"
}
    
###################################################
#    R RawMatrix to Java class org.kchine.r.RJRawMatrix
###################################################
cvtRawMatrixToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJRawMatrix", .convert=FALSE)
    value <- cvtRawToJava2(as.raw(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchRawMatrixToJava2 <- function(x, ...) {
    class(x)=="RawMatrix"
    ## is.array(x) && (!is.matrix(x)) && identical(typeof(x), "raw")
}

###################################################
# Java class org.kchine.r.RJComplexArray to R ComplexArray 
###################################################
cvtComplexArrayFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("ComplexArray", ans)
}

matchComplexArrayFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJComplexArray"
}
    
###################################################
# R ComplexArray to Java class org.kchine.r.RJComplexArray
###################################################
cvtComplexArrayToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJComplexArray", .convert=FALSE)
    value <- cvtComplexToJava2(as.complex(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchComplexArrayToJava2 <- function(x, ...) {
    class(x)=="ComplexArray"
    ##is.array(x) && (!is.matrix(x)) && identical(typeof(x), "complex")
}

###################################################
# Java class org.kchine.r.RJComplexMatrix to R ComplexMatrix 
###################################################
cvtComplexMatrixFromJava2 <- function(x, thisClassName) {
    ans <- cvtArrayFromJavaUtil2(x)
    new("ComplexMatrix", ans)
}

matchComplexMatrixFromJava2 <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RJComplexMatrix"
}
    
###################################################
# R ComplexMatrix to Java class org.kchine.r.RJComplexMatrix
###################################################
cvtComplexMatrixToJava2 <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RJComplexMatrix", .convert=FALSE)
    value <- cvtComplexToJava2(as.complex(x))
    .Java(ans, "setValue", value, .convert=FALSE)
    ans <- cvtArrayToJavaUtil2(x, ans)
    ans
}

matchComplexMatrixToJava2 <- function(x, ...) {
    class(x)=="ComplexMatrix"
    ##is.array(x) && (!is.matrix(x)) && identical(typeof(x), "complex")
}

###################################################
#    R environment to java.util.HashMap
###################################################
cvtEnvToJava2 <- function(x, ...) {
    e <- .JNew("java.util.HashMap", .convert=FALSE)
    for (key in ls(x)) {
        obj <- get(key, envir=x)
        jkey <- .Call("RCharScalar_JavaString", key)
        .Java(e, "put", jkey, obj, .convert=FALSE)
    }
    e
}

matchEnvToJava2 <- function(x, ...) {
    is.environment(x)
}

###################################################
#    Java java.util.HashMap to R environment
###################################################
cvtEnvFromJava2 <- function(x, thisClassName) {
    if (is.null(x))
        return(NULL)
    ans <- new.env()
    entries <- .Java(x, "entrySet", .convert=FALSE)
    entryIterator <- .Java(entries, "iterator", .convert=FALSE)

    # Many calls but little memory per-call.
    while( entryIterator$hasNext() ) {
        thisEntry <- .Java(entryIterator, "next", .convert=FALSE)
        thisKey <- .Java(thisEntry, "getKey")
        thisValue <- .Java(thisEntry, "getValue")
        assign(thisKey, thisValue, envir=ans)
    }
    ans
}

matchEnvFromJava2 <- function(x, thisClassName) {
    thisClassName == "java.util.HashMap"
}

###################################################
#    Register all match / convert functions to SJava
###################################################
regAddonCvt2 <- function(){
    setJavaFunctionConverter(cvtCharacterToJava2, matchCharacterToJava2, 
                             description="R character  to String[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtIntegerToJava2, matchIntegerToJava2, 
                             description="R integer  to int[]",                         
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtNumericToJava2, matchNumericToJava2, 
                             description="R numeric  to double[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtLogicalToJava2, matchLogicalToJava2, 
                             description="R logical  to boolean[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtRawToJava2, matchRawToJava2, 
                             description="R raw  to byte[]",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtComplexFromJava2, matchComplexFromJava2, 
                             description="org.kchine.r.RJComplex to R complex",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtComplexToJava2, matchComplexToJava2, 
                             description="R complex  to org.kchine.r.RComplex",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtListToJava2, matchListToJava2, 
                             description="R list to Object[]",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtFactorFromJava2, matchFactorFromJava2, 
                             description="Java class org.kchine.r.RJFactor to R factor",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtFactorToJava2, matchFactorToJava2, 
                             description="R factor to org.kchine.r.RJFactor",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtDataFrameFromJava2, matchDataFrameFromJava2, 
                             description="Java class org.kchine.r.RJDataFrame to R data.frame",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtDataFrameToJava2, matchDataFrameToJava2, 
                             description="R data.frame to org.kchine.r.RJDataFrame",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtCharArrayFromJava2, matchCharArrayFromJava2, 
                             description="Java class org.kchine.r.RJCharArray to R CharArray",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtCharArrayToJava2, matchCharArrayToJava2, 
                             description="R CharArray to org.kchine.r.RJCharArray",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtIntegerArrayFromJava2, matchIntegerArrayFromJava2, 
                             description="Java class org.kchine.r.RJIntegerArray to R IntegerArray",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtIntegerArrayToJava2, matchIntegerArrayToJava2, 
                             description="R IntegerArray to org.kchine.r.RJIntegerArray",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtNumericArrayFromJava2, matchNumericArrayFromJava2, 
                             description="Java class org.kchine.r.RJNumericArray to R NumericArray",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtNumericArrayToJava2, matchNumericArrayToJava2, 
                             description="R NumericArray to org.kchine.r.RJNumericArray",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtRawArrayFromJava2, matchRawArrayFromJava2, 
                             description="Java class org.kchine.r.RJRawArray to R RawArray",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtRawArrayToJava2, matchRawArrayToJava2, 
                             description="R RawArray to org.kchine.r.RJRawArray",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtLogicalArrayFromJava2, matchLogicalArrayFromJava2, 
                             description="Java class org.kchine.r.RJLogicalArray to R LogicalArray",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtLogicalArrayToJava2, matchLogicalArrayToJava2, 
                             description="R LogicalArray to org.kchine.r.RJLogicalArray",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtComplexArrayFromJava2, matchComplexArrayFromJava2, 
                             description="Java class org.kchine.r.RJComplexArray to R ComplexArray",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtComplexArrayToJava2, matchComplexArrayToJava2, 
                             description="R ComplexArray to org.kchine.r.RJComplexArray",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtCharMatrixFromJava2, matchCharMatrixFromJava2, 
                             description="Java class org.kchine.r.RJCharMatrix to R CharMatrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtCharMatrixToJava2, matchCharMatrixToJava2, 
                             description="R CharMatrix to org.kchine.r.RJCharMatrix",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtIntegerMatrixFromJava2, matchIntegerMatrixFromJava2, 
                             description="Java class org.kchine.r.RJIntegerMatrix to R IntegerMatrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtIntegerMatrixToJava2, matchIntegerMatrixToJava2, 
                             description="R IntegerMatrix to org.kchine.r.RJIntegerMatrix",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(RWebServices:::cvtNumericMatrixFromJava2, matchNumericMatrixFromJava2, 
                             description="Java class org.kchine.r.RJNumericMatrix to R NumericMatrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtNumericMatrixToJava2, matchNumericMatrixToJava2, 
                             description="R NumericMatrix to org.kchine.r.RJNumericMatrix",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtRawMatrixFromJava2, matchRawMatrixFromJava2, 
                             description="Java class org.kchine.r.RJRawMatrix to R RawMatrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtRawMatrixToJava2, matchRawMatrixToJava2, 
                             description="R RawMatrix to org.kchine.r.RJRawMatrix",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtLogicalMatrixFromJava2, matchLogicalMatrixFromJava2, 
                             description="Java class org.kchine.r.RJLogicalMatrix to R LogicalMatrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtLogicalMatrixToJava2, matchLogicalMatrixToJava2, 
                             description="R LogicalMatrix to org.kchine.r.RJLogicalMatrix",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtComplexMatrixFromJava2, matchComplexMatrixFromJava2, 
                             description="Java class org.kchine.r.RJComplexMatrix to R ComplexMatrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtComplexMatrixToJava2, matchComplexMatrixToJava2, 
                             description="R ComplexMatrix to org.kchine.r.RJComplexMatrix",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtEnvToJava2, matchEnvToJava2,
                             description="R environment to java.util.HashMap",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtEnvFromJava2, matchEnvFromJava2,
                             description="java.util.HashMap to R environment",
                             fromJava=TRUE, position=-1)
    ""
}
