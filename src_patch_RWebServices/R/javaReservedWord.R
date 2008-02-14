jReservedWord <- local({
    words <- c("abstract", "boolean", "break", "byte", "byvalue",
               "case", "cast", "catch", "char", "class", "const",
               "continue", "default", "do", "double", "else",
               "extends", "final", "finally", "float", "for",
               "future", "generic", "goto", "if", "implements",
               "import", "inner", "instanceof", "int", "interface",
               "long", "native", "new", "null", "operator", "outer",
               "package", "private", "protected", "public", "rest",
               "return", "short", "static", "super", "switch",
               "synchronized", "this", "throw", "throws", "transient",
               "try", "var", "void", "volatile", "while")
    wwords <- sub("^(\\w)", "\\1\\1", words)
    env <- new.env()
    mapply(assign, words, wwords, MoreArgs=list(envir=env))
    env
})
