package ru.svolf.rxmanager.util;

import java.text.Collator;
import java.util.Comparator;

import ru.svolf.rxmanager.data.AppListData;

/**
 * Created by Martin Styk on 17.06.2017.
 */

public class AppBasicInfoComparator implements Comparator<AppListData> {

    public static final AppBasicInfoComparator INSTANCE = new AppBasicInfoComparator();

    private final Collator sCollator = Collator.getInstance();

    @Override
    public int compare(AppListData object1, AppListData object2) {
        return sCollator.compare(object1.getApplicationName(), object2.getApplicationName());
    }
}
