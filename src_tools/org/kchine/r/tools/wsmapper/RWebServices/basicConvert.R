########################################
# Authors : Nianhua Li, MT Morgan
# License : caBIG 
########################################

###################################################
#    Java java.util.ArrayList -- R list (internal function, not register to table)
###################################################
cvtFromJavaArrayList <- function(v) {
    n <- v$size()
    ite <- .Java(v, "iterator", .convert=FALSE)
    ans <- lapply(seq(length=n), function(x) .Java(ite, "next") )
    ans
}

cvtToJavaArrayList <- function(x) {
    v <- .JNew("java.util.ArrayList", .convert=FALSE)
    lapply(x, function(x) {
            .Java(v, "add", x, .convert=FALSE)
        }) 
    v
}
###################################################
#    Java org.kchine.r.RChar to R character   
###################################################
cvtCharacterFromJava <- function(x, thisClassName) {
   #cat("x="); print(x)
   #cat("cvtCharacterFromJava: ans <- .Java(x, 'getValue')\n")
   ans <- .Java(x, "getValue") 
   indexNA <- .Java(x, "getIndexNA")
   if (is.null(ans)) 
     ans <- character(0)
   if( !is.null(indexNA)) 
        ans[indexNA+1] <- NA
   names(ans) <- .Java(x, "getNames")
   ans
}
  
matchCharacterFromJava <- function(x, thisClassName) {
    #cat("inside match function, character from  java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RChar"
}

###################################################
#    Java org.kchine.r.RInteger to R integer   
###################################################
cvtIntegerFromJava <- function(x, thisClassName) {
   #cat("cvtIntegerFromJava: ans <- .Java(x, 'getValue')\n")
   ans <- .Java(x, "getValue") 
   #cat("cvtVectorFromJava: ans <- .Java(x, 'getIndexNA')\n")
   indexNA <- .Java(x, "getIndexNA")
   if (is.null(ans)) 
     ans <- integer(0)
   if( !is.null(indexNA)) 
        ans[indexNA+1] <- NA
   names(ans) <- .Java(x, "getNames")
   ans
}
  
matchIntegerFromJava <- function(x, thisClassName) {
    #cat("inside match function, integer from  java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RInteger"
}
###################################################
#    Java org.kchine.r.RLogical to R logical 
###################################################
cvtLogicalFromJava <- function(x, thisClassName) {
   #cat("x="); print(x)
   #cat("cvtLogicalFromJava: ans <- .Java(x, 'getValue')\n")
   ans <- .Java(x, "getValue") 
   indexNA <- .Java(x, "getIndexNA")
   if (is.null(ans)) 
     ans <- logical(0)
   if( !is.null(indexNA)) 
        ans[indexNA+1] <- NA
   names(ans) <- .Java(x, "getNames")
   ans
}
  
matchLogicalFromJava <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RLogical"
}

###################################################
#    Java org.kchine.r.RNumeric to R numeric   
###################################################
cvtNumericFromJava <- function(x, thisClassName) {
    #cat("cvtNumericFromJava: ans <- .Java(x, 'getValue')\n")
   ans <- .Java(x, "getValue") 
   indexNA <- .Java(x, "getIndexNA")
   if (is.null(ans)) 
     ans <- numeric(0)
   if( !is.null(indexNA) )
	ans[indexNA+1] <- NA
   names(ans) <- .Java(x, "getNames")
   #cat("-------cvtNumericFromJava---------\n"); print(str(ans))
   ans
}
  
matchNumericFromJava <- function(x, thisClassName) {
    #cat("inside match function, vector from  java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RNumeric"
}
###################################################
#    Java org.kchine.r.RRaw to R raw   
###################################################
cvtRawFromJava <- function(x, thisClassName) {
    #cat("cvtRawFromJava: ans <- .Java(x, 'getValue')\n")
   ans <- .Java(x, "getValue") 
   if (is.null(ans)) 
     ans <- raw(0)
   names(ans) <- .Java(x, "getNames")
   ans
}
  
