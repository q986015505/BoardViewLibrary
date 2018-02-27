package com.indeed.board;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.shapes.PathShape;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.indeed.board.util.StringUtils;
import com.indeed.boardviewlibrary.R;
import com.indeed.board.util.GameUtil;
import com.indeed.board.util.Generics;
import com.indeed.interf.BoardView.OnBoardViewSingleInterface;
import com.indeed.model.DrawStoneModel;
import com.indeed.dialog.BottomMenuDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.indeed.board.Globals._boardView;
import static com.indeed.board.Globals._gameInfo;

public class BoardView
        extends SurfaceView
        implements SurfaceHolder.Callback {
    private static final
    int _ZOOM_BOARD_SIZE = 5; // todo preferences

    private Activity activity;

    //落子音效
    private SoundPool soundPool;
    private SoundPool deadMoreSound;

    private static final
    float _SHAPE_FACTOR = 1;

    private static final
    Paint _xferModePaintSrc = new Paint(),
            _xferModePaintAtop = new Paint(),
            _xferModePaintAtopAlpha,
            _crossCursporPaint,
            _numberPaint,
            _boardPaint;

    static {
        _xferModePaintSrc.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.SRC));
        _xferModePaintAtop.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        _xferModePaintAtopAlpha = new Paint(_xferModePaintAtop);
        _crossCursporPaint = new Paint(_xferModePaintSrc);
        final Paint numberPaint = _numberPaint = new Paint(_xferModePaintAtop);
        numberPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setStrokeWidth(8);
        _boardPaint = new Paint(_xferModePaintAtop);
    }

    private static final
    Bitmap.Config _bitmapConfig = Bitmap.Config.ARGB_8888;

    private final
    int _blackStoneColor, _whiteStoneColor;

    private final int _redoTextColor;

    private final int _stoneMarkesColor;

    private final
    SurfaceHolder _surfaceHolder;

    private final
    GestureDetector _gestureDetector;

    private final
    BoardGestureListener _gestureListener;

    boolean _isZoom = false;

    float _activeCellWidth, _activeCellHeight;

    private
    float
            _zoomFactor, // todo preferences
            _realCellWidth, _zoomCellWidth,
            _realCellHeight,
            _realCellSize,
            _cellWidthDIV2, _zoomCellWidthDIV2;

    private int _marginLeft, _marginRight,
            _marginTop, _marginBottom;

    int
            _boardSize = 19, _boardXsize = 19, _boardYsize = 19, _beginX = 0, _beginY = 0,
            _activeXBoardOffset, _activeYBoardOffset,
            _boardMarginXOffset, _boardMarginYOffset;


    private
    int
            _boardWidth, _boardHeight,
            _intCellWidth, _intCellWidthDIV2,
            _activeIntCellWidth, _activeIntCellWidthDIV2,
            _intZoomCellWidth, _intZoomCellWidthDIV2,
            _yBoardOffset, _xBoardOffset,
            _maxZoomBoardSizeOffset,
            _zoomRangeLow, _zoomRangeHigh,
            _crossCursorStrokeWidth;

    private
    Bitmap
            _boardBitmap,
            _blackStoneBitmap, _whiteStoneBitmap,
            _blackStoneJudgeBitmap, _whiteStoneJudgeBitmap,
            _blackStoneZoomBitmap, _whiteStoneZoomBitmap,
            _blackStoneMarkerBitmap, _whiteStoneMarkerBitmap,
            _blackStoneZoomMarkerBitmap, _whiteStoneZoomMarkerBitmap,
            _blackTerritoryBitmap, _whiteTerritoryBitmap,
            _lastMoveMarkerBitmap;


    private
    PathShape _gridShape, _markersShape;

    private
    float[] _textPosUpper, _textPosLower;

    final
    Set<Position> _legalMoves = Generics.newHashSet();

    private
    List<Position> _markers = Generics.newArrayList();

    Position _zoomViewPoint;

    //棋盘所有点
    public Integer[] pointState = null;

    // 判断各个点的状态
    public static final int EMPTY = 0;
    public static final int WHITE = -1;
    public static final int BLACK = 1;
    public static final int WHITE_LITE = -2;
    public static final int BLACK_LITE = 2;
    public static final int WHITE_VIRTUAL = -3;
    public static final int BLACK_VIRTUAL = 3;
    public static final int WHITE_HIDDED = -4;
    public static final int BLACK_HIDDED = 4;

    // 循环的禁着点的参数
    private int iForbit = -1;

    private boolean playerBlackMoves = true;


    //是否允许落子
    private boolean downFlag = true;

    // 当前步数，最大步数
    private int currentMove = 0, maxmove = 0;

    //存储所有棋子--分支test
    private Position totalMoveList;

    //提子数
    private int blackDeadCount = 0;
    private int whiteDeadCount = 0;

    //试下时临时属性
    private Position tmpTotalMoveLise;
    private int tmpCurrentMove = 0;
    private int tmpMaxMove = 0;
    private boolean isTryDown = false;
    //手数开始位置
    private int drawMarkIndex = 0;
    private boolean isAddBranch = false;
    private Position tmpCurMoveList;

    //当前棋子
    private Position curPosition;

    //是否落子音效
    private boolean isSound = true;

    //显示手数样式
    private MoveType moveType = MoveType.LB;

    //显示几手?
    private int moveCount = 1;

    //loadFlag
    private boolean loadFlag = false;

    //形势判断显示的盘面
    Integer[] showState = null;
    //形势判断显示的盘面
    Double[] newJudgeState = null;

    //只落一种颜色标记
    private boolean isSingleColor = false;

    //当前是否是形势判断
    int isJudge = 0;

    //打谱标记
    private boolean isDapu = false;

    //棋盘margin
    private int boardMargin = 0;

    //绘制坐标线程
    private Handler handler;

    //是否显示分支
    private boolean showBranch = true;

    //newgame是否callback标记
    private boolean newGameIsCallBack = true;

    //学习状态
    private boolean isLearn = false;

    //变化图状态
    private boolean isBranch = false;
    //棋盘样式
    private int boardStyle = 0;
    //猜棋状态
    private boolean isGuess = false;
    //坐标样式
    private int coordiNateStyle = 0;
    //落子方式 0虚影  1直接落  默认1
    private int stoneMoveType = 1;
    //最后操作子的坐标
    Position _lastPoint;
    //微调下是否落子
    private boolean moveingFlag = false;
    //屏幕常亮
    private boolean windowOn = false;
    //底部选择框控件
    BottomMenuDialog bottomMenuDialog;
    //等待框
    Dialog dialog;
    //是否按照预设显示手数样式
    private boolean handStyleFlag = false;
    //棋盘上绘制的点是否存在
    private boolean drawStoneList;

    protected OnBoardViewSingleInterface mOnEndOfGame; //callback interface

    private final class BoardGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        boolean _showHint = true;

        float _cellWidthDIV2, _cellWidthDIV3;

        float _lastX, _lastY;

        boolean _interactionLocked;

        void reset(
                final float pCellWidth
        ) {
            _cellWidthDIV2 = pCellWidth / 2;
            _cellWidthDIV3 = pCellWidth / 2 * 3; // todo: no effect ?
            _lastX = _lastY = -pCellWidth - 1;
            _lastPoint = null;
        }

        Position coord2Point(
                final float x,
                final float y
        ) {
            final float activeCellWidth = _activeCellWidth;
            final float activeCellHeight = _activeCellHeight;
            return new Position(((int) ((x + 1 - _boardMarginXOffset) / activeCellWidth) >= _boardSize - 1 ? _boardSize - 1 : (int) ((x + 1 - _boardMarginXOffset) / activeCellWidth)) + _beginX,
                    ((int) ((y + 1 - _boardMarginYOffset) / activeCellHeight) >= _boardSize - 1 ? _boardSize - 1 : (int) ((y + 1 - _boardMarginYOffset) / activeCellHeight)) + _beginY, (playerBlackMoves ? BLACK : WHITE));
        }


        @Override
        public boolean onScroll(MotionEvent pEvent1, MotionEvent pEvent2, float distanceX, float distanceY) {
            //禁止父控件滑动--如:scrollview之前的滑动冲突
            getParent().requestDisallowInterceptTouchEvent(true);
            final float x = pEvent2.getX() - _activeXBoardOffset,
                    y = pEvent2.getY() - _activeYBoardOffset;
            //判断棋盘锁
            if (_interactionLocked) {
                return true;
            }
            if (isSelect) {   //棋形搜索选择框
                final Position newPoint = coord2Point(x, y);
                //绘制
                drawSelectRect(newPoint);
            }
            return true;
        }

        public boolean onScroll1(
                final MotionEvent pEvent1,
                final MotionEvent pEvent2,
                final float pDistanceX,
                final float pDistanceY
        ) {
            if (waitFlag) return false;
            //虚手再次点击取消当前虚手
            if (_lastPoint != null) {
                drawBoard();
                downFlag = false;
                if (mOnEndOfGame != null) {
                    mOnEndOfGame.onFineStone(false);
                }
                return false;
            }
            //禁止父控件滑动--如:scrollview之前的滑动冲突
            getParent().requestDisallowInterceptTouchEvent(true);

            if (_interactionLocked) {
                return true;
            }
            final float x = pEvent2.getX() - _activeXBoardOffset,
                    y = pEvent2.getY() - _activeYBoardOffset;
            final float cellWidth = _activeCellWidth,
                    lastX = _lastX, lastY = _lastY;
            if (isDapu) {
                //点击棋盘打谱判断
                WindowManager wm = activity.getWindowManager();
                //获取屏幕宽度
                int width = wm.getDefaultDisplay().getWidth();
                //判断点击的位置靠左或靠右
                if (width / 2 > x) {
                    backMove(1);
                } else {
                    nextMove(1);
                }
            } else {
                if (x < lastX || x > lastX + cellWidth
                        || y < lastY || y > lastY + cellWidth) {
                    final Position newPoint = coord2Point(x, y);

                    if (isJudge != 0) {
//                        if (!checkJudgeMove(newPoint)) {
//                            downFlag = false;
//                            return true;
//                        }
                        downFlag = false;
                        return true;
                    } else {
                        //学习状态
                        if (isLearn) {
                            for (Position item : curPosition.childArray) {
                                if (item.x == newPoint.x && item.y == newPoint.y) {
                                    downFlag = true;
                                    nextMove(1);
                                    return true;
                                }
                            }
                        } else {
                            //判断当前是否允许落子
                            if (!checkMoving(newPoint)) {
                                downFlag = false;
                                return true;
                            } else {
                                downFlag = true;
                            }
                            moveStone(newPoint);
                        }
                    }
                }
            }
            return true;
        }

        public void moveStone(
                final Position pNewPoint
        ) {
            final float cellWidth = _activeCellWidth;
            final float cellHeight = _activeCellHeight;
            _lastX = (pNewPoint.x - _beginX) * cellWidth + _boardMarginXOffset;
            _lastY = (pNewPoint.y - _beginY) * cellHeight + _boardMarginYOffset;
            final Position lastPoint = new Position(pNewPoint);
//          final String vertex = Gtp.point2Vertex (pNewPoint, _boardSize);
            if (stoneMoveType == 0) {
                drawMovingStone(pNewPoint, _lastPoint);
                moveingFlag = true;
            }
            _lastPoint = lastPoint;
            if (stoneMoveType == 1) {
                setStone();
            }
        }

        /**
         * 棋盘单击事件
         *
         * @param pEvent
         * @return
         */
        public boolean onSingleTapUp(
                final MotionEvent pEvent
        ) {
            //判断棋盘初始化状态以及是否设置锁定棋盘
            if (_interactionLocked || waitFlag || isSelect) {
                return true;
            }
            //回调
            if (mOnEndOfGame != null) {
                mOnEndOfGame.onClickBoard();
            }
            return onScroll1(null, pEvent, 0, 0);
        }

        void setStone() {
            _showHint = false;
            final Position lastPoint = _lastPoint;
//          _lastPoint = null;
            //判断死活题落子

            if (lastPoint == null) {
                return;
            }
//            if (!checkMove(lastPoint)) {
//                return;
//            }
//            drawMovingStone(new Position(lastPoint), false);
            if (curPosition.childArray.size() > 0 && !isBranch && !isAddBranch && !isTryDown && boardStyle != 2 && !isGuess) {
                //弹出选择框,选择删子或覆盖
                bottomMenuDialog = new BottomMenuDialog.Builder(activity)
                        .addMenu("插入一子", 0xFFFF0000, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //判断当前落子在下一手是否存在
                                if (curPosition.childArray.size() > 0 && (curPosition.childArray.get(0).x == lastPoint.x && curPosition.childArray.get(0).y == lastPoint.y)) {
                                    nextMove(1);
                                } else if (checkMove(lastPoint, true)) {
                                    nextMove();
                                }
                                bottomMenuDialog.dismiss();
                            }
                        })
                        .addMenu("覆盖", 0, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //清空之后的子
                                curPosition.childArray.clear();
                                curPosition.selectRoot = -1;
                                //总手数更新
                                maxmove = curPosition.move;
                                //落子
                                if (checkMove(lastPoint, false)) {
                                    nextMove();
                                }
                                bottomMenuDialog.dismiss();
                            }
                        })
                        .create();
                //show
                bottomMenuDialog.show();
            } else {
                if (checkMove(lastPoint, false)) {
                    nextMove();
                }
            }
        }

        void moveStoneIncrementally(
                int pDiffX,
                int pDiffY
        ) {
            if (_interactionLocked) {
                return;
            }
            Position lastPoint = _lastPoint;
            if (pDiffX == 0 && pDiffY == 0) {
                if (lastPoint != null) {
                    setStone();
                }
                return;
            }

            final Set<Position> legalMoves = _legalMoves;
            if (lastPoint == null) {
                if (legalMoves.size() == 0) {
                    return;
                }
                lastPoint = legalMoves.iterator().next();
                pDiffX = pDiffY = 0;
            }
            int newX = lastPoint.x, newY = lastPoint.y;
            final int boardSize = _boardSize;
            final Position newPoint = new Position();
            do {
                newX += pDiffX;
                newY += pDiffY;
                if (newX < 0 || newX == boardSize
                        || newY < 0 || newY == boardSize) {
                    return;
                }
                newPoint.set(newX, newY);
            }
            while (!legalMoves.contains(newPoint));
            moveStone(newPoint);
        }

        void onUp() {
//
        }

        public boolean onDown(
                final MotionEvent pMotionEvent
        ) {
            if (_interactionLocked || waitFlag) {
                return true;
            }
            return onScroll(null, pMotionEvent, 0, 0);
        }

