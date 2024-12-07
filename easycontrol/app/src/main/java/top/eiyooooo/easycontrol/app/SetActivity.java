package top.eiyooooo.easycontrol.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import top.eiyooooo.easycontrol.app.entity.AppData;
import top.eiyooooo.easycontrol.app.helper.PublicTools;
import top.eiyooooo.easycontrol.app.databinding.ActivitySetBinding;
import top.eiyooooo.easycontrol.app.helper.ShortcutHelper;

public class SetActivity extends Activity {
  private ActivitySetBinding setActivity;

  private static final ArrayAdapter<String> audioChannelAdapter = new ArrayAdapter<>(AppData.main, R.layout.item_spinner_item, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"});
  private static final ArrayAdapter<String> reconnectTimeAdapter = new ArrayAdapter<>(AppData.main, R.layout.item_spinner_item, new String[]{AppData.main.getString(R.string.set_no_auto_countdown), "3", "5", "10"});

  private static final ArrayAdapter<String> orientationAdapter = new ArrayAdapter<>(AppData.main, R.layout.item_spinner_item, new String[]{"Not set", "Portrait", "Landscape", "Reverse Landscape", "Reverse Portrait"});

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PublicTools.setStatusAndNavBar(this);
    PublicTools.setLocale(this);
    setActivity = ActivitySetBinding.inflate(this.getLayoutInflater());
    setContentView(setActivity.getRoot());
    // 设置页面
    drawUi();
    setButtonListener();
  }

