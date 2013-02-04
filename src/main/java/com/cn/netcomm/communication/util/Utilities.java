package com.cn.netcomm.communication.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import com.cn.netcomm.communication.exception.MsgHeadException;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;


/**
 * @author netcomm
 * @version $Revision: 1.2 $ $Date: 2007/05/14 05:35:18 $
 */
public class Utilities extends Object
{
	private static Logger logger =
			Logger.getLogger(Utilities.class.getName());
	private static Utilities service = new Utilities();
    private static Deflater compressor = null;
    private static Inflater decompressor = null;
    private static ByteArrayOutputStream bos = null;
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private static SimpleDateFormat dayAndHourFormatter = new SimpleDateFormat("yyyyMMdd HH");
    private static int Magic_Pois_1 = 4;
    private static int Magic_Pois_2 = 5;
    private static int IsZip_Pois = 6;
    
	private Utilities()
	{
        
	}

	public static Utilities getInstance()
	{
		return service;
	}
	
	public static void printUsedMemory()
    {
        long tmpLong = (Runtime.getRuntime().totalMemory() - Runtime
            .getRuntime()
            .freeMemory())
            / (1024 * 1024);
        System.out.println("已用内存 " + Long.toString(tmpLong) + " M");
    }
	
	public static String getDayAndHourTimeStr()
	{
		Date currentDateTime = new Date();
		String retCurrentDateTime = dayAndHourFormatter.format(currentDateTime);
		return retCurrentDateTime;
	}
	
