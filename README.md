Hermes is the back-bone library used in the [Wearable Internet of Things][1] course at the University of Rhode Island.

It provides wrappers to Google Play Services, and base classes for simple development.

Usage
-----

In your Android Manifest:

```xml
<manifest ...>
    <application android:name="edu.uri.egr.hermes.HermesApplication" ...>
    </application>
</manifest>
```

or, in your Android application class:
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Hermes.init(this);
    }
}
```


To access Hermes anywhere:
```java
Hermes hermes = Hermes.get();
```

Download
--------

In your build.gradle:
```groovy
compile 'edu.uri.egr.hermes:x:y:z'
```


License
-------

    Copyright 2015 Cody Goldberg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



 [1]: http://www.ele.uri.edu/faculty/kunalm/491_591.xhtml
