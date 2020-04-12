/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/

package checkers.library;

import java.util.*;
import checkers.game.*;


/**
 * Bridge between native C++ and Java code.
 */
public class NativeCheckers 
{
	
	private static final String NATIVE_LIBRARY_NANE = "checkers";
	
	/**
	 * Native call. Sets board size.
	 * @param size new board size
	 */
	public static native void setBoardSize(int size);

	/**
	 * Native call. Sets online learning.
	 * @param active true if online learning is active
	 */	
	public static native void setOnlineLearning(boolean active);

	/**
	 * Native call. Finds "best" move for player.
	 * The returned value is an array of square indices, which the selected move
	 * passes through, including the square which the selected piece starts from.
	 * 
	 * @param myColor Player's color
	 * @param myLevel Player's level
	 * @param blackPeons Bit representation of black peons positions
	 * @param blackKings Bit representation of black kings positions
	 * @param whitePeons Bit representation of white poens positions
	 * @param whiteKings Bit representation of white kings positions
	 * @param addBoard indicator for adding board to history
	 * @param useBook indicator for using opening book in play if possible
	 * @return array of squares, which the move passes through.
	 */
	private static native int[] calculateMove(int myColor, int myLevel, long blackPeons,
										 long blackKings, long whitePeons, long whiteKings,boolean addBoard,boolean useBook);
		
	/**
	 * Undo move(s).
	 */									 
	public static native void unDo(int size);

	/**
	 * Redo move(s).
	 */									 
	public static native void reDo(int size);
	
	/**
	 * Native call for learning,
	 * @param color Learning Player's color
	 * @param opponentMoveHops Opponent move squares, encoded into square numbers.
	 * @param opponentMoveHits Opponent move captures, encoded into square numbers.
	 */
	public static native void learn(int color, int opponentMoveHops[], int opponentMoveHits[]);

	/**
	 * Clears boards history.
	 */	
	public static native void clearHistory();
	
	static 
	{
		System.loadLibrary(NATIVE_LIBRARY_NANE);
	}

	/**
	 * Finds the best move for the given player.
	 * @param player The CPU Player.
	 * @param level The level of the Player
	 * @return The selected Move
	 */	
	public static Move calculateMove(CPUPlayer player)
	{
		Board b = Game.getInstance().getBoard();
		boolean addBoard = (Game.getInstance().getBlack() instanceof CPUPlayer ^
				Game.getInstance().getWhite() instanceof CPUPlayer);
		long state[] = b.encode();
		int a[] = calculateMove(player.getColor(), player.getLevel(), 
				state[0], state[1], state[2], state[3],addBoard, Game.getInstance().isOpeningBookUsed());
		if (a == null) return null;
		Peon p = b.getPeonAt(decode(a[0]));
		Move move = new Move(p);
		for (int i=1; i<a.length; i++)
		{
			move.addPoint(decode(a[i]));
		}
		return move;			
	}
	
	/**
	 * Performs online learning.
	 * @param color The color mark of the learning Player
	 * @param move The last move performed by the opponent.
	 */	
	public static void learn(int color, Move move)
	{
		int hops[] = new int[move.getNumberOfHops()+1];
		int hits[] = new int[move.getNumberOfCapturedPeons()];
		int i = 1;
		hops[0] = encode(move.getPeon().getPoint());
		for (Enumeration en = move.getHops(); en.hasMoreElements(); i++)
		{
			Point p = (Point) en.nextElement();
			hops[i] = encode(p);
		}
		i = 0;
		for (Enumeration en = move.getCapturedPeons(); en.hasMoreElements(); i++)
		{
			Peon p = (Peon) en.nextElement();			
			hits[i] = encode(p.getPoint());
		}
		learn(color, hops, hits);
	}

	/**
	 * Creates a Point, given its representation at the native code.
	 * The native representation is the location of a bit that stands for the point
	 * in a bitset.
	 * @param num native point representation.
	 * @return new Point object.
	 */	
	private static Point decode(int num)
	{
		int row = num / (Game.getInstance().getBoardSize() / 2);
		int col = (num % (Game.getInstance().getBoardSize() / 2)) * 2;
		if ((row + col) % 2 == 0) col++;
		return new Point(row, col);
	}

	/**
	 * Encodes a Point to its native code representation.
	 * @param p the Point to encode.
	 * @return position of the Point in a bitset board representation.
	 */	
	private static int encode(Point p)
	{
		return p.getRow() * (Game.getInstance().getBoardSize() / 2) + p.getCol() / 2;
	}
}


