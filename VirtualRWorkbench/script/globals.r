square <- function(x) {return(x^2) }
typeInfo(square) <- SimultaneousTypeSpecification(TypedSignature(x = "numeric"), returnType = "numeric")

setClass("Point", representation(x="numeric", y="numeric"))
setGeneric("distance", function(p1,p2) standardGeneric("distance"))
setMethod("distance", signature("Point", "Point") , function(p1, p2) {  sqrt( (p2@x-p1@x)^2 + (p2@y-p1@y)^2 )    })

