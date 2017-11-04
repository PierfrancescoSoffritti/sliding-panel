# SlidingDrawer

[![](https://jitpack.io/v/PierfrancescoSoffritti/SlidingDrawer.svg)](https://jitpack.io/#PierfrancescoSoffritti/SlidingDrawer)

A custom View implementing the <a href="https://material.google.com/components/bottom-sheets.html?authuser=0">bottom sheet pattern.</a><br/>
This ViewGroup can have only 2 children. The 1st one is the <b>non slidable view</b> ; the 2nd is the <b>slidable view</b>, which can slide over the <b>non slidable view</b>.<br/><br/>
The substantial difference from all other implementations is that in this case is easy to position the <b>slidable view</b> relative to the <b>non slidable view</b>.<br/>
In other implementations the only way to control the position of the <b>slidable view</b>, when collapsed, is by using a peek factor.<br/>
Here instead the <b>slidable view</b> is placed exactly below the <b>non slidable view</b>, just like in a vertical LinearLayout. The <b>slidable view</b> is conceptually part of the hierarchy and it's not above it.

Download the sample app [here](https://github.com/PierfrancescoSoffritti/SlidingDrawer/tree/master/slidingdrawer-sample/apk)

Apps using this library: [Shuffly](https://play.google.com/store/apps/details?id=com.pierfrancescosoffritti.shuffly)
<br/><br/>
<img height="450" src="https://github.com/PierfrancescoSoffritti/SlidingDrawer/blob/master/pics/SlidingView.gif" />
<br/>

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
  compile 'com.github.PierfrancescoSoffritti:SlidingDrawer:0.10'
}
```

## Usage

```
<com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  
  android:id="@+id/sliding_drawer"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  app:shadow_length="4dp" >

  <View
    android:id="@id/non_slidable_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
    
  <FrameLayout
    android:id="@id/slidable_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

</com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer>
```
It's important to set the id attribute to `android:id="@id/non_slidable_view"` for the <b>non slidable view</b> and to `android:id="@id/slidable_view"` for the <b>slidable view</b>.

When you use this ViewGroup remember to always add a <b>drag view</b> `slidingDrawer.setDragView(view);` <br/>
The <b>drag view</b> is the only surface from which the <b>slidable view</b> can be dragged.

In case the <b>slidable view</b> has different views when collapsed (<b>collapsed view</b>) or expanded (<b>expanded view</b>), and the <b>collapsed view</b> isn't a List (or equivalent), it should have the id `android:id="@id/sliding_drawer_collapsed_view"`, so the `SlidingDrawer` can adjust the view's paddingBottom to prevent its content from going offscreen.
