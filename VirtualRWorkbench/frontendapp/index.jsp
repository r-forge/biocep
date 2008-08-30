<html>
<head>


</head>



<% String width=request.getParameter("width")==null ? "99%" : request.getParameter("width");  %>
<% String height=request.getParameter("height")==null ? "99%" : request.getParameter("height");  %>
<% String autologon=request.getParameter("autologon")==null ? "true" : request.getParameter("autologon");  %>
<% String nopool=request.getParameter("nopool")==null ? "true" : request.getParameter("nopool");  %>
<% String save=request.getParameter("save")==null ? "false" : request.getParameter("save");  %>
<% String wait=request.getParameter("wait")==null ? "false" : request.getParameter("wait");  %>
<% String demo=request.getParameter("demo")==null ? "false" : request.getParameter("demo");  %>
<% String lf=request.getParameter("lf")==null ? "0" : request.getParameter("lf");  %>
<% String login=request.getParameter("login")==null ? "guest" : request.getParameter("login");  %>
<% String mode=request.getParameter("mode")==null ? "http" : request.getParameter("mode");  %>
<% String debug=request.getParameter("debug")==null ? "false" : request.getParameter("debug");  %>
<% String url=request.getParameter("url")==null ? "http://xen-ngs001.oerc.ox.ac.uk:8000/rvirtual/cmd" : request.getParameter("url");  %>
<% String privatename=request.getParameter("privatename")==null ? "" : request.getParameter("privatename");  %>

<body>
<center><!--[if !IE]> Firefox and others will use outer object -->
<object align="center" height=98% width=98%
	classid="java:graphics.rmi.GDApplet"
	archive="appletlibs/RJB.jar,appletlibs/commons-httpclient-3.1-rc1.jar,appletlibs/commons-codec-1.3.jar,appletlibs/commons-logging-1.1.1.jar,appletlibs/log4j-1.2.15.jar,appletlibs/mapping.jar,appletlibs/idw-gpl.jar,appletlibs/PDFRenderer.jar,appletlibs/jeditmodes.jar,appletlibs/jedit.jar,appletlibs/pf-joi-full.jar,appletlibs/OpenXLS.jar,appletlibs/servlet-api-2.5-6.1.8.jar,appletlibs/jsp-api-2.1.jar,appletlibs/jsp-2.1.jar,appletlibs/jetty-6.1.8.jar,appletlibs/jetty-util-6.1.8.jar,appletlibs/activation.jar,appletlibs/mail.jar,appletlibs/htmlparser.jar,appletlibs/JRI.jar,appletlibs/derbyclient.jar,appletlibs/ganymed-ssh2.jar,appletlibs/batik-anim.jar,appletlibs/batik-awt-util.jar,appletlibs/batik-bridge.jar,appletlibs/batik-css.jar,appletlibs/batik-dom.jar,appletlibs/batik-svggen.jar,appletlibs/batik-ext.jar,appletlibs/batik-extension.jar,appletlibs/batik-gui-util.jar,appletlibs/batik-gvt.jar,appletlibs/batik-parser.jar,appletlibs/batik-script.jar,appletlibs/batik-svg-dom.jar,appletlibs/batik-swing.jar,appletlibs/batik-util.jar,appletlibs/batik-xml.jar,appletlibs/js.jar,appletlibs/xml-apis.jar,appletlibs/xml-apis-ext.jar,appletlibs/edtftpj.jar,appletlibs/jython.jar,appletlibs/swing-layout-1.0.3.jar,appletlibs/groovy-all-1.5.4.jar,appletlibs/freemindbrowser.jar,appletlibs/biocep-doc.jar"
	type="application/x-java-applet;version=1.5" scriptable=false
	pluginspage="http://java.sun.com/products/plugin/index.html#download"
	autologon="<%=autologon%>" nopool="<%=nopool%>" save="<%=save%>"
	wait="<%=wait%>" demo="<%=demo%>" lf="<%=lf%>" mode="<%=mode%>"
	url="<%=url%>" debug="<%=debug%>" login="<%=login%>"
	privatename="<%=privatename%>"> <!--<![endif]--> <!-- MSIE (Microsoft Internet Explorer) will use inner object -->
	<object align="center" height=98% width=98%
		classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93">
		<param name="archive"
			value="appletlibs/RJB.jar,appletlibs/commons-httpclient-3.1-rc1.jar,appletlibs/commons-codec-1.3.jar,appletlibs/commons-logging-1.1.1.jar,appletlibs/log4j-1.2.15.jar,appletlibs/mapping.jar,appletlibs/idw-gpl.jar,appletlibs/PDFRenderer.jar,appletlibs/jeditmodes.jar,appletlibs/jedit.jar,appletlibs/pf-joi-full.jar,appletlibs/OpenXLS.jar,appletlibs/servlet-api-2.5-6.1.8.jar,appletlibs/jsp-api-2.1.jar,appletlibs/jsp-2.1.jar,appletlibs/jetty-6.1.8.jar,appletlibs/jetty-util-6.1.8.jar,appletlibs/activation.jar,appletlibs/mail.jar,appletlibs/htmlparser.jar,appletlibs/JRI.jar,appletlibs/derbyclient.jar,appletlibs/ganymed-ssh2.jar,appletlibs/batik-anim.jar,appletlibs/batik-awt-util.jar,appletlibs/batik-bridge.jar,appletlibs/batik-css.jar,appletlibs/batik-dom.jar,appletlibs/batik-svggen.jar,appletlibs/batik-ext.jar,appletlibs/batik-extension.jar,appletlibs/batik-gui-util.jar,appletlibs/batik-gvt.jar,appletlibs/batik-parser.jar,appletlibs/batik-script.jar,appletlibs/batik-svg-dom.jar,appletlibs/batik-swing.jar,appletlibs/batik-util.jar,appletlibs/batik-xml.jar,appletlibs/js.jar,appletlibs/xml-apis.jar,appletlibs/xml-apis-ext.jar,appletlibs/edtftpj.jar,appletlibs/jython.jar,appletlibs/swing-layout-1.0.3.jar,appletlibs/groovy-all-1.5.4.jar,appletlibs/freemindbrowser.jar,appletlibs/biocep-doc.jar">
		<param name="code" value="graphics.rmi.GDApplet">
		<param name="type" value="application/x-java-applet;version=1.5">
		<param name="scriptable" value="false">
		<param name="autologon" value="<%=autologon%>">
		<param name="nopool" value="<%=nopool%>">
		<param name="save" value="<%=save%>">
		<param name="wait" value="<%=wait%>">
		<param name="demo" value="<%=demo%>">
		<param name="lf" value="<%=lf%>">
		<param name="mode" value="<%=mode%>">
		<param name="url" value="<%=url%>">
		<param name="debug" value="<%=debug%>">
		<param name="login" value="<%=login%>">
		<param name="privatename" value="<%=privatename%>">

	</object> <!--[if !IE]> close outer object --> </object> <!--<![endif]-->
</div>
</center>
</body>




</html>