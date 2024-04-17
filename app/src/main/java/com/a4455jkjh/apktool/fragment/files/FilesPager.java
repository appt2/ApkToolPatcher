package com.a4455jkjh.apktool.fragment.files;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a4455jkjh.apktool.fragment.FilesFragment;
import com.a4455jkjh.apktool.util.PopupUtils;
import com.a4455jkjh.apktool.util.Settings;

import java.io.File;

import apk.tool.patcher.R;
import apk.tool.patcher.util.PathF;
import ru.svolf.melissa.widget.crumb.CrumbAdapter;
import ru.svolf.melissa.widget.crumb.CrumbView;
import ru.svolf.melissa.widget.crumb.PathItem;

public class FilesPager implements WatchDog {
  private final View view;
  private final Context ctx;
  private final CharSequence title;
  private RecyclerView files;
  private ModernFilesAdapter adapter;
  private Toolbar toolbar;
  private CrumbView pathView;
  private String openedPath;

  public FilesPager(Context context) {
    ctx = context;
    LayoutInflater inflater = LayoutInflater.from(context);
    view = inflater.inflate(R.layout.files, null);
    title = context.getText(R.string.files);
    files = view.findViewById(R.id.files);
    files.setLayoutManager(new LinearLayoutManager(context));
    toolbar = view.findViewById(R.id.toolbar);
    pathView = view.findViewById(R.id.breadcrumbs_view);

    toolbar.inflateMenu(R.menu.menu_files_caption);
    toolbar.setOnMenuItemClickListener(
        new Toolbar.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.more) {
              menu(toolbar);
              return true;
            } else if (item.getItemId() == R.id.refresh) {
              adapter.refresh();
              return true;
            } else {
              return false;
            }
          }
        });

    initPathView();
  }

  public void init(Bundle savedInstanceState, FilesFragment frag) {
    adapter = ModernFilesAdapter.init(frag, files, this);
    adapter.init(savedInstanceState);
  }

  private void initPathView() {
    pathView.sync(Environment.getExternalStorageDirectory().getPath());
  }

  public ModernFilesAdapter getAdapter() {
    return adapter;
  }

  public void save(Bundle outState) {
    adapter.save(outState);
  }

  public CharSequence getTitle() {
    return title;
  }

  public View getView() {
    return view;
  }

  private void menu(final View view) {
    PopupUtils.show(
        view,
        R.menu.dir,
        new PopupUtils.Callback() {
          @Override
          public void call(Context ctx, int pos) {
            if (pos == R.id.go_back) {
              adapter.goBack();
            } else if (pos == R.id.sort) {
              sort();
            } else if (pos == R.id.new_dir || pos == R.id.new_file) {
              adapter.createFileOrDir(pos);
            } else if (pos == R.id.set_as_output_directory) {
              Settings.setOutputDirectory(openedPath, toolbar.getContext());
            }
          }
        });
  }

  protected void sort() {
    new AlertDialog.Builder(ctx)
        .setTitle(R.string.sort)
        .setItems(
            R.array.sort,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface p1, int p2) {
                FileComparator.setDefaultAdapter(p2);
                adapter.refresh();
                PreferenceManager.getDefaultSharedPreferences(ctx)
                    .edit()
                    .putInt("defaultCompator", p2)
                    .apply();
              }
            })
        .setCancelable(false)
        .show();
  }

  @Override
  public void watchForFile(CharSequence path) {
    openedPath = path.toString();
    String currentPath = PathF.pointToName(path.toString());
    toolbar.setTitle(currentPath);
    pathView.sync(path.toString());
    pathView.setOnItemClickListener(
        new CrumbAdapter.OnItemClickListener() {
          @Override
          public void onItemClick(PathItem pathItem, int position) {
            adapter.refresh(new File(pathItem.getPath()));
          }
        });
  }
}
