package org.rosuda.ibase;

import java.util.Vector;

public interface NotifierInterface {
    public void addDepend(Dependent c);
    public void delDepend(Dependent c);
    public void NotifyAll(NotifyMsg msg, Dependent c) ;
    public void NotifyAll(NotifyMsg msg, Vector path);
    public void startCascadedNotifyAll(NotifyMsg msg) ;
    public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) ;
    public void NotifyAll(NotifyMsg msg) ;
    public void beginBatch() ;
    public void endBatch() ;
}
