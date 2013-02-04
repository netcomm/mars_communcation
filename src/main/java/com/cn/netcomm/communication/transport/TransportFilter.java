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

import com.cn.netcomm.communication.message.Message;

/**
 * @version $Revision: 1.5 $
 */
public class TransportFilter implements TransportListener, Transport {
    protected Transport next;
    protected TransportListener transportListener;

    public TransportFilter(Transport next) {
        this.next = next;
    }

    public TransportListener getTransportListener() {
        return transportListener;
    }

    public void setTransportListener(TransportListener channelListener) {
        this.transportListener = channelListener;
        if (channelListener == null) {
            next.setTransportListener(null);
        } else {
            next.setTransportListener(this);
        }
    }
    
    public void onCommand(Message command) {
        transportListener.onCommand(command);
    }

    /**
     * @return Returns the next.
     */
    public Transport getNext() {
        return next;
    }
    
    public void oneway(Message command) throws IOException {
        next.oneway(command);
    }
    
    public void request(Message command) throws IOException
    {
    }

    public Message request(Message command, int timeout) throws IOException
    {
        return null;
    }

    public void onException(Exception error)
    {
    	if (transportListener != null)
    	{
    		transportListener.onException(error);
    	}
    }

	public void transportResumed()
	{
		if (transportListener != null)
    	{
    		transportListener.transportResumed();
    	}
	}

	public void transportFirstConnect()
	{
		if (transportListener != null)
    	{
    		transportListener.transportFirstConnect();
    	}
	}

	@Override
	public void stop() throws Exception {
		next.stop();
	}
}
