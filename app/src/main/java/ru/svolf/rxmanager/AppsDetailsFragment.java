package ru.svolf.rxmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import apk.tool.patcher.App;
import apk.tool.patcher.R;
import apk.tool.patcher.filesystem.FastFs;
import apk.tool.patcher.util.Cs;
import apk.tool.patcher.util.SystemF;
import apk.tool.patcher.util.TextUtil;
import ru.svolf.melissa.swipeback.SwipeBackFragment;
import ru.svolf.rxmanager.adapter.ExtendedAppAdapter;
import ru.svolf.rxmanager.adapter.SimpleAppAdapter;
import ru.svolf.rxmanager.data.AppInfoItem;

public class AppsDetailsFragment extends SwipeBackFragment {
  public static final String FRAGMENT_TAG = "apps-details-fragment";
  private String mPackageId;
  private boolean isApkFile;

  // Header
  private RelativeLayout appBackground;
  private ImageView appIcon;
  private TextView appLabel;

  // Lists container
  private LinearLayout listsContainer;
  private View buttonsContainer;

  // Recyclers
  private RecyclerView listCommon;
  private RecyclerView listPermissions;
  private RecyclerView listActivities;
  private RecyclerView listServices;
  private RecyclerView listReceivers;
  private RecyclerView listProviders;

  // Buttons
  private ImageView buttonLaunch;
  private ImageView buttonExport;
  private ImageView buttonGPlay;

  // Adapters
  private ArrayList<AppInfoItem> commonItems = new ArrayList<>();
  private ArrayList<AppInfoItem> permissionsItems = new ArrayList<>();
  private ArrayList<AppInfoItem> activityItems = new ArrayList<>();
  private ArrayList<AppInfoItem> servicesItems = new ArrayList<>();
  private ArrayList<AppInfoItem> receiversItems = new ArrayList<>();
  private ArrayList<AppInfoItem> providersItems = new ArrayList<>();

