
library(rJava);.jinit(classpath=NULL);
library(JavaGD);
try(library(TypeInfo));

.PrivateEnv<-new.env(parent = .GlobalEnv)
assign('q', q , env=.PrivateEnv);
assign('dir', dir , env=.PrivateEnv);
assign('ls', ls , env=.PrivateEnv);
assign('objects', objects , env=.PrivateEnv);

assign('help', help , env=.PrivateEnv);
assign('q', q , env=.PrivateEnv);

assign('setwd', setwd , env=.PrivateEnv);
assign('getwd', getwd , env=.PrivateEnv);

assign('dev.set', dev.set , env=.PrivateEnv);
assign('dev.off', dev.off , env=.PrivateEnv);
assign('dev.cur', dev.cur , env=.PrivateEnv);
assign('dev.list', dev.list , env=.PrivateEnv);
assign('dev.copy', dev.copy , env=.PrivateEnv);
assign('graphics.off', graphics.off , env=.PrivateEnv);

try(assign('win.graph', win.graph , env=.PrivateEnv),silent=TRUE);
try(assign('x11', x11 , env=.PrivateEnv),silent=TRUE);
try(assign('X11', X11 , env=.PrivateEnv),silent=TRUE);

assign('javapager', function (file, header = rep("", nfiles), title = "R Information", 
    delete.file = FALSE, pager = getOption("pager"), encoding = "") {    
	.jcall("server/RListener",,"pager", as.character(file), as.character(header), as.character(title),  as.character(delete.file) )
}, env=.PrivateEnv)

options(pager=.PrivateEnv$javapager)

load.lightpack <- function( s )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"loadLightPack", s ); 
	if (result[1]=='OK') { eval(parse("", text=result[2])); } else { eval(parse("", text=result[2]));  }
}

list.lightpack <- function()  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"listLightPacks", 'void' ); 
	if (result[1]=='OK') { eval(parse("", text=result[2])); } else { eval(parse("", text=result[2])); }
}

assign('notifyJavaListeners', function(parameters)  {
	.jcall( obj="server/RListener" , "V" ,"notifyJavaListeners", parameters )	
}, env=.PrivateEnv)


assign('nop', function()  {
	return(invisible(NULL))	
}, env=.PrivateEnv)


assign('dev.broadcast',  function () {
	temp_dev_list<-.PrivateEnv$dev.list();
 	if (!is.null(temp_dev_list)) {
		temp_dev_cur<-.PrivateEnv$dev.cur();	
		for (i in 1:length(temp_dev_list)) {
			if (temp_dev_list[i]!=temp_dev_cur) {	
				.PrivateEnv$dev.copy(which=temp_dev_list[i]);	
			}
			.PrivateEnv$dev.set(temp_dev_cur);
		}
	}
	return(invisible(NULL)); 
}, env=.PrivateEnv)


help <- function (topic, offline = FALSE, package = NULL, lib.loc = NULL, 
    verbose = getOption("verbose"), try.all.packages = getOption("help.try.all.packages"), 
    chmhelp = getOption("chmhelp"), htmlhelp = getOption("htmlhelp"), 
    pager = getOption("pager")) {
    
    if (missing(topic)) {
    	topicStr<-'';
    } else {
    	topicStr<-substitute(topic);
    }
    packStr<-substitute(package);
	

    .jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"help", as.character(topicStr)[1], as.character(packStr)[1] , as.character(lib.loc) );
    
    'Help is being displayed..'
 	   
}

q <- function (save = "default", status = 0, runLast = TRUE) {
	.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"q", as.character(save) , as.character(status) , as.character(runLast) );
	return(invisible(NULL)); 
}


assign('contains', function (str, vec) {
	result<-FALSE;
	for (i in 1:length(vec)) {
		if (vec[i]==str) {
			result<-TRUE;
			break;
		}
	}
	result
}, env=.PrivateEnv);

ls <-function (name, pos, envir, all.names = FALSE,  pattern) {

	if (missing(name)) {	
		if (missing(pos) && missing(envir)) {
			lsresult<-.PrivateEnv$ls(parent.frame(), all.names=all.names, pattern);
			e<-parent.frame();
		} else if (missing(pos)) {
			lsresult<-.PrivateEnv$ls(envir=envir, all.names=all.names, pattern);
			e<-envir;
		} else if (missing(envir)) {
			lsresult<-.PrivateEnv$ls(pos=pos, all.names=all.names, pattern);
			e<-as.environment(pos);
		} 
	} else {
		lsresult<-.PrivateEnv$ls(name, all.names=all.names, pattern)	;
		e<-as.environment(name);	
	}	
	
	if (identical(globalenv(), e)) {
		hiddenSymbols<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"forbiddenSymbols", 'void' );
		result<-character(0);
		for (i in 1:length(lsresult)) {
			if (.PrivateEnv$contains(lsresult[i], hiddenSymbols)==FALSE) result<-append(result, lsresult[i]);
		}
		result;
	} else {
		lsresult;
	}		
	
};