matchRawFromJava <- function(x, thisClassName) {
    #cat("inside match function, vector from  java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RRaw"
}

###################################################
#    Java class org.kchine.r.RComplex to R complex   
###################################################
cvtComplexFromJava <- function(x, thisClassName) {
   real <- .Java(x, "getReal") 
   imaginary <- .Java(x, "getImaginary")
   indexNA <- .Java(x, "getIndexNA")
   ans <- complex(real=real, imaginary=imaginary)
   if (!is.null(indexNA))
	ans[indexNA+1] <- NA
   names(ans) <- .Java(x, "getNames")
   #cat("-------cvtComplexFromJava---------\n"); print(str(ans))
   ans
}
  
matchComplexFromJava <- function(x, thisClassName) {
    #cat("inside match function, complex from  java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RComplex"
}

###################################################
#    Java subclass of org.kchine.r.RVector to R vector   
###################################################
cvtVectorFromJava <- function(x, thisClassName) {
    #cat("cvtVectorFromJava: ")
    if( is.null(thisClassName) ) {
        currentClassObject <- .Java(x, "getClass", .convert=FALSE)
        thisClassName <- .Java(currentClassObject, "getName")
        #cat("thisClassName="); print(thisClassName)
    }
    ans <- switch(thisClassName,
            org.kchine.r.RChar=cvtCharacterFromJava(x, thisClassName),
            org.kchine.r.RInteger=cvtIntegerFromJava(x, thisClassName),
            org.kchine.r.RLogical=cvtLogicalFromJava(x, thisClassName),
            org.kchine.r.RNumeric=cvtNumericFromJava(x, thisClassName),
            org.kchine.r.RRaw=cvtRawFromJava(x, thisClassName),
            org.kchine.r.RComplex=cvtComplexFromJava(x, thisClassName)
          )
    ans
}

###################################################
#    R character vector to Java org.kchine.r.RChar 
###################################################
cvtCharacterToJava <- function(x, ...) {
    value <- .Call("RCharVector_JavaStringArray", x)
    indexNA <- which(is.na(x))-1
    if (length(indexNA)==0)
        indexNA <- NULL
    else
        indexNA <- .Call("RIntegerVector_JavaIntArray", as.integer(indexNA))
    name <- names(x)
    if (!is.null(name))
        name <- .Call("RCharVector_JavaStringArray", name)
    ans <- .JNew("org.kchine.r.RChar", .convert=FALSE)
    .Java(ans, "setNames", name, .convert=FALSE)
    .Java(ans, "setValue", value, .convert=FALSE)
    .Java(ans, "setIndexNA", indexNA, .convert=FALSE)
    ans
}

matchCharacterToJava <- function(x, ...) {
    #cat("inside match function, char to java", is.vector(x) && is.character(x),"\n")
    is.vector(x) && is.character(x)
}

###################################################
#    R integer vector to Java org.kchine.r.RInteger 
###################################################
cvtIntegerToJava <- function(x, ...) {
    value <- .Call("RIntegerVector_JavaIntArray", x)
    indexNA <- which(is.na(x))-1
    if (length(indexNA)==0)
        indexNA <- NULL
    else
        indexNA <- .Call("RIntegerVector_JavaIntArray", as.integer(indexNA))
    name <- names(x)
    if (!is.null(name))
        name <- .Call("RCharVector_JavaStringArray", name)
    ans <- .JNew("org.kchine.r.RInteger", .convert=FALSE)
    .Java(ans, "setNames", name, .convert=FALSE)
    .Java(ans, "setValue", value, .convert=FALSE)
    .Java(ans, "setIndexNA", indexNA, .convert=FALSE)
    ans
}

matchIntegerToJava <- function(x, ...) {
    #cat("inside match function, integer to java", is.vector(x) && is.integer(x),"\n")
    is.vector(x) && is.integer(x)
}

