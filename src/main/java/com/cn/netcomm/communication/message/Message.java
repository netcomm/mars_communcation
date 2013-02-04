package com.cn.netcomm.communication.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import com.cn.netcomm.communication.util.Utilities;


/**
 * 消息
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-1-31
 */
public class Message implements Serializable
{
	private long msgId;
	// 消息类型
	private byte msgType;
	// 消息体
	protected byte[] content;
	// 是否需要响应
	private boolean isResponseRequired = false;
	// 其他属性
	private HashMap<String, String> properties;
	private Object theAttachment;
	
	public Message()
	{
		
	}
	
	public Message(byte msgTypeParm, byte[] contentParm,
			boolean isResponseRequiredParm)
	{
		msgId = MsgIdGenerator.getInstance().generateId();
		msgType = msgTypeParm;
		content = contentParm;
		isResponseRequired = isResponseRequiredParm;
	}
	
	public Message(long idParm, byte msgTypeParm, byte[] contentParm,
			boolean isResponseRequiredParm)
	{
		msgId = idParm;
		msgType = msgTypeParm;
		content = contentParm;
		isResponseRequired = isResponseRequiredParm;
	}
	
	public byte[] getContent()
	{
		return content;
	}
	
	public int getMsgType()
	{
		return msgType;
	}

	public void setResponseRequired(boolean isResponseRequired)
	{
		this.isResponseRequired = isResponseRequired;
	}

	public long getMsgId()
	{
		return msgId;
	}

	public boolean isResponseRequired()
	{
		return isResponseRequired;
	}
	
	public void addOneProperty(String keyParm, String valueParm)
	{
		if (properties == null)
		{
			properties = new HashMap();
		}
		
		properties.put(keyParm, valueParm);
	}
	
	public String getOneProperty(String keyParm)
	{
		if (properties != null)
		{
			return properties.get(keyParm);
		}
		
		return null;
	}
	
	public void doUnMarshal(byte[] msgDataBytesParm)
	{
		msgId = Utilities.getLongFromBytes(msgDataBytesParm, 0);
		msgType = msgDataBytesParm[8];
		boolean tmpIsResponseRequired = false;
		if (msgDataBytesParm[9] == (byte)1)
		{
			tmpIsResponseRequired = true;
		}
		isResponseRequired = tmpIsResponseRequired;
		
		int tmpContentLength = Utilities.getIntFromBytes(msgDataBytesParm, 10);
		int tmpPropertiesLength = Utilities.getIntFromBytes(msgDataBytesParm, 14);
		
		int tmpPosi = 18;
		byte[] tmpContent = new byte[tmpContentLength];
		System.arraycopy(msgDataBytesParm, tmpPosi, tmpContent,
				0, tmpContentLength);
		content = tmpContent;
		tmpPosi += tmpContentLength;
		
		if (tmpPropertiesLength > 0)
		{
			byte[] tmpPropertiesBytes = new byte[tmpPropertiesLength];
			System.arraycopy(msgDataBytesParm, tmpPosi, tmpPropertiesBytes,
					0, tmpPropertiesLength);
			unMarshalProperties(tmpPropertiesBytes);
		}
	}
	
