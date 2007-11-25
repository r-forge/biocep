setClusterProperties <- function( gprops )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"setClusterProperties", gprops ); 
	if (result[1]=='OK') { } else { eval(parse("", text=result[2])); }
}

makeCluster <- function( n=3, nodeName='N1' )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"makeCluster",.jlong(n) , nodeName ); 
	if (result[1]=='OK') { result[2] } else { eval(parse("", text=result[2])); '' }
}

clusterEvalQ <- function( cl, exp )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterEvalQ", cl, exp );
	if (result[1]=='OK') {
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

clusterExport <- function( cl, v )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterExport", cl, v );
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}

clusterApply <- function( cl, v, fn)  {
    assign('clusterApplyVar', v , env=.PrivateEnv);
    #assign('clusterApplyFunction', v , env=.PrivateEnv);
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterApply", cl, ".PrivateEnv$clusterApplyVar" , fn );
	rm('clusterApplyVar', envir=.PrivateEnv);
	#rm('clusterApplyFunction', envir=.PrivateEnv);	 
	if (result[1]=='OK') { 
		res<-.PrivateEnv$clusterApplyResult;
		rm('clusterApplyResult', envir=.PrivateEnv);	
		res;
	} else {
		eval(parse("", text=result[2]))
	}
}

stopCluster <- function( cl )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"stopCluster", cl );
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}