###################################################
#    R numeric vector to Java org.kchine.r.RNumeric 
###################################################
cvtNumericToJava <- function(x, ...) {
    value <- .Call("RNumericVector_JavaDoubleArray", x)
    indexNA <- which(is.na(x)&(!is.nan(x)))-1
    if (length(indexNA)==0)
	indexNA <- NULL
    else
	indexNA <- .Call("RIntegerVector_JavaIntArray", as.integer(indexNA))
    name <- names(x)
    if (!is.null(name))
        name <- .Call("RCharVector_JavaStringArray", name)
    ans <- .JNew("org.kchine.r.RNumeric", .convert=FALSE)
    .Java(ans, "setNames", name, .convert=FALSE)
    .Java(ans, "setValue", value, .convert=FALSE)
    .Java(ans, "setIndexNA", indexNA, .convert=FALSE)
    ans
}

matchNumericToJava <- function(x, ...) {
    #cat("inside match function, numeric to java", is.vector(x) && is.double(x),"\n")
    is.vector(x) && is.double(x)
}
    
###################################################
#    R logical vector to Java org.kchine.r.RLogical
###################################################
cvtLogicalToJava <- function(x, ...) {
    value <- .Call("RLogicalVector_JavaBooleanArray", x)
    indexNA <- which(is.na(x))-1
    if (length(indexNA)==0)
        indexNA <- NULL
    else
        indexNA <- .Call("RIntegerVector_JavaIntArray", as.integer(indexNA))
    name <- names(x)
    if (!is.null(name))
        name <- .Call("RCharVector_JavaStringArray", name)
    ans <- .JNew("org.kchine.r.RLogical", .convert=FALSE)
    .Java(ans, "setNames", name, .convert=FALSE)
    .Java(ans, "setValue", value, .convert=FALSE)
    .Java(ans, "setIndexNA", indexNA, .convert=FALSE)
    ans
}

matchLogicalToJava <- function(x, ...) {
    #cat("inside match function, logical to java", is.vector(x) && is.logical(x),"\n")
    is.vector(x) && is.logical(x)
}

###################################################
#    R raw vector to Java org.kchine.r.RRaw
###################################################
cvtRawToJava <- function(x, ...) {
    value <- .Call("RRawVector_JavaByteArray", x)
    name <- names(x)
    if (!is.null(name))
        name <- .Call("RCharVector_JavaStringArray", name)
    ans <- .JNew("org.kchine.r.RRaw", .convert=FALSE)
    .Java(ans, "setNames", name, .convert=FALSE)
    .Java(ans, "setValue", value, .convert=FALSE)
    ans
}

matchRawToJava <- function(x, ...) {
    #cat("inside match function, logical to java", is.vector(x) && is.logical(x),"\n")
    is.vector(x) && (typeof(x)=="raw")
}
    
###################################################
#    R complex vector to Java org.kchine.r.RComplex                                       
###################################################
cvtComplexToJava <- function(x, ...) {
    #cat("real=", Re(x), ", and to java:\n")
    real <- .Call("RNumericVector_JavaDoubleArray", Re(x))
    #cat("imaginary=", Im(x), ", and to java:\n")
    imaginary <- .Call("RNumericVector_JavaDoubleArray", Im(x))
    indexNA <- which(is.na(x)&(!is.nan(x)))-1
    if (length(indexNA)==0)
        indexNA <- NULL
    else
        indexNA <- .Call("RIntegerVector_JavaIntArray", as.integer(indexNA))
    name <- names(x)
    if (!is.null(name))
        name <- .Call("RCharVector_JavaStringArray", name)
    ans <- .JNew("org.kchine.r.RComplex", real, imaginary, indexNA, name, .convert=FALSE)
    ans
}

matchComplexToJava <- function(x, ...) {
    #cat("inside match function, complex to java", is.vector(x) && is.complex(x),"\n")
    is.vector(x) && is.complex(x)
}

