package ru.svolf.melissa.compat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.LayoutRes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * on 26.08.2016.
 */

public class ArrayAdapterCompat<T> extends ArrayAdapter<T> {
    public ArrayAdapterCompat(Context context, @LayoutRes int resource, List<T> entries) {
        super(context, resource, entries);
    }

    public ArrayAdapterCompat(Context context, @LayoutRes int resource) {
        super(context, resource, 0, new ArrayList<T>());
    }

    /**
     * Add all elements in the collection to the end of the adapter.
     *
     * @param list to add all elements
     */
    @SuppressLint("NewApi")
    public void addAll(Collection<? extends T> list) {
        super.addAll(list);
    }

    /**
     * Add all elements in the array to the end of the adapter.
     *
     * @param array to add all elements
     */
    @SafeVarargs
    @SuppressLint("NewApi")
    public final void addAll(T... array) {
        super.addAll(array);
    }
}