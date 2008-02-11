<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title></title>
	</head>
	
	<% String defaultHeight=request.getHeader("User-Agent").contains("MSIE")?"99%":"740";  %>
	<% String width=request.getParameter("width")==null ? "99%" : request.getParameter("width");  %>
	<% String height=request.getParameter("height")==null ? defaultHeight : request.getParameter("height");  %>	
	<% String autologon=request.getParameter("autologon")==null ? "true" : request.getParameter("autologon");  %>	
	<% String nopool=request.getParameter("nopool")==null ? "false" : request.getParameter("nopool");  %>
	<% String save=request.getParameter("save")==null ? "false" : request.getParameter("save");  %>
	<% String wait=request.getParameter("wait")==null ? "false" : request.getParameter("wait");  %>
	<% String demo=request.getParameter("demo")==null ? "false" : request.getParameter("demo");  %>
	<% String lf=request.getParameter("lf")==null ? "0" : request.getParameter("lf");  %>
	<% String login=request.getParameter("login")==null ? "guest" : request.getParameter("login");  %>
	<% String mode=request.getParameter("mode")==null ? "http" : request.getParameter("mode");  %>
	<% String debug=request.getParameter("debug")==null ? "false" : request.getParameter("debug");  %>
	
	<body>
		
		<object
			classid = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
			codebase = "http://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=5,0,0,3"
			WIDTH = "<%=width%>" 
			HEIGHT = "<%=height%>" >
			<PARAM NAME = CODE VALUE = "graphics.rmi.GDApplet.class" >
			<PARAM NAME = ARCHIVE VALUE = "appletlibs/RJB.jar,appletlibs/commons-httpclient-3.1-rc1.jar,appletlibs/commons-codec-1.3.jar,appletlibs/commons-logging-1.1.jar,appletlibs/log4j-1.2.14.jar,appletlibs/mapping.jar,appletlibs/idw-gpl.jar,appletlibs/jeditmodes.jar,appletlibs/jedit.jar,appletlibs/pf-joi-full.jar,appletlibs/OpenXLS.jar,appletlibs/webserver.jar,appletlibs/htmlparser.jar,appletlibs/servlet-api.jar,appletlibs/JRI.jar" >
			<param name = "type" value = "application/x-java-applet;version=1.5">
			<param name = "scriptable" value = "false">
			<param name = "autologon" value = "<%=autologon%>">
			<param name = "nopool" value = "<%=nopool%>">
			<param name = "save" value = "<%=save%>">
			<param name = "wait" value = "<%=wait%>">
			<param name = "demo" value = "<%=demo%>">
			<param name = "lf" value = "<%=lf%>">
			<param name = "mode" value = "<%=mode%>">
			<param name = "debug" value = "<%=debug%>">
			<param name = "login" value = "<%=login%>">
		
			<comment>
				<embed type = "application/x-java-applet;version=1.5" 
					
					CODE = "graphics.rmi.GDApplet.class" 
					ARCHIVE = "appletlibs/RJB.jar,appletlibs/commons-httpclient-3.1-rc1.jar,appletlibs/commons-codec-1.3.jar,appletlibs/commons-logging-1.1.jar,appletlibs/log4j-1.2.14.jar,appletlibs/mapping.jar,appletlibs/idw-gpl.jar,appletlibs/jeditmodes.jar,appletlibs/jedit.jar,appletlibs/pf-joi-full.jar,appletlibs/OpenXLS.jar,appletlibs/webserver.jar,appletlibs/htmlparser.jar,appletlibs/servlet-api.jar,appletlibs/JRI.jar" 					
					WIDTH = "<%=width%>" 
					HEIGHT = "<%=height%>" 
					scriptable = false 
					pluginspage = "http://java.sun.com/products/plugin/index.html#download"
					autologon = "<%=autologon%>" 
					nopool = "<%=nopool%>"
					save = "<%=save%>"
					wait = "<%=wait%>"
					demo = "<%=demo%>"
					lf = "<%=lf%>"
					mode = "<%=mode%>"
					debug = "<%=debug%>"
					login = "<%=login%>"									
					>
					
					<noembed>
					</noembed>
					
				</embed>
			</comment>
			
		</object>
		
    </body>
	
</html>