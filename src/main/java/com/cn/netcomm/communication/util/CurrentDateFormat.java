/*
 * @(#)CurrentDateOp.java
 * 1.5 2007/10/27
 *
 * Copyright (c) 2001-2008  adtec
 * All rights reserved.
 */

package com.cn.netcomm.communication.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


/**
 * 返回需要的日前格式类
 * 
 * @version 1.5 27 Oct. 2007 10:45
 * @author noms研发组
 */
public class CurrentDateFormat
{
    private static CurrentDateFormat service = new CurrentDateFormat();
    
    private String fullTime = "";
    private String curTime8 = "";
    private int curTimeHour = -1;
    private int curTimeMinute = 0;
    private static String curDay = "";    
    private boolean isChangeDayOpSign = false;
    private char[] hourAndMintueAndSecChars = new char[3];
    private boolean isFirstDida = true;
    private Vector exchangeDayObservers = new Vector(1);
    private Vector exchangeHourObservers = new Vector(1);
    private byte[] curDayBytes = null;
    public final static char[][] HMS_CHARS = {
        {'0','0'}, {'0','1'}, {'0','2'}, {'0','3'}, {'0','4'}, {'0','5'},
        {'0','6'}, {'0','7'}, {'0','8'}, {'0','9'}, {'1','0'}, {'1','1'},
        {'1','2'}, {'1','3'}, {'1','4'}, {'1','5'}, {'1','6'}, {'1','7'},
        {'1','8'}, {'1','9'}, {'2','0'}, {'2','1'}, {'2','2'}, {'2','3'},
        {'2','4'}, {'2','5'}, {'2','6'}, {'2','7'}, {'2','8'}, {'2','9'},
        {'3','0'}, {'3','1'}, {'3','2'}, {'3','3'}, {'3','4'}, {'3','5'},
        {'3','6'}, {'3','7'}, {'3','8'}, {'3','9'}, {'4','0'}, {'4','1'},
        {'4','2'}, {'4','3'}, {'4','4'}, {'4','5'}, {'4','6'}, {'4','7'},
        {'4','8'}, {'4','9'}, {'5','0'}, {'5','1'}, {'5','2'}, {'5','3'},
        {'5','4'}, {'5','5'}, {'5','6'}, {'5','7'}, {'5','8'}, {'5','9'}
        };
    
    private CurrentDateFormat()
    {
    }
    
    public void addOneExgDayObserver(TimeExchangeObserver exgObserverParm)
    {
        exchangeDayObservers.add(exgObserverParm);
    }
    
    public void addOneExgHourObserver(TimeExchangeObserver exgObserverParm)
    {
    	exchangeHourObservers.add(exgObserverParm);
    }
    
    public static CurrentDateFormat getInstance()
    {
        return service;
    }
    
    public void startDateOp(boolean isChangeDayOpSignParm)
    {
        isChangeDayOpSign = isChangeDayOpSignParm;
        DateOpThread tmpDOT = new DateOpThread(this);
        tmpDOT.start();
    }

    public String getCurDay()
    {
        return curDay;
    }

    public void setCurDay(String curDayParm)
    {
        // 换日操作
        if (isFirstDida == false && (!curDayParm.equals(curDay)))
        {
            curDay = curDayParm;
            curDayBytes = curDay.getBytes();
            if (isChangeDayOpSign == true)
            {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!! 调用换日操作"+curDay);
                TimeExchangeObserver tmpExgDayObserverObj = null;
                for (int i = 0; i < exchangeDayObservers.size(); i++)
                {
                    tmpExgDayObserverObj =
                        (TimeExchangeObserver)exchangeDayObservers.elementAt(i);
                    tmpExgDayObserverObj.exchangeTime(curDay);
                }
            }
        }
        else
        {
            isFirstDida = false;
            curDay = curDayParm;
            if (curDayBytes == null)
            {
                curDayBytes = curDay.getBytes();
            }
        }
    }

    public String getCurTime8()
    {
        return curTime8;
    }

    private void setCurTime8(String curTime8)
    {
        this.curTime8 = curTime8;
    }

    public String getFullTime()
    {
        return fullTime;
    }

    private void setFullTime(String fullTime)
    {
        this.fullTime = fullTime;
    }

    public int getCurTimeHour()
    {
        return curTimeHour;
    }

    private void setCurTimeHour(int curTimeHourParm)
    {
    	boolean tmpIsExg = false;
    	if (curTimeHourParm != curTimeHour)
    	{
    		tmpIsExg = true;
    	}
        curTimeHour = curTimeHourParm;
        if (isChangeDayOpSign == true && tmpIsExg == true)
        {
            TimeExchangeObserver tmpExgHourObserverObj = null;
            for (int i = 0; i < exchangeHourObservers.size(); i++)
            {
            	tmpExgHourObserverObj =
                    (TimeExchangeObserver)exchangeHourObservers.elementAt(i);
            	tmpExgHourObserverObj.exchangeTime(Integer.toString(curTimeHour));
            }
        }
    }