objects <-function (name, pos, envir, all.names = FALSE,  pattern) {

	if (missing(name)) {	
		if (missing(pos) && missing(envir)) {
			lsresult<-.PrivateEnv$objects(parent.frame(), all.names=all.names, pattern);
			e<-parent.frame();
		} else if (missing(pos)) {
			lsresult<-.PrivateEnv$objects(envir=envir, all.names=all.names, pattern);
			e<-envir;
		} else if (missing(envir)) {
			lsresult<-.PrivateEnv$objects(pos=pos, all.names=all.names, pattern);
			e<-as.environment(pos);
		} 
	} else {
		lsresult<-.PrivateEnv$objects(name, all.names=all.names, pattern)	;
		e<-as.environment(name);	
	}	
	
	if (identical(globalenv(), e)) {
		hiddenSymbols<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"forbiddenSymbols", 'void' );
		result<-character(0);
		for (i in 1:length(lsresult)) {
			if (.PrivateEnv$contains(lsresult[i], hiddenSymbols)==FALSE) result<-append(result, lsresult[i]);
		}
		result;
	} else {
		lsresult;
	}		
	
}

setwd <- function( dir )  {
	.PrivateEnv$setwd(dir);
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"reinitWorkingDirectory", getwd() );
	return(invisible(NULL)); 
}

#win.graph<-function(width = 7, height = 7, pointsize = 12, restoreConsole = FALSE){'win.graph not allowed in this context'}
#x11<-function(width = 7, height = 7, pointsize = 12, restoreConsole = FALSE){'x11 not allowed in this context'}
#X11<-function(width = 7, height = 7, pointsize = 12, restoreConsole = FALSE){'X11 not allowed in this context'}
#graphics.off<-function(){'graphics.off not allowed in this context'}
#dev.set<-function (which = dev.next()){'dev.set not allowed in this context'}
#dev.off<-function(which = dev.cur()){'dev.off not allowed in this context'}

setClusterPropertiesBiocep <- function( gprops )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"setClusterProperties", gprops ); 
	if (result[1]=='OK') { } else { eval(parse("", text=result[2])); }
}

makeClusterBiocep <- function( n=3, nodeName='N1' )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"makeCluster",.jlong(n) , nodeName ); 
	if (result[1]=='OK') { result[2] } else { eval(parse("", text=result[2])); '' }
}

clusterEvalQBiocep <- function( cl, exp )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterEvalQ", cl, exp );
	if (result[1]=='OK') {		
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

clusterExportBiocep <- function( cl, v )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterExport", cl, v );
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}

clusterApplyBiocep <- function( cl, v, fn)  {
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

stopClusterBiocep <- function( cl )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"stopCluster", cl );
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}

pythonExec <- function( exp )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"pythonExec", exp ); 
	if (result[1]=='OK') {		
		if (result[2]!="") { eval(parse("", text=result[2])) } else {return(invisible(NULL)); }
	} else {
		if (result[2]!="") {eval(parse("", text=result[2])) } else {return(invisible(NULL)); }
	}
}

pythonEval <- function( exp )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"pythonEval", exp ); 
	if (result[1]=='OK') {
		res<-.PrivateEnv$pythonEvalResult;
		rm('pythonEvalResult', envir=.PrivateEnv);	
		res;
	} else {
		if (result[2]!="") {eval(parse("", text=result[2])) } else {return(invisible(NULL)); }
	}
}



rlink.make <- function( mode='new' , params='' )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"makeRLink", mode , params); 
	if (result[1]=='OK') { print("RLink Creation Running in Background");result[2] } else { eval(parse("", text=result[2])); '' }
}

rlink.console <- function( cl, exp )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkConsole", cl, exp );
	if (result[1]=='OK') {		
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

rlink.get <- function( cl, exp , ato )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkGet", cl, exp, ato );
	if (result[1]=='OK') {		
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

rlink.put <- function( cl, exp , ato )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkPut", cl, exp, ato  );
	if (result[1]=='OK') {
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

rlink.show <- function( cl )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkShow", cl);
	if (result[1]=='OK') {
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

rlink.list  <- function()  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkList");
	if (result[1]=='OK') {
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

rlink.registry.list  <- function()  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkRegistryList");
	if (result[1]=='OK') {
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

rlink.release <- function( cl )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"RLinkRelease", cl);
	if (result[1]=='OK') {
		return(invisible(NULL));
	} else {
		eval(parse("", text=result[2]))
	}
}

cluster.make <- function( rlinks )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"makeRLinkCluster", rlinks ); 
	if (result[1]=='OK') { result[2] } else { eval(parse("", text=result[2])); '' }
}

cluster.eval <- function( cl, exp )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterEvalQ", cl, exp );
	if (result[1]=='OK') {		
		eval(parse("", text=result[2]))
	} else {
		eval(parse("", text=result[2]))
	}
}

cluster.export <- function( cl, v )  {  
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"clusterExport", cl, v );
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}

cluster.apply <- function( cl, v, fn)  {
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

cluster.stop <- function( cl )  {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"stopCluster", cl );
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}


cells.put <- function (  value , location, name='' ) {
	.PrivateEnv$spreadsheet.put.value<-value;
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"spreadsheetPut", location , name );
	rm (spreadsheet.put.value, envir=.PrivateEnv)
	if (result[1]=='OK') {
		return(invisible(NULL)); 
	} else {
		eval(parse("", text=result[2]))
	}
}

cells.get <- function ( range , type='numeric', name='' ) {
	result<-.jcall( obj="server/RListener" , "[Ljava/lang/String;" ,"spreadsheetGet",  range, type , name);
	if (result[1]=='OK') {
		return(.PrivateEnv$spreadsheet.get.result); 
	} else {
		eval(parse("", text=result[2]))
	}
}


