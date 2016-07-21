package jp.androidgroup.nyartoolkit.sketch;

import java.util.ArrayList;

import jp.androidgroup.nyartoolkit.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class AndSketch extends Activity
{
	public interface IAndSketchEventListerner
	{
		public void onAcResume();
		public void onAcPause();
		public void onAcDestroy() throws Exception;
		public void onAcStop() throws Exception;
	}
	
	public ArrayList<IAndSketchEventListerner> _evlistener=new ArrayList<IAndSketchEventListerner>();
	
	

	public AndSketch()
	{
	}
	//Activityのハンドラ
	@Override
	protected void onResume() {
		super.onResume();
		try {
			for(IAndSketchEventListerner i : this._evlistener) {
				i.onAcResume();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	protected void onPause() {
		super.onPause();
		try {
			for(IAndSketchEventListerner i : this._evlistener) {
				i.onAcPause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(this.getClass().getName(), "onCreate");
		super.onCreate(savedInstanceState);
		this.setupDefaultActivity();
		this.setContentView(R.layout.main);
	}
	@Override
	protected void onStart()
	{
		super.onStart();
		return;
	}
	@Override
	protected void onStop()
	{
		super.onStop();
		try {
			for(IAndSketchEventListerner i : this._evlistener) {
				i.onAcStop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	protected void onDestory()
	{
		super.onDestroy();
		try {
			for(IAndSketchEventListerner i : this._evlistener) {
				i.onAcDestroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * onCreate関数からコールします。	
	 */
	protected void setupDefaultActivity()
	{
		// タイトルは不要
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// フルスクリーン表示
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		this.getWindow().setFormat(PixelFormat.TRANSLUCENT);
		// 画面がスリープに入らないようにする
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 横向き固定
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);		
	}

	public void _finish(Exception e)
	{
		if(e!=null){
			e.printStackTrace();
		}
		super.finish();
	}
}

