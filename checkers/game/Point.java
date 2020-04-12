/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.io.Serializable;

/**
 * The Point class.
 * A representation of row and column.
 */
public class Point implements Serializable
{
	
	/**
	 * row of point 
	 */
	private int _row;
	
	/**
	 * coloumn of point
	 */
	private int _col;
	
	
	/**
	 * construct point with row and coloumn
	 * @param row The row position of Point
	 * @param col The coloumn position of Point
	 */
	public Point(int row, int col)
	{
		_row = row;
		_col = col;
	}
	
	/**
	 * Returns the row.
	 * @return row of Point
	 */
	public int getRow()
	{
		return _row;
	}
	
	/**
	 * Returns the column.
	 * @return coloumn of point
	 */
	public int getCol()
	{
		return _col;
	}
}