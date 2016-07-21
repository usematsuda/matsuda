package jp.androidgroup.nyartoolkit.utils;

import jp.androidgroup.nyartoolkit.utils.gl.AndGLDebugDump;
import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARRgbPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.NyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilter;
import jp.nyatla.nyartoolkit.core.rasterfilter.rgb2gs.INyARRgb2GsFilterRgbAve;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import android.util.Log;


/**
 * YUV420形式のbyte[]をラップするラスタです。
 * GET操作系のみの実装です。SET操作は使用できません。
 */
public class NyARAndYUV420RgbRaster extends NyARRgbRaster
{
	/**
	 * BufferedImageを外部参照したラスタを構築します。
	 * @param i_img
	 * 参照するラスタ
	 * @throws NyARException
	 */
	public NyARAndYUV420RgbRaster(int i_w,int i_h) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_w,i_h,NyARBufferType.BYTE1D_YUV420SP,false);
	}
	public Object createInterface(Class<?> i_iid) throws NyARException
	{
		//use special driver.
		if(i_iid==INyARRgb2GsFilter.class){
			//デフォルトのインタフェイス
			return new NyARRgb2GsFilterRgbAve_BYTE1D_YUV420SP(this);
		}
		//others, RGB raster driver.
		return super.createInterface(i_iid);
	}
	protected void initInstance(NyARIntSize i_size,int i_raster_type,boolean i_is_alloc) throws NyARException
	{
		//内部バッファは作れない
		if(i_is_alloc){
			throw new NyARException();
		}
		//YUV420SP以外は無効
		if(i_raster_type!=NyARBufferType.BYTE1D_YUV420SP){
			throw new NyARException();
		}
		this._buf=null;
		this._rgb_pixel_driver=new AndYUV420RgbPixelReader();
		return;
	}


	/**
	 * YUV420を格納したbyte[]をセットできます。
	 */
	public void wrapBuffer(Object i_ref_buf) throws NyARException
	{
		assert(!this._is_attached_buffer);//バッファがアタッチされていたら機能しない。
		this._buf=(byte[])i_ref_buf;
		//ピクセルドライバを更新
		this._rgb_pixel_driver.switchRaster(this);
	}
}
//
//ラスタドライバ
//
class NyARRgb2GsFilterRgbAve_BYTE1D_YUV420SP implements INyARRgb2GsFilterRgbAve
{
	private INyARRaster _ref_raster;
	public NyARRgb2GsFilterRgbAve_BYTE1D_YUV420SP(INyARRaster i_ref_raster)
	{
		assert i_ref_raster.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP);
		this._ref_raster=i_ref_raster;
	}
	public void convert(INyARGrayscaleRaster i_raster) throws NyARException
	{
		NyARIntSize s=this._ref_raster.getSize();
		this.convertRect(0,0,s.w,s.h,i_raster);
	}
	public void convertRect(int l,int t,int w,int h,INyARGrayscaleRaster o_raster) throws NyARException
	{
		NyARIntSize size=this._ref_raster.getSize();
		int bp = (l+t*size.w)*4;
		final int b=t+h;
		final int row_padding_dst=(size.w-w);
		final int row_padding_src=row_padding_dst*4;
		final int pix_count=w;
		final int pix_mod_part=pix_count-(pix_count%8);
		int dst_ptr=t*size.w+l;
		byte[] in_buf = (byte[]) this._ref_raster.getBuffer();
		switch(o_raster.getBufferType()){
		case NyARBufferType.INT1D_GRAY_8:
			int[] out_buf=(int[])o_raster.getBuffer();
			for (int y = t; y < b; y++) {
				
				int x=0;
				for (x = pix_count-1; x >=pix_mod_part; x--){
					out_buf[dst_ptr++] = (in_buf[bp] & 0xff);
					bp++;
				}
				for (;x>=0;x-=8){
					out_buf[dst_ptr+0] = (in_buf[bp+0] & 0xff);
					bp++;
					out_buf[dst_ptr+1] = (in_buf[bp+1] & 0xff);
					bp++;
					out_buf[dst_ptr+2] = (in_buf[bp+2] & 0xff);
					bp++;
					out_buf[dst_ptr+3] = (in_buf[bp+3] & 0xff);
					bp++;
					out_buf[dst_ptr+4] = (in_buf[bp+4] & 0xff);
					bp++;
					out_buf[dst_ptr+5] = (in_buf[bp+5] & 0xff);
					bp++;
					out_buf[dst_ptr+6] = (in_buf[bp+6] & 0xff);
					bp++;
					out_buf[dst_ptr+7] = (in_buf[bp+7] & 0xff);
					bp+=8;
					dst_ptr+=8;
				}
				bp+=row_padding_src;
				dst_ptr+=row_padding_dst;
			}
			return;
		case NyARBufferType.BYTE1D_YUV420SP:
			byte[] out_buf2=(byte[])o_raster.getBuffer();
			//同じバッファなら何もしない。
			if(out_buf2==in_buf){
				break;
			}
			//行単位にコピー
			for (int y = t; y < b; y++) {
				System.arraycopy(in_buf,(y*size.w+l),out_buf2,(y*size.w+l),pix_count);
			}
			return;			
		default:
			INyARGsPixelDriver out_drv=o_raster.getGsPixelDriver();
			for (int y = t; y < b; y++) {
				for (int x = 0; x<pix_count; x++){
					out_drv.setPixel(x,y,((in_buf[bp] & 0xff) + (in_buf[bp+1] & 0xff) + (in_buf[bp+2] & 0xff)) /3);
					bp+=4;
				}
				bp+=row_padding_src;
			}
			return;
		}
	}
}

