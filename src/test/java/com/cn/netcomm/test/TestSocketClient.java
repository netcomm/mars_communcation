package com.cn.netcomm.test;

import com.cn.netcomm.communication.AbstractClientHandler;
import com.cn.netcomm.communication.SocketClient;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;

public class TestSocketClient
{
	public static void main(String args[])
	{
		SocketClient tmpSocketClient = new SocketClient("127.0.0.1", 11000, 10, new TestClientMsgHanlder());
		
		byte[] tmpRequst = new byte[1024];
		tmpRequst[0] = 100;
		tmpRequst[1023] = 101;
		for (int j = 0; j < 50000; j++)
		{
			long tmpStartTime = System.currentTimeMillis();
			for (int i = 0; i < 1000; i++)
			{
				try
				{
					byte[] tmpResponseBytes = tmpSocketClient.sendMsg(tmpRequst, 10000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			System.out.println(j + " 耗时 " + (System.currentTimeMillis() - tmpStartTime));
		}
	}
}

/**
 * 模拟处理服务器端发送过来请求的业务类
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-2-3
 */
class TestClientMsgHanlder extends AbstractClientHandler
{
	private int count = 0;
	
	public TestClientMsgHanlder()
	{
		
	}
	
	@Override
	public Message doMsgHandler(Message reqMsgParm)
	{
		Message retMsg = null;
		
		try
		{
			retMsg = new Message(MsgMarshallerFactory.Response_MsgType,
				"测试".getBytes("UTF-8"), false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return retMsg;
	}

	@Override
	public void transportOnException(Exception exception)
	{
		System.out.println("链接断开 "+exception.getMessage()+"已处理消息数量 "+count);
	}
}