/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.io.Serializable;
import java.util.*;

/**
 * The Peon class.
 * 
 * Represents a checkers peon (also called "checker").
 */
public class Peon implements Serializable
{
	/**
	 * Peon's current position on the board.
	 */
	protected Point _point;
	
	/**
	 * Peon's owner
	 */
	protected Player _player;
	
	
	/**
	 * Constructor.
	 * @param player peon's owner
	 * @param row initial row
	 * @param col initial column
	 */
	public Peon(Player player, int row, int col)
	{
		_player = player;
		_point = new Point(row, col);
		
	}
	
	/**
	 * Returns the current position of the peon
	 * @return the Point in which the peon is located.
	 */
	public Point getPoint()
	{
		return _point;
	}

	/**
	 * Returns the current row in which the peon is located.
	 * @return the row in which the peon is located.
	 */
	public int getRow()
	{
		return _point.getRow();
	}

	/**
	 * Returns the current column in which the peon is located.
	 * @return the column in which the peon is located.
	 */
	public int getCol()
	{
		return _point.getCol();
	}

	/**
	 * Sets the peon position
	 * @param newPoint the new position of the peon.
	 */
	public void move(Point newPoint)
	{
		_point = newPoint;
	}

	/**
	 * Returns the player who owns this peon
	 * @return Peon's owner.
	 */ 	
	public Player getPlayer()
	{
		return _player;
	}
	

	/**
	 * Returns an Enumeration of the available moves for this peon.
	 * 
	 * @param hitOnly Indicator for getting only possible hit moves targets 
	 * @return Enumeration of Points which are targets of possible Moves for peon
	 * 		  (simple moves only,don't have to be legal...)  
	 */
	public Enumeration getPossibleSimpleTargets(boolean hitOnly)
	{
		Vector targets = new Vector();
		int rowAdd = (_player.getColor() == Player.PLAYER_BLACK) ? -1 : 1;
		for (int row = _point.getRow()+rowAdd;row != _point.getRow()+rowAdd*3;row+=rowAdd)
		{
			int rowDistance = Math.abs(row - _point.getRow());
			if (hitOnly && rowDistance!=2)
				continue;
			for (int col = _point.getCol()-rowDistance;col <= _point.getCol()+rowDistance;col+=2)
			{
				if (row>=0 && row < Game.getInstance().getBoardSize() && col>=0 && col<Game.getInstance().getBoardSize() )
				 // legal boundaries
					targets.addElement(new Point(row,col));					
			}
		}
		return targets.elements();		 
	}	
	


}
