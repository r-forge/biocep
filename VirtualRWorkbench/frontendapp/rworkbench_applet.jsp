<% response.setContentType("text/html"); %>

<% String mode=request.getParameter("mode")==null ? "local" : request.getParameter("mode");  %>
<% String rmi_mode=request.getParameter("rmi_mode")==null ? "" : request.getParameter("rmi_mode");  %>
<% String debug=request.getParameter("debug")==null ? "false" : request.getParameter("debug");  %>
<% String autologon=request.getParameter("autologon")==null ? "true" : request.getParameter("autologon");  %>
<% String nopool=request.getParameter("nopool")==null ? "" : request.getParameter("nopool");  %>
<% String save=request.getParameter("save")==null ? "" : request.getParameter("save");  %>
<% String lf=request.getParameter("lf")==null ? "0" : request.getParameter("lf");  %>
<% String stub=request.getParameter("stub")==null ? "" : request.getParameter("stub");  %>
<% String name=request.getParameter("name")==null ? "" : request.getParameter("name");  %>
<% String privatename=request.getParameter("privatename")==null ? "" : request.getParameter("privatename");  %>
<% String noconfirmation=request.getParameter("noconfirmation")==null ? "" : request.getParameter("noconfirmation");  %>
<% String selfish=request.getParameter("selfish")==null ?"" : request.getParameter("selfish");  %>
<% String url=request.getParameter("url")==null ?"" : request.getParameter("url");  %>
<% String login=request.getParameter("login")==null ? "" : request.getParameter("login");  %>
<% String password=request.getParameter("password")==null ? "" : request.getParameter("password");  %>
<% String wait=request.getParameter("wait")==null ? "" : request.getParameter("wait");  %>
<% String demo=request.getParameter("demo")==null ? "" : request.getParameter("demo");  %>

<html>
<head>


	</head>
	
	
	
	<body><center>

<!--[if !IE]> Firefox and others will use outer object -->
<object align="center" 
	height=100% 
	width=100% 
	classid="java:org.kchine.r.workbench.WorkbenchApplet"   
	archive="appletlibs/RJB.jar,appletlibs/commons-httpclient-3.1-rc1.jar,appletlibs/commons-codec-1.3.jar,appletlibs/commons-logging-1.1.1.jar,appletlibs/log4j-1.2.15.jar,appletlibs/mapping.jar,appletlibs/idw-gpl.jar,appletlibs/PDFRenderer.jar,appletlibs/pf-joi-full.jar,appletlibs/OpenXLS.jar,appletlibs/jsch-0.1.40.jar,appletlibs/jetty-6.1.11.jar,appletlibs/jetty-util-6.1.11.jar,appletlibs/jetty-client-6.1.11.jar,appletlibs/servlet-api-2.5-6.1.11.jar,appletlibs/activation.jar,appletlibs/mail.jar,appletlibs/htmlparser.jar,appletlibs/JRI.jar,appletlibs/derbyclient.jar,appletlibs/ganymed-ssh2.jar,appletlibs/batik-anim.jar,appletlibs/batik-awt-util.jar,appletlibs/batik-bridge.jar,appletlibs/batik-css.jar,appletlibs/batik-dom.jar,appletlibs/batik-svggen.jar,appletlibs/batik-ext.jar,appletlibs/batik-extension.jar,appletlibs/batik-gui-util.jar,appletlibs/batik-gvt.jar,appletlibs/batik-parser.jar,appletlibs/batik-script.jar,appletlibs/batik-svg-dom.jar,appletlibs/batik-swing.jar,appletlibs/batik-util.jar,appletlibs/batik-xml.jar,appletlibs/js.jar,appletlibs/xml-apis.jar,appletlibs/xml-apis-ext.jar,appletlibs/edtftpj.jar,appletlibs/jython.jar,appletlibs/swing-layout-1.0.3.jar,appletlibs/groovy-all-1.5.4.jar,appletlibs/freemindbrowser.jar"
	 
	type = "application/x-java-applet;version=1.5" 
	scriptable = false 
	pluginspage = "http://java.sun.com/products/plugin/index.html#download"
	
		
	mode="<%=mode%>"
	rmi_mode="<%=rmi_mode%>"
	debug="<%=debug%>"
	autologon="<%=autologon%>"
	nopool="<%=nopool%>"
	save="<%=save%>"
	lf="<%=lf%>"
	stub="<%=stub%>"
	name="<%=name%>"
	privatename="<%=privatename%>"
	noconfirmation="<%=noconfirmation%>"
	selfish="<%=selfish%>"
	url="<%=url%>"
	login="<%=login%>"
	password="<%=password%>"
	wait="<%=wait%>"
	demo="<%=demo%>"
	
	>	
	
