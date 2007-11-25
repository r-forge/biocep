Project : biocep
Public Web Resources : http://www.ebi.ac.uk/microarray-srv/frontendapp/
Public Svn :  
   url : https://svn.ebi.ac.uk/ma/branches/biocep/
   login : ma-guest
   password : CN85JIZ5
         
presentations(conferences) : https://secure.bioconductor.org/BioC2007/labs.php#EBIServices
                             http://www.statistik.uni-dortmund.de/useR-2008/tutorials/
     
Local R Workbench Installer : http://www.ebi.ac.uk/microarray-srv/frontendapp/rworkbench.jnlp

Author : Karim Chine   kchine@ebi.ac.uk

Genreral Introduction and Use Cases
-----------------------------------

This is a new Unified Solution for R Integration released by the EMBL-EBI under the Apache License, Version 2.0. 
It includes Frameworks, tools and a Workbench for the following use cases:

- Generate java mappings for R Objects (Standard/S4).
- Generate java mappings for selected packages' functions (Generic/TypeInfoed).
- Use R and the R packages as a Java Toolkit via a Rich, High level, Object-Oriented API. 
- Deploy and use R as a remote component.
- Expose automatically R packages and the R API as JAX-WS stateless or statefull Web Services.
- Use R within a resource pooling infrastructure for scalable, web oriented, data analysis applications.
- Use the Remote Resources Pooling framework (RPF) to deploy and use distributed computational resources (non R based, native libraries with JNI support or java code) 
- Use R for parallel computing via  a Java API or Web Services or from within R (snow' like fucntions, prototype) 
- Use the R API from within an applet (book, use and release and R Instance via HTTP Tunneling) 
- Use R to generate Graphics on the fly for thin web clients
- Use R from within a Workbench including and advanced script editor,  a Spreadsheet View fully connected to R data and functions, an R Object Inspector,
   dockable Views, interactive R devices, clonable R Graphics..
- Use the Workbench from within a browser or via Java Web Start to access a pool of R Processes.
- Use the Workbench to control on demand any remote R Process.
- Provide Packaging for R Based Desktop applications that enables Web based, one click installation (Embedded R for Windows, detected R for other operating systems) 


Prerequisites
-------------

1/ 1.1/ JDK 5 or 6 installed  (Web Services Publishing requires Java SE 6)
   --> http://java.sun.com/javase/downloads/index_jdk5.jsp   
   --> http://java.sun.com/javase/downloads/index.jsp
      	
   1.2/ JAVA_HOME environment variable set to the installation root folder of the jdk
   1.3/ the PATH environment variable must include the jdk bin directory 

2/ 2.1/ ant 1.7.x installed 
   --> http://ant.apache.org/bindownload.cgi
   2.2/ ANT_HOME environment variable set to the installation root folder of ant
   2.3/ the PATH environment variable must include the ant bin directory

3/ 3.1/ R >= 2.5.x installed
   --> http://cran.r-project.org/ 
   3.2/ R_HOME environment variable set to the installation root folder of R
        
	this is mainly used to find the R dynamic library
    on windows, the R dynamic library must be found here : %R_HOME%/bin/
	on Mac OS X and Unix like Systems, the R dynamic library must be found here : $R_HOMe/lib/
	you can use .Library within an R session, your R_HOME, in most cases, is the value of .Library from which you remove the "library" string

 	 	
   3.3/ Bioconductor packages installed (for the demos only vsn is required)
   -->  source("http://bioconductor.org/biocLite.R"); biocLite();

   3.4/ TypeInfo package installed 
   -->  biocLite("TypeInfo")

   3.5/ rJava package installed 
   -->  biocLite("rJava") 

   
        if you encounter problems installing rJava, check the following :
      	on  Unix like systems : 
      	--> grep JAVA $R_HOME/Makeconf   
	    --> if R is not using the right jdk set JAVA_HOME to the path of the right jdk 
	    --> R CMD javareconf
	    --> biocLite("rJava") 


	by default, the JRI dynamic library is supposed to be under ${R_HOME}/library/rJava/jri	
	if you have installed rJava somewhere else change biocep/build.xml accordingly
	the line to change : 'property name="JRI_LIB_PATH" value="${env.R_HOME}/library/rJava/jri"/>'
        
   3.6/  JavaGD package installed 
   	--> biocLite("JavaGD")         
        
4/ apache-tomcat-5.5.x installed (=unzipped)
   --> http://tomcat.apache.org/download-55.cgi   
   4.1/ TOMCAT_HOME environment variable set to the installation root folder of tomcat
   
5/ apache derby 10.x installed (=unzipped)
   --> http://db.apache.org/derby/derby_downloads.html  
   --> DERBY_HOME environment variable set to the installation root folder of derby 	
   (if you are using java SE 6, derby is already embedded with the jdk, just set DERBY_HOME to $JAVA_HOME/db or %JAVA_HOME%/db for windows)
                 

   	
biocep build
------------

1/ cd biocep 

2/ compile all:
   --> ant clean
   --> ant compile
   (on windows only) --> ant download.pstools
   
   try the local version of the Virtual R Workbench 
   --> ant gui (the login/password are not checked and can be anything)
   

3/ run the different demos 
	
	all the ant targets can be launched either in a new terminal(best way to get direct log and to kill the process easily via Ctrl-C) 
	or using -Dspawn=true option to run in background. prefer the first option.
        on windows prefer start ant ... target

	----------- 
	-- demo0 --
	-----------
   			
	* direct use of the RServices interface (high level java interface for accessing R, Java Class Wrappers for all the R types, 
        Java Call to R functions in the R fashion, Java Proxies to R Object..)  
      
        --> cd demos/demo0
        --> ant demorun 

		
	----------- 
	-- demo1 --
	-----------
	   	   
   	* use of RServices interface to access R Pacakages Java Mappers (generated using the gen tool) 
      
    --> cd demos/demo1   
    --> ant demogen
	--> ant demorun
	   
	* R Graphics for Java,  Swing R console, Swing R Graphic device in Callback mode, Swing R Graphic device in pop mode		   

    --> ant demorun2 	        

	----------- 
   	-- demo2 --
   	-----------

        * remote access to RServices and to the mapped R packages   	   
        --> cd demos/demo2    
        --> ant demogen       		
        --> ant -Dspawn=true rmiregistry    or   new terminal (cd biocep/demos/demo2) --> ant rmiregistry       
        --> ant -Dspawn=true demoserver     or   new terminal (cd biocep/demos/demo2) --> ant demoserver        
        --> ant demorun
       
       * remote Graphics and console
        --> ant demorun2 


	the rmiregistry target allow to run/stop/dump an rmi registry 

	ant -Dport=1099 rmiregistry     
	runs the rmi registry on port 1099 (default port)
	
	ant -Dport=1099 -kill=true rmiregistry     
	stops the rmi registry

	ant -Dport=1099 -unbindall=true rmiregistry     
	cleans the rmi registry

	ant -Dport=1099 -show=true rmiregistry     
	dumps the registered RMI objects
	    
       
	----------- 
   	-- demo3 --
   	-----------
   	       
    	* demo of the usage of the implemented pooling framework to access to multiple remoted instances of RServices 
	  and R Packages 
        --> cd demos/demo3        
	--> ant demogen
	--> ant -Dspawn=true rmiregistry
	--> ant -Dspawn=true demoserver
	--> ant -Dspawn=true demoserver
	--> ant -Dspawn=true demoserver	
	--> ant demorun
	   
	----------- 
   	-- demo4 --
   	-----------   
	   
	* demo of tasks parallelization with the framework

       --> cd demos/demo4 		
       --> ant -Dspawn=true rmiregistry
       --> ant -Dspawn=true demoserver   
       --> ant -Dspawn=true demoserver   
       --> ant -Dspawn=true demoserver   
       --> ant demorun

	   
	----------- 
   	-- demo5 --
   	-----------   
	   
	* demo of the JAX-WS 2.0 web services publication 
       --> cd demos/demo5
        			
       --> ant demogen
       generate mapping.jar and ws.war 
       
       --> ant demodeploy
       deploy ws.war to tomcat
       	
       --> ant tomcat.startup
       run tomcat
       
       --> ant -Dspawn=true derbyrun
       run the database (registry)
       
       --> ant demodb
       create the naming database
                 
       --> ant -Dspawn=true demoserver
       create an Remote R Instance
       
       --> ant -Dspawn=true demoserver
       				   
       --> open a browser and type http://127.0.0.1:8080/ws/rGlobalEnvFunction?wsdl
       --> use eclipse or netbeans 5.5 to create java web clients for that URL         

	change demos/demo5/script/globals.r and demos/demo5/script/rjmap.xml to publish your own functions
	
	the statefull web services are accessible via the bunch of methods in the wsdl starting with stateful..
	
	here's an example of code demoing the use of statefull web services, you must generate the web service client artifacts before (from eclipse)


	public static void main(String[] args) throws Exception {

		RGlobalEnvFunctionWeb webService=new RGlobalEnvFunctionWebServiceLocator().getrGlobalEnvFunctionWebPort();
		String session=null;
		try {
		
			// log on and retrieve a session
			session=webService.logOn("", "test", "test", new HashMap());
			System.out.println("session:"+session);

			// very simple use of the statefull web service : console submit
			webService.statefulConsoleSubmit(session, "x=c(45,12)");
			System.out.println(webService.statefulConsoleSubmit(session, "x"));
			
			
			RChar c1=new RChar(); c1.setValue(new String[]{"aaaa"});			
			webService.statefulConsoleSubmit(session, "c2='bbbb'");
			
			RNamedArgument separator=new RNamedArgument();
			separator.setName("sep");
			separator.setRobject(new RChar(null,null,null,new String[]{"##"}));
			
			// Generic call to the R paste function, the first argument is an RChar (wrapper for character)
			// the second argument is a reference to existing data ("c2")
			// the third argument is a named arg (mapping the R named arg sep)
			RChar pasteResult=(RChar)webService.statefulCall(session, "paste", 
					new RObject[]{c1, new RObjectName(null,null,"c2"), separator});
			System.out.println("paste result :"+pasteResult.getValue()[0]);			
			
			
			
			webService.statefulConsoleSubmit(session, "library(vsn)");
			webService.statefulConsoleSubmit(session,"data(kidney)");
			
			// the ExpressionSet is retrieved from the R instance controlled by the statefull web service (R->java)
			ExpressionSet kidney=(ExpressionSet)webService.statefulEvalAndGetObject(session,"kidney");
			
                        // generic call to vsn2 with kidney as argument (java -> R), the result is retrived as a Vsn object (R->java)
			Vsn v=(Vsn)webService.statefulCall(session, "vsn2", new RObject[] { kidney  });
			Double[] hx=((RNumeric)v.getHx().getValue()).getValue();
			Integer[] dimHx=v.getHx().getDim();
			System.out.println(Arrays.toString(hx));
			
			
			// the result of vsn2 is pushed back to R and named "vsn_object"
			webService.statefulPutObjectAndAssignName(session, v  , "vsn_object");
			
			//  save "vsn_object" to a file on the local file system of the controlled R Instance
 			webService.statefulConsoleSubmit(session, "save(vsn_object,file='vsn_oject_file')");
			int fileSize=(int)webService.statefulGetWorkingDirectoryFileDescription(session, "vsn_oject_file").getSize();
			//  the file content is retrieved via the web service 
			byte[] file_buffer=webService.statefulReadWorkingDirectoryFileBlock(session, "vsn_oject_file", 0 , fileSize);
			RandomAccessFile f=new RandomAccessFile("my_vsn_object","rw");
			for (int i=0; i<file_buffer.length;++i) f.write(file_buffer[i]);
			f.close();
			
			
		} finally {
			// log off, free the R instance used
			webService.logOff(session);
		}

	}	   


	----------- 
   	-- demo6 --
   	-----------   
	   
	* demo of the call backs from R functions to Java (for reporting operation progress)
        --> cd demos/demo6 		    
        --> ant -Dspawn=true rmiregistry
	--> ant -Dspawn=true demoserver
	--> ant demo6run
	   
	   
	----------- 
   	-- demo7 --
   	-----------   

       * demo of the Distributed R Objects (S4 object having its attributes as proxies to data on different remote R instances)
       --> cd demos/demo7 		    
       --> ant demogen
       --> ant -Dspawn=true rmiregistry
       --> ant -Dspawn=true demoserver1
       --> ant -Dspawn=true demoserver2
       --> ant -Dspawn=true demoserver3
       --> ant demorun
	   
	   
	------------- 
   	-- demoRPF --
   	-------------  		
        * demo of the  use of the RPF framework indepently from R 		
	  generic Java Tasks executors (pooling framework plugged to the java tutorial)

        --> cd demos/demoRPF         	 
	--> ant -Dspawn=true derbyrun
        --> ant demodb         
        --> ant democompile         
        --> ant -Dspawn=true demoserver
        --> ant -Dspawn=true demoserver
		
	--> new terminal -> ant demorun
	--> new terminal -> ant demorun
	   
	   
	  	   
	--------------------------------------------------- 
   	-- Deployement of a Virtualized R infrastructure --
   	---------------------------------------------------   	
	
	--> cd  VirtualRWorkbench
	--> ant demogen
		generate mapping.jar and ws.war
		 
	--> ant webcompile
	 	generate frontendapp.war, this the web app with all the fucntional servlets: 
	 	/cmd : http tunneling servlet 
	 	/helpme : help browsing servlet 
	 	/config : tomcat configuration checking and setting servlet 
	 	/graphics on the fly graphics generation servlet 
	 	/jaws   : jsp enabling Java Web Start Access to the pooling infrastructure
	 	/desktopjaws  : jsp enabling Java Web Start installer for the local R Workbench)
	 	                the local R Workbench Web Installer creates an RWorkbench dir on your machine
	 	                under RWorkbench, the file  VRWorkbench.txt can be used to run local Workbench offline
	 	                on windows : rename VRWorkbench.txt to VRWorkbench.bat and double click on the .bat file
	 	                on Mac OS X : type 'source VRWorkbench.txt' on command line	 	                 
	 	  
	 	
	--> ant demodeploy
	deploy frontendapp.war and ws.war to tomcat
	
	--> ant -Dspawn=true tomcat.startup
	run tomcat
	
	--> ant -Dspawn=true derbyrun
	run the database (registry)
	
	--> ant demodb
	--> ant -Dspawn=true demotop
	    
	    this runs the Supervisor Gui
		notice in the pool panel that there is a pool named R (prefix RSERVANT_)
		and a node named N1 corresponding the local machine and to the prefix RSERVANT_
		you can create new servants (Remote R Instances) by right-clicking on the node and choosing 'New Servant'
		you can create/edit/remove nodes by right clicking on the Nodes Panel
		you can create/edit/remove Pools definitions by right clicking on the Pools Panel
		you can kill/unbind(remove DB entry)/unlock/.. Remote R instances by right-clicking on the Servants Panel
		you can use Debug in the Menu bar to export the Remote R Instance Log to a local Panel, to interact with
		it in sevral ways (log its RMI identity, open a console, disable the cleaning of the instance after use, 
		push and pop symbols to and from the instance..   
		 

	--> ant -Dspawn=true demoserver
	create a Remote R Instance
	
	--> ant -Dspawn=true demoserver
	--> ant -Dspawn=true demoserver

	--> new browser -> type the URL : http://127.0.0.1/frontendapp/ (applet)
				       or http://127.0.0.1/frontendapp/jaws (Java Web Start)   
	    you get the applet version or the Desktop version of the R Workbench (prefer the Java Web Start version)
	    you can also type : 'javaws http://127.0.0.1/frontendapp/jaws' on your command line  
	    
	     
	    log on (the login/password are not checked and can be anything) 
	    you have several option for logging :
	    * persistent workspace : your workspace is saved when you log off and restored when you logon again
	    * Create private R : instead of using the R Pool, an new Remote instance of R is created when you log on and destroyed when you log off 
	      to enable this feature, you should have launched ant -Dspawn=true demonode   
	    * wait until an R resource is available : self explanatory
	    * play demo : self explanatory
	    
	    after logging, a Remote R Instance is booked for you and will be released when you logoff or when your session expires 
	    
	    interact with the booked remote R instance via the different views:
	    ------------------------------------------------------------------
	    
	    - R console view : you enter your R expression in the text field at the bottom of view and type return
	    
	    - R File Workspace View : shows the content of your virtual workspace, the file you create appear there
	    and can be downloaded to your local disk (popup menu), the scripts you have on your local disk can be 
	    uploaded to the Remote R instance (popup menu), they appear then in the worksapce view and can be sourced
	    
        - R Graphics View : the Graphics events generated by R are popped by a dedicated Java Thread, Java 2D is used 
        to create a Bufferized Image with those events and the image is rendered in this view. Resizing fires an event 
        to the Remote R instance that  triggers recomputation of all the current Graphic events for the Virtual Device,
        they are popped again and displayed on real time.
        a popup menu allows to create a snapshot of the R Graphics View, to save the Graphics as png or jpeg
        the Graphics item in the menu bar (Mouse Tracker option) enable mouse tracking on the R Graphics (displays axis and the 
        real coordinates values of mouse position). you can click on the Graphics in several mouse positions and retrieve the 
        real coordinates values of those positions via the R function locator() [redefined to work in a virtualized context]
        
        - The Script Editor View (Tools/script editor) : this is jEdit, the popular open source editor that has been patched to work as a view within 
        the R workbench. it has syntaxe highliting enabled for all programming languages and some partial syntaxe highlighting 
        for R (activated for files with .r as extension). Two actions has been added (accessible via the Tool bar, and having the R Symbole in the upper right corner)
        the first toolbar button (run R) allows to upload the content of the editor to the Remote R Insantce and to source it. you should save before
        the second button (save to R) uploads to the Remote R Instance the script you are editing (you can see the result in the R Working Directory View)
        
        - The SpreadSheet View (Tools/ Spreadsheet Editor) : this is JSpreadSheet (open source project) that has been enhanced, patched and fully connected to R
        from within the spreadsheet you can : 
        *import any R Data (numeric, integer, character, logical, complex, factor, matrix, data frame ) via the toolbar button import from R (R+arrow towards the spreadsheet)
        *export selected cells to R and assign the content of the cells to an R variable via the toolbar button export to R (R+arrow towards R)
         you specify the type of export (numeric, integer, character, logical, complex, factor, data frame ) 
         for data frame, you should append to the column name the type of the column between parathesis ("weight(integer)", "mesure1(numeric)", "state(factor)", ..)         
        * Evaluate an R expression and use the current selection as argument (the toolbar button R evaluate (R+ruuning man))
         for example you can type "t(%%)" in the R Expression field. This transposes the selected cells matrix. The result is sent to the clipboard and you can paste on will
        * type in a cell an expression to evaluate and use any R fucntion, example in A4 type "=mean(A1:A3)" the content of A4 will be computed using R fucntion mean and the 
         cell value will be the mean of the vector A1:A3. all the R functions dealing with numeric vectors or matrixes can be used  
        * Copy/Paste to and from Excel
        * create as many SpreadSheet Views as needed and specify the suitable dimensions  
        
        - The Help View : can be opened via the menu bar (Help/Help Content) it allows you to browse the full R help (from the Remote R instance)
        you can also type help(cos) of ?cos in the R console to open this view and get help about the cos fucntion (for example)
        This a -very light weight- browser. It can be used from simple pages but shouldn't be confounded with Forefox. trying to acces too complex pages may freeze the hole 
        Workbench. you may want to copy the Url and paste it to your browser url field.
         
        - The Inspector View : allows to browse the internal structure of R Objects. This is enabled for all standard R Objects and for all the S4 R Objects for which a mapping has been
        generated
        
        - The console log viewer : allows to see the output of R on real time. this is usefull when an action (installing a package,..) takes time. The console doesn't display
        log before the evalutation is fully done.
        
        - The Java menu : allows to extract objects from R to Java and Save the Java serialization and the opposite as well.
          the Java Serialization can be used via the RServices API within Java applications
          
        - The docking Framework :  allows you to compose your views on will, drag&drop them, dock/undock them, expand them, reduce them ....
          see http://www.infonode.net/index.html?idw for further details
        
			    
	      
       	   
	On the Fly Generation of R Graphics
	------ 
	
		--> http://127.0.0.1:8080/frontendapp/graphics?height=500&width=400&expression=hist(rnorm(100))
            this URL returns a JPG generated on the fly by a servlet that borrows a remote R instance, evaluates the expression,
            pops the elementary graphics events from the remote R, creates a bufferized Image using Java 2D and returns it to the browser.
    	    expression can be any R graphical function    	    
    	    
    Resources Life Cycle Management
    ------
    for the majority of cases, the default startegy (once a booked Remote R Instance is released, it is "cleaned" and reused again) is convenient.
    
    mainly because of the memory fragmentation problem (the memory becomes like a cheese after several allocations deallocations and R looses of its capacity to deal 
    with big data) and beacuse of the Libraries  unloading problem (Loaded R Packages' Dynamic Libraries stay in memory), it may be required to implement a different startegy:
    once a booked R Resource is released, the corresponding process is killed (chronjob based of the administration API of RPF or the ant target node) and a new clean R process
    is created to replace it.
	to activate this strategy :
	--> ant -Dspawn=true demonode 
	--> edit the Node N1 to fix the min number of servants (Remote R instances)
	--> type the following url that changes the tomcat properties on the fly :http://127.0.0.1:8080/frontendapp/config?pools.dbmode.killused=true
	--> log on / log off and observe on the Supervisor Gui what happens		
	to restrore the by default startegy use http://127.0.0.1:8080/frontendapp/config?pools.dbmode.killused=false


	Create and Control Several Remote R Instances from within R / Parallel computation
	----------------
	(prototype)
		
		same syntax as the snow package 
	    makeCluster    
	    clusterEvalQ
	    clusterExport
	    clusterApply
	    stopCluster     
	    setClusterProperties
	
		run the node manager (used to create and kill the Remote R Instances) --> ant -Dspawn=true demonode
		
		R Console >	list.lightpack()
		lists all the "light weight packages" : r scripts that you would have put under src/monoscriptpackage before compiling
			
		> logon
		Logged on as guest
		> load.lightpack('rmisnow')
		[1] rmisnow loaded successfully
		> cl<-makeCluster(3)
		-- creates 3 Remote R Instances and 'assign' them to the cluster cl
		
		> l<-list(x=as.numeric(c(1:15)), y=as.numeric(c(1:300)))
		> cos_l<-clusterApply(cl,l,'cos')
		-- use the cluster to apply cos to the list l, the compuations for l$x and l$y are done in parallel by different Remote R Instances	
		
		
		> x<-rnorm(100)
		> clusterExport(cl,'x')
		> clusterEvalQ(cl,'ls()')
		[1] worker<RSERVANT_3>:
		[1] [1] 'x'
		[1] 
		[1] worker<RSERVANT_4>:
		[1] [1] 'x'
		[1] 
		[1] worker<RSERVANT_2>:
		[1] [1] 'x'
		[1] 
		[1]
		-- export the variable x to all the elements of the cluster 
		-- evalute the expression 'ls()' in each element of the cluster
		
		> stopCluster(cl)  
		-- kill the Remote R Instances that have been created 

	LSF
	---
	
	see biocep/VirtualRWorkbench/lsf
	
	typical command for creating a Remote R Instance on an LSF cluster (Field Create Command of your node):
	bsub -J R${PROCESS_COUNTER} ant ${OPTIONS} -f ${INSTALL_DIR}/VirtualRWorkbench/build.xml -Dnode=N1 -Dattr.lsf.processid=R${PROCESS_COUNTER} demoserver 
	 
	typical command for killing a Remote R Instance on an LSF cluster (Field Kill Command of your node) :
	bkill -J R${attr.lsf.processid}
	
	
	Ant Scrpits Overview
	-------------------- 
	
	the ant scripts (biocep/build.xml and biocep/VirtualRWorkbench/build.xml) contain targets for every required action
	
	The main properties to set in biocep/VirtualRWorkbench/build.xml are
	 
	<property name="db.host" value="127.0.0.1" />
	<property name="db.port" value="1527" />			
	<property name="tomcat.host" value="127.0.0.1" />
	<property name="tomcat.port" value="8080" />
	
	the first two are the hostname/port of the derby database, the last ones are the hostname/port of the tomcat server 
	if you change the tomcat port, this will affect all the targets accessing the server but you must change the Connector port 
	in $TOMCAT_HOME/conf/server.xml as well 
	 
	once these values are set you can use the following targets :	
	
	ant derbyrun 
	ant derbystop
	ant demodb
	
	to startup / shutdown / recreate the derby database
	
	
	ant tomcat.startup
	ant tomcat.shutdown
	
	to startup / shutdown the tomcat server
	
	
	ant demogen
	ant webcompile
	ant demodeploy
	
	to generate the mapping jar and the web services war, generate the front end application for the Http Tunneling, deploy the war files to tomcat  	

	ant demoserver 
	to create a server belonging to the pool R (node N1) , tomcat must be running with the wars deployed, the database must have been created and the database server running 

	
	ant -Dname=TOTO simpleserver
	to create a self managed server named TOTO , tomcat must be running with the wars deployed, the database must have been created and the database server running
		

	ant demotop
	to run the supervisor that allows to :
	- create, edit and remove Pools definitions
	- create, edit and remove Nodes definitions, create servers for selected Nodes
	- manage ( kill, debug, unbind ..)  the servers  referenced in the database (DB Registry)
		
 		 
	ant demoapplet
	to launch the Virtual R Workbench in http mode and connect to the tomcat on tomcat.host:tomcat.port

	
	ant -Dmode=http -Durl=http://ant17.ebi.ac.uk:8080/frontendapp/cmd  demoapplet
	to launch the Virtual R Workbench in http mode and connect to the ant17.ebi.ac.uk:8080
        to connect to a named R Instance you launched via ant -Dname=TOTO simpleserver, use as login your_login@@TOTO

	 	 
	ant -Dmode=local demoapplet
	to launch the Virtual R Workbench in local mode (using the local R sitting under R_HOME)
	 
	ant -Dmode=rmi -Dname=TOTO demoapplet
	to launch the Virtual R Workbench in RMI mode and control the R server which name is TOTO (database on db.host:db.port)
	
	ant demorun
	launch a mutilthreaded process for parallel computation using the framework
	
	ant demorun2
	demo of the http tunneling Api for accessing R. The stateful web services are based on this API, the Virtual R Workbench as well (in http mode)
	
	
	

