package jp.androidgroup.nyartoolkit.utils.gl;

/**
 * コールスタックのデバック情報を表示します。
 */
public class AndGLDebugDump extends AndGLTextLabel
{
	/** 任意のメッセージを表示します。変数を上書きして、意図的に例外を発生させてください。*/
	public static String msg="Exception! tell to @nyatla!";
	public AndGLDebugDump(AndGLView i_context)
	{
		super(i_context);
	}
	public void draw(Exception e)
	{
		StackTraceElement[] se=e.getStackTrace();
		this.draw(msg,0,0);
		for(int i=0;i<se.length;i++){
			this.draw(se[i].getFileName()+":"+se[i].getLineNumber(),0,(i+1)*16);
    	}
	}
	
}
