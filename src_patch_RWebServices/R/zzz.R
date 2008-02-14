.onLoad <- function(libname, pkgname) {
  ## Converters
    require("methods")
    require("SJava")
  info_getJNIEnv <- getNativeSymbolInfo("getJNIEnv", PACKAGE="SJava")$address
  info_JavaStringArray <- getNativeSymbolInfo("JavaStringArray", PACKAGE="SJava")$address
  info_JavaObjectArray <- getNativeSymbolInfo("JavaObjectArray", PACKAGE="SJava")$address
  info_anonymousAssign <- getNativeSymbolInfo("anonymousAssign", PACKAGE="SJava")$address
  info_s_to_java_basic <- getNativeSymbolInfo("s_to_java_basic", PACKAGE="SJava")$address
  .Call("RWebServices_init", info_getJNIEnv, info_JavaStringArray, 
	info_JavaObjectArray, info_s_to_java_basic, info_anonymousAssign)

  ## Console and warnings
  .sinkScreenConfigure()
}
