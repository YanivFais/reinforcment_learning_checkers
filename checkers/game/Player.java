/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;


import java.util.*;

/**
 * The abstract Player class.
 * Represents a checkers player.
 */
public abstract class Player implements java.io.Serializable
{
	/**
	 * White Player mark
	 */
	public static final byte PLAYER_WHITE = 1;	
	/**
	 * Black Player mark
	 */
	public static final byte PLAYER_BLACK = 2;
	
	/**
	 * Mark of this Player (White/Black)
	 */
	protected byte _color;
	
	/**
	 * Peons collection of the Player
	 */
	protected Vector _peons;
	
	/**
	 * Creates a new Player with the given color.
	 * @param color Player's Color
	 */
	public Player(byte color)
	{
		_color = color;
		_peons = new Vector();
	}
	
	/**
	 * Returns the color of this Player.
	 * @return color of Player
	 */
	public byte getColor()
	{
		return _color;
	}
	
	/**
	 * Adds a Peon to Player 's collection
	 * @param p The Peon to add
	 */
	public void addPeon(Peon p)
	{
		_peons.addElement(p);
	}
	
	/**
	 * Remove a Peon from Player 's collection
	 * @param p The Peon to remove from Player 's collection
	 */
	public void removePeon(Peon p)
	{
		_peons.removeElement(p);
	}
	
	/**
	 * Returns the number of Peons owned by this Player.
	 * @return number of Peon s Player has 
	 */
	public int getNumberOfPeons()
	{
		return _peons.size();
	}
	
	/**
	 * Returns the Peons owned by this Player.
	 * @return Enumeration of Peon with Player 's Peons
	 */
	public Enumeration getPeons()
	{
		return _peons.elements();
	}
	
	/**
	 * Returns true if this Player has Peons that can move.
	 * @return true iff Player has legal moves to do
	 */
	public boolean gotMoves()
	{
		int sz = _peons.size();
		for (int i=0; i < sz; i++)
		{
			Peon p = (Peon) _peons.elementAt(i);
			if (Game.getInstance().getLegalSimpleTargets(p).size() > 0)
				return true;		
		}
		return false;
	}		
	
	
	/**
	 * Returns the opposite color mark to the given color mark.
	 * @param player original Player color mark
	 * @return color reversed color mark
	 */
	public static byte reverse(byte player)	{
		return (byte)(3-player);
	}
	
	/**
	 * Plays one move.
	 * This method is abstract.
	 * @return Move The player's next Move.
	 */
	public abstract Move play();

}
