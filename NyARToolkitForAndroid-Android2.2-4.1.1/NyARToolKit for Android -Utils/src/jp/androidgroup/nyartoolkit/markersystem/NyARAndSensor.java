package jp.androidgroup.nyartoolkit.markersystem;

import java.io.IOException;

import jp.androidgroup.nyartoolkit.utils.NyARAndYUV420GsRaster;
import jp.androidgroup.nyartoolkit.utils.NyARAndYUV420RgbRaster;
import jp.androidgroup.nyartoolkit.utils.camera.CameraPreview;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;
/**
 * この関数は、非同期カメライベントをラップした{@see NyARSensor}です。
 * 画像取得APIにアクセスするときには、オブジェクトをsynchronizedしてください。
 */
public class NyARAndSensor extends NyARSensor implements CameraPreview.IOnPreviewFrame
{
	private int _fps;
	private CameraPreview _cpv_ref;
	private NyARAndYUV420RgbRaster _rgb_raster;
	/**
	 * カメラプレビューをバインドしたインスタンスを作ります。
	 * @param i_preview
	 * 制御するカメラプレビューオブジェクト。このインスタンスの所有権はインスタンスに移ります。
	 * @param i_w
	 * 映像の幅
	 * @param i_h
	 * 映像の高さ
	 * @param i_fps
	 * 取得レート
	 * @throws NyARException
	 */
	public NyARAndSensor(CameraPreview i_preview,int i_w,int i_h,int i_fps) throws NyARException
	{
		super(new NyARIntSize(i_w,i_h));
		this._cpv_ref=i_preview;
		this._rgb_raster=new NyARAndYUV420RgbRaster(i_w,i_h);
		this._fps=i_fps;
		//Listenerついかしとく？
	}
	protected void initResource(NyARIntSize s) throws NyARException
	{
		this._gs_raster=new NyARAndYUV420GsRaster(s.w,s.h);
	}
	/**
	 * CameraPreviewからの非同期コールバックハンドラです。
	 * 呼び出さないでください。
	 * @param data
	 * @param camera
	 */
	@Override
	public void onPreviewFrame(byte[] i_frame)
	{
		//RGBにセット。
		synchronized(this)
		{
			try {
				this._rgb_raster.wrapBuffer(i_frame);
				this._gs_raster.wrapBuffer(i_frame);
				this.update(this._rgb_raster);
			} catch (NyARException e){
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 映像の非同期更新を開始します。
	 * @throws NyARException
	 */
	public void start() throws NyARException
	{
		try{
			synchronized(this){
				NyARIntSize s=this._rgb_raster.getSize();
				this._cpv_ref.start(s.w,s.h,this._fps,this);
				byte[] frame=this._cpv_ref.getCurrentBuffer();
				this._rgb_raster.wrapBuffer(frame);
				this._gs_raster.wrapBuffer(frame);
			}
		}catch(Exception e){
			throw new NyARException();
		}
	}
	/**
	 * 映像の非同期更新を停止します。
	 * @throws NyARException
	 */
	public void stop() throws NyARException
	{
		try {
			synchronized(this){
				this._cpv_ref.stop();
			}
		} catch (IOException e) {
			throw new NyARException();
		}
	}	
	public INyARGrayscaleRaster getGsImage() throws NyARException
	{
		return super.getGsImage();
//		//必要に応じてグレースケール画像の生成
//		if(this._src_ts!=this._gs_id_ts){
//			this._gs_id_ts=this._src_ts;
//		}
//		return this._gs_raster;
		//
	}
}
