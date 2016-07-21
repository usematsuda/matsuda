package jp.androidgroup.nyartoolkit.utils;

import jp.nyatla.nyartoolkit.core.NyARException;
import jp.nyatla.nyartoolkit.core.labeling.rlelabeling.NyARLabeling_Rle;
import jp.nyatla.nyartoolkit.core.pixeldriver.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.core.raster.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.raster.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.INyARHistogramFromRaster;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARContourPickup;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARHistogram;
import jp.nyatla.nyartoolkit.core.types.NyARIntCoordinates;
import jp.nyatla.nyartoolkit.core.types.NyARIntPoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;


/**
 * YUV420形式のbyte[]をラップするラスタです。
 * GET操作系のみの実装です。SET操作は使用できません。
 */
public class NyARAndYUV420GsRaster extends NyARGrayscaleRaster
{
	/**
	 * BufferedImageを外部参照したラスタを構築します。
	 * @param i_img
	 * 参照するラスタ
	 * @throws NyARException
	 */
	public NyARAndYUV420GsRaster(int i_w,int i_h) throws NyARException
	{
		//NyARToolkit互換のラスタを定義する。
		super(i_w,i_h,NyARBufferType.BYTE1D_YUV420SP,false);
	}
	public Object createInterface(Class<?> i_iid) throws NyARException
	{
		//YUV420SP以外は無効
		if(!this.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP)){
			throw new NyARException();
		}		
		if(i_iid==NyARLabeling_Rle.IRasterDriver.class){
			return new NyARRlePixelDriver_YUV420Reader(this);
		}
		if(i_iid==NyARContourPickup.IRasterDriver.class){
			return new NyARContourPickup_YUV420Reader(this);
		}
		if(i_iid==INyARHistogramFromRaster.class){
			return new NyARHistogramFromRaster_YUV420(this);
		}
		throw new NyARException();
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
		this._pixdrv=new AndYUV420GsPixelReader();
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
		this._pixdrv.switchRaster(this);
	}
}

class NyARHistogramFromRaster_YUV420 implements INyARHistogramFromRaster
{
	private INyARRaster _gsr;
	public NyARHistogramFromRaster_YUV420(INyARRaster i_raster)
	{
		assert i_raster.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP);
		this._gsr=i_raster;
	}
	public void createHistogram(int i_skip,NyARHistogram o_histogram) throws NyARException
	{
		NyARIntSize s=this._gsr.getSize();
		this.createHistogram(0,0,s.w,s.h,i_skip,o_histogram);
	}
	public void createHistogram(int i_l,int i_t,int i_w,int i_h,int i_skip,NyARHistogram o_histogram)
	{
		o_histogram.reset();
		final byte[] buf=(byte[])this._gsr.getBuffer();
		NyARIntSize s=this._gsr.getSize();
		int skip=(i_skip*s.w-i_w);
		final int pix_count=i_w;
		final int pix_mod_part=pix_count-(pix_count%8);			
		//左上から1行づつ走査していく
		int pt=(i_t*s.w+i_l);
		int[] data=o_histogram.data;
		for (int y = i_h-1; y >=0 ; y-=i_skip){
			int x;
			for (x = pix_count-1; x >=pix_mod_part; x--){
				data[0xff & buf[pt++]]++;
			}
			for (;x>=0;x-=8){
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
				data[0xff & buf[pt++]]++;
			}
			//スキップ
			pt+=skip;
		}
		o_histogram.total_of_data=i_w*i_h/i_skip;
		return;
	}
}
//
//ラスタドライバ
//
abstract class NyARContourPickup_Base implements NyARContourPickup.IRasterDriver
{
	//巡回参照できるように、テーブルを二重化
	//                                           0  1  2  3  4  5  6  7   0  1  2  3  4  5  6
	/** 8方位探索の座標マップ*/
	protected final static int[] _getContour_xdir = { 0, 1, 1, 1, 0,-1,-1,-1 , 0, 1, 1, 1, 0,-1,-1};
	/** 8方位探索の座標マップ*/
	protected final static int[] _getContour_ydir = {-1,-1, 0, 1, 1, 1, 0,-1 ,-1,-1, 0, 1, 1, 1, 0};
	public abstract boolean getContour(int i_l, int i_t, int i_r, int i_b, int i_entry_x, int i_entry_y, int i_th, NyARIntCoordinates o_coord) throws NyARException;	
}
/**
 * (INT_BIN_8とINT_GS_8に対応)
 */
