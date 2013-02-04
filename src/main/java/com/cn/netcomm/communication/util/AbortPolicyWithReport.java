package com.cn.netcomm.communication.util;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.log4j.Logger;

public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {
	private static Logger logger =
			Logger.getLogger(AbortPolicyWithReport.class.getName());
    private final String threadName;
    
    public AbortPolicyWithReport(String threadName) {
        this.threadName = threadName;
    }
    
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED!" +
                " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d)" ,
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize());
        logger.warn(msg);
        throw new RejectedExecutionException(msg);
    }

}