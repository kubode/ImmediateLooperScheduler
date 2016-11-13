package com.github.kubode.rx.android.schedulers;

import android.os.Looper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A {@link Scheduler} which executes actions on {@link Looper}.
 * It executes actions immediately if it subscribed on same {@link Looper}.
 */
public class ImmediateLooperScheduler extends Scheduler {

    /**
     * A {@link Scheduler} which executes actions on the Android UI thread.
     */
    public static final ImmediateLooperScheduler MAIN = new ImmediateLooperScheduler(Looper.getMainLooper());

    private final Looper looper;
    private final Scheduler looperScheduler;
    private final Scheduler immediateScheduler;

    /**
     * Constructs a new instance.
     *
     * @param looper executes actions on this.
     */
    public ImmediateLooperScheduler(Looper looper) {
        this.looper = looper;
        this.looperScheduler = AndroidSchedulers.from(looper);
        this.immediateScheduler = Schedulers.trampoline();
    }

    @Override
    public Worker createWorker() {
        return new ImmediatelyWorker(looper, looperScheduler.createWorker(), immediateScheduler.createWorker());
    }

    private static class ImmediatelyWorker extends Worker {

        private final Looper looper;
        private final Worker looperWorker;
        private final Worker immediateWorker;
        private final Disposable disposable;

        private ImmediatelyWorker(Looper looper, Worker looperWorker, Worker immediateWorker) {
            this.looper = looper;
            this.looperWorker = looperWorker;
            this.immediateWorker = immediateWorker;
            this.disposable = new CompositeDisposable(looperWorker, immediateWorker);
        }

        private Worker getWorker() {
            if (Looper.myLooper() == looper) {
                return immediateWorker;
            } else {
                return looperWorker;
            }
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            return getWorker().schedule(run, delay, unit);
        }

        @Override
        public void dispose() {
            disposable.dispose();
        }

        @Override
        public boolean isDisposed() {
            return disposable.isDisposed();
        }
    }
}
