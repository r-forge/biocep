########################################
# section : class definition
########################################  

## mapping information for a R data type
##  slots:
##      jType: name of the corresponding java type.
##              if typeGroup="s4", jType=capName(rType)
#3              if typeGroup="union", jType is rType plus "Factory"
##      package: the package name of the java type;
##              if typeGroup="primary", then package="org.bioconductor.packages.rservices";
##              otherwise package is the name of the environment where the
##                  S4 class or ClassUnion is defined, with "." replaced by "r"
##                  eg. "rGlobalEnv"
##      typeGroup:  R data types are divided into three categories. They are handled
##             by different functions.
##          primary: R data types that have build-in mapping in RWebServices
##          union:   R ClassUnion (auto-generated mapping)
##          s4:      R S4 Classes (auto-generated mapping)
##      j2r: convert function name if java to R mapping (function parameter) exists,
##           character(0) otherwise
##      r2j: convert function name if R to java mapping (function return value) exists,
##           character(0) otherwise
setClass("RJMap", representation(jType="character", jPackage="character", 
                        rPackage="character", typeGroup="character", 
                        r2j="character", j2r="character"))
                  

##  java mapping information for the functions of a particular R package
##  slots:
##      rPackage:  a R package name
##      javaClass: the full name (with package name in front) of the java class which
##              contains all java mapping functions of the functions in the R 
##              package (rPackage)
##      javaTypeInfo: the function siguatures of those java mapping functions 
##              inside 'javaClass', it is a list of "RJavaSignature" class instances.
setClass("RJavaPkgFunctions",
          representation(rPackage="character", javaClass="character", javaTypeInfo="list"))

setMethod("show", signature(object="RJavaPkgFunctions"),
           function(object) {
               cat("R package:", object@rPackage, "\n")
               cat("Java class:", object@javaClass, "\n")
               cat("Function signatures:\n")
               show(object@javaTypeInfo)
           })
                        
setClass("deployMode")
setClass("demo", contains="deployMode")
setClass("jms", contains="deployMode")

setClass("typeMode")
setClass("robject", contains="typeMode")
setClass("javalib", contains="typeMode")