class NyARContourPickup_YUV420Reader extends NyARContourPickup_Base
{

	private INyARGrayscaleRaster _ref_raster;
	public NyARContourPickup_YUV420Reader(INyARGrayscaleRaster i_ref_raster)
	{
		assert i_ref_raster.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP);
		this._ref_raster=i_ref_raster;
	}
	public boolean getContour(int i_l,int i_t,int i_r,int i_b,int i_entry_x,int i_entry_y,int i_th,NyARIntCoordinates o_coord) throws NyARException
	{
		assert(i_t<=i_entry_x);
		byte[] buf=(byte[])this._ref_raster.getBuffer();
		int w=this._ref_raster.getWidth();
		final int[] xdir = _getContour_xdir;// static int xdir[8] = { 0, 1, 1, 1, 0,-1,-1,-1};
		final int[] ydir = _getContour_ydir;// static int ydir[8] = {-1,-1, 0, 1, 1, 1, 0,-1};
		//クリップ領域の上端に接しているポイントを得る。
		NyARIntPoint2d[] coord=o_coord.items;
		int max_coord=o_coord.items.length;
		coord[0].x = i_entry_x;
		coord[0].y = i_entry_y;
		int coord_num = 1;
		int dir = 5;

		int c = i_entry_x;
		int r = i_entry_y;
		for (;;) {
			dir = (dir + 5) % 8;//dirの正規化
			//境界に接しているとき
			int i;
			for (i = 0; i < 8; i++){				
				final int x=c + xdir[dir];
				final int y=r + ydir[dir];
				//境界チェック
				if(x>=i_l && x<=i_r && y>=i_t && y<=i_b){
					if ((0xff & buf[x+y*w]) <= i_th) {
						break;
					}
				}
				dir++;//倍長テーブルを参照するので問題なし
			}
			if (i == 8) {
				//8方向全て調べたけどラベルが無いよ？
				throw new NyARException();// return(-1);
			}				
			// xcoordとycoordをc,rにも保存
			c = c + xdir[dir];
			r = r + ydir[dir];
			coord[coord_num].x = c;
			coord[coord_num].y = r;
			//終了条件判定
			if (c == i_entry_x && r == i_entry_y){
				//開始点と同じピクセルに到達したら、終点の可能性がある。
				coord_num++;
				//末端のチェック
				if (coord_num == max_coord) {
					//輪郭bufが末端に達した
					return false;
				}				
				//末端候補の次のピクセルを調べる
				dir = (dir + 5) % 8;//dirの正規化
				for (i = 0; i < 8; i++){				
					final int x=c + xdir[dir];
					final int y=r + ydir[dir];
					//境界チェック
					if(x>=i_l && x<=i_r && y>=i_t && y<=i_b){
						if ((0xff & buf[x+y*w]) <= i_th) {
							break;
						}
					}
					dir++;//倍長テーブルを参照するので問題なし
				}
				if (i == 8) {
					//8方向全て調べたけどラベルが無いよ？
					throw new NyARException();
				}
				//得たピクセルが、[1]と同じならば、末端である。
				c = c + xdir[dir];
				r = r + ydir[dir];
				if(coord[1].x ==c && coord[1].y ==r){
					//終点に達している。
					o_coord.length=coord_num;
					break;
				}else{
					//終点ではない。
					coord[coord_num].x = c;
					coord[coord_num].y = r;
				}
			}
			coord_num++;
			//末端のチェック
			if (coord_num == max_coord) {
				//輪郭が末端に達した
				return false;
			}
		}
		return true;
	}
}

