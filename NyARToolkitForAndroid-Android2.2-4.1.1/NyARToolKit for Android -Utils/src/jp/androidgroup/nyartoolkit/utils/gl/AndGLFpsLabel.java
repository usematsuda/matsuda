package jp.androidgroup.nyartoolkit.utils.gl;

/**
 * コールスタックのデバック情報を表示します。
 */
public class AndGLFpsLabel extends AndGLTextLabel
{
	/** 任意のメッセージを表示します。変数を上書きして、意図的に例外を発生させてください。*/
	public static String msg="Exception! tell to @nyatla!";
	public AndGLFpsLabel(AndGLView i_context,String i_inisial_string)
	{
		super(i_context);
		this._text=i_inisial_string;
	}
	private long _last=System.currentTimeMillis();
	private long _ct=0;
	private int _c=0;
	private String _text;
	public String prefix="";
	public void draw(int i_x,int i_y)
	{
        long n=System.currentTimeMillis();
        this._ct+= (n-this._last);
        this._c++;
        this._last=n;
        if(this._c==15){
        	this._text=this.prefix+(1000/(this._ct/this._c))+"fps";
        	this._ct=this._c=0;
        }
        this.draw(this._text,0,0);
	}
	
}
