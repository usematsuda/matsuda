/**
 * NyARToolkit for Android SDK
 *   Copyright (C)2012 NyARToolkit for Android team
 *   Copyright (C)2012 R.Iizuka(nyatla)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://sourceforge.jp/projects/nyartoolkit-and/
 * 
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 *    http://www.hitl.washington.edu/artoolkit/
 *   Ryo Iizuka(nyatla)
 *    http://nyatla.jp/nyatoolkit/
 * 
 * Contributor(s)
 *  Atsuo Igarashi
 *  Yasuhide Matsumoto
 *  Fuu Rokubou
 */
package jp.androidgroup.nyartoolkit.utils.camera;

import java.io.IOException;
import java.util.List;

import jp.androidgroup.nyartoolkit.sketch.AndSketch;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * CameraPreviewの制御クラスです。
 * 開始、停止、フレームの非同期通知を制御します。
 * {@link #start},{@link #stop},{@link #getCurrentBuffer()}関数は同時できません。
 * コールバック関数{@link #onPreviewFrame}の中から、これらの関数を使用できません。
 * 
 * 
 * 
 * しないようにしてください。
 * @author nyatla
 *
 */
public class CameraPreview extends SurfaceView implements AndSketch.IAndSketchEventListerner,SurfaceHolder.Callback,Camera.PreviewCallback
{
	public interface IOnPreviewFrame
	{
		public void onPreviewFrame(byte[] i_frame);
	}
	private Camera _camera_ref;
	private SurfaceHolder _holder;
	private boolean _is_enabled=false;
	private byte[][] _cap_buf;
	private int _cap_index;
	private IOnPreviewFrame _callback;
	/**
	 * Must call in onSetup
	 * @param context
	 */
	public CameraPreview(AndSketch context)
	{
		super(context);
		try {
			context._evlistener.add(this);
			this._camera_ref=Camera.open();
			if(this._is_enabled){
				this._camera_ref.setPreviewDisplay(this._holder);
				this._camera_ref.startPreview();
			}			
			
			this._holder = getHolder();
			this._holder.addCallback(this);
			this._holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			((AndSketch)this.getContext())._finish(e);
		}
	}

	public void start(int i_w,int i_h,int i_fps,IOnPreviewFrame i_callback) throws IOException
	{
		assert i_callback!=null;
		//cameraのプレビューサイズを設定
		Camera.Parameters cparam=this._camera_ref.getParameters();
		cparam.setPreviewFormat(ImageFormat.NV21);
		cparam.setPreviewSize(i_w, i_h);
		cparam.setPreviewFrameRate(i_fps);
		this._camera_ref.setParameters(cparam);
		this._callback=i_callback;

		if(this.isEnabled()){
			this._camera_ref.setPreviewDisplay(this._holder);
		}
		this._cap_buf=new byte[2][cparam.getPreviewSize().width*cparam.getPreviewSize().height*ImageFormat.getBitsPerPixel(ImageFormat.NV21)/8];
		this._camera_ref.setPreviewCallbackWithBuffer(this);
		this._cap_index=1;
		this._camera_ref.startPreview();
		this._camera_ref.addCallbackBuffer(this._cap_buf[this._cap_index]);
		this._is_enabled=true;
	}
	public void stop() throws IOException
	{
		this._camera_ref.setPreviewDisplay(null);
		this._camera_ref.stopPreview();		
		this._is_enabled=false;
	}
	/**
	 * 現在のフレームを格納したバッファを返す。
	 * @return
	 */
	public byte[] getCurrentBuffer()
	{
		return this._cap_buf[(this._cap_index+1)%2];
	}
	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		this._cap_index=(this._cap_index+1)%2;
		//callEvent;
		this._callback.onPreviewFrame(data);
		this._camera_ref.addCallbackBuffer(this._cap_buf[this._cap_index]);
	}		
	/**
	 * 指定したFPSに最も近いFPSを返す。
	 * @param i_target_fps
	 * @return
	 */
	public int getSupportedFpsTolerance(int i_target_fps)
	{
		List<Integer> l=this._camera_ref.getParameters().getSupportedPreviewFrameRates();
		int ret=0;
		for (Integer i : l) {
			if(ret==0){
				ret=i;
				continue;
			}
			if(Math.abs(ret-i_target_fps)>Math.abs(i-i_target_fps)){
				ret=i;
			}
		}
		return ret;
	}
	/**
	 * 与えたプレビューサイズに以上で最も小さいものを探す
	 * @return
	 */
	public Size getRecommendPreviewSize(int i_w,int i_h)
	{
		// カメラのキャプチャサイズのリストを取得
		List<Size> sizes = this._camera_ref.getParameters().getSupportedPreviewSizes();
		
		// 適切なサイズはどれかな？
		Size ret=null;
		for (Size size : sizes){
			if(i_w>size.width || i_h>size.height){
				continue;
			}
			if(ret==null){
				ret=size;
				continue;
			}
			if(ret.width>=size.width && ret.height>=size.height){
				ret=size;
			}
		}
		return ret;
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
	{
		//surface ready!
		return;
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if(this._is_enabled){
			try {
				this._camera_ref.setPreviewDisplay(this._holder);
			} catch (IOException e) {
				((AndSketch)this.getContext())._finish(e);
			}
		}
		return;
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		try {
			if(this._camera_ref!=null){
				this._camera_ref.setPreviewDisplay(null);
			}
		} catch (IOException e) {
			((AndSketch)this.getContext())._finish(e);
		}
	}
	public synchronized void onAcStop() throws Exception
	{
		if(this._camera_ref!=null){
			this._camera_ref.stopPreview();		
			this._camera_ref.setPreviewCallbackWithBuffer(null);
			this._camera_ref.setPreviewDisplay(null);
			this._camera_ref.release();
			this._camera_ref=null;
		}
	}
	@Override
	public void onAcDestroy() throws Exception
	{
		assert(this._camera_ref==null);
	}
	@Override
	public void onAcResume()
	{
	}
	@Override
	public void onAcPause()
	{
	}
}
