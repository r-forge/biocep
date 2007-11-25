
############################
# section 4: creating/updating the java <-> R lookup table
############################
       
## initialize a lookup table to keep java <-> R mapping of basic R data types 
##      return: a R environment serving as a lookup table
##              key is R data type in character string
##              value is instances of RJMap 
setGeneric("newLookup", function(typeMode) standardGeneric("newLookup"))
            
setMethod("newLookup", signature(typeMode="robject"), function(typeMode) {     
    lookup <- new.env(parent=emptyenv())
    ## primary type mappings
    rType <- c("numeric", "integer", "double", 
                "character", "logical", "matrix", 
                "array", "list", "factor", 
                "data.frame", "environment", "raw",
                "complex", "vector", "NULL")
    jType <- c("RNumeric", "RInteger", "RNumeric", 
                "RChar", "RLogical", "RMatrix", 
                "RArray", "RList", "RFactor", 
                "RDataFrame", "REnvironment", "RRaw",
                "RComplex", "RVector", "RObject" )
    r2j <- c("cvtNumericToJava", "cvtIntegerToJava", "cvtNumericToJava",
            "cvtCharacterToJava", "cvtLogicalToJava", "cvtMatrixToJava",
            "cvtArrayToJava", "cvtListToJava", "cvtFactorToJava",
            "cvtDataFrameToJava", "cvtEnvToJava", "cvtRawToJava",
            "cvtComplexToJava", "cvtVectorToJava", "" )
    j2r <- c("cvtNumericFromJava", "cvtIntegerFromJava", "cvtNumericFromJava",
            "cvtCharacterFromJava", "cvtLogicalFromJava", "cvtMatrixFromJava",
            "cvtArrayFromJava", "cvtListFromJava", "cvtFactorFromJava",
            "cvtDataFrameFromJava", "cvtEnvFromJava", "cvtRawFromJava",
            "cvtComplexFromJava", "cvtVectorFromJava", "")
    ## We implcitly imply that if the convert function name is cvtXXXX, then
    ## the match function name is matchXXXX. We don't keep match function name
    ## in the lookup table because it is only used to generate convert function
    ## of ClassUnion (r->java). But this may cause subtle problem if someone
    ## tries to add conversions for some type but not following this naming
    ## conversions.
    for ( i in seq (along = rType )) {
            rjMap <- new("RJMap", 
                        jType = jType[[i]],  
                        jPackage=primaryDataPkg(),
                        rPackage="base",
                        typeGroup = "primary", 
                        r2j = r2j[[i]], 
                        j2r = j2r[[i]])
            assign( rType[[i]], rjMap, lookup )
    }
    toRUnknown <- c( "function", "MethodDefinition", "genericFunction", 
        "functionWithTrace", "derivedDefaultMethod", "MethodWithNext", 
        "SealedMethodDefinition", "standardGeneric", "nonstandardGenericFunction", 
        "groupGenericFunction", "language", "expression", "name", "call", "{", 
        "if", "<-", "for", "while", "repeat", "(", "externalptr", "NULL")
    assign("RWebServices_RType_To_JClass_RUnknown", toRUnknown, lookup)
    lookup
})

