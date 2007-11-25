setGeneric("createMap",
           function(funcs, pkgs,
                    generateTests=TRUE,
                    outputDirectory=stop("specify outputDirectory"),
                    typeMode="javalib",
                    deployMode="jms",
                    verbose=FALSE,
                    ...) standardGeneric("createMap"),
           signature=c("funcs", "pkgs"))

asValidNames <- function(nms) {
    ## remove embedded . and replace with camel-case
    nms <- gsub("([a-zA-Z1-9])\\.([a-zA-Z1-9])","\\1\\U\\2", nms, perl=TRUE)
    ## leading .
    nms <- gsub("^\\.", "_", nms)
    nms
}

setMethod("createMap",
          signature(funcs="missing", pkgs="character"),
          function(funcs, pkgs,
                   generateTests, outputDirectory, typeMode, deployMode, verbose,
                   splitPkgsToVector=TRUE,
                   ...) {
              if (splitPkgsToVector && length(pkgs)==1) pkgs <- unlist(strsplit(pkgs, ","))
              sigs <- lapply(as.list(pkgs), function(pkg) {
                  if (!require(pkg, quietly=TRUE, character.only=TRUE))
                    stop("could not find package '", pkg, "'")
                  typed <- unlist(eapply(getNamespace(pkg), function(elt)
                                         "TypeInfo" %in% names(attributes(elt))))
                  nms <- asValidNames(names(typed[typed]))
                  mapply(typeInfo2Java,
                         lapply(nms, get, envir=getNamespace(pkg)),
                         nms)
              })
              generateFunctionMap(unlist(sigs, recursive=FALSE),
                                  genTest=generateTests,
                                  workDir=outputDirectory,
                                  verbose=verbose,
                                  deployModeName=deployMode,
                                  typeModeName=typeMode,
                                  ...)
          })

setMethod("createMap",
          signature(funcs="standardGeneric", pkgs="missing"),
          function(funcs, pkgs,
                   generateTests, outputDirectory, typeMode, deployMode, verbose,
                   ...) {
              sigs <- typeInfo2Java(funcs, asValidNames(deparse(substitute(funcs))), ...)
              generateFunctionMap(sigs,
                                  genTest=generateTests,
                                  workDir=outputDirectory,
                                  verbose=verbose,
                                  deployModeName=deployMode,
                                  typeModeName=typeMode,
                                  ...)
          })

setMethod("createMap",
          signature(funcs="character", pkgs="missing"),
          function(funcs, pkgs,
                   generateTests, outputDirectory, typeMode, deployMode, verbose,
                   ...) {
              nms <- asValidNames(funcs)
              funcs <- lapply(funcs, get, envir=parent.frame())
              sigs <- typeInfo2Java(funcs, nms)
              if (verbose) { cat("typeInfo2Java:\n"); print(sigs) }
              generateFunctionMap(sigs,
                                  genTest=generateTests,
                                  workDir=outputDirectory,
                                  verbose=verbose,
                                  deployModeName=deployMode,
                                  typeModeName=typeMode,
                                  ...)
          })
