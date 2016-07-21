package jp.androidgroup.nyartoolkit.utils.gl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * 立方体の表示クラスです。
 *
 */
public class AndGLBox  implements AndGLView.IGLViewEventListener
{
	private GL10 _ref_gl;
	
	private FloatBuffer _vertex;
	private FloatBuffer _color;
	private ByteBuffer _index;
	public AndGLBox(AndGLView i_context,float i_size)
	{
		i_context._evl.add(this);
		float s=i_size/2;
		float[] square = {
				-s, -s, -s, // 0(bottom)
				s, -s, -s, // 
				s,  s, -s, // 
				-s,  s, -s, // 3
				-s, -s,  s, // 4(top)
				s, -s,  s, // 
				s,  s,  s, // 
				-s,  s,  s};
		this._vertex=AndGLHelper.makeFloatBuffer(square);
		float[] colors = {
			0.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f
		};
		this._color=AndGLHelper.makeFloatBuffer(colors);
		// 面設定
		byte[] indices = {
				0, 4, 5,	0, 5, 1,
				1, 5, 6, 	1, 6, 2, 
				2, 6, 7, 	2, 7, 3, 
				3, 7, 4, 	3, 4, 0, 
				4, 7, 6, 	4, 6, 5, 
				3, 0, 1, 	3, 1, 2};
		this._index=AndGLHelper.makeByteBuffer(indices);
	}
	/**
	 * BOXを描画します。
	 * この関数は、次のパラメータを変更します。
	 * GL_COLOR_ARRAY,GL_VERTEX_ARRAY,GL_TEXTURE_2D,GL_NORMALIZE,GL_LIGHTING
	 * この関数は、MatrixModeを変更します。
	 * @param i_x
	 * @param i_y
	 * @param i_z
	 */
	public void draw(float i_x,float i_y,float i_z)
	{
		GL10 gl=this._ref_gl;
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_NORMALIZE);  
		gl.glDisable(GL10.GL_LIGHTING); 			
		
		gl.glColorPointer( 4, GL10.GL_FLOAT, 0,this._color);
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0,this._vertex);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glTranslatef(i_x,i_y,i_z);
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE,this._index);
		gl.glPopMatrix();		
//		gl.glEnable(GL10.GL_TEXTURE_2D);
//		gl.glEnable(GL10.GL_NORMALIZE);  
//		gl.glEnable(GL10.GL_LIGHTING); 			
//		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
//		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
	@Override
	public void onGlChanged(GL10 i_gl, int i_width, int i_height)
	{
		if(this._ref_gl!=null){
		}
		this._ref_gl=i_gl;
	}
	@Override
	public void onGlMayBeStop()
	{
		this._ref_gl=null;
	}
}

/* (non-Javadoc)
 * @see jp.nyatla.nyartoolkit.android.renderer.DefaultRenderer#draw(javax.microedition.khronos.opengles.GL10)
 *//*
@Override
public void draw(GL10 gl)
{
	Log.d(TAG, "draw");
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	
	gl.glMatrixMode(GL10.GL_PROJECTION);
//	gl.glLoadIdentity();
	gl.glLoadMatrixf(NyARToolKitWrapper.getInstance().getGlProjectionMatrix(), 0);
	
//	gl.glEnable(GL10.GL_CULL_FACE);
//	gl.glShadeModel(GL10.GL_SMOOTH);
//	gl.glEnable(GL10.GL_DEPTH_TEST);
//	gl.glFrontFace(GL10.GL_CW);
	
	if (NyARToolKitWrapper.getInstance().queryMarkerVisible()) {
		Log.d(TAG, "draw visible");
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLoadMatrixf(NyARToolKitWrapper.getInstance().queryMarkerTransformation(), 0);
		
		Log.d(TAG, "draw model");
		_drawModel(gl);
	}
}
*/