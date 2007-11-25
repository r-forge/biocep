setClass("Point", representation(x="numeric", y="numeric"))
setGeneric("distance", function(p1,p2) standardGeneric("distance"))
setMethod("distance", signature("Point", "Point") , function(p1, p2) {  sqrt( (p2@x-p1@x)^2 + (p2@y-p1@y)^2 )    })

library(vsn)

data(kidney)
getKidney <- function(x) { if (x==0) exprs(kidney) else exprs(justvsn(kidney)) }
typeInfo(getKidney) <- SimultaneousTypeSpecification( TypedSignature(x = "integer"), returnType = "matrix" )

getKidneyES <- function(x) { if (x==0) kidney else justvsn(kidney) }
typeInfo(getKidneyES) <- SimultaneousTypeSpecification( TypedSignature(x = "integer"), returnType = "ExpressionSet" )

typeInfo(justvsn) <- SimultaneousTypeSpecification( TypedSignature(x = 'ExpressionSet'), returnType = 'ExpressionSet' )
