package jp.androidgroup.nyartoolkit.utils.gl;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import jp.androidgroup.nyartoolkit.sketch.AndSketch;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLU;
import android.opengl.GLUtils;


/**
 * スプライトテクスチャのベースクラス。
 * http://pr0jectze10.blogspot.jp/2011/02/androidopengl.html
 * を参考にしました。
 */
public class AndGLBasicSprite implements AndGLView.IGLViewEventListener
{

    private int[] _gl_state_holder=new int[10];
	
	public int index; // イメージバインドID
	protected Bitmap _image; // ビットマップイメージ
	protected Canvas _canvas;
	private GL10 _ref_gl;
	private boolean _is_attache;
	protected AndSketch _context;

	/**
	 * コンストラクタ
	 */
	protected AndGLBasicSprite(AndGLView i_view)
	{
		i_view._evl.add(this);
		//bitmapの生成
		this._image =null;
		this._is_attache=false;

	}
	/**
	 * ビットマップをセットします。
	 * @param i_bitmap
	 * @param i_is_attache
	 */
	protected void setBitmap(Bitmap i_bitmap,boolean i_is_attache)
	{
		this._image=i_bitmap;
		this._is_attache=i_is_attache;
		this._canvas=new Canvas(this._image);
	}
	/**
	 * ビットマップをリサイズします。
	 * この関数を実行するには、ビットマップを所有している必要があります。
	 * @param i_w
	 * @param i_h
	 * @throws Exception
	 */
	protected void resize(int i_w,int i_h) throws Exception
	{
		if(!this._is_attache){
			throw new Exception("can not resize no attached bitmap");
		}
		this._image.recycle();
		this._image=Bitmap.createBitmap(i_w, i_h, Bitmap.Config.ARGB_8888);
		this._canvas=new Canvas(this._image);
	}

	private int _screen_height;
	private int _screen_width;

	protected void sync()
	{
		GL10 gl=this._ref_gl;
		//GLが未初期化なら何もしない
		if(gl==null){
			return;
		}
		// ビットマップイメージの設定
		gl.glActiveTexture(AndGLHelper.TEXTURE_CHANNEL);
		gl.glBindTexture(GL10.GL_TEXTURE_2D,this.index);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0,this._image, 0);
	}
	private FloatBuffer _draw_tmp = AndGLHelper.makeFloatBuffer(3*4); //Vertex
	private FloatBuffer _uv_tmp = AndGLHelper.makeFloatBuffer(2*4);// UVバッファ


	/**
	 * This function changes the matrix mode to MODEL_VIEW , and changes some parameter.
	 * @param i_x
	 * @param i_y
	 * @param i_w
	 * @param i_h
	 * @param i_sx
	 * @param i_sy
	 */
	protected void draw(int i_x, int i_y,int i_w,int i_h,int i_sx,int i_sy)
	{
		assert this._image!=null;
//		int[] st=this._gl_state_holder;
		GL10 gl=this._ref_gl;
//		{	//<SaveGLstatus>
//			gl.glGetIntegerv(GL10.GL_NORMALIZE,st,1);
//			gl.glGetIntegerv(GL10.GL_LIGHTING,st,2);
//			gl.glGetIntegerv(GL10.GL_VERTEX_ARRAY,st,3);
//			gl.glGetIntegerv(GL10.GL_TEXTURE_COORD_ARRAY,st,4);
//			gl.glGetIntegerv(GL10.GL_COLOR_ARRAY,st,5);
//			//gl.glGetIntegerv(GL10.GL_MATRIX_MODE,st,5); //なんでないんだよ！
//		}
		//set GL state
		gl.glCullFace(GL10.GL_BACK);
		gl.glActiveTexture(AndGLHelper.TEXTURE_CHANNEL);
    	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_REPEAT);
    	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_REPEAT);
    	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
    	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
		// ポリゴン色とテクスチャ色の合成方法
    	gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,GL10.GL_MODULATE);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_NORMALIZE);  
		gl.glDisable(GL10.GL_LIGHTING); 			
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glBindTexture(GL10.GL_TEXTURE_2D,this.index);   	

		
		//平行投影へ。
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl,0,this._screen_width,0,this._screen_height);//平行投影		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		// 描画
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0,getVertices(i_x,i_y,i_w,i_h,this._draw_tmp));

		// UV座標
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, getUv(0,0,i_w,i_h,this._uv_tmp));
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		

//		{	//Restore
//			if(st[1]!=0){gl.glEnable(GL10.GL_NORMALIZE);}
//			if(st[2]!=0){gl.glEnable(GL10.GL_LIGHTING);}			
//			if(st[3]==0){gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);}
//			if(st[4]==0){gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);}
//			if(st[5]!=0){gl.glEnableClientState(GL10.GL_COLOR_ARRAY);}
//		}

	}
	private FloatBuffer getUv(int x, int y, int width,int height,FloatBuffer i_buf)
	{
		int bw=this._image.getWidth();
		int bh=this._image.getHeight();
		float l=((float)x)/((float)bw);
		float t=((float)y)/((float)bh);
		float r=((float)(width+x))/((float)bw);
		float b=((float)(height+y))/((float)bh);
		float[] vertices = {
				l,b,
				r,b,
				l,t,
				r,t};
		i_buf.put(vertices);
		i_buf.position(0);
		return i_buf;		
	}
	private static FloatBuffer getVertices(int x, int y, int width,int height,FloatBuffer i_buf)
	{
		// 頂点バッファ
		float[] vertices = {
			x, y, 0,
			x + width, y, 0,
			x, y + height, 0,
			x + width, y + height, 0};
		i_buf.position(0);
		i_buf.put(vertices);
		i_buf.position(0);
		return i_buf;
	}

	@Override
	public void onGlChanged(GL10 i_gl, int i_width, int i_height)
	{
		if(this._ref_gl!=null){
			int[] textures = {this.index};
			this._ref_gl.glDeleteTextures(1, textures, 0);
		}
		this._ref_gl=i_gl;
		this._screen_height=i_height;
		this._screen_width=i_width;
		
		// テクスチャIDの設定
		int[] ids={-1};
		i_gl.glGenTextures(1, ids, 0);
		this.index=ids[0];

    	//bitmap画あるときはここでsync
    	if(this._image!=null){
    		this.sync();
    	}
	}
	@Override
	public void onGlMayBeStop() {
		int[] textures = {this.index};
		this._ref_gl.glDeleteTextures(1, textures, 0);
		this._image = null;
		this._image.recycle();
		this._ref_gl=null;
		
	}
}

