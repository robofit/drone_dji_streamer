package cz.butfit.djistreamerv5;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cz.butfit.djistreamerv5.utils.AltitudeDialog;
import cz.butfit.djistreamerv5.utils.BinaryFileHelper;
import cz.butfit.djistreamerv5.utils.FileHelper;
import cz.butfit.djistreamerv5.utils.ServerIPDialog;
import dji.sdk.keyvalue.key.BatteryKey;
import dji.sdk.keyvalue.key.DJIKeyInfo;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.GimbalKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.key.ProductKey;
import dji.sdk.keyvalue.key.RemoteControllerKey;
import dji.sdk.keyvalue.value.common.Attitude;
import dji.sdk.keyvalue.value.common.CameraLensType;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
import dji.sdk.keyvalue.value.common.Velocity3D;
import dji.sdk.keyvalue.value.flightcontroller.FlightCoordinateSystem;
import dji.sdk.keyvalue.value.flightcontroller.GPSSignalLevel;
import dji.sdk.keyvalue.value.flightcontroller.RollPitchControlMode;
import dji.sdk.keyvalue.value.flightcontroller.VerticalControlMode;
import dji.sdk.keyvalue.value.flightcontroller.VirtualStickFlightControlParam;
import dji.sdk.keyvalue.value.flightcontroller.YawControlMode;
import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotation;
import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotationMode;
import dji.sdk.keyvalue.value.product.ProductType;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.common.register.DJISDKInitEvent;
import dji.v5.manager.KeyManager;
import dji.v5.manager.SDKManager;
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager;
import dji.v5.manager.datacenter.MediaDataCenter;
import dji.v5.manager.datacenter.camera.CameraStreamManager;
import dji.v5.manager.interfaces.ICameraStreamManager;
import dji.v5.manager.interfaces.SDKManagerCallback;
import dji.v5.network.DJINetworkManager;
import dji.v5.network.IDJINetworkStatusListener;
import dji.v5.utils.common.JsonUtil;
import dji.v5.utils.common.LogPath;
import dji.v5.utils.common.LogUtils;
import dji.v5.ux.accessory.RTKStartServiceHelper;
import dji.v5.ux.cameracore.widget.autoexposurelock.AutoExposureLockWidget;
import dji.v5.ux.cameracore.widget.cameracontrols.CameraControlsWidget;
import dji.v5.ux.cameracore.widget.cameracontrols.lenscontrol.LensControlWidget;
import dji.v5.ux.cameracore.widget.focusexposureswitch.FocusExposureSwitchWidget;
import dji.v5.ux.cameracore.widget.focusmode.FocusModeWidget;
import dji.v5.ux.cameracore.widget.fpvinteraction.FPVInteractionWidget;
import dji.v5.ux.core.base.DJISDKModel;
import dji.v5.ux.core.base.SchedulerProvider;
import dji.v5.ux.core.communication.BroadcastValues;
import dji.v5.ux.core.communication.GlobalPreferenceKeys;
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore;
import dji.v5.ux.core.communication.UXKeys;
import dji.v5.ux.core.extension.ViewExtensions;
import dji.v5.ux.core.panel.systemstatus.SystemStatusListPanelWidget;
import dji.v5.ux.core.panel.topbar.TopBarPanelWidget;
import dji.v5.ux.core.util.CameraUtil;
import dji.v5.ux.core.util.DataProcessor;
import dji.v5.ux.core.util.ViewUtil;
import dji.v5.ux.core.widget.fpv.FPVWidget;
import dji.v5.ux.core.widget.hsi.HorizontalSituationIndicatorWidget;
import dji.v5.ux.core.widget.hsi.PrimaryFlightDisplayWidget;
import dji.v5.ux.core.widget.setting.SettingWidget;
import dji.v5.ux.core.widget.simulator.SimulatorIndicatorWidget;
import dji.v5.ux.core.widget.systemstatus.SystemStatusWidget;
import dji.v5.ux.gimbal.GimbalFineTuneWidget;
import dji.v5.ux.map.MapWidget;
import dji.v5.ux.mapkit.core.maps.DJIUiSettings;
import dji.v5.ux.sample.showcase.defaultlayout.DefaultLayoutActivity;
import dji.v5.ux.training.simulatorcontrol.SimulatorControlWidget;
import dji.v5.ux.visualcamera.CameraNDVIPanelWidget;
import dji.v5.ux.visualcamera.CameraVisiblePanelWidget;
import dji.v5.ux.visualcamera.zoom.FocalZoomWidget;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.retry.Retry;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        ServerIPDialog.ServerIPDialogListener,
        AltitudeDialog.AltitudeDialogListener,
        ICameraStreamManager.CameraFrameListener,
        View.OnClickListener {

    //region Fields
    private static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_PERMISSION_CODE = 1001;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.KILL_BACKGROUND_PROCESSES,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public static enum READYSTATE {
        NOT_YET_CONNECTED,
        CONNECTING,
        OPEN,
        CLOSING,
        CLOSED,
        FAILURE
    }

    private Bundle instanceState;

    protected FPVWidget primaryFpvWidget;
    protected FPVInteractionWidget fpvInteractionWidget;
    protected FPVWidget secondaryFPVWidget;
    protected SystemStatusListPanelWidget systemStatusListPanelWidget;
    protected SimulatorControlWidget simulatorControlWidget;
    protected LensControlWidget lensControlWidget;
    protected AutoExposureLockWidget autoExposureLockWidget;
    protected FocusModeWidget focusModeWidget;
    protected FocusExposureSwitchWidget focusExposureSwitchWidget;
    protected CameraControlsWidget cameraControlsWidget;
    protected HorizontalSituationIndicatorWidget horizontalSituationIndicatorWidget;
    protected PrimaryFlightDisplayWidget pfvFlightDisplayWidget;
    //protected CameraNDVIPanelWidget ndviCameraPanel;
    //protected CameraVisiblePanelWidget visualCameraPanel;
    protected FocalZoomWidget focalZoomWidget;
    protected SettingWidget settingWidget;
    protected MapWidget mapWidget;
    protected TopBarPanelWidget topBarPanel;
    protected ConstraintLayout fpvParentView;
    private DrawerLayout mDrawerLayout;
    //private TextView gimbalAdjustDone;
    private GimbalFineTuneWidget gimbalFineTuneWidget;
    private ComponentIndexType lastDevicePosition;
    private CameraLensType lastLensType;

    private Button setServerIPBtn;
    private Button setAltitudeBtn;
    private ToggleButton liveShowToggle;
    private ToggleButton flightLogRecordToggle;


    private CompositeDisposable compositeDisposable;
    private DataProcessor<CameraSource> cameraSourceProcessor;

    private IDJINetworkStatusListener networkStatusListener;

    private ICameraStreamManager.AvailableCameraUpdatedListener availableCameraUpdatedListener;


    private SharedPreferences sharedPreferences;

    private String serverIP = "";
    private String serverPort = "";
    private double takeoffAltitude = Double.NaN;

    private Spinner calibrationPointsSpinner;
    private double latitudeOffset = 0;
    private double longitudeOffset = 0;
    private double latestKnownLatitudeRaw = 49.227240;
    private double latestKnownLongitudeRaw = 16.597338;
    private double latestKnownLatitude = 49.227240;
    private double latestKnownLongitude = 16.597338;

    private boolean aircraftConnected = false;
    private READYSTATE connectionState = READYSTATE.CLOSED;
    private Disposable connection;
    private WebsocketOutbound outboundClient;
    private String clientID;
    private final int connectionRetry = 5;
    private boolean droneControlEnabled = false;

    private FileHelper fileHelper = null;
    private BinaryFileHelper fileHelperBinary = null;
    private boolean logFlight = false;

    private LocationCoordinate3D currentLocation = new LocationCoordinate3D();
    private Attitude currentAttitude;
    private double currentCompassHeading;
    private Velocity3D currentVelocity;
    private Attitude currentGimbalAttitude;
    private double currentGimbalYawRelativeToAircraftHeading;
    private float pitch, roll, yaw, compass;
    private float velX, velY, velZ;
    private int satellites;
    private int batteryPercentRemaining;
    private ProductType aircraftType;
    private String aircraftSerial;

    private Integer leftStickHorizontal;
    private Integer leftStickVertical;
    private Integer rightStickHorizontal;
    private Integer rightStickVertical;

    private Integer gpsSatelliteCount;
    private GPSSignalLevel gpsSignalLevel;

    private ComponentIndexType cameraIndex = ComponentIndexType.UNKNOWN;

    private boolean telemetryVideoStreamOn = false;


    //endregion

    //region App Lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instanceState = savedInstanceState;

        checkAndRequestPermissions();
        initDJISDK();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!SDKManager.getInstance().isRegistered()) {
            Log.w(TAG, "DJI SDK not ready yet, skipping subscriptions");
            return;
        }

        mapWidget.onResume();
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(systemStatusListPanelWidget.closeButtonPressed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pressed -> {
                    if (pressed) {
                        ViewExtensions.hide(systemStatusListPanelWidget);
                    }
                }));
        compositeDisposable.add(simulatorControlWidget.getUIStateUpdates()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(simulatorControlWidgetState -> {
                    if (simulatorControlWidgetState instanceof SimulatorControlWidget.UIState.VisibilityUpdated) {
                        if (((SimulatorControlWidget.UIState.VisibilityUpdated) simulatorControlWidgetState).isVisible()) {
                            hideOtherPanels(simulatorControlWidget);
                        }
                    }
                }));
        compositeDisposable.add(cameraSourceProcessor.toFlowable()
                .observeOn(SchedulerProvider.io())
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribeOn(SchedulerProvider.io())
                .subscribe(result -> runOnUiThread(() -> onCameraSourceUpdated(result.devicePosition, result.lensType)))
        );
        compositeDisposable.add(ObservableInMemoryKeyedStore.getInstance()
                .addObserver(UXKeys.create(GlobalPreferenceKeys.GIMBAL_ADJUST_CLICKED))
                .observeOn(SchedulerProvider.ui())
                .subscribe(this::isGimbalAdjustClicked));
        ViewUtil.setKeepScreen(this, true);
    }


    @Override
    protected void onPause() {
        super.onPause();

        if(!SDKManager.getInstance().isRegistered()) {
            Log.w(TAG, "DJI SDK not ready yet, skipping subscriptions");
            return;
        }

        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
        mapWidget.onPause();
        ViewUtil.setKeepScreen(this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapWidget.onDestroy();
        MediaDataCenter.getInstance().getCameraStreamManager().removeAvailableCameraUpdatedListener(availableCameraUpdatedListener);
        DJINetworkManager.getInstance().removeNetworkStatusListener(networkStatusListener);

    }
    //endregion

    //region App Initializaiton
    private void checkAndRequestPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSION_CODE);
                Log.w(TAG, "permissions not granted");
            }
        }
    }

    private void initDJISDK() {
        Log.i(TAG, "Initializing DJI SDK...");

        SDKManager.getInstance().init(this, new SDKManagerCallback() {
            @Override
            public void onInitProcess(DJISDKInitEvent event, int totalProcess) {
                Log.i(TAG, "onInitProcess: ");
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    SDKManager.getInstance().registerApp();
                }
            }

            @Override
            public void onRegisterSuccess() {
                Log.i(TAG, "onRegisterSuccess: ");
                runOnUiThread(() -> initAfterProductConnect());
            }

            @Override
            public void onRegisterFailure(IDJIError error) {
                Log.i(TAG, "onRegisterFailure: ");
            }

            @Override
            public void onProductConnect(int productId) {
                Log.i(TAG, "onProductConnect: " + productId);
                aircraftConnected = true;
                SubscribeFlightData();
                SubscribeVideoFeed();

                // Aircraft connected, connect to the server and start sending data.
                if(!serverIP.isEmpty() && !serverPort.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        connectToServer(serverIP, serverPort);
                    }
                }
            }

            @Override
            public void onProductDisconnect(int productId) {
                Log.i(TAG, "onProductDisconnect: ");
                aircraftConnected = false;
                UnsubscribeFlightData();
                UnsubscribeVideoFeed();
                closeConnection();
            }

            @Override
            public void onProductChanged(int productId) {
                Log.i(TAG, "onProductChanged: ");
            }

            @Override
            public void onDatabaseDownloadProgress(long current, long total) {
                Log.i(TAG, "onDatabaseDownloadProgress: " + (current / total));
            }
        });
    }

    private void initAfterProductConnect() {
        lastDevicePosition = ComponentIndexType.UNKNOWN;
        lastLensType = CameraLensType.UNKNOWN;

        cameraSourceProcessor = DataProcessor.create(new CameraSource(ComponentIndexType.UNKNOWN,
                CameraLensType.UNKNOWN));

        availableCameraUpdatedListener = new ICameraStreamManager.AvailableCameraUpdatedListener() {
            @Override
            public void onAvailableCameraUpdated(@NonNull List<ComponentIndexType> availableCameraList) {
                runOnUiThread(() -> updateFPVWidgetSource(availableCameraList));
            }

            @Override
            public void onCameraStreamEnableUpdate(@NonNull Map<ComponentIndexType, Boolean> cameraStreamEnableMap) {
                //
            }
        };

        networkStatusListener = isNetworkAvailable -> {
            if (isNetworkAvailable) {
                LogUtils.d(TAG, "isNetworkAvailable=" + true);
                RTKStartServiceHelper.INSTANCE.startRtkService(false);
            }
        };


        setContentView(R.layout.activity_main);
        fpvParentView = findViewById(R.id.m_fpv_holder);
        mDrawerLayout = findViewById(R.id.m_root_view);
        topBarPanel = findViewById(R.id.m_panel_top_bar);
        settingWidget = topBarPanel.getSettingWidget();
        primaryFpvWidget = findViewById(R.id.m_widget_primary_fpv);
        fpvInteractionWidget = findViewById(R.id.m_widget_fpv_interaction);
        secondaryFPVWidget = findViewById(R.id.m_widget_secondary_fpv);
        systemStatusListPanelWidget = findViewById(R.id.m_widget_panel_system_status_list);
        simulatorControlWidget = findViewById(R.id.m_widget_simulator_control);
        lensControlWidget = findViewById(R.id.m_widget_lens_control);
        //ndviCameraPanel = findViewById(R.id.m_panel_ndvi_camera);
        //visualCameraPanel = findViewById(R.id.m_panel_visual_camera);
        autoExposureLockWidget = findViewById(R.id.m_widget_auto_exposure_lock);
        focusModeWidget = findViewById(R.id.m_widget_focus_mode);
        focusExposureSwitchWidget = findViewById(R.id.m_widget_focus_exposure_switch);
        pfvFlightDisplayWidget = findViewById(R.id.m_widget_fpv_flight_display_widget);
        focalZoomWidget = findViewById(R.id.m_widget_focal_zoom);
        cameraControlsWidget = findViewById(R.id.m_widget_camera_controls);
        horizontalSituationIndicatorWidget = findViewById(R.id.m_widget_horizontal_situation_indicator);
        //gimbalAdjustDone = findViewById(R.id.fpv_gimbal_ok_btn);
        gimbalFineTuneWidget = findViewById(R.id.m_setting_menu_gimbal_fine_tune);
        mapWidget = findViewById(R.id.m_widget_map);

        //ndviCameraPanel.setActivated(false);
//        mapWidget.initMapLibreMap(getApplicationContext(), map -> {
//            DJIUiSettings uiSetting = map.getUiSettings();
//            if (uiSetting != null) {
//                uiSetting.setZoomControlsEnabled(false);//hide zoom widget
//            }
//        });
//        mapWidget.onCreate(instanceState);
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        DJINetworkManager.getInstance().addNetworkStatusListener(networkStatusListener);

        initClickListener();
        MediaDataCenter.getInstance().getCameraStreamManager().addAvailableCameraUpdatedListener(availableCameraUpdatedListener);
        primaryFpvWidget.setOnFPVStreamSourceListener((devicePosition, lensType) -> cameraSourceProcessor.onNext(new CameraSource(devicePosition, lensType)));

        secondaryFPVWidget.setSurfaceViewZOrderOnTop(true);
        secondaryFPVWidget.setSurfaceViewZOrderMediaOverlay(true);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        serverIP = sharedPreferences.getString("serverIP", "");
        serverPort = sharedPreferences.getString("serverPort", "");

        takeoffAltitude = Float.parseFloat(sharedPreferences.getString("altitude", "0f"));

        setServerIPBtn = (Button) findViewById(R.id.btn_set_server_ip);
        setAltitudeBtn = (Button) findViewById(R.id.btn_set_altitude);
        liveShowToggle = (ToggleButton) findViewById(R.id.toggle_live_show);
        flightLogRecordToggle = (ToggleButton) findViewById(R.id.toggle_flight_log_record);

        setServerIPBtn.setOnClickListener(this);
        setAltitudeBtn.setOnClickListener(this);
        liveShowToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Going LIVE: " + isChecked);
                startLiveShow(isChecked);
            }
        });
        flightLogRecordToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    fileHelper = new FileHelper(getApplicationContext(), "flight_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".txt");
                    fileHelperBinary = new BinaryFileHelper(getApplicationContext(), "flight_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".bin");
                    logFlight = true;
                } else {
                    if(logFlight && fileHelper != null && fileHelperBinary != null) {
                        closeLogFile();
                    }
                    logFlight = false;
                }
            }
        });


