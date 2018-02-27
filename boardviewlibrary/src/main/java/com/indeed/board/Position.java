package com.indeed.board;

import com.indeed.board.util.Generics;

import java.util.List;
import java.util.Map;

/**
 * Created by yandequan on 16/12/22.
 */

public class Position {
    public int x = -1;
    public int y = -1;
    public int c = -1;
    public boolean isPass;
    //当前手数
    public int move = 0;
    //存储每一手的信息,如:落子时间 评论
    public Map<String,String> moveInfo = Generics.newHashMap();
    //父节点
    public Position parent;
    //分支
    public List<Position> childArray = Generics.newArrayList();
    //setup
    public List<Position> setup = Generics.newArrayList();
    //当前选中的分支
    public int selectRoot = 0;

    public int whiteDeadCount = 0;
    public int blackDeadCount = 0;

    public Integer[] curState;

    public Position(){};

    public Position(int x,int y){
        this.x = x;
        this.y = y;
    };

    public Position(int x,int y,int c){
        this.x = x;
        this.y = y;
        this.c = c;
    };

    public Position(int x,int y,int c,Integer[] curState){
        this.x = x;
        this.y = y;
        this.c = c;
        this.curState = curState;
    };

    public Position(Position position){
        this.x = position.x;
        this.y = position.y;
        this.c = position.c;
    }

    public Position(Position position, Integer[] curState, int move){
        this.x = position.x;
        this.y = position.y;
        this.c = position.c;
        this.move = move;
        this.curState = curState;
    }

    /**
     * Set the point's x and y coordinates
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }


    /**
     * Offset the point's coordinates by dx, dy
     */
    public final void offset(int dx, int dy) {
        x += dx;
        y += dy;
    }
}
