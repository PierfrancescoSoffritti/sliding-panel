# sliding-panel

<img align="right" width="200px" src="./images/icon_512.png" title="logo" />

[![Build Status](https://travis-ci.com/PierfrancescoSoffritti/sliding-panel.svg?branch=master)](https://travis-ci.com/PierfrancescoSoffritti/sliding-panel) 
[![core](https://api.bintray.com/packages/pierfrancescosoffritti/maven/sliding-panel%3Acore/images/download.svg) ](https://bintray.com/pierfrancescosoffritti/maven/sliding-panel%3Acore/_latestVersion) [![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-sliding--panel-green.svg?style=flat )]( https://android-arsenal.com/details/1/7532 )

[![share on twitter](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Custom%20View%20implementing%20a%20sliding%20panel%20that%20is%20part%20of%20the%20view%20hierarchy,%20not%20above%20it:&url=https://github.com/PierfrancescoSoffritti/sliding-panel&via=psoffritti&hashtags=opensource,slidingpanel,bottomsheet,androiddev)

A ViewGroup that implements a sliding panel that is part of the view hierarchy, not above it.

### Difference from other libraries
All other implementations of the [_bottom sheet pattern_](https://material.io/design/components/sheets-bottom.html) and _sliding panel pattern_ implement a panel that sits above all the other Views of the app. When the panel is collapsed (but visible) the only way to set its position is by using a peek factor (its distance from the bottom of the screen).

With this library the sliding panel is placed exactly where it is supposed to be in the view hierarchy, just like it would be in a vertical (or horizontal) `LinearLayout`. It doesn't sit above other Views.

### Overview
`SlidingPanel` is a ViewGroup exending `FrameLayout`.

It has exacly two children: a **non sliding view** and a **sliding view**.

* The **non sliding view** is just a view that doesn't move, positioned as if `SlidingPanel` was a `LinearLayout`.
* The **sliding view** is a View that can be dragged by the user. It slides over the non sliding view, both vertically and horizontally.

The sliding view can be **collapsed** or **expanded**.

* When **collapsed**, the sliding view is exactly where it would be if `SlidingPanel` was a LinearLayout.
* When **expanded**, the sliding view is positioned to exactly cover the non sliding view. (Therefore the maximum amount of movement allowed to the sliding view is equal to the height (or width) of the non sliding view)

<img width="250px" src="./images/sliding_panel.gif" title="logo" />

## Sample app
You can download the apk of the sample app [at this link](./sample-app/apk), or [on the PlayStore](https://play.google.com/store/apps/details?id=com.psoffritti.slidingpanel.sampleapp).

<a href='https://play.google.com/store/apps/details?id=com.psoffritti.slidingpanel.sampleapp&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
    <img width='200px' alt='Get it on Google Play'
         src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/>
</a>

The code of the sample app is available [at this link](./sample-app/).

Having the sample apps installed is a good way to be notified of new releases. Although watching this repository will allow GitHub to email you whenever a new release is published.

## Download
The Gradle dependency is available via [jCenter](https://bintray.com/pierfrancescosoffritti/maven). jCenter is the default Maven repository used by Android Studio.

The minimum API level supported by this library is API 15.

```
dependencies {
  implementation 'com.psoffritti.slidingpanel:core:1.0.0'
}
```

## Quick start
In order to start using the library you need to add a `SlidingPanel` to your layout.

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:id="@+id/sliding_panel"
  android:layout_width="match_parent"
  android:layout_height="match_parent"

  app:orientation="vertical"
  app:nonSlidingView="@id/non_sliding_view"
  app:slidingView="@id/sliding_view"
  app:elevation="4dp" >

  <TextView
    android:id="@+id/non_sliding_view"
    android:layout_width="match_parent"
    android:layout_height="150dp"
    android:gravity="center"
    android:text="non sliding view"
    android:background="#af4448" />

  <TextView
    android:id="@+id/sliding_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:text="sliding view"
    android:background="#e57373" />

</com.psoffritti.slidingpanel.SlidingPanel>
```

`non_sliding_view` and `sliding_view` can be whatever View or ViewGroup you need.

If you want to listen to slide events, you can add a `OnSlideListener` to the `SlidingPanel`.

```kotlin
sliding_panel.addSlideListener { slidingPanel, state, currentSlide ->
  when(state) {
    PanelState.COLLAPSED -> { }
    PanelState.EXPANDED -> { }
    PanelState.SLIDING -> { }
  }
}
```

## API documentation

### Table of contents
1. [SlidingPanel attributes](#slidingpanel-attributes)
    1. [slidingView](#slidingview)
    2. [nonSlidingView](#nonslidingview)
    3. [dragView](#dragview)
    4. [fitToScreenView](#fittoscreenview)
    5. [orientation](#orientation)
    6. [elevation](#elevation)
3. [API](#api)
    1. [Panel state](#panel-state)
    2. [slideTo](#slideto)
    3. [slideDuration](#slideduration)
    4. [Programmatically set drag view](#programmatically-set-drag-view)
    5. [Listen to events](#listen-to-events)

## SlidingPanel attributes
`SlidingPanel` has a set of attributes that you can set to customize its behviour. Some of this attributes are mandatory.

### slidingView
**Mandatory: yes      -- Value: view reference         -- Default: `null`**

This mandatory attribute is used to tell `SlidingPanel` which of its two children is the sliding view. If this attribute is not set `SlidingPanel` will throw an Excpetion.

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  ...
  app:slidingView="@id/sliding_view" >

  ...
</com.psoffritti.slidingpanel.SlidingPanel>
```

### nonSlidingView
**Mandatory: yes      -- Value: view reference         -- Default: `null`**

This mandatory attribute is used to tell `SlidingPanel` which of its two children is the non sliding view. If this attribute is not set `SlidingPanel` will throw an Excpetion.

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  ...
  app:nonSlidingView="@id/non_sliding_view" >

  ...
</com.psoffritti.slidingpanel.SlidingPanel>
```

### dragView
**Mandatory: no       -- Value: view reference         -- Default: slidingView**

This attribute is used to tell `SlidingPanel` which View should be used to drag the sliding view. If not set this value defaults to the sliding view. Therefore the whole panel will be sensible to dragging.

Note that if the whole panel is draggable it won't be possible to use scrollable views inside the sliding view.

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  ...
  app:dragView="@id/drag_view" >

  ...
</com.psoffritti.slidingpanel.SlidingPanel>
```

### fitToScreenView
**Mandatory: no       -- Value: view reference         -- Default: `null`**

When collapsed, the sliding view is shifted down (or right) by an amount equal to the height (or width) of the non sliding view. Therefore, when collapsed, the bottom (or right) part of the sliding view will be out of the screen.

This attribute is used to tell `SlidingPanel` that we want a view to be shifted up (or left) so that it is always visible.

See the screenshots below to better understand. In the first one `fitToScreenView` is set, in the second one it isn't.

![fitToScreenView](./images/fitToScreenView.png)

Notice the white text at the bottom of the screen. It is not visible in the second screen, it is visible only when the panel is expanded.

The sample app has [an example specific for this attribute](./sample-app/src/main/java/com/psoffritti/slidingpanel/sampleapp/examplesfittoscreenview/FitToScreenViewExampleActivity.kt).

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  ...
  app:fitToScreenView="@id/fit_to_screen_view" >

  ...
</com.psoffritti.slidingpanel.SlidingPanel>
```

### orientation
**Mandatory: no       -- Value: `vertical` | `horizontal`  -- Default: `vertical`**

This attribute is used to set the orientation of `SlidingPanel` in the same way that it is used for a `LinearLayout`. By default it is set to `vertical`.

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  ...
  app:orientation="horizontal" >

  ...
</com.psoffritti.slidingpanel.SlidingPanel>
```

### elevation
**Mandatory: no       -- Value: dimension              -- Default: `4dp`**

This attribute is used to set the length of the shadow drawn to the top (or left) side of the sliding view.

```xml
<com.psoffritti.slidingpanel.SlidingPanel
  ...
  app:elevation="10dp" >

  ...
</com.psoffritti.slidingpanel.SlidingPanel>
```

## API
You can interact with `SlidingPanel` programmatically through its API.

### Panel state
`SlidingPanel` has a state that can be one of: `EXPANDED`, `COLLAPSED` and `SLIDING`.

You can `get` the state but are not allowed to set it directly.

To programmatically change the state of the panel you should use the [`slideTo`](#slideto) method.

### `slideTo`
This method takes as argument one of the possible states of the panel: `EXPANDED`, `COLLAPSED`. If you try to pass `SLIDING` the panel will throw an `IllegalArgumentException`.

When this method is called the panel will automatically animate to to match the state passed as argument.

### `slideDuration`
You can set the duration of the slide animation using the `slideDuration` property.

This property affects: 
- the duration of the slide triggered by `slideTo`.
- the speed at which the panel autocompletes the slides when the user stops dragging it before reaching the `EXPANDED` or `COLLAPSED` state.

You can use the constants defined in `SlidingPanel` (`SlidingPanel.SLIDE_DURATION_SHORT`, `SlidingPanel.SLIDE_DURATION_LONG`) or set it to an arbitrary duration in millisecond.

```kotlin
sliding_panel.slideDuration = SlidingPanel.SLIDE_DURATION_SHORT
sliding_panel.slideDuration = SlidingPanel.SLIDE_DURATION_LONG
```

### Programmatically set drag view
Sometimes it is usefull to change the dragView at runtime.

[An example is give in the sample app, in the "advanced example"](./sample-app/src/main/java/com/psoffritti/slidingpanel/sampleapp/examples/advanced). In this case a list is shown when the panel is expanded, therefore the drag view has to be changed. Otherwise the list wouldn't be scrollable.

Use the `setDragView(view: View)` method to programmatically set the drag view.

### Listen to events
You can listen to slide events, by adding an `OnSlideListener` to the `SlidingPanel`.

```kotlin
sliding_panel.addSlideListener { slidingPanel, state, currentSlide ->
  when(state) {
    PanelState.COLLAPSED -> { }
    PanelState.EXPANDED -> { }
    PanelState.SLIDING -> { }
  }
}
```
or

```kotlin
sliding_panel.addSlideListener(object : SlidingPanel.OnSlideListener {
  override fun onSlide(slidingPanel: SlidingPanel, state: PanelState, currentSlide: Float) { }
})
```

- The first argument is a referece to the `SlidingPanel` emitting the event.
- The second argument is the current state of the panel.
- The third argument is a value between 0 and 1. The value is 0 when the state is `COLLAPSED` and 1 when `EXPANDED`.

---

For any question feel free to [open an issue on the GitHub repository](https://github.com/PierfrancescoSoffritti/sliding-panel/issues).