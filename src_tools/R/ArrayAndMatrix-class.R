########################################
# Authors : Nianhua Li, MT Morgan
# License : caBIG 
########################################
## *Matrix and *Array -- essentially setClass("NumericArray", contains="array"), etc
local({
    labels <- c("Raw", "Char", "Logical", "Integer", "Numeric", "Complex")
    types <- c("raw", "character", "logical", "integer", "double", "complex")
    ClassLabels <- c("Matrix", "Array")
    classLabels <- sub("^([A-Z])", "\\L\\1", ClassLabels, perl=TRUE)

    for (i in seq(along=classLabels)) {
        classes <- mapply(function(lbl, type) {
            substitute(setClass(CLASSNAME,
                                prototype=local({
                                    m <- new(CONTAINS)
                                    mode(m) <- TYPE
                                    m
                                }),
                                contains=c(CONTAINS),
                                validity=function(object) {
                                    if (typeof(object)==TYPE) TRUE
                                    else paste("typeof(", CLASSNAME, ") must be '", TYPE,
                                               "' but is '", mode(object), "'", sep="")
                                }),
                       list(CLASS=lbl,
                            CLASSNAME=paste(lbl, ClassLabels[[i]], sep=""),
                            TYPE=type,
                            CONTAINS=classLabels[[i]]))
        }, labels, types, SIMPLIFY=FALSE)
        for (cl in classes) eval(cl, envir=topenv())
    }
})