    public synchronized Object[] getDayAndHourAndMintueAndSecObjs()
    {
    	Object[] retObjs = new Object[2];
    	retObjs[0] = curDayBytes;
    	retObjs[1] = hourAndMintueAndSecChars;
        return retObjs;
    }

    public void setHourAndMintueAndSecChars(char[] hourAndMintueAndSecChars)
    {
        this.hourAndMintueAndSecChars = hourAndMintueAndSecChars;
    }
    
    public static char[] getHourAndMinuteAndSecondChars(String formatTime)
    {
        char[] retChars = new char[3];
        char[] tmpChars = new char[2];
        
        formatTime.getChars(0, 2, tmpChars, 0);
        retChars[0] = (char)((tmpChars[0] - 48)*10 + (tmpChars[1] - 48));
        
        formatTime.getChars(3, 5, tmpChars, 0);
        retChars[1] = (char)((tmpChars[0]-48)*10 + (tmpChars[1]-48));
        
        formatTime.getChars(6, 8, tmpChars, 0);
        retChars[2] = (char)((tmpChars[0]-48)*10 + (tmpChars[1]-48));
        
        return retChars;
    }
    
    public static String getFullTimeFormatStr(
        String dayStrParm, byte[] time3BytesParm)
    {
        StringBuffer tmpStrBuf = new StringBuffer(dayStrParm);
        tmpStrBuf.append(' ');
        tmpStrBuf.append(HMS_CHARS[time3BytesParm[0]]);
        tmpStrBuf.append(':');
        tmpStrBuf.append(HMS_CHARS[time3BytesParm[1]]);
        tmpStrBuf.append(':');
        tmpStrBuf.append(HMS_CHARS[time3BytesParm[2]]);
        
        return tmpStrBuf.toString();
    }

    public byte[] getCurDayBytes()
    {
        return curDayBytes;
    }

    public int getCurTimeMinute()
    {
        return curTimeMinute;
    }

    public void setCurTimeMinute(int curTimeMinute)
    {
        this.curTimeMinute = curTimeMinute;
    }
    
    public synchronized void setCurTime(String fullTimeParm,
    		String theCurTime8StrParm, String curDayParm,
    		String[] theHourAndMinuteTimeStrs,
    		char[] tmpHourAndMinuteAndSecondCharsParm)
    {
    	setCurDay(curDayParm);
    	setFullTime(fullTimeParm);
    	setCurTime8(theCurTime8StrParm);
        setCurTimeHour(Integer.parseInt(theHourAndMinuteTimeStrs[0]));
        setCurTimeMinute(Integer.parseInt(theHourAndMinuteTimeStrs[1]));
        setHourAndMintueAndSecChars(tmpHourAndMinuteAndSecondCharsParm);
    }
}

// 每隔500ms更新一下当前各种日期字符串
class DateOpThread extends Thread
{
    private CurrentDateFormat dateFormatter;
    private SimpleDateFormat curTodayDateformatter
                                = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat curTimeformatter =
                new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat fullDateFormatter =
                new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private static Date             fullDate = new Date();
    
    public DateOpThread(CurrentDateFormat curFormatter)
    {
        dateFormatter = curFormatter;
        handleTimeOp();
    }
    
    public void run()
    {
        try
        {
            while(true)
            {
                handleTimeOp();
                Thread.sleep(500);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void handleTimeOp()
    {
        fullDate.setTime(System.currentTimeMillis());
        String tmpCurTime8Str = curTimeformatter.format(fullDate);
        char[] tmpHourAndMinuteAndSecondChars = CurrentDateFormat
        				.getHourAndMinuteAndSecondChars(tmpCurTime8Str);
        String[] tmpTimeStrs = getHourAndMinuteAndSecondStr(tmpCurTime8Str);
        dateFormatter.setCurTime(fullDateFormatter.format(fullDate),
        		tmpCurTime8Str, curTodayDateformatter.format(fullDate),
        		tmpTimeStrs, tmpHourAndMinuteAndSecondChars);
    }
    
    private String[] getHourAndMinuteAndSecondStr(String formatTime)
    {
        String[] retStrs = new String[3];
        retStrs[0] = formatTime.substring(0,2);
        retStrs[1] = formatTime.substring(3,5);
        retStrs[2] = formatTime.substring(6);
        return retStrs;
    }
    
}