  public static AppsDetailsFragment newInstance(String packageName, @Nullable String apk) {
    AppsDetailsFragment fragment = new AppsDetailsFragment();
    Bundle bundle = new Bundle();
    if (apk != null) {
      bundle.putString(Cs.ARG_APP_INFO, apk);
    } else {
      bundle.putString(Cs.ARG_APP_INFO, packageName);
    }
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    if (getArguments() != null) {
      mPackageId = getArguments().getString(Cs.ARG_APP_INFO);
      isApkFile = mPackageId.contains("/");
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return attachToSwipeBack(inflater.inflate(R.layout.fragment_app_info, container, false));
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    appBackground = view.findViewById(R.id.app_info_header);
    appIcon = appBackground.findViewById(R.id.app_icon);
    appLabel = appBackground.findViewById(R.id.app_name);
    buttonsContainer = appBackground.findViewById(R.id.buttons_container);
    buttonLaunch = appBackground.findViewById(R.id.button_launch);
    buttonExport = appBackground.findViewById(R.id.button_export);
    buttonGPlay = appBackground.findViewById(R.id.button_play);

    if (isApkFile) {
      buttonsContainer.setVisibility(View.INVISIBLE);
    }

    listsContainer = view.findViewById(R.id.lists_container);

    listCommon = listsContainer.findViewById(R.id.list_common);
    listPermissions = listsContainer.findViewById(R.id.list_permissions);
    listActivities = listsContainer.findViewById(R.id.list_activities);
    listServices = listsContainer.findViewById(R.id.list_services);
    listReceivers = listsContainer.findViewById(R.id.list_receivers);
    listProviders = listsContainer.findViewById(R.id.list_providers);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (mPackageId != null) {
      try {
        prepare();
        if (isApkFile) {
          getPackageArchiveInfo(mPackageId);
        } else {
          getPackageIdInfo(mPackageId);
        }
        complete();
      } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
      }
    }
    // Hide keyboard when user tapped an app item from search screen
    SystemF.hideKeyboard(requireActivity());
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      if (requestCode == Cs.REQ_CODE_EXTCARD) {
        extractApk();
      }
    }
  }

  private void prepare() {
    if (!isApkFile) {
      buttonLaunch.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              final Intent intent =
                  App.get().getPackageManager().getLaunchIntentForPackage(mPackageId);
              if (intent != null) {
                startActivity(intent);
              } else {
                Toast.makeText(getContext(), "Cannot launch", Toast.LENGTH_LONG).show();
              }
            }
          });

      buttonExport.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (ContextCompat.checkSelfPermission(
                      getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                  == PackageManager.PERMISSION_GRANTED) {
                extractApk();
              } else {
                ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[] {
                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    Cs.REQ_CODE_EXTCARD);
              }
            }
          });

      buttonGPlay.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              TextUtil.goLink(
                  getActivity(), "https://play.google.com/store/apps/details?id=" + mPackageId);
            }
          });
    }

    listCommon.setLayoutManager(new GridLayoutManager(getContext(), 2));
    listActivities.setLayoutManager(new LinearLayoutManager(getContext()));
    listPermissions.setLayoutManager(new LinearLayoutManager(getContext()));
    listServices.setLayoutManager(new LinearLayoutManager(getContext()));
    listReceivers.setLayoutManager(new LinearLayoutManager(getContext()));
    listProviders.setLayoutManager(new LinearLayoutManager(getContext()));

    listCommon.setNestedScrollingEnabled(false);
    listActivities.setNestedScrollingEnabled(false);
    listPermissions.setNestedScrollingEnabled(false);
    listServices.setNestedScrollingEnabled(false);
    listReceivers.setNestedScrollingEnabled(false);
    listProviders.setNestedScrollingEnabled(false);
  }

  private void extractApk() {
    final PackageManager manager = getContext().getPackageManager();
    try {
      final PackageInfo appInfo = manager.getPackageInfo(mPackageId, PackageManager.GET_META_DATA);
      final File test = new File(appInfo.applicationInfo.sourceDir);
      if (test.exists()) {
        final File doc =
            new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS);
        if (!doc.exists()) {
          doc.mkdirs();
        }
        final File apk =
            new File(
                String.format(
                    "%s/%s [%s].apk",
                    doc, appInfo.applicationInfo.loadLabel(manager), appInfo.versionName));
        FastFs.copyFile(AppsDetailsFragment.this, test, apk);
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void getPackageIdInfo(String packageId) throws PackageManager.NameNotFoundException {
    final PackageManager manager = getContext().getPackageManager();

    final PackageInfo appInfo = manager.getPackageInfo(packageId, PackageManager.GET_META_DATA);
    getMetaInfo(appInfo);

    final PackageInfo permissionsInfo =
        manager.getPackageInfo(packageId, PackageManager.GET_PERMISSIONS);
    getPermissions(permissionsInfo);

    final PackageInfo activitiesInfo =
        manager.getPackageInfo(packageId, PackageManager.GET_ACTIVITIES);
    getActivities(activitiesInfo);

    final PackageInfo servicesInfo = manager.getPackageInfo(packageId, PackageManager.GET_SERVICES);
    getServices(servicesInfo);

    final PackageInfo receiversInfo =
        manager.getPackageInfo(packageId, PackageManager.GET_RECEIVERS);
    getReceivers(receiversInfo);

    final PackageInfo providersInfo =
        manager.getPackageInfo(packageId, PackageManager.GET_PROVIDERS);
    getProviders(providersInfo);
  }

  private void getPackageArchiveInfo(String packageId) {
    final PackageManager manager = getContext().getPackageManager();

    final PackageInfo appInfo =
        manager.getPackageArchiveInfo(packageId, PackageManager.GET_META_DATA);
    getMetaInfo(appInfo);

    final PackageInfo permissionsInfo =
        manager.getPackageArchiveInfo(packageId, PackageManager.GET_PERMISSIONS);
    getPermissions(permissionsInfo);

    final PackageInfo activitiesInfo =
        manager.getPackageArchiveInfo(packageId, PackageManager.GET_ACTIVITIES);
    getActivities(activitiesInfo);

    final PackageInfo servicesInfo =
        manager.getPackageArchiveInfo(packageId, PackageManager.GET_SERVICES);
    getServices(servicesInfo);

    final PackageInfo receiversInfo =
        manager.getPackageArchiveInfo(packageId, PackageManager.GET_RECEIVERS);
    getReceivers(receiversInfo);

    final PackageInfo providersInfo =
        manager.getPackageArchiveInfo(packageId, PackageManager.GET_PROVIDERS);
    getProviders(providersInfo);
  }

  private void getMetaInfo(PackageInfo info) {
    if (info != null) {
      appLabel.setText(info.applicationInfo.loadLabel(getContext().getPackageManager()));
      if (!isApkFile) {
        appIcon.setImageDrawable(info.applicationInfo.loadIcon(getContext().getPackageManager()));
      } else {
        info.applicationInfo.publicSourceDir = mPackageId;
        appIcon.setImageDrawable(info.applicationInfo.loadIcon(getContext().getPackageManager()));
      }

      commonItems.add(new AppInfoItem(getString(R.string.appinfo_pkg_name)));
      commonItems.add(new AppInfoItem(info.packageName));

      commonItems.add(new AppInfoItem(getString(R.string.appinfo_ver)));
      commonItems.add(new AppInfoItem(info.versionName));

      commonItems.add(new AppInfoItem(getString(R.string.appinfo_ver_code)));
      commonItems.add(new AppInfoItem(Integer.toString(info.versionCode)));

      if (!isApkFile) {
        commonItems.add(new AppInfoItem("UID"));
        commonItems.add(new AppInfoItem(Integer.toString(info.applicationInfo.uid)));
      }

      commonItems.add(new AppInfoItem(getString(R.string.appinfo_msdk)));
      commonItems.add(new AppInfoItem(Integer.toString(info.applicationInfo.minSdkVersion)));

      commonItems.add(new AppInfoItem(getString(R.string.appinfo_tsdk)));
      commonItems.add(new AppInfoItem(Integer.toString(info.applicationInfo.targetSdkVersion)));

      if (!isApkFile) {
        commonItems.add(new AppInfoItem(getString(R.string.appinfo_apk_dir)));
        commonItems.add(new AppInfoItem(info.applicationInfo.sourceDir));

        commonItems.add(new AppInfoItem(getString(R.string.appinfo_pkg_data)));
        commonItems.add(new AppInfoItem(info.applicationInfo.dataDir));
      }
    }
  }

  private void getActivities(PackageInfo info) {
    if (info.activities != null) {
      for (ActivityInfo activityInfo : info.activities) {
        activityItems.add(new AppInfoItem(activityInfo.name));
      }
    } else {
      activityItems.add(new AppInfoItem(getString(R.string.no_data)));
    }
  }

  private void getPermissions(PackageInfo info) {
    if (info != null) {
      if (info.requestedPermissions == null && info.permissions == null) {
        permissionsItems.add(new AppInfoItem(getString(R.string.no_data)));
      } else {
        // Получение списка системных разрешений (те, которые в манифесте объявлены)
        if (info.requestedPermissions != null) {
          for (String perm : info.requestedPermissions) {
            permissionsItems.add(new AppInfoItem(perm));
          }
        }
        // Получение списка разрешений (всякие C2D_MESSAGE для гугловских пушей)
        if (info.permissions != null) {
          for (PermissionInfo permissionInfo : info.permissions) {
            permissionsItems.add(new AppInfoItem(permissionInfo.name));
          }
        }
      }
    }
  }

  private void getServices(PackageInfo info) {
    if (info != null) {
      if (info.services != null) {
        for (ServiceInfo serviceInfo : info.services) {
          servicesItems.add(new AppInfoItem(serviceInfo.name));
        }
      } else {
        servicesItems.add(new AppInfoItem(getString(R.string.no_data)));
      }
    }
  }

  private void getReceivers(PackageInfo info) {
    if (info != null) {
      if (info.receivers != null) {
        for (ActivityInfo activityInfo : info.receivers) {
          receiversItems.add(new AppInfoItem(activityInfo.name));
        }
      } else {
        receiversItems.add(new AppInfoItem(getString(R.string.no_data)));
      }
    }
  }

  private void getProviders(PackageInfo info) {
    if (info != null) {
      if (info.providers != null) {
        for (ProviderInfo providerInfo : info.providers) {
          providersItems.add(new AppInfoItem(providerInfo.name));
        }
      } else {
        providersItems.add(new AppInfoItem(getString(R.string.no_data)));
      }
    }
  }

  private void complete() {
    listCommon.setAdapter(new ExtendedAppAdapter(commonItems));
    listPermissions.setAdapter(new SimpleAppAdapter(permissionsItems));
    listActivities.setAdapter(new SimpleAppAdapter(activityItems));
    listServices.setAdapter(new SimpleAppAdapter(servicesItems));
    listReceivers.setAdapter(new SimpleAppAdapter(receiversItems));
    listProviders.setAdapter(new SimpleAppAdapter(providersItems));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    commonItems = null;
    permissionsItems = null;
    activityItems = null;
    servicesItems = null;
    receiversItems = null;
  }
}
