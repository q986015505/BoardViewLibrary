package com.indeed.board;

import com.indeed.board.util.Generics;

import java.util.Map;

/**
 * Created by yandequan on 16/12/22.
 */

public class GameInfo {
    public boolean _playerBlackMoves = true;
    //存储棋盘头信息
    public Map<String,String> headInfo = Generics.newHashMap();
}
