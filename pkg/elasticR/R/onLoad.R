.onLoad <- function(libname, pkgname) {
  .jpackage(pkgname, lib.loc = libname)  
  
  if (isStandalonePackage()) {
	print ("Standalone Package");
	  .jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"init", getwd());   
	  library(tcltk);   
	  startTimer();
  } else {
	print ("Bound package");
  }
}

