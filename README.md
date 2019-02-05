# sliding-panel

[![Build Status](https://travis-ci.com/PierfrancescoSoffritti/sliding-drawer.svg?branch=master)](https://travis-ci.com/PierfrancescoSoffritti/sliding-drawer) 
[![core](https://api.bintray.com/packages/pierfrancescosoffritti/maven/sliding-panel%3Acore/images/download.svg) ](https://bintray.com/pierfrancescosoffritti/maven/sliding-panel%3Acore/_latestVersion)
[![share on twitter](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Custom%20View%20implementing%20a%20sliding%20panel%20that%20is%20part%20of%20the%20view%20hierarchy,%20not%20above%20it:&url=https://github.com/PierfrancescoSoffritti/sliding-panel&via=PierfrancescoSo&hashtags=opensource,slidingpanel,bottomsheet,androiddev)

A custom View implementing a sliding panel ([bottom sheet pattern](https://material.io/design/components/sheets-bottom.html)) that is part of the view hierarchy, not above it.

## Overview
All other implementations of the bottom _sheet patter_ and _sliding panel pattern_ implement a panel that sits above all the other Views of the app. When the panel is collapsed (but visible) the only way to set its position is by using a peek factor (its distance from the bottom of the screen).

With sliding-panel instead, the bottom sheet is placed exactly where it is supposed to be in the view hierarchy, just like it would be in a vertical `LinearLayout`. It doesn't sit above other Views.

## Sample app
You can download the apk for the sample app of this library [at this link](./sample-app/apk), or [on the PlayStore](https://play.google.com/store/apps/details?id=com.psoffritti.slidingpanel.sampleapp).

<a href='https://play.google.com/store/apps/details?id=com.psoffritti.slidingpanel.sampleapp&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
    <img width='200px' alt='Get it on Google Play'
         src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/>
</a>

The code of the sample app is available [at this link](./sample-app/).

Having the sample apps installed is a good way to be notified of new releases. Although watching this repository will allow GitHub to email you whenever a new release is published.

# Download
The Gradle dependency is available via [jCenter](https://bintray.com/pierfrancescosoffritti/maven). jCenter is the default Maven repository used by Android Studio.

The minimum API level supported by this library is API 15.

```
dependencies {
  implementation 'com.psoffritti.slidingpanel:core:1.0.0'
}
```

## Quick start
In order to start using the library you need to add a [SlidingPanel]() to your layout

```xml
<LinearLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:orientation="vertical" >

  <TextView
    android:id="@+id/placeholder_text_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="100dp"
    android:gravity="center"
    android:text="Hello World!"
    android:background="@android:color/white" />

    <com.psoffritti.slidingpanel.SlidingPanel
      android:id="@+id/sliding_panel"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:orientation="vertical"

      app:nonSlidingView="@id/non_sliding_view"
      app:slidingView="@id/sliding_view"
      app:dragView="@+id/sliding_view"

      app:fitViewToScreen="@+id/collapsed_view"
      app:fitSlidingContentToScreen="false"
      
      app:elevation="4dp">

      <include
        android:id="@+id/non_sliding_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        layout="@layout/video_controls" />

      <FrameLayout
        android:id="@+id/sliding_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    </com.psoffritti.slidingpanel.SlidingPanel>

</LinearLayout>
```

Get a reference to the `YouTubePlayerView` in your code and initialize it

```java
YouTubePlayerView youtubePlayerView = findViewById(R.id.youtube_player_view);
getLifecycle().addObserver(youtubePlayerView);

youtubePlayerView.initialize(new YouTubePlayerInitListener() {
    @Override
    public void onInitSuccess(@NonNull final YouTubePlayer initializedYouTubePlayer) {
        initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady() {
                String videoId = "6JYIGclVQdw";
                initializedYouTubePlayer.loadVideo(videoId, 0);
            }
        });
    }
}, true);
```

That's all you need, a YouTube video is now playing in your app.








You can download the sample app [here](https://github.com/PierfrancescoSoffritti/SlidingDrawer/tree/master/slidingdrawer-sample/apk).

A list of published apps that are using this library:

- [Shuffly](https://play.google.com/store/apps/details?id=com.pierfrancescosoffritti.shuffly)

## Download
Add this to you project-level `build.gradle`:
```
allprojects {
  repositories {
    ...
    maven { url "https://jitpack.io" }
  }
}
```
Add this to your module-level `build.gradle`:
```
dependencies {
  compile 'com.github.PierfrancescoSoffritti:SlidingDrawer:0.12'
}
```

## Usage
The `SlidingDrawer` ViewGroup offerd by this library can have only 2 children.
* The first children is the `non slidable view` (the view that will be covered when the bottom sheet is exapnded).
* The seconds children is the `slidable view` (the actual bottom sheet, the view that will be sliding over the `non slidable view`).

A simple example of an XML file using SlidingDrawer. The `LinearLayout` is the `non slidable view`, the `FrameLayout` is the `slidable view`.

```
<com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:elevation="4dp" >
  
  <LinearLayout
      android:id="@id/non_slidable_view"
      android:layout_width="match_parent"
      android:layout_height="wrap_content" >

      <TextView
          android:layout_width="wrap_content"
          android:layout_height="100dp"
          android:text="non slidable content" />
          
  </LinearLayout>

  <FrameLayout
      android:id="@id/slidable_view"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@android:color/white" >
      
      <TextView
          android:id="@+id/drag_view"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:text="drag me"
          android:clickable="true" />
   </FrameLayout>

</com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer>
```

It's important to set the id attribute to `android:id="@id/non_slidable_view"` for the `non slidable view` and to `android:id="@id/slidable_view"` for the `slidable view`.

When you use a SlidingDrawer remember to always register a `drag view` in your Java/Kotlin code, `slidingDrawer.setDragView(view);`

The `drag view` is the only surface from which the `slidable view` can be dragged. **The drag view must be clickable.**

If you want the `slidable view` to show a different view when it's collapsed and expanded, the `collapsed view` should have a specific id: `android:id="@id/sliding_drawer_collapsed_view"`.
