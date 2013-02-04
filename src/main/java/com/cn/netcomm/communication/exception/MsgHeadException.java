package com.cn.netcomm.communication.exception;

import java.io.IOException;

public class MsgHeadException extends IOException
{
	public MsgHeadException(String message)
	{
		super(message);
	}
	
	public MsgHeadException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