setMethod("newLookup", signature(typeMode="javalib"), function(typeMode) {    
    lookup <- new.env(parent=emptyenv())
    ## primary type mappings
    rType <- c("numeric", "integer", "double", 
                "character", "logical", "list",  
                "data.frame", "raw",
                "complex", "factor", "NULL",
		"CharArray", "CharMatrix",
		"IntegerArray", "IntegerMatrix",
		"NumericArray", "NumericMatrix",
		"LogicalArray", "LogicalMatrix",
		"RawArray", "RawMatrix",
		"ComplexArray", "ComplexMatrix"
		)
    jType <- c("double[]", "int[]", "double[]", 
                "String[]", "boolean[]", "Object[]", 
                "RJDataFrame", "byte[]",
                "RJComplex", "RJFactor", "Object",
		"RJCharArray", "RJCharMatrix",
		"RJIntegerArray", "RJIntegerMatrix",
		"RJNumericArray", "RJNumericMatrix",
		"RJLogicalArray", "RJLogicalMatrix",
		"RJRawArray", "RJRawMatrix",
		"RJComplexArray", "RJComplexMatrix"
		)
    primaryDataPkgName <- primaryDataPkg()
    jPackage <- c("", "", "",
                  "", "", "",
                  primaryDataPkgName, "",
                  primaryDataPkgName, primaryDataPkgName, "java.lang",
		  primaryDataPkgName, primaryDataPkgName,
		  primaryDataPkgName, primaryDataPkgName,
		  primaryDataPkgName, primaryDataPkgName,
		  primaryDataPkgName, primaryDataPkgName,
		  primaryDataPkgName, primaryDataPkgName,
		  primaryDataPkgName, primaryDataPkgName
		)
    r2j <- c("cvtNumericToJava2", "cvtIntegerToJava2", "cvtNumericToJava2",
            "cvtCharacterToJava2", "cvtLogicalToJava2", "cvtListToJava2",
            "cvtDataFrameToJava2", "cvtRawToJava2",
            "cvtComplexToJava2", "cvtFactorToJava2", "",
	    "cvtCharArrayToJava2", "cvtCharMatrixToJava2",
	    "cvtIntegerArrayToJava2", "cvtIntegerMatrixToJava2",
	    "cvtNumericArrayToJava2", "cvtNumericMatrixToJava2",
	    "cvtLogicalArrayToJava2", "cvtLogicalMatrixToJava2",
	    "cvtRawArrayToJava2", "cvtRawMatrixToJava2",
	    "cvtComplexArrayToJava2", "cvtComplexMatrixToJava2"
	     )
    j2r <- c("", "", "",
            "", "", "",
            "cvtDataFrameFromJava2", "",
            "cvtComplexFromJava2", "cvtFactorFromJava2", "",
	    "cvtCharArrayFromJava2", "cvtCharMatrixFromJava2",
	    "cvtIntegerArrayFromJava2", "cvtIntegerMatrixFromJava2",
	    "cvtNumericArrayFromJava2", "cvtNumericMatrixFromJava2",
	    "cvtLogicalArrayFromJava2", "cvtLogicalMatrixFromJava2",
	    "cvtRawArrayFromJava2", "cvtRawMatrixFromJava2",
	    "cvtComplexArrayFromJava2", "cvtComplexMatrixFromJava2"
 	    )
    ## We implcitly imply that if the convert function name is cvtXXXX, then
    ## the match function name is matchXXXX. We don't keep match function name
    ## in the lookup table because it is only used to generate convert function
    ## of ClassUnion (r->java). But this may cause subtle problem if someone
    ## tries to add conversions for some type but not following this naming
    ## conversions.
    for ( i in seq (along = rType )) {
            rjMap <- new("RJMap", 
                        jType = jType[[i]],  
                        jPackage=jPackage[[i]],
                        rPackage="base",
                        typeGroup = "primary", 
                        r2j = r2j[[i]], 
                        j2r = j2r[[i]])
            assign( rType[[i]], rjMap, lookup )
    }
    toRUnknown <- c( "function", "MethodDefinition", "genericFunction", 
        "functionWithTrace", "derivedDefaultMethod", "MethodWithNext", 
        "SealedMethodDefinition", "standardGeneric", "nonstandardGenericFunction", 
        "groupGenericFunction", "language", "expression", "name", "call", "{", 
        "if", "<-", "for", "while", "repeat", "(", "externalptr", "NULL", 
        "matrix", "vector", "environment")
    assign("RWebServices_RType_To_JClass_RUnknown", toRUnknown, lookup)
    lookup
})

printLookup <- function(lookup, all=TRUE) {
        keys <- objects(envir=lookup)
        values <- lapply(keys, get, envir=lookup)
        runknown <- which(keys=="RWebServices_RType_To_JClass_RUnknown")
        cat("---------------------------------------------------------------\n")
        cat("[R Type]  =>  [Java Type]  [Java Package]  [R Package]  [TypeGroup]  [R->Java]  [Java->R]\n")
        cat("---------------------------------------------------------------\n")
        mapply(function(key, value){
            if (all || (value@typeGroup != "primary"))
                cat(key, "=>", value@jType, " ", value@jPackage, " ", value@rPackage, " ", value@typeGroup, " ", value@r2j, " ", value@j2r, "\n")    
        }, keys[-runknown], values[-runknown])
        cat("---------------------------------------------------------------\n")
        return()
}

## inner function used by generateDataMap
##      param: rType is the name of a R ClassUnion
##      return: a character vector of all direct children of this ClassUnion, exclude "NULL"
getDirectSubKlass <- function(rType) {
           subKlass <- getClassDef(rType)@subclasses    ## all descendants
            isDirectChild <- sapply(subKlass, function(y) length(y@by)==0 ) 
            directSubKlass <- names(subKlass[isDirectChild])  ## children only
            ## Don't need to generate java mapping for NULL because any java obj can be NULL
            directSubKlass <- directSubKlass[directSubKlass!="NULL"]  
            directSubKlass
}

