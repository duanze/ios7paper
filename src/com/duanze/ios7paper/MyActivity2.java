package com.duanze.ios7paper;

import java.io.FileNotFoundException;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MyActivity2 extends BaseActivity {

	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	Uri uri;
	String myURI;
	// ��ʾͼƬ��imageView
	ImageView imageView2;

	Button cancel, select, other;

	public static void actionStart(Context context, String myURI) {
		Intent intent = new Intent(context, MyActivity2.class);
		intent.putExtra("myURI", myURI);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);

		setContentView(R.layout.mypaper);

		imageView2 = (ImageView) findViewById(R.id.image2);
		select = (Button) findViewById(R.id.select2);
		other = (Button) findViewById(R.id.otherpaper2);

		// ActionBar ����
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		myURI = intent.getStringExtra("myURI");
		uri = Uri.parse(myURI);
		ContentResolver cr = this.getContentResolver();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
			imageView2.setImageBitmap(bitmap);
		} catch (FileNotFoundException e) {
			Log.e("Exception", e.getMessage(), e);
		}

		preferences = getSharedPreferences("ios7paper", MODE_PRIVATE);

		// �Զ���ǽֽ
		other.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				/* ����Pictures����Type�趨Ϊimage */
				intent.setType("image/*");
				/* ʹ��Intent.ACTION_GET_CONTENT���Action */
				intent.setAction(Intent.ACTION_GET_CONTENT);
				/* ȡ����Ƭ�󷵻ر����� */
				startActivityForResult(intent, 2);
			}
		});

		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// ��URI����SharedPreferences
				myURI = uri.toString();
				LogUtil.i("MyURI is:", myURI);
				editor = preferences.edit();
				editor.putString("myURI", myURI);
				editor.commit();

				Toast.makeText(MyActivity2.this, "�Զ���ɹ���", Toast.LENGTH_SHORT)
						.show();
			}
		});

	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 2) {
			if (resultCode == RESULT_OK) {

				uri = data.getData();
				ContentResolver cr = this.getContentResolver();

				try {
					Bitmap bitmap = BitmapFactory.decodeStream(cr
							.openInputStream(uri));
					imageView2.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					Log.e("Exception", e.getMessage(), e);
				}

			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return true;
		}
	}

}
