package com.cn.netcomm.communication;

import com.cn.netcomm.communication.message.Message;


/**
 * 客户端消息处理的基类
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-1-31
 */
public abstract class AbstractClientHandler
{
	/**
	 * 接收服务器端发过来的请求消息,适用于双向通讯,client端也需要处理服务器端发过来的请求时。
	 * @param reqMsgParm
	 * @return
	 */
	public abstract Message doMsgHandler(Message reqMsgParm);
	
	/**
	 * 连接发生异常时,触发该方法
	 * @param exception
	 */
	public void transportOnException(Exception exception)
	{
		
	}
	
	/**
	 * 连接重新恢复建立
	 */
	public void transportOnResumed()
	{
		
	}
	
	/**
	 * 第一次建立连接
	 */
	public void transportOnFirstConnect()
	{
		
	}
}
