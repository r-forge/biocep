<% response.setContentType("application/x-java-jnlp-file"); %>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.5+" codebase="<%=request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/desktopjaws"))+"/"%>" >

<information>
  <title>Virtual R Workbench</title>
  <vendor>Karim Chine</vendor>
  <homepage href="" />
  <description/>
</information>

  <security>
      <all-permissions/>
  </security>
<resources>
  <j2se version="1.5+"/>
  
   <property name="debug" value="<%=request.getParameter("debug")%>"/>
   <property name="mode" value="local"/>
   <property name="autologon" value="true"/>
     
   <jar href="appletlibs/RJB.jar"/>
   <jar href="appletlibs/commons-httpclient-3.1-rc1.jar"/>
   <jar href="appletlibs/commons-codec-1.3.jar"/>
   <jar href="appletlibs/commons-logging-1.1.1.jar"/>
   <jar href="appletlibs/mapping.jar"/>
   <jar href="appletlibs/idw-gpl.jar"/>
   <jar href="appletlibs/pf-joi-full.jar"/>
   <jar href="appletlibs/OpenXLS.jar"/>
   <jar href="appletlibs/JRI.jar"/>
   <jar href="appletlibs/htmlparser.jar"/>
   
  <jar href="appletlibs/servlet-api-2.5-6.1.8.jar"/>
  <jar href="appletlibs/jsp-api-2.1.jar"/>  
  <jar href="appletlibs/jsp-2.1.jar"/>  
    
  <jar href="appletlibs/jetty-6.1.8.jar"/>  
  <jar href="appletlibs/jetty-util-6.1.8.jar"/>   
  <jar href="appletlibs/activation.jar"/>
  <jar href="appletlibs/mail.jar"/>
   
  
</resources>
<application-desc main-class="graphics.rmi.GDAppletLauncher"/>
</jnlp>
