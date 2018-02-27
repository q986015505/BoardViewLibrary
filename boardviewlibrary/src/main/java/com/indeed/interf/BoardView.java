package com.indeed.interf;

/**
 * Created by yandequan on 17/3/13.
 */

public class BoardView {
    public interface OnBoardViewSingleInterface {
        public void onBoardViewSingle();
        public void onBoardNewGame();
        public void onNextOrBack(boolean isNext);
        public void onFineStone(boolean isFine);
        public void onClickBoard();
    }
}
