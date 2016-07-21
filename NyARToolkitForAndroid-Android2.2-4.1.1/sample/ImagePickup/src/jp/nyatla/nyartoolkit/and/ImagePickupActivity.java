package jp.nyatla.nyartoolkit.and;

import javax.microedition.khronos.opengles.GL10;

import jp.androidgroup.nyartoolkit.R;
import jp.androidgroup.nyartoolkit.markersystem.NyARAndMarkerSystem;
import jp.androidgroup.nyartoolkit.markersystem.NyARAndSensor;
import jp.androidgroup.nyartoolkit.sketch.AndSketch;
import jp.androidgroup.nyartoolkit.utils.camera.CameraPreview;
import jp.androidgroup.nyartoolkit.utils.gl.*;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

/**
 * Hiroマーカ平面から画像を取得します。
 * 画像取得APIは低速です。解像度の高いものは避けてください。
 * 撮影画面全体のキャプチャをするときは、CameraPreviewから直接画像を得た方が良いです。
 *
 */
public class ImagePickupActivity extends AndSketch implements AndGLView.IGLFunctionEvent
{
	CameraPreview _camera_preview;
	AndGLView _glv;
	Camera.Size _cap_size;
	/**
	 * onStartでは、Viewのセットアップをしてください。
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		FrameLayout fr=((FrameLayout)this.findViewById(R.id.sketchLayout));
		//カメラの取得
		this._camera_preview=new CameraPreview(this);
		this._cap_size=this._camera_preview.getRecommendPreviewSize(320,240);
		//画面サイズの計算
		int h = this.getWindowManager().getDefaultDisplay().getHeight();
		int screen_w,screen_h;
		screen_w=(this._cap_size.width*h/this._cap_size.height);
		screen_h=h;
		//camera
		fr.addView(this._camera_preview, 0, new LayoutParams(screen_w,screen_h));
		//GLview
		this._glv=new AndGLView(this);
		fr.addView(this._glv, 0,new LayoutParams(screen_w,screen_h));
		
	}

	NyARAndSensor _ss;
	NyARAndMarkerSystem _ms;
	private int _mid;
	AndGLFpsLabel fps;
	AndGLBitmapSprite _bmp;
	AndGLDebugDump _debug=null;
	AndGLTextLabel text;
	public void setupGL(GL10 gl)
	{
		try
		{
			AssetManager assetMng = getResources().getAssets();
			//create sensor controller.
			this._ss=new NyARAndSensor(this._camera_preview,this._cap_size.width,this._cap_size.height,30);
			//create marker system
			this._ms=new NyARAndMarkerSystem(new NyARMarkerSystemConfig(this._cap_size.width,this._cap_size.height));
			this._mid=this._ms.addARMarker(assetMng.open("AR/data/hiro.pat"),16,25,80);
			this._ss.start();
			//setup openGL Camera Frustum
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadMatrixf(this._ms.getGlProjectionMatrix(),0);
			this.fps=new AndGLFpsLabel(this._glv,"MarkerPlaneActivity");
			this.fps.prefix=this._cap_size.width+"x"+this._cap_size.height+":";
			this._bmp=new AndGLBitmapSprite(this._glv,64,64);
			this._debug=new AndGLDebugDump(this._glv);
			this.text=new AndGLTextLabel(this._glv);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.finish();
		}
	}
	/**
	 * 継承したクラスで表示したいものを実装してください
	 * @param gl
	 */
	public void drawGL(GL10 gl)
	{
		try{
			//背景塗り潰し色の指定
			gl.glClearColor(0,0,0,0);
	        //背景塗り潰し
	        gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);
	        if(ex!=null){
	        	_debug.draw(ex);
	        	return;
	        }
	        this.fps.draw(0,0);
			synchronized(this._ss){
				this._ms.update(this._ss);
				if(this._ms.isExistMarker(this._mid)){
			        this.text.draw("found"+this._ms.getConfidence(this._mid),0,16);
					this._ms.getMarkerPlaneImage(this._mid,this._ss,-40,-40,80,80,this._bmp.lockBitmap());
					this._bmp.unlockBitmap();
					this._bmp.draw(0,50);
				}
			}
		}catch(Exception e)
		{
			ex=e;
		}
	}
	Exception ex=null;
}
