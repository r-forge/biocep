########################################
# Authors : Nianhua Li, MT Morgan
# License : caBIG 
########################################

############################
# section 3: Creating Match/Convert functions in R for java <-> R mapping
############################

## create match/convert function to map the java bean to R S4 class
## param:
##      rType: name of the R S4 class
##      rjMap: value for key rType in envir lookup, of type RJMap
##          slot jType is the java class name (short name, without package)
##          slot j2r is the convert function name
##      slotList: a named vector, name is the slot names of the R S4 class,
##                  value is the slot data type.
##      lookup: environment, key is rType, value is of type "RJMap", provide the java type, 
##              java package, and java <-> R convert function names for the key.
## return: match/convert functions in a character string
createMapFromJava <- function(rType, rjMap, slotList, lookup) {
        ## full java class name
        jType <- paste(rjMap@jPackage, ".", rjMap@jType, sep="", collapse="")
        thisCvtFun <- rjMap@j2r
        ## warning: implicit naming rule for match / convert functions
        thisMatchFun <- sub("cvt", "match", thisCvtFun, fixed=TRUE)
        header <-  paste("\n#####################\n",
                        "# Matcher and Converter for Java Class ", jType, ".\n",
                        "#####################\n",
                        sep="", collapse="")
        matcher <- paste(
                        "\n#  Function Matches Java Class ", jType, ".\n",
                        thisMatchFun, " <- function(x, thisClassName){\n",
                        "\tthisClassName == \"", jType, "\"\n",
                        "}\n", sep="", collapse="")
        cvtFunNames <- sapply(slotList, function(x) (get(x, envir=lookup, inherits=FALSE))@j2r )
        defaultCvtIndex <- which(cvtFunNames=="")
        slotName <- names(slotList)
        ## warning: implicit naming rule: assume the data field name of the java
        ## class for this S4 class is the mangleName(slotName), and the access function
        ## name in the java class is paste("get", capName(mangleName(slotName)), sep="")
        jAccessFunc <- paste("get", capName(mangleName(slotName)), sep="")
        cvtPart <- paste("\tinput_", slotName, " <- .Java(x, \"", 
                         jAccessFunc, "\", .convert=FALSE)\n", 
                         "\tif (!is.null(input_", slotName, "))\n", 
                         "\t\t", slotName, " <- ", cvtFunNames, "(",
                        "input_", slotName, ", NULL)\n",
                         "\telse\n",
                         "\t\tstop(\"java to R converter for class '",
                         rType, "'\",",
                         "\n\t\t     \" recieved NULL for slot '",
                         slotName, "'\")\n",
                        sep="")
        cvtPart[defaultCvtIndex] <- paste("\t", slotName[defaultCvtIndex], " <- .Java(x, \"",
                        jAccessFunc[defaultCvtIndex], "\")\n", sep="")
        cvtPart <- paste(cvtPart, sep="", collapse="")    
        converter <- paste(
                        "\n# Function Converts Java Class ", jType, "\n",
                        "# to R Object ", rType, ".\n",
                        thisCvtFun, " <- function(x, thisClassName){\n",
                        cvtPart,
                        '\tnew("', rType, '",\n\t    ',
                           paste(slotName, '=', slotName,
                                 sep="", collapse=",\n\t    "),
                           ")\n",
                        "}\n", sep="", collapse="")
        register <- paste(
                        "\n# Register matcher and converter\n",
                        "setJavaFunctionConverter( \n",
                        "\t", thisCvtFun, ",\n",
                        "\t", thisMatchFun, ",\n",
                        "\tdescription=\"Java ", jType, " to R ", rType, "\",\n",
                        "\tfromJava=T, position=1)\n", sep="", collapse="")
        wholeStr <- paste(header, matcher, converter, register, sep="", collapse="")
        wholeStr
}

## create match/convert function to map the java class for R ClassUnion
## param:
##      rType: name of the R ClassUnion
##      rjMap: value for key rType in envir lookup, of type RJMap
##          slot jType is the java class name (short name, without package)
##          slot j2r is the convert function name
## return: match/convert functions in a character string
createFactoryMapFromJava <- function(rType, rjMap) {
        jType <- paste(rjMap@jPackage, ".", rjMap@jType, sep="", collapse="")
        thisCvtFun <- rjMap@j2r
        header <-  paste("\n#####################\n",
                        "# Converter for Java Class ", jType, ".\n",
                        "#####################\n",
                        sep="", collapse="")
        converter <- paste(
                        "\n# Function Converts Java Class ", jType, "\n",
                        "# to R Object ", rType, ".\n",
                        thisCvtFun, " <- function(x, thisClassName){\n",
                        "\tans <- .Java(x, \"getData\")\n",
                        "\tans\n",
                        "}\n", sep="", collapse="")
        wholeStr <- paste(header, converter, sep="", collapse="")
        wholeStr
}