###################################################
#    R vector to Java subclass of org.kchine.r.RVector
###################################################
cvtVectorToJava <- function(x, ...) {
    if(matchCharacterToJava(x))
        ans <- cvtCharacterToJava(x)
    else if (matchIntegerToJava(x))
        ans <- cvtIntegerToJava(x)
    else if (matchLogicalToJava(x))
        ans <- cvtLogicalToJava(x)
    else if (matchNumericToJava(x))
        ans <- cvtNumericToJava(x)
    else if (matchRawToJava(x))
        ans <- cvtRawToJava(x)
    else if (matchComplexToJava(x))
        ans <- cvtComplexToJava(x)
    else {
        cat("cvtVectorToJava: x="); print(x)
        stop("cvtVectorToJava: input R object is not a valid vector.\n")
    }
    ans
}

matchVectorToJava <- function(x, ...) {
    matchCharacterToJava(x) || matchIntegerToJava(x) || matchLogicalToJava(x) || matchNumericToJava(x) || matchRawToJava(x) || matchComplexToJava(x)
}
    
###################################################
#    R list to Java org.kchine.r.RList
###################################################
cvtListToJava <- function(x, ...) {
   #cat("cvt list to java\n")
    v <- .Call("RList_JavaObjectArray", x)
    l <- .JNew("org.kchine.r.RList", .convert=FALSE)
    .Java(l, "setValue", v, .convert=FALSE)
    name <- names(x)
    if (!is.null(name)) {
        if (all(is.na(name))||(length(name)==0))
            name <- NULL
        else
            name <- .Call("RCharVector_JavaStringArray", name)
    }
    .Java(l, "setNames", name, .convert=FALSE)     
    l
}

matchListToJava <- function(x, ...) {
    #cat("inside match function, list to java...")
    #cat(inherits(x,"list")&&(!is.array(x))&&(!is.data.frame(x)), "\n")
    inherits(x,"list") && (!is.array(x)) && (!is.data.frame(x))
}

###################################################
#    Java org.kchine.r.RList to R list   
###################################################
cvtListFromJava <- function(x, thisClassName) {
   ans <- .Java(x, "getValue") # either a list or NULL
   if (is.null(ans))      # map java default value to R default value
        ans <- list()
    names(ans) <- x$getNames()     # java String[] or String, mapped to R character()
                                   # or java null, mapped to R NULL
    ans
}
  
matchListFromJava <- function(x, thisClassName) {
    #cat("inside match function, list from  java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RList"
}

###################################################
#    R array to Java class org.kchine.r.RArray
###################################################
cvtArrayToJavaUtil <- function(x, jclsname="org.kchine.r.RArray") {
    #cat(jclsname, ": v <- cvtArrayToJavaUtil(x)\n")
    v <- x
    if (is.list(v)) {
        warning(jclsname, ": Array contains a list rather than a normal vector. Unexpected result will occur.\n")
        v <- unlist(v)
    }
    v <- switch(typeof(v), 
                    character=cvtCharacterToJava(x),
                    integer=cvtIntegerToJava(x),
                    double=cvtNumericToJava(x),
                    logical=cvtLogicalToJava(x),
                    NULL=v,
                    v)
    dim <- dim(x)   # R integer vector , mapped to java int[] 
                    # dim(x) won't be NULL, otherwise x is not an array
    if (!is.null(dim))
        dim <- .Call("RIntegerVector_JavaIntArray", dim)
    if (is.null(dimnames(x))) {
        dimnames <- NULL
    } else {
        dimnames <- cvtListToJava(dimnames(x)) 
    } 
    
    #cat(jclsname, ": a <- .JNew('org.kchine.r.RArray', .convert=FALSE)\n") 
    a <- .JNew(jclsname, .convert=FALSE)
    #cat(jclsname, ": .Java(a, 'setValue', v, .convert=FALSE)\n")
    .Java(a, "setValue", v, .convert=FALSE)
    #cat(jclsname, ": .Java(a, 'setDim', dim, .convert=FALSE)\n")
    .Java(a, "setDim", dim, .convert=FALSE)
    #cat(jclsname, ": .Java(a, 'setDimnamesFromR', dimnames, .convert=FALSE)\n")
    .Java(a, "setDimnames", dimnames, .convert=FALSE) 
    a
}