<!--<![endif]-->
   <!-- MSIE (Microsoft Internet Explorer) will use inner object --> 
   <object align="center" height=100% width=100% classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93">
	 
	 <param name="archive" value="appletlibs/RJB.jar,appletlibs/commons-httpclient-3.1-rc1.jar,appletlibs/commons-codec-1.3.jar,appletlibs/commons-logging-1.1.1.jar,appletlibs/log4j-1.2.15.jar,appletlibs/mapping.jar,appletlibs/idw-gpl.jar,appletlibs/PDFRenderer.jar,appletlibs/pf-joi-full.jar,appletlibs/OpenXLS.jar,appletlibs/jsch-0.1.40.jar,appletlibs/jetty-6.1.11.jar,appletlibs/jetty-util-6.1.11.jar,appletlibs/jetty-client-6.1.11.jar,appletlibs/servlet-api-2.5-6.1.11.jar,appletlibs/activation.jar,appletlibs/mail.jar,appletlibs/htmlparser.jar,appletlibs/JRI.jar,appletlibs/derbyclient.jar,appletlibs/ganymed-ssh2.jar,appletlibs/batik-anim.jar,appletlibs/batik-awt-util.jar,appletlibs/batik-bridge.jar,appletlibs/batik-css.jar,appletlibs/batik-dom.jar,appletlibs/batik-svggen.jar,appletlibs/batik-ext.jar,appletlibs/batik-extension.jar,appletlibs/batik-gui-util.jar,appletlibs/batik-gvt.jar,appletlibs/batik-parser.jar,appletlibs/batik-script.jar,appletlibs/batik-svg-dom.jar,appletlibs/batik-swing.jar,appletlibs/batik-util.jar,appletlibs/batik-xml.jar,appletlibs/js.jar,appletlibs/xml-apis.jar,appletlibs/xml-apis-ext.jar,appletlibs/edtftpj.jar,appletlibs/jython.jar,appletlibs/swing-layout-1.0.3.jar,appletlibs/groovy-all-1.5.4.jar,appletlibs/freemindbrowser.jar">
	 <param name="code" value="org.kchine.r.workbench.WorkbenchApplet">
	 <param name = "type" value = "application/x-java-applet;version=1.5">
	 <param name = "scriptable" value = "false">
	 
	<param name = "mode" value ="<%=mode%>"
	<param name = "rmi_mode" value ="<%=rmi_mode%>"
	<param name = "debug" value ="<%=debug%>"
	<param name = "autologon" value ="<%=autologon%>"
	<param name = "nopool" value ="<%=nopool%>"
	<param name = "save" value ="<%=save%>"
	<param name = "lf" value ="<%=lf%>"
	<param name = "stub" value ="<%=stub%>"
	<param name = "name" value ="<%=name%>"
	<param name = "privatename" value ="<%=privatename%>"
	<param name = "noconfirmation" value ="<%=noconfirmation%>"
	<param name = "selfish" value ="<%=selfish%>"
	<param name = "url" value ="<%=url%>"
	<param name = "login" value ="<%=login%>"
	<param name = "password" value ="<%=password%>"
	<param name = "wait" value ="<%=wait%>"
	<param name = "demo" value ="<%=demo%>"

   </object>
<!--[if !IE]> close outer object -->
		 
		 
</object>
<!--<![endif]-->
</div>
</center>
</body>
</html>