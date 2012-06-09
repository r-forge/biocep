

action.log <- function() {
	cat(.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"peekAttributeString", "log"));
}

action.assign <- function() {
 assign(.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"peekAttributeString", "ato"), 
			          unserialize(.jcall( obj="org/kchine/r/server/RRestListener" , "[B" ,"peekAttributeRawArray", "object")),
				  globalenv() ) ;
}

action.ptassign <- function() {
	ptid=.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"peekAttributeString", "ptid");
	n=.jcall( obj="org/kchine/r/server/RRestListener" , "I" ,"PTGetLength", ptid);						
	seq=new(.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"PTGetType", ptid));
	i=0;
	while (i<n) {
		seq[[i+1]]=unserialize(.jcall( obj="org/kchine/r/server/RRestListener" , "[B" ,"PTGetOutput", ptid, as.integer(i)));
		i=i+1;
	}	
	names=.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"PTGetNames", ptid);
	if (is.null(names) || length(names)==0) {
	} else {
		names(seq)<-names;
	} 				 	
	assign(.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"peekAttributeString", "ato"), seq, globalenv() );
	.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PTDispose", ptid) ;				
}

action.eval <- function() {
   eval(parse("", text= .jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"peekAttributeString", "expression") ));
}

action.browse <- function() {
  browseURL(.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"peekAttributeString", "url"));
}

handle.actions <- function(){	
	while (.jcall( obj="org/kchine/r/server/RRestListener" , "Z" ,"hasActions")) {		
		type=.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" , "peekAttributeString" , "type" );
		if (is.null(type)) {
		} else {		
			if (type=="RLLog") {		
				try ( action.log() );						
			} else if (type=="RLAssign") {				
				try ( action.assign() );				  				  
			} else if (type=="RLPTAssign") {		    
			    try ( action.ptassign() );		    				
			} else if (type=="RLEval") {						
				try (action.eval());						
			} else if (type=="RLBrowse") {
				try (action.browse());
			}
			.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"takeAction");
		}
	}	
}

java.tempname <- function() {
	stopTimer();
	try (result<-.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"newTemporaryName"), TRUE);
	startTimer();
	return (result);
}

java.showlog  <- function()  {  
	stopTimer();
	try (result<-.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"GetJavaLog"), TRUE);		
	startTimer();
	eval(parse("", text=result[1]));
}

java.resetlog  <- function()  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"ResetJavaLog"), TRUE);
	startTimer();
}

www.apply.internal <-function( rlid, ato, fun , seq , tnumber=10, asynch=TRUE) {
	n=length(seq);
	if (class(fun)=='function') fun=serialize(fun,NULL);
	ptid=.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"makePTask" , n);
	.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PTSetType" , ptid, as.character(class(seq)));
	
	if (is.null(names(seq))) {
	
	} else {
		.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PTSetNames" , ptid, as.character(names(seq)));
	}
	
	i=0;	
	while (i<n) {
		.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PTSetInput" , ptid, as.integer(i), serialize( seq[[i+1]] , NULL ) );
		i=i+1;
	}	 	
	.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"CLApply" , rlid, ptid, ato, fun, as.integer(tnumber), asynch );
}

www.apply <-function( rlid, ato, fun , seq , tnumber=10, asynch=TRUE) {
	stopTimer();
	try(www.apply.internal(rlid, ato, fun , seq , tnumber, asynch), TRUE);
	startTimer();
}

www.connect <- function( url, login, password, name, vmuid='', events=TRUE, log=TRUE,  conmode=0, device='gdprimary', spreadsheet='ssprimary', panel='ssprimary', asynch=TRUE)  {
	stopTimer();
	try (result<-.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"makeRL", url, login , password , name, vmuid, events, log , as.integer(conmode), device, spreadsheet, panel, asynch), TRUE); 
	startTimer();
	if ( length(result)>=1 && result[1]=='NOK') { eval(parse("", text=result[2])); '' }
	else { print("RLink Creation Running in Background"); result }  	
}

www.show <- function(rlid)  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLShow", as.character(rlid)), TRUE);
	startTimer();
}

www.setname <- function(rlid, name)  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLSetName", as.character(rlid), as.character(name) ), TRUE);
	startTimer();
}

www.synchup <- function(rlid)  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLSynchUp", as.character(rlid), as.character(search()) ), TRUE);
	startTimer();
}

www.synchdown <- function(rlid)  {
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLSynchDown", as.character(rlid) ), TRUE);
	startTimer();
}

www.enablelog <- function(rlid)  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLEnableLog", as.character(rlid)), TRUE);
	startTimer();
}

www.disablelog <- function(rlid)  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLDisableLog", as.character(rlid)), TRUE);
	startTimer();
}

