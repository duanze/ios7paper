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
	// 图片id
	int paperId;
	// 显示图片的imageView
	ImageView imageView;

	Button next, previous, select;

	// 访问图片的数组
	int[] images = new int[] { R.drawable.a1, R.drawable.a2, R.drawable.a3,
			R.drawable.a4, R.drawable.a5, R.drawable.a6, R.drawable.a7,
			R.drawable.a8, R.drawable.a9, R.drawable.a10,R.drawable.a11 };
	// 初始显示图片
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
			currentImg = paperId - 1;// 默认值为0，做一个+1处理，此处-1
		}
		imageView.setImageResource(images[currentImg]);

		// 定义查看下一张图片的监听器
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

				Toast.makeText(MyActivity.this, "选定成功！", Toast.LENGTH_SHORT)
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
		// 设置对话框标题
				.setTitle("用法").setMessage("选择喜爱的墙纸，在“动态壁纸”中启用，尽情享受惊艳美感！");
		// 为AlertDialog.Builder添加【确定】按钮
		setPositiveButton(builder).create().show();
	}

	private void feedback() {
		// 必须明确使用mailto前缀来修饰邮件地址
		Uri uri = Uri.parse("mailto:端泽<blue3434@qq.com>");
		// String[] email = {"端泽<blue3434@qq.com>"};
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		// intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
		intent.putExtra(Intent.EXTRA_SUBJECT, "重力墙纸使用反馈"); // 主题
		intent.putExtra(Intent.EXTRA_TEXT, ""); // 正文
		startActivity(Intent.createChooser(intent, "请选择邮件应用"));
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
		// 设置对话框标题
				.setTitle("关于").setMessage("重力墙纸Version 2.3.0\nCoded by Duanze");
		// 为AlertDialog.Builder添加【确定】按钮
		setPositiveButton(builder).create().show();

	}

	/**
	 * 调用看图程序自定义墙纸
	 */
	private void other() {
		Intent intent = new Intent();
		/* 开启Pictures画面Type设定为image */
		intent.setType("image/*");
		/* 使用Intent.ACTION_GET_CONTENT这个Action */
		intent.setAction(Intent.ACTION_GET_CONTENT);
		/* 取得相片后返回本画面 */
		startActivityForResult(intent, 1);
	}

	/**
	 * 为builder添加确定按钮并返回其引用
	 * 
	 * @param builder
	 *            AlertDialog.Builder
	 */
	private AlertDialog.Builder setPositiveButton(AlertDialog.Builder builder) {
		// 定义为不可取消
		builder.setCancelable(false);
		// 调用setPositiveButton方法添加确定按钮
		return builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	}
}
