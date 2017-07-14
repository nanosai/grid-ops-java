package com.nanosai.gridops.threadloop;

/**
 * Created by jjenkov on 20/04/2017.
 */
public class ThreadLoopDefaultImpl implements Runnable {

    private IThreadLoopActor[] threadLoopActors = null;
    private ThreadLoopBackoff threadLoopBackoff = null;

    private volatile boolean shouldStop;

    public ThreadLoopDefaultImpl(ThreadLoopBackoff threadLoopBackoff, IThreadLoopActor ... threadLoopActors){
        this.threadLoopBackoff= threadLoopBackoff;
        this.threadLoopActors = threadLoopActors;
    }

    public void stop() {
        this.shouldStop = true;
    }

    public boolean shouldStop() {
        return this.shouldStop;
    }

    @Override
    public void run() {
        while(!shouldStop()){
            int actions = 0;
            for(int i=0; i<threadLoopActors.length; i++) {
                actions += threadLoopActors[i].act();
            }
            if(this.threadLoopBackoff != null){
                this.threadLoopBackoff.backoff(actions);
            }
        }

    }
}
