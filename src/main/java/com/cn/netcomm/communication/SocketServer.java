package com.cn.netcomm.communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;
import com.cn.netcomm.communication.transport.InactiveConnectionMonitor;
import com.cn.netcomm.communication.transport.Transport;
import com.cn.netcomm.communication.transport.TransportFilter;
import com.cn.netcomm.communication.transport.TransportListener;
import com.cn.netcomm.communication.util.AbortPolicyWithReport;
import com.cn.netcomm.communication.util.Utilities;


/**
 * SocketServer类
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-1-30
 */
public class SocketServer extends Thread
{
	private static Logger logger =
		Logger.getLogger(SocketServer.class.getName());
	// 执行业务处理的工作线程池大小, 默认100
	private final static int Default_WorkerPool_Threads = 100;
	// 心跳的轮询时间,单位(毫秒), 默认30000
	private final static int Default_ReadCheckTime = 30000;
	// 心跳动作启动的延时时间,单位(毫秒), 默认10000
	private final static int Default_InitialDelayTime = 10000;
	public static final int Socket_Close = 1;
	private int port = 10000;
	private ServerSocket theServerSocket;
	private WorkerHandler theServerMsgHanlderClass;
	private int workerPoolThreads = Default_WorkerPool_Threads;
	private int readCheckTime = Default_ReadCheckTime;
	private int initialDelayTime = Default_InitialDelayTime;
	private WorkerHandlerThreadPool theWorkerHandlerThreadPool;
	
	public SocketServer(int portParm, WorkerHandler theServerMsgHanlderParm)
	{
		this(portParm, theServerMsgHanlderParm, Default_WorkerPool_Threads,
				Default_ReadCheckTime, Default_InitialDelayTime);
	}
	
	/**
	 * 
	 * @param portParm socket端口号
	 * @param theServerMsgHanlderParm 业务处理类
	 * @param workerPoolThreadsParm 执行业务处理的工作线程池大小, 默认100
	 * @param readCheckTimeParm 心跳的轮询时间,单位(毫秒), 默认30000
	 * @param initialDelayTimeParm 心跳动作启动的延时时间,单位(毫秒), 默认10000
	 */
	public SocketServer(int portParm, WorkerHandler theServerMsgHanlderParm,
			int workerPoolThreadsParm, int readCheckTimeParm, int initialDelayTimeParm)
	{
		port = portParm;
		theServerMsgHanlderClass = theServerMsgHanlderParm;
		workerPoolThreads = workerPoolThreadsParm;
		readCheckTime = readCheckTimeParm;
		initialDelayTime = initialDelayTimeParm;
		theWorkerHandlerThreadPool = new WorkerHandlerThreadPool(workerPoolThreadsParm);
	}
	