	public byte[] doMarshal()
	{
		byte[] retBytes = null;
		byte tmpMsgTypeByte = msgType;
		byte tmpIsResponseRequired = (byte)0;
		if (isResponseRequired() == true)
		{
			tmpIsResponseRequired = (byte)1;
		}
		
		byte[] tmpContent = getContent();
		byte[] tmpFormatPropBytes =
			marshalPropertiesToFormatBytes();
		
		int tmpContentLength = tmpContent.length;
		int tmpFormatPropBytesLength = tmpFormatPropBytes.length;
		
		retBytes = new byte[8 + 1
		                    + 1 + 8 + tmpContentLength
		                    + tmpFormatPropBytesLength];
		
		retBytes[0] = (byte)((msgId >> 56) & 0xFF);
		retBytes[1] = (byte)((msgId >>> 48) & 0xFF);
		retBytes[2] = (byte)((msgId >>> 40) & 0xFF);
		retBytes[3] = (byte)((msgId >>> 32) & 0xFF);
		retBytes[4] = (byte)((msgId >>> 24) & 0xFF);
		retBytes[5] = (byte)((msgId >>> 16) & 0xFF);
		retBytes[6] = (byte)((msgId >>>  8) & 0xFF);
		retBytes[7] = (byte)((msgId >>>  0) & 0xFF);
		
		retBytes[8] = tmpMsgTypeByte;
		
		retBytes[9] = tmpIsResponseRequired;
		
		retBytes[10] = (byte)((tmpContentLength >>> 24) & 0xFF);
		retBytes[11] = (byte)((tmpContentLength >>> 16) & 0xFF);
		retBytes[12] = (byte)((tmpContentLength >>>  8) & 0xFF);
		retBytes[13] = (byte)((tmpContentLength >>>  0) & 0xFF);
		
		retBytes[14] = (byte)((tmpFormatPropBytesLength >>> 24) & 0xFF);
		retBytes[15] = (byte)((tmpFormatPropBytesLength >>> 16) & 0xFF);
		retBytes[16] = (byte)((tmpFormatPropBytesLength >>>  8) & 0xFF);
		retBytes[17] = (byte)((tmpFormatPropBytesLength >>>  0) & 0xFF);
		
		int tmpPosi = 18;
		System.arraycopy(tmpContent, 0, retBytes,
				tmpPosi, tmpContent.length);
		tmpPosi = tmpPosi + tmpContent.length;
		
		System.arraycopy(tmpFormatPropBytes, 0, retBytes,
				tmpPosi, tmpFormatPropBytes.length);
		
		return retBytes;
	}

	public void setMsgId(long msgId)
	{
		this.msgId = msgId;
	}
	
	public int getSize()
	{
		int retSz = 8;
		if (content != null)
		{
			retSz += content.length;
		}
		return retSz;
	}
	
	private void unMarshalProperties(byte[] detailBytesParm)
	{
		ArrayList tmpBytes = Utilities.parseFormatBytes(detailBytesParm);
		int tmpSize = tmpBytes.size()/2;
		for (int i = 0; i < tmpSize; i++)
		{		
			if (properties == null)
			{
				properties = new HashMap();
			}
			properties.put(new String((byte[])tmpBytes.get(i * 2)),
						new String((byte[])tmpBytes.get((i * 2)+1)));
		}
	}
	
	private byte[] marshalPropertiesToFormatBytes()
	{
		byte[] retBytes = new byte[0];
		if (properties == null)
		{
			return retBytes;
		}
		
		byte[] tmpKeyFormatBytes = null;
		byte[] tmpValueFormatBytes = null;
		byte[] tmpAllFormatBytes = new byte[0];
		byte[] tmpOneFormatBytes = new byte[0];
		String[] tmpKeys = Utilities.getStrMapKeys(properties);
		for (int i = 0; i < tmpKeys.length; i++)
		{
			String tmpValue = properties.get(tmpKeys[i]);
			tmpKeyFormatBytes =
				Utilities.generateFormatBytes(tmpKeys[i].getBytes());
			tmpValueFormatBytes =
				Utilities.generateFormatBytes(tmpValue.getBytes());
			tmpOneFormatBytes = new byte[tmpKeyFormatBytes.length
			                             +tmpValueFormatBytes.length];
			System.arraycopy(tmpKeyFormatBytes, 0,
					tmpOneFormatBytes, 0, tmpKeyFormatBytes.length);
			System.arraycopy(tmpValueFormatBytes, 0,
					tmpOneFormatBytes, tmpKeyFormatBytes.length,
					tmpValueFormatBytes.length);
			byte[] tmpByte =
				new byte[tmpAllFormatBytes.length + tmpOneFormatBytes.length];
			System.arraycopy(tmpAllFormatBytes, 0,
					tmpByte, 0, tmpAllFormatBytes.length);
			System.arraycopy(tmpOneFormatBytes, 0,
					tmpByte, tmpAllFormatBytes.length, tmpOneFormatBytes.length);
			tmpAllFormatBytes = tmpByte;
		}
		
		retBytes = Utilities.generateFormatBytes(tmpAllFormatBytes);
		return retBytes;
	}

	public void setContent(byte[] content)
	{
		this.content = content;
	}

	public HashMap<String, String> getProperties()
	{
		return properties;
	}

	public Object getTheAttachment() {
		return theAttachment;
	}

	public void setTheAttachment(Object theAttachment) {
		this.theAttachment = theAttachment;
	}
}