cvtArrayToJava <- function(x, ...) {
    cvtArrayToJavaUtil(x)
}

matchArrayToJava <- function(x, ...) {
    #cat("inside match function, array to java", is.array(x)&&(!is.matrix(x)),"\n")
    is.array(x) && (!is.matrix(x))
}

###################################################
#    Java class org.kchine.r.RArray to R array  
###################################################
cvtArrayFromJava <- function(x, thisClassName) {
    #cat("cvtArrayFromJava: v <- .Java(x, 'getValue', .convert=FALSE)\n")
    v <- .Java(x, "getValue", .convert=FALSE)    # either a foreign object reference or NULL
    if (is.null(v))      # map java default value to R default value
        ans <- 0
    else 
        ans <- cvtVectorFromJava(v, NULL)
    #cat("cvtArrayFromJava: dim <- x$getDim()\n")
    dim <-x$getDim()
    if (is.null(dim)) dim <- length(ans)   # map java default value to R default
                                           # value
    #cat("cvtArrayFromJava: dimnames <- .Java(x, 'getDimnames', .convert=FALSE)\n")
    dimnames <-.Java(x, "getDimnames", .convert=FALSE)
    if (!is.null(dimnames)) {
        #cat("cvtArrayFromJava:  ans <- cvtFromJavaArrayList(v)\n")  
        dimnames <- cvtListFromJava(dimnames)
    }
    #cat("cvtArrayFromJava: ans <- array(ans, dim, dimnames)\n")
    ans2 <- array(ans, dim, dimnames)
    names(ans2) <- names(ans)
    #cat("cvtArrayFromJava: ans\n")
    ans2
}

matchArrayFromJava <- function(x, thisClassName) {
    #cat("inside match function, array from java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RArray"
}
        
###################################################
#    R matrix to Java class org.kchine.r.RMatrix
###################################################
cvtMatrixToJava <- function(x, ...) {
    cvtArrayToJavaUtil(x, jclsname="org.kchine.r.RMatrix")
}

matchMatrixToJava <- function(x, ...) {
    #cat("inside match function, matrix to java", is.matrix(x), "\n")
    is.matrix(x) 
}
    
###################################################
#    Java class org.kchine.r.RMatrix to R matrix  
###################################################
cvtMatrixFromJava <- function(x, thisClassName) {
    #cat("cvtMatrixFromJava: ans <- cvtArrayFromJava(x, thisClassName)\n")
    ans <- cvtArrayFromJava(x, thisClassName)
    #cat("cvtMatrixFromJava: as.matrix(ans)\n")
    as.matrix(ans)
}

matchMatrixFromJava <- function(x, thisClassName) {
    #cat("inside match function, matrix from java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RMatrix"
}

###################################################
#    Java class org.kchine.r.RFactor to R factor  
###################################################
cvtFactorFromJava <- function(x, thisClassName) {
    #cat("cvtFactorFromJava: theCode <- x$getCode()\n")
    theCode <- x$getCode()
    theCode[theCode==-1] <- NA
    theCode <- theCode +1
    #cat("cvtFactorFromJava: theLevels <- x$getLevels()\n")
    theLevels <- x$getLevels()
    #cat("cvtFactorFromJava: ans <-factor(theCode, labels=theLevels)\n")
    if(is.null(theLevels))
        ans <- factor(theCode)
    else
        ans <- factor(theCode, labels=theLevels)
    #cat("cvtFactorFromJava: ans\n")
    ans
}

matchFactorFromJava <- function(x, thisClassName) {
    #cat("inside match function, factor from java", thisClassName, "\n")
    thisClassName == "org.kchine.r.RFactor"
}
    
