/*
 * @(#)ExchangeDayObserver.java   
 * 1.5 2007/03/21
 *
 * Copyright (c) 2001-2008  adtec
 * All rights reserved.
 */

package com.cn.netcomm.communication.util;


/**
 * 时间变化观察者的接口定义类
 * @version 1.5 21 Mar. 2007 11:30
 * @author noms研发组
 */
public interface TimeExchangeObserver
{
    public void exchangeTime(String curTimeParm);
}
