## toDir <- "/home/mtmorgan/tmp/RWebServices"

unpackAntScript <- function(toDir=stop("specify destination directory for unpacking"),
                            overwrite=FALSE) {
    fromDir <- system.file("scripts", package="RWebServices")
    fromFiles <- c("userbuild.xml", "RWebServicesEnv.properties", "RWebServicesTuning.properties")
    toFiles <- c("build.xml", fromFiles[-1])
    from <- file.path(fromDir, fromFiles)
    to <- file.path(toDir, toFiles)
    if (!file.exists(toDir)) {
        cat("Creating:", toDir, "\n")
        dir.create(toDir, recursive=TRUE)
    }
    results <- file.copy(from, to, overwrite=overwrite)
    if (!all(results))
      warning("files already exist, not copied:\n\t",
              paste(to[!results], collapse="\n\t"), "\n")
}
