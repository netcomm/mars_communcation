package com.cn.netcomm.communication.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 数据压缩解压缩类
 * 
 * @version 1.5 Nov 14, 2007
 * @author noms研发组
 */
public class DataZipUnZip
{
	/**
	 * 将Zip数据解压到文件
	 * 
	 * @param data
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String UnZipToFile(byte[] data, String file) throws Exception
	{
		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
		int slen;
		byte[] c = new byte[1024];
		FileOutputStream out = new FileOutputStream(file);
		while ((slen = in.read(c, 0, c.length)) != -1)
		{
			out.write(c, 0, slen);
		}
		out.flush();
		out.close();
		return file;
	}

	/**
	 * 将数据解压到内存
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] unZipToMem(byte[] data, int offset, int length)
			throws IOException
	{
		GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data,
				offset, length));
		int slen;
		byte[] c = new byte[1024];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((slen = in.read(c, 0, c.length)) != -1)
		{
			out.write(c, 0, slen);
		}
		out.flush();
		out.close();

		return out.toByteArray();
	}

	public static byte[] zipData(String what)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream dos = new GZIPOutputStream(out);
			dos.write(what.getBytes());
			dos.finish();
			dos.flush();
			return out.toByteArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] zipData(byte[] valuesParm)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream dos = new GZIPOutputStream(out);
			dos.write(valuesParm);
			dos.finish();
			dos.flush();
			return out.toByteArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