/**
 * YUV420のRGBアクセスクラス。GET操作のみ対応。SET操作はできません。
 */
final class AndYUV420RgbPixelReader implements INyARRgbPixelDriver
{
	protected byte[] _ref_buf;
	private NyARIntSize _ref_size;
	private int _frame_size;
	public void getPixel(int i_x, int i_y, int[] o_rgb)
	{
		int c;
		int w=this._ref_size.w;
		byte[] buf=_ref_buf;
		int y = (0xff & ((int)buf[i_x+w*i_y])) - 16;
		if (y < 0){
			y = 0;
		}
		int uvp = this._frame_size + ((i_x>>1)+(i_y>>1)*(w/2))*2;

		int v = (0xff & buf[uvp]) - 128;
		int u = (0xff & buf[uvp+1]) - 128;
		int y1192 = 1192 * y;
		//R
		c = (y1192 + 1634 * v);
		if (c < 0) c = 0; else if (c > 262143) c = 262143;
		o_rgb[0] =((c < 0)?0:(c > 262143)?262143:c)>>10;
		//G
		c=(y1192 - 833 * v - 400 * u);
		if (c < 0) c = 0; else if (c > 262143) c = 262143;
		o_rgb[1] =((c < 0)?0:(c > 262143)?262143:c)>>10;
		//B
		c=(y1192 + 2066 * u);
		if (c < 0) c = 0; else if (c > 262143) c = 262143;
		o_rgb[2] =((c < 0)?0:(c > 262143)?262143:c)>>10;
		return;
	}
	public void getPixelSet(int[] i_x, int[] i_y, int i_num, int[] o_rgb)
	{
		int c;
		int w=this._ref_size.w;
		byte[] buf=_ref_buf;
		for (int i = i_num - 1; i >= 0; i--){
			int y = (0xff & ((int)buf[i_x[i]+w*i_y[i]])) - 16;
			if (y < 0){
				y = 0;
			}
			int uvp = this._frame_size +((i_x[i]>>1)+(i_y[i]>>1)*(w/2))*2;
			int v = (0xff & buf[uvp]) - 128;
			int u = (0xff & buf[uvp+1]) - 128;
			int y1192 = 1192 * y;
			//R
			c = (y1192 + 1634 * v);
			if (c < 0) c = 0; else if (c > 262143) c = 262143;
			o_rgb[i * 3 +0] =((c < 0)?0:(c > 262143)?262143:c)>>10;
			//G
			c=(y1192 - 833 * v - 400 * u);
			if (c < 0) c = 0; else if (c > 262143) c = 262143;
			o_rgb[i * 3 +1] =((c < 0)?0:(c > 262143)?262143:c)>>10;
			//B
			c=(y1192 + 2066 * u);
			if (c < 0) c = 0; else if (c > 262143) c = 262143;
			o_rgb[i * 3 +2] =((c < 0)?0:(c > 262143)?262143:c)>>10;
		}
		return;
	}
	public void setPixel(int i_x, int i_y, int[] i_rgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void setPixel(int i_x, int i_y, int i_r,int i_g,int i_b) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intrgb) throws NyARException
	{
		NyARException.notImplement();		
	}
	public void switchRaster(INyARRgbRaster i_raster)throws NyARException
	{
		this._ref_buf=(byte[])i_raster.getBuffer();
		this._ref_size=i_raster.getSize();
		this._frame_size=this._ref_size.w*this._ref_size.h;
	}

	public boolean isCompatibleRaster(INyARRgbRaster iRaster)
	{
		return iRaster.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP);
	}
	public NyARIntSize getSize() {
		return this._ref_size;
	}
}
