package com.github.kubode.rx.android.schedulers;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;

import rx.Scheduler;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImmediateLooperSchedulerTest {

    private final Looper main = Looper.getMainLooper();
    private final Handler mainHandler = new Handler(main);
    private Looper other;
    private Handler otherHandler;
    private volatile boolean isEmitted;

    public ImmediateLooperSchedulerTest() {
        // initialize other looper.
        final Semaphore lock = createLock();
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("other");
                Looper.prepare();
                other = Looper.myLooper();
                otherHandler = new Handler(other);
                release(lock);
                Looper.loop();
            }
        }.start();
        waitRelease(lock);
    }

    @Before
    public void setUp() {
        isEmitted = false;
    }

    private Semaphore createLock() {
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquireUninterruptibly();
        return semaphore;
    }

    private void release(Semaphore semaphore) {
        semaphore.release();
    }

    private void waitRelease(Semaphore semaphore) {
        semaphore.acquireUninterruptibly();
    }

    private void test(final Handler handler, final Scheduler scheduler, final boolean isEmitExpected) {
        final Semaphore handlerLock = createLock();
        final Semaphore onNextLock = createLock();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Single.just("test").observeOn(scheduler).subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        SystemClock.sleep(100);
                        isEmitted = true;
                        release(onNextLock);
                    }
                });
                release(handlerLock);
            }
        });
        waitRelease(handlerLock);
        assertEquals(isEmitExpected, isEmitted);
        waitRelease(onNextLock);
    }

    @Test
    public void looperSchedulerNotEmitImmediately() {
        test(mainHandler, AndroidSchedulers.from(main), false);
    }

    @Test
    public void immediateOnMainToMain() {
        test(mainHandler, new ImmediateLooperScheduler(main), true);
    }

    @Test
    public void notImmediateOnMainToOther() {
        test(mainHandler, new ImmediateLooperScheduler(other), false);
    }

    @Test
    public void notImmediateOnOtherToMain() {
        test(otherHandler, new ImmediateLooperScheduler(main), false);
    }

    @Test
    public void immediateOnOtherToOther() {
        test(otherHandler, new ImmediateLooperScheduler(other), true);
    }
}
