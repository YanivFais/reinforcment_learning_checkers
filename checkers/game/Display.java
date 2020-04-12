/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;

/**
 * The Display interface. 
 * Used by the game to communicate with a UI system.
 */
public interface Display
{
	/**
	 * Refreshes the Display
	 * @param undo is Undo available
	 * @param redo is Redo available
	 */
	public void update(boolean undo,boolean redo);
	
	/**
	 * Prints out a message
	 * @param message the message to show
	 */
	public void showMessage(String message);
	
	
	/**
	 * Shows move on the display
	 * @param move The Move to show
	 */
	public void showMove(Move move);
}