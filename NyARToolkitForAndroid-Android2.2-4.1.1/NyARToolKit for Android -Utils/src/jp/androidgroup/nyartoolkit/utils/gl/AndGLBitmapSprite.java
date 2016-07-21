package jp.androidgroup.nyartoolkit.utils.gl;

import android.graphics.Bitmap;

/**
 * Bitmapをスプライトとして表示するクラスです。
 */
public class AndGLBitmapSprite extends AndGLBasicSprite
{
	private int _internal_w;
	private int _internal_h;
	public AndGLBitmapSprite(AndGLView i_view,int i_w,int i_h)
	{
		super(i_view);
		//2^nサイズを求める。
		int s=AndGLHelper.getPow2Size(i_w, i_h);
		this.setBitmap(Bitmap.createBitmap(s,s, Bitmap.Config.ARGB_8888),true);
		this._internal_w=i_w;
		this._internal_h=i_h;
	}
	private boolean _locked=false;
	public Bitmap lockBitmap()
	{
		assert this._locked==false;
		this._locked=true;
		return this._image;
	}
	public void unlockBitmap()
	{
		assert this._locked==true;
		this._locked=false;
		this.sync();
	}

	public void draw(int i_dx,int i_dy,int i_w,int i_h,int i_sx,int i_sy)
	{
		super.draw(i_dx, i_dy, i_w, i_h, i_sx, i_sy);
	}
	public void draw(int i_dx,int i_dy)
	{
		super.draw(i_dx, i_dy,this._internal_w, this._internal_h,0,0);
	}
}

