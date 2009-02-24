<% response.setContentType("application/x-java-jnlp-file"); %>
<?xml version="1.0" encoding="UTF-8"?>

<% java.net.URL thisUrl=new java.net.URL(request.getRequestURL().toString()); %>
<jnlp spec="1.5+" codebase="<%="http://"+thisUrl.getHost()+":"+thisUrl.getPort()+"/rvirtual/"%>" >


<% String mode=request.getParameter("mode")==null ? "http" : request.getParameter("mode");  %>
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
<% String url=request.getParameter("url")==null ?"http://"+thisUrl.getHost()+":"+thisUrl.getPort()+"/rvirtual/cmd" : request.getParameter("url");  %>
<% String login=request.getParameter("login")==null ? "" : request.getParameter("login");  %>
<% String password=request.getParameter("password")==null ? "" : request.getParameter("password");  %>
<% String wait=request.getParameter("wait")==null ? "" : request.getParameter("wait");  %>
<% String demo=request.getParameter("demo")==null ? "" : request.getParameter("demo");  %>

  <information>
   <title>Virtual R Workbench</title>
  <vendor>Karim Chine</vendor>
    <homepage href="index.htm"/>
    <description>Virtual R Workbench</description>
    <description kind="short">R Workbench.</description>
    <icon href="R_R.png"/>
    <offline-allowed/>
    <shortcut online="true">
        <desktop/>
        <menu submenu="RWorkbench"/>
     </shortcut>
  </information>

  <security>
      <all-permissions/>
  </security>

  
<resources>
  <j2se version="1.5+"/>
  
  <property name="mode" value="<%=mode%>"/>
  <property name="rmi_mode" value="<%=rmi_mode%>"/>
  <property name="debug" value="<%=debug%>"/>
  <property name="autologon" value="<%=autologon%>"/>
  <property name="nopool" value="<%=nopool%>"/>
  <property name="save" value="<%=save%>"/>
  <property name="lf" value="<%=lf%>"/>
  <property name="stub" value="<%=stub%>"/>
  <property name="name" value="<%=name%>"/>
  <property name="privatename" value="<%=privatename%>"/>
  <property name="noconfirmation" value="<%=noconfirmation%>"/>
  <property name="selfish" value="<%=selfish%>"/>
  <property name="url" value="<%=url%>"/>
  <property name="login" value="<%=login%>"/>
  <property name="password" value="<%=password%>"/>
  <property name="wait" value="<%=wait%>"/>
  <property name="demo" value="<%=demo%>"/>
  
  <jar href="appletlibs/RJB.jar"/>
  <jar href="appletlibs/commons-httpclient-3.1-rc1.jar"/>
  <jar href="appletlibs/commons-codec-1.3.jar"/>
  <jar href="appletlibs/commons-logging-1.1.1.jar"/>
  <jar href="appletlibs/log4j-1.2.15.jar"/>
    <jar href="appletlibs/mapping.jar"/>
  <jar href="appletlibs/idw-gpl.jar"/>
    <jar href="appletlibs/PDFRenderer.jar"/>
  <jar href="appletlibs/pf-joi-full.jar"/>
 <jar href="appletlibs/OpenXLS.jar"/>
  <jar href="appletlibs/jsch-0.1.40.jar"/>
  <jar href="appletlibs/jetty-6.1.11.jar"/>
  <jar href="appletlibs/jetty-util-6.1.11.jar"/>
  <jar href="appletlibs/jetty-client-6.1.11.jar"/>
  <jar href="appletlibs/servlet-api-2.5-6.1.11.jar"/>
  <jar href="appletlibs/activation.jar"/>
<jar href="appletlibs/mail.jar"/>
  <jar href="appletlibs/htmlparser.jar"/>
<jar href="appletlibs/JRI.jar"/>
  <jar href="appletlibs/derbyclient.jar"/>
  <jar href="appletlibs/ganymed-ssh2.jar"/>
  <jar href="appletlibs/batik-anim.jar"/>
  <jar href="appletlibs/batik-awt-util.jar"/>
  <jar href="appletlibs/batik-bridge.jar"/>
  <jar href="appletlibs/batik-css.jar"/>
  <jar href="appletlibs/batik-dom.jar"/>
  <jar href="appletlibs/batik-svggen.jar"/>
  <jar href="appletlibs/batik-ext.jar"/>
  <jar href="appletlibs/batik-extension.jar"/>
  <jar href="appletlibs/batik-gui-util.jar"/>
  <jar href="appletlibs/batik-gvt.jar"/>
  <jar href="appletlibs/batik-parser.jar"/>
  <jar href="appletlibs/batik-script.jar"/>
  <jar href="appletlibs/batik-svg-dom.jar"/>
  <jar href="appletlibs/batik-swing.jar"/>
  <jar href="appletlibs/batik-util.jar"/>
  <jar href="appletlibs/batik-xml.jar"/>
  <jar href="appletlibs/js.jar"/>
  <jar href="appletlibs/xml-apis.jar"/>
  <jar href="appletlibs/xml-apis-ext.jar"/>
  <jar href="appletlibs/edtftpj.jar"/>
  <jar href="appletlibs/jython.jar"/>
  <jar href="appletlibs/swing-layout-1.0.3.jar"/>
  <jar href="appletlibs/groovy-all-1.5.4.jar"/>
  <jar href="appletlibs/freemindbrowser.jar"/>
  <jar href="appletlibs/biocep-doc.jar"/>
  
  
</resources>

<application-desc main-class="org.kchine.r.workbench.WorkbenchLauncher"/>

</jnlp>