www.engines <- function(includeFailed=FALSE)  {  
	stopTimer();
	try (result<-.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"ListEngines", as.logical(includeFailed)), TRUE);
	startTimer();
	return (result);
}

www.portals <- function(includeFailed=FALSE)  {  
	stopTimer();
	try (result<-.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"ListPortals", as.logical(includeFailed)), TRUE);
	startTimer();
	return(result);
}

www.console <- function(rlid, expression , asynch=TRUE )  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLConsole", as.character(rlid), as.character(expression) , as.logical(asynch)), TRUE);
	startTimer(); 
}

www.mathematica.console <- function(rlid, expression , asynch=TRUE )  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLMathematicaConsole", as.character(rlid), as.character(expression) , as.logical(asynch)), TRUE);
	startTimer();
}

www.mathematicag.console <- function(rlid, expression , asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLMathematicaGConsole", as.character(rlid), as.character(expression) , as.logical(asynch)), TRUE);
	startTimer();
}

www.eval <- function(rlid, expression , n=1, asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLEval", as.character(rlid), as.character(expression), as.integer(n) , as.logical(asynch)), TRUE);
	startTimer();
}

www.source <- function(rlid, file ,  wd=getwd(), asynch=TRUE )  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLSource", as.character(rlid), as.character(file), as.character(wd) , as.logical(asynch)), TRUE);
	startTimer();
}

www.stop <- function(rlid, asynch=TRUE )  {
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLStop", as.character(rlid), as.logical(asynch)), TRUE);
	startTimer();
}

www.dispose <- function(rlid, asynch=TRUE )  {  
        stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLDispose", as.character(rlid), as.logical(asynch)) , TRUE);
	startTimer();
}

www.pdispose <- function(plid, asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PLDispose", as.character(plid), as.logical(asynch)), TRUE);
	startTimer();
}

www.put <- function(rlid, var , ato='' , asynch=TRUE)  {
	if (ato=='') ato<-as.character(substitute(var));
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLPut", rlid, as.raw(serialize(var,NULL)) , as.character(ato), as.logical(asynch) ), TRUE);
	startTimer();
}

www.putlist.internal <- function(rlid, list , asynch=TRUE)  {
	n<-length(list);
	ptid<-.jcall( obj="org/kchine/r/server/RRestListener" , "Ljava/lang/String;" ,"makePTask" , n);
	.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PTSetNames" , ptid, list);	
	i=0;	
	while (i<n) {
		.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PTSetInput" , ptid, as.integer(i), serialize( eval(parse("",text=list[[i+1]])) , NULL ) );
		i=i+1;
	}
	.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLPutList", rlid, as.character(ptid), as.logical(asynch) );	
}

www.putlist<- function(rlid, list , asynch=TRUE)  {
	stopTimer();
	try (www.putlist(rlid, list , asynch), TRUE); 
	startTimer();
}

www.call <- function(rlid, ato, fun, ...)  {
	if (class(fun)=='function') fun=serialize(fun,NULL);   
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLCall", rlid, as.character(ato), fun, as.raw(serialize(list(...),NULL)), TRUE) , TRUE);
	startTimer();
}

www.call.synchronous <- function(rlid, ato, fun, ...)  {
	if (class(fun)=='function') fun=serialize(fun,NULL);  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLCall", rlid, as.character(ato), fun, as.raw(serialize(list(...),NULL)), FALSE ) , TRUE);
	startTimer();
}

www.cells.put <- function(rlid, var , location , asynch=TRUE)  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLCellsPut", rlid, as.raw(serialize(var,NULL)) , location, as.logical(asynch) ), TRUE);
	startTimer();
}

www.get <- function( rlid, exp , ato='', asynch=TRUE )  {  
        if (ato=='') ato<-exp;
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLGet", rlid, exp, ato , as.logical(asynch)), TRUE);
	startTimer(); 
}

www.cells.get <- function( rlid, range , ato, type='numeric', asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLCellsGet", rlid, range, as.character(ato) , as.character(type), as.logical(asynch)), TRUE);
	startTimer(); 
}

www.cells.undo <- function(rlid, asynch=TRUE)  {  
	stopTimer();
	try(.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLCellsUndo", rlid, as.logical(asynch) ), TRUE);
	startTimer(); 
}

www.cells.redo <- function(rlid, asynch=TRUE)  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLCellsRedo", rlid, as.logical(asynch) ), TRUE);
	startTimer(); 
}

www.upload <- function( rlid, file , wd=getwd() , asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLUpload", as.character(rlid), as.character(file) , as.character(wd), as.logical(asynch)) , TRUE);
	startTimer(); 
}

