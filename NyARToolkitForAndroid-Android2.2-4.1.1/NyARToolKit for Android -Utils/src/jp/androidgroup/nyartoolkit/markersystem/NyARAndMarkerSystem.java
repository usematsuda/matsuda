package jp.androidgroup.nyartoolkit.markersystem;

import android.graphics.Bitmap;
import jp.androidgroup.nyartoolkit.utils.NyARAndBitmapRaster;
import jp.androidgroup.nyartoolkit.utils.gl.NyARGLUtil;
import jp.nyatla.nyartoolkit.core.NyARCode;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARPerspectiveCopy;
import jp.nyatla.nyartoolkit.markersystem.INyARMarkerSystemConfig;
import jp.nyatla.nyartoolkit.markersystem.INyARSingleCameraSystemObserver;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystem;
import jp.nyatla.nyartoolkit.markersystem.NyARSensor;

public class NyARAndMarkerSystem extends NyARMarkerSystem
{
	//状態変化を検知するためのObserver(簡易実装してある_projection_matの同期用)
	private class Observer implements INyARSingleCameraSystemObserver
	{
		private NyARAndMarkerSystem _parent;
		public Observer(NyARAndMarkerSystem i_parent)
		{
			this._parent=i_parent;
		}
		public void onUpdateCameraParametor(NyARParam i_param, double i_near,double i_far)
		{
			NyARGLUtil.toCameraFrustumRH(i_param,1,i_near,i_far,this._parent._projection_mat);
		}
	}

	public NyARAndMarkerSystem(INyARMarkerSystemConfig i_config)
			throws NyARException
	{
		super(i_config);
	}
	private float[] _projection_mat;
	private Observer _obs;
	protected void initInstance(INyARMarkerSystemConfig i_config) throws NyARException
	{
		super.initInstance(i_config);
		this._projection_mat=new float[16];
		this._obs=new Observer(this);
		this.addObserver(this._obs);
	}
	/**
	 * {@link #addARMarker(INyARRgbRaster, int, int, double)}のラッパーです。Bitmapからマーカパターンを作ります。
	 * 引数については、{@link #addARMarker(INyARRgbRaster, int, int, double)}を参照してください。
	 * @param i_img
	 * @param i_patt_resolution
	 * @param i_patt_edge_percentage
	 * @param i_marker_size
	 * @return
	 * @throws NyARException
	 */
	public int addARMarker(Bitmap i_img,int i_patt_resolution,int i_patt_edge_percentage,double i_marker_size) throws NyARException
	{
		int w=i_img.getWidth();
		int h=i_img.getHeight();
		NyARAndBitmapRaster bmr=new NyARAndBitmapRaster(i_img.getWidth(),i_img.getHeight());
		bmr.wrapBuffer(i_img);
		NyARCode c=new NyARCode(i_patt_resolution,i_patt_resolution);
		//ラスタからマーカパターンを切り出す。
		INyARPerspectiveCopy pc=(INyARPerspectiveCopy)bmr.createInterface(INyARPerspectiveCopy.class);
		NyARRgbRaster tr=new NyARRgbRaster(i_patt_resolution,i_patt_resolution);
		pc.copyPatt(0,0,w,0,w,h,0,h,i_patt_edge_percentage, i_patt_edge_percentage,4, tr);
		//切り出したパターンをセット
		c.setRaster(tr);
		return super.addARMarker(c,i_patt_edge_percentage,i_marker_size);
	}
	/**
	 * OpenGLスタイルのProjectionMatrixを返します。
	 * @param i_gl
	 * @return
	 * [readonly]
	 */
	public float[] getGlProjectionMatrix()
	{
		return this._projection_mat;
	}
	/**
	 * この関数は、i_bufに指定idのOpenGL形式の姿勢変換行列を設定して返します。
	 * @param i_id
	 * @param i_buf
	 * @return
	 * @throws NyARException 
	 */
	public void getMarkerMatrix(int i_id,float[] i_buf) throws NyARException
	{
		NyARGLUtil.toCameraViewRH(this.getMarkerMatrix(i_id),1,i_buf);
	}
	/**
	 * この関数はOpenGL形式の姿勢変換行列を新規に割り当てて返します。
	 * @param i_buf
	 * @return
	 * @throws NyARException 
	 */
	public float[] getGlMarkerMatrix(int i_id) throws NyARException
	{
		float[] b=new float[16];
		this.getMarkerMatrix(i_id,b);
		return b;
	}
	/**
	 * この関数は、{@link #getMarkerPlaneImage(int, NyARSensor, int, int, int, int, int, int, int, int, INyARRgbRaster)}
	 * のラッパーです。取得画像を{@link #BufferedImage}形式で返します。
	 * @param i_id
	 * @param i_sensor
	 * @param i_x1
	 * @param i_y1
	 * @param i_x2
	 * @param i_y2
	 * @param i_x3
	 * @param i_y3
	 * @param i_x4
	 * @param i_y4
	 * @param i_img
	 * @return
	 * @throws NyARException
	 */
	public void getMarkerPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_x1,int i_y1,
	    int i_x2,int i_y2,
	    int i_x3,int i_y3,
	    int i_x4,int i_y4,
	    Bitmap i_img) throws NyARException
		{
			NyARAndBitmapRaster bmr=new NyARAndBitmapRaster(i_img.getWidth(),i_img.getHeight());
			bmr.wrapBuffer(i_img);
			super.getMarkerPlaneImage(i_id, i_sensor, i_x1, i_y1, i_x2, i_y2, i_x3, i_y3, i_x4, i_y4,bmr);
			return;
		}
	/**
	 * この関数は、{@link #getMarkerPlaneImage(int, NyARSensor, int, int, int, int, INyARRgbRaster)}
	 * のラッパーです。取得画像を{@link #BufferedImage}形式で返します。
	 * @param i_id
	 * マーカid
	 * @param i_sensor
	 * 画像を取得するセンサオブジェクト。通常は{@link #update(NyARSensor)}関数に入力したものと同じものを指定します。
	 * @param i_l
	 * @param i_t
	 * @param i_w
	 * @param i_h
	 * @param i_raster
	 * 出力先のオブジェクト
	 * @throws NyARException
	 */
	public void getMarkerPlaneImage(
		int i_id,
		NyARSensor i_sensor,
	    int i_l,int i_t,
	    int i_w,int i_h,
	    Bitmap i_img) throws NyARException
    {
		NyARAndBitmapRaster bmr=new NyARAndBitmapRaster(i_img.getWidth(),i_img.getHeight());
		bmr.wrapBuffer(i_img);
		super.getMarkerPlaneImage(i_id, i_sensor, i_l, i_t, i_w, i_h, bmr);
		this.getMarkerPlaneImage(i_id,i_sensor,i_l+i_w-1,i_t+i_h-1,i_l,i_t+i_h-1,i_l,i_t,i_l+i_w-1,i_t,bmr);
		return;
    }	
	
	
}
