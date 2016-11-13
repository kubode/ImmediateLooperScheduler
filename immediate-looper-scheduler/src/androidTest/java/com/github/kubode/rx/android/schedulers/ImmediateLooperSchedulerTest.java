package com.github.kubode.rx.android.schedulers;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImmediateLooperSchedulerTest {

    private final Looper main = Looper.getMainLooper();
    private Looper other;

    public ImmediateLooperSchedulerTest() {
        // initialize other looper.
        final Semaphore lock = createLock();
        new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("other");
                Looper.prepare();
                other = Looper.myLooper();
                release(lock);
                Looper.loop();
            }
        }.start();
        waitRelease(lock);
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

    private void test(Looper from, final Scheduler scheduler, final boolean isDelayed, boolean isEmitExpected) {
        final Semaphore handlerLock = createLock();
        final Semaphore onNextLock = createLock();
        final AtomicBoolean isEmitted = new AtomicBoolean();
        new Handler(from).post(new Runnable() {
            @Override
            public void run() {
                Single<Boolean> single = Single.just(true);
                if (isDelayed) {
                    single = single.delay(20, TimeUnit.MILLISECONDS, scheduler);
                } else {
                    single = single.observeOn(scheduler);
                }
                single.subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean b) {
                        SystemClock.sleep(20);
                        isEmitted.set(b);
                        release(onNextLock);
                    }
                });
                release(handlerLock);
            }
        });
        waitRelease(handlerLock);
        assertEquals(isEmitExpected, isEmitted.get());
        waitRelease(onNextLock);
    }

    @Test
    public void looperSchedulerNotEmitImmediately() {
        test(main, AndroidSchedulers.from(main), false, false);
    }

    @Test
    public void immediateOnMainToMain() {
        test(main, new ImmediateLooperScheduler(main), false, true);
    }

    @Test
    public void notImmediateOnMainToOther() {
        test(main, new ImmediateLooperScheduler(other), false, false);
    }

    @Test
    public void notImmediateOnOtherToMain() {
        test(other, new ImmediateLooperScheduler(main), false, false);
    }

    @Test
    public void immediateOnOtherToOther() {
        test(other, new ImmediateLooperScheduler(other), false, true);
    }

    // Delayed
    @Test
    public void looperSchedulerNotEmitImmediatelyDelayed() {
        test(main, AndroidSchedulers.from(main), true, false);
    }

    @Test
    public void immediateDelayedOnMainToMain() {
        test(main, new ImmediateLooperScheduler(main), true, true);
    }
}
