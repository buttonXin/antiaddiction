package com.oldhigh.antiaddiction.bean;

import android.graphics.Point;

public class EventClick {

    /**
     * 昵称
     */
    public String nickName;
    /**
     * 要点击的名字
     */
    public String clickName;
    /**
     * 点击的坐标 xy
     */
    public Point point;
    /**
     * 等待时间
     */
    public long delayTime;

    public EventClick() {
    }

    public EventClick(String clickName) {
        this.clickName = clickName;
    }

    public EventClick(String clickName, Point point) {
        this.clickName = clickName;
        this.point = point;
    }

    @Override
    public String toString() {
        return "EventClick{" +
                "nickName='" + nickName + '\'' +
                ", clickName='" + clickName + '\'' +
                ", point=" + point +
                ", delayTime=" + delayTime +
                '}';
    }
}
