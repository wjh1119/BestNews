package com.example.showapi;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.show.api.ShowApiRequest;

public class MainActivity extends Activity {
	protected Handler mHandler =  new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final TextView txt = (TextView) this.findViewById(R.id.textView1);
		Button myBtn = (Button) this.findViewById(R.id.button1);
		myBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new Thread(){
					//在新线程中发送网络请求
					public void run() {
						String appid="xxx";//要替换成自己的
						String secret="xxxxxxx";//要替换成自己的
						final String res=new ShowApiRequest("http://route.showapi.com/6-1",appid,secret)
						.addTextPara("num","189123456789")
						.post();
						System.out.println(res);
						//把返回内容通过handler对象更新到界面
						mHandler.post(new Thread(){
							public void run() {
								txt.setText(res+"  "+new Date());
							}
						});
					}
				}.start();
				
				
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
