package com.cn.netcomm.communication;

import com.cn.netcomm.communication.message.Message;


/**
 * server端业务处理接口
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-2-3
 */
public interface WorkerHandler
{
	/**
	 * 业务处理方法
	 * @param reqMsgParm
	 * @return
	 */
	public Message doMsgHandler(Message reqMsgParm);
	public void transportOnException(Exception exception);
}