###################################################
#    R factor to Java class org.kchine.r.RFactor
###################################################
cvtFactorToJava <- function(x, ...) {
    #cat("cvtFactorToJava: .Call\n")
    code <- as.integer(x)-1
    code[is.na(code)] <- -1
    code <- .Call("RIntegerVector_JavaIntArray", as.integer(code))
    level <- .Call("RCharVector_JavaStringArray", levels(x))
    #cat("cvtFactorToJava: v <- .JNew('org.kchine.r.RFactor', .convert=FALSE)\n")
    v <- .JNew("org.kchine.r.RFactor", .convert=FALSE)
    #cat("cvtFactorToJava: .Java(v, 'setLevels', levels(x), .convert=FALSE)\n")
    .Java(v, "setLevels", level, .convert=FALSE)
    #cat("cvtFactorToJava: .Java(v, 'setCode', as.integer(x), .convert=FALSE)\n")
    .Java(v, "setCode", code, .convert=FALSE)
    #cat("cvtFactorToJava: v\n")
    v
}

matchFactorToJava <- function(x, ...) {
    #cat("inside match function, factor to java...", is.factor(x), "\n")
    is.factor(x)
}
    
###################################################
#    Java class org.kchine.r.RDataFrame to R data.frame  
###################################################
cvtDataFrameFromJava <- function(x, thisClassName) {
    theData <- .Java(x, "getData", .convert=cvtListFromJava)
    theRowNames <- x$getRowNames()
    ans <- data.frame(theData, row.names=theRowNames, stringsAsFactors=FALSE)
    ans
}

matchDataFrameFromJava <- function(x, thisClassName) {
    #cat("inside match function, df from java...", thisClassName, "\n")
    thisClassName == "org.kchine.r.RDataFrame"
}
    
###################################################
#    R data.frame to Java class org.kchine.r.RDataFrame
###################################################
cvtDataFrameToJava <- function(x, ...) {
    v <- .JNew("org.kchine.r.RDataFrame", .convert=FALSE)
    dfdata <- cvtListToJava(as.list(x))
    .Java(v, "setData", dfdata, .convert=FALSE)
    rowname <- .Call("RCharVector_JavaStringArray", row.names(x))
    .Java(v, "setRowNames", rowname, .convert=FALSE)
    v
}

matchDataFrameToJava <- function(x, ...) {
    #cat("inside match function, df to java...", is.data.frame(x), "\n")
    is.data.frame(x)
}

###################################################
#    R environment to Java org.kchine.r.REnvironment
###################################################
cvtEnvToJava <- function(x, ...) {
    e <- .JNew("java.util.HashMap", .convert=FALSE)
    for (key in ls(x)) {
        obj <- get(key, envir=x)
        jkey <- .Call("RCharScalar_JavaString", key)
        .Java(e, "put", jkey, obj, .convert=FALSE)
    }
    ans <- .JNew("org.kchine.r.REnvironment", .convert=FALSE)
    .Java(ans, "setData", e, .convert=FALSE)
    ans
}

matchEnvToJava <- function(x, ...) {
    #cat("inside match function, env to java...", class(x), "\n")
    is.environment(x)
}

