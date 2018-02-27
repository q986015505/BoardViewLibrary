package com.indeed.board;

import com.indeed.board.util.Generics;

import java.util.List;

/**
 * Created by yandequan on 16/12/22.
 */

 final class Globals {
    static GameInfo _gameInfo;
    static List<Position> _totalMoves = Generics.newArrayList();
    static BoardView _boardView;
}
