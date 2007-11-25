STS <- SimultaneousTypeSpecification
TS <- TypedSignature


square <- function(x) {return(x^2) }
typeInfo(square) <- STS(TS(x = "numeric"), returnType = "numeric")


square_root <- function(x) { sqrt(x) }
typeInfo(square_root) <- STS(TS(x = "numeric"), returnType = "numeric")



setClass("A", representation( y="character", bb="numeric" , x="character", a="numeric"))
setClass("B", representation( q="A", v="numeric"))
setClass("C", representation( s="numeric"))



setClass("D", representation( pchar="character", pnum="numeric", plogic="logical", pcomplex="complex", pint="integer"))

setClass("E", representation( c="C", b="B" ))


setGeneric("foo", function(x) standardGeneric("foo"))
setMethod("foo", "C", function(x) {new ("B", v=x@s)})






