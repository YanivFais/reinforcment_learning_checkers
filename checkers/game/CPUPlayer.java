/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.io.Serializable;

import checkers.library.NativeCheckers;
import checkers.game.Game;
import checkers.game.Move;
import checkers.game.Player;

/**
 * CPU Player.
 */
public class CPUPlayer extends Player implements Serializable
{
	/**
	 * CPU Player's level.
	 */
	private int _level;
	

	/**
	 * Constructor.
	 * @param color Player color
	 * @param level CPU Player's level
	 */
	public CPUPlayer(byte color, int level)
	{
		super(color);
		_level = level;
	}
	
	/**
	 * Plays next move.
	 * This method uses the native bridge.
	 * @return The played Move
	 * @see checkers.library.NativeCheckers
	 */
	public Move play()
	{
		Move move = null;		
		try
		{
			Thread.sleep(500);
			move = NativeCheckers.calculateMove(this);
			Game.getInstance().isLegalMove(move);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return move;
	}
	
	/**
	 * Returns the CPU Player's level.
	 * @return level of Player
	 */
	public int getLevel()
	{
		return _level;
	}
}