## create match/convert function to map the java bean to R S4 class
## param:
##      rType: name of the R S4 class
##      rjMap: value for key rType in envir lookup, of type RJMap
##          slot jType is the java class name (short name, without package)
##          slot r2j is the convert function name
##      slotList: a vector of the slot names of the R S4 class
##      lookup: environment, key is rType, value is of type "RJMap", provide the java type, 
##              java package, and java <-> R convert function names for the key.
## return: match/convert functions in a character string
createMapToJava <- function(rType, rjMap, slotList, lookup) {
        jType <- paste(rjMap@jPackage, ".", rjMap@jType, sep="", collapse="")
        thisCvtFun <- rjMap@r2j
        ## warning: implicit naming rule for match / convert functions
        thisMatchFun <- sub("cvt", "match", thisCvtFun, fixed=TRUE)
        header <-  paste("\n#####################\n",
                        "# Matcher and Converter for R Object ", rType, ".\n",
                        "#####################\n",
                        sep="", collapse="")
        matcher <- paste(
                        "\n#  Function Matches R Object ", rType, ".\n",
                        thisMatchFun, " <- function(x, ...){\n",
                        "\tinherits(x, \"", rType, "\")\n",
                        "}\n", sep="", collapse="")
        cvtFunNames <- sapply(slotList, function(x) (get(x, envir=lookup, inherits=FALSE))@r2j )
        slotName <- names(slotList)
        ## warning: implicit naming rule: assume the data field name of the java
        ## class for this S4 class is the mangleName(slotName), and the assert function
        ## name in the java class is paste("set", capName(mangleName(slotName)), sep="")
        jAssertFunc <- paste("set", capName(mangleName(slotName)), sep="")
        cvtPart <- paste( "\tinput_", slotName, " <- ", cvtFunNames, 
                        "( x@", slotName, " )\n",                        
                        "\t.Java(thisClass, \"", jAssertFunc, "\", ",
                        "input_", slotName, ", .convert=FALSE)\n", sep="") 
        cvtPart <- paste(cvtPart, sep="", collapse="")    
        converter <- paste(
                        "\n# Function Converts R Object ", rType, "\n",
                        "# to Java Class ", jType, ".\n",
                        thisCvtFun, " <- function(x, ...){\n",
                        "\tthisClass <- .JNew(\"", jType, "\", .convert=FALSE)\n",
                        cvtPart,
                        "\tthisClass\n",
                        "}\n", sep="", collapse="")
        register <- paste(
                        "\n# Register matcher and converter\n",
                        "setJavaFunctionConverter( \n",
                        "\t", thisCvtFun,",\n",
                        "\t", thisMatchFun, ",\n",
                        "\tdescription=\"R ", rType, " to Java ", jType, "\",\n",
                        "\tfromJava=F, position=1)\n", sep="", collapse="")
        wholeStr <- paste(header, matcher, converter, register, sep="", collapse="")
        wholeStr
}

