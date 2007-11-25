checkJava2R <- function(javaData, rVariable) {
	rData <- get(rVariable)
	if (!identical(rData, javaData)) {
        stop(paste(capture.output({
            cat("<expect>\n")
            debugPrint(rData)
            cat("<but was>\n")
            debugPrint(javaData)
        }), collapse="\n"))
    }
    TRUE
}

debugPrint <- function(rData) {
    x <- try(str(rData), silent=T)
    if(is(x, "try-error"))
        print(rData)
}

reflectObj <- function(x, verbose=FALSE) {
    if (verbose) {
        cat("[R] get ", class(x), " (", length(x), "):")
        print(x)
        if(is.environment(x))
            print(ls(envir=x))
        cat("now return to java...\n")
    }
    x
}    

checkPkgVersion <- function(pkgName, expVersion) {
    actualVersion <- packageDescription(pkgName)$Version
    if(!identical(actualVersion, expVersion))
      warning("expected ", pkgName, " v. ", expVersion,
              " but got v. ", actualVersion, ".\n")
    ""
}
