package jp.androidgroup.nyartoolkit.utils.gl;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.androidgroup.nyartoolkit.sketch.AndSketch;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class AndGLView extends GLSurfaceView implements AndSketch.IAndSketchEventListerner
{
	public interface IGLFunctionEvent
	{
		public void setupGL(GL10 i_gl);
		public void drawGL(GL10 i_gl);
	}
	public interface IGLViewEventListener{
		public void onGlChanged(GL10 i_gl,int i_width,int i_height);
		public void onGlMayBeStop();
		
	}		
	public ArrayList<IGLViewEventListener> _evl=new ArrayList<IGLViewEventListener>();
	
	public AndGLView(AndSketch i_context)
	{
		super(i_context);
		i_context._evlistener.add(this);		
		this.setEGLConfigChooser( 8, 8, 8, 8, 16, 0);
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		this.setRenderer(new GLRenderer(i_context,this));
		
	}

	@Override
	public void onAcResume()
	{
		super.onResume();
	}
	@Override
	public void onAcPause()
	{
		super.onPause();
	}

	@Override
	public void onAcDestroy() throws Exception
	{
	}

	@Override
	public void onAcStop() throws Exception
	{
		for(AndGLView.IGLViewEventListener i : this._evl) {
			i.onGlMayBeStop();
		}		
	}


	
}



class GLRenderer implements GLSurfaceView.Renderer
{
	private AndGLView _view;
	private AndSketch _context;
	private AndGLView.IGLFunctionEvent _function_if;
	public final static int AST_NULL=0;
	public final static int AST_SETUP=1;
	public final static int AST_RUN  =2;
	private int _ast=AST_NULL;	
	/**
	 * i_contextが{@link AndGLView.IGLFunctionEvent}を持つ場合
	 * @param i_context
	 * @param i_view
	 */
	public GLRenderer(AndSketch i_context,AndGLView i_view)
	{
		this._view=i_view;
		this._context=i_context;
		if(!(i_context instanceof AndGLView.IGLFunctionEvent)){
			i_context._finish(new Exception());
		}
		this._function_if=(AndGLView.IGLFunctionEvent)i_context;
	}
	/**
	 * Logging Tag
	 */	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		// Transparent background
		gl.glClearColor(0.5f, 0.0f, 0.0f, 0.f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		this._ast=AST_SETUP;
		this._function_if.setupGL(gl);
		this._ast=AST_RUN;
		
	}
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		//Surfaceの再構築で、Sketchの所有しているGLサーフェイスの
		for(AndGLView.IGLViewEventListener i : this._view._evl) {
			i.onGlChanged(gl,width,height);
		}
		gl.glViewport(0, 0, width, height);
	}
	@Override
	public void onDrawFrame(GL10 gl)
	{
		if(this._ast==AST_RUN){
			this._function_if.drawGL(gl);
		}
	}
}