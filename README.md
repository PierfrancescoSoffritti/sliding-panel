# SlidingDrawer

[![](https://jitpack.io/v/PierfrancescoSoffritti/SlidingDrawer.svg)](https://jitpack.io/#PierfrancescoSoffritti/SlidingDrawer)

A custom View implementing the <a href="https://material.google.com/components/bottom-sheets.html?authuser=0">bottom sheet pattern.</a>

## Overview
The substantial difference from all other implementations of the bottom sheet pattern is that, in this case, the bottom sheet is part of the view hierarchy, and not above it.

In other implementations the only way to control the position of a collapsed bottom sheet is by using a peek factor (its distance from the bottom of the screen).
Here instead a collapsed bottom sheet is placed exactly where it is supposed to be in view hierarchy, just like it would be in a vertical `LinearLayout`.

<img height="450" src="https://github.com/PierfrancescoSoffritti/SlidingDrawer/blob/master/pics/SlidingView.gif" />

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
  compile 'com.github.PierfrancescoSoffritti:SlidingDrawer:0.11'
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
