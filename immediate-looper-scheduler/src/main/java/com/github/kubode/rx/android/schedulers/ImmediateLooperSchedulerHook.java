package com.github.kubode.rx.android.schedulers;

import rx.Scheduler;
import rx.android.plugins.RxAndroidSchedulersHook;

/**
 * Hook for {@link ImmediateLooperScheduler}.
 */
public class ImmediateLooperSchedulerHook extends RxAndroidSchedulersHook {
    @Override
    public Scheduler getMainThreadScheduler() {
        return ImmediateLooperScheduler.MAIN;
    }
}
