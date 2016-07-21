package jp.androidgroup.nyartoolkit.utils.gl;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

/**
 * 1行の文字列を表示します。
 */
public class AndGLTextLabel extends AndGLBasicSprite
{
	protected final Paint fgpaint= new Paint();
	protected final Paint bgpaint= new Paint();	
	public AndGLTextLabel(AndGLView i_context)
	{
		super(i_context);
		//2^nサイズを求める。
		this.setBitmap(Bitmap.createBitmap(32,32, Bitmap.Config.ARGB_8888),true);
    	this.fgpaint.setColor(Color.WHITE);
    	this.fgpaint.setTypeface(Typeface.DEFAULT);
    	this.fgpaint.setTextSize(16.0f);
    	this.fgpaint.setAntiAlias(true);
    	this.bgpaint.setColor(Color.BLACK);
    	this.bgpaint.setStyle(Style.FILL);
		
	}
	private int _last_str_w=1;
	private int _last_str_h=1;
	private String _last_str="";
	public void draw(double i_s,int i_dx,int i_dy)
	{
		this.draw(String.valueOf(i_s),i_dx,i_dy);
	}
	public void draw(long i_s,int i_dx,int i_dy)
	{
		this.draw(String.valueOf(i_s),i_dx,i_dy);
	}
	public void draw(String i_s,int i_dx,int i_dy)
	{
    	Paint.FontMetrics fontMetrics = this.fgpaint.getFontMetrics();
    	//文字列の内容が異なっていたら、テクスチャ/BMPの再構築
    	int lw=this._last_str_w;
    	int lh=this._last_str_h;
    	if(i_s.compareTo(this._last_str)!=0){
    		//bitmapの再構築を試行
	    	int w=this._last_str_w=(int)Math.ceil(this.fgpaint.measureText(i_s));
	    	int h=this._last_str_h=(int)Math.ceil(Math.abs(fontMetrics.ascent) +Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.leading));
	    	//ビットマップのサイズをチェックしてリサイズ
	    	int s=AndGLHelper.getPow2Size(w,h);
	    	if(s> this._image.getWidth()|| s>this._image.getHeight()){
	    		try {
					this.resize(s,s);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					this._context._finish(e);
				}
	    		lw=lh=s;
	    	}
	    	this._last_str=i_s;
    	}
    	//埋め戻し
		this._canvas.drawRect(0, 0,lw,lh,this.bgpaint);
		this._canvas.drawText(i_s,0, Math.abs(fontMetrics.ascent),this.fgpaint);
    	this.sync();
		super.draw(i_dx, i_dy,this._last_str_w,this._last_str_h, 0,0);
	}
}
