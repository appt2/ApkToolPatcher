package apk.tool.patcher.ui.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.StyleRes;

import apk.tool.patcher.App;
import apk.tool.patcher.R;

/** Created by Snow Volf on 02.09.2017, 12:12 */
public abstract class ThemeWrapper {
  /** Применяем тему активити */
  public static void applyTheme(Activity ctx) {
    int theme;
    switch (Theme.values()[getThemeIndex()]) {
      case DARK:
        theme = R.style.AppTheme_Dark;
        break;
      case BLUE:
        theme = R.style.AppTheme_Blue;
        break;
      default:
        theme = R.style.AppTheme;
        break;
    }
    ctx.setTheme(theme);
  }

  /** Получаем индекс темы из настроек */
  private static int getThemeIndex() {
    return Integer.parseInt(
        App.get()
            .getPreferences()
            .getString("ui.theme", String.valueOf(ThemeWrapper.Theme.LIGHT.ordinal())));
  }

  public static boolean isLightTheme() {
    return getThemeIndex() == Theme.LIGHT.ordinal();
  }

  @StyleRes
  public static int getDialogTheme() {
    int theme = 0;
    switch (Theme.values()[getThemeIndex()]) {
      case DARK:
        theme = R.style.DarkAppTheme_Dialog;
        break;
      case BLUE:
        theme = R.style.BlueAppTheme_Dialog;
        break;
    }
    return theme;
  }

  @StyleRes
  public static int getBottomDialogTheme() {
    int theme;
    switch (Theme.values()[getThemeIndex()]) {
      case DARK:
        theme = R.style.AppBottomSheetDialogTheme_Dark;
        break;
      case BLUE:
        theme = R.style.AppBottomSheetDialogTheme_Dark;
        break;
      default:
        theme = R.style.AppBottomSheetDialogTheme;
    }
    return theme;
  }

  public static int resolveNavBarColor(Context context) {
    // До Android 8 у нас нет возможности сделать кнопки навигации черными,
    // Поэтому делаем черной всю панель целиком
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && isLightTheme()) {
      return Color.BLACK;
    } else {
      return App.getColorFromAttr(context, com.google.android.material.R.attr.colorPrimary);
    }
  }

  /** Список тем */
  public enum Theme {
    LIGHT,
    DARK,
    BLUE
  }
}
