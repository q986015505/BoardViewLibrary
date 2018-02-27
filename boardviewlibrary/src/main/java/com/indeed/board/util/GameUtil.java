package com.indeed.board.util;

import android.util.Log;

import com.indeed.board.BoardView;
import com.indeed.board.GameInfo;
import com.indeed.board.Position;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yandequan on 16/12/22.
 */

public class GameUtil {

    public static final
    String
            _POSITION_LETTERS_STR = "ABCDEFGHJKLMNOPQRST",
            _ESTIMIATED_SCORE_PATTERN = "^(B|W)(.[0-9]+\\.[0-9]+)",
            _SGF_VALUE_PATTERN_STRING = "\\[([^]]+)\\]",
            _SGF_COLOR_2_PLAY_PATTERN_STRING = "PL",
            _SGF_COLOR_2_PLAY_WHITE = "W",
            _SGF_COLOR_2_PLAY_BLACK = "B",
            _SGF_TMP_FILE_SUFFIX = "_tmp",
            _ESTIMATED_SCORE_BLACK_WINS_LETTER = "B";

    public static final String[] indexStr = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};

    static final String REG_SEQ = "\\(|\\)|(;(\\s*[A-Z]+(\\s*((\\[\\])|(\\[(.|\\s)*?([^\\\\]\\]))))+)*)";
    static final String REG_NODE = "[A-Z]+(\\s*((\\[\\])|(\\[(.|\\s)*?([^\\\\]\\]))))+";
    static final String REG_IDENT = "[A-Z]+";
    static final String REG_PROPS = "(\\[\\])|(\\[(.|\\s)*?([^\\\\]\\]))";


    static final
    public char[] _POSITION_LETTERS_CHARS = _POSITION_LETTERS_STR.toCharArray();

    //Sgf解析
    public static Position parseSgf(String sgf, GameInfo gameInfo) {
        Position root = new Position();

        //栈
        Stack<Position> stack = new Stack<>();

        Position node = null;

        List<String> sequence = Generics.newArrayList(),
                props = Generics.newArrayList(),
                vals = Generics.newArrayList();

        sequence = getMatch(sgf, REG_SEQ);

        int pubCount = 0;
        for (int i = 0; i < sequence.size(); i++) {
            // 推栈
            if (sequence.get(i).equals("(")) {
                stack.push(node);
                pubCount++;
            }
            //移除栈
            else if (")".equals(sequence.get(i)) && pubCount > 0) {
                node = stack.pop();
                pubCount--;
            }
            //读取节点（以“;”开头的字符串
            else {
                //创建节点或使用跟节点
                if (node == null) {
                    node = root;
                } else {
                    node.childArray.add(new Position(node));
                    //默认选中fen zhi
                    if (node.childArray.size() > 0) {
                        node.selectRoot = 0;
                    }
                    //父节点赋值
                    node.childArray.get(node.childArray.size() - 1).parent = node;
                    node = node.childArray.get(node.childArray.size() - 1);
                }
                //make array of properties
                props = getMatch(sequence.get(i), REG_NODE);
                //将所有属性插入节点
                for (String pro : props) {
                    //获取节点标识
                    String ident = exec(REG_IDENT, pro)[0];
                    //获取节点值
                    vals = getMatch(pro, REG_PROPS);
                    // 删除其他大括号[ and ]
                    for (int k = 0; k < vals.size(); k++) {
                        String str = vals.get(k);
                        vals.remove(k);
                        str = str.substring(1, str.length() - 1).replace("\\\\(?!\\\\)/g", "");
                        vals.add(k, str);
                    }
                    //处理属性
                    if (ident.equals("B") || ident.equals("W")) {
                        if (vals.get(0).equals("tt")) {
                            node.isPass = true;
                        } else {
                            node.x = toNum(vals.get(0), 0);
                            node.y = toNum(vals.get(0), 1);
                            if (node.x == -1 && node.y == -1) node.isPass = true;
                        }
                        node.c = (ident.equals("B")) ? BoardView.BLACK : BoardView.WHITE;
                    } else if (ident.equals("AB") || ident.equals("AW")) {
                        for (int k = 0; k < vals.size(); k++) {
                            Position setup = new Position();
                            setup.x = toNum(vals.get(k), 0);
                            setup.y = toNum(vals.get(k), 1);
                            if (setup.x == -1 && setup.y == -1) setup.isPass = true;

                            setup.c = (ident.equals("AB")) ? BoardView.BLACK : BoardView.WHITE;
                            node.setup.add(setup);
                            if (node.parent != null) {
                                node.x = setup.x;
                                node.y = setup.y;
                                if (node.x == -1 && node.y == -1) node.isPass = true;
                                node.c = setup.c;
                            }
                        }
                    } else if (ident.equals("C")) {
                        node.moveInfo.put("C", vals.get(0));
                    } else if (ident.equals("TE")) {
                        node.moveInfo.put("TE", vals.get(0));
                        //将默认分支选中有正确答案
                        Position position = node.parent;
                        while (position != null) {
                            position.selectRoot = position.childArray.size() - 1;
                            if (position.parent != null) {
                                position = position.parent;
                            } else {
                                break;
                            }
                        }
                    } else {
                        gameInfo.headInfo.put(ident, vals.get(0));
                    }
                }
            }
        }
        return root;
    }

    /**
     * 棋盘转换SGF
     *
     * @param gameInfo
     * @param position
     * @param boardSize
     * @return
     */
    public static String toSgf(GameInfo gameInfo, Position position, int boardSize) {
        String[] sgf = new String[]{"(;"};
        //棋盘大小录入
        gameInfo.headInfo.put("SZ", boardSize + "");
        //若sgf中不存在某些信息,录入自定义
        gameInfo.headInfo.put("SO", "弈客围棋");
        //write root
        for (Map.Entry<String, String> entry : gameInfo.headInfo.entrySet()) {
            sgf[0] += entry.getKey() + "[" + entry.getValue() + "]";
        }
        sgfWriteNode(position, sgf);
        sgf[0] += ")";
        Log.d("GameUtil", "toSgf: " + sgf[0]);
        return sgf[0];
    }

    /**
     * 棋盘转换SGF
     *
     * @param gameInfo
     * @param position
     * @param boardSize
     * @return
     */
    public static String toSgfNoBranch(GameInfo gameInfo, Position position, int boardSize, boolean writeHeader) {
        String[] sgf = new String[]{"(;"};
        //棋盘大小录入
        gameInfo.headInfo.put("SZ", boardSize + "");
        if (writeHeader) {
            //若sgf中不存在某些信息,录入自定义
            gameInfo.headInfo.put("SO", "弈客围棋");
            //write root
            for (Map.Entry<String, String> entry : gameInfo.headInfo.entrySet()) {
                sgf[0] += entry.getKey() + "[" + entry.getValue() + "]";
            }
        }
        List<Position> positions = Generics.newArrayList();
        positions.add(position);
        Position tmpPosition;
        while (!positions.isEmpty()) {
            tmpPosition = positions.remove(0);
            if (tmpPosition.parent != null) {
                sgf[0] += ";";
            }
            sgfWriteNode2(tmpPosition, sgf);
            if (tmpPosition.childArray.size() > 0) {
                positions.add(tmpPosition.childArray.get(0));
            }
//            if (tmpPosition.childArray.size() == 1) {
//                positions.add(tmpPosition.childArray.get(0));
//            } else if (tmpPosition.childArray.size() > 1) {
//                sgf[0] += "(;";
//                for (Position node : tmpPosition.childArray) {
//                       positions.add(node);
//                }
//                sgf[0] += ")";
//            }
        }
        sgf[0] += ")";
        return sgf[0];
    }

    /**
     * 生成sgf从当前手生成到第一手
     *
     * @param gameInfo
     * @param position
     * @param boardSize
     * @return
     */
    public static String toSgfLastToFirst(GameInfo gameInfo, Position position, int boardSize) {
        String[] sgf = new String[]{")"};
        //棋盘大小录入
        gameInfo.headInfo.put("SZ", boardSize + "");
        while (position.parent != null) {
            sgfWriteNodeToFirst(position, sgf);
            sgf[0] = ";" + sgf[0];
            position = position.parent;
        }
        sgfWriteNodeToFirst(position, sgf);
        //让子数
        String ha = gameInfo.headInfo.get("HA");
        sgf[0] = "HA[" + (ha != null ? ha : "0") + "]" + sgf[0];
        //添加贴目
        String km = gameInfo.headInfo.get("KM");
        sgf[0] = "KM[" + (km != null ? km : "7.5") + "]" + sgf[0];
        //棋盘大小
        sgf[0] = "SZ[" + boardSize + "]" + sgf[0];
        sgf[0] = "(;" + sgf[0];
        return sgf[0];
    }

    /**
     * 填子,从前面拼接sgf
     *
     * @param position
     * @param sgf
     */
    private static void sgfWriteNodeToFirst(Position position, String[] sgf) {
        //move
        /**
         * 生成一个子的坐标
         */
        String move = "";
        if (position.parent != null) {
            if (position.x != -1 && position.y != -1 && !position.isPass) {
                move = numToString(position.x, position.y);
            } else {
                move = "tt";
            }
            if (position.c == BoardView.BLACK) sgf[0] = "B[" + move + "]" + sgf[0];
            else sgf[0] = "W[" + move + "]" + sgf[0];
        }
        //setup
        if (position.setup.size() > 0 && position.parent == null) {//头上面默认的几个子
            List<String> AB = Generics.newArrayList();
            List<String> AW = Generics.newArrayList();

            for (Position setup : position.setup) {
                if (setup.c == BoardView.BLACK) AB.add(numToString(setup.x, setup.y));
                else if (setup.c == BoardView.WHITE) AW.add(numToString(setup.x, setup.y));
            }
            sgfWriteGroupToFirst("B", AB, sgf);
            sgfWriteGroupToFirst("W", AW, sgf);
        }
    }

    private static void sgfWriteGroupToFirst(String key, List<String> values, String[] sgf) {
        if (values.size() == 0) return;
        for (String k : values) {
            sgf[0] = ";" + key + "[" + k + "]" + sgf[0];
        }
    }


    /**
     * 棋盘转换SGF
     *
     * @param gameInfo
     * @param position
     * @param boardSize
     * @return
     */
    public static String toSimpleSgf(GameInfo gameInfo, Position position, int boardSize) {
        String[] sgf = new String[]{"(;"};
        //棋盘大小录入
        gameInfo.headInfo.put("SZ", boardSize + "");
        sgfWriteNode(position, sgf);
        sgf[0] += ")";
        Log.d("GameUtil", "toSgf: " + sgf[0]);
        return sgf[0];
    }

    /**
     * 递归解析每手信息
     *
     * @param position
     * @param sgf
     */
    private static void sgfWriteNode(Position position, String[] sgf) {
        //move
        /**
         * 生成一个子的坐标
         */
        String move = "";
        if (position.parent != null) {
            if (position.x != -1 && position.y != -1 && !position.isPass) {
                move = numToString(position.x, position.y);
            } else {
                move = "tt";
            }
            if (position.c == BoardView.BLACK) sgf[0] += "B[" + move + "]";
            else sgf[0] += "W[" + move + "]";
        }
        //setup
        if (position.setup.size() > 0 && position.parent == null) {//头上面默认的几个子
            List<String> AB = Generics.newArrayList();
            List<String> AW = Generics.newArrayList();
            List<String> AE = Generics.newArrayList();

            for (Position setup : position.setup) {
                if (setup.c == BoardView.BLACK) AB.add(numToString(setup.x, setup.y));
                else if (setup.c == BoardView.WHITE) AW.add(numToString(setup.x, setup.y));
                else AE.add(numToString(setup.x, setup.y));
            }

            sgfWriteGroup("AB", AB, sgf);
            sgfWriteGroup("AW", AW, sgf);
            sgfWriteGroup("AE", AE, sgf);
        }

        //markup


        //other
        for (Map.Entry<String, String> entry : position.moveInfo.entrySet()) {//当前子的信息
            if (entry.getKey().equals("C")) sgf[0] += "C[" + entry.getValue() + "]";
            else sgf[0] += entry.getKey() + "[" + entry.getValue() + "]";
        }

        if (position.childArray.size() == 1) {//没有分支
            sgf[0] += ";";
            sgfWriteNode(position.childArray.get(0), sgf);
        } else if (position.childArray.size() > 1) {
            for (Position node : position.childArray) {
                sgfWriteVariantion(node, sgf);
            }
        }
    }

    /**
     * 不包含递归解析
     *
     * @param position
     * @param sgf
     */
    private static void sgfWriteNode2(Position position, String[] sgf) {
        //move
        /**
         * 生成一个子的坐标
         */
        String move = "";
        if (position.parent != null) {
            if (position.x != -1 && position.y != -1 && !position.isPass) {
                move = numToString(position.x, position.y);
            } else {
                move = "tt";
            }
            if (position.c == BoardView.BLACK) sgf[0] += "B[" + move + "]";
            else sgf[0] += "W[" + move + "]";
        }
        //setup
        if (position.setup.size() > 0 && position.parent == null) {//头上面默认的几个子
            List<String> AB = Generics.newArrayList();
            List<String> AW = Generics.newArrayList();
            List<String> AE = Generics.newArrayList();

            for (Position setup : position.setup) {
                if (setup.c == BoardView.BLACK) AB.add(numToString(setup.x, setup.y));
                else if (setup.c == BoardView.WHITE) AW.add(numToString(setup.x, setup.y));
                else AE.add(numToString(setup.x, setup.y));
            }

            sgfWriteGroup("AB", AB, sgf);
            sgfWriteGroup("AW", AW, sgf);
            sgfWriteGroup("AE", AE, sgf);
        }

        //markup


        //other
        for (Map.Entry<String, String> entry : position.moveInfo.entrySet()) {//当前子的信息
            if (entry.getKey().equals("C")) sgf[0] += "C[" + entry.getValue() + "]";
            else sgf[0] += entry.getKey() + "[" + entry.getValue() + "]";
        }
    }


    private static void sgfWriteVariantion(Position node, String[] sgf) {
        sgf[0] += "(;";
        sgfWriteNode(node, sgf);
        sgf[0] += ")";
    }

    private static void sgfWriteGroup(String key, List<String> values, String[] sgf) {
        if (values.size() == 0) return;

        sgf[0] += key;
        for (String k : values) {
            sgf[0] += "[" + k + "]";
        }
    }

    public static List<String> getMatch(String node, String matchStr) {
        List<String> tmpArray = Generics.newArrayList();
        Pattern pattern = Pattern.compile(matchStr);
        Matcher matcher = pattern.matcher(node);
        while (matcher.find()) {
            tmpArray.add(matcher.group(0));
        }
        return tmpArray;
    }

    // 模拟reg.exec 捕获分组
    private static String[] exec(String reg, String para) {

        Pattern regExp = Pattern.compile(reg);

        Matcher match = regExp.matcher(para);

        int count = match.groupCount() + 1;

        String[] matchs = new String[count];

        if (match.find()) {
            for (int i = 0; i < count; i++)
                matchs[i] = match.group(i);
            return matchs;
        }

        return null;

    }

    public static int toNum(String str, int i) {
        try {
            return str.charAt(i) - 97;
        } catch (Exception e) {
            return -1;
        }
    }

    //int坐标转换String坐标  如:0,0 to aa
    public static String numToString(int x, int y) {
        String xy = String.valueOf((char) (x + 97)) + String.valueOf((char) (y + 97));
        return xy;
    }

}