www.download <- function( rlid, file ,  wd=getwd() , asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLDownload", as.character(rlid), as.character(file) , as.character(wd), as.logical(asynch)), TRUE);
	startTimer(); 
}

www.image.put <- function(rlid  , asynch=TRUE) {
	fileName=java.tempname();
	
	tempd=dev.cur();
	jpeg(file=fileName)
	jpegd=dev.cur();
	dev.set(tempd);
	dev.copy(which=jpegd);
	dev.set(jpegd);
	dev.off();
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLImagePut", as.character(rlid), as.character(fileName), as.character(getwd()) , as.logical(asynch)), TRUE);
	startTimer();	
}

www.image.get <- function( rlid , format='jpeg', asynch=TRUE )  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLImageGet", as.character(rlid), as.character(format), as.character(getwd()), as.logical(asynch)), TRUE);
	startTimer(); 
}

www.proxy.set <- function ( host, port, login='', password='') {
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLProxySet", as.character(host), as.integer(port), as.character(login), as.character(password)), TRUE);
	startTimer(); 
}

www.proxy.detect <- function (purl='https://www.elastic-r.org/portal' ) {
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLProxyDetect", as.character(purl)), TRUE);
	startTimer(); 
}

www.proxy.show <- function () {
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLProxyShow"), TRUE);
	startTimer(); 
}

www.pconnect<- function( plogin , ppassword ,  purl='https://www.elastic-r.org/portal' ,  events=TRUE, log=TRUE, asynch=TRUE) {
	print("Portal Link Creation In Progress..");
	stopTimer();
	try (
	result<-.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"makePL", as.character(plogin), as.character(ppassword), 
	as.character(purl),  as.logical(events), as.logical(log) , as.logical(asynch)), TRUE);
	startTimer();
	if (length(result)==0) {
		return(invisible(NULL));
	} else {
		return (result);
	}
}

www.pshow <- function (plid) {	
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"PLShow", as.character(plid)), TRUE);
	startTimer(); 
}

engine.attrs.all <- function() {
    c("instanceId","instanceLabel","name","restfulUrl","login","password","vmuid","rVersion","javaVersion","osName","osVersion","osArch","memoryMin","memoryMax","volumeId","instanceType","availabilityZone","imageId","shareWith","anonymousShareWith","poolName","poolPassword");	
}

engine.attrs.basic <- function() {
   c("instanceId","instanceLabel","name","restfulUrl","login","password","rVersion","volumeId","instanceType");
}

www.plist <- function( plid , instance='*', name='*', pool='*',  private=TRUE, starting=TRUE, shared=TRUE, public=TRUE,  validate=FALSE, ping=FALSE, attrs=engine.attrs.basic())  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLPList", as.character(plid) , as.character(instance), as.character(name), as.character(pool) , 
	as.logical(private), as.logical(starting) , as.logical(shared), as.logical(public) ,
	as.logical(validate), as.logical(ping), as.character(attrs) ), TRUE);
	startTimer(); 
}

www.pget <- function( plid, 
	instance='*', name='*', pool='*' , 
	private=TRUE, shared=TRUE, public=TRUE,
	validate=FALSE, ping=FALSE, attrs='',
	events=TRUE, log=TRUE,  conmode=0,
	device='gdprimary', spreadsheet='ssprimary', panel='ssprimary',  asynch=TRUE)  {  
	
	stopTimer();
	
	try (result<-.jcall( obj="org/kchine/r/server/RRestListener" , "[Ljava/lang/String;" ,"RLPGet", 
	as.character(plid), 
	as.character(instance), as.character(name), as.character(pool), 
	as.logical(private), as.logical(shared), as.logical(public) ,
	as.logical(validate), as.logical(ping),
	as.logical(events), as.logical(log), as.integer(conmode),  
	as.character(device), as.character(spreadsheet), as.character(panel),	
	as.logical(asynch)
	), TRUE);
	
	startTimer(); 
	
	return (result); 
}

www.option <- function( key , value)  {  
	stopTimer();
	try (.jcall( obj="org/kchine/r/server/RRestListener" , "V" ,"RLOption", as.character(key), as.character(value)), TRUE);
	startTimer(); 
}

stopTimer <- function() {
	if (isStandalonePackage() && exists(".taskid", envir=.GlobalEnv)) {
		tcl("after", "cancel", get(".taskid", envir=.GlobalEnv));
	}
}

isStandalonePackage <-function() {
.jcall( obj="org/kchine/r/server/RRestListener" , "Z" ,"isStandalonePackage");
}

startTimer <- function(){	
   handle.actions();	
   if (isStandalonePackage()) assign('.taskid',tcl("after",1000,startTimer), envir=.GlobalEnv);
}  