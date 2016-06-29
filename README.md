ImmediateLooperScheduler
---

A `Scheduler` which executes actions on `Looper`.
It executes actions immediately if it subscribed on same `Looper`.

[![GitHub release](https://img.shields.io/github/release/kubode/ImmediateLooperScheduler.svg?maxAge=2592000)]()
[![Build Status](https://travis-ci.org/kubode/ImmediateLooperScheduler.svg?branch=master)](https://travis-ci.org/kubode/ImmediateLooperScheduler)
[![codecov](https://codecov.io/gh/kubode/ImmediateLooperScheduler/branch/master/graph/badge.svg)](https://codecov.io/gh/kubode/ImmediateLooperScheduler)
[![license](https://img.shields.io/github/license/kubode/ImmediateLooperScheduler.svg?maxAge=2592000)]()


Usage
---

Add dependency to `build.gradle`.

```gradle
repositories {
    jcenter()
}
dependencies {
    compile "com.github.kubode:immediate-looper-scheduler:$latestVersion"
}
```

There are two ways of implementing `ImmediateLooperScheduler`.

1. Override `AndroidSchedulers.mainThread()` by register the hook.

    ```java
    public class MyApplication extends Application {
        @Override
        protected void onCreate() {
            RxAndroidPlugins.getInstance().registerSchedulersHook(new ImmediateLooperSchedulerHook());
        }
    }
    ```

2. Replace `AndroidSchedulers.mainThread()` to `ImmediateLooperScheduler.MAIN`.

    ```java
    observable.observeOn(ImmediateLooperScheduler.MAIN).subscribe(::doSomething);
    ```


Why
---

`AndroidSchedulers.mainThread()` is always dispatches actions to `Handler`.

So, `AndroidSchedulers.mainThread()` not executes actions when subscribing to data sets that using `BehaviorSubject` in MVVM pattern.

```java
public static final BehaviorSubject<List<String>> dataSet = BehaviorSubject.create(Collections.emptyList());

// In ListActivity
@Override protected void onCreate(Bundle savedInstanceState) {
    ArrayAdapter<String> adapter = new ArrayAdapter();
    setAdapter(adapter);
    dataSet.observeOn(AndroidSchedulers.mainThread())
            .subscribe(list -> {
                // This block called after onResume()
                adapter.clear();
                adapter.addAll(list);
            });
}
```

In this case, `ListView` can't restore scroll position when re-created.

`ImmediateLooperScheduler` avoids this issue because it executes actions immediately when actions emitted on same `Looper`.


License
---

```text
Copyright 2016 Masatoshi Kubode

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
