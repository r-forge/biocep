########################################
# Authors : Nianhua Li, MT Morgan
# License : caBIG 
########################################

########################################
# section 0: functions for name mangling 
########################################
javaPkgRoot <- new.env(parent=emptyenv())
assign("root", "org.kchine.r", env=javaPkgRoot)

updateJavaPkgRoot <- function(inputPkgRoot) {
    assign("root", inputPkgRoot, env=javaPkgRoot)
}

capName <- function(s) {
    paste(toupper(substring(s, 1,1)), substring(s, 2), sep="")
}

mangleName <- function(s) {
    s <- gsub(".", "r", s, fixed=TRUE)
    s <- unlist(lapply(s, function(x) { 
            if (exists(x, envir=jReservedWord, inherits=FALSE))  
                x <- get(x, envir=jReservedWord, inherits=FALSE)
            x
    }))
    s
}

memberFactoryName <- function(mainJType, rMemberClass) {
    paste(mainJType, "ForR", mangleName(rMemberClass), sep="")
}

setGeneric("getFullModeName", function(deployMode) standardGeneric("getFullModeName"))
setMethod("getFullModeName", signature(deployMode="demo"), 
    function(deployMode) {
	javaPkgRootStr <- get("root", javaPkgRoot)
	paste(javaPkgRootStr, "rserviceDemo", sep=".")
    }
)
setMethod("getFullModeName", signature(deployMode="jms"), 
    function(deployMode) {
	javaPkgRootStr <- get("root", javaPkgRoot)
	paste(javaPkgRootStr, "rserviceJms", sep=".")
    }
)

ServToServPkg <- function(servName, deployMode) {
    #servName <- mangleName(servName)
    #servName <- sub("(\\w)", "\\L\\1", servName, perl=T) ## first letter lower case
    paste(getFullModeName(deployMode), "services", servName, sep=".")
}

rworkerPkg <- function() {
	javaPkgRootStr <- get("root", javaPkgRoot)
    	paste(javaPkgRootStr, "rserviceJms.worker", sep=".")
}

setGeneric("cvtScriptsDir", function(deployMode) standardGeneric("cvtScriptsDir"))
setMethod("cvtScriptsDir", signature(deployMode="demo"), 
    function(deployMode) {
	javaPkgRootStr <- get("root", javaPkgRoot)
        cvtDir <- paste(javaPkgRootStr, "rserviceDemo.services.Demo.R", sep=".")
        gsub(".", .Platform$file.sep, cvtDir, fixed=TRUE)
    }
)
setMethod("cvtScriptsDir", signature(deployMode="jms"), 
    function(deployMode) {
	javaPkgRootStr <- get("root", javaPkgRoot)
        cvtDir <- paste(javaPkgRootStr, "rserviceJms.worker.R", sep=".")
        gsub(".", .Platform$file.sep, cvtDir, fixed=TRUE)
    }
) 
    
## generate java package name for a given R package name, based on arbitrary
## naming rules.
rPkgTojPkg <- function(rPkg) {
    ## usually java pakcage name doesn't contain "."
    jPkg <- mangleName(rPkg)
    jPkg <- sub("(\\w)", "\\L\\1", jPkg, perl=T) ## first letter lower case
    javaPkgRootStr <- get("root", javaPkgRoot)
    paste(javaPkgRootStr, "packages", jPkg, sep=".")
}

##  inner function used by generateFunctionMap
##  input a R package name, return a java class name based on arbitary naming rules.
##  The java class holds the java mapping for r functions in the input R package
rPkgToMainJKlass <- function(rPkg) {
    ## usually java pakcage name doesn't contain "."
    ## mtm: class names may sometimes be lowercase
    paste(mangleName(rPkg), "Function", sep="")
}

## param:
##      rPkg: R package name
##      JtypeSig: signature of a java function, which is a wrapper for a R function
##                  the wrapped R function belongs to rPkg
## return:
##      a java function name in the main java API. This java function will invoke
##      the java function represented by JtypeSig
getApiFuncName <- function (rPkg, JtypeSig) {
    apiFunc <- paste(mangleName(rPkg), JtypeSig@funcName, sep="_")
    sub("(\\w)", "\\L\\1", apiFunc, perl=T) ## first letter lower case
}  

rPkgToService <- function(rPkg) {
    ## mtm: class names may sometimes be lowercase
    mangleName(rPkg)
}

