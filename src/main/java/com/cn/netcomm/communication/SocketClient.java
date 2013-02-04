package com.cn.netcomm.communication;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;
import com.cn.netcomm.communication.transport.AutoReconnectDataSocket;
import com.cn.netcomm.communication.transport.InactiveConnectionMonitor;
import com.cn.netcomm.communication.transport.Transport;


/**
 * SocketClient类
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-1-31
 */
public class SocketClient
{
	private static Logger logger =
		Logger.getLogger(SocketClient.class.getName());
	// 默认等待15秒进行重连
	private final static int Reconnection_Delay_Time = 15 * 1000;
	// 心跳的轮询时间,单位(毫秒), 默认30000
	private final static int ReadCheck_Time = 30000;
	// 心跳动作启动的延时时间,单位(毫秒), 默认10000
	private final static int InitialDelay_Time = 10000;
	// 处理服务器端主动发送请求的工作线程池大小, 默认等于-1(不创建)
	private final static int WorkerPool_Threads = -1;
	private ClientTransportConnection theClientTransportConnection;
	
	public SocketClient(String serverIpAddrParm, int portParm)
	{
		this(serverIpAddrParm, portParm, Reconnection_Delay_Time);
	}
	
	/**
	 * 
	 * @param serverIpAddrParm 服务器ip地址
	 * @param portParm 服务器端口
	 * @param reconnectionDelayTimeParm 连接发生异常,每次重连的等待时间
	 */
	public SocketClient(String serverIpAddrParm, int portParm,
			int reconnectionDelayTimeParm)
	{
		this(serverIpAddrParm, portParm, reconnectionDelayTimeParm, ReadCheck_Time,
				InitialDelay_Time, WorkerPool_Threads, null);
	}
	
	/**
	 * 
	 * @param serverIpAddrParm 服务器ip地址
	 * @param portParm 服务器端口
	 * @param reconnectionDelayTimeParm 连接发生异常,每次重连的等待时间
	 * @param readCheckTimeParm 心跳的轮询时间,单位(毫秒), 默认30000
	 * @param initialDelayTimeParm 心跳动作启动的延时时间,单位(毫秒), 默认10000
	 * @param workerPoolThreadsParm 处理服务器端主动发送请求的工作线程池大小, 默认10
	 */
	public SocketClient(String serverIpAddrParm, int portParm,
			int workerPoolThreadsParm, AbstractClientHandler theAbstractClientHandlerParm)
	{
		AutoReconnectDataSocket tmpSocketClient =
				new AutoReconnectDataSocket(serverIpAddrParm,
						portParm, Reconnection_Delay_Time);
		
		WorkerHandlerThreadPool tmpWorkerHandlerThreadPool = null;
		if (workerPoolThreadsParm != WorkerPool_Threads)
		{
			tmpWorkerHandlerThreadPool =
				new WorkerHandlerThreadPool(workerPoolThreadsParm);
		}
		
		theClientTransportConnection = new ClientTransportConnection(tmpSocketClient,
						InactiveConnectionMonitor.Only_Write_InactiveMonitor,
						ReadCheck_Time,
						InitialDelay_Time,
						tmpWorkerHandlerThreadPool,
						theAbstractClientHandlerParm);
		
		Thread threadMe = new Thread(tmpSocketClient);
		threadMe.start();
	}
	
	/**
	 * 
	 * @param serverIpAddrParm 服务器ip地址
	 * @param portParm 服务器端口
	 * @param reconnectionDelayTimeParm 连接发生异常,每次重连的等待时间
	 * @param readCheckTimeParm 心跳的轮询时间,单位(毫秒), 默认30000
	 * @param initialDelayTimeParm 心跳动作启动的延时时间,单位(毫秒), 默认10000
	 * @param workerPoolThreadsParm 处理服务器端主动发送请求的工作线程池大小, 默认10
	 */
	public SocketClient(String serverIpAddrParm, int portParm,
			int reconnectionDelayTimeParm, int readCheckTimeParm,
			int initialDelayTimeParm, int workerPoolThreadsParm,
			AbstractClientHandler theAbstractClientHandlerParm)
	{
		AutoReconnectDataSocket tmpSocketClient =
				new AutoReconnectDataSocket(serverIpAddrParm,
						portParm, reconnectionDelayTimeParm);
		
		WorkerHandlerThreadPool tmpWorkerHandlerThreadPool = null;
		if (workerPoolThreadsParm != WorkerPool_Threads)
		{
			tmpWorkerHandlerThreadPool =
				new WorkerHandlerThreadPool(workerPoolThreadsParm);
		}
		
		theClientTransportConnection = new ClientTransportConnection(tmpSocketClient,
						InactiveConnectionMonitor.Only_Write_InactiveMonitor,
						readCheckTimeParm,
						initialDelayTimeParm,
						tmpWorkerHandlerThreadPool,
						theAbstractClientHandlerParm);
		
		Thread threadMe = new Thread(tmpSocketClient);
		threadMe.start();
	}
	
	public byte[] sendMsg(byte[] requstParm, int timeOutParm) throws IOException
	{
		byte[] retBytes = null;
		Message tmpRequestMsg = new Message(MsgMarshallerFactory.Request_MsgType, requstParm, true);
		Message tmpResponseMsg = (Message)theClientTransportConnection
			.sendMsg(tmpRequestMsg, timeOutParm);
		if (tmpResponseMsg != null)
		{
			retBytes = tmpResponseMsg.getContent();
		}
		
		return retBytes;
	}
	
	public void sendMsg(byte[] requstParm) throws IOException
	{
		Message tmpRequestMsg = new Message(MsgMarshallerFactory.Request_MsgType, requstParm, false);
		theClientTransportConnection.sendMsg(tmpRequestMsg);
	}
	
	class ClientTransportConnection extends TransportConnection
	{
		private AbstractClientHandler theAbstractClientHandler;
		
		public ClientTransportConnection(Transport transportParm,
				int inactiveMonitorTypeParm,
				int readCheckTimeParm,
				int initialDelayTimeParm,
				WorkerHandlerThreadPool workerHandlerThreadPoolParm,
				AbstractClientHandler theAbstractClientHandlerParm)
		{
			super(transportParm, inactiveMonitorTypeParm, readCheckTimeParm,
					initialDelayTimeParm, workerHandlerThreadPoolParm);
			theAbstractClientHandler = theAbstractClientHandlerParm;
		}
		
		public Message doMsgHandler(Message reqMsgParm)
		{
			Message response = null;
			if (theAbstractClientHandler != null)
			{
				response = theAbstractClientHandler.doMsgHandler(reqMsgParm);
			}
			else
			{
				try
				{
					response = new Message(MsgMarshallerFactory.BusinExceptionResponse_MsgType,
							new String("没有设置相应的消息处理类").getBytes("UTF-8"), false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			return response;
		}
	}
}