## main function of this section
## Create java beans, and mapping function for a R data type if they are not available
## param:
##      rType: R data type, either a S4 or a ClassUnion
##      javaToR: logical, TRUE if want to generate Java -> R mapping, FALSE if
##              want to generate R -> Java mapping.
##      lookup: environment, key is rType, value is of type "RJMap", provide the java type, 
##              java package, and java <-> R convert function names for the key.
## return: updated lookup envir
## SIDE EFFECTS WARNING! It also two environments: lookup 
generateDataMap <- function( rType, javaToR, deployMode, typeMode, lookup) 
{
    ## inner function: recursively create java beans for R S4 classes or ClassUnion
    ## SIDE EFFECT WARNING! Modifies environment lookup
    updateLookup_jType <- function( rType ) {
        toRUnknown <- get("RWebServices_RType_To_JClass_RUnknown", lookup)
        if (rType %in% ls(lookup))  ## primary data type
            return()
        if ((!isClass(rType)) || (rType %in% toRUnknown)) { ## types we don't handle
            stop("generateDataMap: data type ", rType, " is not supported.")
        }
        rPkgName <- getClassDef(rType)@package
        jPkgName <- rPkgTojPkg( rPkgName )
        if (isClassUnion(rType)) {   ## ClassUnion
            ## R type name of member classes
            directSubKlassR <- getDirectSubKlass(rType)
            ## full java type name of member classes (with java package name in front) 
            directSubKlassJ <- sapply(directSubKlassR, function(x) {
                updateLookup_jType(x)
                thisMap <- get(x, lookup, inherits=FALSE)
                jPackage <- thisMap@jPackage
                if (identical(jPackage, ""))
                    res <- thisMap@jType
                else
                    res <- paste(jPackage, ".", thisMap@jType, sep="", collapse="")
                res
            })   
  
            if (length(directSubKlassR)>0) {
                names(directSubKlassJ) <- directSubKlassR
                ## jType is the java type name of rType; notice that jType is a
                ## short name (without package name)
                jType <- generateUnionFactory( rType, jPkgName, directSubKlassJ, typeMode )
                rjMap <- new("RJMap", 
                            jType=jType, jPackage=jPkgName, 
                            rPackage=rPkgName, typeGroup="union", 
                            r2j=character(0), j2r=character(0))
                assign( rType, rjMap, lookup)
            }
        } else {                    ## S4    
            ## recursively set Java matching type
            slotListR <- getClass(rType)@slots
            slotListJ <- lapply(slotListR, function (x) {
                updateLookup_jType( x )
                thisMap <- get( x, lookup, inherits=FALSE )
                jPackage <- thisMap@jPackage
                if (identical(jPackage, ""))
                    res <- thisMap@jType
                else
                    res <- paste(jPackage, ".", thisMap@jType, sep="", collapse="")
                res
            })
            names(slotListJ) <- names(slotListR)
            jType <- generateJavaBean( rType, jPkgName, slotListJ, typeMode)    
            rjMap <- new("RJMap", 
                        jType=jType, jPackage=jPkgName, 
                        rPackage=rPkgName, typeGroup="s4", 
                        r2j=character(0), j2r=character(0))
            assign( rType, rjMap, lookup )
        } 
        return()
    }
    
    ## inner function: recursively create java <-> R mapping functions
    updateLookup_map <- function(rType, javaToR) {
        rjMap <- get(rType, envir=lookup, inherits=FALSE)
        if ( (javaToR && (length(rjMap@j2r)==0)) || ((!javaToR) && (length(rjMap@r2j)==0)) )
        {
            if (isClassUnion(rType)) {   ## ClassUnion
                ## R type name of member classes
                theList <- getDirectSubKlass(rType)
            } else {                     ## S4 Class
                theList <- getClass(rType)@slots
            }
            for (x in unlist(theList)) {
                    updateLookup_map(x, javaToR)
            }
            if (javaToR) {
                rjMap <- generateMapFromJava(rType, theList, lookup, deployMode)
            } else {
                rjMap <- generateMapToJava(rType, theList, lookup, deployMode)
            }
            assign(rType, rjMap, lookup)
        }
        return()
    }
    ##printLookup(lookup)
    ##cat("===============updateLookup_jType(...)=========\n")
    updateLookup_jType( rType )
    ##printLookup(lookup)
    ##cat("===============updateLookup_map(...)=========\n")
    updateLookup_map( rType, javaToR )
    ##printLookup(lookup)
    lookup
} 

                
## parse R function signature and resolves java-R mapping for function
##      parameters and return value
## SIDE EFFECTS WARNING: modifies environment "lookup"  
rjSigsToRJMap <- function( rjSigs, lookup, deployMode, typeMode, verbose ){
    for ( sig in rjSigs ) {
        if (verbose) {
            cat(">>>>>> Generate data map for R function: \n")
            print(sig)
        }
        if (verbose)
            cat("\tdata map for: ", sig@returnType, "\n")
        lookup <- generateDataMap(sig@returnType, javaToR=FALSE, deployMode=deployMode, 
                        typeMode=typeMode, lookup=lookup) 
        for ( arg in sig@args ) {
            if (verbose)
                cat("\tdata map for: ", arg, "\n") 
            lookup <- generateDataMap(arg, javaToR=TRUE, deployMode=deployMode,
                            typeMode=typeMode, lookup=lookup) 
        }
    }
    if (verbose) 
            printLookup(lookup, all=FALSE)
    lookup
}
