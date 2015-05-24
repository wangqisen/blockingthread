package blockingthread;


import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import blockingthread.exceptions.CancellationException;
import blockingthread.exceptions.ExecutionException;
import blockingthread.exceptions.TimeOutException;

/**
 * Created by xiaowang on 2015/5/23.
 */
public class BlockingThread extends Thread {

    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long waitersOffset;

    static{
        try{
            Field f = Unsafe.class.getDeclaredField("theUnsafe"); //Internal reference
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            Class<?> k = BlockingThread.class;
            stateOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("state"));
            waitersOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("waiters"));
        }catch (Exception e){
            throw new Error(e);
        }
    }


    private Object result;
    private volatile int state;

    private static final int NEW = 0;
    private static final int COMPLETING = 1;
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;
    private static final int CANCELLED = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED = 6;
    private volatile WaitNode waiters;

    private Thread runThread;

    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode() {
            thread = Thread.currentThread();
        }
    }

    public BlockingThread(Thread thread){
        this.runThread=thread;
        this.state=NEW;
    }


    public boolean isDone(){
        return state>=NORMAL;
    }

    public boolean isCancelled(){
        return state>=CANCELLED;
    }

    public boolean cancel(boolean mayInterruptIfRunning){
        if (!(state == NEW &&
                UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                        mayInterruptIfRunning ? INTERRUPTING : CANCELLED))) {

            return false;
        }
        try {    // in case call to interrupt throws exception
            if (mayInterruptIfRunning) {
                try {
                	this.interrupt();
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    public void run(){
        boolean ran;
        try{
            if(runThread!=null)
                runThread.run();
            ran=true;
        }catch (Throwable ex){
            result=null;
            ran=false;
            setException(ex);
        }
        if(ran){
            if(UNSAFE.compareAndSwapInt(this,stateOffset,NEW,COMPLETING)){
                UNSAFE.putOrderedInt(this,stateOffset,NORMAL);
                finishCompletion();
            }
        }

    }


    private void setException(Throwable t){
        if(UNSAFE.compareAndSwapInt(this,stateOffset,NEW,COMPLETING)){
            UNSAFE.putOrderedInt(this,stateOffset,EXCEPTIONAL);
            finishCompletion();
        }
    }

    private void finishCompletion(){
        for(WaitNode q;(q=waiters)!=null;){
            if(UNSAFE.compareAndSwapObject(this,waitersOffset,q,null)){
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }
    }

    public void report(int state) throws ExecutionException,CancellationException {
        if (state == NORMAL) {
            return ;
        } else if (state >= CANCELLED) {
            throw new CancellationException();
        }else{
            throw new ExecutionException();
        }
    }

    public void get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        report(s);
    }

    public void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeOutException,NullPointerException {
        if(unit==null)
            throw new NullPointerException();
        int s=state;
        if(s<=COMPLETING&&(s=awaitDone(true,unit.toNanos(timeout)))<=COMPLETING){
            throw new TimeOutException();
        }
        report(s);
    }

    private void removeWaitNode(WaitNode node){
        if(node!=null) {
            node.thread = null;
            retry:
            while(true){
                for(WaitNode pred=null,q=waiters,s=null;q!=null;q=s){
                    s=q.next;
                    if (q.thread!=null)
                        pred=q;
                    else if(pred!=null){
                        pred.next=s;
                        if(pred.thread==null){
                            continue retry;
                        }
                    }else if(!UNSAFE.compareAndSwapObject(this, waitersOffset,
                            q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    private int awaitDone(boolean hasTimeLimit,long nanos) throws InterruptedException{
        final long deadline=hasTimeLimit?System.nanoTime()+nanos:0L;
        WaitNode q=null;
        boolean isQueued=false;
        while (true){
            if(Thread.interrupted()){
                removeWaitNode(q);
                throw new InterruptedException();
            }
            int s=state;
            if(s>COMPLETING){
                if(q!=null){
                    q.thread=null;
                    return s;
                }
            }
            else if(s==COMPLETING){
                Thread.yield();
            }
            else if(q==null)
                q=new WaitNode();
            else if(!isQueued){
                isQueued=UNSAFE.compareAndSwapObject(this,waitersOffset,q.next=waiters,q);
            }
            else if(hasTimeLimit){
                nanos=deadline-System.nanoTime();
                if(nanos<=0L){
                    removeWaitNode(q);
                    return state;
                }
                LockSupport.parkNanos(this,nanos);
            }
        }
    }

}
