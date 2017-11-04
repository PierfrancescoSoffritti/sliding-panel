package com.pierfrancescosoffritti.slidingdrawer_sample.utils;

import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

/**
 * Utility class for Fragment transaction
 * @author Pierfrancesco Soffritti
 */
public class FragmentsUtils {

    @SafeVarargs
    public static @NonNull Fragment swapFragments(
            @NonNull FragmentManager supportFragmentManager,
            @IdRes int fragmentContainer,
            @NonNull Fragment newFragment,
            Pair<View, String>... sharedViews) {

        FragmentTransaction fragmentTransaction = replaceFragment_internal(supportFragmentManager, fragmentContainer, newFragment, sharedViews);

        fragmentTransaction.commit();

        return newFragment;
    }

    @SafeVarargs
    public static @NonNull Fragment swapFragmentsAddBackStack(
            @NonNull FragmentManager supportFragmentManager,
            @IdRes int fragmentContainer,
            @NonNull Fragment newFragment,
            Pair<View, String>... sharedViews) {

        FragmentTransaction fragmentTransaction = replaceFragment_internal(supportFragmentManager, fragmentContainer, newFragment, sharedViews);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        return newFragment;
    }

    @SafeVarargs
    private static @NonNull FragmentTransaction replaceFragment_internal(
            @NonNull FragmentManager supportFragmentManager,
            @IdRes int fragmentContainer,
            @NonNull Fragment newFragment,
            Pair<View, String>... sharedViews) {

        newFragment = findFragment(supportFragmentManager, newFragment, null);

        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            for (Pair<View, String> sharedView : sharedViews)
                if (sharedView != null)
                    fragmentTransaction.addSharedElement(sharedView.first, sharedView.second);

        fragmentTransaction.replace(fragmentContainer, newFragment, newFragment.getClass().getName());

        return fragmentTransaction;
    }

    public static @NonNull Fragment addFragment(
            @NonNull FragmentManager supportFragmentManager,
            @IdRes int fragmentContainer,
            @NonNull Fragment newFragment,
            @Nullable String TAG) {

        newFragment = findFragment(supportFragmentManager, newFragment, null);

        String tag;
        if(TAG != null)
            tag = TAG;
        else
            tag = newFragment.getClass().getName();

        supportFragmentManager
                .beginTransaction()
                .add(fragmentContainer, newFragment, tag)
                .commit();

        return newFragment;
    }

    public static @NonNull Fragment findFragment(
            @NonNull FragmentManager supportFragmentManager, @NonNull Fragment newFragment, @Nullable String TAG) {

        String newFragmentClass = newFragment.getClass().getName();

        Fragment oldFragment = null;
        if(TAG != null)
            oldFragment = supportFragmentManager.findFragmentByTag(TAG);
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentByTag(newFragmentClass);
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentByTag(newFragment.getTag());
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentById(newFragment.getId());

        if (oldFragment != null) {
            newFragment = oldFragment;
            Log.d(FragmentsUtils.class.getName(), "Fragment founded: " +newFragmentClass);
        }

        return newFragment;
    }

    public static void removeFragment(@NonNull FragmentManager supportFragmentManager, @NonNull Fragment fragment) {
        supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit();
    }

    public static void removeFragment(@NonNull FragmentManager supportFragmentManager, @NonNull String TAG) {
        supportFragmentManager
                .beginTransaction()
                .remove(supportFragmentManager.findFragmentByTag(TAG))
                .commit();
    }
}