## create match/convert function to map the java bean to R S4 class
## param:
##      rType: name of the R ClassUnion
##      rjMap: value for key rType in envir lookup, of type RJMap
##          slot jType is the java class name (short name, without package)
##          slot j2r is the convert function name
##      memberList: a vector of the member class names of the R ClassUnion
##      lookup: environment, key is rType, value is of type "RJMap", provide the java type, 
##              java package, and java <-> R convert function names for the key.
## return: match/convert functions in a character string
createFactoryMapToJava <- function(rType, rjMap, memberList, lookup) {
        jType <- paste(rjMap@jPackage, ".", rjMap@jType, sep="", collapse="")
        thisCvtFun <- rjMap@r2j
        ## warning: implicit naming rule for match / convert functions
        thisMatchFun <- sub("cvt", "match", thisCvtFun, fixed=TRUE)
        cvtFunNames <- sapply(memberList, function(x) (get(x, envir=lookup, inherits=FALSE))@r2j )
        ## warning: implicit naming rule for match / convert functions
        matchFunNames <- sub("cvt", "match", cvtFunNames, fixed=TRUE)
        ## warning: implicit naming rule for member factories
        ## It may cause subtle errors if someone tries to use his own factories
        memberJType <- memberFactoryName(jType, memberList)     
        header <-  paste("\n#####################\n",
                        "# Matcher and Converter for R Object ", rType, ".\n",
                        "#####################\n",
                        sep="", collapse="")
        matcher <- paste(
                        "\n#  Function Matches R Object ", rType, ".\n",
                        thisMatchFun, " <- function(x, ...){\n",
                        "\tinherits(x, \"", rType, "\")\n",
                        "}\n", sep="", collapse="")
        ifPart <- rep("\t} else if (", length(memberList)-1)
        ifPart <- c("\tif (", ifPart)
        cvtPart <- paste(ifPart, matchFunNames, "(x)) {\n",
                        "\t\tfactory <- .JNew(\"", memberJType, "\", .convert=FALSE)\n",
                        "\t\tx <- ", cvtFunNames, "(x)\n", 
                        sep="", collapse="")
        subKlass <- names(getClassDef(rType)@subclasses)    ## all descendants
        if( "NULL" %in% subKlass )
            nullPart <- paste(
                 "\t} else if ( is.null(x) ) {\n",
                 "\t\tfactory <- .JNew(\"", memberJType[[1]], "\", .convert=FALSE)\n",
                 sep="")
        else
            nullPart <- ""
        cvtPart <- paste(cvtPart, nullPart, 
                    "\t} else {\n",
                    "\t\tcat(\"WARNING: \", class(x), \" is an invalid data type for ", rType, ".\n\")\n",
                    "\t}\n",
                    sep="", collapse="")    
        converter <- paste(
                        "\n# Function Converts R Object ", rType, "\n",
                        "# to Java Class ", jType, ".\n",
                        thisCvtFun, " <- function(x, ...){\n",
                        cvtPart,
                        "\t.Java(factory, \"setData\", x, .convert=FALSE)\n",
                        "\tfactory\n",
                        "}\n", sep="", collapse="")
        wholeStr <- paste(header, matcher, converter, sep="", collapse="")
        wholeStr
}
## write the match/convert functions to a file
## param:
##      funBody: function bodies in character string
##      prefix: the sub-directory of file "TypeConvert.R"
## return: NULL
## details: create  directory "prefix" under working directory
##                  file "TypeConvert.R" under directory "prefix"
writeMapFun <- function( funBody, rPkgName, deployMode ) {
       outputPath <- cvtScriptsDir(deployMode)
       dir.create(path=outputPath, showWarnings=FALSE, recursive=TRUE)
       outputFile <- file.path(".", outputPath, paste(rPkgName, ".R", sep="", collapse=""))
       cat(funBody, file=outputFile, append=TRUE)
       return()
}

## inner function used by generateMapFromJava  and generateMapToJava
## add convert function name to RJMap
addCvtToMap <- function(rType, lookup, fromTo) {
    rjMap <- get(rType, envir=lookup, inherits=FALSE)
    fullJType <- paste(rjMap@rPackage, ".", rjMap@jType, sep="", collapse="")
    cvtFunc <- paste("cvt_", fullJType, "_", fromTo, "Java", sep="", collapse="")
    if (fromTo == "From")
        rjMap@j2r <- cvtFunc
    else
        rjMap@r2j <- cvtFunc
    rjMap
}

## main functions of this section
## param:
##      rType: a R S4 class name or ClassUnion name 
##      theList: slot class name or member class name (R data type name)
##              if rType is a S4 class, then names(theList) is the slot name
##      lookup: environment, key is rType, value is of type "RJMap", provide the java type, 
##              java package, and java <-> R convert function names for the key.
##      sourceList: a string containing a block of R code. The R code block will be added
##              to the front of TypeConvert.R. 
## return: updated "RJMap" for rType
generateMapFromJava <- function(rType, theList, lookup, deployMode) {
    rjMap <- addCvtToMap(rType, lookup, "From")
    if (rjMap@typeGroup == "union")
        funcBody <- createFactoryMapFromJava(rType, rjMap)
    else        ## rjMap@typeGroup == "s4"
        funcBody <- createMapFromJava(rType, rjMap, theList, lookup)
    writeMapFun(funcBody, rjMap@rPackage, deployMode)
    rjMap
}

## main functions of this section
## param:
##      rType: a R S4 class name or ClassUnion name 
##      theList: slot class name or member class name (R data type name)
##              if rType is a S4 class, then names(theList) is the slot name
##      lookup: environment, key is rType, value is of type "RJMap", provide the java type, 
##              java package, and java <-> R convert function names for the key.
##      sourceList: a string containing a block of R code. The R code block will be added
##              to the front of TypeConvert.R. 
## return: updated "RJMap" for rType
generateMapToJava <- function(rType, theList, lookup, deployMode) {
    rjMap <- addCvtToMap(rType, lookup, "To") 
    if (rjMap@typeGroup == "union")
        funcBody <- createFactoryMapToJava(rType, rjMap, theList, lookup)
    else       ## rjMaep@typeGroup == "s4"
        funcBody <- createMapToJava(rType, rjMap, theList, lookup)
    writeMapFun(funcBody, rjMap@rPackage, deployMode)
    rjMap
}