//        calibrationPointsSpinner = (Spinner) findViewById(R.id.calibration_points_spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this,
//                R.array.calibrationPoints,
//                android.R.layout.simple_spinner_item
//        );
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        calibrationPointsSpinner.setAdapter(adapter);
//        calibrationPointsSpinner.setOnItemSelectedListener(this);
    }


    private void isGimbalAdjustClicked(BroadcastValues broadcastValues) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
        }
        horizontalSituationIndicatorWidget.setVisibility(View.GONE);
        if (gimbalFineTuneWidget != null) {
            gimbalFineTuneWidget.setVisibility(View.VISIBLE);
        }
    }

    private void initClickListener() {
        secondaryFPVWidget.setOnClickListener(v -> swapVideoSource());

        if (settingWidget != null) {
            settingWidget.setOnClickListener(v -> toggleRightDrawer());
        }

        // Setup top bar state callbacks
        SystemStatusWidget systemStatusWidget = topBarPanel.getSystemStatusWidget();
        if (systemStatusWidget != null) {
            systemStatusWidget.setOnClickListener(v -> ViewExtensions.toggleVisibility(systemStatusListPanelWidget));
        }

        SimulatorIndicatorWidget simulatorIndicatorWidget = topBarPanel.getSimulatorIndicatorWidget();
        if (simulatorIndicatorWidget != null) {
            simulatorIndicatorWidget.setOnClickListener(v -> ViewExtensions.toggleVisibility(simulatorControlWidget));
        }
//        gimbalAdjustDone.setOnClickListener(view -> {
//            horizontalSituationIndicatorWidget.setVisibility(View.VISIBLE);
//            if (gimbalFineTuneWidget != null) {
//                gimbalFineTuneWidget.setVisibility(View.GONE);
//            }
//        });
    }


    /**
     * Swap the video sources of the FPV and secondary FPV widgets.
     */
    private void swapVideoSource() {
        ComponentIndexType primarySource = primaryFpvWidget.getWidgetModel().getCameraIndex();
        ComponentIndexType secondarySource = secondaryFPVWidget.getWidgetModel().getCameraIndex();

        if (primarySource != ComponentIndexType.UNKNOWN && secondarySource != ComponentIndexType.UNKNOWN) {
            primaryFpvWidget.updateVideoSource(secondarySource);
            secondaryFPVWidget.updateVideoSource(primarySource);
        }
    }

    private void toggleRightDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.END);
    }



    private void hideOtherPanels(@Nullable View widget) {
        View[] panels = {
                simulatorControlWidget
        };

        for (View panel : panels) {
            if (widget != panel) {
                panel.setVisibility(View.GONE);
            }
        }
    }

    private void updateFPVWidgetSource(List<ComponentIndexType> availableCameraList) {
        LogUtils.i(TAG, JsonUtil.toJson(availableCameraList));
        if (availableCameraList == null) {
            return;
        }

        ArrayList<ComponentIndexType> cameraList = new ArrayList<>(availableCameraList);

        if (cameraList.isEmpty()) {
            secondaryFPVWidget.setVisibility(View.GONE);
            return;
        }

        if (cameraList.size() == 1) {
            primaryFpvWidget.updateVideoSource(availableCameraList.get(0));
            secondaryFPVWidget.setVisibility(View.GONE);
            return;
        }

        ComponentIndexType primarySource = getSuitableSource(cameraList, ComponentIndexType.LEFT_OR_MAIN);
        primaryFpvWidget.updateVideoSource(primarySource);
        cameraList.remove(primarySource);

        ComponentIndexType secondarySource = getSuitableSource(cameraList, ComponentIndexType.FPV);
        secondaryFPVWidget.updateVideoSource(secondarySource);

        secondaryFPVWidget.setVisibility(View.VISIBLE);

        cameraIndex = primarySource;
    }

    private ComponentIndexType getSuitableSource(List<ComponentIndexType> cameraList, ComponentIndexType defaultSource) {
        if (cameraList.contains(ComponentIndexType.LEFT_OR_MAIN)) {
            return ComponentIndexType.LEFT_OR_MAIN;
        } else if (cameraList.contains(ComponentIndexType.RIGHT)) {
            return ComponentIndexType.RIGHT;
        } else if (cameraList.contains(ComponentIndexType.UP)) {
            return ComponentIndexType.UP;
        } else if (cameraList.contains(ComponentIndexType.PORT_1)) {
            return ComponentIndexType.PORT_1;
        } else if (cameraList.contains(ComponentIndexType.PORT_2)) {
            return ComponentIndexType.PORT_2;
        } else if (cameraList.contains(ComponentIndexType.PORT_3)) {
            return ComponentIndexType.PORT_4;
        } else if (cameraList.contains(ComponentIndexType.PORT_4)) {
            return ComponentIndexType.PORT_4;
        } else if (cameraList.contains(ComponentIndexType.VISION_ASSIST)) {
            return ComponentIndexType.VISION_ASSIST;
        }
        return defaultSource;
    }


    private void onCameraSourceUpdated(ComponentIndexType devicePosition, CameraLensType lensType) {
        LogUtils.i(LogPath.SAMPLE, "onCameraSourceUpdated", devicePosition, lensType);
        if (devicePosition == lastDevicePosition && lensType == lastLensType) {
            return;
        }
        lastDevicePosition = devicePosition;
        lastLensType = lensType;
        updateViewVisibility(devicePosition, lensType);
        updateInteractionEnabled();

        if (fpvInteractionWidget.isInteractionEnabled()) {
            fpvInteractionWidget.updateCameraSource(devicePosition, lensType);
        }
        if (lensControlWidget.getVisibility() == View.VISIBLE) {
            lensControlWidget.updateCameraSource(devicePosition, lensType);
        }
//        if (ndviCameraPanel.getVisibility() == View.VISIBLE) {
//            ndviCameraPanel.updateCameraSource(devicePosition, lensType);
//        }
//        if (visualCameraPanel.getVisibility() == View.VISIBLE) {
//            visualCameraPanel.updateCameraSource(devicePosition, lensType);
//        }
        if (autoExposureLockWidget.getVisibility() == View.VISIBLE) {
            autoExposureLockWidget.updateCameraSource(devicePosition, lensType);
        }
        if (focusModeWidget.getVisibility() == View.VISIBLE) {
            focusModeWidget.updateCameraSource(devicePosition, lensType);
        }
        if (focusExposureSwitchWidget.getVisibility() == View.VISIBLE) {
            focusExposureSwitchWidget.updateCameraSource(devicePosition, lensType);
        }
        if (cameraControlsWidget.getVisibility() == View.VISIBLE) {
            cameraControlsWidget.updateCameraSource(devicePosition, lensType);
        }
        if (focalZoomWidget.getVisibility() == View.VISIBLE) {
            focalZoomWidget.updateCameraSource(devicePosition, lensType);
        }
        if (horizontalSituationIndicatorWidget.getVisibility() == View.VISIBLE) {
            horizontalSituationIndicatorWidget.updateCameraSource(devicePosition, lensType);
        }
    }

    private void updateViewVisibility(ComponentIndexType devicePosition, CameraLensType lensType) {
        pfvFlightDisplayWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.VISIBLE : View.INVISIBLE);

        lensControlWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        //ndviCameraPanel.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        //visualCameraPanel.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        autoExposureLockWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        focusModeWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        focusExposureSwitchWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        cameraControlsWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        focalZoomWidget.setVisibility(CameraUtil.isFPVTypeView(devicePosition) ? View.INVISIBLE : View.VISIBLE);
        horizontalSituationIndicatorWidget.setSimpleModeEnable(CameraUtil.isFPVTypeView(devicePosition));

        //ndviCameraPanel.setVisibility(CameraUtil.isSupportForNDVI(lensType) ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateInteractionEnabled() {
        fpvInteractionWidget.setInteractionEnabled(!CameraUtil.isFPVTypeView(primaryFpvWidget.getWidgetModel().getCameraIndex()));
    }

    private static class CameraSource {
        ComponentIndexType devicePosition;
        CameraLensType lensType;

        public CameraSource(ComponentIndexType devicePosition, CameraLensType lensType) {
            this.devicePosition = devicePosition;
            this.lensType = lensType;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
    //endregion

    //region SDK Subscription
    private void SubscribeVideoFeed() {
        CameraStreamManager streamManager = CameraStreamManager.getInstance();

        streamManager.addFrameListener(cameraIndex == ComponentIndexType.UNKNOWN ? ComponentIndexType.LEFT_OR_MAIN : cameraIndex, ICameraStreamManager.FrameFormat.YUV420_888, this);
    }

    private void UnsubscribeVideoFeed() {
        CameraStreamManager.getInstance().removeFrameListener(this);
    }

    private void SubscribeFlightData() {
        KeyManager km = KeyManager.getInstance();

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D),
                this,
                (locationCoordinate3D, t1) -> {
                    currentLocation = t1 != null ? t1 : locationCoordinate3D;
                });

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeyAircraftAttitude),
                this,
                (attitude, t1) -> {
                    currentAttitude = t1 != null ? t1 : attitude;
                });

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeyAircraftVelocity),
                this,
                (velocity3D, t1) -> {
                    currentVelocity = t1 != null ? t1 : velocity3D;
                }
        );

        km.listen(
                KeyTools.createKey(GimbalKey.KeyGimbalAttitude),
                this,
                (attitude, t1) -> {
                    currentGimbalAttitude = t1 != null ? t1 : attitude;
                }
        );

        km.listen(
                KeyTools.createKey(GimbalKey.KeyYawRelativeToAircraftHeading),
                this,
                (aDouble, t1) -> {
                    currentGimbalYawRelativeToAircraftHeading = t1 != null ? t1 : aDouble;
                }
        );

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeyCompassHeading),
                this,
                (aDouble, t1) -> {
                    currentCompassHeading = t1 != null ? t1 : aDouble;
                }
        );

        km.listen(
                KeyTools.createKey(RemoteControllerKey.KeyStickLeftHorizontal),
                this,
                (integer, t1) -> {
                    leftStickHorizontal = t1 != null ? t1 : integer;
                    checkManualSticksChange();
                }
        );

        km.listen(
                KeyTools.createKey(RemoteControllerKey.KeyStickLeftVertical),
                this,
                (integer, t1) -> {
                    leftStickVertical = t1 != null ? t1 : integer;
                    checkManualSticksChange();
                }
        );

        km.listen(
                KeyTools.createKey(RemoteControllerKey.KeyStickRightHorizontal),
                this,
                (integer, t1) -> {
                    rightStickHorizontal = t1 != null ? t1 : integer;
                    checkManualSticksChange();
                }
        );

        km.listen(
                KeyTools.createKey(RemoteControllerKey.KeyStickRightVertical),
                this,
                (integer, t1) -> {
                    rightStickVertical = t1 != null ? t1 : integer;
                    checkManualSticksChange();
                }
        );

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeyGPSSatelliteCount),
                this,
                (integer, t1) -> {
                    gpsSatelliteCount = t1 != null ? t1 : integer;
                }
        );

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeyGPSSignalLevel),
                this,
                (gpsSignalLevel, t1) -> {
                    this.gpsSignalLevel = t1 != null ? t1 : gpsSignalLevel;
                }
        );

        km.listen(
                KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent),
                this,
                (integer, t1) -> {
                    batteryPercentRemaining = t1 != null ? t1 : integer;
                }
        );

        km.listen(
                KeyTools.createKey(ProductKey.KeyProductType),
                this,
                (productType, t1) -> {
                    aircraftType = t1 != null ? t1 : productType;
                }
        );

        km.listen(
                KeyTools.createKey(FlightControllerKey.KeySerialNumber),
                this,
                (s, t1) -> {
                    aircraftSerial = t1 != null ? t1 : s;
                }
        );

