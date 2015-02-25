package com.duanze.ios7paper;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MyActivity extends BaseActivity {

	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	// ͼƬid
	int paperId;
	// ��ʾͼƬ��imageView
	ImageView imageView;

	Button next, previous, select;

	// ����ͼƬ������
	int[] images = new int[] { R.drawable.a1, R.drawable.a2, R.drawable.a3,
			R.drawable.a4, R.drawable.a5, R.drawable.a6, R.drawable.a7,
			R.drawable.a8, R.drawable.a9, R.drawable.a10,R.drawable.a11 };
	// ��ʼ��ʾͼƬ
	int currentImg = 0;

	public static void actionStart(Context context) {
		Intent intent = new Intent(context, MyActivity.class);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);

		setContentView(R.layout.layout7);
		imageView = (ImageView) findViewById(R.id.image1);
		next = (Button) findViewById(R.id.next);
		previous = (Button) findViewById(R.id.previous);
		select = (Button) findViewById(R.id.select);

		preferences = getSharedPreferences("ios7paper", MODE_PRIVATE);

		paperId = preferences.getInt("paperId", 0);
		if (paperId != 0) {
			currentImg = paperId - 1;// Ĭ��ֵΪ0����һ��+1�����˴�-1
		}
		imageView.setImageResource(images[currentImg]);

		// ����鿴��һ��ͼƬ�ļ�����
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imageView
						.setImageResource(images[++currentImg % images.length]);
			}
		});

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (currentImg == 0)
					currentImg = images.length;

				imageView
						.setImageResource(images[--currentImg % images.length]);

			}
		});

		select.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editor = preferences.edit();
				editor.putInt("paperId", currentImg % images.length + 1);
				editor.putString("myURI", null);
				editor.commit();

				Toast.makeText(MyActivity.this, "ѡ���ɹ���", Toast.LENGTH_SHORT)
						.show();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String myURI = data.getData().toString();
				MyActivity2.actionStart(MyActivity.this, myURI);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mi) {
		switch (mi.getItemId()) {
		case R.id.guide:
			guide();
			break;
		case R.id.feedback:
			feedback();
			break;
		case R.id.evaluate:
			evaluate(MyActivity.this);
			break;
		case R.id.about:
			about();
			break;
		case R.id.action_plus:
			other();
			break;
		default:
			break;
		}

		return true;
	}

	private void guide() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		// ���öԻ������
				.setTitle("�÷�").setMessage("ѡ��ϲ����ǽֽ���ڡ���̬��ֽ�������ã��������ܾ������У�");
		// ΪAlertDialog.Builder��ӡ�ȷ������ť
		setPositiveButton(builder).create().show();
	}

	private void feedback() {
		// ������ȷʹ��mailtoǰ׺�������ʼ���ַ
		Uri uri = Uri.parse("mailto:����<blue3434@qq.com>");
		// String[] email = {"����<blue3434@qq.com>"};
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		// intent.putExtra(Intent.EXTRA_CC, email); // ������
		intent.putExtra(Intent.EXTRA_SUBJECT, "����ǽֽʹ�÷���"); // ����
		intent.putExtra(Intent.EXTRA_TEXT, ""); // ����
		startActivity(Intent.createChooser(intent, "��ѡ���ʼ�Ӧ��"));
	}

	private void evaluate(Context context) {
		Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "Couldn't launch the market !",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void about() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		// ���öԻ������
				.setTitle("����").setMessage("����ǽֽVersion 2.3.0\nCoded by Duanze");
		// ΪAlertDialog.Builder��ӡ�ȷ������ť
		setPositiveButton(builder).create().show();

	}

	/**
	 * ���ÿ�ͼ�����Զ���ǽֽ
	 */
	private void other() {
		Intent intent = new Intent();
		/* ����Pictures����Type�趨Ϊimage */
		intent.setType("image/*");
		/* ʹ��Intent.ACTION_GET_CONTENT���Action */
		intent.setAction(Intent.ACTION_GET_CONTENT);
		/* ȡ����Ƭ�󷵻ر����� */
		startActivityForResult(intent, 1);
	}

	/**
	 * Ϊbuilder���ȷ����ť������������
	 * 
	 * @param builder
	 *            AlertDialog.Builder
	 */
	private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder) {
		// ����Ϊ����ȡ��
		builder.setCancelable(false);
		// ����setPositiveButton�������ȷ����ť
		return builder.setPositiveButton("ȷ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	}
}
