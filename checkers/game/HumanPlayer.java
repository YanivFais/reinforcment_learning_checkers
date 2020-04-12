/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.io.Serializable;

/**
 * The HumanPlayer class. 
 * This player waits for an input from the UI system, in order to play.
 */
public class HumanPlayer extends Player implements Serializable
{
	private Move _nextMove;

	/**
	 * Constructor.
	 * @param color The player color
	 */	
	public HumanPlayer(byte color)
	{
		super(color);
	}
	
	/**
	 * Returns the next move of this player.
	 * Human player waits for notification from UI system before returning the move.
	 * @return the next move
	 */
	public Move play()
	{
		_nextMove = null;
		synchronized (this) 
		{
			try
			{
				wait();
			}
			catch (InterruptedException ie)
			{
			}
		}
		return _nextMove;
	}
	
	
	/**
	 * Sets the next move for this player.
	 * This method is called by the UI system.
	 * @param move
	 */
	public void setNextMove(Move move)
	{
		_nextMove = move;
		synchronized (this)
		{
			notify();
		}
	}

}
