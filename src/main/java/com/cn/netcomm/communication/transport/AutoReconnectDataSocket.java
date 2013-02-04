package com.cn.netcomm.communication.transport;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import org.apache.log4j.Logger;

import com.cn.netcomm.communication.exception.MsgHeadException;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.util.Utilities;


/**
 * 带自动重连功能的socket
 * 
 * @author netcomm(baiwenzhi@360buy.com)
 * @date 2013-2-3
 */
public class AutoReconnectDataSocket implements Runnable, Transport
{
	private static Logger logger =
		Logger.getLogger(AutoReconnectDataSocket.class.getName());
	private Socket theSocket;
	private String ipAddr = "";
	private int port = 0;
	// 默认等待15秒进行重连
	private int reconnectionDelayTime = 15 * 1000;
	private TransportListener transportListener;
	private Object reConnectWaitObj = null;
	private boolean stop = false;

	public AutoReconnectDataSocket(String ipAddrParm, int portParm,
									int reconnectionDelayTimeParm)
	{
		ipAddr = ipAddrParm;
		port = portParm;
		reconnectionDelayTime = reconnectionDelayTimeParm;
	}
	
	private void initSocket()
	{
		InetAddress addr = null;
		SocketAddress sockaddr = null;
		while (true)
		{
			if (stop == true)
			{
				break;
			}
			try
			{
				theSocket = new Socket();
				addr = InetAddress.getByName(ipAddr);
				sockaddr = new InetSocketAddress(addr, port);
				theSocket.connect(sockaddr, 3000);
				theSocket.setSendBufferSize(1024 * 32);
				theSocket.setTcpNoDelay(true);
				logger.info("成功建立链接: " + ipAddr+ ":"+ port);
				break;
			}
			catch (IOException ioE)
			{
				ioE.printStackTrace();
				logger.warn((reconnectionDelayTime/1000) + " 秒进行重试链接 "
						+" "+ ipAddr+ ":"+ port);
				try
				{
					Thread.sleep(reconnectionDelayTime);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void doSocketRead()
	{
		while(true)
		{
			if (stop == true)
			{
				logger.info("停止AutoReconnectDataSocket");
				break;
			}
			
			try
			{
				InputStream inputStream = theSocket.getInputStream();
				while (true)
				{
					Message tmpOneReqMsg = Utilities.getInstance().readMsg(inputStream);
					if (tmpOneReqMsg != null)
					{
						transportListener.onCommand(tmpOneReqMsg);
					}
					else
					{
						logger.error("连接已经关闭");
						throw new Exception("连接已经关闭");
					}
				}
			}
			catch (MsgHeadException mHEx)
			{
				try
				{
					/*
					 * 由于消息头验证失败，这种情况目前只会因为magic出错，
					 * 这将导致后续消息流处理会出现未知异常，所以保险期间强制关闭该链接，
					 * 这种情况出现的可能性极少。
					 */
					stop();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				logger.error(theSocket + " 读取发生异常", e);
				onException(e);
				while(true)
				{
					try
					{
						if (stop == true)
						{
							logger.info("停止AutoReconnectDataSocket");
							break;
						}
						
						Thread.sleep(100);
						if (reConnectWaitObj != null)
						{
							break;
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}
	
	public void run()
	{
		firstInitConnect();
	}
	
	private void firstInitConnect()
	{
		initSocket();
		// 初次连接建立好,调用相应的观察者
		TransportFirstConnectNotify tmpThrd = new TransportFirstConnectNotify();
		tmpThrd.start();
		doSocketRead();
	}
	
	class TransportFirstConnectNotify extends Thread
	{
		protected TransportFirstConnectNotify()
		{
		}
		
		public void run()
		{
			try
			{
				Thread.sleep(1000);
				getTransportListener().transportFirstConnect();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private void onException(Exception ex)
	{
		// 异常发生时做相应的清除工作
		if (stop == false)
		{
			transportListener.onException(ex);
			reConnect();
		}
	}
	
	private void reConnect()
	{
		reConnectWaitObj = null;
		initSocket();
		if (stop == false)
		{
			TransportResumedNotifyThrd tmpThrd = new TransportResumedNotifyThrd();
			tmpThrd.start();
			//getTransportListener().transportResumed();
			reConnectWaitObj = new Object();
		}
	}
	
	class TransportResumedNotifyThrd extends Thread
	{
		protected TransportResumedNotifyThrd()
		{
		}
		
		public void run()
		{
			try
			{
				Thread.sleep(1000);
				getTransportListener().transportResumed();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public String getRemoteAddress()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void oneway(Message sendMsgParm) throws IOException
	{
		Utilities.getInstance().writeMsgThroughTcp(sendMsgParm,
				theSocket.getOutputStream());
	}

	public TransportListener getTransportListener()
	{
		return transportListener;
	}
	
	public void setTransportListener(TransportListener transportListenerParm)
	{
		transportListener = transportListenerParm;
	}
	
	public void stop() throws Exception
	{
		try
		{
			stop = true;
			if (transportListener != null)
			{
				transportListener.onException(new IOException("关闭连接"));
				transportListener = null;
			}
			if (theSocket != null)
			{
				theSocket.close();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}