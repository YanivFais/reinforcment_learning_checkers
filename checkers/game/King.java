/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.util.*;

/**
 * The King class.
 * The King is a Peon that can move both forward and backward.
 */
public class King extends Peon 
{
	
	public King(Player player, int x, int y)
	{
		super(player, x, y);
	}
	
	/**
	 * @param hitOnly Indicator for getting only possible hit moves targets 
	 * @return Enumeration of Points which are targets of possible Moves for peon
	 * 		  (simple moves only,don't have to be legal...)  
	 */
	public Enumeration getPossibleSimpleTargets(boolean hitOnly)
	{
		Vector targets = new Vector();
		for (int row = _point.getRow()-2;row <= _point.getRow()+2;row++)
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
