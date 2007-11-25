<% response.setContentType("application/x-java-jnlp-file"); %>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.5+" codebase="<%=request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/desktopjaws"))+"/"%>" >

<information>
  <title>Virtual R Workbench</title>
  <vendor>EMBL-EBI-Microarray Informatics</vendor>
  <homepage href="" />
  <description/>
</information>

  <security>
      <all-permissions/>
  </security>
<resources>
  <j2se version="1.5+"/>
  <property name="debug" value="<%=request.getParameter("debug")%>"/>  
  <jar href="appletlibs/RJB.jar"/>
  <jar href="appletlibs/commons-httpclient-3.1-rc1.jar"/>
  <jar href="appletlibs/commons-codec-1.3.jar"/>
  <jar href="appletlibs/commons-logging-1.1.jar"/>
  <jar href="appletlibs/mapping.jar"/>
  <jar href="appletlibs/idw-gpl.jar"/>
  <jar href="appletlibs/jeditmodes.jar"/>
  <jar href="appletlibs/jedit.jar"/>
  <jar href="appletlibs/pf-joi-full.jar"/>
  <jar href="appletlibs/OpenXLS.jar"/>
</resources>
<application-desc main-class="graphics.rmi.GDDesktopLauncher"/>
</jnlp>
