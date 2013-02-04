package com.cn.netcomm.communication.exception;

import java.io.IOException;

public class ReqTimeOutException extends IOException
{
	public ReqTimeOutException(String message)
	{
		super(message);
	}
	
	public ReqTimeOutException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
