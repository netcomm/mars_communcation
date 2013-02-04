package com.cn.netcomm.communication.transport;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cn.netcomm.communication.exception.InactivityIOException;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;
import com.cn.netcomm.communication.util.SchedulerTimerTask;


/**
 * Used to make sure that commands are arriving periodically from the peer of
 * the transport.
 * 心跳功能：
 * 心跳分为'读'和'写'两种类型
 * @version $Revision$
 */
public class InactiveConnectionMonitor extends TransportFilter
{
	private static final ThreadPoolExecutor ASYNC_TASKS;

	private static int CHECKER_COUNTER;
	private static Timer READ_CHECK_TIMER;
	private static Timer WRITE_CHECK_TIMER;
	
	private final AtomicBoolean monitorStarted = new AtomicBoolean(false);

	private final AtomicBoolean commandSent = new AtomicBoolean(false);
	private final AtomicBoolean inSend = new AtomicBoolean(false);
	private final AtomicBoolean failed = new AtomicBoolean(false);

	private final AtomicBoolean commandReceived = new AtomicBoolean(true);
	private final AtomicBoolean inReceive = new AtomicBoolean(false);
	private SchedulerTimerTask writeCheckerTask;
	private SchedulerTimerTask readCheckerTask;
	// 读类型的轮询时间
	private long readCheckTime = 30000;
	// 写类型的轮询时间是读类型的 1/3
	private long writeCheckTime;
	private long initialDelayTime = 10000;
	
	private int startType = Only_Read_InactiveMonitor;
	public static final int Only_Read_InactiveMonitor = 0;
	public static final int Only_Write_InactiveMonitor = 1;
	public static final int ReadWrite_InactiveMonitor = 2;
	
	public InactiveConnectionMonitor(Transport nextParm,
			int inactiveMonitorTypeParm,
			int readCheckTimeParm, int initialDelayTimeParm)
	{
		super(nextParm);
		startType = inactiveMonitorTypeParm;
		readCheckTime = readCheckTimeParm;
		initialDelayTime = initialDelayTimeParm;
	}

	private final Runnable readChecker = new Runnable()
	{
		long lastRunTime;

		public void run()
		{
			long now = System.currentTimeMillis();
			long elapsed = (now - lastRunTime);

			// Perhaps the timer executed a read check late.. and then executes
			// the next read check on time which causes the time elapsed between
			// read checks to be small..

			// If less than 90% of the read check Time elapsed then abort this
			// readcheck.
			if (!allowReadCheck(elapsed))
			{ // FUNKY qdox bug does not allow me to inline this expression.
				System.out
						.println("Aborting read check.. Not enough time elapsed since last read check.");
				return;
			}

			lastRunTime = now;
			readCheck();
		}
	};

	private boolean allowReadCheck(long elapsed)
	{
		return elapsed > (readCheckTime * 9 / 10);
	}

	private final Runnable writeChecker = new Runnable()
	{
		long lastRunTime;

		public void run()
		{
			long now = System.currentTimeMillis();
			lastRunTime = now;
			writeCheck();
		}
	};

	final void writeCheck()
	{
		if (inSend.get())
		{
			return;
		}

		if (!commandSent.get())
		{
			//System.out.println("No message sent since last write check, sending a KeepAliveInfo");
			ASYNC_TASKS.execute(new Runnable()
			{
				public void run()
				{
					if (monitorStarted.get())
					{
						try
						{
							if (startType == Only_Write_InactiveMonitor)
							{
								oneway(new Message(MsgMarshallerFactory.KeepAlive_MsgType,
										new byte[0], false));
							}
							if (startType == ReadWrite_InactiveMonitor)
							{
								oneway(new Message(MsgMarshallerFactory.KeepAlive_MsgType,
										new byte[0], true));
							}
						}
						catch (IOException e)
						{
							onException(e);
						}
					}
				};
			});
		}

		commandSent.set(false);
	}

	final void readCheck()
	{
		if (inReceive.get())
		{
			return;
		}
		if (!commandReceived.get())
		{
			System.out.println
				("No message received since last read check for!"
						+"Throwing InactivityIOException.");
			ASYNC_TASKS.execute(new Runnable()
			{
				public void run()
				{
					onException(new InactivityIOException(
							"Channel was inactive for too long: "
									+ getNext()));
				};
			});
		}
		commandReceived.set(false);
	}

