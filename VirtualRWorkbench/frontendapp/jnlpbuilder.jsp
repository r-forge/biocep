<% response.setContentType("application/x-java-jnlp-file"); %>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.5+" codebase="<%=request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/jaws"))+"/"%>" >

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
  <j2se version="1.5+" />
  <property name="url" value="<%=request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/jaws"))+"/cmd"%>"/>
  <property name="autologon" value="true"/>
  <property name="nopool" value="=false"/>
  <property name="save" value="false"/>
  <property name="demo" value="false"/>
  <property name="lf" value="0"/>
  <property name="login" value="guest"/>
  
  <jar href="appletlibs/RJB.jar"/>
  <jar href="appletlibs/commons-httpclient-3.1-rc1.jar"/>
  <jar href="appletlibs/commons-codec-1.3.jar"/>
  <jar href="appletlibs/commons-logging-1.1.jar"/>
  <jar href="appletlibs/log4j-1.2.14.jar"/>
  <jar href="appletlibs/idw-gpl.jar"/>
  <jar href="appletlibs/jeditmodes.jar"/>
  <jar href="appletlibs/jedit.jar"/>
  <jar href="appletlibs/pf-joi-full.jar"/>
  <jar href="appletlibs/OpenXLS.jar"/>
  <jar href="appletlibs/JRI.jar"/>     
  <jar href="appletlibs/htmlparser.jar"/>       
  <jar href="appletlibs/servlet-api-2.5-6.1.8.jar"/>  
  <jar href="appletlibs/jetty-6.1.8.jar"/>  
  <jar href="appletlibs/jetty-util-6.1.8.jar"/>   
  <jar href="appletlibs/activation.jar"/>
  <jar href="appletlibs/mail.jar"/>  
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
  <jar href="appletlibs/derbyclient.jar"/> 
  <jar href="appletlibs/ganymed-ssh2.jar"/>   
  <jar href="appletlibs/edtftpj.jar"/>
  <jar href="appletlibs/jython.jar"/>
  <jar href="appletlibs/swing-layout-1.0.3.jar"/>
  <jar href="appletlibs/groovy-all-1.5.4.jar"/>
  <jar href="appletlibs/biocep-doc.jar"/>
  <jar href="appletlibs/mapping.jar"/>
   
</resources>

<application-desc main-class="graphics.rmi.GDAppletLauncher"/>

</jnlp>
