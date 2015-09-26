Hermes is the back-bone library used in the [Wearable Internet of Things][1] course at the University of Rhode Island.

It provides wrappers to Google Play Services, and base classes for simple development.

Usage
-----

In your Android application class:
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        Hermes.Config config = new Hermes.Config()
            .addApi(***)
            .setRootFolder(***);
            
        Hermes.init(this, config);
    }
}
```

Feel free to configure Hermes as you'd like.  You can add GoogleAPI's to Hermes.Config for it to init for you.


To access Hermes anywhere:
```java
Hermes hermes = Hermes.get();
```

Download
--------

In your build.gradle:
```groovy
repositories {
    jcenter()
}

compile 'com.github.prefanatic.hermes:hermes-core:0.3.0'
compile 'com.github.prefanatic.hermes:hermes-ble:0.3.0'
compile 'com.github.prefanatic.hermes:hermes-wear:0.3.0'
```

You need not install all modules.  If you're only looking for BLE support, install hermes-ble -> applies to all.


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
