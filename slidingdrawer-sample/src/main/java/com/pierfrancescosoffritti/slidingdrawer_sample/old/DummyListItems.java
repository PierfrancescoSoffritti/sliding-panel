package com.pierfrancescosoffritti.slidingdrawer_sample.old;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DummyListItems {

    private static final int COUNT = 25;

    public static final List<DummyItem> ITEMS = new ArrayList<>();

    static {
        for (int i = 1; i <= COUNT; i++)
            ITEMS.add(new DummyItem("Item " +i));
    }

    public static class DummyItem {
        public final String content;

        DummyItem(String content) {
            this.content = content;
        }

        @NonNull
        @Override
        public String toString() {
            return content;
        }
    }
}
