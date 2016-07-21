package jp.androidgroup.nyartoolkit.utils;

import android.graphics.Bitmap;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class NyARAndBitmapRaster extends NyARRgbRaster
{
	/**
	 * BufferedImageを外部参照したラスタを構築します。
	 * @param i_img
	 * 参照するラスタ
	 * @throws NyARException
	 */
	public NyARAndBitmapRaster(int i_w,int i_h) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_w,i_h,NyARBufferType.OBJECT_And_Bitmap,false);
	}
	public Object createInterface(Class<?> i_iid) throws NyARException
	{
		//low speed interface only.
		//必要なら実装してね。
		return super.createInterface(i_iid);
	}
	protected void initInstance(NyARIntSize i_size,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		//内部バッファは作れない
		if(i_is_alloc){
			throw new NyARException();
		}
		//YUV420SP以外は無効
		if(i_raster_type!=NyARBufferType.OBJECT_And_Bitmap){
			throw new NyARException();
		}
		this._buf=null;
		this._rgb_pixel_driver=new AndBitmapRasterReader();
		return;
	}


	/**
	 * Bitmapをセットできます。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=(Bitmap)i_ref_buf;
		//ピクセルドライバを更新
		this._rgb_pixel_driver.switchRaster(this);
	}
}


/**
 * BitmapのRGBアクセスクラス。
 */
final class AndBitmapRasterReader implements INyARRgbPixelDriver
{
	protected Bitmap _ref_buf;
	private NyARIntSize _ref_size;
	public void getPixel(int i_x, int i_y, int[] o_rgb)
	{
		Bitmap buf=_ref_buf;
		int p=buf.getPixel(i_x,i_y);
		o_rgb[0] =0xff &(p>>16);
		o_rgb[1] =0xff &(p>>8);
		o_rgb[2] =0xff &(p);
		return;
	}
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		Bitmap buf=_ref_buf;
		for (int i = i_num - 1; i >= 0; i--){
			int p=buf.getPixel(i_x[i],i_y[i]);
			o_rgb[i*3+0] =0xff &(p>>16);
			o_rgb[i*3+1] =0xff &(p>>8);
			o_rgb[i*3+2] =0xff &(p);
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		Bitmap buf=this._ref_buf;
		buf.setPixel(i_x,i_y, 0xff000000|(0xff0000 &(i_rgb[0]<<16))|(0x00ff00 &(i_rgb[1]<<8))|(0x0000ff &(i_rgb[2])));
	}
	public void setPixel(int i_x, int i_y, int i_r,int i_g,int i_b) throws NyARException
	{
		Bitmap buf=this._ref_buf;
		buf.setPixel(i_x,i_y, 0xff000000|(0xff0000 &(i_r<<16))|(0x00ff00 &(i_g<<8))|(0x0000ff &(i_b)));
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		Bitmap buf=this._ref_buf;
		for(int i=0;i<i_num;i++){
			buf.setPixel(i_x[i],i_y[i], 0xff000000|(0xff0000 &(i_intrgb[0+i*3]<<16))|(0x00ff00 &(i_intrgb[1+i*3]<<8))|(0x0000ff &(i_intrgb[2+i*3])));
		}
	}
	public void switchRaster(INyARRgbRaster i_raster)throws NyARException
	{
		this._ref_buf=(Bitmap)i_raster.getBuffer();
		this._ref_size=i_raster.getSize();
	}

	public boolean isCompatibleRaster(INyARRgbRaster iRaster)
	{
		return iRaster.isEqualBufferType(NyARBufferType.OBJECT_And_Bitmap);
	}
	public NyARIntSize getSize() {
		return this._ref_size;
	}
}
