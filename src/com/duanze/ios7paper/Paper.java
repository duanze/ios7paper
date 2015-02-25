package com.duanze.ios7paper;

import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

public class Paper extends WallpaperService {
	// 背景位图
	private Bitmap paper;

	// 屏幕长宽及背景位图长宽
	private int width, height, bitW, bitH;

	// 显示位图的起点及起点限制值
	private float lx, ly, limx, limy;

	// 千分之一比例数
	private float factorx, factory;

	// 相对位图起点位移量
	private float tranx, trany;

	// 因重力而产生的位移速度
	private int gx = 0, gy = 0;

	// 获取设备屏幕分辨率的准备
	DisplayMetrics dm = new DisplayMetrics();

	// 标志是否第一次绘图
	private boolean first;
	private float oldPitch, oldRoll;

	// 创建SensorManager对象
	private SensorManager mSensorManager01;

	// 访问图片的数组
	int[] images = new int[] { R.drawable.a1, R.drawable.a2, R.drawable.a3,
			R.drawable.a4, R.drawable.a5, R.drawable.a6, R.drawable.a7,
			R.drawable.a8, R.drawable.a9, R.drawable.a10,R.drawable.a11 };
	// 初始显示图片
	int currentImg = 0;

	public static final String TAG = "Paper";

	// 监听器
	// 创建SensorListener捕捉onSensorChanged事件

	// 信号量，进程同步
	boolean signal = false;

	private SensorEventListener mSensorEventListener = new SensorEventListener() {
		float[] accelerometerValues = new float[3];
		float[] magneticValues = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event) {

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = event.values.clone();
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticValues = event.values.clone();
			}

			if (signal) {
				float[] R = new float[9];
				float[] values = new float[3];
				SensorManager.getRotationMatrix(R, null, accelerometerValues,
						magneticValues);
				SensorManager.getOrientation(R, values);

				// 取得pitch角度
				float fPitchAngle = (float) Math.toDegrees(values[1]), fRollAngle = (float) Math
						.toDegrees(values[2]);

				// LogUtil.i(TAG, "pitch:" + fPitchAngle + " roll:" +
				// fRollAngle);

				float disPitch = fPitchAngle - oldPitch, disRoll = fRollAngle
						- oldRoll;
				LogUtil.i(TAG, "dispitch:" + disPitch + " disroll:" + disRoll);

				if (Math.abs(disPitch) > 2) {// 如果翻转角度大于 ,前后处理
					if (disPitch < 0) {// 自顶部开始翻转
						if (disPitch >= -5) {
							gy = 1;
						}

						else if (disPitch >= -10) {
							gy = 2;
						}

						else if (disPitch >= -15) {
							gy = 3;
						}

						else {
							gy = 4;
						}
					} else {// 自底部开始翻转
						if (disPitch <= 5) {
							gy = -1;
						}

						else if (disPitch <= 10) {
							gy = -2;
						}

						else if (disPitch <= 15) {
							gy = -3;
						}

						else {
							gy = -4;
						}
					}
					//
					oldPitch = fPitchAngle;
				} else {
					// 无角度归零！！！
					gy = 0;
				}

				if (Math.abs(disRoll) > 2) {// 如果翻转角度大于 ,左右处理
					if (disRoll > 0) {// 从左往右翻
						if (disRoll <= 5) {
							gx = 1;
						}

						else if (disRoll <= 10) {
							gx = 2;
						}

						else if (disRoll <= 15) {
							gx = 3;
						}

						else {
							gx = 4;
						}

					} else {// 往左翻转
						if (disRoll >= -5) {
							gx = -1;
						}

						else if (disRoll >= -10) {
							gx = -2;
						}

						else if (disRoll >= -15) {
							gx = -3;
						}

						else {
							gx = -4;
						}
					}
					//
					oldRoll = fRollAngle;
				} else {
					gx = 0;
				}

				signal = false;
			}// if signal
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	// 实现WallpaperService必须实现的抽象方法
	@Override
	public Engine onCreateEngine() {
		SharedPreferences preferences = getSharedPreferences("ios7paper",
				MODE_PRIVATE);
		String myURI = preferences.getString("myURI", null);

		if (myURI == null) {

			int paperId = preferences.getInt("paperId", 0);
			if (paperId != 0) {
				currentImg = paperId - 1;// 默认值为0，帮做一个+1处理，此处-1
			}

			// 加载背景图片
			paper = BitmapFactory.decodeResource(getResources(),
					images[currentImg]);
		} else {

			Uri uri = Uri.parse(myURI);
			ContentResolver cr = this.getContentResolver();

			try {
				// 加载自定义图片
				paper = BitmapFactory.decodeStream(cr.openInputStream(uri));
			} catch (FileNotFoundException e) {
				Log.e("Exception", e.getMessage(), e);

			}

		}

		bitW = paper.getWidth();
		bitH = paper.getHeight();

		// 返回自定义的Engine
		return new MyEngine();
	}

