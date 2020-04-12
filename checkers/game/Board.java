/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * The Board class.
 * Represents the checkers game board.
 */
public class Board implements Cloneable,Serializable
{

	/**
	 * Matrix of peons.
	 */	
	private Peon[][] _peons;
	
	private Board()
	{
	}

	/**
	 * Constructor for Board.
	 * @param size dimension of the board
	 * @param rows number of starting rows for each player
	 */
	public Board(int size, int rows) 
	{
		_peons = new Peon[size][size];
		Player white = Game.getInstance().getWhite();
		Player black = Game.getInstance().getBlack();
		for (int row = 0; row < rows; row++)
			for (int col = 0; col < size; col +=2)
			{
				int redOffset = (row%2==0)?1:0;
				int blackOffset = (row%2==0)?0:1;
				Peon p1 = new Peon(white, row, col+redOffset);	
				white.addPeon(p1);
				Peon p2 = new Peon(black, size - row - 1, col+blackOffset);
				black.addPeon(p2);
				_peons[row][col+redOffset] = p1;
				_peons[size - row - 1][col+blackOffset] = p2;
			}		
	}
	
	/**
	 * Returns the peon located at the given point.
	 * @param point the requested Point
	 * @return the peon located at the Point, or null.
	 */
	public Peon getPeonAt(Point point)
	{
		return _peons[point.getRow()][point.getCol()];
	}
	
	/**
	 * Positions a peon on the board.
	 * @param point the requested Point
	 * @param peon the peon to postion, or null to clear the square
	 */
	public void setPeonAt(Point point, Peon peon)
	{
		_peons[point.getRow()][point.getCol()] = peon;
	}
	

	/**
	 * Returns the peon located at the given row and column.
	 * @param row the requested row
	 * @param col the requested column
	 * @return the peon located at the given position, or null.
	 */
	public Peon getPeonAt(int row, int col)
	{
		return _peons[row][col];
	}	
	
	
	/**
	 * Updates board status according the the given Move.
	 * This method assumes that the given Move is legal.
	 * @param move the Move to perform
	 * @return true on success
	 */
	public boolean makeMove(Move move)
	{		
		if (move == null) return false;
		int sourceRow = move.getPeon().getRow();
		int sourceCol = move.getPeon().getCol();
		int targetRow = move.getLastHop().getRow();
		int targetCol = move.getLastHop().getCol();
		Player white = Game.getInstance().getWhite();
		Player black = Game.getInstance().getBlack();
		move.getPeon().move(move.getLastHop());
		_peons[sourceRow][sourceCol] = null;
		_peons[targetRow][targetCol] = move.getPeon();
		for (Enumeration en = move.getCapturedPeons(); en.hasMoreElements();)
		{
			Peon p = (Peon) en.nextElement();
			_peons[p.getRow()][p.getCol()] = null;
			if (p.getPlayer().equals(white))
				white.removePeon(p);
			else	
				black.removePeon(p);
		}
		if (move.getPeon().getPlayer().equals(white))
		{
			if (targetRow == _peons.length - 1)
			{
				King king = new King(white, targetRow, targetCol);
				white.removePeon(move.getPeon());
				white.addPeon(king);
				_peons[targetRow][targetCol] = king;		
			}
		}
		else
		{
			if (targetRow == 0)
			{
				King king = new King(black, targetRow, targetCol);
				black.removePeon(move.getPeon());
				black.addPeon(king);
				_peons[targetRow][targetCol] = king;		
			}		
		}
		return true;
	}	
	
	/**
	 * Creates an encoded representation of the board.
	 * The representation consists of 5 32-bit integers.
	 * The first integer bits indicate locations of black peons.
	 * The second integer bits indicate locations of black kings. 
	 * The third integer bits indicate locations of white peons.
	 * The fourth integer bits indicate locations of white kings. 
	 * The fifth integer bits indicate empty squares (0=empty, 1=taken).
	 */
	public long[] encode()
	{
		long i=0x0001;
		long blackPeons = 0;
		long blackKings = 0;
		long whitePeons = 0;
		long whiteKings = 0;
		long usedSquares = 0;
		for (int row=0; row<Game.getInstance().getBoardSize(); row++)
			for (int col=0; col<Game.getInstance().getBoardSize(); col++)
			{
				if ((row+col)%2 == 0) continue; //white square
				Peon p = _peons[row][col];
				if (p != null)
				{
					usedSquares |= i;
					if (p instanceof King)
					{
						if (p.getPlayer().getColor() == Player.PLAYER_BLACK)
							blackKings |= i;
						else
							whiteKings |= i;
					}
					else
					{
						if (p.getPlayer().getColor() == Player.PLAYER_BLACK)
							blackPeons |= i;
						else
							whitePeons |= i;
					}
							
				}
				i = i << 1;
			}
		return new long[]{blackPeons, blackKings, whitePeons, whiteKings, usedSquares};
	}
	
	/**
	 * Checks Board equality. Overrides the inherited behaviour of the equals method.
	 * @param other Board to compare
	 * @return true iff this==other (in terms of overall look,not specific peons)
	 */
	public boolean equals(Board other)
	{
		if (other == null) return false;
		for (int row=0; row<Game.getInstance().getBoardSize(); row++)
			for (int col=0; col<Game.getInstance().getBoardSize(); col++)
			{
				if ((row+col)%2 == 0) continue; //white square
				Peon p1 = _peons[row][col];
				Peon p2 = other._peons[row][col];
				//System.out.println("["+row+","+col+"] "+p1+" "+p2);
				
				if (p1 == null && p2 == null) continue;
				if (p1 == null || p2 == null) return false;
				if ((p1 instanceof King) == (p2 instanceof King))
					if (p1.getPlayer().getColor() == p2.getPlayer().getColor())
						continue;
				/*if (p1 != p2) */return false;
			}
		return true;
	}
	
	/**
	 * Returns a clone of this Board object
	 * @return A new board, similar to the original
	 */
	public Object clone()
	{
		Board b = new Board();
		int sz = Game.getInstance().getBoardSize();
		b._peons = new Peon[sz][sz];
		for (int row=0; row<Game.getInstance().getBoardSize(); row++)
			for (int col=0; col<Game.getInstance().getBoardSize(); col++)
			{
				if ((row+col)%2 == 0) continue; //white square
				b._peons[row][col] = _peons[row][col];
			}		
		return b;
	}
	
	/**
	 * Updates the peons inner point to board matrix points
	 * @see Game.update
	 */
	public void updatePeonsLocations()
	{
		for (int row=0; row<Game.getInstance().getBoardSize(); row++)
			for (int col=0; col<Game.getInstance().getBoardSize(); col++)
			{
				if ((row+col)%2 == 0) continue; //white square
				if (_peons[row][col]!=null)
					_peons[row][col].move(new Point(row,col));
			}	
	}
}