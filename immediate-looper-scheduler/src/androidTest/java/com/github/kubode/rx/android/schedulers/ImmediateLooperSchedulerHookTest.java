package com.github.kubode.rx.android.schedulers;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.android.plugins.RxAndroidPlugins;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImmediateLooperSchedulerHookTest {

    @Test
    public void overrideMainThreadScheduler() {
        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new ImmediateLooperSchedulerHook());
        assertEquals(ImmediateLooperScheduler.MAIN, AndroidSchedulers.mainThread());
    }
    void a() {
        BehaviorSubject<List<String>> dataSet = BehaviorSubject.<List<String>>create(new ArrayList<String>());
    }
}