primaryDataPkg <- function() {
    "org.kchine.r"
}

########################################
# section 1: generate java doc from R help doc
########################################
parse_link <- function(txt) {
    if (nchar(txt)>0) {
        txt <- paste(" ", txt, sep="", collapse="")
        while ((pos <- regexpr("(\\\\link\\{)([^\\}]*)(\\})", txt)) != -1) {
            linkstr <- substring(txt, pos+6, pos+attr(pos, "match.length")-2)
            txt <- paste(substring(txt, 1, pos-1), linkstr,
                         substring(txt, pos+attr(pos, "match.length")), sep="", collapse="")
        }
        txt <- (substring(txt, 2))
    }
    txt
}

parse_desc <- function(txt, descTag=TRUE) {
    # convert Rd format tags in plain text into HTML tags
    txt <- gsub("<", "&lt;", txt)
    txt <- gsub(">", "&gt;", txt)
    txt <- gsub("&", "&amp;", txt)
    txt <- mapply(parse_link, txt)
    txt <- gsub("(\\\\samp\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\pkg\\{)([^\\}]*)(\\})", "package \\2", txt)
    txt <- gsub("(\\\\file\\{)([^\\}]*)(\\})", "file \\2", txt)
    txt <- gsub("(\\\\email\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\url\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\var\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\env\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\option\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\command\\{)([^\\}]*)(\\})", "R command\\2", txt)
    txt <- gsub("(\\\\dfn\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\cite\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\acronym\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\code\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\kbd\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\preformatted\\{)([^\\}]*)(\\})", "\\2", txt)
    txt <- gsub("(\\\\emph\\{)([^\\}]*)(\\})", "<B>\\2</B>", txt)
    txt <- gsub("(\\\\strong\\{)([^\\}]*)(\\})", "<B>\\2</B>", txt)
    txt <- gsub("(\\\\bold\\{)([^\\}]*)(\\})", "<B>\\2</B>", txt)
    txt <- gsub("(\\\\sQuote\\{)([^\\}]*)(\\})", "'\\2'", txt)
    txt <- gsub("(\\\\dQuote\\{)([^\\}]*)(\\})", "\"\\2\"", txt)
    txt <- gsub("(^[[:space:]]+)", "", txt)
    txt <- gsub("([[:space:]]+$)", "", txt)
    txt <- gsub("(\\\\code\\{\\\\link\\[[^\\{]*\\{)([^\\}]*)(\\}\\})", "\\2", txt)
    txt <- sapply(txt, function(x) {
                    x <- paste(strwrap(x, 50, indent=8), collapse="\n\t*\t")
                    substring(x, 9)
           })
    txt
}

getClassDoc <- function(s4Class) {
        rPkg <- getClass(s4Class)@package
        db <- try( Rd_db(rPkg), TRUE)
        alias_index <- NA 
        if (! identical(class(db), "try-error")) {
            db <- mapply(paste, db, collapse="\n")
            db_alias_texts <- mapply(tools:::get_Rd_section, db, type="alias")
            alias_indices <- rep(1:length(db_alias_texts), lapply(db_alias_texts, length))
            i <- match(paste(s4Class, "-class", sep="", collapse=""), unlist(db_alias_texts))
            alias_index <- alias_indices[i]
        }
        
        class_doc <- new.env(parent=emptyenv(), hash=T)
        class_desc <- paste("It represents the S4 Class ", s4Class, 
            " in R package ", rPkg, ". ", sep="", collapse="")
        slotNames <- names(getSlots(s4Class))
        if(is.na(alias_index)) {
            lapply(slotNames, function(x) assign(x, "", class_doc))
            assign("ClassDesc_RWebServices", class_desc, class_doc)
        } else {
            db_description_text <- tools:::get_Rd_section(db[[alias_index]], type="description")
            class_desc <- paste(class_desc, parse_desc(db_description_text), 
                sep="\n\t*\t", collapse="") 
            assign("ClassDesc_RWebServices", class_desc, class_doc)
            slot_text <- tools:::get_Rd_section(db[[alias_index]], type="Slots", predefine=F) 
            if (length(slot_text)>0) {
                slot_text <- sub("^(\n| |\t)*\\\\describe\\{", "", slot_text)
                slot_text <- sub("}(\n| )*$", "", slot_text)
                slot_list <- unlist(strsplit(slot_text, "((^|\n) *\\\\item\\{)"))
                slot_list <- unlist(strsplit(slot_list, "(\\}(\n)*$)"))
                slot_list <- sub("\\}\\{", "@@@@", slot_list)
                slot_list <- strsplit(slot_list, "@@@@")
                slot_name <- unlist(mapply("[", slot_list, i=1))
                slot_desc <- unlist(mapply("[", slot_list, i=2))
                # some elements of slot_list may be a list of variable names seperated by comma
                slot_name <- strsplit(slot_name, "(,)")
                slot_desc <- rep(slot_desc, mapply(length, slot_name))
                slot_desc <- parse_desc(slot_desc)
                slot_name <- unlist(slot_name)
                slot_name <- gsub("( |:)", "", slot_name)
                slot_name <- gsub("(\\\\code\\{)([^\\}]*)(\\})", "\\2", slot_name)
            } else {
                slot_name <- character(0)
            }
            lapply(slotNames, function(thisSlot) {
                j <- match(thisSlot, slot_name)
                if (is.na(j))
                    desc <- ""
                else
                    desc <- slot_desc[[j]]
                assign(thisSlot, desc, envir=class_doc)
            })
        }
        class_doc
}          

