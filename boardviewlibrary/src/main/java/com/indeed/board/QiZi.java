package com.indeed.board;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class QiZi {
	private static int count = 0;

	private BitSet qiziAddress = null;

	private int _boardSize = 9;
	
	public QiZi(boolean b,int boardSize) {
		this._boardSize = boardSize;
		qiziAddress = new BitSet(8);
		if(b == true) {
			count++;
		}
	}

	public boolean isBlack(Integer[] pointsState , int order) {
		if(pointsState[order] == BoardView.BLACK) {
			return true;
		}
		return false;
	}
	
	public void clear() {
		count--;
	}
	
	public boolean isHasEmpty(Integer[] pointsState , int order) {
		int point = 0;
		for(int i=0; i<4; i++) {
			int a = -1;
			if(i==0 && order%_boardSize != 0) {
				a = order - 1;
			} else if(i==1 && order > _boardSize - 1) {
				a = order - _boardSize;
			} else if(i==2 && order%_boardSize != _boardSize-1) {
				a = order + 1;
			} else if (i== 3 && order< _boardSize * (_boardSize - 1)){
				a = order + _boardSize;
			}
			if( a != -1 && pointsState[a] == BoardView.EMPTY) {
				return true;
			}
			point = a;
		}
		return false;
	}

	
	//设置第一位为为左边有相同的子，第二位为上边有相同的子，第三位为右边有相同的子，第四位为下边有相同的子
	public BitSet sameQiZi(Integer[] pointsState, int target) {
		int same = pointsState[target];
		BitSet bit = new BitSet(4);
		if(target%_boardSize!=0 && pointsState[target - 1] == same) {
			bit.set(0, true);
		}
		if(target>_boardSize-1 && pointsState[target - _boardSize] == same) {
			bit.set(1, true);
		}
		if(target%_boardSize != _boardSize-1 && pointsState[target + 1] == same) {
			bit.set(2, true);
		}
		if(target<_boardSize * (_boardSize-1) && pointsState[target + _boardSize] == same) {
			bit.set(3, true);
		}
		return bit;
	}
	
	////设置第一位为为左边有不同的子，第二位为上边有不同的子，第三位为右边有不同的子，第四位为下边有不同的子
	public BitSet differQiZi(Integer[] pointsState, int target) {
		int differ = 0;
		if(isBlack(pointsState, target)) {
			differ = BoardView.WHITE;
		} else {
			differ = BoardView.BLACK;
		}
		BitSet bit = new BitSet(4);
		
		if(target%_boardSize==0 || pointsState[target - 1] == differ) {
			bit.set(0, true);
		}
		if(target<_boardSize || pointsState[target - _boardSize] == differ) {
			bit.set(1, true);
		}
		if(target%_boardSize == _boardSize-1 || pointsState[target + 1] == differ) {
			bit.set(2, true);
		}
		if(target>_boardSize*(_boardSize-1)-1 || pointsState[target + _boardSize] == differ) {
			bit.set(3, true);
		}
		return bit;
	}

	//记录该棋子身边的不同棋子的位置
	public Set<Integer>
	differQiZiArray(Integer[] pointsState, int target) {
		Set<Integer> differArray = new HashSet<Integer>();
		BitSet differs = this.differQiZi(pointsState, target);
		if(differs.get(0) == true && target%_boardSize!=0) {
			differArray.add(target - 1);
		}
		if(differs.get(1) == true && target>_boardSize-1) {
			differArray.add(target - _boardSize);
		}
		if(differs.get(2) == true && target%_boardSize != _boardSize-1) {
			differArray.add(target + 1);
		}
		if(differs.get(3) == true && target<_boardSize*(_boardSize-1)) {
			differArray.add(target + _boardSize);
		}
		return differArray;
	}
	
	//记录相同的棋子相连的位置 (递归算法——关联算法)
	public Map<Integer, Integer> relationQiZi(
									Integer[] pointsState,
									int target, 
									Map<Integer, Integer> maps,
									Map<Integer,Integer> tmaps) {
		
		if(maps.isEmpty()) {
			maps.put(target, -1);
		}
		
		//当target左、上、右、下边有子的情况下
		for(int i=0; i<4; i++) {
			if(this.sameQiZi(pointsState, target).get(i) == true) {
				int a = -1;
				if(i==0) {
					a = target - 1;
				} else if(i==1) {
					a = target -_boardSize;
				} else if(i==2) {
					a = target + 1;
				} else if(i==3){
					a = target + _boardSize;
				} 
				if(a != -1) {
					if(maps.containsKey(a) == false) {
						maps.put(a, -1);
					} else {
						if(tmaps.containsKey(a) == true){
							maps.put(a, 0);
						}
					}
				}
			}
			tmaps.put(target,1);
			maps.put(target, 0);
		}
				
		//  对map当中的值为-1的键赋值给target,并且此target不能等于赋值前的target
		Set<Integer> keys = maps.keySet();
		Iterator it = keys.iterator();
		int same = 0;
		while(it.hasNext()) {
			int key = (Integer)it.next();
			if(maps.get(key) == -1) {
				target = key;
				break;
			} else {
				same++;
			}
			if(same == maps.size()) {
				return maps;
			}
		}
		return this.relationQiZi(pointsState, target, maps,tmaps);
	}

	//重载——记录相同的棋子相连的位置
	public Integer[] relationQiZi(Integer[] pointsState, int target) {
		Map<Integer, Integer> maps = new Hashtable<Integer, Integer>();
		Map<Integer, Integer> tmaps = new Hashtable<Integer, Integer>();
		Integer[] members = new Integer[this.relationQiZi(pointsState, target, maps,tmaps).size()];
		
		Set<Integer> keys = new HashSet<Integer>();
		keys = this.relationQiZi(pointsState, target, maps,tmaps).keySet();
		int i=0;
		for(Iterator it = keys.iterator(); it.hasNext();) {
			members[i++] = (Integer)it.next();
		}
		return members;
	}
	
	// 返回身边相同棋子的数目
	public int sameSideCount(Integer[] pointsState, int target) {
		int number = 0;
		for(int i=0; i<4; i++) {
			if(this.sameQiZi(pointsState, target).get(i) == true) {
				number++;
			}
		}
		return number;
	}
	
	// 返回身边不相同棋子的数目
	public int differSideCount(Integer[] pointsState, int target) {
		int number = 0;
		BitSet bits = this.differQiZi(pointsState, target);
		for(int i=0; i<4; i++) {
			if(bits.get(i) == true) {
				number++;
			}
		}
		return number;
	}
	
	// 返回一团棋子被包围的数目
	public int surroundedCount(Integer[] pointsState, int target) {
		Integer[] members = this.relationQiZi(pointsState, target);
		int count = 0;
		for(int i=0; i<members.length; i++) {
			count = count + this.differSideCount(pointsState, members[i]);
		}
		return count;
	}
	
	// 判断当前是否有死子
	public boolean isDeadQiZi(Integer[] pointsState, int target) {
		int a = 0;
		Integer[] sameArray = this.relationQiZi(pointsState, target);
		for(int i=0; i<sameArray.length; i++) {
			if(this.isHasEmpty(pointsState, sameArray[i])) {
				return false;
			}
		}
		return true;
	}
	public Integer[] handleDead(Integer[] pointsState, int target) {
		Integer[] deads = this.relationQiZi(pointsState, target);
		for(int i=0; i<deads.length; i++) {
			pointsState[deads[i]] = BoardView.EMPTY;
		}
		return pointsState;
	}
	
	// 经过处理之后，返回各个子的状态
	public Integer[] qiziStates(Integer[] pointsState) {
		return pointsState;
	}
}