###################################################
#    Java org.kchine.r.REnvionment to R environment
###################################################
cvtEnvFromJava <- function(x, thisClassName) {
    x <- .Java(x, "getData", .convert=FALSE)   #ref of HashMap 
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

matchEnvFromJava <- function(x, thisClassName) {
    #cat("inside match environment from Java... thisClassName=", thisClassName, "\n")
    thisClassName == "org.kchine.r.REnvironment"
}

###################################################
#    R objs to Java org.kchine.r.RUnknown
###################################################
cvtUnknownToJava <- function(x, ...) {
    ans <- .JNew("org.kchine.r.RUnknown", .convert=FALSE)
    l <- .Call("RIntegerVector_JavaIntArray", length(x))
    rclass <-  .Call("RCharVector_JavaStringArray", class(x))
    contents <- .Call("RCharVector_JavaStringArray", print(x))
    .Java(ans, "setRclass", rclass, .convert=FALSE)
    .Java(ans, "setLength", l, .convert=FALSE)
    .Java(ans, "setContents", contents, .convert=FALSE)
    ans
}

matchUnknownToJava <- function(x, ...) {
    !is.null(x)
}

###################################################
#    Java org.kchine.r.RUnknown to other R objects
###################################################
cvtUnknownFromJava <- function(x, thisClassName) {
    l <- .Java(x, "getLength")
    rclass <- .Java(x, "getRclass")
    if (is.null(getClassDef(rclass))) {
        ans <- vector("list", l)
        class(ans) <- rclass
    } else {
        ans <- new(rclass)
    }
    ans
}

matchUnknownFromJava <- function(x, thisClassName) {
    thisClassName == "org.kchine.r.RUnknown"
}


###################################################
#    Register all match / convert functions to SJava
###################################################
regAddonCvt <- function() {
    setJavaFunctionConverter(cvtCharacterFromJava, matchCharacterFromJava, 
                             description="org.kchine.r.RChar to R character",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtIntegerFromJava, matchIntegerFromJava, 
                             description="org.kchine.r.RInteger to R integer",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtNumericFromJava, matchNumericFromJava, 
                             description="org.kchine.r.RNumeric to R numeric",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtLogicalFromJava, matchLogicalFromJava, 
                             description="org.kchine.r.RLogical to R logical",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtRawFromJava, matchRawFromJava, 
                             description="org.kchine.r.RRaw to R raw",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtComplexFromJava, matchComplexFromJava, 
                             description="org.kchine.r.RComplex to R complex",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtCharacterToJava, matchCharacterToJava, 
                             description="R character  to org.kchine.r.RChar",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtIntegerToJava, matchIntegerToJava, 
                             description="R integer  to org.kchine.r.RInteger",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtNumericToJava, matchNumericToJava, 
                             description="R numeric  to org.kchine.r.RNumeric",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtLogicalToJava, matchLogicalToJava, 
                             description="R logical  to org.kchine.r.RLogical",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtRawToJava, matchRawToJava, 
                             description="R raw  to org.kchine.r.RRaw",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtComplexToJava, matchComplexToJava, 
                             description="R complex  to org.kchine.r.RComplex",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtListToJava, matchListToJava, 
                             description="R list to org.kchine.r.RList",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtListFromJava, matchListFromJava, 
                             description="org.kchine.r.RList to R list",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtArrayToJava, matchArrayToJava, 
                             description="R array  to org.kchine.r.RArray",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtArrayFromJava, matchArrayFromJava, 
                             description="Java class org.kchine.r.RArray to R array",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtMatrixToJava, matchMatrixToJava, 
                             description="R matrix  to org.kchine.r.RMatrix",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtMatrixFromJava, matchMatrixFromJava, 
                             description="Java class org.kchine.r.RMatrix to R matrix",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtFactorFromJava, matchFactorFromJava, 
                             description="Java class org.kchine.r.RFactor to R factor",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtFactorToJava, matchFactorToJava, 
                             description="R factor to org.kchine.r.RFactor",
                             fromJava=FALSE, position=1)
    setJavaFunctionConverter(cvtDataFrameFromJava, matchDataFrameFromJava, 
                             description="Java class org.kchine.r.RDataFrame to R data.frame",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtDataFrameToJava, matchDataFrameToJava, 
                             description="R data.frame to org.kchine.r.RDataFrame",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtEnvToJava, matchEnvToJava,
                             description="R environment to org.kchine.r.REnvironment",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtEnvFromJava, matchEnvFromJava,
                             description="org.kchine.r.REnvironment to R environment",
                             fromJava=TRUE, position=-1)
    setJavaFunctionConverter(cvtUnknownToJava, matchUnknownToJava,
                             description="R objects to org.kchine.r.RUnknown",
                             fromJava=FALSE, position=-1)
    setJavaFunctionConverter(cvtUnknownFromJava, matchUnknownFromJava,
                             description="org.kchine.r.RUnknown to other R objects",
                             fromJava=TRUE, position=-1)
    ""
}
