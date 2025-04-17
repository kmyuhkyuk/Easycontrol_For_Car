package top.eiyooooo.easycontrol.app.client.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

import top.eiyooooo.easycontrol.app.helper.PublicTools;
import top.eiyooooo.easycontrol.app.R;
import top.eiyooooo.easycontrol.app.client.Client;
import top.eiyooooo.easycontrol.app.databinding.ActivityFullBinding;
import top.eiyooooo.easycontrol.app.entity.AppData;

public class FullActivity extends Activity implements SensorEventListener {
  private ClientView clientView;
  private ActivityFullBinding fullActivity;

  private Client client;

  private boolean isChangeView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fullActivity = ActivityFullBinding.inflate(this.getLayoutInflater());
    setContentView(fullActivity.getRoot());
    try {
      client = Client.allClient.get(getIntent().getIntExtra("index", 0));
      if (client.isClosed()) throw new Exception();
      if (client.clientView.textureView.getParent() != null) client.clientView.hide(false);
    } catch (Exception ignored) {
      finish();
      return;
    }
    clientView = client.clientView;
    clientView.setFullView(this);

    if (nextOrientationData != null) {
      isFirstOrientationEvent = nextOrientationData.isFirstOrientationEvent;
      lockOrientation = nextOrientationData.lockOrientation;
      lastOrientation = nextOrientationData.lastOrientation;

      nextOrientationData = null;
    }
    else {
      lockOrientation = AppData.setting.getDefaultLockOrientation();
    }

    setNavBarOrientation();