//        public void onLongPress(
//                final MotionEvent pEvent
//        ) {
//            if (_interactionLocked) {
//                return;
//            }
//            if (_isZoom) {
//                _isZoom = false;
//            } else {
//                _isZoom = true;
//                final Position lastPoint = _lastPoint;
//                _zoomViewPoint = _legalMoves.contains(lastPoint) ? lastPoint
//                        : coord2Point(pEvent.getX(), pEvent.getY());
//            }
//            drawBoard(false);
//            _wasTapUp = true;
//        }
    }

    public BoardView( // 1
                      final Context pContext,
                      final AttributeSet pAttrs
    ) {
        super(pContext, pAttrs);
        final Resources resources = getResources();
        _boardView = this;
        _xferModePaintAtopAlpha.setAlpha(resources.getInteger(
                R.integer.movingStoneAlphaTransperency));
        _crossCursporPaint.setStrokeWidth(
                _crossCursorStrokeWidth =
                        resources.getInteger(R.integer.crossCursorStrokeWidth));
        _crossCursporPaint.setColor(resources.getColor(R.color.crossCursorColor));
        //原背景图
//        _boardPaint.setShader(new BitmapShader(
//                BitmapFactory.decodeResource(resources, R.drawable.board),
//                Shader.TileMode.MIRROR, Shader.TileMode.MIRROR));
//        _boardPaint.setAlpha(1000);
        _blackStoneColor = resources.getColor(R.color.blackStoneColor);
        _whiteStoneColor = resources.getColor(R.color.whiteStoneColor);
        _redoTextColor = resources.getColor(R.color.redoTextColor);
        _stoneMarkesColor = resources.getColor(R.color.stoneMarkesColor);

        //获取绘制类型
        TypedArray typedArray = pContext.obtainStyledAttributes(pAttrs, R.styleable.BoardView);
        if (typedArray != null) {
            boardStyle = typedArray.getInt(R.styleable.BoardView_boardStyle, 0);
            typedArray.recycle();
        }
        //// TODO: 18/2/26 是否在此处初始化棋盘设置

        final SurfaceHolder holder = _surfaceHolder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);

        handler = new Handler();

        final BoardGestureListener gestureListener = _gestureListener =
                new BoardGestureListener();
        _gestureDetector = new GestureDetector(pContext, gestureListener);
    }

    public void surfaceCreated( // 3
                                final SurfaceHolder pHolder
    ) {
    }

    public void surfaceDestroyed(
            final SurfaceHolder pHolder
    ) {
    }

    public void surfaceChanged( // 4
                                final SurfaceHolder pHolder,
                                final int pFormat,
                                final int pWidth,
                                final int pHeight
    ) {
        final boolean isLandscape = pWidth > pHeight;
        final int boardWidth = isLandscape ? pHeight : pWidth;
//      final View scoreView = _scoreView;
//      final int scoreWidth = scoreView.getWidth (),
//              scoreHeight = scoreView.getHeight ();


//        final Bitmap backgroundBitmap = Bitmap.createBitmap(1000,
//                1000, _bitmapConfig);
//        final Resources resources = getResources();
//        final Drawable drawable = resources.getDrawable(R.drawable.painting);
//        final int drawableWidth = drawable.getIntrinsicWidth(),
//                drawableHeight = drawable.getIntrinsicHeight();
//        drawable.setBounds(0, 0, drawableWidth, drawableHeight);
//        final Canvas canvas = new Canvas(backgroundBitmap);
//        canvas.scale(
//                (float) 1000 / drawableWidth, (float) 1000 / drawableHeight);
//        drawable.draw(canvas);

        //mainActivity.sizeScoreViewTexts (scoreWidth, scoreHeight);

        final Bitmap boardBitmap = _boardBitmap;
        if (boardBitmap != null && _boardWidth == boardWidth) {
            drawBoard2Surface();
            return;
        }
        if (boardBitmap == null) {
            _boardBitmap = Bitmap.createBitmap(pWidth, pHeight, _bitmapConfig);
            _boardWidth = pWidth;
            _boardHeight = pHeight;
            if (isLandscape) {
                _yBoardOffset = 0;
//                _xBoardOffset = (pWidth - pHeight) / 2;
                _xBoardOffset = 0;
            } else {
                _yBoardOffset = 0;
//                _yBoardOffset = (pHeight - pWidth) / 2;
                _xBoardOffset = 0;
            }
            //初始化棋盘背景
            drawBoardBack();
            //初始化完成
            loadFlag = true;
        } else {
            drawBoard();
        }
    }

    void initBoard(
            final int pSize,
            final int xSize,
            final int ySize
    ) {
        _boardSize = pSize;
        final int boardW = _boardWidth, boardH = _boardHeight;
        int boardMargin = (boardW > boardH ? boardH : boardW) / (xSize > ySize ? ySize : xSize);
        _marginLeft = _beginX > 0 ? -10 : boardMargin;
        _marginRight = pSize - _beginX > xSize ? -10 : boardMargin;
        _marginTop = _beginY > 0 ? -10 : boardMargin;
        _marginBottom = pSize - _beginY > ySize ? -10 : boardMargin;
        //坐标样式判断
        if (boardStyle != 2) { //不是死活题
            switch (coordiNateStyle) {
                case 0:     //默认
                    _marginRight /= 2;
                    _marginBottom /= 2;
                    _marginLeft /= 2;
                    _marginTop /= 2;
                    break;
                case 1:     //left  top
                    _marginRight /= 2;
                    _marginBottom /= 2;
                    break;
                case 2:    //bottom right
                    _marginLeft /= 2;
                    _marginTop /= 2;
                    break;
            }

        }
        final float shapeFactor = _SHAPE_FACTOR,
                boardWidth = boardW * shapeFactor,
                boardHeight = boardH * shapeFactor,
                cellWidth = (boardWidth - (_marginLeft + _marginRight)) / (xSize - 1),
                cellHeight = (boardHeight - (_marginTop + _marginBottom)) / (ySize - 1),
                cellWidthDIV2 = boardMargin,
                realCellWidth = cellWidth > cellHeight ? cellHeight : cellWidth / shapeFactor,
                realCellHeight = _realCellHeight = cellHeight / shapeFactor,
                realCellWidthDIV2 = cellWidthDIV2 / shapeFactor;
        final int intCellWidth = _intCellWidth = (int) realCellWidth;
        _intCellWidthDIV2 = (int) (_cellWidthDIV2 = realCellWidthDIV2);
        final float zoomFactor =
                _zoomFactor = boardW / (_ZOOM_BOARD_SIZE * realCellWidth);
        _zoomRangeHigh = pSize - (_zoomRangeLow = _ZOOM_BOARD_SIZE / 2);
        //棋子真实大小
        _realCellSize = realCellWidth > realCellHeight ? realCellHeight : realCellWidth;
        //棋子偏移量
        _boardMarginXOffset = _marginLeft - (int) _realCellSize / 2;
        _boardMarginYOffset = _marginTop - (int) _realCellSize / 2;

        _maxZoomBoardSizeOffset = pSize - _ZOOM_BOARD_SIZE;
        final float zoomCellWidth = _zoomCellWidth = realCellWidth * zoomFactor,
                zoomCellWidthDIV2 = realCellWidthDIV2 * zoomFactor;
        final int intZoomCellWidth = _intZoomCellWidth = (int) zoomCellWidth;
        _intZoomCellWidthDIV2 = (int) (_zoomCellWidthDIV2 = zoomCellWidthDIV2);
        _realCellWidth = cellWidth / shapeFactor;
        _activeCellWidth = _realCellWidth;
        _activeCellHeight = realCellHeight;
        _activeXBoardOffset = _xBoardOffset;
        _activeYBoardOffset = _yBoardOffset;

        final Resources resources = getResources();
        Path path = new Path();
        final float lineEnd = boardWidth - _marginRight;
        final float lineEnd2 = boardHeight - _marginBottom;
        for (int idx = 0; idx < ySize; idx++) {
            final float pos = _marginTop + idx * cellHeight;
            path.moveTo(_marginLeft, pos);
            path.lineTo(lineEnd, pos);
        }
        for (int idx = 0; idx < xSize; idx++) {
            final float pos2 = _marginLeft + idx * cellWidth;
            path.moveTo(pos2, _marginTop);
            path.lineTo(pos2, lineEnd2);
        }

        PathShape shape = _gridShape = new PathShape(path, boardWidth, boardWidth);
        shape.resize(boardW, boardW);

        path = new Path();
        final List<Position> markers = Generics.newArrayList();
        markers.addAll(points2Coords(_markers));
        final float markerRadius = 100 * shapeFactor / 19,
                markerOffset = _realCellSize / 2 - shapeFactor / 2,
                xBoardOffset = _xBoardOffset * shapeFactor,
                yBoardOffest = _yBoardOffset * shapeFactor;
        //判断星位超出范围临时位置
        Position lastPoint;
        for (final Position point : markers) {
            lastPoint = new Position(_beginX, _beginY);
            point2Coord(lastPoint);
            //若当前要绘制的星位坐标小于起始位置,则不绘制
            if (lastPoint.x > point.x || lastPoint.y > point.y) {
                continue;
            }
            path.addCircle(point.x * shapeFactor + markerOffset - xBoardOffset,
                    point.y * shapeFactor + markerOffset - yBoardOffest,
                    markerRadius, Path.Direction.CW);
        }
        shape = _markersShape = new PathShape(path, boardWidth, boardWidth);
        shape.resize(boardW, boardW);
        //////

        final Bitmap.Config bitmapConfig = _bitmapConfig;
        // TODO: 17/1/10  背景图片平铺
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        Bitmap bStone = BitmapFactory.decodeResource(resources, R.drawable.bstone, options),
                wStone = BitmapFactory.decodeResource(resources, R.drawable.wstone, options),
                wStoneLite = BitmapFactory.decodeResource(resources, R.drawable.wstone_lite, options),
                bStoneLite = BitmapFactory.decodeResource(resources, R.drawable.bstone_lite, options);

        _blackStoneBitmap = Bitmap.createScaledBitmap(bStone, intCellWidth, intCellWidth, true);
        _whiteStoneBitmap = Bitmap.createScaledBitmap(wStone, intCellWidth, intCellWidth, true);
        _blackStoneJudgeBitmap = Bitmap.createScaledBitmap(bStoneLite, intCellWidth, intCellWidth, true);
        _whiteStoneJudgeBitmap = Bitmap.createScaledBitmap(wStoneLite, intCellWidth, intCellWidth, true);


        final Canvas canvas = new Canvas();
        final float stoneCenter = boardWidth / 2,
                stoneRadius = stoneCenter - boardWidth / 200;
        path = new Path();
        final Paint paint = new Paint(_xferModePaintSrc);

//      Path paht1 = new Path();
//      paht1.moveTo(stoneCenter, 20);// 此点为多边形的起点
//      paht1.lineTo(stoneCenter+40, 25);
//      paht1.lineTo(stoneCenter, 25);
//      paht1.close();
//      path.addPath(paht1);
//      path.lineTo(stoneCenter,1000);
//      path.addCircle (stoneCenter, stoneCenter, stoneRadius * 0.7f,
//              Path.Direction.CW);
        //当前子角标
        float stoneCenter1 = stoneCenter * 0.9f;
        path.moveTo(stoneCenter1, stoneCenter1);// 此点为多边形的起点
        path.lineTo(stoneCenter1 + stoneRadius * 0.9f, stoneCenter1);
        path.lineTo(stoneCenter1, stoneCenter1 + stoneRadius * 0.9f);

        shape = new PathShape(path, boardWidth, boardWidth);
        shape.resize(realCellWidth, realCellWidth);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(stoneCenter / 9);

        Bitmap bitmap = _blackStoneMarkerBitmap = Bitmap.createBitmap(
                intCellWidth, intCellWidth, bitmapConfig);
        canvas.setBitmap(bitmap);
        paint.setColor(_stoneMarkesColor);
        shape.draw(canvas, paint);

        //有数字时最后一子标记
        path = new Path();
        path.moveTo(stoneRadius * 2 - stoneRadius * 1.7f, stoneCenter1 * 1.5f);// 此点为多边形的起点
        path.lineTo(stoneRadius * 1.7f, stoneCenter1 * 1.5f);
        path.lineTo(stoneRadius, stoneRadius * 1.8f);

        shape = new PathShape(path, boardWidth, boardWidth);
        shape.resize(realCellWidth, realCellWidth);
        bitmap = _lastMoveMarkerBitmap = Bitmap.createBitmap(
                intCellWidth, intCellWidth, bitmapConfig);
        canvas.setBitmap(bitmap);
        paint.setColor(_stoneMarkesColor);
        shape.draw(canvas, paint);


//        bitmap = _blackStoneZoomMarkerBitmap = Bitmap.createBitmap(
//                intZoomCellWidth, intZoomCellWidth, bitmapConfig);
//        canvas.setBitmap(bitmap);
//        shape.resize(zoomCellWidth, zoomCellWidth);
//        shape.draw(canvas, paint);
//        bitmap = _whiteStoneZoomMarkerBitmap = Bitmap.createBitmap(
//                intZoomCellWidth, intZoomCellWidth, bitmapConfig);
//        canvas.setBitmap(bitmap);
//        shape.draw(canvas, blackPaint);
//
//        bitmap = _whiteTerritoryBitmap = Bitmap.createBitmap(
//                intCellWidth, intCellWidth, bitmapConfig);
//        canvas.setBitmap(bitmap);
//        path.rewind();
//        float strokeStart = boardWidth / 7,
//                strokeEnd = boardWidth - strokeStart;
//        path.moveTo(strokeStart, strokeStart);
//        path.lineTo(strokeEnd, strokeEnd);
//        path.moveTo(strokeStart, strokeEnd);
//        path.lineTo(strokeEnd, strokeStart);
//        shape = new PathShape(path, boardWidth, boardWidth);
//        shape.resize(realCellWidth, realCellWidth);
//        paint.setStrokeWidth(boardWidth / 15);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        shape.draw(canvas, paint);
//
//        bitmap = _blackTerritoryBitmap = Bitmap.createBitmap(
//                intCellWidth, intCellWidth, bitmapConfig);
//        canvas.setBitmap(bitmap);
//        paint.setColor(blackStoneColor);
//        paint.setStrokeWidth(boardWidth / 30);
//        shape.draw(canvas, paint);


//        path = new Path();
//        path.addCircle(stoneCenter, stoneCenter, judgeStoneRadius, Path.Direction.CW);
//        shape = new PathShape(path, boardWidth, boardWidth);
//        shape.resize(realCellWidth, realCellWidth);
//
//        bitmap = _whiteStoneJudgeBitmap = Bitmap.createBitmap(
//                intCellWidth, intCellWidth, bitmapConfig);
//        canvas.setBitmap(bitmap);
//        paint.setShader(new LinearGradient(
//                circleHighlight, circleHighlight,
//                highlightEnd, highlightEnd,
//                whiteStoneColor, resources.getColor(R.color.whiteStoneHighlightColor),
//                Shader.TileMode.CLAMP));
//        shape.draw(canvas, paint);
//
//        bitmap = _blackStoneJudgeBitmap = Bitmap.createBitmap(
//                intCellWidth, intCellWidth, bitmapConfig);
//        canvas.setBitmap(bitmap);
//        paint.setShader(new RadialGradient(
//                circleHighlight, circleHighlight, radiusHighlight,
//                resources.getColor(R.color.blackStoneHighlightColor),
//                blackStoneColor, Shader.TileMode.CLAMP));
//        shape.draw(canvas, paint);
    }


    void drawBoardBack() {
        final Bitmap boardBitmap = _boardBitmap;
        final int boardWidth = _boardWidth;

        final Resources resources = getResources();
        final Paint paint = new Paint(_xferModePaintSrc);
        paint.setColor(resources.getColor(R.color.boardColor));
        final Canvas canvas = new Canvas(boardBitmap);
        final int boardHeight = _boardHeight;

        // TODO: 17/1/10  背景图片平铺
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap boardImage = null, scaledBoardImage;
        if (boardStyle == 0) {    //普通全图棋盘背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_new1, options);
        } else if (boardStyle == 1) {  //直播室留边背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_studio, options);
        } else if (boardStyle == 2) {  //死活题背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.gomission, options);
        } else if (boardStyle == 3) { //猜棋背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_guess, options);
        } else {
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_new_2, options);
        }
        scaledBoardImage = Bitmap.createScaledBitmap(boardImage, boardWidth, boardHeight, true);
        _boardPaint.setShader(new BitmapShader(
                scaledBoardImage,  //board_11
                Shader.TileMode.MIRROR, Shader.TileMode.MIRROR));
        //透明度0~255  255不透明
        _boardPaint.setAlpha(255);
        //end

        canvas.drawRect(0, 0, boardWidth, boardHeight, paint);
        canvas.drawRect(0, 0, boardWidth, boardHeight, _boardPaint);
        drawBoard2Surface();
    }


    void drawBoard(
            final List<Position> pRedoPoints,
            final boolean moveCountFlag,
            final int pMoveNumber,
            final boolean pMarkBlack,
            MoveType moveType
    ) {
        final Bitmap boardBitmap = _boardBitmap;
        final int boardWidth = _boardWidth;
        final float realCellWidth = _realCellWidth,
                realCellHeight = _realCellHeight,
                activeCellWidthDIV2;

        final Resources resources = getResources();
        final Paint paint = new Paint(_xferModePaintSrc);
        paint.setColor(resources.getColor(R.color.boardColor));
        final Canvas canvas = new Canvas(boardBitmap);
        final int boardHeight = _boardHeight;

        // TODO: 17/1/10  背景图片平铺
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap boardImage = null, scaledBoardImage;
        if (boardStyle == 0) {    //普通全图棋盘背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_new1, options);
        } else if (boardStyle == 1) {  //直播室留边背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_studio, options);
        } else if (boardStyle == 2) {  //死活题背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.gomission, options);
        } else if (boardStyle == 3) { //猜棋背景
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_guess, options);
        } else {
            boardImage = BitmapFactory.decodeResource(resources, R.drawable.board_new1, options);
        }
        scaledBoardImage = Bitmap.createScaledBitmap(boardImage, boardWidth, boardHeight, true);
        _boardPaint.setShader(new BitmapShader(
                scaledBoardImage,  //board_11
                Shader.TileMode.MIRROR, Shader.TileMode.MIRROR));
        //透明度0~255  255不透明
        _boardPaint.setAlpha(255);
        //end

        canvas.drawRect(0, 0, boardWidth, boardHeight, paint);
        canvas.drawRect(0, 0, boardWidth, boardHeight, _boardPaint);