	class MyEngine extends Engine {

		// 记录程序界面是否可见
		private boolean mVisible;

		// 定义一个Handler
		Handler mHandler = new Handler();

		// 定义一个周期性执行的任务
		private final Runnable drawThread = new Runnable() {
			@Override
			public void run() {
				drawFrame();
			}
		};

		public MyEngine() {
			LogUtil.i(TAG, "MyEngine's created.");

			// 获取屏幕分辨率！！！
			dm = getResources().getDisplayMetrics();
			width = dm.widthPixels;
			height = dm.heightPixels;

			LogUtil.i(TAG, "width:" + width + "  height" + height);

			factorx = bitW * 0.001f;
			factory = bitH * 0.001f;

			lx = bitW * 0.025f;
			ly = bitH * 0.025f;

			tranx = bitW - 2 * lx;
			trany = bitH - 2 * ly;
			limx = 2 * lx;
			limy = 2 * ly;

			// 指定第一次绘图
			first = true;

			// 创建,取得SENSOR_SERVICE服务
			mSensorManager01 = (SensorManager) getSystemService(SENSOR_SERVICE);

		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			// 删除回调
			if (mHandler != null) {
				mHandler.removeCallbacks(drawThread);
			}
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			// 当界面可见时候，执行drawFrame()方法。
			if (visible) {
				// 创建SensorManager对象
				Sensor magneticSensor = mSensorManager01
						.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
				Sensor accelerometerSensor = mSensorManager01
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

				mSensorManager01.registerListener(mSensorEventListener,
						magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
				mSensorManager01.registerListener(mSensorEventListener,
						accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

				// 动态地绘制图形
				drawFrame();
			} else {
				// 取消传感器
				mSensorManager01.unregisterListener(mSensorEventListener);
				// 线程睡眠500ms

				try {
					Thread.sleep(500);
					// 重定位
					lx = bitW * 0.025f;
					ly = bitH * 0.025f;
					first = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 如果界面不可见，删除回调
				mHandler.removeCallbacks(drawThread);

			}
		}

		// 定义绘制图形的工具方法
		private void drawFrame() {
			// 获取该壁纸的SurfaceHolder
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			// 对画布加锁
			c = holder.lockCanvas();

			drawTime(c);

			holder.unlockCanvasAndPost(c);

			if (mVisible) {
				// 指定 ms秒后重新执行drawThread一次
				mHandler.postDelayed(drawThread, 150);
			}
		}

		// 核心处理方法
		private void drawTime(Canvas canvas) {

			if (first) {

				oldPitch = 0.0f;
				oldRoll = 0.0f;

				canvas.drawBitmap(paper, new Rect((int) lx, (int) ly,
						(int) (lx + tranx), (int) (ly + trany)), new Rect(0, 0,
						width, height), null);
				first = false;

				LogUtil.i(TAG, "---First!---");
				LogUtil.i(TAG, "lx:" + lx + "  ly:" + ly + "  limx:" + limx
						+ "  limy:" + limy);

			} else {

				LogUtil.i(TAG, "gx:" + gx + "  gy:" + gy);
				LogUtil.i(TAG, "lx:" + lx + "  ly:" + ly);
				LogUtil.i(TAG, "---------");

				// 确保不越界显示
				lx += gx * 2 * factorx;
				if (lx < 0)
					lx = 0;
				else if (lx > limx)
					lx = limx;

				ly += gy * 2 * factory;
				if (ly < 0)
					ly = 0;
				else if (ly > limy)
					ly = limy;

				canvas.drawBitmap(paper, new Rect((int) lx, (int) ly,
						(int) (lx + tranx), (int) (ly + trany)), new Rect(0, 0,
						width, height), null);
				signal = true;
			}
		}

	}
}