package info.kuonteje.voxeltest.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ModelUtil
{
	public static void addVertices(FloatBuffer buf, float[] vertices, int x, int y, int z)
	{
		for(int i = 0; i < vertices.length;)
		{
			buf.put(vertices[i++] + x);
			buf.put(vertices[i++] + y);
			buf.put(vertices[i++] + z);
		}
	}
	
	public static void addTint(ByteBuffer buf, int tint)
	{
		buf.put((byte)((tint >> 16) & 0xFF));
		buf.put((byte)((tint >> 8) & 0xFF));
		buf.put((byte)(tint & 0xFF));
		buf.put((byte)0xFF);
	}
}