getFunctionDoc <- function(rPkg, funcSig) {
        db <- try( Rd_db(rPkg), TRUE)
        alias_map <- new.env(parent=emptyenv(), hash=T)
        func_doc <- new.env(parent=emptyenv(), hash=T)
        if (! identical(class(db), "try-error")) {
            db <- mapply(paste, db, collapse="\n")
            db_alias_texts <- mapply(tools:::get_Rd_section, db, type="alias")
            db_description_texts <- mapply(tools:::get_Rd_section, db, type="description")
            db_value_texts <- mapply(tools:::get_Rd_section, db, type="value")
            db_arg_texts <- mapply(tools:::get_Rd_section, db, type="arguments")
            
            alias_indices <- rep(1:length(db_alias_texts), lapply(db_alias_texts, length))
            mapply( function(x, y) assign(x, y, envir=alias_map), 
                    unlist(db_alias_texts), 
                    alias_indices)
        }
            
        lapply(funcSig, 
            function(sig) {
                func_desc <- paste("Java wrapper for R function ", sig@funcName, 
                            ". ", sep="", collapse="") 
                if (exists(sig@funcName, alias_map)) {
                    i <- get(sig@funcName, alias_map)
                    arg_text <- gsub("\\\\dots", "...", db_arg_texts[[i]])
                    arg_list <- unlist(strsplit(arg_text, "((^|\n) *\\\\item\\{)"))
                    arg_list <- unlist(strsplit(arg_list, "(\\}(\n)*$)"))
                    arg_list <- sub("\\}\\{", "@@@@", arg_list)
                    arg_list <- strsplit(arg_list, "@@@@")
                    arg_name <- unlist(mapply("[", arg_list, i=1))
                    arg_desc <- unlist(mapply("[", arg_list, i=2))
                    # some elements of arg_list may be a list of variable names seperated by comma
                    arg_name <- strsplit(arg_name, "(,)")
                    arg_desc <- rep(arg_desc, mapply(length, arg_name))
                    arg_desc <- parse_desc(arg_desc)
                    arg_name <- unlist(arg_name)
                    arg_name <- gsub("( |:)", "", arg_name)
                    lapply(names(sig@args), function(thisArg) {
                        j <- match(thisArg, arg_name)
                        if (is.na(j))
                            desc <- ""
                        else
                            desc <- arg_desc[[j]]
                        
                        assign(paste(sig@funcName, thisArg, sep="_", collapse=""),
                            desc, envir=func_doc)
                        ""
                    })
                    assign(paste(sig@funcName, "return", sep="_", collapse=""), 
                        parse_desc(db_value_texts[[i]]), envir=func_doc)
                    func_desc <- paste(func_desc,
                                    parse_desc(db_description_texts[[i]]),
                                    sep="\n\t*\t", collapse="")
                } else {
                    lapply(names(sig@args), function(thisArg) {
                        assign(paste(sig@funcName, thisArg, sep="_", collapse=""),
                            "", envir=func_doc)
                        ""}
                    )
                    assign(paste(sig@funcName, "return", sep="_", collapse=""), 
                        "result", envir=func_doc)
                    
                }
                assign(sig@funcName, func_desc, envir=func_doc)
        })
        func_doc
}   

