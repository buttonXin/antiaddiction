package com.xreal.evapro.toolsapp.bean;

import ai.nreal.common.nrealdatabase.db.Column;
import ai.nreal.common.nrealdatabase.db.PrimaryKey;
import ai.nreal.common.nrealdatabase.db.Table;

@Table(tableName = "NoteBean")
public class NoteBean {

    /**
     * 本地数据库id，自增长
     */
    @PrimaryKey(column = "id")
    private int id;
    @Column(column = "content")
    public String content;
    /**
     * 0.重要紧急
     * 1.重要不紧急
     * 2.不紧急重要
     * 3.不紧急不重要
     */
    @Column(column = "type")
    public int type;
    /**
     * 置顶
     */
    @Column(column = "showTop")
    public int showTop;
    /**
     * 删除数据
     */
    @Column(column = "garbage")
    public boolean garbage;
    /**
     * 创建时间
     */
    @Column(column = "createTime")
    public long createTime;
    /**
     * 修改时间
     */
    @Column(column = "updateTime")
    public long updateTime;
    /**
     * 配置图片路径 uri等
     */
    @Column(column = "imagePath")
    public String imagePath;

}
