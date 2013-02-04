package com.cn.netcomm.communication.message;


public class MsgMarshallerFactory
{
	private static MsgMarshallerFactory service = new MsgMarshallerFactory();
	// 心跳消息类型
	public static byte KeepAlive_MsgType = 0;
	// 请求消息类型
	public static byte Request_MsgType = 2;
	// 响应消息类型
	public static byte Response_MsgType = 3;
	// 通讯异常消息类型
	public static byte TransportExceptionResponse_MsgType = 4;
	// 业务异常消息类型
	public static byte BusinExceptionResponse_MsgType = 5;
	
	private MsgMarshallerFactory()
	{
		
	}
	
	public static MsgMarshallerFactory getInstance()
	{
		return service;
	}
	
	public Message doUnMarshal(byte[] recievedDetailBytesParm)
	{
		Message tmpNewMsg = new Message();
		tmpNewMsg.doUnMarshal(recievedDetailBytesParm);
		return tmpNewMsg;
	}
}
