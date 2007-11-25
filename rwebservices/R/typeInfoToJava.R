setClass("RJavaSignature", representation(returnType = "character",
                                          funcName = "character",
                                          args = "character"),
         validity = function(object) {
             msg <- NULL
             if (is.na(object@returnType))
               msg <- paste(msg,"\n    return type 'NA' should be specified as character string 'NULL'")
             if (is.null(msg)) TRUE else msg
         })

setMethod("show", signature(object = "RJavaSignature"),
          function(object) {
              cat(object@returnType, " ", object@funcName, "(",
                  paste(object@args, names(object@args), collapse=", "),
                  ")", sep="")
          })

setGeneric("typeInfo2Java",
           function( x, ... ) standardGeneric( "typeInfo2Java" ))

setMethod("typeInfo2Java", signature( x = "function" ),
          function( x, funcName, ... ) {
              if ( missing(funcName) )
                  funcName <- sys.call(-2)[[2]]
              typeInfo2Java( typeInfo( x ), funcName )
          })

setMethod("typeInfo2Java", signature( x = "genericFunction" ),
          function( x, funcName, S4DefaultTypedSig = TypedSignature(), ... ) {
              ## typeInfo-like info for S4generics
              updateASig <- function( base, args, classes, returnType ) {
                  base[args] <- lapply( classes, InheritsTypeTest )
                  base
              }
              typeSigs <- list()
              lmList <- linearizeMlist( getMethods( x ))
              if ( length( x@valueClass ))
                  S4DefaultTypedSig@returnType <- InheritsTypeTest( x@valueClass )
              typeSigs <-
                  lapply(1:length(lmList@arguments),
                         function(i)
                         updateASig(S4DefaultTypedSig, lmList@arguments[[i]], lmList@classes[[i]],
                                    x@valueClass ))
              typeInfo <- as(typeSigs, "SimultaneousTypeSpecification")
              ## typeInfo2Java
              if ( missing(funcName) )
                  funcName <- sys.call(-2)[[2]]
              typeInfo2Java( typeInfo, funcName )
          })

setMethod("typeInfo2Java", signature( x = "ANY" ),
          function( x, ... ) {
              stop(paste( "Class \"", class(x), "\" not yet implemented\n", sep = ""))
          })

setMethod("typeInfo2Java", signature( x = "list" ),
          function( x, args, ... ) {
              unlist(sapply(seq( along = x ),
                            function( i ) typeInfo2Java( x[[i]], args[[i]], ... )))
          })

setMethod("typeInfo2Java", signature( x = "SimultaneousTypeSpecification" ),
          function( x, funcName, ... ) {
              aSignature <- function( typeSig ) {
                  if (length(typeSig@returnType) == 0 ||
                      (length( typeSig@returnType ) == 1 && is.na( typeSig@returnType )))
                      typeSig@returnType <- x@returnType
                  sig <- typeInfo2Java( typeSig, ... )
                  sig@funcName <- as.character(funcName)
                  sig
              }
              if ( length( x ) == 0 ) # return type specification only
                  aSignature( TypedSignature( returnType = x@returnType ))
              else
                  lapply( x, aSignature )
          })

setMethod("typeInfo2Java", signature( x = "IndependentTypeSpecification" ),
          function( x, funcName, ... ) {
              returnType <- typeInfo2Java( x@returnType, "returnType" )
              ## signatures convuluted...
              convolute <- function( args ) {
                  if ( !length(args) ) return(list())
                  if ( length(args) == 1 ) return(args[[1]])
                  outer(args[[1]], convolute( args[-1] ),
                        function( x, y ) lapply(seq(along = x),
                                                function(i) c( x[i], y[i] )))
              }
              argNames <- names( x )
              args <- sapply( seq( along = x@.Data),
                             function(i) typeInfo2Java( x[[i]], argNames[[i]]  ))
              argSigs <- convolute( args )
              ## ...and formated
              if ( length( x ) == 0 ) {
                  new("RJavaSignature",
                      returnType = returnType,
                      funcName = as.character( funcName ),
                      args = argSigs)
              } else {
                  lapply(argSigs, function( args ) {
                    args <- args[args!="missing"] # drop missing args
                    new("RJavaSignature",
                        returnType = returnType,
                        funcName = as.character(funcName),
                        args = args )
                  })
              }
          })

## typically corresponds to a single function call
setMethod("typeInfo2Java", signature( x = "TypedSignature" ),
          function( x, ... ) {
            ret <- as.character( typeInfo2Java( x@returnType, "returnType", ... ))
            args <- typeInfo2Java( x@.Data, names( x ))
            args <- args[args!="missing"] # drop missing args
            new("RJavaSignature",
                returnType = ret,
                args = args)
          })

## argument 
setMethod("typeInfo2Java", signature( x = "character" ),
          function( x, args, ... ) {
            names(x) <- rep(args, length( x ))
            x
          })

## argument or return type
setMethod("typeInfo2Java", signature( x = "InheritsTypeTest" ),
          function( x, args, ... ) {
              x <- x@.Data
##               x[is.na(x)] <- character(0)
              names( x ) <- rep( args, length( x ))
              x
            })

setMethod("typeInfo2Java", signature( x = "StrictIsTypeTest" ),
          function( x, args, ... ) {
              warning("StrictIsTypeTest treated as InheritsTypeTest for Java")
              x <- x@.Data
##               x[is.na(x)] <- character(0)
              names( x ) <- rep( args, length( x ))
              x
          })

setMethod("typeInfo2Java", signature( x = "DynamicTypeTest" ),
          function( x, args, ... ) {
              warning("DynamicTypeTest converted to \"void\" for Java")
              x <- rep("void", length(args))
              names( x ) <- args
              x
          })