  // 设置默认值
  private void drawUi() {
    // 默认参数
    PublicTools.createDeviceOptionSet(this, setActivity.setDefault, null);
    // 显示
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_wake_up_screen_on_connect), getString(R.string.set_wake_up_screen_on_connect_detail), AppData.setting.getTurnOnScreenIfStart(),
            isChecked -> {
              if (!isChecked) {
                setActivity.setDisplay.removeViewAt(1);
                AppData.setting.setTurnOffScreenIfStart(false);
                setActivity.setDisplay.addView(PublicTools.createSwitchCardEx(this, getString(R.string.set_light_off_on_connect), getString(R.string.set_light_off_on_connect_detail), AppData.setting.getTurnOffScreenIfStart(),
                        (buttonView, isChecked1) -> {
                          if (!AppData.setting.getTurnOnScreenIfStart()) {
                            buttonView.setChecked(false);
                            Toast.makeText(this, getString(R.string.set_light_off_on_connect_error), Toast.LENGTH_SHORT).show();
                          }
                          else AppData.setting.setTurnOffScreenIfStart(isChecked1);
                        }).getRoot(), 1);
              }
              AppData.setting.setTurnOnScreenIfStart(isChecked);
            }).getRoot());

    setActivity.setDisplay.addView(PublicTools.createSwitchCardEx(this, getString(R.string.set_light_off_on_connect), getString(R.string.set_light_off_on_connect_detail), AppData.setting.getTurnOffScreenIfStart(),
            (buttonView, isChecked) -> {
              if (!AppData.setting.getTurnOnScreenIfStart()) {
                buttonView.setChecked(false);
                Toast.makeText(this, getString(R.string.set_light_off_on_connect_error), Toast.LENGTH_SHORT).show();
              }
              else AppData.setting.setTurnOffScreenIfStart(isChecked);
            }).getRoot());

    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_lock_screen_on_close), getString(R.string.set_lock_screen_on_close_detail), AppData.setting.getTurnOffScreenIfStop(),
            isChecked -> {
              if (isChecked) {
                setActivity.setDisplay.removeViewAt(3);
                AppData.setting.setTurnOnScreenIfStop(false);
                setActivity.setDisplay.addView(PublicTools.createSwitchCardEx(this, getString(R.string.set_light_on_on_close), getString(R.string.set_light_on_on_close_detail), AppData.setting.getTurnOnScreenIfStop(),
                        (buttonView, isChecked1) -> {
                          if (AppData.setting.getTurnOffScreenIfStop()) {
                            buttonView.setChecked(false);
                            Toast.makeText(this, getString(R.string.set_light_on_on_close_error), Toast.LENGTH_SHORT).show();
                          }
                          else AppData.setting.setTurnOnScreenIfStop(isChecked1);
                        }).getRoot(), 3);
              }
              AppData.setting.setTurnOffScreenIfStop(isChecked);
            }).getRoot());

    setActivity.setDisplay.addView(PublicTools.createSwitchCardEx(this, getString(R.string.set_light_on_on_close), getString(R.string.set_light_on_on_close_detail), AppData.setting.getTurnOnScreenIfStop(),
            (buttonView, isChecked) -> {
              if (AppData.setting.getTurnOffScreenIfStop()) {
                buttonView.setChecked(false);
                Toast.makeText(this, getString(R.string.set_light_on_on_close_error), Toast.LENGTH_SHORT).show();
              }
              else AppData.setting.setTurnOnScreenIfStop(isChecked);
            }).getRoot());

    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_keep_screen_awake), getString(R.string.set_display_keep_screen_awake_detail), AppData.setting.getKeepAwake(), isChecked -> AppData.setting.setKeepAwake(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_auto_back_on_start_default), getString(R.string.set_display_auto_back_on_start_default_detail), AppData.setting.getAutoBackOnStartDefault(), isChecked -> AppData.setting.setAutoBackOnStartDefault(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_default_mini_on_outside), getString(R.string.set_display_default_mini_on_outside_detail), AppData.setting.getDefaultMiniOnOutside(), isChecked -> AppData.setting.setDefaultMiniOnOutside(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_mini_recover_on_timeout), getString(R.string.set_display_mini_recover_on_timeout_detail), AppData.setting.getMiniRecoverOnTimeout(), isChecked -> AppData.setting.setMiniRecoverOnTimeout(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_full_to_mini_on_exit), getString(R.string.set_display_full_to_mini_on_exit_detail), AppData.setting.getFullToMiniOnExit(), isChecked -> AppData.setting.setFullToMiniOnExit(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_full_fill), getString(R.string.set_display_full_fill_detail), AppData.setting.getFillFull(), isChecked -> AppData.setting.setFillFull(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_default_show_nav_bar), getString(R.string.set_display_default_show_nav_bar_detail), AppData.setting.getDefaultShowNavBar(), isChecked -> AppData.setting.setDefaultShowNavBar(isChecked)).getRoot());

    setActivity.setDisplay.addView(PublicTools.createSpinnerCard(this, getString(R.string.set_display_default_orientation), getString(R.string.set_display_default_orientation_detail), AppData.setting.getDefaultOrientation(), orientationAdapter, str -> AppData.setting.setDefaultOrientation(str)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_default_lock_orientation), getString(R.string.set_display_default_lock_orientation_detail), AppData.setting.getDefaultLockOrientation(), isChecked -> AppData.setting.setDefaultLockOrientation(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_can_drag_button_more), getString(R.string.set_display_can_drag_button_more_detail), AppData.setting.getCanDragButtonMore(), isChecked -> AppData.setting.setCanDragButtonMore(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_not_full_to_mini_on_exit), getString(R.string.set_display_not_full_to_mini_on_exit_detail), AppData.setting.getNotFullToMiniOnExit(), isChecked -> AppData.setting.setNotFullToMiniOnExit(isChecked)).getRoot());
    setActivity.setDisplay.addView(PublicTools.createSwitchCard(this, getString(R.string.set_display_nav_bar_to_right), getString(R.string.set_display_nav_bar_to_right_detail), AppData.setting.getNavBarToRight(), isChecked -> AppData.setting.setNavBarToRight(isChecked)).getRoot());
    // 其他
    setActivity.setOther.addView(PublicTools.createSpinnerCard(this, getString(R.string.set_audio_channel), getString(R.string.set_audio_channel_detail), String.valueOf(AppData.setting.getAudioChannel()), audioChannelAdapter, str -> AppData.setting.setAudioChannel(Integer.parseInt(str))).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_enable_usb), getString(R.string.set_enable_usb_detail), AppData.setting.getEnableUSB(), isChecked -> AppData.setting.setEnableUSB(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_set_full_screen), getString(R.string.set_set_full_screen_detail), AppData.setting.getSetFullScreen(), isChecked -> AppData.setting.setSetFullScreen(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_always_full_mode), getString(R.string.set_always_full_mode_detail), AppData.setting.getAlwaysFullMode(), isChecked -> AppData.setting.setAlwaysFullMode(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_mirror_mode), getString(R.string.set_mirror_mode_detail), AppData.setting.getNewMirrorMode(), isChecked -> AppData.setting.setNewMirrorMode(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_force_desktop_mode), getString(R.string.set_force_desktop_mode_detail), AppData.setting.getForceDesktopMode(), isChecked -> AppData.setting.setForceDesktopMode(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_try_start_default_in_app_transfer), getString(R.string.set_try_start_default_in_app_transfer_detail), AppData.setting.getTryStartDefaultInAppTransfer(), isChecked -> AppData.setting.setTryStartDefaultInAppTransfer(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_reconnect), getString(R.string.set_reconnect_detail), AppData.setting.getShowReconnect(), isChecked -> AppData.setting.setShowReconnect(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSwitchCard(this, getString(R.string.set_connect_usb), getString(R.string.set_connect_usb_detail), AppData.setting.getShowConnectUSB(), isChecked -> AppData.setting.setShowConnectUSB(isChecked)).getRoot());
    setActivity.setOther.addView(PublicTools.createSpinnerCard(this, getString(R.string.set_auto_countdown), getString(R.string.set_auto_countdown_detail), AppData.setting.getCountdownTime(), reconnectTimeAdapter, str -> AppData.setting.setCountdownTime(str)).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCardDetail(this, getString(R.string.set_app_monitor), getString(R.string.set_app_monitor_detail), () -> startActivity(new Intent(this, MonitorActivity.class))).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCard(this, getString(R.string.set_create_startup_shortcut), () -> ShortcutHelper.addShortcut(AppData.main, StartDeviceActivity.class, getString(R.string.tip_default_device), R.drawable.phones, null)).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCard(this, getString(R.string.set_other_log), () -> startActivity(new Intent(this, LogActivity.class))).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCard(this, getString(R.string.set_about_ip), () -> startActivity(new Intent(this, IpActivity.class))).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCard(this, getString(R.string.set_other_custom_key), () -> startActivity(new Intent(this, AdbKeyActivity.class))).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCard(this, getString(R.string.set_other_clear_key), () -> {
      AppData.reGenerateAdbKeyPair(this);
      Toast.makeText(this, getString(R.string.set_other_clear_key_code), Toast.LENGTH_SHORT).show();
    }).getRoot());
    setActivity.setOther.addView(PublicTools.createTextCard(this, getString(R.string.set_other_locale), () -> {
      String locale = AppData.setting.getDefaultLocale();
      if (locale.isEmpty()) {
        if (String.valueOf(setActivity.textAbout.getText()).contains("关于")) AppData.setting.setDefaultLocale("en");
        else AppData.setting.setDefaultLocale("zh");
      }
      else if (locale.equals("en")) AppData.setting.setDefaultLocale("zh");
      else if (locale.equals("zh")) AppData.setting.setDefaultLocale("en");
      Toast.makeText(this, getString(R.string.set_other_locale_code), Toast.LENGTH_SHORT).show();
    }).getRoot());
    // 关于
    setActivity.setAbout.addView(PublicTools.createTextCard(this, getString(R.string.set_about_website), () -> PublicTools.startUrl(this, "https://github.com/eiyooooo/Easycontrol_For_Car")).getRoot());
    setActivity.setAbout.addView(PublicTools.createTextCard(this, getString(R.string.set_about_how_to_use), () -> PublicTools.openWebViewActivity(this, "file:///android_asset/usage.html")).getRoot());
    setActivity.setAbout.addView(PublicTools.createTextCard(this, getString(R.string.set_about_privacy), () -> PublicTools.startUrl(this, "https://github.com/eiyooooo/Easycontrol_For_Car/blob/main/PRIVACY.md")).getRoot());
    setActivity.setAbout.addView(PublicTools.createTextCard(this, getString(R.string.set_license), () -> PublicTools.openWebViewActivity(this, "file:///android_asset/license.html")).getRoot());
    setActivity.setAbout.addView(PublicTools.createTextCard(this, getString(R.string.set_about_version) + BuildConfig.VERSION_NAME, () -> PublicTools.startUrl(this, "https://github.com/eiyooooo/Easycontrol_For_Car/releases")).getRoot());
    setActivity.setAbout.addView(PublicTools.createTextCard(this, getString(R.string.car_version_message), () -> PublicTools.startUrl(this, "https://github.com/eiyooooo/Easycontrol_For_Car")).getRoot());
  }

  // 设置按钮监听
  private void setButtonListener() {
    setActivity.backButton.setOnClickListener(v -> finish());
  }
}
