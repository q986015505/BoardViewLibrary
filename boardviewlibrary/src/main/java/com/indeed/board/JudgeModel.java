package com.indeed.board;

/**
 * Created by yandequan on 17/1/18.
 */

public class JudgeModel {
    private int blackCount = 0;
    private int whiteCount = 0;
    private int whiteLiteCount = 0;
    private int blackLiteCount = 0;
    private int whiteTackCount = 0;
    private int blackTackCount = 0;

    public int getBlackCount() {
        return blackCount;
    }

    public void setBlackCount(int blackCount) {
        this.blackCount = blackCount;
    }

    public int getWhiteCount() {
        return whiteCount;
    }

    public void setWhiteCount(int whiteCount) {
        this.whiteCount = whiteCount;
    }

    public int getWhiteLiteCount() {
        return whiteLiteCount;
    }

    public void setWhiteLiteCount(int whiteLiteCount) {
        this.whiteLiteCount = whiteLiteCount;
    }

    public int getBlackLiteCount() {
        return blackLiteCount;
    }

    public void setBlackLiteCount(int blackLiteCount) {
        this.blackLiteCount = blackLiteCount;
    }

    public int getWhiteTackCount() {
        return whiteTackCount;
    }

    public void setWhiteTackCount(int whiteTackCount) {
        this.whiteTackCount = whiteTackCount;
    }

    public int getBlackTackCount() {
        return blackTackCount;
    }

    public void setBlackTackCount(int blackTackCount) {
        this.blackTackCount = blackTackCount;
    }

    public void setOffsetBlack(){
        blackCount++;
    }

    public void setOffsetWhite(){
        whiteCount++;
    }

    public void setOffsetBlackLiteCount(){
        blackLiteCount++;
    }

    public void setOffsetWhiteLiteCount(){
        whiteLiteCount++;
    }

    public void setOffsetBlackTackCount(){
        blackTackCount++;
    }

    public void setOffsetWhiteTackCount(){
        whiteTackCount++;
    }
}
