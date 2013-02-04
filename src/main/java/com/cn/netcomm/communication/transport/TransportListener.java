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

import com.cn.netcomm.communication.message.Message;


/**
 * 消息处理类
 *
 * @version $Revision$
 */
public interface TransportListener {
    
    /**
     * 执行具体的任务
     * @param command
     */
    void onCommand(Message command);
    
    /**
     * 在链路上发生了一个不可恢复的异常
     * @param error
     */
    void onException(Exception error);
    
    /**
     * 链路失效后重新恢复
     *
     */
    void transportResumed();
    
    /**
     * 链路第一次建立时会调用该方法
     */
    void transportFirstConnect();
}
