README FILE

## Google Fit Documentation
https://developers.google.com/fit/preview

## Getting Fit on your device
Nexus 5 (GSM/LTE) "hammerhead"
http://storage.googleapis.com/androiddevelopers/preview/hammerhead-lpv81c-preview-15580494.tgz

Nexus 7 v2 (Wi-Fi) "razor"
http://storage.googleapis.com/androiddevelopers/preview/razor-lpv81c-preview-93dc3e65.tgz

## Get the Google Fit Preview client library
1. Start the Android SDK Manager and scroll to the bottom of the packages list.
2. In the Extras section, select Google Play services for Fit Preview Client Library.
3. In the Extras section, select Google Repository if an update is available.
4. Click Install packages.
5. Accept the Licensing Agreement and click Install.

Note: You need JDK 7 to build Android apps with Google Fit.

## Configure Your Project
compileSdkVersion is set to 'android-L'
minSdkVersion is set to 'L'
targetSdkVersion is set to 'L'

```
dependencies {
    compile 'com.google.android.gms:play-services:5.2.08'
}
```

## License
```
Copyright 2014 Cesar Aguilar (Fuzz Productions)

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