//        canvas.drawBitmap(scaledBoardImage, 0, 0, null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(resources.getColor(R.color.boardLineColor));
//        canvas.drawLine(0, 0, boardWidth, 0, paint);

        int intCellWidth, intCellWidthDIV2;
        final float zoomFactor = _zoomFactor, inverseZoomFactor = 1 / zoomFactor,
                xBoardOffset, yBoardOffset;
        final boolean isZoom = _isZoom;
        if (isZoom) {
            canvas.scale(zoomFactor, zoomFactor);
            final float zoomRangeLow = _zoomRangeLow,
                    zoomRangeHigh = _zoomRangeHigh,
                    maxZoomBoardSizeOffset = _maxZoomBoardSizeOffset;
            final Position zoomViewPoint = _zoomViewPoint;
            int x = zoomViewPoint.x, y = zoomViewPoint.y;
            xBoardOffset = x < zoomRangeLow ? 0
                    : (x >= zoomRangeHigh ? maxZoomBoardSizeOffset
                    : x - zoomRangeLow) * -realCellWidth;
            yBoardOffset = y < zoomRangeLow ? 0
                    : (y >= zoomRangeHigh ? maxZoomBoardSizeOffset :
                    y - zoomRangeLow) * -realCellWidth;
            _activeXBoardOffset = (int) (xBoardOffset * zoomFactor);
            _activeYBoardOffset = (int) (yBoardOffset * zoomFactor);
            _activeCellWidth = _zoomCellWidth;
            intCellWidth = _intZoomCellWidth;
            intCellWidthDIV2 = _intZoomCellWidthDIV2;
            activeCellWidthDIV2 = _zoomCellWidthDIV2;
        } else {
            xBoardOffset = _activeXBoardOffset = _xBoardOffset;
            yBoardOffset = _activeYBoardOffset = _yBoardOffset;
            _activeCellWidth = _realCellWidth;
            intCellWidth = _intCellWidth;
            intCellWidthDIV2 = _intCellWidthDIV2;
            activeCellWidthDIV2 = _cellWidthDIV2;
        }
        canvas.translate(xBoardOffset, yBoardOffset);
        _activeIntCellWidth = intCellWidth;
        _activeIntCellWidthDIV2 = intCellWidthDIV2;
        _gestureListener.reset(_activeCellWidth);

        _gridShape.draw(canvas, paint);

        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        _markersShape.draw(canvas, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm;
        final float textYOffset = _marginTop,
                textXPos = boardWidth
                        - _cellWidthDIV2 / 2f;
        for (int idx = 0; idx < _boardSize - _beginY; idx++) {
            final float pos = textYOffset + idx * realCellHeight;
            //重新计算Y轴,使其居中
            final String numberText = String.valueOf(_boardSize - _beginY - idx);
            if (_marginLeft > 0 && (coordiNateStyle == 1 || boardStyle == 2)) {
                paint.setTextSize(_marginLeft / 2.5f);
                fm = paint.getFontMetrics();
                float textY = pos + (fm.descent - fm.ascent) / 2 - fm.descent;
                canvas.drawText(numberText, _cellWidthDIV2 / 2f, textY, paint);
            }
            if (_marginRight > 0 && (boardStyle == 2)) {
                paint.setTextSize(_marginRight / 2.5f);
                fm = paint.getFontMetrics();
                float textY = pos + (fm.descent - fm.ascent) / 2 - fm.descent;
                canvas.drawText(numberText, textXPos, textY, paint);
            }
        }

        //上方和下方的坐标
        final int numPosEntries = _boardSize * 2;
        final float[] textPosUpper = _textPosUpper = new float[numPosEntries],
                textPosLower = _textPosLower = new float[numPosEntries];
        final float upperPosY = _cellWidthDIV2 / 2f, lowerPosY = boardHeight - _cellWidthDIV2 / 2f;
        //重新计算Y轴,使其居中
        paint.setTextSize(_marginTop / 2.5f);
        fm = paint.getFontMetrics();
        float textY = upperPosY + (fm.descent - fm.ascent) / 2 - fm.descent;
        paint.setTextSize(_marginBottom / 2.5f);
        fm = paint.getFontMetrics();
        float textY2 = lowerPosY + (fm.descent - fm.ascent) / 2 - fm.descent;

        for (int idx = 0, posIdx = 0; idx < _boardSize - _beginX; idx++, posIdx += 2) {
            final float pos = _marginLeft + idx * realCellWidth;
            textPosUpper[posIdx] = textPosLower[posIdx] = pos;
            final int yIdx = posIdx + 1;
            textPosUpper[yIdx] = textY;
            textPosLower[yIdx] = textY2;
        }
        final char[] lettersText = GameUtil._POSITION_LETTERS_STR.substring(_beginX, _boardSize).toCharArray();
        if (_marginTop > 0 && (coordiNateStyle == 1 || boardStyle == 2)) {
            paint.setTextSize(_marginTop / 2.5f);
            canvas.drawPosText(lettersText, 0, _boardXsize, _textPosUpper, paint);
        }
        if (_marginBottom > 0 && (boardStyle == 2)) {
            paint.setTextSize(_marginBottom / 2.5f);
            canvas.drawPosText(lettersText, 0, _boardXsize, _textPosLower, paint);
        }
        //end
        canvas.translate(-xBoardOffset, -yBoardOffset);
        if (isZoom) {
            canvas.scale(inverseZoomFactor, inverseZoomFactor);
        }
        if (isJudge == 0) {
            drawStones(canvas, false, false, false);
        } else if (isJudge == 1) {
            //绘子
            drawStones2(canvas);
        } else {
            drawStones(canvas, false, false, false);
            drawJudgeView(canvas);
        }
        if (moveCountFlag && isJudge == 0) {
            final Position point = new Position(isTryDown ? tmpTotalMoveLise : curPosition);
            //最后一手三角形
            if (point != null && point.x >= 0 && point.y >= 0 && moveType == MoveType.TR && currentMove > 0) {
                drawStone(canvas, point,
                        true, true, false, false, false, false);
            } else {
                if (currentMove > 0) {
                    //绘制手数
                    labelStones(null,
                            intCellWidthDIV2, activeCellWidthDIV2, intCellWidth, canvas,
                            SpecialStonesType.HISTORY, moveType, pMoveNumber);
                    //标记
                    drawStone(canvas, point,
                            false, true, false, false, false, false);
                }
            }

        }
        if (pRedoPoints != null && !pRedoPoints.isEmpty()) {
            labelStones(pRedoPoints,
                    intCellWidthDIV2, activeCellWidthDIV2, intCellWidth, canvas,
                    SpecialStonesType.REDO_MOVES, moveType, pMoveNumber);
        }
        drawStoneList = false;
        drawBoard2Surface();
    }

    enum
    SpecialStonesType {
        HISTORY,
        REDO_MOVES
    }

    //手数样式
    public static enum MoveType {
        TR,
        LB
    }

    //棋子微调方向
    public static enum Direction {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT
    }

    void labelStones(
            final List<Position> pStones,
            final int pIntCellWidthDIV2,
            final float pActiveCellWidthDIV2,
            final int pIntCellWidth,
            final Canvas pCanvas,
            final SpecialStonesType pType,
            final MoveType moveType,
            int moveCount
    ) {
        final Paint numberPaint = _numberPaint;
        numberPaint.setTextSize(_realCellSize / 2.2f);
        final Paint.FontMetrics fm = numberPaint.getFontMetrics();
        final int blackColor = _blackStoneColor, whiteColor = _whiteStoneColor,
                redoColor = _redoTextColor;
        char redoChar = 'A';
        //当前子颜色
        boolean pColorBlack = (isTryDown ? tmpTotalMoveLise.c : curPosition.c) == WHITE ? true : false;

        if (pType == SpecialStonesType.HISTORY && moveType == MoveType.LB) { //历史手数
            //画手数
            Integer[] tmpPointState = new Integer[_boardSize * _boardSize];
            for (int i = 0; i < tmpPointState.length; i++) {
                tmpPointState[i] = EMPTY;
            }
            //落子所在数组中的下标
            int address = -1;
            //判断最后一子颜色
            int tmpCount = moveCount == 999 ? getParentNodeCount() : moveCount;
            tmpCount = tmpCount - drawMarkIndex;
            //数字开始位置
            Position tmpPosition = isTryDown ? tmpTotalMoveLise : curPosition;
            //计算显示的手数
            int initMove = tmpPosition.move - drawMarkIndex;
            for (int i = tmpCount; i > 0; i--) {
                if (tmpPosition.parent == null || tmpPosition.x == -1 || tmpPosition.y == -1 || initMove <= 0) {
                    break;
                }
                address = tmpPosition.y * _boardSize + tmpPosition.x;
                if (isJudge == 1) {
                    if ((showState[address] == WHITE_VIRTUAL && pointState[address] == WHITE)
                            || (showState[address] == BLACK_VIRTUAL && pointState[address] == BLACK)) {
                        pColorBlack = !pColorBlack;
                        tmpPosition = tmpPosition.parent;
                        initMove--;
                        continue;
                    }
                }
                int move = pointState[address];
                if (move != 0 && move == tmpPosition.c && tmpPointState[address] != move) {
                    if (tmpPosition.isPass) {
                        tmpPosition = tmpPosition.parent;
                        initMove--;
                        continue;
                    }
                    Position point = new Position(tmpPosition.x, tmpPosition.y, move);
//                    if (moveCount == tmpCount) {
//                        numberPaint.setColor(_stoneMarkesColor);
//                    } else {
//                        numberPaint.setColor(move == WHITE ? blackColor : whiteColor);
//                    }
                    numberPaint.setColor(move == WHITE ? blackColor : whiteColor);
                    String text = "";
                    text = String.valueOf(initMove);
                    point2Coord(point);
                    point.offset((int) _realCellSize / 2, (int) _realCellSize / 2);
                    float textY = point.y + (fm.descent - fm.ascent) / 2 - fm.descent;
                    pCanvas.drawText(text, point.x, textY, numberPaint);

                    tmpPointState[address] = move;
                    tmpPosition = tmpPosition.parent;
                    initMove--;
                } else {
                    tmpPosition = tmpPosition.parent;
                    initMove--;
                    continue;
                }
            }
        } else if (pType == SpecialStonesType.REDO_MOVES && showBranch) {    //分支
            if (pStones == null) {
                return;
            }
            final int numStones = pStones.size();
            for (int pointIdx = 0;
                 pointIdx < numStones; pointIdx++) {
                Position point = new Position(pStones.get(pointIdx));
                String text = "";
                //设置分支字母颜色
                numberPaint.setColor(redoColor);
                text = String.valueOf(redoChar++);
                point2Coord(point);

                point.offset((int) _realCellSize / 2, (int) _realCellSize / 2);
                float textY = point.y + (fm.descent - fm.ascent) / 2 - fm.descent;
                pCanvas.drawText(text, point.x, textY, numberPaint);
            }
        }
    }

    void drawBoard(
            final int pMoveNumber,
            final boolean pMarkBlack
    ) {
        drawBoard(null, true,
                pMoveNumber, pMarkBlack, moveType);
    }

    void drawTerritory(
            final List<Position> totalMoves,
            final boolean pIsCoord
    ) {
        final Canvas canvas = new Canvas(_boardBitmap);
        drawStones(canvas, true, pIsCoord, false);
    }

    void drawDeadStones() {
        final Canvas canvas = new Canvas(_boardBitmap);
        drawStones(canvas, false, false, true);
    }

    public void drawBoard2Surface() {
        final SurfaceHolder surfaceHolder = _surfaceHolder;
        final Canvas surfaceCanvas = surfaceHolder.lockCanvas();
        if (surfaceCanvas != null) {
            surfaceCanvas.drawBitmap(_boardBitmap, 0, 0, _xferModePaintSrc);
            surfaceHolder.unlockCanvasAndPost(surfaceCanvas);
        }
    }

    private boolean drawMovingStone(
            final Position pMovingStone,
            final Position pLastStone
    ) {
        final SurfaceHolder surfaceHolder = _surfaceHolder;
        if (pLastStone != null) {
            final Rect lastRect = createCellRect(pLastStone);
            final int boardWidth = _boardWidth, boardHeight = _boardHeight;
            eraseCrossCursor(surfaceHolder, getCrossCursorRect(pLastStone, true,
                    boardWidth, boardHeight));
            eraseCrossCursor(surfaceHolder, getCrossCursorRect(pLastStone, false,
                    boardWidth, boardHeight));
            final Canvas canvas = surfaceHolder.lockCanvas(lastRect);
            if (canvas != null) {
                canvas.drawBitmap(_boardBitmap,
                        lastRect, lastRect, _xferModePaintSrc);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
        return drawMovingStone(pMovingStone, true);
    }

    boolean drawMovingStone(
            final Position pMovingStone,
            final boolean pIsMoving
    ) {
        final int width = _boardWidth, height = _boardHeight;
        final Rect movingStoneRect = createCellRect(pMovingStone);
        final SurfaceHolder surfaceHolder = _surfaceHolder;
//        drawCrossCursor(surfaceHolder, pMovingStone, true, width, height);
//        drawCrossCursor(surfaceHolder, pMovingStone, false, width, height);
        final boolean isBlack = playerBlackMoves;
        final Canvas canvas = surfaceHolder.lockCanvas(movingStoneRect);
        if (canvas != null) {
            canvas.drawBitmap(_boardBitmap,
                    movingStoneRect, movingStoneRect, _xferModePaintSrc);
            drawStone(canvas, pMovingStone, isBlack, false,
                    false, true, pIsMoving, false);
            if (!pIsMoving) // mark
            {
                drawStone(canvas, pMovingStone, isBlack, true, false,
                        true, pIsMoving, false);
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
        //微调回调
        //回调
        if (mOnEndOfGame != null) {
            mOnEndOfGame.onFineStone(true);
        }
        return isBlack;
    }

    /**
     * @param x
     * @param y
     * @param color
     */
    int status = 0; //闪烁状态
    int count = 0;  //闪烁次数

    public void drawStoneGradient(int x, int y, int color) {
        drawStoneList = true;
        status = 0;
        count = 0;
        final SurfaceHolder surfaceHolder = _surfaceHolder;
        final Rect movingStoneRect = new Rect(0, 0, _boardHeight, _boardWidth);

        final Paint paint = new Paint(_xferModePaintSrc);
        paint.setStyle(Paint.Style.FILL);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);


        final Position position = new Position(x, y);
        point2Coord(position);

        //绘制虚点
        int intCellWidth = (int) _realCellWidth;
        final Bitmap.Config bitmapConfig = _bitmapConfig;
        final Bitmap stontBitmap = Bitmap.createBitmap(
                intCellWidth, intCellWidth, bitmapConfig);
        Canvas stontCanvas = new Canvas(stontBitmap);
        final float stoneCenter = _boardWidth / 2;
        Path path = new Path();
        path.addCircle(stoneCenter, stoneCenter, stoneCenter, Path.Direction.CW);
        PathShape shape = new PathShape(path, _boardWidth, _boardWidth);
        shape.resize(intCellWidth, intCellWidth);
        paint.setShader(new RadialGradient(
                stoneCenter, stoneCenter, stoneCenter,
                color,
                Color.TRANSPARENT, Shader.TileMode.MIRROR));
        shape.draw(stontCanvas, paint);
        //释放canvas资源
        stontCanvas = null;
        //闪烁
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情
                if (status == 0) {
                    final Canvas canvas = surfaceHolder.lockCanvas(movingStoneRect);
                    //判断canvas是否为空
                    if (canvas == null) {
                        return;
                    }
                    canvas.drawBitmap(_boardBitmap,
                            movingStoneRect, movingStoneRect, _xferModePaintSrc);
                    canvas.drawBitmap(stontBitmap, position.x, position.y, _xferModePaintAtop);
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    status = 1;
                } else {
                    final Canvas canvas = surfaceHolder.lockCanvas(movingStoneRect);
                    if (canvas == null) {
                        return;
                    }
                    canvas.drawBitmap(_boardBitmap,
                            movingStoneRect, movingStoneRect, _xferModePaintSrc);
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    status = 0;
                }
                count += 1;
                if (count < 1) {
                    handler.postDelayed(this, 200);
                } else {
                    //释放图片资源
                    stontBitmap.recycle();
                }
            }
        };
        handler.postDelayed(runnable, 200);
    }

    /**
     * 批量绘制虚点
     *
     * @param model
     */
    public void drawStoneGradientList(List<DrawStoneModel> model) {
        drawStoneList = true;
        final SurfaceHolder surfaceHolder = _surfaceHolder;
        final Rect movingStoneRect = new Rect(0, 0, _boardHeight, _boardWidth);

        final Paint paint = new Paint(_xferModePaintSrc);
        paint.setStyle(Paint.Style.FILL);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        final Canvas canvas = surfaceHolder.lockCanvas(movingStoneRect);
        canvas.drawBitmap(_boardBitmap,
                movingStoneRect, movingStoneRect, _xferModePaintSrc);

        int intCellWidth = (int) _realCellWidth;
        final Bitmap.Config bitmapConfig = _bitmapConfig;
        final float stoneCenter = _boardWidth / 2;
        Bitmap stontBitmap;
        Canvas stontCanvas;
        Path path;

        for (DrawStoneModel item : model) {
            final Position position = new Position(item.getX(), item.getY());
            point2Coord(position);
            //绘制虚点

            stontBitmap = Bitmap.createBitmap(
                    intCellWidth, intCellWidth, bitmapConfig);
            stontCanvas = new Canvas(stontBitmap);

            path = new Path();
            path.addCircle(stoneCenter, stoneCenter, stoneCenter, Path.Direction.CW);
            PathShape shape = new PathShape(path, _boardWidth, _boardWidth);
            shape.resize(intCellWidth, intCellWidth);
            paint.setShader(new RadialGradient(
                    stoneCenter, stoneCenter, stoneCenter,
                    item.getC(),
                    Color.TRANSPARENT, Shader.TileMode.MIRROR));
            shape.draw(stontCanvas, paint);

            canvas.drawBitmap(stontBitmap, position.x, position.y, _xferModePaintAtop);
        }


        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    /**
     * 新形势判断
     *
     * @param canvas
     */
    public void drawJudgeView(Canvas canvas) {

        Position curMove;
        Paint judgePaint = new Paint();
        judgePaint.setStyle(Paint.Style.FILL);
        float rangeLong;
        double move;
        int move2;
        boolean isBlack;
        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                //结果点的颜色
                move = newJudgeState[x * _boardSize + y];
                //太小则不绘制
                if (Math.abs(move) > 0.1) {
                    //棋盘点的颜色
                    move2 = pointState[y * _boardSize + x];
                    //是否为黑
                    isBlack = move > 0 ? true : false;
                    //接口给的子不为空并且颜色不重复或者这个点事empty
                    if (move != 0 && (isBlack != (move2 == BLACK) || move2 == EMPTY)) {
                        //当前子生成对象
                        curMove = new Position(x, y, 1);
                        //开根
                        rangeLong = (float) Math.sqrt(Math.abs(move));
                        //绘制
                        drawJudgeStone(canvas, curMove, isBlack, false, judgePaint, rangeLong);
                    }
                }
            }
        }
    }

    private void drawJudgeStone(
            final Canvas pCanvas,
            final Position pPoint,
            final boolean pBlack,
            final boolean pIsCoord,
            Paint pPaint,
            final float stoneW
    ) {
        if (!pIsCoord) {
            point2CoordCenter(pPoint);
        }
        final int x = pPoint.x, y = pPoint.y;
        float radius = (_activeCellWidth * stoneW) / 2 * 0.5f;
        if (pBlack) {
            pPaint.setColor(Color.BLACK);
        } else {
            pPaint.setColor(Color.WHITE);
        }
        pCanvas.drawRect(x - radius, y - radius, x + radius, y + radius, pPaint);
    }


    /**
     * @param x
     * @param y
     */
    private Position selectRectPosition = null; //选中的结束卓比奥
    private Position beginRectPosition = new Position(0, 0);  //起始点
    private int beginDirection = 0;             //默认选中的角
    private boolean isSelect = false;           //是否为搜索状态

    private void drawSelectRect(Position selectPosition) {
        //判断不合法绘制
        if (selectPosition.x < 0 || selectPosition.x > _boardSize - 1 || selectPosition.y > _boardSize - 1 || selectPosition.y < 0 || !isSelect) {
            return;
        }
        final SurfaceHolder surfaceHolder = _surfaceHolder;
        final Rect movingStoneRect = new Rect(0, 0, _boardHeight, _boardWidth);
        Paint paint = new Paint();

        paint.setAntiAlias(true);

        paint.setColor(0x7BC9DB);
        paint.setAlpha(64);
        paint.setStyle(Paint.Style.FILL);//设置为空心
        paint.setStrokeWidth(1);
        final Canvas canvas = surfaceHolder.lockCanvas(movingStoneRect);
        canvas.drawBitmap(_boardBitmap,
                movingStoneRect, movingStoneRect, _xferModePaintSrc);
        selectRectPosition = selectPosition;
        point2Coord(selectRectPosition);
        Position beginPosition = new Position(beginRectPosition.x, beginRectPosition.y);
        point2Coord(beginPosition);
        switch (beginDirection) {
            case 0: //左上角
                canvas.drawRect(beginPosition.x, beginPosition.y, selectRectPosition.x + _realCellSize, selectRectPosition.y + _realCellSize, paint);
                paint.setStyle(Paint.Style.STROKE);//设置为空心
                paint.setColor(0x3F85C5);
                paint.setAlpha(255);
                //绘制边框
                canvas.drawRect(beginPosition.x, beginPosition.y, selectRectPosition.x + _realCellSize, selectRectPosition.y + _realCellSize, paint);
                break;
            case 1: //右上角
                canvas.drawRect(beginPosition.x + _realCellSize, beginPosition.y, selectRectPosition.x, selectRectPosition.y + _realCellSize, paint);
                paint.setStyle(Paint.Style.STROKE);//设置为空心
                paint.setColor(0x3F85C5);
                paint.setAlpha(255);
                //绘制边框
                canvas.drawRect(beginPosition.x + _realCellSize, beginPosition.y, selectRectPosition.x, selectRectPosition.y + _realCellSize, paint);
                break;
            case 2: //左下角
                canvas.drawRect(beginPosition.x, beginPosition.y + _realCellSize, selectRectPosition.x + _realCellSize, selectRectPosition.y, paint);
                paint.setStyle(Paint.Style.STROKE);//设置为空心
                paint.setColor(0x3F85C5);
                paint.setAlpha(255);
                //绘制边框
                canvas.drawRect(beginPosition.x, beginPosition.y + _realCellSize, selectRectPosition.x + _realCellSize, selectRectPosition.y, paint);
                break;
            case 3: //右下角
                canvas.drawRect(beginPosition.x + _realCellSize, beginPosition.y + _realCellSize, selectRectPosition.x, selectRectPosition.y, paint);
                paint.setStyle(Paint.Style.STROKE);//设置为空心
                paint.setColor(0x3F85C5);
                paint.setAlpha(255);
                //绘制边框
                canvas.drawRect(beginPosition.x + _realCellSize, beginPosition.y + _realCellSize, selectRectPosition.x, selectRectPosition.y, paint);
                break;
        }
        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public String getSelectStr() {
        Position position = _gestureListener.coord2Point(selectRectPosition.x, selectRectPosition.y);
        int yLength = Math.abs(beginRectPosition.y - position.y) + 1;
        int xLength = Math.abs(beginRectPosition.x - position.x) + 1;
        //方向长宽
        StringBuilder str = new StringBuilder(beginDirection + "|" + xLength + "|" + yLength + "|");
        int newX = 0;
        int newY = 0;
        for (int y = 0; y < yLength; y++) {
            for (int x = 0; x < xLength; x++) {

                if (beginDirection == 0) {
                    newX = x;
                    newY = y;
                } else if (beginDirection == 1) {
                    newX = position.x + x;
                    newY = y;
                } else if (beginDirection == 2) {
                    newX = x;
                    newY = position.y + y;
                } else {
                    newX = position.x + x;
                    newY = position.y + y;
                }
                int address = newY * _boardSize + newX;
                int move = pointState[address];
                str.append(moveToMark(move));
            }
//            str += "\n";
        }
        return str.toString();
    }

    /**
     * 批量绘制字母到指定坐标
     *
     * @param points
     */
    public void drawLabel(List<Position> points) {
        if (points == null || points.size() == 0) return;
        final SurfaceHolder surfaceHolder = _surfaceHolder;
        final Rect movingStoneRect = new Rect(0, 0, _boardHeight, _boardWidth);

        final Canvas canvas = surfaceHolder.lockCanvas(movingStoneRect);
        canvas.drawBitmap(_boardBitmap,
                movingStoneRect, movingStoneRect, _xferModePaintSrc);

        final Paint numberPaint = _numberPaint;
        numberPaint.setTextSize(_realCellSize / 1.5f);
        final Paint.FontMetrics fm = numberPaint.getFontMetrics();

        //从此字母开始
        char redoChar = 'A';
        final int numStones = points.size();
        for (int pointIdx = 0;
             pointIdx < numStones; pointIdx++) {
            Position point = new Position(points.get(pointIdx));
            String text = "";
            //设置分支字母颜色
            numberPaint.setColor(Color.RED);
            text = String.valueOf(redoChar++);
            point2Coord(point);

            point.offset((int) _realCellSize / 2, (int) _realCellSize / 2);
            float textY = point.y + (fm.descent - fm.ascent) / 2 - fm.descent;
            canvas.drawText(text, point.x, textY, numberPaint);
        }

        if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    private String moveToMark(int move) {
        if (move == EMPTY) {
            return ".";
        } else if (move == BLACK) {
            return "X";
        } else if (move == WHITE) {
            return "O";
        } else {
            return ".";
        }
    }

    private void eraseCrossCursor(
            final SurfaceHolder pSurfaceHolder,
            final Rect pCrossCursorRect
    ) {
        Canvas canvas = pSurfaceHolder.lockCanvas(pCrossCursorRect);
        if (canvas != null) {
            canvas.drawBitmap(_boardBitmap, pCrossCursorRect, pCrossCursorRect,
                    _xferModePaintSrc);
            pSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private
    int _numDrawCrossCursorInvocations = 0;

    private void drawCrossCursor(
            final SurfaceHolder pSurfaceHolder,
            final Position pPoint,
            final boolean pHorizontal,
            final int pWidth,
            final int pHeight
    ) {
        final Rect crossRect =
                getCrossCursorRect(pPoint, pHorizontal, pWidth, pHeight);
        final Canvas canvas = pSurfaceHolder.lockCanvas(crossRect);
        if (canvas != null) {
            final Rect clipBounds = canvas.getClipBounds();
            if (clipBounds.top == 0 && clipBounds.left == 0
                    && _numDrawCrossCursorInvocations <= 1) {
                canvas.drawBitmap(_boardBitmap,
                        crossRect, crossRect, _xferModePaintSrc);
                pSurfaceHolder.unlockCanvasAndPost(canvas);
                _numDrawCrossCursorInvocations++;
                drawCrossCursor(pSurfaceHolder, pPoint,
                        pHorizontal, pWidth, pHeight);
                return;
            }
            _numDrawCrossCursorInvocations = 0;
            canvas.drawLine(crossRect.left, crossRect.top,
                    pHorizontal ? crossRect.right : crossRect.left,
                    pHorizontal ? crossRect.top : crossRect.bottom,
                    _crossCursporPaint);
            pSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private Rect getCrossCursorRect(
            final Position pPoint,
            final boolean pHorizontal,
            final int pWitdh,
            final int pHeight
    ) {
        if (pHorizontal) {
            final int y = pPoint.y + (int) _realCellSize / 2 - 1;
            return new Rect(0, y, pWitdh, y + _crossCursorStrokeWidth);
        } else {
            final int x = pPoint.x + (int) _realCellSize / 2 - 1;
            return new Rect(x, 0, x + _crossCursorStrokeWidth, pHeight);
        }
    }

    private void drawStones(
            final Canvas pCanvas,
            final boolean pTerritory,
            final boolean pIsCoord,
            final boolean pPaintAlpha
    ) {
        //绘子
        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                int move = pointState[y * _boardSize + x];
                if (move != 0) {
                    boolean isBlack = move == BLACK ? true : false;
                    Position curMove = new Position(x, y, move);
                    drawStone(pCanvas, curMove, isBlack, false,
                            pTerritory, pIsCoord, pPaintAlpha, false);
                }
            }
        }
    }

    private void drawStone(
            final Canvas pCanvas,
            final Position pPoint,
            final boolean pBlack,
            final boolean pMark,
            final boolean pTerritory,
            final boolean pIsCoord,
            final boolean pPaintAlpha,
            final boolean pIsJudge
    ) {
//      if (pPoint instanceof GameInfo.Passed)
//      {
//          return;
//      }
        if (!pIsCoord) {
            point2Coord(pPoint);
        }
        final int x = pPoint.x, y = pPoint.y;
        final boolean isZoom = _isZoom;
        if (pMark) {
            if (!curPosition.isPass) {
                pCanvas.drawBitmap(pBlack ?
                                (isZoom ? _blackStoneZoomMarkerBitmap : _blackStoneMarkerBitmap)
                                : (isZoom ? _whiteStoneZoomMarkerBitmap : _lastMoveMarkerBitmap),
                        x, y, _xferModePaintAtop);
            }
        } else {
            pCanvas.drawBitmap(
                    pBlack ? (pIsJudge ? _blackStoneJudgeBitmap : (pTerritory ? _blackTerritoryBitmap :
                            (isZoom ? _blackStoneZoomBitmap : _blackStoneBitmap)))
                            : (pIsJudge ? _whiteStoneJudgeBitmap : (pTerritory ? _whiteTerritoryBitmap :
                            (isZoom ? _whiteStoneZoomBitmap : _whiteStoneBitmap))),
                    x, y, pPaintAlpha ? _xferModePaintAtopAlpha : _xferModePaintAtop);
        }
    }

    private Rect createCellRect(
            final Position pStone
    ) {
        point2Coord(pStone);
        final int cellWidth = (int) _realCellSize, x = pStone.x, y = pStone.y;
        return new Rect(x, y, x + cellWidth, y + cellWidth);
    }

    private void point2CoordCenter(
            final Position pPoint
    ) {
//      if (pPoint instanceof GameInfo.Passed)
//      {
//          return;
//      }
        final float cellWidth = _activeCellWidth;
        final float cellHeight = _activeCellHeight;
        pPoint.set(
                (int) ((pPoint.x - _beginX) * cellWidth + _boardMarginXOffset),
                (int) ((pPoint.y - _beginY) * cellHeight + _boardMarginYOffset));
        pPoint.x += cellWidth / 2;
        pPoint.y += cellHeight / 2;
    }


    private void point2Coord(
            final Position pPoint
    ) {
//      if (pPoint instanceof GameInfo.Passed)
//      {
//          return;
//      }
        final float cellWidth = _activeCellWidth;
        final float cellHeight = _activeCellHeight;
        pPoint.set(
                (int) ((pPoint.x - _beginX) * cellWidth + _boardMarginXOffset),
                (int) ((pPoint.y - _beginY) * cellHeight + _boardMarginYOffset));
    }

    private List<Position> points2Coords(
            final List<Position> pPoints
    ) {
        for (final Position point : pPoints) {
            point2Coord(point);
        }
        return pPoints;
    }

    void setLegalMoves(
            final List<Position> pPoints
    ) {
        final Set<Position> legalMoves = _legalMoves;
        legalMoves.clear();
        if (pPoints != null) {
            legalMoves.addAll(pPoints);
        }
    }

    void moveStone(
            final int pDiffX,
            final int pDiffY
    ) {
        _gestureListener.moveStoneIncrementally(pDiffX, pDiffY);
    }

    void showKeyUpHint() {
        final BoardGestureListener gestureListener = _gestureListener;
        if (!gestureListener._interactionLocked) {
//          gestureListener.showHint ();
        }
    }

    public void lockScreen(
            final boolean pLock
    ) {
        _gestureListener._interactionLocked = pLock;
    }

    void setZoom(
            final boolean pZoom
    ) {
        _isZoom = pZoom;
    }

    public boolean onTouchEvent(
            final MotionEvent pEvent
    ) {
        _gestureDetector.onTouchEvent(pEvent);
        if (pEvent.getAction() == MotionEvent.ACTION_UP) {
            _gestureListener.onUp();
        }
        return true;
    }

    private int initWhite = 0;

    @Override
    protected void onMeasure(
            final int pWidthMeasureSpec,
            final int pHeightMeasureSpec
    ) {
        final int width =
                resolveSize(getSuggestedMinimumWidth(), pWidthMeasureSpec);
        if (width != initWhite) {
            initWhite = width - boardMargin;
        }
        if (boardStyle == 2) {
            setMeasuredDimension(initWhite, initWhite - initWhite / 8);
        } else {
            setMeasuredDimension(width, width);
        }
    }

    boolean interactionLocked() {
        return _gestureListener._interactionLocked;
    }

    //落子
    public void nextMove() {
        setZoom(false);
        //
        moveingFlag = false;
        //下一次落子颜色取反
        if (!isSingleColor) {
            playerBlackMoves = !((isTryDown ? tmpTotalMoveLise.c : curPosition.c) == BLACK ? true : false);
        }
        //重绘棋盘
        drawBoard(false);
        lockScreen(false);
        //回调
        if (mOnEndOfGame != null) {
            mOnEndOfGame.onBoardViewSingle();
        }
    }

    //初始化棋盘绘制
    public void drawBoard(boolean pInit) {
        if (pInit) {
            initBoard(_boardSize, _boardXsize, _boardYsize);
        }
        drawBoard();
    }

    //线程等待标记
    boolean waitFlag = true;

    //创建新棋盘
    public void newGame(final int bz, final boolean isLock) {
        //todo 若在acticity中直接调用此方法,出现异常
        // 因为SurfaceView还在测量控件大小,我们调用initview时还未测量完毕
        // 这里创建一个子线程,循环判断SurfaceView是否加载完毕
        // 若加载完成则初始化棋盘并关闭这个线程
        waitFlag = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作
                while (waitFlag) {
                    if (loadFlag) {//SurfaceView加载完毕
                        _boardSize = bz;
                        _gameInfo = new GameInfo();
                        _gameInfo.headInfo.put("SZ", bz + "");
                        //试下功能如果处于开启状态则关闭
//                        endDown();
                        //初始化棋子数组
                        pointState = new Integer[_boardSize * _boardSize];

                        for (int i = 0; i < pointState.length; i++) {
                            pointState[i] = EMPTY;
                        }
                        //棋子root节点初始化
                        totalMoveList = new Position();
                        totalMoveList.curState = pointState.clone();
                        //当前节点赋值--root
                        curPosition = totalMoveList;
                        //棋盘属性初始化
                        boardSetting();
                        //设置棋盘星位
                        storeMarkers(_boardSize);
                        //计算棋盘样式
                        calcBoardStyle();
                        setZoom(false);
                        drawBoard(true);
                        lockScreen(isLock);
                        //关闭线程
                        waitFlag = false;
                        //回调
                        if (mOnEndOfGame != null) {
                            if (newGameIsCallBack) {
                                mOnEndOfGame.onBoardNewGame();
                            }
                        }
                        if (!newGameIsCallBack) newGameIsCallBack = true;
                    }
                }
            }
        });
        thread.start(); //启动线程
    }

    //创建新棋盘-－并且加载sgf
    public void newGame(final String sgf, final boolean boardLock, final boolean isInitBoard, final int initMove) {
        //todo 若在acticity中直接调用此方法,出现异常
        // 因为SurfaceView还在测量控件大小,我们调用initview时还未测量完毕
        // 这里创建一个子线程,循环判断SurfaceView是否加载完毕
        // 若加载完成则初始化棋盘并关闭这个线程
        waitFlag = true;
        //判断sgf是否为空
        if (sgf == null && sgf.isEmpty()) {
            newGame(19, boardLock);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作
                while (waitFlag) {
                    if (loadFlag) {//SurfaceView加载完毕
                        SystemClock.sleep(100);
                        //试下功能如果处于开启状态则关闭
//                        endDown();
                        _gameInfo = new GameInfo();
                        //棋盘属性初始化
                        boardSetting();
                        //加载sgf
                        loadSgf(sgf);
                        //设置棋盘星位
                        storeMarkers(_boardSize);
                        setZoom(false);
                        //关闭线程
                        waitFlag = false;
                        //
                        if (initMove != -1) {
                            if (isInitBoard) {
                                initBoard(_boardSize, _boardXsize, _boardYsize);
                            }
                            goTo(initMove);
                        } else {
                            drawBoard(isInitBoard);
                        }
                        lockScreen(boardLock);
                        //回调
                        if (mOnEndOfGame != null) {
                            mOnEndOfGame.onBoardNewGame();
                        }
                    }
                }
            }
        });
        thread.start(); //启动线程
    }

    /**
     * 创建棋盘,并回退n步
     *
     * @param sgf
     * @param boardLock
     * @param isInitBoard
     * @param backMove
     */
    public void newGameBackMove(final String sgf, final boolean boardLock, final boolean isInitBoard, final int backMove) {
        //todo 若在acticity中直接调用此方法,出现异常
        // 因为SurfaceView还在测量控件大小,我们调用initview时还未测量完毕
        // 这里创建一个子线程,循环判断SurfaceView是否加载完毕
        // 若加载完成则初始化棋盘并关闭这个线程
        waitFlag = true;
        //判断sgf是否为空
        if (sgf == null && sgf.isEmpty()) {
            newGame(19, boardLock);
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作
                while (waitFlag) {
                    if (loadFlag) {//SurfaceView加载完毕
                        SystemClock.sleep(100);
                        //试下功能如果处于开启状态则关闭
//                        endDown();
                        _gameInfo = new GameInfo();
                        //棋盘属性初始化
                        boardSetting();
                        //加载sgf
                        loadSgf(sgf);
                        //设置棋盘星位
                        storeMarkers(_boardSize);
                        setZoom(false);
                        //关闭线程
                        waitFlag = false;
                        //
                        if (backMove != -1) {
                            if (isInitBoard) {
                                initBoard(_boardSize, _boardXsize, _boardYsize);
                            }
                            goTo(maxmove - backMove);
                        } else {
                            drawBoard(isInitBoard);
                        }
                        lockScreen(boardLock);
                        //回调
                        if (mOnEndOfGame != null) {
                            mOnEndOfGame.onBoardNewGame();
                        }
                    }
                }
            }
        });
        thread.start(); //启动线程
    }

    //重绘棋盘
    public void drawBoard() {
        //当前盘面棋子状态更新
        //TODO bug
        if ((isTryDown ? tmpTotalMoveLise.curState : curPosition.curState) != null) {
            pointState = isTryDown ? tmpTotalMoveLise.curState.clone() : curPosition.curState.clone();
            //绘制棋盘
//            if ((isTryDown ? tmpTotalMoveLise.childArray.size() : curPosition.childArray.size()) > 1) {  //绘制分支
//                drawBoard(isTryDown ? tmpTotalMoveLise.childArray : curPosition.childArray, true, moveCount, !playerBlackMoves, moveType);
//            } else {  //不绘制分支
//                drawBoard(null, true, moveCount, !playerBlackMoves, moveType);
//            }
            if (isTryDown || isBranch || isAddBranch || boardStyle == 2) {
                moveType = MoveType.LB;
                moveCount = 999;
            } else if (isGuess) {
                moveType = moveType;
                moveCount = moveCount;
            } else if (handStyleFlag) {
                moveType = MoveType.LB;
                moveCount = 10;
            } else {
                //手数标识样式
                int handStyle = StringUtils.get(activity, "hand_type", 0);
                if (handStyle == 0) {
                    moveType = MoveType.TR;
                } else if (handStyle == 1) {
                    moveType = MoveType.LB;
                    moveCount = 999;
                } else if (handStyle == 2) {
                    moveType = MoveType.LB;
                    moveCount = 1;
                }
            }
            drawBoard(null, true, moveCount, !playerBlackMoves, moveType);
        }
    }


    //初始化星位坐标
    void storeMarkers(int boardSize) {

        _markers.clear();
        //判断棋盘大小返回不同星位坐标
        switch (boardSize) {
            case 9:
                _markers.add(new Position(2, 2));
                _markers.add(new Position(6, 2));
                _markers.add(new Position(4, 4));
                _markers.add(new Position(2, 6));
                _markers.add(new Position(6, 6));
                break;
            case 13:
                _markers.add(new Position(3, 3));
                _markers.add(new Position(9, 3));
                _markers.add(new Position(6, 6));
                _markers.add(new Position(3, 9));
                _markers.add(new Position(9, 9));
                break;
            case 19:
                _markers.add(new Position(3, 3));
                _markers.add(new Position(9, 3));
                _markers.add(new Position(15, 3));
                _markers.add(new Position(3, 9));
                _markers.add(new Position(9, 9));
                _markers.add(new Position(15, 9));
                _markers.add(new Position(3, 15));
                _markers.add(new Position(9, 15));
                _markers.add(new Position(15, 15));
                break;
        }
    }

    //确认落子校验
    private boolean checkMove(Position position, boolean addStone) {
        //落子所在数组中的下标
        int x = position.x;
        int y = position.y;
        int address = y * _boardSize + x;
        //判断是否超出边界
        if (x > 0 || x < _boardSize - 1 || y < _boardSize - 1 || y > 0) {  //未超出边界,设置棋盘可落子
            downFlag = true;
        } else if (pointState[address] == EMPTY) {    //所落子的位置是空的,设置棋盘可落子
            downFlag = true;
        }
        //判断当前点不可落
        if (!downFlag) {
            return false;
        }
        // 单提的循环禁着点_2
        if (iForbit != address) {
            iForbit = -1;
        }
        //判断当前点是否可落
        if (pointState[address] == EMPTY) {
            QiZi qizi = new QiZi(true, _boardSize);
            //吧该位置的点状态设置为落子
            pointState[address] = position.c;

            // 判断当前是否存在死子;
            boolean bDead = false;
            Set<Integer> differs = qizi.differQiZiArray(pointState, address);
            for (Iterator it = differs.iterator(); it.hasNext(); ) {
                int target = (Integer) it.next();
                int size = 0;
                if (pointState[target] != EMPTY
                        && qizi.isDeadQiZi(pointState, target)) {
                    Integer[] deads = qizi.relationQiZi(pointState, target);
                    for (int i = 0; i < deads.length; i++) {
                        if (pointState[deads[i]] == BLACK) {
                            blackDeadCount++;
                        } else if (pointState[deads[i]] == WHITE) {
                            whiteDeadCount++;
                        }
                    }
                    pointState = qizi.handleDead(pointState, target);
                    size = qizi.relationQiZi(pointState, target).length;
                    // 单提的循环禁着点_1
                    if (size == 1) {
                        if (pointState[address] == BLACK) {
                            pointState[target] = WHITE;
                        } else {
                            pointState[target] = BLACK;
                        }
                        if (qizi.isDeadQiZi(pointState, address) && qizi.relationQiZi(pointState, address).length == 1) {
                            iForbit = target;
                        }
                        pointState[target] = EMPTY;
                    }
                    bDead = true;
                }
            }
            // 自提的禁着点
            if (bDead == false) {
                if (qizi.isDeadQiZi(pointState, address)) {
                    qizi.clear();
                    pointState[address] = EMPTY;
                    return false;
                }
            }

            //判断是否根节点
            if (totalMoveList != null) {
                boolean isBranch = false;
                //当前手数增加
                if (!isAddBranch) {   //分支不增加总手数
                    maxmove++;
                }
                currentMove++;
                //判断当前操作是否是选择已有分支

                //判断当前操作是否是选择已有分支
                if ((isTryDown ? tmpTotalMoveLise.childArray.size() : curPosition.childArray.size()) >= 1) { //存在分支
                    int index = -1;
                    for (Position p : (isTryDown ? tmpTotalMoveLise.childArray : curPosition.childArray)) {
                        index++;
                        if (p.x == position.x && p.y == position.y && p.c == position.c) {
                            isBranch = true;
                            break;
                        }
                    }
                    if (isBranch) {
                        if (isTryDown) {
                            //选中的分支下标
                            tmpTotalMoveLise.selectRoot = index;
                            //当前子赋值
                            tmpTotalMoveLise = tmpTotalMoveLise.childArray.get(index);
                            tmpTotalMoveLise.move = currentMove;
                            //判断数组是否为空,为空更新
                            if (tmpTotalMoveLise.curState == null) {
                                tmpTotalMoveLise.curState = pointState;
                            }
                        } else {
                            //选中的分支下标
                            curPosition.selectRoot = index;
                            //当前子赋值
                            curPosition = curPosition.childArray.get(index);
                            curPosition.move = currentMove;
                            curPosition.curState = pointState;
                            //判断数组是否为空,为空更新
                            if (curPosition.curState == null) {
                                curPosition.curState = pointState;
                            }
                        }
                    }
                }
                if (!isBranch) {
                    if (isTryDown) {
                        //分之中落子
                        tmpTotalMoveLise.childArray.clear();
                        tmpTotalMoveLise.childArray.add(new Position(position, pointState.clone(), currentMove));
                        //选中的分支下标
                        tmpTotalMoveLise.selectRoot = tmpTotalMoveLise.childArray.size() - 1;
                        //父节点赋值
                        tmpTotalMoveLise.childArray.get(tmpTotalMoveLise.selectRoot).parent = tmpTotalMoveLise;
                        //当前子赋值
                        tmpTotalMoveLise = tmpTotalMoveLise.childArray.get(tmpTotalMoveLise.selectRoot);
                        //每一手提子数
                        tmpTotalMoveLise.blackDeadCount = blackDeadCount;
                        tmpTotalMoveLise.whiteDeadCount = whiteDeadCount;

                    } else {
                        if (addStone) {
                            Position newNode = new Position(position, pointState.clone(), currentMove);
                            Position node = curPosition.childArray.get(0);
                            node.parent = newNode;
                            newNode.parent = curPosition;
                            newNode.childArray.add(node);
                            curPosition.childArray.set(0, newNode);
                            curPosition = newNode;
                            curPosition.blackDeadCount = blackDeadCount;
                            curPosition.whiteDeadCount = whiteDeadCount;
                        } else {
                            //录入分支时从第零手落子,先清空之前落的分支
                            if (drawMarkIndex == curPosition.move && curPosition.childArray.size() > addBranchTmpCount) {
                                curPosition.childArray.remove(curPosition.childArray.size() - 1);
                            }
                            //分之中落子
                            curPosition.childArray.add(new Position(position, pointState.clone(), currentMove));
                            //选中的分支下标
                            curPosition.selectRoot = curPosition.childArray.size() - 1;
                            //父节点赋值
                            curPosition.childArray.get(curPosition.selectRoot).parent = curPosition;
                            //当前子赋值
                            curPosition = curPosition.childArray.get(curPosition.selectRoot);
                            //每一手提子数
                            curPosition.blackDeadCount = blackDeadCount;
                            curPosition.whiteDeadCount = whiteDeadCount;
                            branchMoveCount += 1;
                        }
                    }
                }
                //播放音效
                if (isSound) {
                    if (bDead) {
                        playSound(2);
                    } else {
                        playSound(1);
                    }
                }
            }
        }
        return true;
    }

    //落子中的校验,判断当前点是否可以落
    public boolean checkMoving(Position position) {
        //落子所在数组中的下标
        int x = position.x;
        int y = position.y;
        int address = y * _boardSize + x;
        //判断是否超出边界
        if (x < 0 || x > _boardSize - 1 || y > _boardSize - 1 || y < 0) {
            return false;
        } else if (pointState[address] != EMPTY) {
            return false;
        }
        // 单提的循环禁着点_2
        if (iForbit == address) {
            return false;
        }

        //判断当前点是否可落
        if (pointState[address] == EMPTY) {
            QiZi qizi = new QiZi(true, _boardSize);
            //吧该位置的点状态设置为落子
            pointState[address] = position.c;

            // 判断当前是否存在死子;
            boolean bDead = false;
            Set<Integer> differs = qizi.differQiZiArray(pointState, address);
            for (Iterator it = differs.iterator(); it.hasNext(); ) {
                int target = (Integer) it.next();
                if (pointState[target] != EMPTY
                        && qizi.isDeadQiZi(pointState, target)) {
                    bDead = true;
                }
            }
            // 自提的禁着点
            if (bDead == false) {
                if (qizi.isDeadQiZi(pointState, address)) {
                    qizi.clear();
                    pointState[address] = EMPTY;
                    return false;
                }
            }
            pointState[address] = EMPTY;
        }
        return true;
    }

    //回退
    public boolean backMove(int num) {
        if (isJudge != 0 || waitFlag || currentMove == 0) {
            return false;
        }
        int backCount = 0;
        if (isTryDown) {
            try {
                //显示等待框
//                showLoadingDialog1(null, false, null, true);
                //更新提子数
                blackDeadCount = tmpTotalMoveLise.blackDeadCount;
                whiteDeadCount = tmpTotalMoveLise.whiteDeadCount;
                for (int i = 0; i < num; i++) {
                    //parent为空,是root节点,禁止回退
                    if (tmpTotalMoveLise.parent == null) {
                        return false;
                    }
                    backCount++;
                    updateState(tmpTotalMoveLise);
                    tmpTotalMoveLise = tmpTotalMoveLise.parent;
                    //判断下一次落子颜色
                    if (!isSingleColor) {
                        if (tmpTotalMoveLise.parent == null) {
                            playerBlackMoves = tmpTotalMoveLise.childArray.get(tmpTotalMoveLise.selectRoot).c == BLACK ? true : false;
                        } else {
                            playerBlackMoves = tmpTotalMoveLise.c == BLACK ? false : true;
                        }
                    }
                }
                return true;
            } finally {
                currentMove = tmpTotalMoveLise.move;
                drawBoard();
                //播放音效
                if (isSound && backCount > 0) {
                    playSound(1);
                }
                if (mOnEndOfGame != null) {
                    mOnEndOfGame.onNextOrBack(false);
                }
                //关闭等待框
//                hideLodingDialog1();
            }
        } else {
            try {
                //显示等待框
//                showLoadingDialog1(null, false, null, true);
                //更新提子数
                blackDeadCount = curPosition.blackDeadCount;
                whiteDeadCount = curPosition.whiteDeadCount;
                for (int i = 0; i < num; i++) {
                    //parent为空,是root节点,禁止回退
                    if (curPosition.parent == null || curPosition.move == drawMarkIndex) {
                        return false;
                    }
                    backCount++;
                    updateState(curPosition);
                    curPosition = curPosition.parent;
                    //当前手数矫正,因为插入子造成了变化
                    curPosition.move = curPosition.childArray.get(0).move - 1;
                    currentMove = curPosition.move;
                    //判断下一次落子颜色
                    if (!isSingleColor) {
                        if (curPosition.parent == null) {
                            playerBlackMoves = curPosition.childArray.get(curPosition.selectRoot).c == BLACK ? true : false;
                        } else {
                            playerBlackMoves = curPosition.c == BLACK ? false : true;
                        }
                    }
                }
                return true;
            } finally {
                drawBoard();
                //播放音效
                if (isSound && backCount > 0) {
                    playSound(1);
                }
                if (mOnEndOfGame != null) {
                    mOnEndOfGame.onNextOrBack(false);
                }
                //关闭等待框
//                hideLodingDialog1();
            }
        }
    }

    //前进
    public boolean nextMove(int num) {
        int nextCount = 0;
        if (isJudge != 0 || waitFlag) {
            return false;
        }
        if (isTryDown) {
            try {
                //显示等待框
//                showLoadingDialog1(null, false, null, true);
                //更新提子数
                blackDeadCount = tmpTotalMoveLise.blackDeadCount;
                whiteDeadCount = tmpTotalMoveLise.whiteDeadCount;
                //判断没有下一子
                for (int i = 0; i < num; i++) {
                    if (tmpTotalMoveLise.childArray.size() == 0) {
                        return false;
                    }
                    tmpTotalMoveLise = tmpTotalMoveLise.childArray.get(tmpTotalMoveLise.selectRoot);
                    if (tmpTotalMoveLise.isPass) {
                        tmpTotalMoveLise.c = tmpTotalMoveLise.parent.c;
                        tmpTotalMoveLise.x = tmpTotalMoveLise.parent.x;
                        tmpTotalMoveLise.y = tmpTotalMoveLise.parent.y;
                    }
                    //判断下一次落子颜色
                    if (!isSingleColor) {
                        if (tmpTotalMoveLise.c == BLACK) {
                            playerBlackMoves = false;
                        } else {
                            playerBlackMoves = true;
                        }
                    }
                    nextCount++;
                    updateState(tmpTotalMoveLise);
                }
                return true;
            } finally {
                currentMove = tmpTotalMoveLise.move;
                drawBoard();
                //播放音效
                if (isSound && nextCount > 0) {
                    playSound(1);
                }
                if (mOnEndOfGame != null) {
                    mOnEndOfGame.onNextOrBack(true);
                }
                //关闭等待框
//                hideLodingDialog1();
            }
        } else {
            try {
                //显示等待框
//                showLoadingDialog1(null, false, null, true);
                //更新提子数
                blackDeadCount = curPosition.blackDeadCount;
                whiteDeadCount = curPosition.whiteDeadCount;
                //判断没有下一子
                for (int i = 0; i < num; i++) {
                    if (curPosition.childArray.size() == 0) {
                        return false;
                    }
                    curPosition = curPosition.childArray.get(curPosition.selectRoot);
                    if (curPosition.isPass) {
                        curPosition.c = curPosition.parent.c;
                        curPosition.x = curPosition.parent.x;
                        curPosition.y = curPosition.parent.y;
                    }
                    //判断下一次落子颜色
                    if (!isSingleColor) {
                        if (curPosition.c == BLACK) {
                            playerBlackMoves = false;
                        } else {
                            playerBlackMoves = true;
                        }
                    }
                    nextCount++;
                    //判断是否有数组 没有则调用算法解析
                    updateState(curPosition);
                    //当前手数矫正,因为插入子造成了变化
                    curPosition.move = curPosition.parent.move + 1;
                    currentMove = curPosition.move;
                }
                return true;
            } finally {
                currentMove = curPosition.move;
                drawBoard();
                //播放音效
                if (isSound && nextCount > 0) {
                    playSound(1);
                }
                if (mOnEndOfGame != null) {
                    mOnEndOfGame.onNextOrBack(true);
                }
                //关闭等待框
//                hideLodingDialog1();
            }
        }
    }

    //计算当前分支下总手数
    private int getCurrentNodeCount() {
        Position tmpPosition = totalMoveList;
        int count = 0;
        while (true) {
            if (tmpPosition.selectRoot != -1) {
                tmpPosition = tmpPosition.childArray.get(tmpPosition.selectRoot);
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * 查找当前子之前有多少手
     *
     * @return
     */
    private int getParentNodeCount() {
        Position tmpPosition = isTryDown ? tmpTotalMoveLise : curPosition;
        int count = 0;
        while (true) {
            if (tmpPosition.parent != null) {
                tmpPosition = tmpPosition.parent;
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    //设置activity主体,用来交互
    public void setActicity(Activity activity) {
        this.activity = activity;
        //预加载落子音效文件
        soundPool = new SoundPool(4, AudioManager.STREAM_SYSTEM, 100);
        deadMoreSound = new SoundPool(4, AudioManager.STREAM_SYSTEM, 100);
        soundPool.load(activity, R.raw.move, 1);
        deadMoreSound.load(activity, R.raw.deadone, 1);
        if (windowOn) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //设置音效开关
    public void setSound(boolean isSound) {
        this.isSound = isSound;
    }

    //初始化棋盘每一手的变化--解析sgf
    public void parseSgfNode(Position parsePosition) {
        boardSetting();
        totalMoveList = parsePosition;
        curPosition = totalMoveList;
        while (true) {
            if (curPosition.parent == null) { //根节点
                pointState = new Integer[_boardSize * _boardSize];
                for (int i = 0; i < pointState.length; i++) {
                    pointState[i] = EMPTY;
                }
                curPosition.curState = pointState.clone();
                //初始棋子
                for (Position setup : curPosition.setup) {
                    updateState(setup);
                }
                if (curPosition.childArray.size() == 0 || curPosition.selectRoot == -1) {
                    break;
                }
                curPosition = curPosition.childArray.get(curPosition.selectRoot);
                playerBlackMoves = true;
            } else if (curPosition.selectRoot != -1 || curPosition.childArray.size() == 0) {
                if (curPosition.isPass) {
                    //总手数增加
                    maxmove++;
                    //当前手数增加
                    currentMove++;
                    curPosition.move = currentMove;
                    curPosition.whiteDeadCount = curPosition.parent.whiteDeadCount;
                    curPosition.blackDeadCount = curPosition.parent.blackDeadCount;
                    curPosition.curState = curPosition.parent.curState.clone();
                } else {
                    //总手数增加
                    maxmove++;
                    //当前手数增加
                    currentMove++;
                    updateState(curPosition);
                }
                if (curPosition.childArray.size() > 0) {
                    curPosition = curPosition.childArray.get(curPosition.selectRoot);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        //第一手颜色
        if (boardStyle == 2) {       //死活题
            curPosition = totalMoveList;
            pointState = curPosition.curState.clone();
            if (curPosition.childArray.size() > 0) {
                playerBlackMoves = curPosition.childArray.get(curPosition.selectRoot).c == BLACK ? true : false;
            }
        } else {
            playerBlackMoves = !(curPosition.c == BLACK ? true : false);
        }
        //当前手
        currentMove = curPosition.move;
    }

    //从sgf中加载内容
    public void loadSgf(String sgf) {
        try {
            if (!loadFlag) {
                return;
            }
            Position parsePosition = GameUtil.parseSgf(sgf, _gameInfo);
            //棋盘大小
            if (_gameInfo.headInfo.containsKey("SZ")) {
                _boardSize = StringUtils.toInt(_gameInfo.headInfo.get("SZ"), 19);
            } else {
                _boardSize = 19;
            }
            //计算棋盘样式
            calcBoardStyle();
            parseSgfNode(parsePosition);
        } catch (Exception e) {
            pointState = new Integer[_boardSize * _boardSize];
            for (int i = 0; i < pointState.length; i++) {
                pointState[i] = EMPTY;
            }
            curPosition.curState = pointState;
        }
    }

    /**
     * 计算棋盘样式
     */
    private void calcBoardStyle() {
        //死活题不显示手数
        try {
            if (_gameInfo.headInfo.containsKey("VW")) {
                String view = _gameInfo.headInfo.get("VW");
                //不显示分支
                showBranch = false;
                String[] views = view.split(":");
                int x1 = GameUtil.toNum(views[0], 0);
                int y1 = GameUtil.toNum(views[0], 1);
                int x2 = GameUtil.toNum(views[1], 0);
                int y2 = GameUtil.toNum(views[1], 1);
                if (x1 > 0) x1 -= 1;
                if (y1 > 0) y1 -= 1;
                //绘制开始坐标
                _beginX = y1;
                _beginY = x1;
                //绘制的大小1~19
                _boardXsize = x2 - y1 + 1;
                _boardYsize = y2 - x1 + 1;
            } else {
                //绘制开始坐标
                _beginX = 0;
                _beginY = 0;
                //绘制的大小1~19
                _boardXsize = _boardSize;
                _boardYsize = _boardSize;
            }
        } catch (Exception ex) {
            //绘制开始坐标
            _beginX = 0;
            _beginY = 0;
            //绘制的大小1~19
            _boardXsize = _boardSize;
            _boardYsize = _boardSize;
        }
    }

    //触发落子算法,更新棋子数组
    public void updateState(Position position) {
        //落子所在数组中的下标
        int x = position.x;
        int y = position.y;
        int address = y * _boardSize + x;
        if (x == -1 || y == -1) {
            if (isTryDown) {
                tmpTotalMoveLise.curState = pointState.clone();
            } else {
                curPosition.curState = pointState.clone();
                curPosition.move = currentMove;
                //每一手的提子数
                curPosition.whiteDeadCount = whiteDeadCount;
                curPosition.blackDeadCount = blackDeadCount;
            }
            return;
        }
        //判断是否超出边界
        if (x > 0 || x < _boardSize - 1 || y < _boardSize - 1 || y > 0) {  //未超出边界,设置棋盘可落子
            downFlag = true;
        } else if (pointState[address] == EMPTY) {    //所落子的位置是空的,设置棋盘可落子
            downFlag = true;
        }

        // 单提的循环禁着点_2
        if (iForbit != address) {
            iForbit = -1;
        }
        //判断当前点是否可落
//        if (pointState[address] == EMPTY) {
        if (true) {
            QiZi qizi = new QiZi(true, _boardSize);
            //吧该位置的点状态设置为落子
            pointState[address] = position.c;

            // 判断当前是否存在死子;
            boolean bDead = false;
            int deadNum = 0;
            Set<Integer> differs = qizi.differQiZiArray(pointState, address);
            for (Iterator it = differs.iterator(); it.hasNext(); ) {
                int target = (Integer) it.next();
                int size = 0;
                if (pointState[target] != EMPTY
                        && qizi.isDeadQiZi(pointState, target)) {
                    //提子记录
                    Integer[] deads = qizi.relationQiZi(pointState, target);
                    for (int i = 0; i < deads.length; i++) {
                        if (pointState[deads[i]] == BLACK) {
                            blackDeadCount++;
                        } else if (pointState[deads[i]] == WHITE) {
                            whiteDeadCount++;
                        }
                    }
                    pointState = qizi.handleDead(pointState, target);
                    size = qizi.relationQiZi(pointState, target).length;
                    // 单提的循环禁着点_1
                    if (size == 1) {
                        if (pointState[address] == BLACK) {
                            pointState[target] = WHITE;
                        } else {
                            pointState[target] = BLACK;
                        }
                        if (qizi.isDeadQiZi(pointState, address) && qizi.relationQiZi(pointState, address).length == 1) {
                            //死活题不考虑循环打劫
                            if (boardStyle != 2) {
                                iForbit = target;
                            }
                        }
                        pointState[target] = EMPTY;
                    }
                    bDead = true;
                }
            }
            // 自提的禁着点
            if (bDead == false) {
                if (qizi.isDeadQiZi(pointState, address)) {
                    qizi.clear();
                    pointState[address] = EMPTY;
                }
            }
            //更新当前手棋盘状态
            if (isTryDown) {
                tmpTotalMoveLise.curState = pointState.clone();
            } else {
                curPosition.curState = pointState.clone();
                curPosition.move = currentMove;
                //每一手的提子数
                curPosition.whiteDeadCount = whiteDeadCount;
                curPosition.blackDeadCount = blackDeadCount;
            }
        }
    }

    //落子音效播放
    private void playSound(int type) {
        switch (type) {
            case 1:
                soundPool.play(1, 1, 1, 0, 0, 1);
                break;
            case 2:
                deadMoreSound.play(1, 1, 1, 0, 0, 1);
                break;
        }
    }

    //设置手数样式
    public void setMoveType(MoveType moveType, int moveCount) {
        this.moveType = moveType;
        this.moveCount = moveCount;
        drawBoard();
    }

    //获取当前解说
    public String getComment() {
        String comment = "";
        try {
            comment = curPosition.moveInfo.get("C").replace("\n,", "\n");
        } catch (Exception ex) {
            comment = "";
        }
        return comment;
    }

    //试下
    public void tryDown() {
        if (isTryDown || isJudge != 0) {
            return;
        }
        isTryDown = true;
        //当前棋盘数据保存
        tmpTotalMoveLise = new Position();
        //当前手保存
        tmpCurMoveList = curPosition;
        //手数保存
        tmpCurrentMove = currentMove;
        //试下的子 手数从1开始
        currentMove = 0;

        tmpMaxMove = maxmove;
        //总手数从0开始
        maxmove = 0;
        //起始步设置
        tmpTotalMoveLise.curState = pointState.clone();
        tmpTotalMoveLise.parent = tmpCurMoveList.parent;
        tmpTotalMoveLise.x = tmpCurMoveList.x;
        tmpTotalMoveLise.y = tmpCurMoveList.y;
        tmpTotalMoveLise.c = curPosition.c;
        //重绘棋盘
        drawBoard();
    }

    //试下
    public void goBranch(String sgf) {
        if (isBranch) {
            return;
        }
        isBranch = true;
        //当前棋盘数据保存
        tmpTotalMoveLise = new Position();
        //当前手保存
        tmpCurMoveList = curPosition;
        //手数保存
        tmpCurrentMove = currentMove;
        tmpMaxMove = maxmove;

        newGame(sgf, false, false, -1);
    }

    //试下
    public void closeBranch() {
        if (!isBranch) {
            return;
        }
        isBranch = false;
        //手数恢复
        currentMove = tmpCurrentMove;
        curPosition = tmpCurMoveList;
        //下一手落子颜色更新
        playerBlackMoves = (isTryDown ? tmpTotalMoveLise.c : curPosition.c) == BLACK ? false : true;

        maxmove = tmpMaxMove;
        //重绘棋盘
        drawBoard();
    }

    //结束试下
    public void endDown() {
        if (!isTryDown || isJudge != 0) {
            return;
        }
        isTryDown = false;
        //手数恢复
        currentMove = tmpCurrentMove;
        curPosition = tmpCurMoveList;
        //下一手落子颜色更新
        playerBlackMoves = (isTryDown ? tmpTotalMoveLise.c : curPosition.c) == BLACK ? false : true;

        maxmove = tmpMaxMove;
        //重绘棋盘
        drawBoard();
    }

    /**
     * 添加分支
     */
    private int addBranchTmpCount = 0;
    private Position addBranchTmpPosition;
    //添加分支时落了几只
    private int branchMoveCount = 0;
    //录分支前,棋盘是否可落子
    private boolean tmpLock = false;

    public void addBranch(int move) {
        //试下中不允许添加分支
        if (isTryDown || isAddBranch) {
            return;
        }
        isAddBranch = true;
        addBranchTmpCount = curPosition.childArray.size();
        drawMarkIndex = currentMove;
        branchMoveCount = 0;
        addBranchTmpPosition = curPosition;
        //跳转到分支
        if (move > 0) {
            if (curPosition.childArray.size() > 1) {
                curPosition.selectRoot = move;
                nextMove(999);
            }
        }
        //棋盘解锁
        tmpLock = _gestureListener._interactionLocked;
        lockScreen(false);
        drawBoard();
    }

    /**
     * 删除当前分支
     */
    public void deleteBranch() {
        if (!isAddBranch || isTryDown) {
            return;
        }
        //从start位置删除分支
        Position node = totalMoveList;
        while (true) {
            if (node.move == drawMarkIndex) {
                if (addBranchTmpCount <= node.childArray.size()) {
                    //判断是否为空
                    if (node.childArray.get(node.selectRoot) != null) {
                        //删除当前分支
                        node.childArray.remove(node.selectRoot);
                    }
                }
                break;
            } else {
                if (node.childArray.size() > 0) {
                    node = node.childArray.get(node.selectRoot);
                } else {
                    break;
                }
            }
        }
        //分支状态变更
        isAddBranch = false;
        //手数开始位置从0开始
        drawMarkIndex = 0;
        addBranchTmpPosition = null;
        curPosition = node;
        //回到主线
        curPosition.selectRoot = 0;
        currentMove = node.move;
        //重绘
        lockScreen(tmpLock);
        drawBoard();
        //提示
        Toast.makeText(activity, "变化已删除", Toast.LENGTH_SHORT).show();
    }

    /**
     * 保存录入的分支
     */
    public void saveBranch(boolean isDrawDrak, boolean isSave) {
        if (!isAddBranch || isTryDown) {
            return;
        }
        isAddBranch = false;
        //跳转到录入分支时的手数
        int markIndex = drawMarkIndex;
        if (isDrawDrak) {
            drawMarkIndex = 0;
        } else {
            drawMarkIndex = 999;
        }
        //是否保存落的子
        if (!isSave) {
            deletePostion(branchMoveCount);
        }
        addBranchTmpPosition = null;
        goTo(markIndex);
        //回到主线
        curPosition.selectRoot = 0;
        lockScreen(tmpLock);
    }

    /**
     * 判断是否正在录入分支
     *
     * @return
     */
    public boolean getIsAddBranch() {
        return isAddBranch;
    }

    /**
     * 跳转到棋盘某一手
     *
     * @param move 跳转到第几手?
     */
    public void goTo(int move) {
        //判断需要向前走还是向后走
        int gotoMove = Math.abs(currentMove - move);
        if (move > currentMove) {
            nextMove(gotoMove);
        } else if (move < currentMove) {
            backMove(gotoMove);
        }
    }

    private void drawStones2(
            final Canvas pCanvas
    ) {
        boolean pIsCoord = false;
        //绘子
        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                int move = showState[y * _boardSize + x];
                Position curMove = new Position(x, y, move);
                switch (move) {
                    case BLACK:
                        drawStone(pCanvas, curMove, true, false,
                                false, pIsCoord, false, false);
                        break;
                    case WHITE:
                        drawStone(pCanvas, curMove, false, false,
                                false, pIsCoord, false, false);
                        break;
                    case WHITE_LITE:
                        drawStone(pCanvas, curMove, false, false,
                                false, pIsCoord, false, true);
                        break;
                    case BLACK_LITE:
                        drawStone(pCanvas, curMove, true, false,
                                false, pIsCoord, false, true);
                        break;
                    case BLACK_VIRTUAL:
                        drawStone(pCanvas, curMove, true, false,
                                false, pIsCoord, true, false);

                        drawStone(pCanvas, curMove, false, false,
                                false, !pIsCoord, false, true);
                        break;

                    case WHITE_VIRTUAL:
                        drawStone(pCanvas, curMove, false, false,
                                false, pIsCoord, true, false);

                        drawStone(pCanvas, curMove, true, false,
                                false, !pIsCoord, false, true);
                        break;
                }
            }
        }
    }

    public void judgeNew(Double[] pos) {
        //形势判断标记
        isJudge = 2;
        newJudgeState = pos;

        drawBoard();
    }


    public JudgeModel judge(String w, String b) {
        //形势判断标记
        isJudge = 1;

        JudgeModel judge = new JudgeModel();

        String[] ww = w.split(":");
        String[] bb = b.split(":");

        Integer[] tmpState = new Integer[_boardSize * _boardSize];
        showState = new Integer[_boardSize * _boardSize];
        for (int i = 0; i < tmpState.length; i++) {
            tmpState[i] = EMPTY;
        }

        //遍历白子结果
        for (int i = 0; i < ww.length; i++) {
            int x = GameUtil.toNum(ww[i], 0);
            int y = GameUtil.toNum(ww[i], 1);
            //棋子在数组中的位置
            if (x == -1 || y == -1) {
                continue;
            }
            int address = y * _boardSize + x;
            if (tmpState[address] == EMPTY) {
                tmpState[address] = WHITE;
            }
        }
        //遍历黑子结果
        for (int i = 0; i < bb.length; i++) {
            int x = GameUtil.toNum(bb[i], 0);
            int y = GameUtil.toNum(bb[i], 1);
            //棋子在数组中的位置
            if (x == -1 || y == -1) {
                continue;
            }
            int address = y * _boardSize + x;
            if (tmpState[address] == EMPTY) {
                tmpState[address] = BLACK;
            }
        }

        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                int address = y * _boardSize + x;
                if (tmpState[address] != 0 && pointState[address] == 0) {
                    if (tmpState[address] == BLACK) {
                        showState[address] = BLACK_LITE;
                        judge.setOffsetBlackLiteCount();
                    } else {
                        showState[address] = WHITE_LITE;
                        judge.setOffsetWhiteLiteCount();
                    }
                } else if (tmpState[address] != 0 && pointState[address] != 0 && tmpState[address] != pointState[address]) {
                    if (tmpState[address] == BLACK) {
                        showState[address] = WHITE_VIRTUAL;
                        judge.setOffsetBlackTackCount();
                    } else if (tmpState[address] == WHITE) {
                        showState[address] = BLACK_VIRTUAL;
                        judge.setOffsetWhiteTackCount();
                    }
                } else {
                    if (tmpState[address] == BLACK) {
                        judge.setOffsetBlack();
                    } else if (tmpState[address] == WHITE) {
                        judge.setOffsetWhite();
                    }
                    showState[address] = pointState[address];
                }
            }
        }
        drawBoard();
        //返回统计结果
        return judge;
    }

    //形势判断时微调吃子
    private boolean checkJudgeMove(Position position) {
        QiZi qizi = new QiZi(true, _boardSize);
        int address = position.y * _boardSize + position.x;
        //判断当前子的状态
        switch (showState[address]) {
            case EMPTY:
                break;
            case BLACK:
                Integer[] blackStates = qizi.relationQiZi(showState, address);
                for (int i = 0; i < blackStates.length; i++) {
                    showState[blackStates[i]] = BLACK_VIRTUAL;
                }
                break;
            case WHITE:
                Integer[] whiteState = qizi.relationQiZi(showState, address);
                for (int i = 0; i < whiteState.length; i++) {
                    showState[whiteState[i]] = WHITE_VIRTUAL;
                }
                break;
            case WHITE_LITE:
                showState[address] = WHITE_HIDDED;
                break;
            case BLACK_LITE:
                showState[address] = BLACK_HIDDED;
                break;
            case BLACK_VIRTUAL:
                //重新算
                break;
            case WHITE_VIRTUAL:
                //重新算
                break;
            case WHITE_HIDDED:
                showState[address] = WHITE_LITE;
                break;
            case BLACK_HIDDED:
                showState[address] = BLACK_LITE;
                break;
        }
        drawBoard();
        return false;
    }

    public void join() {
        QiZi qizi = new QiZi(true, 19);
        Integer[] states = qizi.relationQiZi(pointState, 196);
        int a = 1;
    }

    //棋盘属性初始化
    public void boardSetting() {
        currentMove = 0;
        maxmove = 0;
        iForbit = -1;
        isJudge = 0;
        isTryDown = false;
        isAddBranch = false;
        whiteDeadCount = 0;
        blackDeadCount = 0;
        drawMarkIndex = 0;
        isDapu = false;
        playerBlackMoves = true;
        isGuess = false;
        isSelect = false;
        //是否播放音效
        int moveSound = StringUtils.get(activity,"move_sound", 0);
        isSound = moveSound == 0 ? false : true;
        //页面常亮
        int screen = StringUtils.get(activity,"screen_lighton", 0);
        windowOn = screen == 0 ? false : true;
        //获取棋盘配置
        coordiNateStyle = StringUtils.get(activity,"chess_coordinate", 1);
    }

    //清空棋盘
    public void clearBoard() {
        newGame(_boardSize, false);
    }

    //只落白或只落黑
    public void singleColor(boolean isBLack) {
        isSingleColor = true;
        playerBlackMoves = isBLack;
    }

    //交换颜色落子
    public void changeColor() {
        isSingleColor = false;
        playerBlackMoves = !((isTryDown ? tmpTotalMoveLise.c : curPosition.c) == BLACK ? true : false);
    }

    //pass
    public void pass() {
        //分之中落子
        curPosition.childArray.add(new Position(curPosition, pointState.clone(), currentMove - 1));
        //选中的分支下标
        curPosition.selectRoot = curPosition.childArray.size() - 1;
        //父节点赋值
        curPosition.childArray.get(curPosition.selectRoot).parent = curPosition;
        curPosition.childArray.get(curPosition.selectRoot).isPass = true;
        //当前子赋值
        curPosition = curPosition.childArray.get(curPosition.selectRoot);
        //当前子颜色
        curPosition.c = curPosition.c == BLACK ? WHITE : BLACK;
        curPosition.whiteDeadCount = curPosition.parent.whiteDeadCount;
        curPosition.blackDeadCount = curPosition.parent.blackDeadCount;

        //当前手
        currentMove++;
        curPosition.move = currentMove;
        //
        maxmove++;
        //颜色
        iForbit = -1;
        playerBlackMoves = !((isTryDown ? tmpTotalMoveLise.c : curPosition.c) == BLACK ? true : false);
        if (mOnEndOfGame != null) {
            mOnEndOfGame.onBoardViewSingle();
        }
    }

    public void setOnBoardSingle(OnBoardViewSingleInterface xOnBoardSingle) {
        mOnEndOfGame = xOnBoardSingle;
    }

    /**
     * 获取当前手
     *
     * @return
     */
    public int getCurrMove() {
        if (isTryDown) {
            return tmpTotalMoveLise != null ? tmpTotalMoveLise.move : 0;
        } else {
            return curPosition != null ? curPosition.move : 0;
        }
    }

    //获取棋盘总手数
    public int getMaxMove() {
        return maxmove;
    }

    /**
     * 回退后落子——删除其他分支中的落子
     */
    public void playNewStone() {
        //判断当前是否在分支中,有则删除其他分支
        if (isTryDown) {
            return;
        }
        Position curPoint = getCurPosition();
        curPosition.parent.childArray.clear();
        curPosition.parent.childArray.add(curPoint);
        curPosition.parent.selectRoot = 0;
        maxmove = currentMove;
    }

    /**
     * 返回当前该落什么颜色的子
     *
     * @return 1黑 -1白
     */
    public int getCurColor() {
        return isTryDown ? tmpTotalMoveLise.c : curPosition.c;
    }

    /**
     * 返回当前该落什么颜色的子  死活题用
     *
     * @return true 黑 false 白
     */
    public boolean getGomissionCurColor() {
        return playerBlackMoves;
    }

    /**
     * 获取试下状态
     *
     * @return
     */
    public boolean getIsTryDown() {
        return isTryDown;
    }

    /**
     * 判断当前手是否pass
     *
     * @return
     */
    public boolean isPass() {
        return curPosition.isPass;
    }

    /**
     * 棋盘转换sgf
     *
     * @return
     */
    public String toSgf() {
        return GameUtil.toSgf(_gameInfo, totalMoveList, _boardSize);
    }

    /**
     * 棋盘转换sgf
     *
     * @return
     */
    public String toSimpleSgf() {
        return GameUtil.toSimpleSgf(_gameInfo, totalMoveList, _boardSize);
    }

    public String toSgfLastToFirst() {
        return GameUtil.toSgfLastToFirst(_gameInfo, isTryDown ? tmpTotalMoveLise : curPosition, _boardSize);
    }

    /**
     * 不包含分支的sgf
     *
     * @param writeHeader 是否写入头信息
     * @return
     */
    public String toSgfNoBranch(boolean writeHeader) {
        return GameUtil.toSgfNoBranch(_gameInfo, totalMoveList, _boardSize, writeHeader);
    }

    /**
     * 获取棋盘大小
     *
     * @return
     */
    public int getBoardSize() {
        return _boardSize;
    }

    /**
     * 开启或关闭打谱
     *
     * @param dapuFlag
     */
    public void openOrCloseDapu(boolean dapuFlag) {
        this.isDapu = dapuFlag;
    }

    /**
     * 获取当前手中的评论
     *
     * @return
     */
    public void setComment(String comment) {
        curPosition.moveInfo.put("C", comment);
    }

    /**
     * 关闭形式判断
     */
    public void closeJudge() {
        isJudge = 0;
        drawBoard();
    }

    /**
     * 设置头属性
     */
    public void setHeadInfo(String key, String value) {
        _gameInfo.headInfo.put(key, value);
    }

    /**
     * 落子
     *
     * @param x
     * @param y
     * @param c
     */
    public boolean playerMove(int x, int y, int c) {
        try {
            if (x == -1 && y == -1) {
                //分之中落子
                curPosition.childArray.add(new Position(curPosition, pointState.clone(), currentMove - 1));
                //选中的分支下标
                curPosition.selectRoot = curPosition.childArray.size() - 1;
                //父节点赋值
                curPosition.childArray.get(curPosition.selectRoot).parent = curPosition;
                curPosition.childArray.get(curPosition.selectRoot).isPass = true;
                //当前子赋值
                curPosition = curPosition.childArray.get(curPosition.selectRoot);
                //当前子颜色
                curPosition.c = curPosition.c == BLACK ? WHITE : BLACK;
                curPosition.whiteDeadCount = curPosition.parent.whiteDeadCount;
                curPosition.blackDeadCount = curPosition.parent.blackDeadCount;

                //当前手
                currentMove++;
                curPosition.move = currentMove;
                //
                maxmove++;
                return true;
            } else {
                Position move = new Position(x, y, c);
                if (checkMove(move, false)) {
                    setZoom(false);
//                    lockScreen(false);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            playerBlackMoves = c == BLACK ? false : true;
        }
    }

    /**
     * @return
     */
    public Position getCurPosition() {
        return isTryDown ? tmpTotalMoveLise : curPosition;
    }

    /**
     * 删除子
     *
     * @param deleteCount
     * @return
     */
    public boolean deletePostion(int deleteCount) {
        try {
            for (int i = 0; i < deleteCount; i++) {
                if (curPosition.parent != null) {
                    curPosition = curPosition.parent;
                    curPosition.childArray.clear();
                    currentMove = curPosition.move;
                    playerBlackMoves = !playerBlackMoves;
                    maxmove--;
                    blackDeadCount = curPosition.blackDeadCount;
                    whiteDeadCount = curPosition.whiteDeadCount;
                } else {
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (mOnEndOfGame != null) {
                mOnEndOfGame.onNextOrBack(false);
            }
        }
    }

    /**
     * 从棋盘上删除一个子,最后一手直接删除,棋盘中间则做处理
     *
     * @param isAll 是否删除后续节点
     */
    public boolean deletePositionNew(boolean isAll, boolean isBack) {
        //判断是否还有后续手
        if (curPosition.parent == null) {
            Toast.makeText(activity, "无子可删除", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            //判断删除几手
            if (isAll) {
                curPosition = curPosition.parent;
                curPosition.childArray.clear();
                curPosition.selectRoot = -1;
                currentMove = curPosition.move;
                maxmove = curPosition.move;
            } else {
                if (curPosition.childArray.size() > 0) {
                    for (Position item : curPosition.childArray) {
                        item.parent = curPosition.parent;
                    }
                    Position node = curPosition.parent;
                    node.childArray.remove(node.selectRoot);
                    node.childArray.addAll(curPosition.childArray);
                    node.selectRoot = curPosition.selectRoot;
                    curPosition = node;
                } else {
                    curPosition = curPosition.parent;
                    curPosition.childArray.clear();
                    curPosition.selectRoot = -1;
                }
                maxmove--;
                currentMove = curPosition.move;
            }
            //当前手赋值
            if (!isSingleColor) {
                playerBlackMoves = !((isTryDown ? tmpTotalMoveLise.c : curPosition.c) == BLACK ? true : false);
            }
            //提子数更新
            blackDeadCount = curPosition.blackDeadCount;
            whiteDeadCount = curPosition.whiteDeadCount;
            //回调
            if (mOnEndOfGame != null && isBack) {
                mOnEndOfGame.onBoardViewSingle();
            }
            //重绘棋盘
            drawBoard();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 猜棋更新当前手(删除当前手,回到上一手)
     */
    public void guessUpdateMove() {
        curPosition = curPosition.parent;
        if (curPosition.childArray.size() > 1) {
            curPosition.childArray.remove(1);
            curPosition.selectRoot = 0;
        }
        pointState = curPosition.curState;
        maxmove--;
    }

    /**
     * 获取黑被提子数
     *
     * @return
     */
    public int getBlackDeadCount() {
        return curPosition != null ? curPosition.whiteDeadCount : 0;
    }

    /**
     * 获取白被提子数
     *
     * @return
     */
    public int getWhiteDeadCount() {
        return curPosition != null ? curPosition.blackDeadCount : 0;
    }

    //允许回调
    public void setNewGameIsCallBack(boolean newGameIsCallBack) {
        this.newGameIsCallBack = newGameIsCallBack;
    }

    //设置学习状态
    public void setLearn(boolean isLearn) {
        this.isLearn = isLearn;
    }

    /**
     * 当前盘面棋子输出
     *
     * @return
     */
    public HashMap<String, String> getStoneList() {
        //白子列表
        StringBuilder wStones = new StringBuilder();
        //黑子列表
        StringBuilder bStones = new StringBuilder();
        HashMap<String, String> wbStones = Generics.newHashMap();
        //当前盘面坐标输出
        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                int move = pointState[y * _boardSize + x];
                if (move != 0) {
                    if (move == BLACK) {
                        bStones.append(GameUtil.numToString(x, y) + ":");
                    } else if (move == WHITE) {
                        wStones.append(GameUtil.numToString(x, y) + ":");
                    }
                }
            }
        }
        //删除多余字符":"
        if (wStones.length() > 0) {
            wStones.deleteCharAt(wStones.length() - 1);
        }
        if (bStones.length() > 0) {
            bStones.deleteCharAt(bStones.length() - 1);
        }
        wbStones.put("wStones", wStones.toString());
        wbStones.put("bStones", bStones.toString());
        return wbStones;
    }

    /**
     * 当前盘面棋子输出
     *
     * @return
     */
    public String getStoneStrs() {
        //存放所有的子坐标
        StringBuilder wbStones = new StringBuilder();
        //当前盘面坐标输出
        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                int move = pointState[y * _boardSize + x];
                if (move == BLACK || move == WHITE) {
                    wbStones.append(GameUtil.numToString(x, y) + ",");
                }
            }
        }
        //删除多余字符","
        if (wbStones.length() > 0) {
            wbStones.deleteCharAt(wbStones.length() - 1);
        }
        return wbStones.toString();
    }

    /**
     * 获得当前盘面各棋子数量及未落子数
     *
     * @return
     */
    public HashMap<String, Integer> getCurrentState() {
        HashMap<String, Integer> state = Generics.newHashMap();
        //黑子数
        int bCount = 0;
        //白子数
        int wCount = 0;
        //空白数
        int emptyCount = 0;
        for (int x = 0; x < _boardSize; x++) {
            for (int y = 0; y < _boardSize; y++) {
                int move = pointState[y * _boardSize + x];
                switch (move) {
                    case EMPTY:
                        emptyCount++;
                        break;
                    case BLACK:
                        bCount++;
                        break;
                    case WHITE:
                        wCount++;
                        break;
                }
            }
        }
        state.put("bCount", bCount);
        state.put("wCount", wCount);
        state.put("emptyCount", emptyCount);
        return state;
    }

    public double getKomi() {
        double komi = 0;
        try {
            komi = Double.parseDouble(_gameInfo.headInfo.get("KM"));
        } catch (Exception ex) {
            komi = 7.5;
        }
        return komi;
    }

    public double getHandicap() {
        double handicap = 0;
        try {
            handicap = Double.parseDouble(_gameInfo.headInfo.get("HA"));
        } catch (Exception ex) {
            handicap = 0;
        }
        return handicap;
    }

    /**
     * @return 0:错误  1:进入下一个分支  2:正确并结束
     */
    public int gomissionCheck() {
        try {
            //判断当前节点是否有分支
            if (curPosition.childArray.size() > 0) {
                //前进一手 继续做题
                nextMove(1);
                //判断自动落的一手是否结束
                if (curPosition.childArray.size() == 0) {
                    //判断是否有TE
                    String isSuc = curPosition.moveInfo.get("TE");
                    if (isSuc.equals("1")) {
                        return 2;
                    } else {
                        return 0;
                    }
                } else {
                    return 1;
                }
            } else {  //最后一手
                //判断是否有TE
                String isSuc = curPosition.moveInfo.get("TE");
                if (isSuc.equals("1")) {
                    return 2;
                } else {
                    return 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Bitmap drawBitmap() {
        return _boardBitmap;
    }

    /**
     * 设置落子方式
     *
     * @param stoneMoveType 0虚收 1直接落
     */
    public void setStoneMoveType(int stoneMoveType) {
        this.stoneMoveType = stoneMoveType;
    }

    /**
     * 微调
     *
     * @param direction
     */
    public void fineStone(Direction direction) {
        //获取最后落子的坐标
        if (_lastPoint == null || stoneMoveType == 1) {
            return;
        }
        Position position = new Position(_lastPoint);

        if (direction == Direction.TOP) {
            while (true) {
                if (position.y > 0) {
                    position.y -= 1;
                    if (checkMoving(position)) {
                        _gestureListener.moveStone(position);
                        break;
                    }
                } else {
                    break;
                }
            }
        } else if (direction == Direction.BOTTOM) {
            while (true) {
                if (position.y < 18) {
                    position.y += 1;
                    if (checkMoving(position)) {
                        _gestureListener.moveStone(position);
                        break;
                    }
                } else {
                    break;
                }
            }
        } else if (direction == Direction.LEFT) {
            while (true) {
                if (position.x > 0) {
                    position.x -= 1;
                    if (checkMoving(position)) {
                        _gestureListener.moveStone(position);
                        break;
                    }
                } else {
                    break;
                }
            }
        } else if (direction == Direction.RIGHT) {
            while (true) {
                if (position.x < 18) {
                    position.x += 1;
                    if (checkMoving(position)) {
                        _gestureListener.moveStone(position);
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 微调时确认落子
     */
    public void confirmStone() {
        if (moveingFlag) {
            _gestureListener.setStone();
        }
    }

    /**
     * 棋盘是否初始化完成
     *
     * @return
     */
    public boolean isInitBoard() {
        return !waitFlag;
    }


    /**
     * 设置当前落子颜色
     *
     * @param isBlack 是否执黑  true执黑  false执白
     */
    public void setMoveColor(boolean isBlack) {
        playerBlackMoves = isBlack;
    }

    /**
     * 获取棋谱头部信息  根据key
     *
     * @param key
     * @return
     */
    public String getHead(String key) {
        String value;
        try {
            value = _gameInfo.headInfo.get(key);
            //ru特殊处理
            if (key.equals("RU")) {
                if (StringUtils.isEmpty(value)) value = "cn";
                value = value.toLowerCase();
                if (value.contains("jp")) {
                    value = "jp";
                } else {
                    value = "cn";
                }
            }
            //km特殊处理
            if (key.equals("KM")) {
                //判断不是数字
                value = StringUtils.toDouble(value, 7.5) + "";
                String ha = _gameInfo.headInfo.get("HA");
                if (StringUtils.isEmpty(ha)) {
                    if (StringUtils.isEmpty(value)) value = "7.5";
                } else {
                    if (!ha.equals("0")) {
                        value = "0";
                    }
                }
            }
        } catch (Exception ex) {
            value = "";
        }
        return value;
    }

    public void destroy() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (deadMoreSound != null) {
            deadMoreSound.release();
            deadMoreSound = null;
        }
        _boardView = null;
        _gameInfo = null;
        activity = null;
    }

    /*
     判断当前是否是最新手
     */
    public boolean isNewStone() {
        if (curPosition != null && curPosition.childArray.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取所有手数,包含评论或分支
     *
     * @param getType 0所有分支,1首尾和包含评论或分支的手
     */
    public ArrayList<HashMap> getStoneListByCondition(int getType) {
        if (isTryDown || isAddBranch) {
            return null;
        }
        ArrayList stoneList = Generics.newArrayList();
        HashMap stoneMap;
        Position tmpPosition = totalMoveList;
        while (true) {
            if (tmpPosition != null) {
                if (getType == 0) {
                    stoneMap = new HashMap();
                    stoneMap.put("move", tmpPosition.move);
                    stoneMap.put("isComment", getComment2(tmpPosition).isEmpty() ? "0" : "1");
                    stoneMap.put("branchCount", tmpPosition.childArray.size() - 1);
                    stoneList.add(stoneMap);
                    if (tmpPosition.childArray.size() > 0) {
                        tmpPosition = tmpPosition.childArray.get(tmpPosition.selectRoot);
                    } else {
                        break;
                    }
                } else if (getType == 1) {
                    if (!getComment2(tmpPosition).isEmpty() || tmpPosition.childArray.size() > 1 || tmpPosition.move == 0 || tmpPosition.childArray.size() == 0) {
                        stoneMap = new HashMap();
                        stoneMap.put("move", tmpPosition.move);
                        stoneMap.put("isComment", getComment2(tmpPosition).isEmpty() ? "0" : "1");
                        stoneMap.put("branchCount", tmpPosition.childArray.size() - 1);
                        stoneList.add(stoneMap);
                    }
                    if (tmpPosition.childArray.size() > 0) {
                        tmpPosition = tmpPosition.childArray.get(tmpPosition.selectRoot);
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return stoneList;
    }

    //获取当前解说
    private String getComment2(Position position) {
        String comment = "";
        try {
            comment = position.moveInfo.get("C").replace("\n,", "\n");
        } catch (Exception ex) {
            comment = "";
        }
        return comment;
    }

    //切换手数样式
    public void changeMoveStyle() {
        if (moveType == MoveType.TR) {
            moveType = moveType.LB;
            moveCount = 999;
            //棋局设置动态修改
            StringUtils.set(activity,"hand_type", 1);
        } else if (moveType == MoveType.LB && moveCount == 999) {
            moveType = moveType.LB;
            moveCount = 1;
            //棋局设置动态修改
            StringUtils.set(activity,"hand_type", 2);
        } else {
            moveType = MoveType.TR;
            //棋局设置动态修改
            StringUtils.set(activity,"hand_type", 0);
        }
        drawBoard();
    }

    /**
     * @return
     */
    public List<HashMap<String, Integer>> getCurBranchs() {
        List<HashMap<String, Integer>> list = Generics.newArrayList();
        HashMap<String, Integer> map;
        if (isAddBranch) {
            for (int i = 1; i < addBranchTmpPosition.childArray.size(); i++) {
                map = new HashMap();
                map.put("move", drawMarkIndex);
                map.put("branch", i);
                list.add(map);
            }
        } else {
            for (int i = 1; i < curPosition.childArray.size(); i++) {
                map = new HashMap();
                map.put("move", curPosition.move);
                map.put("branch", i);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 设置选择框方向
     *
     * @param isSelect
     * @param direction
     */
    public void setSelectRect(boolean isSelect, int direction) {
        //判断是否为19路盘
        this.isSelect = isSelect;
        //设置默认区域
        beginDirection = direction;
        switch (direction) {
            case 0:
                beginRectPosition = new Position(0, 0);
                drawSelectRect(new Position(9, 9));
                break;
            case 1:
                beginRectPosition = new Position(18, 0);
                drawSelectRect(new Position(9, 9));
                break;
            case 2:
                beginRectPosition = new Position(0, 18);
                drawSelectRect(new Position(9, 9));
                break;
            case 3:
                beginRectPosition = new Position(18, 18);
                drawSelectRect(new Position(9, 9));
                break;
        }
    }

    /**
     * 关闭棋形搜索
     */
    public void closeSelectRect() {
        isSelect = false;
        drawBoard();
    }

    /**
     * 获取棋形搜索获取字符串中包含子的数量
     *
     * @param srcText
     * @param findText
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }


    /**
     * 获取试下时落子集合
     *
     * @return
     */
    public JSONArray getTryDownMoveList() {
        JSONArray jsonArray = new JSONArray();
        JSONObject move;
        Position position = tmpTotalMoveLise;
        while (position.parent != null) {
            if (!position.isPass || position.x != -1 || position.y != -1) {
                move = new JSONObject();
                move.put("x", position.x);
                move.put("y", position.y);
                move.put("c", position.c);
                jsonArray.add(move);
            }
            position = position.parent;
        }
        return jsonArray;
    }

    /**
     * 棋盘当前是否处于型势判断
     *
     * @return
     */
    public boolean getIsJudge() {
        return isJudge == 0 ? false : true;
    }

    /**
     * 棋盘当前是否处于直播室查看分支状态
     *
     * @return
     */
    public boolean getIsBranch() {
        return isBranch;
    }

    /**
     * 设置猜棋状态
     *
     * @param isGuess
     */
    public void setGuess(boolean isGuess) {
        this.isGuess = isGuess;
    }

    /**
     * 判断棋盘上是否有额外绘制上去的棋子等等
     *
     * @return
     */
    public boolean checkBoardStone() {
        return drawStoneList;
    }

    /**
     * 默认设置棋盘为未绘制状态
     */
    public void setBoardStone() {
        drawStoneList = false;
    }

    /**
     * 设置是否按照设置显示手数样式
     *
     * @param handStyleFlag
     */
    public void setHandStyleFlag(boolean handStyleFlag) {
        this.handStyleFlag = handStyleFlag;
    }
}