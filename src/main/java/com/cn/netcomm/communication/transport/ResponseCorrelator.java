/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cn.netcomm.communication.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

import com.cn.netcomm.communication.exception.ReqTimeOutException;
import com.cn.netcomm.communication.message.MsgMarshallerFactory;
import com.cn.netcomm.communication.message.Message;
import com.cn.netcomm.communication.util.FutureResponse;

/**
 * Adds the incrementing sequence number to commands along with performing the
 * corelation of responses to requests to create a blocking request-response
 * semantics.
 * 关联发出的请求消息和接收到的响应
 * 
 * @version $Revision: 1.4 $
 */
public class ResponseCorrelator extends TransportFilter
{
	private static Logger logger =
		Logger.getLogger(ResponseCorrelator.class.getName());
    private final Map<Long, FutureResponse> requestMap =
    	new HashMap<Long, FutureResponse>();

    public ResponseCorrelator(Transport nextParm)
    {
    	super(nextParm);
    }
    
    public FutureResponse asyncRequest(Message msgParm) throws IOException
    {
    	Message tmpMsg = (Message)msgParm;
    	tmpMsg.setResponseRequired(true);
        FutureResponse future = new FutureResponse();
        synchronized (requestMap)
        {
            requestMap.put(msgParm.getMsgId(), future);
        }
        getNext().oneway(msgParm);
        return future;
    }
    
    public Message request(Message msgParm, int timeout) throws IOException
    {
        FutureResponse response = asyncRequest(msgParm);
        Message retObj = response.getResult(timeout);
        if (retObj == null) // 超时返回
        {
        	synchronized (requestMap)
            {
        		requestMap.remove(msgParm.getMsgId());
            }
        	logger.error("请求超时 " + msgParm.getMsgId());
        	throw new ReqTimeOutException("comm timeout");
        }
        return retObj;
    }

    /**
     * 不需要响应的请求
     */
    public void request(Message msgParm) throws IOException
    {
    	getNext().oneway(msgParm);
    }
    
    /**
     * 处理消息
     */
    public void onCommand(Message msgParm)
    {
    	Message command = msgParm;
    	// 如果是消息类型是响应
        if (command.getMsgType() == MsgMarshallerFactory.Response_MsgType
        		|| command.getMsgType() == MsgMarshallerFactory.TransportExceptionResponse_MsgType
        		|| command.getMsgType() == MsgMarshallerFactory.BusinExceptionResponse_MsgType)
        {
            FutureResponse future = null;
            synchronized (requestMap)
            {
                future = requestMap.remove(command.getMsgId());
            }
            
            if (future != null)
            {
                future.set(command);
            }
        }
        else
        {
            getTransportListener().onCommand(command);
        }
    }
    
    public void onException(Exception error)
    {
    	logger.error("通讯发送异常清除requestMap里的内容,requestMap的大小 "
    			+requestMap.size());
        ArrayList<FutureResponse> requests=null; 
        synchronized(requestMap)
        {
        	requests = new ArrayList<FutureResponse>(requestMap.values());
            requestMap.clear();
        }
        if( requests!=null )
        {
            for (Iterator<FutureResponse> iter = requests.iterator(); iter.hasNext();)
            {
                FutureResponse fr = iter.next();
                fr.set(new Message(MsgMarshallerFactory.TransportExceptionResponse_MsgType,
                		new String("通讯异常").getBytes(), false));
            }
        }
        
        super.onException(error);
    }
}