    // 监听
    setButtonListener();
    setMoreListener();
    setKeyEvent();
    fullActivity.textureViewLayout.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)->{
      if (left == oldLeft && top == oldTop && right == oldRight && bottom == oldBottom) return;
      updateMaxSize();
    });
    // 更新textureView
    fullActivity.textureViewLayout.addView(clientView.textureView, 0);
    setNavBarHide(AppData.setting.getDefaultShowNavBar());
    changeMode(-clientView.mode);
    // 页面自动旋转
    AppData.sensorManager.registerListener(this, AppData.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
  }

  private void setNavBarOrientation() {
    if (AppData.setting.getNavBarToRight() && (lastOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || lastOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)) {
      fullActivity.navBar.bringToFront();
      FrameLayout.LayoutParams buttonMoreParams = (FrameLayout.LayoutParams)fullActivity.buttonMore.getLayoutParams();
      buttonMoreParams.gravity = Gravity.END;
      fullActivity.buttonMore.setLayoutParams(buttonMoreParams);
      FrameLayout.LayoutParams barViewParams = (FrameLayout.LayoutParams)fullActivity.barView.getLayoutParams();
      barViewParams.gravity = Gravity.END;
      barViewParams.rightMargin = 100;
      fullActivity.barView.setLayoutParams(barViewParams);
    }
  }

  public Pair<Integer, Integer> fullMaxSize;

  private void updateMaxSize() {
    fullMaxSize = new Pair<>(fullActivity.textureViewLayout.getMeasuredWidth(), fullActivity.textureViewLayout.getMeasuredHeight());
    clientView.updateMaxSize(fullMaxSize);
    if (clientView.mode == 1 && clientView.device.setResolution)
      clientView.changeSize((float) fullMaxSize.first / (float) fullMaxSize.second);
  }

  @Override
  protected void onPause() {
    AppData.sensorManager.unregisterListener(this);
    if (isChangingConfigurations()) fullActivity.textureViewLayout.removeView(clientView.textureView);
    else if (!client.isClosed() && AppData.setting.getNotFullToMiniOnExit()) {
      if (!isChangeView) {
        client.enableAudio(false);
      }
      else {
        isChangeView = false;
      }
    }
    else if (clientView != null) {
      if (AppData.setting.getFullToMiniOnExit()) clientView.changeToMini(2);
      else clientView.changeToSmall();
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    if (AppData.setting.getSetFullScreen()) PublicTools.setFullScreen(this);
    if (clientView.volume) client.enableAudio(true);
    super.onResume();
  }

  @Override
  public void onBackPressed() {
    Toast.makeText(AppData.main, getString(R.string.error_refused_back), Toast.LENGTH_SHORT).show();
  }

  public void hide() {
    try {
      fullActivity.textureViewLayout.removeView(clientView.textureView);
      clientView.setFullView(null);
      clientView = null;
      finish();
    } catch (Exception ignored) {
    }
  }

  public void changeMode(int mode) {
    fullActivity.buttonSwitch.setVisibility(mode == 0 ? View.VISIBLE : View.INVISIBLE);
    fullActivity.buttonHome.setVisibility(mode == 0 ? View.VISIBLE : View.INVISIBLE);
    if (mode == 0) fullActivity.buttonTransfer.setImageResource(R.drawable.share_out);
    else fullActivity.buttonTransfer.setImageResource(R.drawable.share_in);
    if (mode > 0 && clientView.mode == 1 && clientView.device.setResolution) {
      updateMaxSize();
    }
  }

  // 设置按钮监听
  private void setButtonListener() {
    fullActivity.buttonRotate.setOnClickListener(v -> {
      clientView.controlPacket.sendRotateEvent();
      barViewTimer();
    });
    fullActivity.buttonBack.setOnClickListener(v -> clientView.controlPacket.sendKeyEvent(4, 0, -1));
    fullActivity.buttonHome.setOnClickListener(v -> clientView.controlPacket.sendKeyEvent(3, 0, -1));
    fullActivity.buttonSwitch.setOnClickListener(v -> clientView.controlPacket.sendKeyEvent(187, 0, -1));
    fullActivity.buttonNavBar.setOnClickListener(v -> {
      setNavBarHide(fullActivity.navBar.getVisibility() == View.GONE);
      barViewTimer();
    });
    if (!AppData.setting.getAlwaysFullMode()) {
      fullActivity.buttonMini.setOnClickListener(v -> {
        clientView.changeToMini(0);
        isChangeView = true;
      });
      fullActivity.buttonFullExit.setOnClickListener(v -> {
        clientView.changeToSmall();
        isChangeView = true;
      });
    } else {
      fullActivity.buttonMini.setOnClickListener(v -> PublicTools.logToast(getString(R.string.error_mode_not_support)));
      fullActivity.buttonFullExit.setOnClickListener(v -> PublicTools.logToast(getString(R.string.error_mode_not_support)));
    }
    fullActivity.buttonClose.setOnClickListener(v -> clientView.onClose.run());
    if (clientView.mode == 1) fullActivity.buttonTransfer.setImageResource(R.drawable.share_in);
    fullActivity.buttonTransfer.setOnClickListener(v -> {
      clientView.changeMode.run(clientView.mode == 0 ? 1 : 0);
      barViewTimer();
    });
    if (!clientView.lightState) fullActivity.buttonLightOff.setImageResource(R.drawable.lightbulb);
    fullActivity.buttonLightOff.setOnClickListener(v -> {
      if (clientView.lightState) {
        clientView.controlPacket.sendLightEvent(Display.STATE_UNKNOWN);
        fullActivity.buttonLightOff.setImageResource(R.drawable.lightbulb);
        clientView.lightState = false;
      } else {
        clientView.controlPacket.sendLightEvent(Display.STATE_ON);
        fullActivity.buttonLightOff.setImageResource(R.drawable.lightbulb_off);
        clientView.lightState = true;
      }
      barViewTimer();
    });
    fullActivity.buttonPower.setOnClickListener(v -> {
      clientView.controlPacket.sendPowerEvent();
      barViewTimer();
    });
    if (AppData.setting.getAlwaysFullMode()) {
      lockOrientation = true;
      fullActivity.buttonLock.setImageResource(R.drawable.unlock);
      fullActivity.buttonLock.setOnClickListener(v -> PublicTools.logToast(getString(R.string.error_mode_not_support)));
      DisplayMetrics metrics = getResources().getDisplayMetrics();
      int orientation;
      if (metrics.widthPixels > metrics.heightPixels) orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
      else orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
      setRequestedOrientation(orientation);
      lastOrientation = orientation;
    } else {
      fullActivity.buttonLock.setImageResource(lockOrientation ? R.drawable.unlock : R.drawable.lock);
      fullActivity.buttonLock.setOnClickListener(v -> {
        lockOrientation = !lockOrientation;
        fullActivity.buttonLock.setImageResource(lockOrientation ? R.drawable.unlock : R.drawable.lock);
        barViewTimer();
      });
    }
    fullActivity.buttonKeyboard.setOnClickListener(v -> clientView.showKeyboard());
    fullActivity.buttonPaste.setOnClickListener(v -> clientView.pasteClipboard());
    if (!clientView.volume) fullActivity.buttonVolumeOff.setImageResource(R.drawable.volume_up_24px);
    fullActivity.buttonVolumeOff.setOnClickListener(v -> {
      if (clientView.volume) {
        clientView.changeEnableAudio.run(false);
        fullActivity.buttonVolumeOff.setImageResource(R.drawable.volume_up_24px);
        clientView.volume = false;
      } else {
        clientView.changeEnableAudio.run(true);
        fullActivity.buttonVolumeOff.setImageResource(R.drawable.volume_off_24px);
        clientView.volume = true;
      }
    });
  }

  //设置展开导航栏监听控制
  @SuppressLint("ClickableViewAccessibility")
  private void setMoreListener(){
    if (AppData.setting.getCanDragButtonMore()) {
      AtomicInteger xx = new AtomicInteger();
      AtomicInteger yy = new AtomicInteger();

      GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
          changeBarView();
          barViewTimer();

          return true;
        }
      });

      fullActivity.buttonMore.setOnTouchListener((view, motionEvent) -> {
        gestureDetector.onTouchEvent(motionEvent);

        switch (motionEvent.getActionMasked()){
          case MotionEvent.ACTION_DOWN:
            xx.set((int)motionEvent.getX());
            yy.set((int)motionEvent.getY());
            break;
          case MotionEvent.ACTION_MOVE:
            float moveX;
            float moveY;
            moveX = motionEvent.getX();
            moveY = motionEvent.getY();

            float distanceX = moveX - xx.get();
            float distanceY = moveY - yy.get();
            view.setX(view.getX() + distanceX);
            view.setY(view.getY() + distanceY);
            break;
        }

        return true;
      });
    }
    else {
      fullActivity.buttonMore.setOnClickListener(v -> {
        changeBarView();
        barViewTimer();
      });
    }
  }

  // 导航栏隐藏
  private void setNavBarHide(boolean isShow) {
    fullActivity.navBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    fullActivity.buttonNavBar.setImageResource(isShow ? R.drawable.not_equal : R.drawable.equals);
  }

  private void changeBarView() {
    if (clientView == null) return;
    boolean toShowView = fullActivity.barView.getVisibility() == View.GONE;
    boolean isLandscape = lastOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || lastOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    clientView.viewAnim(fullActivity.barView, toShowView, 0, PublicTools.dp2px(40f) * (isLandscape ? -1 : 1), (isStart -> {
      if (isStart && toShowView) fullActivity.barView.setVisibility(View.VISIBLE);
      else if (!isStart && !toShowView) fullActivity.barView.setVisibility(View.GONE);
    }));
  }

  private Thread barViewTimerThread = null;
  private void barViewTimer() {
    if (barViewTimerThread != null) barViewTimerThread.interrupt();
    barViewTimerThread = new Thread(() -> {
      try {
        Thread.sleep(2000);
        AppData.uiHandler.post(() -> {
          if (fullActivity.barView.getVisibility() == View.VISIBLE) changeBarView();
        });
      } catch (InterruptedException ignored) {
      }
    });
    barViewTimerThread.start();
  }

  public static class OrientationData {
    public final boolean isFirstOrientationEvent;
    public final boolean lockOrientation;
    public final int lastOrientation;

    public OrientationData(boolean isFirstOrientationEvent, boolean lockOrientation, int lastOrientation) {
      this.isFirstOrientationEvent = isFirstOrientationEvent;
      this.lockOrientation = lockOrientation;
      this.lastOrientation = lastOrientation;
    }
  }

  private static OrientationData nextOrientationData;

  private boolean isFirstOrientationEvent = true;
  private boolean lockOrientation = false;
  private int lastOrientation = -1;

  public int getDefaultOrientation() {
    String defaultOrientationSetting = AppData.setting.getDefaultOrientation();

    switch (defaultOrientationSetting) {
      case "Portrait":
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
      case "Landscape":
        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
      case "Reverse Landscape":
        return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
      case "Reverse Portrait":
        return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
    }

    return -1;
  }

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (isFirstOrientationEvent) {
      int defaultOrientation = getDefaultOrientation();

      if (defaultOrientation != -1) {
        lastOrientation = defaultOrientation;
        setRequestedOrientation(defaultOrientation);
        nextOrientationData = new OrientationData(isFirstOrientationEvent, lockOrientation, lastOrientation);
        setNavBarOrientation();
        isFirstOrientationEvent = false;

        return;
      }

      isFirstOrientationEvent = false;
    }
    else {
      if (lockOrientation || Sensor.TYPE_ACCELEROMETER != sensorEvent.sensor.getType()) return;
    }

    float[] values = sensorEvent.values;
    float x = values[0];
    float y = values[1];
    int newOrientation = lastOrientation;

    if (x > -3 && x < 3 && y >= 4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    else if (y > -3 && y < 3 && x >= 4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    else if (y > -3 && y < 3 && x <= -4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    else if (x > -3 && x < 3 && y <= -4.5) newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

    if (lastOrientation != newOrientation) {
      lastOrientation = newOrientation;
      setRequestedOrientation(newOrientation);
      nextOrientationData = new OrientationData(isFirstOrientationEvent, lockOrientation, lastOrientation);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {

  }

  // 设置键盘监听
  private void setKeyEvent() {
    fullActivity.editText.requestFocus();
    fullActivity.editText.setInputType(InputType.TYPE_NULL);
    fullActivity.editText.setOnKeyListener((v, keyCode, event) -> {
      if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
        clientView.controlPacket.sendKeyEvent(event.getKeyCode(), event.getMetaState(), 0);
        return true;
      }
      return false;
    });
  }
}