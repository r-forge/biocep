setClass("JustVSNParameter",
         representation=representation(
           strata="factor",
           lts.quantile="numeric",
           subsample="integer"),
         prototype=prototype(
           strata=factor(integer(0), levels="all"),
           lts.quantile=1,
           subsample=0L)) 
              
 webVsn <- function(expressionMatrix, justVSNParameter) {
    klass <- class(justVSNParameter)
    args <- lapply(slotNames(klass),
                   function(elt) slot(justVSNParameter, elt))
    names(args) <- slotNames(klass)
    res <- do.call("justvsn",
                   c(new("ExpressionSet", exprs=expressionMatrix),
                     args))
    exprs(res)
}

typeInfo(webVsn) <-
  SimultaneousTypeSpecification(
    TypedSignature(
      expressionMatrix="matrix",
      justVSNParameter="JustVSNParameter"),
    returnType="matrix") 