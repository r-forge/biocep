/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import genericnaming.httpregistryClass;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.kchine.r.server.RServices;
import org.kchine.r.server.http.RHttpProxy;
import org.kchine.r.server.http.frontend.ConnectionFailedException;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.SupervisorInterface;


/**
 *
 * @author Karim Chine
 */
public class Main {

    static Vector<RServices> workers=new Vector<RServices>();

    public static void main(String[] args) throws Exception{

        /*
        
        {
                 HashMap<String, Object> options=new HashMap<String, Object>();
                 String session= RHttpProxy.logOn( "http://127.0.0.1:8080/rvirtual/cmd", null, "guest"+"@@"+"toto", "guest" , options);
                 RServices r= RHttpProxy.getR( "http://127.0.0.1:8080/rvirtual/cmd", session, false, 100);
                 r.consoleSubmit("45+9");
                 System.out.println("status"+r.getStatus());

                 if (true) return;
        }
         */
        


        String[] amiUrls=new String[] {
            "http://127.0.0.1:8080/rvirtual/cmd"
        };

        String login="guest";
        String password="guest";
        

        for (int i=0; i<amiUrls.length; ++i) {

            Properties props = new Properties();
            props.put("httpregistry.url", amiUrls[i]);
            props.put("httpregistry.login", login);
            props.put("httpregistry.password", password);
            httpregistryClass hr = new genericnaming.httpregistryClass();
            
            DBLayerInterface db = null;
            try {
                db = (DBLayerInterface) hr.getRegistry(props);
            } catch (ConnectionFailedException ex) {
                ex.printStackTrace();
                return;
            }
            /*
            SupervisorInterface supervisorInterface = hr.getSupervisorInterface();
             * */

            HashMap<String, Object> options=new HashMap<String, Object>();
                 String session= RHttpProxy.logOn( "http://127.0.0.1:8080/rvirtual/cmd", null, "guest"+"@@"+"toto", "guest" , options);
                 System.out.println("new session:"+session);
                 RServices r= RHttpProxy.getR( "http://127.0.0.1:8080/rvirtual/cmd", session, false, 100);
                 r.consoleSubmit("45+9");
                 System.out.println("status"+r.getStatus());

            /*

            String[] worker_names=db.list();
             for (int j=0; j<worker_names.length; ++j) {
                 System.out.println( "url:"+amiUrls[i]+" name:<"+login+"@@"+worker_names[j]+">" );
                 HashMap<String, Object> options=new HashMap<String, Object>();

                 String session= RHttpProxy.logOn(amiUrls[i], null, login+"@@"+worker_names[j], password , options);
                 RServices r= RHttpProxy.getR(amiUrls[i], session, false, 100);
                 r.consoleSubmit("45+6");
                 System.out.print(r.getStatus());
                 workers.add(r);


             }
             * */
        }


        

        /*
        for (int i=0; i<workers.size();++i) {
            workers.elementAt(i).consoleSubmit("8+3");
            System.out.println(workers.elementAt(i).getStatus());
        }
         */
        


    }





public class WorkersFactory extends BasePoolableObjectFactory {
    public Object makeObject() throws Exception  {
        if (workers.size()>0) {
            int index=new Random(System.currentTimeMillis()).nextInt(workers.size());
            RServices result=workers.elementAt(index);
            workers.remove(index);
            return result;
        } else {
            throw new NoSuchElementException("");
        }        
    }

    public void destroyObject(Object obj) throws Exception {
        workers.add((RServices) obj);
    }
}

}