	public void run()
	{
		try
		{
			theServerSocket = new ServerSocket(port);
			System.out.println("启动侦听服务成功:端口 "+port);
			while (true)
			{
				Socket socket = theServerSocket.accept();
				System.out.println("接受一个新连接 " + socket);
				try
				{
					new SocketHandler(socket, readCheckTime, initialDelayTime,
							theWorkerHandlerThreadPool, theServerMsgHanlderClass);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("服务器接受连接请求发生异常。请重启系统");
		}
	}
}

class MsgTranspConnection extends TransportConnection
{
	private WorkerHandler theWorkerHanlder = null;
	
	public MsgTranspConnection(Transport transportParm, int inactiveMonitorTypeParm,
			int readCheckTimeParm, int initialDelayTimeParm,
			WorkerHandlerThreadPool theWorkerHandlerThreadPoolParm,
			WorkerHandler theServerMsgHanlderParm)
	{
		super(transportParm, inactiveMonitorTypeParm,
				readCheckTimeParm, initialDelayTimeParm,
				theWorkerHandlerThreadPoolParm);
		theWorkerHanlder = theServerMsgHanlderParm;
	}
	
	/**
	 * 重载doMsgHandler方法,以执行真正的业务功能
	 */
	public Message doMsgHandler(Message reqMsgParm)
	{
		synchronized(theWorkerHanlder)
		{
			return theWorkerHanlder.doMsgHandler(reqMsgParm);
		}
	}
	
	public void transportOnException(Exception exception)
	{
		theWorkerHanlder.transportOnException(exception);
	}
}

class SocketHandler implements Runnable, Transport
{
	private static Logger logger =
			Logger.getLogger(SocketHandler.class.getName());
	private Socket openedSocket = null;
	private TransportListener transportListener;
	private MsgTranspConnection theCustTransportConnection;

	public SocketHandler(Socket openedSocketParm,
			int readCheckTimeParm, int initialDelayTimeParm,
			WorkerHandlerThreadPool theWorkerHandlerThreadPool,
			WorkerHandler theServerWorkerHanlderClassParm) throws Exception
	{
		theCustTransportConnection =
				new MsgTranspConnection(this,
						InactiveConnectionMonitor.Only_Read_InactiveMonitor,
						readCheckTimeParm, initialDelayTimeParm,
						theWorkerHandlerThreadPool,
						theServerWorkerHanlderClassParm);
		
		openedSocket = openedSocketParm;
		Thread theThread = new Thread(this);
		theThread.start();
	}
	
	public void run()
	{
		try
		{
			InputStream inputStream = openedSocket.getInputStream();
			while (true)
			{
				Message tmpOneReqMsg = Utilities.getInstance().readMsg(inputStream);
				transportListener.onCommand(tmpOneReqMsg);
			}
		}
		catch (Exception ioe)
		{
			ioe.printStackTrace();
			System.out.println(openedSocket + "连接关闭");
			logger.error(openedSocket + "连接关闭", ioe);
			onException(ioe);
		}
	}
	
	private void onException(Exception ex)
	{
		transportListener.onException(ex);
		transportListener = null;
		openedSocket = null;
		theCustTransportConnection = null;
	}

	public String getRemoteAddress()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void oneway(Message sendMsgParm) throws IOException
	{
		Utilities.getInstance().writeMsgThroughTcp(
				sendMsgParm, openedSocket.getOutputStream());
	}
	
	public TransportListener getTransportListener()
	{
		return transportListener;
	}
	
	public void setTransportListener(TransportListener commandListener)
	{
		transportListener = commandListener;
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}
}

class WorkerHandlerThreadPool
{
	private static Logger logger =
		Logger.getLogger(WorkerHandlerThreadPool.class.getName());
	private ExecutorService executor; // 工作线程池
	
	public WorkerHandlerThreadPool(int workerPoolThreadsParm)
	{
		executor = new ThreadPoolExecutor(
			workerPoolThreadsParm, workerPoolThreadsParm, 100, TimeUnit.MILLISECONDS, 
        	new SynchronousQueue<Runnable>(),
        	Executors.defaultThreadFactory(),
        	new AbortPolicyWithReport("mars communication thread"));
	}

	public void doHandle(Message msgParm,
			TransportConnection theTransportConnectionParm,
			TransportFilter theTransportParm)
	{
		RunnableTrld tmpRunnableTrld = new RunnableTrld(msgParm,
				theTransportConnectionParm, theTransportParm);
		executor.execute(tmpRunnableTrld);
	}
	
	class RunnableTrld implements Runnable
	{
		private TransportConnection theTransportConnection;
		private TransportFilter theTransport;
		private Message msgParm;
		
		protected RunnableTrld(Message theMsgParm,
				TransportConnection theTransportConnectionParm,
				TransportFilter theTransportParm)
		{
			theTransportConnection = theTransportConnectionParm;
			theTransport = theTransportParm;
			msgParm = theMsgParm;
		}
		
		public void run()
		{
			Message response = null;
			boolean responseRequired = msgParm.isResponseRequired();
	        try
	        {
	        	// 进行业务处理,获得处理结果
	            response = theTransportConnection.doMsgHandler(msgParm);
	        }
	        catch (Throwable e)
	        {
	        	e.printStackTrace();
	        	logger.error("在进行业务处理的时候发生异常!!!", e);
	            if (responseRequired)
	            {
	            	// 如果业务处理失败,则响应类型设置为BusinExceptionResponse_MsgType
	                response = new Message(MsgMarshallerFactory.BusinExceptionResponse_MsgType,
	                	e.toString().getBytes(), false);
	            }
	        }
	        
	        if (responseRequired)
	        {
	            if (response == null)
	            {
	            	logger.error("请求端需要有响应信息,实际却没有生成响应!!!");
	                response = new Message(MsgMarshallerFactory.BusinExceptionResponse_MsgType,
	                		"".getBytes(), false);
	            }
	            
	            // 设置响应消息的请求id
	            response.setMsgId(msgParm.getMsgId());
	            try
	            {
	            	// 发生响应给请求端
	            	theTransport.oneway(response);
	            }
	            catch(Exception e)
	            {
	            	e.printStackTrace();
	            	logger.error("发生响应给请求端,发生异常!!!", e);
	            }
	        }
		}
	}
}
