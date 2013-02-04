package com.cn.netcomm.test;

import com.cn.netcomm.communication.SocketServer;
import com.cn.netcomm.communication.WorkerHandler;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;

public class TestSocketServer
{
	public static void main(String args[])
	{
		SocketServer tmpSocketServer = new SocketServer(11000, new TestServerMsgHanlder());
		tmpSocketServer.start();
	}
}

/**
 * 模拟处理client端发送过来请求的业务类
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-2-3
 */
class TestServerMsgHanlder implements WorkerHandler
{
	private int count = 0;
	private long tmpStartTime = System.currentTimeMillis();
	
	public TestServerMsgHanlder()
	{
		
	}
	
	@Override
	public Message doMsgHandler(Message reqMsgParm)
	{
		byte[] tmpBytes = new byte[1024 * 1];
		tmpBytes[10] = (byte)10;
		tmpBytes[1000] = (byte)100;
		Message retMsg = new Message(MsgMarshallerFactory.Response_MsgType,
				tmpBytes, false);
		int oneTimeThrdCnt = 1000;
		count++;
		if (count % oneTimeThrdCnt == 0)
		{
			System.out.println(Thread.currentThread() +"消息笔数 "+oneTimeThrdCnt+" 总消费消息 "+count
				+" 耗时 "+(System.currentTimeMillis() - tmpStartTime));
			tmpStartTime = System.currentTimeMillis();
		}
		
		return retMsg;
	}

	@Override
	public void transportOnException(Exception exception)
	{
		System.out.println("链接断开 "+exception.getMessage()+"已处理消息数量 "+count);
	}
}