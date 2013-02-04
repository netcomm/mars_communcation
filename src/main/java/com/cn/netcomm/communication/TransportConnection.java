package com.cn.netcomm.communication;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;
import com.cn.netcomm.communication.transport.DefaultTransportListener;
import com.cn.netcomm.communication.transport.InactiveConnectionMonitor;
import com.cn.netcomm.communication.transport.ResponseCorrelator;
import com.cn.netcomm.communication.transport.Transport;
import com.cn.netcomm.communication.transport.TransportFilter;

/**
 * TransportConnection
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-1-30
 */
public class TransportConnection
{
	private static Logger logger =
			Logger.getLogger(TransportConnection.class.getName());
	private TransportFilter theTransport;
	private WorkerHandlerThreadPool workerHandlerThreadPool;
	
	/**
	 * 
	 * @param transportParm
	 * @param inactiveMonitorTypeParm 心跳动作模式
	 * @param readCheckTimeParm 心跳的轮询时间,单位(毫秒), 默认30000
	 * @param initialDelayTimeParm 心跳动作启动的延时时间,单位(毫秒), 默认10000
	 */
	public TransportConnection(Transport transportParm, int inactiveMonitorTypeParm,
			int readCheckTimeParm, int initialDelayTimeParm,
			WorkerHandlerThreadPool workerHandlerThreadPoolParm)
	{
		workerHandlerThreadPool = workerHandlerThreadPoolParm;
		
		// 创建Transport职责链的第二个环节,它负责心跳消息的处理
		InactiveConnectionMonitor tmpInactiveTrans
			= new InactiveConnectionMonitor(
				transportParm, inactiveMonitorTypeParm,
				readCheckTimeParm, initialDelayTimeParm);
		
		// 创建Transport职责链的第一个环节,它负责响应消息和原始发送请求的匹配处理
		ResponseCorrelator tmpNewRespCorrelatorTrans =
					new ResponseCorrelator(tmpInactiveTrans);
		theTransport = tmpNewRespCorrelatorTrans;
		theTransport.setTransportListener(new DefaultTransportListener()
		{
            public void onCommand(Message msgParm)
            {
            	try
            	{
            		doHandleOneMsg(msgParm);
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}
            }
            
            public void onException(Exception exception)
            {
            	transportOnException(exception);
            }
            
            public void transportResumed()
            {
            	transportOnResumed();
            }
            
            public void transportFirstConnect()
        	{
            	transportOnFirstConnect();
        	}
		});
	}
	
	private void doHandleOneMsg(Message msgParm)
	{
		if (workerHandlerThreadPool != null)
		{
			workerHandlerThreadPool.doHandle(msgParm, this, theTransport);
		}
		else
		{
			logger.error("workerHandlerThreadPool为null,不能处理发过来的请求,请求的具体内容: "
					+ new String(msgParm.getContent()));
		}
	}
	
	/**
	 * 业务处理方法,该方法要被子类重载以完成具体业务职能
	 * @param reqMsgParm
	 * @return
	 * @throws Throwable
	 */
	public Message doMsgHandler(Message reqMsgParm) throws Throwable
	{
		Message response = null;
		if (reqMsgParm.isResponseRequired())
		{
			response = new Message(MsgMarshallerFactory.Response_MsgType,
        		new String("").getBytes(), false);
		}
		return response;
	}
	
	public void transportOnException(Exception exception)
	{
	}
	
	public void transportOnResumed()
	{
	}
	
	public void transportOnFirstConnect()
	{
	}

	public void stop() throws Exception
	{
		workerHandlerThreadPool = null;
		if (theTransport != null)
		{
			theTransport.stop();
		}
	}
	
	public Message sendMsg(Message msgParm, int timeOutParm) throws IOException
	{
		msgParm.setResponseRequired(true);
		return (Message)theTransport.request(msgParm, timeOutParm);
	}
	
	public void sendMsg(Message msgParm) throws IOException
	{
		theTransport.request(msgParm);
	}
	
	public TransportFilter getTheTransport()
	{
		return theTransport;
	}

	public void setTheTransport(TransportFilter theTransport)
	{
		this.theTransport = theTransport;
	}
}
