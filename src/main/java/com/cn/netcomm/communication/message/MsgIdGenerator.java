package com.cn.netcomm.communication.message;

public class MsgIdGenerator
{
	private static MsgIdGenerator service = new MsgIdGenerator();
	private long messageId = 0;
	
	private MsgIdGenerator()
	{
		
	}
	
	public static MsgIdGenerator getInstance()
	{
		return service;
	}
	
	public synchronized long generateId()
	{
		messageId++;
		return messageId;
	}
}