//        aircraftType = km.getValue(KeyTools.createKey(ProductKey.KeyProductType));
//        aircraftSerial = km.getValue(KeyTools.createKey(FlightControllerKey.KeySerialNumber));
    }

    private void UnsubscribeFlightData() {
        KeyManager.getInstance().cancelListen(this);
    }

    //endregion

    //region Server Communication
    private Mono<Void> doServerHandshake(WebsocketInbound inbound, WebsocketOutbound outbound) {
        JSONObject msg = new JSONObject();
        JSONObject data = new JSONObject();
        Log.d(TAG, "Trying to handshake");
        if (aircraftType != null && aircraftSerial != null) {
            try {
                msg.put("type", "hello");
                data.put("ctype", 0);
                data.put("drone_name", aircraftType.value());
                data.put("serial", aircraftSerial);
                msg.put("data", data);

                if(logFlight && fileHelper != null && fileHelperBinary != null) {
                    fileHelper.writeLine(msg.toString());
                }
                return outbound.sendString(Mono.just(msg.toString()))
                        .then()
                        .doOnSuccess(unused -> Log.d(TAG, "Message sent successfully"))
                        .doOnError(error -> Log.e(TAG, "Failed to send message", error));

            } catch (JSONException e) {
                Log.e(TAG, "Doing server handshake", e);
                return Mono.error(e);
            }
        } else {
            Log.e(TAG, "Aircaft not initialized yet");
            return Mono.error(new Throwable("Aircaft not initialized yet"));
        }
    }

    private void handleServerHandshake(JSONObject msg) throws JSONException {
        clientID = msg.getString("client_id");
        Log.i(TAG, msg.toString());
        connectionState = READYSTATE.OPEN;
        liveShowToggle.setChecked(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void connectToServer(String ip, String port) {
        connection = HttpClient.create()
                .websocket()
                .uri("ws://" + ip + ":" + port + "/")
                .handle((inbound, outbound) -> {
                    this.outboundClient = outbound; // Save outbound for later use
                    return doServerHandshake(inbound, outbound)
                            .then(handleWebSocket(inbound, outbound));
                })
                .doOnSubscribe(subscription -> {
                    Log.d(TAG, "Connected to WebSocket server");
                })
                .doOnTerminate(() -> {
                    Log.d(TAG, "Disconnected from WebSocket server");
                    connectionState = READYSTATE.CLOSED;
                    onConnectionCloseOrFailure();
                })
                .doOnError(error -> {
                    Log.e(TAG, "WebSocket connection failed", error);
                    connectionState = READYSTATE.FAILURE;
                    onConnectionCloseOrFailure();
                })
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(connectionRetry))
                        .doBeforeRetry(retrySignal -> Log.d(TAG, "Retrying connection...")))
                .subscribe();
    }


    private Mono<Void> handleWebSocket(WebsocketInbound inbound, WebsocketOutbound outbound) {
        // Log incoming messages
        inbound.receive()
                .asString()
                .doOnNext(message -> {
                    Log.d(TAG, "Received message: " + message);
                    try {
                        JSONObject receivedMsg = new JSONObject(message);
                        String msgType = receivedMsg.getString("type");
                        switch (msgType) {
                            case "hello_resp":
                                handleServerHandshake(receivedMsg.getJSONObject("data"));
                                break;
                            case "enable_control":
                                handleEnableControl(receivedMsg.getJSONObject("data"));
                                break;
                            case "control_command":
                                if(droneControlEnabled) {
                                    handleControlCommand(receivedMsg.getJSONObject("data"));
                                } else {
                                    Log.d("CONTROLS", "Drone control is not enabled!");
                                }
                                break;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "On message received", e);
                    }
                })
                .subscribe();

        return Mono.never();
    }

    private void closeConnection() {
        if(logFlight && fileHelper != null && fileHelperBinary != null) {
            fileHelper.close();
            fileHelperBinary.close();
        }

        if(outboundClient != null) {
            outboundClient.sendClose() // Initiate a close handshake
                    .then()
                    .doOnSuccess(unused -> {
                        Log.d(TAG, "WebSocket close frame sent.");
                        // Clean up after successful close
                        outboundClient = null; // Clear the outbound client reference
                        connectionState = READYSTATE.CLOSED;
                    })
                    .doOnError(error -> Log.e(TAG, "Failed to send close frame", error))
                    .subscribe();
        }

        if(connection != null) {
            connection.dispose();
            connection = null;
        }

        onConnectionCloseOrFailure();
    }

    private void onConnectionCloseOrFailure() {
        liveShowToggle.setChecked(false);
    }
    //endregion

    //region Telemetry and Image
    private void startLiveShow(boolean isOn) {
        telemetryVideoStreamOn = isOn;
    }

    @Override
    public void onFrame(@NonNull byte[] frameData, int offset, int length, int width, int height, @NonNull ICameraStreamManager.FrameFormat format) {
        //Log.i(TAG, "NEW FRAME RECEIVED");

        if (telemetryVideoStreamOn && connectionState == READYSTATE.OPEN) {

            byte[] jpegByte = parseToJPEG(frameData, width, height);

            try {
                //Log.d(TAG, "WSRS: " + String.valueOf(connectionState));
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");
                latestKnownLatitudeRaw = Double.isNaN(currentLocation.getLatitude()) ? latestKnownLatitudeRaw : currentLocation.getLatitude();
                latestKnownLongitudeRaw = Double.isNaN(currentLocation.getLongitude()) ? latestKnownLongitudeRaw : currentLocation.getLongitude();

                latestKnownLatitude = latestKnownLatitudeRaw + latitudeOffset;
                latestKnownLongitude = latestKnownLongitudeRaw + longitudeOffset;

                //if (connectionState == READYSTATE.OPEN) {
                JSONObject msg = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject gps = new JSONObject();
                JSONObject aircraftOrientation = new JSONObject();
                JSONObject aircraftVelocity = new JSONObject();
                JSONObject gimbalOrientation = new JSONObject();
                JSONObject sticks = new JSONObject();
                JSONObject leftStickJson = new JSONObject();
                JSONObject rightStickJson = new JSONObject();
                try {
                    msg.put("type", "data_broadcast");
                    data.put("client_id", clientID);
                    // Fill altitude
                    data.put("altitude", takeoffAltitude + currentLocation.getAltitude());
                    data.put("relative_altitude", currentLocation.getAltitude());
                    // Fill GPS data
                    gps.put("latitude", latestKnownLatitude);
                    gps.put("longitude", latestKnownLongitude);
                    data.put("gps", gps);
                    // Fill aircraft orientation
                    aircraftOrientation.put("pitch", currentAttitude.getPitch());
                    aircraftOrientation.put("roll", currentAttitude.getRoll());
                    aircraftOrientation.put("yaw", currentAttitude.getYaw());
                    aircraftOrientation.put("compass", currentCompassHeading);
                    data.put("aircraft_orientation", aircraftOrientation);
                    // Fill aircraft velocity
                    aircraftVelocity.put("velocity_x", currentVelocity.getX());
                    aircraftVelocity.put("velocity_y", currentVelocity.getY());
                    aircraftVelocity.put("velocity_z", currentVelocity.getZ());
                    data.put("aircraft_velocity", aircraftVelocity);
                    // Fill aircraft orientation
                    gimbalOrientation.put("pitch", currentGimbalAttitude.getPitch());
                    gimbalOrientation.put("roll", currentGimbalAttitude.getRoll());
                    gimbalOrientation.put("yaw", currentGimbalAttitude.getYaw());
                    gimbalOrientation.put("yaw_relative", currentGimbalYawRelativeToAircraftHeading);

                    // Fill stick data
                    leftStickJson.put("x", leftStickHorizontal);
                    leftStickJson.put("y", leftStickVertical);
                    rightStickJson.put("x", rightStickHorizontal);
                    rightStickJson.put("y", rightStickVertical);

                    sticks.put("left_stick", leftStickJson);
                    sticks.put("right_stick", rightStickJson);

                    data.put("gimbal_orientation", gimbalOrientation);

                    data.put("satellite_count", gpsSatelliteCount);
                    data.put("gps_signal_level", gpsSignalLevel);
                    // Battery state
                    data.put("battery", batteryPercentRemaining);

                    data.put("sticks", sticks);

                    // Fill timestamp
                    data.put("timestamp", timestampFormat.format(currentTime));

                    // Fill jpeg frame
                    //data.put("frame", latestJPEGframe);

                    // Put everything together
                    msg.put("data", data);

                    //Log.i(TAG, msg.toString());

                    byte[] jsonBytes = msg.toString().getBytes(StandardCharsets.UTF_8);
                    byte[] jpegBytes = jpegByte;
                    ByteBuffer buffer = ByteBuffer.allocate(4 + jsonBytes.length + 4 + jpegBytes.length);
                    // 1. Put 4 bytes: JSON length
                    buffer.putInt(jsonBytes.length);
                    // 2. Put JSON bytes
                    buffer.put(jsonBytes);
                    // 3. Put 4 bytes: JPEG length
                    buffer.putInt(jpegBytes.length);
                    // 4. Put JPEG bytes
                    buffer.put(jpegBytes);
                    // 5. Send it
                    if (connectionState == READYSTATE.OPEN) {
                        outboundClient.sendByteArray(Mono.just(buffer.array()))
                                .then()
                                .doOnSuccess(unused -> {
                                    //Log.d(TAG, "Binary message sent successfully");
                                })
                                .doOnError(error -> {
                                    Log.e(TAG, "Failed to send binary message", error);
                                    connectionState = READYSTATE.FAILURE;
                                    onConnectionCloseOrFailure();
                                })
                                .subscribe();
                    }

                    if (logFlight && fileHelper != null && fileHelperBinary != null) {
                        fileHelperBinary.writeBinary(buffer.array());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Sending flight data", e);
                }
            } catch (Exception e) {
                Log.e(TAG, "Flight Data send exception", e);
            }
        }
    }

    private byte[] parseToJPEG(byte[] yuvFrame, int width, int height) {

        if (yuvFrame.length < width * height) {
            return null;
        }
        int length = width * height;

        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];

        for (int i = 0; i < u.length; i ++) {
            u[i] = yuvFrame[length + i];
            v[i] = yuvFrame[length + u.length + i];
        }
        for (int i = 0; i < u.length; i++) {
            yuvFrame[length + 2 * i] = v[i];
            yuvFrame[length + 2 * i + 1] = u[i];
        }

        YuvImage yuvImage = new YuvImage(yuvFrame,
                ImageFormat.NV21,
                width,
                height,
                null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        yuvImage.compressToJpeg(new Rect(0,
                0,
                width,
                height), 50, outputStream);

        return outputStream.toByteArray();
    }
    //endregion

    //region Stick Control

    private void handleEnableControl(JSONObject msg) throws JSONException {
        boolean enable = msg.getBoolean("enable");

        enableVirtualStick(enable);
    }

    private void enableVirtualStick(boolean enable) {
        if(enable) {
            VirtualStickManager.getInstance().enableVirtualStick(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onSuccess() {
                    droneControlEnabled = true;
                    VirtualStickManager.getInstance().setVirtualStickAdvancedModeEnabled(true);
                    Log.i(TAG, "Drone virtual stick mode enabled");
                    Toast.makeText(getApplicationContext(),
                            "Virtual stick mode enabled successfully",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(@NonNull IDJIError idjiError) {
                    Log.e(TAG, idjiError.toString());
                }
            });
        } else {
            VirtualStickManager.getInstance().disableVirtualStick(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onSuccess() {
                    droneControlEnabled = false;
                    VirtualStickManager.getInstance().setVirtualStickAdvancedModeEnabled(false);
                    Log.i(TAG, "Drone virtual stick mode disabled");
                    Toast.makeText(getApplicationContext(),
                            "Virtual stick mode disabled successfully",
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(@NonNull IDJIError idjiError) {
                    Log.e(TAG, idjiError.toString());
                }
            });
        }
    }

    private void handleControlCommand(JSONObject msg) throws JSONException {
        Log.d(TAG, msg.toString());

        if (droneControlEnabled) {
            VirtualStickFlightControlParam command = new VirtualStickFlightControlParam();
            command.setPitch(msg.optDouble("pitch", 0.0)); // Forward/backward (m/s)
            command.setRoll(msg.optDouble("roll", 0.0)); // Left/right (m/s)
            command.setYaw(msg.optDouble("yaw", 0.0)); // Yaw rate (deg/s)
            command.setVerticalThrottle(msg.optDouble("throttle", 0.0)); // Vertical speed (m/s)
            command.setVerticalControlMode(VerticalControlMode.VELOCITY);
            command.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
            command.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            command.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

            VirtualStickManager.getInstance().sendVirtualStickAdvancedParam(command);

            GimbalAngleRotation gimbalRotation = new GimbalAngleRotation();
            gimbalRotation.setMode(GimbalAngleRotationMode.RELATIVE_ANGLE);
            gimbalRotation.setPitch(msg.optDouble("gimbal_pitch", 0.0));

            KeyManager.getInstance().performAction(
                    KeyTools.createKey(GimbalKey.KeyRotateByAngle),
                    gimbalRotation,
                    new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                        @Override
                        public void onSuccess(EmptyMsg emptyMsg) {
                            Log.i(TAG, "Gimbal rotated");
                        }

                        @Override
                        public void onFailure(@NonNull IDJIError idjiError) {
                            Log.e(TAG, idjiError.toString());
                        }
                    }
            );
        }
    }

    private void sendVirtualStickDisabled() {
        JSONObject msg = new JSONObject();
        JSONObject data = new JSONObject();
        Log.d(TAG, "Sending virtual sticks disabled");
        try {
            msg.put("type", "manual_control_override");
            data.put("client_id", clientID);
            msg.put("data", data);

            if (connectionState == READYSTATE.OPEN) {
                outboundClient.sendString(Mono.just(msg.toString()))
                        .then()
                        .doOnSuccess(unused -> {
                            //Log.d(TAG, "Binary message sent successfully");
                        })
                        .doOnError(error -> Log.e(TAG, "Failed to send message", error))
                        .subscribe();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Sending virtual sticks disabled error", e);
        }
    }

    private void checkManualSticksChange() {
        if (droneControlEnabled && isManualOverrideDetected(leftStickHorizontal, leftStickVertical, rightStickHorizontal, rightStickVertical)) {
            Log.w(TAG, "Manual input detected! Disabling virtual stick mode.");
            enableVirtualStick(false);
            sendVirtualStickDisabled();
        }
    }

    private boolean isManualOverrideDetected(float lx, float ly, float rx, float ry) {
        float threshold = 0.05f; // small deadzone to ignore jitter
        return Math.abs(lx) > threshold || Math.abs(ly) > threshold ||
                Math.abs(rx) > threshold || Math.abs(ry) > threshold;
    }
    //endregion

    //region UI
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        Log.d(TAG, "Selected item: " + adapterView.getItemAtPosition(pos).toString());
        double latitudeCalibrationPoint = 0;
        double longitudeCalibrationPoint = 0;

        // Put your calibration points here, create as many cases you like
        switch(adapterView.getItemAtPosition(pos).toString()) {
            case "none":
                latitudeCalibrationPoint = 0;
                longitudeCalibrationPoint = 0;
                break;
            case "1_brno":
                latitudeCalibrationPoint = 49.2272250;
                longitudeCalibrationPoint = 16.5974175;
                break;
            case "2_graz":
                latitudeCalibrationPoint = 47.058524;
                longitudeCalibrationPoint = 15.459548;
                break;
        }

        if (latitudeCalibrationPoint != 0 && longitudeCalibrationPoint != 0) {
            latitudeOffset = latitudeCalibrationPoint - latestKnownLatitudeRaw;
            longitudeOffset = longitudeCalibrationPoint - latestKnownLongitudeRaw;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG, "Nothing selected");
    }

    @Override
    public void applyInputs(String altitude) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("altitude", altitude);
        editor.commit();

        takeoffAltitude = Float.parseFloat(altitude);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void applyInputs(String ip, String port) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("serverIP", ip);
        editor.putString("serverPort", port);
        editor.commit();

        serverIP = ip;
        serverPort = port;

        if(aircraftConnected) {
            if(connectionState == READYSTATE.OPEN) {
                closeConnection();
            }
            connectToServer(ip, port);
        }
    }

    private void openServerIPDialog() {
        ServerIPDialog serverIPDialog = new ServerIPDialog();
        serverIPDialog.SetHint(serverIP, serverPort);
        serverIPDialog.show(getSupportFragmentManager(), "server ip dialog");
    }

    private void openAltitudeDialog() {
        AltitudeDialog altitudeDialog = new AltitudeDialog();
        altitudeDialog.SetHint(String.valueOf(takeoffAltitude));
        altitudeDialog.show(getSupportFragmentManager(), "altitude dialog");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_server_ip:
                openServerIPDialog();
                break;
            case R.id.btn_set_altitude:
                openAltitudeDialog();
                break;
        }
    }

    private void closeLogFile() {
        fileHelper.close();
        fileHelperBinary.close();

        fileHelper = null;
        fileHelperBinary = null;

        Toast.makeText(getApplicationContext(), "Save file closed", Toast.LENGTH_LONG).show();
    }
    //endregion

}
