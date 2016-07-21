package jp.androidgroup.nyartoolkit.utils.gl;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL用のヘルパ関数をまとめます。
 */
public class AndGLHelper
{
	public static final int TEXTURE_CHANNEL=GL10.GL_TEXTURE0;
	
    public static FloatBuffer makeFloatBuffer(int i_len)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(i_len*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.position(0);
        return fb;
    }	
    public static FloatBuffer makeFloatBuffer(float[] i_arr)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(i_arr.length*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(i_arr);
        fb.position(0);
        return fb;
    }
    public static ByteBuffer makeByteBuffer(byte[] i_arr)
    {
    	ByteBuffer bb = ByteBuffer.allocateDirect(i_arr.length);
    	bb.put(i_arr);
    	bb.position(0);
    	return bb;
    }    
    public static int getPow2Size(int i_w,int i_h)
    {
    	int c=i_w>i_h?i_w:i_h;
    	int s=0x1;
    	while(s<c){
    		s=s<<1;
    	}
    	return s;
    }
}

