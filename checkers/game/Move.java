/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

import java.util.*;

/**
 * The Move class.
 */
public class Move implements java.io.Serializable
{
	/**
	 * The moving peon.
	 */
	private Peon _peon;
	
	/**
	 * The row where the move starts.
	 */
	private int _sourceRow;
	
	/**
	 * The coloumn where the move starts.
	 */
	private int _sourceCol;
	
	/**
	 * The row where the move ends.
	 */
	private int _targetRow;
	
	/**
	 * The column where the move ends.
	 */
	private int _targetCol;
	
	/**
	 * Points on the way of Move
	 */
	private Vector _hops;
	
	/**
	 * Opponent Peons captured during the move
	 */
	private Vector _capturedPeons;
	
	/**
	 * Constructs a Move for a Peon.
	 * @param peon The Peon that moves.
	 */
	public Move(Peon peon)
	{
		_peon = peon;
		_hops = new Vector();
		_capturedPeons = new Vector();
	}
	

	/**
	 * Construct Move for Peon with a target Point.
	 * The initial move position is determined accroding to the Peon's current position.
	 * @param peon The Peon that moves.
	 * @param target Point where the move ends.
	 */
	public Move(Peon peon, Point target)
	{
		this(peon);
		_hops.addElement(target);		
	}
	
	/**
	 * Adds a point on the way of move.
	 * @param p a Point on the way of move.
	 */
	public void addPoint(Point p)
	{
		if (!(_hops.contains(p)))
			_hops.addElement(p);
	}

	/**
	 * Returns the moving Peon.
	 * @return Moving Peon
	 */
	public Peon getPeon()
	{
		return _peon;
	}
	
	/**
	 * Adds a captured opponent Peon
	 * @param peon The captured Peon
	 */
	public void addCapturedPeon(Peon peon)
	{
		_capturedPeons.addElement(peon);
	}
	
	/**
	 * Returns the Points along the Move.
	 * @return Hops of Point in the Move way
	 */
	public Enumeration getHops()
	{
		return _hops.elements();
	}
	
	/**
	 * Returns the Peons that are captured during the Move.
	 * @return peons captured during Move
	 */
	public Enumeration getCapturedPeons()
	{
		return _capturedPeons.elements();
	}
	
	/**
	 * Returns the final destination of the Move.
	 * @return final destination of Move
	 */
	public Point getLastHop()
	{
		return (Point) _hops.lastElement();
	}
	
	/**
	 * Removes the destination point of the Move
	 */
	public void removeLastHop()
	{
		if (_hops.size() > 0)
			_hops.removeElementAt(_hops.size()-1);
	}
	
	/**
	 * Returns the number of Points on the Move.
	 */
	public int getNumberOfHops()
	{
		return _hops.size();
	}

	/**
	 * Returns the number of captures on the Move.
	 */
	public int getNumberOfCapturedPeons()
	{
		return _capturedPeons.size();
	}


}

