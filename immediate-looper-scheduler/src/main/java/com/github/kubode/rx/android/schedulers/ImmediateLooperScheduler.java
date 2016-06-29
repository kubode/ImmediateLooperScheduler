package com.github.kubode.rx.android.schedulers;

import android.os.Looper;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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
        this.immediateScheduler = Schedulers.immediate();
    }

    @Override
    public Worker createWorker() {
        return new ImmediatelyWorker(looper, looperScheduler.createWorker(), immediateScheduler.createWorker());
    }

    private static class ImmediatelyWorker extends Worker {

        private final Looper looper;
        private final Worker looperWorker;
        private final Worker immediateWorker;
        private final Subscription subscription;

        private ImmediatelyWorker(Looper looper, Worker looperWorker, Worker immediateWorker) {
            this.looper = looper;
            this.looperWorker = looperWorker;
            this.immediateWorker = immediateWorker;
            this.subscription = new CompositeSubscription(looperWorker, immediateWorker);
        }

        @Override
        public void unsubscribe() {
            subscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return subscription.isUnsubscribed();
        }

        @Override
        public Subscription schedule(Action0 action) {
            return getWorker().schedule(action);
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            return getWorker().schedule(action, delayTime, unit);
        }

        private Worker getWorker() {
            if (Looper.myLooper() == looper) {
                return immediateWorker;
            } else {
                return looperWorker;
            }
        }
    }
}
