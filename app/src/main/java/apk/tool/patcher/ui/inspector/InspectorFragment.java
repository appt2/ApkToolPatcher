package apk.tool.patcher.ui.inspector;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.viewpager.widget.ViewPager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apk.tool.patcher.App;
import apk.tool.patcher.R;
import apk.tool.patcher.api.Project;
import apk.tool.patcher.ui.base.adapters.ViewPagerAdapter;
import apk.tool.patcher.ui.misc.EasterEggFragment;
import ru.svolf.melissa.widget.BigTabsLayout;
import apk.tool.patcher.util.Cs;
import apk.tool.patcher.util.Preferences;
import apk.tool.patcher.util.SysUtils;
import ru.svolf.melissa.fragment.dialog.SweetWaitDialog;
import ru.svolf.melissa.model.InterestSmaliItem;
import ru.svolf.melissa.swipeback.SwipeBackFragment;
import ru.svolf.melissa.swipeback.SwipeBackLayout;

public class InspectorFragment extends SwipeBackFragment {
    public static final String FRAGMENT_TAG = "smali_parent_fragment";
    private static final String TAG = "InspectorFragment";
    private String mPath;
    private int i;

    private ArrayList<InterestSmaliItem> mItems, mNormalItems, mSortedItems;
    private View rootView;
    private LinearLayout mNotFound;
    private SweetWaitDialog mWaitDialog;
    private ViewPager mPager;
    private Project mProject;

    // TODO: Rename and change types and number of parameters
    public static InspectorFragment newInstance(Project projekt) {
        Log.d(TAG, "newInstance() called with: projekt = [" + projekt + "]");
        InspectorFragment fragment = new InspectorFragment();
        Bundle args = new Bundle();
        args.putParcelable(Cs.ARG_PATH_NAME, projekt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_smali_inspector, container, false);
        return attachToSwipeBack(rootView);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BigTabsLayout tabLayout = view.findViewById(R.id.tab_layout);
        mPager = view.findViewById(R.id.tab_pager);
        mNotFound = view.findViewById(R.id.not_found);
        tabLayout.setupWithPager(mPager);

        if (getArguments() != null) {
            mProject = getArguments().getParcelable(Cs.ARG_PATH_NAME);
            if (mProject != null)
                mPath = mProject.getPath();
        }

        if (mPath != null && !mPath.isEmpty() && mProject.isValid()) {
            new Inspector().execute(mPath);
        } else {
            mPager.setVisibility(View.GONE);
            mNotFound.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        mPath = null;
        mNotFound = null;
        rootView = null;
        mWaitDialog = null;
        mItems = null;
        mNormalItems = null;
        mSortedItems = null;
        super.onDestroyView();
    }

    private void setViewPager(ViewPager pager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(VisibleFragment.newInstance(mNormalItems), App.bindString(R.string.tab_visible, mNormalItems.size()));
        adapter.addFragment(HiddenFragment.newInstance(mSortedItems), App.bindString(R.string.tab_hidden, mSortedItems.size()));
        adapter.addFragment(AnalyticsFragment.newInstance(mProject), App.bindString(R.string.tab_extract_analytics));
        adapter.addFragment(new EasterEggFragment(), "");
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if (i == 0) {
                    setEdgeLevel(SwipeBackLayout.EdgeLevel.MED);
                } else {
                    setEdgeLevel(App.dpToPx(1));
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    @SuppressWarnings("JavadocReference")
    private class Inspector extends AsyncTask<String, String, String> {
        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute() called");
            mItems = new ArrayList<>();
            mNormalItems = new ArrayList<>();
            mSortedItems = new ArrayList<>();
            mWaitDialog = new SweetWaitDialog(getContext());
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param strings The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground() called with: strings = [" + Arrays.toString(strings) + "]");
            ArrayMap<Pattern, String> pat = new ArrayMap<Pattern, String>();
            pat.put(Pattern.compile("(const-string [pv]\\d+, (\".*Premium.*|\".*IsPro.*|\".*RemoveAds.*|\".*Free.*|\"pro\"|\"Pro\"|\".*Vip.*\"|\".*Paid.*\"|\".*Purchased.*\"|\".*Subscribed.*|\".*gold.*\"|\".*Ads.*|\".*Gold.*\"|\".*subscribed.*|\".*paid.*|\".*purchased.*\"|\".*premium.*\"|\".*vip.*\"))"), "a");
            inspect(strings[0], pat);
            return "test";
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param s The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute() called with: s = [" + s + "]");
            if (mWaitDialog != null && mWaitDialog.isShowing()) {
                mWaitDialog.dismiss();
                mWaitDialog = null;
            }
            if (mItems.size() != 0) {
                Log.d(TAG, "onPostExecute: items != 0, (" + mItems.size() + ")");
                for (InterestSmaliItem item : mItems) {
                    if (Preferences.hasExcludedPackage(item.getSmaliPath())) {
                        mSortedItems.add(item);
                        SysUtils.Log(String.format("Sorting item %s", item.getSmaliPath()));
                    } else {
                        mNormalItems.add(item);
                    }
                }
                setViewPager(mPager);
            } else
                mNotFound.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (mWaitDialog != null && mWaitDialog.isShowing()) {
                mWaitDialog.setMessage(String.format(Locale.ENGLISH, "%s places found", values[0]));
            }
        }

        public void inspect(String directoryName, ArrayMap<Pattern, String> pat) {
            Log.d(TAG, "inspect() called with: directoryName = [" + directoryName + "], pat = [" + pat + "]");
            File directory = new File(directoryName);
            byte[] bytes;
            BufferedInputStream buf;
            String content;
            Matcher mat;
            File[] fList = directory.listFiles();
            if (fList != null) {
                Log.d(TAG, "inspect: flist != null");
                for (File file : fList) {
                    if (file.isFile()) {
                        if (file.getAbsolutePath().endsWith(".smali")) {
                            Log.d(TAG, "inspect: trying to replace");
                            try {
                                bytes = new byte[(int) file.length()];
                                buf = new BufferedInputStream(new FileInputStream(file));
                                buf.read(bytes, 0, bytes.length);
                                buf.close();
                                content = new String(bytes);
                                for (ArrayMap.Entry<Pattern, String> mEntry : pat.entrySet()) {
                                    mat = mEntry.getKey().matcher(content);
                                    while (mat.find()) {
                                        i++; // не обращай внимания
                                        publishProgress(Integer.toString(i));
                                        mItems.add(new InterestSmaliItem(file.getAbsolutePath(), mat.group()));
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "inspect: error", e);
                            }
                        }
                    } else if (file.isDirectory()) {
                        inspect(file.getAbsolutePath(), pat);
                    }
                }
            }
        }
    }
}