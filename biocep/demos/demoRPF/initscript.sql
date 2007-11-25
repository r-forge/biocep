insert into POOL_DATA (POOL_NAME, TIMEOUT, POOL_PREFIXES) values ('R',400000,'RSERVANT_');
insert into NODE_DATA (NODE_NAME, HOST_IP , POOL_PREFIX ,HOST_NAME, LOGIN, PWD ,INSTALL_DIR, CREATE_SERVANT_COMMAND, KILL_SERVANT_COMMAND, OS , SERVANT_NBR_MIN, SERVANT_NBR_MAX,PROCESS_COUNTER) values ('N1', '<%=uk.ac.ebi.microarray.pools.PoolUtils.getHostIp()%>', 'RSERVANT_', '<%=uk.ac.ebi.microarray.pools.PoolUtils.getHostName()%>', '','', '.', 'ant -f ${INSTALL_DIR}/demos/demoRPF/build.xml demoserver','', '<%=uk.ac.ebi.microarray.pools.PoolUtils.getOs()%>',0,5, 0 );
commit;
 