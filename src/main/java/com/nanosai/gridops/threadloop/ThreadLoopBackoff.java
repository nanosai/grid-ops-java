package com.nanosai.gridops.threadloop;

/**
 * A ThreadLoopBackoff can make the Thread Loop back off from using the CPU. If the ThreadLoopActor's of a
 * Thread Loop are called repeatedly and report back that they had nothing to do, the Thread Loop can
 * call ThreadLoopBackoff to make the thread sleep.
 *
 * The longer the ThreadLoopActor's had no work to do, the longer the ThreadLoopBackoff sleeps between iterations.
 * Once the ThreadLoopActor's report they had again work to do, the ThreadLoopBackoff resets sleep time
 * and the cycle starts again.
 *
 */
public class ThreadLoopBackoff {

    private int sleepTimeNanos = 0;
    private int sleepTimeMin   = 0;
    private int sleepTimeMax   = 0;
    private int sleepTimeStep  = 0;

    public ThreadLoopBackoff(int sleepTimeMin, int sleepTimeMax, int sleepTimeStep) {
        this.sleepTimeMin = sleepTimeMin;
        this.sleepTimeMax = sleepTimeMax;
        this.sleepTimeStep = sleepTimeStep;
    }

    public void backoff(int actionsPerformed){
        if(actionsPerformed > 0){
            this.sleepTimeNanos = 0;
        } else {
            this.sleepTimeNanos += this.sleepTimeStep;
            if(this.sleepTimeNanos > this.sleepTimeMax){
                this.sleepTimeNanos = this.sleepTimeMax;
            }
        }

        if(this.sleepTimeNanos >= this.sleepTimeMin) {
            try {
                int millis = this.sleepTimeNanos / 1000;
                int nanos  = this.sleepTimeNanos % 1000;

                Thread.sleep(millis, nanos);
            } catch (InterruptedException e) {
                //ignore - lost sleep is not fatal in any way.
            }
        }

    }
}
