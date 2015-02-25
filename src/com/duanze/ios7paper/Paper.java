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
	// ����λͼ
	private Bitmap paper;

	// ��Ļ��������λͼ����
	private int width, height, bitW, bitH;

	// ��ʾλͼ����㼰�������ֵ
	private float lx, ly, limx, limy;

	// ǧ��֮һ������
	private float factorx, factory;

	// ���λͼ���λ����
	private float tranx, trany;

	// ��������������λ���ٶ�
	private int gx = 0, gy = 0;

	// ��ȡ�豸��Ļ�ֱ��ʵ�׼��
	DisplayMetrics dm = new DisplayMetrics();

	// ��־�Ƿ��һ�λ�ͼ
	private boolean first;
	private float oldPitch, oldRoll;

	// ����SensorManager����
	private SensorManager mSensorManager01;

	// ����ͼƬ������
	int[] images = new int[] { R.drawable.a1, R.drawable.a2, R.drawable.a3,
			R.drawable.a4, R.drawable.a5, R.drawable.a6, R.drawable.a7,
			R.drawable.a8, R.drawable.a9, R.drawable.a10,R.drawable.a11 };
	// ��ʼ��ʾͼƬ
	int currentImg = 0;

	public static final String TAG = "Paper";

	// ������
	// ����SensorListener��׽onSensorChanged�¼�

	// �ź���������ͬ��
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

				// ȡ��pitch�Ƕ�
				float fPitchAngle = (float) Math.toDegrees(values[1]), fRollAngle = (float) Math
						.toDegrees(values[2]);

				// LogUtil.i(TAG, "pitch:" + fPitchAngle + " roll:" +
				// fRollAngle);

				float disPitch = fPitchAngle - oldPitch, disRoll = fRollAngle
						- oldRoll;
				LogUtil.i(TAG, "dispitch:" + disPitch + " disroll:" + disRoll);

				if (Math.abs(disPitch) > 2) {// �����ת�Ƕȴ��� ,ǰ����
					if (disPitch < 0) {// �Զ�����ʼ��ת
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
					} else {// �Եײ���ʼ��ת
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
					// �޽Ƕȹ��㣡����
					gy = 0;
				}

				if (Math.abs(disRoll) > 2) {// �����ת�Ƕȴ��� ,���Ҵ���
					if (disRoll > 0) {// �������ҷ�
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

					} else {// ����ת
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

	// ʵ��WallpaperService����ʵ�ֵĳ��󷽷�
	@Override
	public Engine onCreateEngine() {
		SharedPreferences preferences = getSharedPreferences("ios7paper",
				MODE_PRIVATE);
		String myURI = preferences.getString("myURI", null);

		if (myURI == null) {

			int paperId = preferences.getInt("paperId", 0);
			if (paperId != 0) {
				currentImg = paperId - 1;// Ĭ��ֵΪ0������һ��+1�����˴�-1
			}

			// ���ر���ͼƬ
			paper = BitmapFactory.decodeResource(getResources(),
					images[currentImg]);
		} else {

			Uri uri = Uri.parse(myURI);
			ContentResolver cr = this.getContentResolver();

			try {
				// �����Զ���ͼƬ
				paper = BitmapFactory.decodeStream(cr.openInputStream(uri));
			} catch (FileNotFoundException e) {
				Log.e("Exception", e.getMessage(), e);

			}

		}

		bitW = paper.getWidth();
		bitH = paper.getHeight();

		// �����Զ����Engine
		return new MyEngine();
	}

	class MyEngine extends Engine {

		// ��¼��������Ƿ�ɼ�
		private boolean mVisible;

		// ����һ��Handler
		Handler mHandler = new Handler();

		// ����һ��������ִ�е�����
		private final Runnable drawThread = new Runnable() {
			@Override
			public void run() {
				drawFrame();
			}
		};

		public MyEngine() {
			LogUtil.i(TAG, "MyEngine's created.");

			// ��ȡ��Ļ�ֱ��ʣ�����
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

			// ָ����һ�λ�ͼ
			first = true;

			// ����,ȡ��SENSOR_SERVICE����
			mSensorManager01 = (SensorManager) getSystemService(SENSOR_SERVICE);

		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			// ɾ���ص�
			if (mHandler != null) {
				mHandler.removeCallbacks(drawThread);
			}
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			// ������ɼ�ʱ��ִ��drawFrame()������
			if (visible) {
				// ����SensorManager����
				Sensor magneticSensor = mSensorManager01
						.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
				Sensor accelerometerSensor = mSensorManager01
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

				mSensorManager01.registerListener(mSensorEventListener,
						magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
				mSensorManager01.registerListener(mSensorEventListener,
						accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

				// ��̬�ػ���ͼ��
				drawFrame();
			} else {
				// ȡ��������
				mSensorManager01.unregisterListener(mSensorEventListener);
				// �߳�˯��500ms

				try {
					Thread.sleep(500);
					// �ض�λ
					lx = bitW * 0.025f;
					ly = bitH * 0.025f;
					first = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// ������治�ɼ���ɾ���ص�
				mHandler.removeCallbacks(drawThread);

			}
		}

		// �������ͼ�εĹ��߷���
		private void drawFrame() {
			// ��ȡ�ñ�ֽ��SurfaceHolder
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas c = null;
			// �Ի�������
			c = holder.lockCanvas();

			drawTime(c);

			holder.unlockCanvasAndPost(c);

			if (mVisible) {
				// ָ�� ms�������ִ��drawThreadһ��
				mHandler.postDelayed(drawThread, 150);
			}
		}

		// ���Ĵ�����
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

				// ȷ����Խ����ʾ
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