	public void onCommand(Message sendMsgParm)
	{
		commandReceived.set(true);
		inReceive.set(true);
		
		try
		{
			if (sendMsgParm.getMsgType() == MsgMarshallerFactory.KeepAlive_MsgType)
			{
				if (sendMsgParm.isResponseRequired() == true)
				{
					try
					{
		                oneway(new Message(MsgMarshallerFactory.KeepAlive_MsgType,
		                		new byte[0], false));
		            }
					catch (IOException e)
					{
		                onException(e);
		            }
				}
			}
			else
			{
				transportListener.onCommand(sendMsgParm);
			}
		}
		finally
		{
            inReceive.set(false);
        }
	}
	
	public void oneway(Message sendMsgParm) throws IOException
	{
		// Disable inactivity monitoring while processing a command.
		// synchronize this method - its not synchronized
		// further down the transport stack and gets called by more
		// than one thread by this class
		synchronized (inSend)
		{
			inSend.set(true);
			try
			{
				if (failed.get())
				{
					throw new InactivityIOException(
							"Channel was inactive for too long: "
									+ next);
				}
				next.oneway(sendMsgParm);
			}
			finally
			{
				commandSent.set(true);
				inSend.set(false);
			}
		}
	}

	public void transportResumed()
	{
		startMonitorThreads();
		super.transportResumed();
	}

	public void transportFirstConnect()
	{
		startMonitorThreads();
		super.transportFirstConnect();
	}
	
	public void onException(Exception ex)
	{
		if (failed.compareAndSet(false, true))
		{
			System.out.println("connection has been disconnected.");
			stopMonitorThreads();
			transportListener.onException(ex);
		}
	}
	
	/**
	 * @param startTypeParm 监控类型: 0 = 单向读; 1 = 单向写; 2 = 双向读写
	 * @param readCheckTimeParm
	 * @param initialDelayTimeParm
	 * @throws IOException
	 */
	private synchronized void startMonitorThreads()
	{
		if (monitorStarted.get())
		{
			return;
		}
		
		if (readCheckTime > 0)
		{
			monitorStarted.set(true);
			failed.set(false);
			if (startType == Only_Read_InactiveMonitor ||
					startType == ReadWrite_InactiveMonitor)
			{
				readCheckerTask = new SchedulerTimerTask(readChecker);
			}
			if (startType == Only_Write_InactiveMonitor ||
					startType == ReadWrite_InactiveMonitor)
			{
				writeCheckerTask = new SchedulerTimerTask(writeChecker);			
				writeCheckTime = readCheckTime / 3;
			}
			synchronized (InactiveConnectionMonitor.class)
			{
				if (CHECKER_COUNTER == 0)
				{
					READ_CHECK_TIMER = new Timer("InactivityMonitor ReadCheck",
							true);
					WRITE_CHECK_TIMER = new Timer(
							"InactivityMonitor WriteCheck", true);
				}
				CHECKER_COUNTER++;
				if (startType == Only_Read_InactiveMonitor
							|| startType == ReadWrite_InactiveMonitor)
				{
					READ_CHECK_TIMER.scheduleAtFixedRate(readCheckerTask,
							initialDelayTime, readCheckTime);
				}
				if (startType == Only_Write_InactiveMonitor
							|| startType == ReadWrite_InactiveMonitor)
				{
					WRITE_CHECK_TIMER.scheduleAtFixedRate(writeCheckerTask,
							initialDelayTime, writeCheckTime);
				}
			}
			
			System.out.println("启动socket链路探测!!!");
		}
	}

	/**
	 * 
	 */
	private synchronized void stopMonitorThreads()
	{
		if (monitorStarted.compareAndSet(true, false))
		{
			if (readCheckerTask != null)
			{
				readCheckerTask.cancel();
			}
			if (writeCheckerTask != null)
			{
				writeCheckerTask.cancel();
			}
			synchronized (InactiveConnectionMonitor.class)
			{
				if (WRITE_CHECK_TIMER != null)
				{
					WRITE_CHECK_TIMER.purge();
				}
				if (READ_CHECK_TIMER != null)
				{
					READ_CHECK_TIMER.purge();
				}
				CHECKER_COUNTER--;
				if (CHECKER_COUNTER == 0)
				{
					if (WRITE_CHECK_TIMER != null)
					{
						WRITE_CHECK_TIMER.cancel();
					}
					if (READ_CHECK_TIMER != null)
					{
						READ_CHECK_TIMER.cancel();
					}
					WRITE_CHECK_TIMER = null;
					READ_CHECK_TIMER = null;
				}
			}
		}
	}

	static
	{
		ASYNC_TASKS = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
				new ThreadFactory()
				{
					public Thread newThread(Runnable runnable)
					{
						Thread thread = new Thread(runnable,
								"InactivityMonitor Async Task: " + runnable);
						thread.setDaemon(true);
						return thread;
					}
				});
	}
}
