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
 * Represents the client side of a transport allowing messages to be sent
 * synchronously, asynchronously and consumed.
 * 
 * @version $Revision: 1.5 $
 */
public interface Transport
{
    /**
     * 通过链路发送一条消息
     * @param command
     * @throws IOException
     */
    void oneway(Message command) throws IOException;
    
    /**
     * Returns the current transport listener
     * 
     * @return
     */
    TransportListener getTransportListener();

    /**
     * Registers an inbound command listener
     * 
     * @param commandListener
     */
    void setTransportListener(TransportListener commandListener);
    
    void stop() throws Exception;
}