/**
 * GSPixelDriverを使ったクラス
 */
class NyARRlePixelDriver_YUV420Reader implements NyARLabeling_Rle.IRasterDriver
{
	private INyARGrayscaleRaster _ref_raster;
	public NyARRlePixelDriver_YUV420Reader(INyARGrayscaleRaster i_raster) throws NyARException
	{
		assert i_raster.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP);
		this._ref_raster=i_raster;
	}
	public int xLineToRle(int i_x,int i_y,int i_len,int i_th,NyARLabeling_Rle.RleElement[] i_out) throws NyARException
	{
		byte[] buf=(byte[])this._ref_raster.getBuffer();
		int current = 0;
		int r = -1;
		// 行確定開始
		int st=i_x+this._ref_raster.getWidth()*i_y;
		int x = st;
		final int right_edge = st + i_len - 1;
		while (x < right_edge) {
			// 暗点(0)スキャン
			if ((0xff & buf[x]) > i_th) {
				x++;//明点
				continue;
			}
			// 暗点発見→暗点長を調べる
			r = (x - st);
			i_out[current].l = r;
			r++;// 暗点+1
			x++;
			while (x < right_edge) {
				if ((0xff & buf[x]) > i_th) {
					// 明点(1)→暗点(0)配列終了>登録
					i_out[current].r = r;
					current++;
					x++;// 次点の確認。
					r = -1;// 右端の位置を0に。
					break;
				} else {
					// 暗点(0)長追加
					r++;
					x++;
				}
			}
		}
		// 最後の1点だけ判定方法が少し違うの。
		if ((0xff &buf[x]) > i_th) {
			// 明点→rカウント中なら暗点配列終了>登録
			if (r >= 0) {
				i_out[current].r = r;
				current++;
			}
		} else {
			// 暗点→カウント中でなければl1で追加
			if (r >= 0) {
				i_out[current].r = (r + 1);
			} else {
				// 最後の1点の場合
				i_out[current].l = (i_len - 1);
				i_out[current].r = (i_len);
			}
			current++;
		}
		// 行確定
		return current;
	}
}




/**
 * YUV420のRGBアクセスクラス。GET操作のみ対応。SET操作はできません。
 */
final class AndYUV420GsPixelReader implements INyARGsPixelDriver
{
	protected byte[] _ref_buf;
	private NyARIntSize _ref_size;
	@Override
	public void getPixelSet(int[] i_x, int[] i_y, int i_n, int[] o_buf,
			int i_st_buf) throws NyARException
	{
		int w=this._ref_size.w;
		byte[] buf=_ref_buf;
		for (int i = i_n - 1; i >= 0; i--){
			int y = (0xff & ((int)buf[i_x[i]+w*i_y[i]])) - 16;
			if (y < 0){
				y = 0;
			}
			o_buf[i]=y;
		}
		return;	
		
	}
	@Override
	public int getPixel(int i_x, int i_y) throws NyARException
	{
		int y = (0xff & ((int)this._ref_buf[i_x+this._ref_size.w*i_y])) - 16;
		if (y < 0){
			y = 0;
		}
		return y;
	}
	@Override
	public void switchRaster(INyARRaster i_ref_raster) throws NyARException
	{
		this._ref_buf=(byte[])i_ref_raster.getBuffer();
		this._ref_size=i_ref_raster.getSize();
	}
	@Override
	public boolean isCompatibleRaster(INyARRaster i_raster) {
		return i_raster.isEqualBufferType(NyARBufferType.BYTE1D_YUV420SP);

	}
	@Override
	public void setPixel(int i_x, int i_y, int i_gs) throws NyARException {
		NyARException.notImplement();		
	}
	@Override
	public NyARIntSize getSize()
	{
		return this._ref_size;
	}
	@Override
	public void setPixels(int[] i_x, int[] i_y, int i_num, int[] i_intgs)
			throws NyARException {
		NyARException.notImplement();		
	}
}