    private static void initCompress()
    {
        compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_SPEED);
        bos = new ByteArrayOutputStream(10240);
    }
    
    public static String getFullDateTime()
    {
        String strCurrentDateTime="";
        Date currentDateTime = new Date();
        strCurrentDateTime= formatter.format(currentDateTime);
        return  strCurrentDateTime; 
    }
    
    private static void initDeCompress()
    {
        decompressor = new Inflater();
    }
    
    public static String checkXMLValidAndTransferOk(String origStrParm)
	{
		StringBuffer retStrBuf = new StringBuffer();
		int tmpIndexPosi = 0;
		int tmpBeginPosi = 0;
		while (tmpIndexPosi != -1)
		{
			tmpIndexPosi = origStrParm.indexOf("\"", tmpBeginPosi);
			if (tmpIndexPosi != -1)
			{
				retStrBuf.append(origStrParm.substring(tmpBeginPosi, tmpIndexPosi));
				tmpBeginPosi = tmpIndexPosi + 1;
				retStrBuf.append("'");
			}
			else
			{
				retStrBuf.append(origStrParm.substring(tmpBeginPosi));
			}
		}
		
		return retStrBuf.toString();
	}
    
    /*
     * Wait for a shutdown invocation elsewhere
     * 
     * @throws Exception
     */
    public static void waitForShutdown() throws Exception {
        final boolean[] shutdown = new boolean[] {
            false
        };
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                synchronized (shutdown)
                {
                    shutdown[0] = true;
                    shutdown.notify();
                }
            }
        });

        // Wait for any shutdown event
        synchronized (shutdown) {
            while (!shutdown[0]) {
                try {
                    shutdown.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        // 结束时,停止所有相应的动作.
        System.out.println("成功停止");
    }
    
    public static String getFormatFullDateTime()
    {
        return CurrentDateFormat.getInstance().getFullTime();
    }
    
    /**
     * getTodayDate操作 取得 yyyyMMdd 8位格式的日期
     * 
     * @param
     * @return
     */
    public static String getTodayDate()
    {
        return CurrentDateFormat.getInstance().getCurDay();
    }
    
    public static byte[] bytesAppendByteArray(byte[] appendBytes, byte[] data)
	{
		byte[] newB = new byte[appendBytes.length + data.length];
		System.arraycopy(appendBytes, 0, newB, 0, appendBytes.length);
		System.arraycopy(data, 0, newB, appendBytes.length, data.length);
		data = null;
		return newB;
	}
    
    public static byte[] bytesAppendByteArray(byte[] appendBytes, byte data)
	{
		byte[] newB = new byte[appendBytes.length + 1];
		System.arraycopy(appendBytes, 0, newB, 0, appendBytes.length);
		newB[newB.length - 1] = data;
		return newB;
	}
    
    public static boolean compareTwoBytes(
    				byte[] srcBytesParm, byte[] destBytesParm)
    {
    	boolean retBool = false;
    	if (srcBytesParm.length >= destBytesParm.length)
    	{
    		if (srcBytesParm[0] == destBytesParm[0]
    		                && srcBytesParm[1] == destBytesParm[1])
    		{
    			retBool = true;
    		}
    	}
    	
    	return retBool;
    }
    
    public synchronized static byte[] compressBytes(byte[] input)
    {
        if (compressor == null)
        {
            initCompress();
        }
        compressor.reset();
        compressor.setInput(input);
        compressor.finish();

        if (input.length > bos.size())
        {
            bos = new ByteArrayOutputStream(input.length);
        }
        bos.reset();
        
        byte[] buf = new byte[10240];
        while (!compressor.finished())
        {
            int count = compressor.deflate(buf);           
            bos.write(buf, 0, count);
        }
        
        try
        {
            bos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        byte[] compressedData = bos.toByteArray();
        return compressedData;
    }
    
    public synchronized static byte[] deCompressBytes(byte[] compressedData,
                                         int off, int len)
    {
        if (decompressor == null)
        {
            initDeCompress();
        }
        decompressor.reset();
        decompressor.setInput(compressedData, off, len);

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(
                compressedData.length);

        // Decompress the data
        byte[] buf = new byte[8196];
        while (!decompressor.finished())
        {
            try
            {
                int count = decompressor.inflate(buf);
                if (count == 0)
                {
                	break;
                }
                bos.write(buf, 0, count);
            }
            catch(Exception ex)
            {
            	ex.printStackTrace();
            	break;
            }
        }
        
        try
        {
            bos.close();
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }

        // Get the decompressed data
        byte[] decompressedData = bos.toByteArray();
        return decompressedData;
    }
    
    public static byte[] getHourAndMinuteAndSecBytes(char[] hmsCharsParm)
    {
        byte[] retBytes = new byte[3];
        
        for (int i = 0; i < hmsCharsParm.length; i++)
        {
            retBytes[i] = (byte)hmsCharsParm[i];
        }
        
        return retBytes;
    }
    
    /**
	 * strSplit操作
	 * 
	 * @param strSource
	 *            要被分割的字符串; delimiter 分割符
	 * @return String[]型的分割完的字符串组
	 */
	public static String[] strSplit(String strSource, String delimiter)
	{
		int intPos = 0;
		String str = null;
		Vector vector = new Vector(5);
		String[] strRet = null;

		// 校验输入参数
		if (strSource == null)
			return (new String[0]);
		if (delimiter == null)
			return null;
		if (strSource.equals(""))
			return (new String[0]);
		//
		intPos = strSource.indexOf(delimiter);
		String strTemp = "";
		while (intPos != -1)
		{
			// 判断是否为转义符
			if (intPos != 0)
			{
				if (strSource.substring(intPos - 1, intPos).equals("\\"))
				{
					strTemp = strTemp + strSource.substring(0, intPos - 1)
							+ delimiter;
					strSource = strSource.substring(intPos + 1);
					intPos = strSource.indexOf(delimiter);
					continue;
				}
			}
			// 非转义符
			str = strTemp.equals("") ? strSource.substring(0, intPos)
					: strTemp + strSource.substring(0, intPos);
			strSource = strSource.substring(intPos + delimiter.length());
			vector.addElement(str);
			strTemp = "";
			intPos = strSource.indexOf(delimiter);
		}
		vector.addElement(strSource);

		strRet = new String[vector.size()];
        System.arraycopy(vector.toArray(), 0, strRet, 0, vector.size());
		return strRet;
	}

	public static Vector bytesSplit(byte[] bytesSource, byte[] delimiter)
	{
		Vector tmpVector = new Vector(1,1);
		int lastEqualPosi = 0;
		int tmpLength = 0;
		byte[] tmpBytes = new byte[0];
        boolean isEqual = true;
		for (int i = 0; i < bytesSource.length; i++)
		{
			if (bytesSource[i] == delimiter[0])
			{
                isEqual = true;
                if (i < bytesSource.length - delimiter.length +1)
                {
                    for (int j = 1; j < delimiter.length; j++)
                    {
                        if (bytesSource[i+j] != delimiter[j])
                        {
                            isEqual = false;
                            break;
                        }
                    }
                }
                
                if (isEqual == true)
                {
    				tmpLength = i - lastEqualPosi;
    				tmpBytes = new byte[tmpLength];
    				System.arraycopy(bytesSource, lastEqualPosi,
    						tmpBytes, 0, tmpLength);
    				tmpVector.add(tmpBytes);
    				lastEqualPosi = i+delimiter.length;
                }
			}
		}

        if (lastEqualPosi != bytesSource.length)
        {
            tmpBytes = new byte[bytesSource.length - lastEqualPosi];
            System.arraycopy(bytesSource, lastEqualPosi,
                    tmpBytes, 0, tmpBytes.length);
            tmpVector.add(tmpBytes);
        }
        
		return tmpVector;
	}
	
	/**
	 * strSplitExtend操作
	 * 
	 * @param strSource
	 *            要被分割的字符串; delimiter 分割符
	 * @return String型的进行过分割的字符串
	 */
	public static String strSplitExtend(String strSource, char delimiter)
	{
		char[] tmpChars = strSource.toCharArray();

		// 校验输入参数
		if (strSource == null)
			return (null);
		if ((strSource.trim()).equals(""))
			return (null);

		String strTemp = "";
		for (int i = 1; i < tmpChars.length; i++)
		{
			if (!((tmpChars[i] == delimiter) && (tmpChars[i - 1] == delimiter)))
			{
				strTemp = strTemp + tmpChars[i - 1];
			}
			if (i == (tmpChars.length - 1))
			{
				strTemp = strTemp + tmpChars[i];
			}
		}

		return strTemp;
	}

	/**
	 * roundDouble操作 保留precision位小数点后几位,如var =2.3426 precision=2,返回就是2.34
	 * 
	 * @param val:
	 *            要转变的double变量; precision: 保留的精度
	 * @return 返回转变后的double型
	 */
	public static double roundDouble(double val, int precision)
	{
		double factor = Math.pow(10, precision);
		return Math.floor(val * factor + 0.5) / factor;
	}
	
    public static String[] getQuotationDetailItemStr(int strsCntParm, String strSourceParm)
    {
        String[] returnStrs = new String[strsCntParm];
        int haveQuotaNum = 0;
        int beginPos = 0;
        int returnStrsIndx = 0;
        char[] tmpStrChars = strSourceParm.toCharArray();        
        for (int i = 0; i < tmpStrChars.length; i++)
        {
            if (tmpStrChars[i] == 34)
            {
                haveQuotaNum++;
                if (haveQuotaNum == 2)
                {
                    haveQuotaNum = 0;
                    returnStrs[returnStrsIndx] =
                                    strSourceParm.substring(beginPos+1, i);
                    returnStrsIndx++;
                    if (returnStrsIndx == strsCntParm)
                    {
                        break;
                    }
                }
                else
                {
                    beginPos = i;
                }
            }
        }
        
        return returnStrs;
    }
    
    /**
	 * Method getMapKeys
	 * 
	 * @param paraMap
	 *            要解析的HashMap引用
	 * @return keysStr 返回字符串数组包含HashMap所有的Key
	 */
	public static String[] getStrMapKeys(HashMap paraMap)
	{
		HashMap rtnMap = paraMap;
		Set keysSet = rtnMap.keySet();
		Object[] keysObj = keysSet.toArray();
		int lengthSet = keysObj.length;
		String[] keysStr = new String[lengthSet];
		System.arraycopy(keysObj, 0, keysStr, 0, keysObj.length);
		
		// 12月22号 netcomm修改
		keysObj = null;
		return keysStr;
	}

	public static String getEnsFormatStr(Object objValue, String pattern)
	{
		String returnStr = "";
		String thePattern = "#,###,###";
		if (!pattern.equals(""))
		{
			thePattern = pattern;
		}

		NumberFormat formatter = new DecimalFormat(thePattern);
		returnStr = formatter.format(objValue);
		formatter = null;
		return returnStr;
	}

	public static String getTheHexStr(String[] theStrs)
	{
		String returnStr = "";
		for (int i = 0; i < theStrs.length; i++)
		{
			if (i == 0)
			{
				returnStr = theStrs[0];
			}
			else
			{
				returnStr = returnStr + ":" + theStrs[i];
			}
		}

		return returnStr;
	}

	/**
     * 取得 yyyyMMdd 8位格式的日期 它的效率一般，适合于不追求效率的情况下取当天日期的场合
     * 
     * @return
     */
    public static String getThisYear_SlowVersion()
    {
        Date today;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");

        today = new Date();
        String returnStr = formatter.format(today);
        today = null;
        formatter = null;
        return returnStr;
    }
	
	public static Vector transHMap2Vect(HashMap hMapParm)
	{
		Vector returnVector = null;
        returnVector = new Vector(hMapParm.values());
		return returnVector;
	}
	
    /**
     * 生成整数值对应的10位数char
     * @param valueParm
     * @return
     */
    public static char[] transIntToChars(int valueParm)
    {
        char[] tmpChars = new char[10];
        char[] tmpIntValueChars = Integer.toString(valueParm).toCharArray();
        
        if (tmpIntValueChars.length < 10)
        {
            for (int i = 0; i < 10 - tmpIntValueChars.length; i++)
            {
                tmpChars[i] = 48;
            }
            System.arraycopy(tmpIntValueChars, 0, tmpChars,
                    10 - tmpIntValueChars.length, tmpIntValueChars.length);
        }
        
        return tmpChars;
    }
    
    /**
     * 生成整数值对应的指定位数char内容
     * @param valueParm
     * @param retCharsWidthParm
     * @return
     */
    public static char[] transIntToChars(int valueParm, int retCharsWidthParm)
    {
        char[] retChars = null;
        char[] tmpIntValueChars = Integer.toString(valueParm).toCharArray();
        
        if (tmpIntValueChars.length <= retCharsWidthParm)
        {
            retChars = new char[retCharsWidthParm];
            for (int i = 0; i < retCharsWidthParm - tmpIntValueChars.length; i++)
            {
                retChars[i] = 48;
            }
            System.arraycopy(tmpIntValueChars, 0, retChars,
                    retCharsWidthParm - tmpIntValueChars.length,
                    tmpIntValueChars.length);
        }
        
        return retChars;
    }
    
    /**
     * 转化int值为4位的char组
     * @param valueParm
     * @param retCharsWidthParm
     * @return
     */
    public static char[] transIntToCharsTo4Chars(int v) 
    {
        char[] retChars = new char[4];
        retChars[0] = (char)((v >>> 24) & 0xFF);
        retChars[1] = (char)((v >>> 16) & 0xFF);
        retChars[2] = (char)((v >>>  8) & 0xFF);
        retChars[3] = (char)((v >>>  0) & 0xFF);
        return retChars;
    }
    
    public static char[] transDoubleToChars(double dvParm)
    {
        long v = Double.doubleToLongBits(dvParm);
        char[] retChars = new char[4];
        retChars[0] = (char)((v >>> 48) & 0xFFFF);        
        retChars[1] = (char)((v >>> 32) & 0xFFFF);
        retChars[2] = (char)((v >>> 16) & 0xFFFF);        
        retChars[3] = (char)((v >>>  0) & 0xFFFF);
        return retChars;
    }
    
    public static double readCharsToDouble(char[] charValues)
    {
        long tmpLongValue =
                ((long)(charValues[0]) << 48) + ((long)(charValues[1]) << 32)
                    + (int)(charValues[2] << 16) + (int)(charValues[3] << 0);
        return Double.longBitsToDouble(tmpLongValue);
    }
    
    public static char[] transLongToChars(long v)
    {
        char[] retChars = new char[4];
        retChars[0] = (char)((v >>> 48) & 0xFFFF);        
        retChars[1] = (char)((v >>> 32) & 0xFFFF);
        retChars[2] = (char)((v >>> 16) & 0xFFFF);        
        retChars[3] = (char)((v >>>  0) & 0xFFFF);
        return retChars;
    }
    
    public static long readCharsToLong(char[] charValues)
    {
        long retLongValue =
                ((long)(charValues[0]) << 48) + ((long)(charValues[1]) << 32)
                    + (int)(charValues[2] << 16) + (int)(charValues[3] << 0);
        return retLongValue;
    }
    
    public static char[] transIntTo2Chars(int v) 
    {
        char[] retChars = new char[2];
        retChars[0] = (char)((v >>> 16) & 0xFFFF);        
        retChars[1] = (char)((v >>>  0) & 0xFFFF);
        return retChars;
    }
        
    public static int readCharsToInt(char[] charValues)
    {
        return ((charValues[0] << 16) + (charValues[1] << 0));
    }
    
    /**
     * 取带长度结构的字符串结构中的长度信息
     * @param valueParm
     * @param retCharsWidthParm 字符串中长度信息的位数
     * @param beginPosiParm 字符串中长度信息所在的开始位置
     * @return
     */
    public static int transCharsToInt(String valueParm,
                            int retCharsWidthParm, int beginPosiParm)
    {
        return Integer.parseInt(valueParm.substring(beginPosiParm,
                                             beginPosiParm+retCharsWidthParm));
    }
            
    /**
     * 取得带长度的字符串结构中的长度信息
     * @param valueParm
     * @param beginPosiParm 字符串中长度信息所在的开始位置
     * @return
     */
    public static int transStrToInt(String valueParm, int beginPosiParm)
    {
        return Integer.parseInt(valueParm.substring(beginPosiParm,
                                                    beginPosiParm+10));
    }

    public static Vector transFormatedStrToStrArray(String strParm)
    {
        Vector retVect = new Vector(5);
        int tmpStrLength = 0;
        int tmpStrBeginPosi = 0;
        String tmpStr = null;
        
        if (strParm.length() > 0)
        {
            boolean tmpIsStrEnd = false;
            try
            {
                while ( !tmpIsStrEnd)
                {
                    tmpStrLength = transStrToInt(strParm, tmpStrBeginPosi);
                    tmpStr = strParm.substring(tmpStrBeginPosi + 10,
                                        tmpStrBeginPosi + 10 + tmpStrLength);
                    retVect.add(tmpStr);
                    tmpStrBeginPosi = tmpStrBeginPosi + 10 + tmpStrLength;
                    if (tmpStrBeginPosi >= strParm.length())
                    {
                        tmpIsStrEnd = true;
                    }
                }
            }
            catch(Exception e)
            {
                retVect.clear();
                e.printStackTrace();
            }
        }
        return retVect;
    }
    
	public static int transHexToInt(String hexStr)
	{
		int totalInt = 0;
		try
		{
			char[] tmpChars = hexStr.toLowerCase().toCharArray();
			if (tmpChars[0] == 97)
			{
				totalInt = totalInt + 10;
			}
			if (tmpChars[0] == 98)
			{
				totalInt = totalInt + 11;
			}
			if (tmpChars[0] == 99)
			{
				totalInt = totalInt + 12;
			}
			if (tmpChars[0] == 100)
			{
				totalInt = totalInt + 13;
			}
			if (tmpChars[0] == 101)
			{
				totalInt = totalInt + 14;
			}
			if (tmpChars[0] == 102)
			{
				totalInt = totalInt + 15;
			}
			if (tmpChars[0] < 97)
			{
				totalInt = totalInt + ((int) tmpChars[0] - 48);
			}

			totalInt = totalInt * 16;
			if (tmpChars[1] == 97)
			{
				totalInt = totalInt + 10;
			}
			if (tmpChars[1] == 98)
			{
				totalInt = totalInt + 11;
			}
			if (tmpChars[1] == 99)
			{
				totalInt = totalInt + 12;
			}
			if (tmpChars[1] == 100)
			{
				totalInt = totalInt + 13;
			}
			if (tmpChars[1] == 101)
			{
				totalInt = totalInt + 14;
			}
			if (tmpChars[1] == 102)
			{
				totalInt = totalInt + 15;
			}
			if (tmpChars[1] < 97)
			{
				totalInt = totalInt + ((int) tmpChars[1] - 48);
			}

		}
		catch (Exception e)
		{
			;
		}

		return totalInt;
	}

	/**
	 * toHex操作
	 * 
	 * @param
	 * @return
	 */
	public static String toHex(char[] value)
	{
		int length;
		StringBuffer buffer = new StringBuffer("");

		length = value.length;
		if (length > 0)
		{
			for (int i = 0; i < length - 1; i++)
			{
				buffer.append(toHex(value[i])).append(":");
			}
			buffer.append(toHex(value[length - 1]));
		}

		return buffer.toString();
	}

	public static String toHex(int val)
	{
		int val1, val2;

		val1 = (val >> 4) & 0x0F;
		val2 = (val & 0x0F);

		return ("" + HEX_DIGIT[val1] + HEX_DIGIT[val2]);
	}

	public static byte[] getBytesFromLong(long longValueParm)
	{
		byte[] returnInt = new byte[8];
		returnInt[0] = (byte)((longValueParm >> 56) & 0xFF);
		returnInt[1] = (byte)((longValueParm >>> 48) & 0xFF);
		returnInt[2] = (byte)((longValueParm >>> 40) & 0xFF);
		returnInt[3] = (byte)((longValueParm >>> 32) & 0xFF);
		returnInt[4] = (byte)((longValueParm >>> 24) & 0xFF);
		returnInt[5] = (byte)((longValueParm >>> 16) & 0xFF);
		returnInt[6] = (byte)((longValueParm >>>  8) & 0xFF);
		returnInt[7] = (byte)((longValueParm >>>  0) & 0xFF);
		return returnInt;
	}
	
	public static byte[] getBytesFromInt(int intValueParm)
	{
		byte[] returnInt = new byte[4];
		returnInt[0] = (byte)((intValueParm >>> 24) & 0xFF);
		returnInt[1] = (byte)((intValueParm >>> 16) & 0xFF);
		returnInt[2] = (byte)((intValueParm >>>  8) & 0xFF);
		returnInt[3] = (byte)((intValueParm >>>  0) & 0xFF);
		return returnInt;
	}
	
	public static int getIntFromBytes(byte[] valueBytesParm)
	{
		int returnInt = -1;
		try
		{
			int ch1 = (valueBytesParm[0] & 0xFF);
			int ch2 = (valueBytesParm[1] & 0xFF);
			int ch3 = (valueBytesParm[2] & 0xFF);
			int ch4 = (valueBytesParm[3] & 0xFF);
			returnInt = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return returnInt;
	}
    
    public static int getIntFromBytes(byte[] valueBytesParm, int offsetParm)
    {
        int returnInt = -1;
        try
        {
            int ch1 = (valueBytesParm[offsetParm+0] & 0xFF);
            int ch2 = (valueBytesParm[offsetParm+1] & 0xFF);
            int ch3 = (valueBytesParm[offsetParm+2] & 0xFF);
            int ch4 = (valueBytesParm[offsetParm+3] & 0xFF);
            returnInt = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return returnInt;
    }
    
    public static byte[] getBytesFromChar(char charValueParm)
    {
        byte[] returnBytes = new byte[2];        
        returnBytes[0] = (byte)((charValueParm >>>  8) & 0xFF);
        returnBytes[1] = (byte)((charValueParm >>>  0) & 0xFF);
        return returnBytes;
    }
    
    public static char getCharFromBytes(byte[] valueBytesParm)
    {
        char returnChar = 0;
        try
        {
            int ch1 = (valueBytesParm[0] & 0xFF);
            int ch2 = (valueBytesParm[1] & 0xFF);
            returnChar = (char)((ch1 << 8) + (ch2 << 0));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return returnChar;
    }
    
    public static char getCharFromBytes(byte[] valueBytesParm, int offsetParm)
    {
        char returnChar = 0;
        try
        {
            int ch1 = (valueBytesParm[offsetParm+0] & 0xFF);
            int ch2 = (valueBytesParm[offsetParm+1] & 0xFF);
            returnChar = (char)((ch1 << 8) + (ch2 << 0));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return returnChar;
    }

    public static char[] getCharsFromInt(int intValueParm)
    {
        char[] returnInt = new char[2];
        returnInt[0] = (char) ((intValueParm >>> 16) & 0xFFFF);
        returnInt[1] = (char) ((intValueParm >>> 0) & 0xFFFF);        
        return returnInt;
    }
    
    public static byte[] getBytesFromString(String strValueParm)
    {
        char[] tmpChars = strValueParm.toCharArray();
        byte[] returnBytes = new byte[tmpChars.length << 1];
        int j = 0;
        
        for (int i = 0; i < tmpChars.length; i++)
        {
            returnBytes[j] = (byte)((tmpChars[i] >>>  8) & 0xFF);
            j++;
            returnBytes[j] = (byte)((tmpChars[i] >>>  0) & 0xFF);
            j++;
        }
        
        return returnBytes;
    }
    
    public static String transBytesToStr(byte[] data)
    {
        int ch1 = 0;
        int ch2 = 0;
        char[] tmpReadChars = null;
        try
        {
            tmpReadChars = new char[data.length / 2];
            int j = 0;
            for (int i = 0; i < data.length; i += 2)
            {
                ch1 = data[i];
                ch2 = data[i + 1];
                if (ch2 < 0)
                {
                    ch2 = 256 + ch2;
                }
                tmpReadChars[j] = (char) ((ch1 << 8) + (ch2 << 0));
                j++;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return new String(tmpReadChars);
    }
    
    /**
     * 
     * @param data
     * @param beginPosiParm inclusive.
     * @param endPosiParm   exclusive.
     * @return
     * @throws Exception
     */
    public static char[] transBytesToChars(
                byte[] data, int beginPosiParm, int endPosiParm)
    {
        int ch1 = 0;
        int ch2 = 0;
        char[] tmpReadChars = null;
        int tmpTransBytesLength = endPosiParm - beginPosiParm;
        
        try
        {
            tmpReadChars = new char[tmpTransBytesLength / 2];
            int j = 0;
            for (int i = beginPosiParm; i < endPosiParm; i += 2)
            {
                ch1 = data[i];
                ch2 = data[i + 1];
                if (ch2 < 0)
                {
                    ch2 = 256 + ch2;
                }
                tmpReadChars[j] = (char) ((ch1 << 8) + (ch2 << 0));
                j++;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    
        return tmpReadChars;
    }
    
	public static byte[] byteAtrayPlus(Vector byteArrayParm)
	{
        int tmpByteLength = 0;
        byte[] tmpBytes = null;
        for (int i = 0; i < byteArrayParm.size(); i++)
        {
            tmpBytes = (byte[])byteArrayParm.elementAt(i);
            tmpByteLength += tmpBytes.length; 
        }
        
		byte[] returnBytes = new byte[tmpByteLength];
        tmpByteLength = 0;
        for (int i = 0; i < byteArrayParm.size(); i++)
        {
            tmpBytes = (byte[])byteArrayParm.elementAt(i);
            System.arraycopy(tmpBytes, 0, returnBytes, tmpByteLength, tmpBytes.length);
            tmpByteLength += tmpBytes.length;
        }
        
		return returnBytes;
	}
	
	/**
	 * 12个byte的报文头
	 *  byte位置           内容
	 *  0-3:      4byte的内容长度
	 *  4-5:      2byte的验证信息
	 *    6:      第7位是否压缩标志:0=没压缩;1=压缩
	 */
	public void writeMsgThroughTcp(Message sendMsgParm,
				OutputStream theSocketOPParm) throws IOException
	{
		byte[] tmpValueBytes = sendMsgParm.doMarshal();
		byte tmpIsZip = (byte) Constants.UnZip_ByteFlag;
		if (tmpValueBytes.length > Constants.ZipDataLimit)
		{
			tmpIsZip = (byte) Constants.Zip_ByteFlag;
			tmpValueBytes = DataZipUnZip.zipData(tmpValueBytes);
		}

		byte[] tmpSendBytes = new byte[tmpValueBytes.length + 12];
		int tmpValueBytesLength = tmpValueBytes.length;
		
		tmpSendBytes[0] = (byte)((tmpValueBytesLength >>> 24) & 0xFF);
		tmpSendBytes[1] = (byte)((tmpValueBytesLength >>> 16) & 0xFF);
		tmpSendBytes[2] = (byte)((tmpValueBytesLength >>>  8) & 0xFF);
		tmpSendBytes[3] = (byte)((tmpValueBytesLength >>>  0) & 0xFF);
		
		// magic
		tmpSendBytes[Magic_Pois_1] = (byte)8;
		tmpSendBytes[Magic_Pois_2] = (byte)9;
		
		// 是否压缩标识
		tmpSendBytes[IsZip_Pois] = tmpIsZip;
		System.arraycopy(tmpValueBytes, 0, tmpSendBytes, 12,
				tmpValueBytes.length);

		theSocketOPParm.write(tmpSendBytes);
	}
	
	public Message readMsg(InputStream theInputStreamParm) throws Exception
	{
		Message retMsg = null;
		byte[] tmpBytes = null;
		byte[] tmpHeadBytes = null;

		tmpHeadBytes = readBytes(theInputStreamParm, 12);
		if (tmpHeadBytes != null)
		{
			int tmpDataDetailLength = Utilities.getIntFromBytes(tmpHeadBytes, 0);
			if (tmpHeadBytes[Magic_Pois_1] != (byte)8 || tmpHeadBytes[Magic_Pois_2] != (byte)9)
			{
				System.out.println("接收的头信息异常:magic不对, "+tmpHeadBytes[4]+"#"+tmpHeadBytes[5]);
				logger.info("接收的头信息异常:magic不对, "+tmpHeadBytes[4]+"#"+tmpHeadBytes[5]);
				throw new MsgHeadException("接收的头信息异常:magic不对, "+tmpHeadBytes[4]+"#"+tmpHeadBytes[5]);
			}
			
			tmpBytes = readBytes(theInputStreamParm, tmpDataDetailLength);
			if (tmpBytes != null)
			{
				boolean tmpIsZip = false;
				if (tmpHeadBytes[IsZip_Pois] == Constants.Zip_ByteFlag)
				{
					tmpIsZip = true;
				}
				
				if (tmpIsZip == true)
				{
					tmpBytes = DataZipUnZip
							.unZipToMem(tmpBytes, 0, tmpBytes.length);
				}
	
				retMsg = MsgMarshallerFactory.getInstance().doUnMarshal(tmpBytes);
			}
		}
		
		return retMsg;
	}

	/**
	 * to int.
	 * 
	 * @param b byte array.
	 * @param off offset.
	 * @return int.
	 */
	public int bytes2int(byte[] b, int off)
	{
		return ((b[off + 3] & 0xFF) << 0) +
	       ((b[off + 2] & 0xFF) << 8) +
	       ((b[off + 1] & 0xFF) << 16) +
	       ((b[off + 0]) << 24);
	}
	
	private byte[] readBytes(InputStream theInputStreamParm,
								int totalLengthParm) throws Exception
	{
		int tmpReadCnt = 0;
		int tmpWantToReadCnt = totalLengthParm;
		int tmpHaveReadCnt = 0;
		byte[] tmpBytes = null;
		byte[] returnBytes = new byte[totalLengthParm];
		while (true)
		{
			if (tmpWantToReadCnt < 1024)
			{
				tmpBytes = new byte[tmpWantToReadCnt];
			}
			else
			{
				tmpBytes = new byte[1024];
			}
		
			//long tmpStartTime = System.currentTimeMillis();
			tmpReadCnt = theInputStreamParm.read(tmpBytes);
			//long tmpEndTime = System.currentTimeMillis() - tmpStartTime;
			//System.out.println("读数据耗时" + tmpEndTime + "读入数据量" + tmpReadCnt);
			
			if (tmpReadCnt != -1)
			{
				System.arraycopy(tmpBytes, 0, returnBytes, tmpHaveReadCnt,
						tmpReadCnt);
				tmpHaveReadCnt += tmpReadCnt;
				tmpWantToReadCnt -= tmpReadCnt;
				if (tmpWantToReadCnt == 0)
				{
					return returnBytes;
				}
			}
			else
			{
				System.out.println("socket 正常关闭");
				return null;
			}
		}
	}
	
	public static long getLongFromBytes(byte[] valueBytesParm, int offsetParm)
	{
		long returnLong = -1;
		try
		{
			byte[] tmpHighBytes = new byte[4];
			System.arraycopy(valueBytesParm, offsetParm, tmpHighBytes, 0, 4);
			byte[] tmpLowBytes = new byte[4];
			System.arraycopy(valueBytesParm, offsetParm+4, tmpLowBytes, 0, 4);
			returnLong = ((long)(getIntFromBytes(tmpHighBytes)) << 32)
						+ (getIntFromBytes(tmpLowBytes) & 0xFFFFFFFFL);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return returnLong;
	}
    
    public static long getLongFromBytes(byte[] valueBytesParm)
    {
        long returnLong = -1;
        try
        {
            byte[] tmpHighBytes = new byte[4];
            System.arraycopy(valueBytesParm, 0, tmpHighBytes, 0, 4);
            byte[] tmpLowBytes = new byte[4];
            System.arraycopy(valueBytesParm, 4, tmpLowBytes, 0, 4);
            returnLong = ((long)(getIntFromBytes(tmpHighBytes)) << 32)
                        + (getIntFromBytes(tmpLowBytes) & 0xFFFFFFFFL);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return returnLong;
    }
    
    public static byte[] getBytesFromDouble(double dvParm)
    {
        long v = Double.doubleToLongBits(dvParm);
        return getBytesFromLong(v);
    }
    
    public static double getDoubleFromBytes(byte[] byteValues)
    {
        long tmpLongValue = getLongFromBytes(byteValues);
        return Double.longBitsToDouble(tmpLongValue);
    }
    
    public static double getDoubleFromBytes(byte[] byteValues, int offsetParm)
    {
        long tmpLongValue = getLongFromBytes(byteValues, offsetParm);
        return Double.longBitsToDouble(tmpLongValue);
    }
    
    public static int getIntValueFromFormatChars(char[] valueCharParm,
                                                 int offsetParm)
    {
        int retInt = 0;
        retInt = (valueCharParm[offsetParm+0] - 48) * 10 + (valueCharParm[offsetParm+1] - 48);
        return retInt;
    }

    /*
     * 生产有格式的字符串,格式内容如下:
     * 1个char的长度的位数+长度+内容,如 210abcdefgabc
     */
    public static String generateFormatStr(String sourceStrParm)
    {
        StringBuffer tmpStrBuf = new StringBuffer();
        int tmpSourceStrLength = sourceStrParm.length();
        String tmpSourceStrLengthStr = Integer.toString(tmpSourceStrLength);
        int tmpLengthDigits = tmpSourceStrLengthStr.length();
        tmpStrBuf.append(tmpLengthDigits);
        tmpStrBuf.append(tmpSourceStrLengthStr);
        tmpStrBuf.append(sourceStrParm);
        return tmpStrBuf.toString();
    }
    
    public static ArrayList parseFormatStr(String sourceStrParm)
    {
        ArrayList retList = new ArrayList(10);
        int tmpBeginPosi = 0;
        int tmpOneAttrLengthDigits = 0;
        int tmpOneAttrLength = 0;
        String tmpOneAttrLengthStr = null;
        String tmpOneAttr = "";
        int tmpTotalStrLength = sourceStrParm.length();
        
        while (tmpBeginPosi < tmpTotalStrLength)
        {
            tmpOneAttrLengthDigits = sourceStrParm.charAt(tmpBeginPosi);
            tmpOneAttrLengthDigits -= 48;
            tmpBeginPosi++;
            tmpOneAttrLengthStr = sourceStrParm
                .substring(tmpBeginPosi, tmpBeginPosi+tmpOneAttrLengthDigits);
            tmpOneAttrLength = Integer.parseInt(tmpOneAttrLengthStr);
            tmpBeginPosi += tmpOneAttrLengthDigits;
            tmpOneAttr = sourceStrParm.substring(
                    tmpBeginPosi, tmpBeginPosi+tmpOneAttrLength);
            tmpBeginPosi += tmpOneAttrLength;
            retList.add(tmpOneAttr);
        }
        
        return retList;
    }
    
    /**
     * @return  boolean
     */
    public static void delFile(final String filePathAndName)
    {
        try
        {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            File myDelFile = new File(filePath);
            myDelFile.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /*
     * 生产有格式的byte串,格式内容如下: 1个byte的长度的位数+长度+内容,如 210abcdefgabc
     */
    public static byte[] generateFormatBytes(byte[] sourceBytesParm)
    {
        String tmpSourceStrLengthStr = Integer.toString(sourceBytesParm.length);
        byte[] tmpLength = getBytesFromString(tmpSourceStrLengthStr);
        int tmpLengthDigits = tmpLength.length;
        byte[] retBytes = new byte[1 + tmpLengthDigits + sourceBytesParm.length];
        retBytes[0] = (byte)tmpLengthDigits;
        System.arraycopy(tmpLength, 0, retBytes, 1, tmpLength.length);
        System.arraycopy(sourceBytesParm, 0, retBytes, 1+tmpLength.length, sourceBytesParm.length);
        return retBytes;
    }
    
    public static ArrayList parseFormatBytes(byte[] sourceBytesParm)
    {
        ArrayList retList = new ArrayList(10);
        int tmpBeginPosi = 0;
        int tmpOneAttrLengthDigits = 0;
        int tmpOneAttrLength = 0;
        String tmpOneAttrLengthStr = null;
        byte[] tmpOneAttrLengthBytes = null;
        byte[] tmpOneAttrBytes = null;
        int tmpTotalStrLength = sourceBytesParm.length;

        while (tmpBeginPosi < tmpTotalStrLength)
        {
            tmpOneAttrLengthDigits = sourceBytesParm[tmpBeginPosi];
            tmpBeginPosi++;
            tmpOneAttrLengthBytes = new byte[tmpOneAttrLengthDigits];
            System.arraycopy(sourceBytesParm, tmpBeginPosi,
                tmpOneAttrLengthBytes, 0,
                tmpOneAttrLengthDigits);
            tmpOneAttrLengthStr = new String(transBytesToChars(
                tmpOneAttrLengthBytes, 0 ,tmpOneAttrLengthDigits));
            tmpOneAttrLength = Integer.parseInt(tmpOneAttrLengthStr);
            tmpBeginPosi += tmpOneAttrLengthDigits;
            tmpOneAttrBytes = new byte[tmpOneAttrLength];
            System.arraycopy(sourceBytesParm, tmpBeginPosi,
                tmpOneAttrBytes, 0, tmpOneAttrLength);
            tmpBeginPosi += tmpOneAttrLength;
            retList.add(tmpOneAttrBytes);
        }

        return retList;
    }

    public static ArrayList parseFormatBytes(byte[] sourceBytesParm,
    		int beginPosiParm, int paramSize)
    {
        ArrayList retList = new ArrayList(10);
        int tmpBeginPosi = beginPosiParm;
        int tmpOneAttrLengthDigits = 0;
        int tmpOneAttrLength = 0;
        String tmpOneAttrLengthStr = null;
        byte[] tmpOneAttrLengthBytes = null;
        byte[] tmpOneAttrBytes = null;
        int tmpTotalStrLength = sourceBytesParm.length;

        while (tmpBeginPosi < tmpTotalStrLength)
        {
            tmpOneAttrLengthDigits = sourceBytesParm[tmpBeginPosi];
            tmpBeginPosi++;
            tmpOneAttrLengthBytes = new byte[tmpOneAttrLengthDigits];
            System.arraycopy(sourceBytesParm, tmpBeginPosi,
                tmpOneAttrLengthBytes, 0,
                tmpOneAttrLengthDigits);
            tmpOneAttrLengthStr = new String(transBytesToChars(
                tmpOneAttrLengthBytes, 0 ,tmpOneAttrLengthDigits));
            tmpOneAttrLength = Integer.parseInt(tmpOneAttrLengthStr);
            tmpBeginPosi += tmpOneAttrLengthDigits;
            tmpOneAttrBytes = new byte[tmpOneAttrLength];
            System.arraycopy(sourceBytesParm, tmpBeginPosi,
                tmpOneAttrBytes, 0, tmpOneAttrLength);
            tmpBeginPosi += tmpOneAttrLength;
            retList.add(tmpOneAttrBytes);
            if (retList.size() == paramSize)
            {
            	retList.add(tmpBeginPosi);
            	break;
            }
        }

        return retList;
    }
    
    public static Vector getQuotationDetailItemStr(String strSourceParm)
    {
        Vector tmpVector = new Vector();
        int haveQuotaNum = 0;
        int beginPos = 0;
        char[] tmpStrChars = strSourceParm.toCharArray();        
        for (int i = 0; i < tmpStrChars.length; i++)
        {
            if (tmpStrChars[i] == 34)
            {
                haveQuotaNum++;
                if (haveQuotaNum == 2)
                {
                    haveQuotaNum = 0;
                    tmpVector.add(strSourceParm.substring(beginPos+1, i));
                }
                else
                {
                    beginPos = i;
                }
            }
        }
        
        return tmpVector;
    }
    
    public static String transXMLSepcialChar(String sourceXMLStrParm)
    {
        StringBuffer tmpStrBuffer = new StringBuffer(100);
        int tmpBeginPosi = 0;
        int tmpEndPosi = sourceXMLStrParm.indexOf("&");
        while (tmpEndPosi != -1)
        {
            tmpStrBuffer.append(
                    sourceXMLStrParm.substring(tmpBeginPosi, tmpEndPosi));
            tmpStrBuffer.append("&amp;");
            tmpBeginPosi = tmpEndPosi+1;
            tmpEndPosi = sourceXMLStrParm.indexOf("&", tmpBeginPosi);
        }
        tmpStrBuffer.append(sourceXMLStrParm.substring(tmpBeginPosi));
        
        return tmpStrBuffer.toString();
    }
    
    /**
     */
    public static int getCurrentTimeHour()
    {
        return CurrentDateFormat.getInstance().getCurTimeHour();
    }
    
	final static char[]	HEX_DIGIT	=
									{ '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    public final static char[][] HMS_CHARS_100 = {
        {'0','0'}, {'0','1'}, {'0','2'}, {'0','3'}, {'0','4'}, {'0','5'}, {'0','6'}, {'0','7'}, {'0','8'}, {'0','9'},
        {'1','0'}, {'1','1'}, {'1','2'}, {'1','3'}, {'1','4'}, {'1','5'}, {'1','6'}, {'1','7'}, {'1','8'}, {'1','9'},
        {'2','0'}, {'2','1'}, {'2','2'}, {'2','3'}, {'2','4'}, {'2','5'}, {'2','6'}, {'2','7'}, {'2','8'}, {'2','9'},
        {'3','0'}, {'3','1'}, {'3','2'}, {'3','3'}, {'3','4'}, {'3','5'}, {'3','6'}, {'3','7'}, {'3','8'}, {'3','9'},
        {'4','0'}, {'4','1'}, {'4','2'}, {'4','3'}, {'4','4'}, {'4','5'}, {'4','6'}, {'4','7'}, {'4','8'}, {'4','9'},
        {'5','0'}, {'5','1'}, {'5','2'}, {'5','3'}, {'5','4'}, {'5','5'}, {'5','6'}, {'5','7'}, {'5','8'}, {'5','9'},
        {'6','0'}, {'6','1'}, {'6','2'}, {'6','3'}, {'6','4'}, {'6','5'}, {'6','6'}, {'6','7'}, {'6','8'}, {'6','9'},
        {'7','0'}, {'7','1'}, {'7','2'}, {'7','3'}, {'7','4'}, {'7','5'}, {'7','6'}, {'7','7'}, {'7','8'}, {'7','9'},
        {'8','0'}, {'8','1'}, {'8','2'}, {'8','3'}, {'8','4'}, {'8','5'}, {'8','6'}, {'8','7'}, {'8','8'}, {'8','9'},
        {'9','0'}, {'9','1'}, {'9','2'}, {'9','3'}, {'9','4'}, {'9','5'}, {'9','6'}, {'9','7'}, {'9','8'}, {'9','9'}
        };
    
}