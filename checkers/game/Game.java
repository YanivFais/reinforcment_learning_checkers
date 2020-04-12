/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.game;
import java.util.*;
import checkers.library.NativeCheckers;
import java.io.*;


/**
 * The Game class.
 * The Game is responsible of the game control.
 * Implemented as a singleton, i.e., only one instance of this class may exist.
 */
public class Game implements Runnable,java.io.Serializable
{
	public static final String RESULTS_FILE = "data/results.dat";
	
	private static final String PROPERTIES_FILE = "checkers.properties";
	
	public static final String MAX_LEVEL_PROPERTY = "maxLevel";
	
	/**
	 * The one and only instance of Game
	 */
	private static Game _singleton;

	/**
	 * Size of Board (number of rows or columns)
	 */
	private int _boardSize;

	/**
	 * White Player
	 */
	private Player _whitePlayer;

	/**
	 * Back Player
	 */
	private Player _blackPlayer;

	/**
	 * Game board
	 */
	private Board _board;

	/**
	 * Game display (UI system)
	 */
	private static Display _display;

	/**
	 * Determines whose Player's turn to play.
	 */
	private byte _turn;

	/**
	 * Indicator to stop the current game.
	 */
	private boolean _stop;

	/**
	 * Determines whether online learning is active.
	 */
	private boolean _onlineLearning;

	/**
	 * Game control thread.
	 */
	private Thread _gameThread;

	/**
	 * Holds previous board for draw detection and Undo/Redo mechanism
	 */
	private Vector _history;

	/**
	 * Keeps location of current board within the history vector for Redo/Undo
	 */
	private int _historyLocation;

	/**
	 * Indicator for updated fields for "run" thread exit
	 */
	private boolean _updated;

	/**
	 * Task to do while updating in background
	 */
	private String _task;

	/**
	 * Size of undo for game (1 for Human Vs. CPU, 2 for Human Vs. Human)
	 */
	private int _undoSize;

	/**
	 * Indicates whether an opening book is used by machine players.
	 */
	private boolean _useOpeningBook;
	
	/**
	 * Game Properties
	 */
	private Properties _properties;

	/**
	 * Set the display system of the game.
	 * @param display game's display.
	 */
	public static void setDisplay(Display display)
	{
		_display = display;
	}

	/**
	 * Construct a new Game. 
	 * The constructor has a private access, since only one instance of this class can exist.
	 */
	private Game()
	{
		_stop = false;
		_turn = Player.PLAYER_WHITE;
		_updated = false;
		_properties = new Properties();
		try
		{
			FileInputStream fis = new FileInputStream(PROPERTIES_FILE);
			_properties.load(fis);
		}
		catch (IOException ioe)
		{
			System.err.println("Error reading game properties file: "+ioe.getMessage());
		}
	}
	
	public String getGameProperty(String propertyName)
	{
		return _properties.getProperty(propertyName);
	}

	/**
	 * Stops the game thread.
	 */
	public void stopGame()
	{
		synchronized (this)
		{
			_stop = true;
		}
	}

	/**
	 * Start a new game
	 * @param white white Player
	 * @param black black Player
	 * @param boardSize size of board
	 * @param rows number of starting rows per player
	 */
	public void startGame(Player white, Player black, int boardSize, int rows)
	{
		_stop = false;
		_updated = false;
		_whitePlayer = white;
		_blackPlayer = black;
		_boardSize = boardSize;
		NativeCheckers.setBoardSize(boardSize);
		_board = new Board(boardSize, rows);
		_history = new Vector();
		_historyLocation = 0;
		_turn = Player.PLAYER_WHITE;
		_gameThread = new Thread(this);
		_gameThread.start();
	}

	/**
	 * Restarts game with updated field
	 */
	private void reStartGame()
	{
		if (! _stop)
			stopGame();
		_stop = false;
		_updated = true; // set update in game fields
		_gameThread = new Thread(this);
		_gameThread.start();
	}

	/**
	 * Returns the instance of this class.
	 * @return The one and only instance of the Game
	 */
	public static Game getInstance()
	{
		if (_singleton == null)
			_singleton = new Game();
		return _singleton;
	}

	/**
	 * Return the Peon at the requested position.
	 * @param row row of peon
	 * @param col column of peon
	 * @return Peon at location
	 */
	public Peon getPeonAt(int row, int col)
	{
		return _board.getPeonAt(row, col);
	}

	/**
	 * Return the Peon at the requested position.
	 * @param p Point of Peon
	 * @return Peon at location
	 */
	public Peon getPeonAt(Point p)
	{
		return _board.getPeonAt(p);
	}

	/**
	 * Reutrn the Player color whose turn is to play now.
	 * @return color of Player
	 */
	public byte getCurrentPlayerColor()
	{
		return _turn;
	}

	/**
	 * Setting online learning feature
	 * @param learning indicator for enable
	 */
	public void setOnlineLearning(boolean learning)
	{
		_onlineLearning = learning;
		NativeCheckers.setOnlineLearning(learning);
	}

	/**
	 * Returns the status of the online learning feature.
	 * @return boolean status of online learning feature
	 */
	public boolean getOnlineLearning()
	{
		return _onlineLearning;
	}


	/**
	 * Checks the legality of a Move.
	 * The Given Move may be a partial Move, i.e., a jump that should be continued.
	 * @param move Move the check legality
	 * @return true iff Move is legal
	 */
	public boolean isLegalPartialMove(Move move)
	{
		return isLegalMove(move, true);
	}

	/**
	 * Checks the legality of a Move.
	 * The Move must be a complete legal move to pass this test.
	 * @param move Move the check legality
	 * @return true iff Move is legal
	 */
	public boolean isLegalMove(Move move)
	{
		return isLegalMove(move, false);
	}


	/**
	 * Checks the legality of a complete or partial move.
	 * @param move The move to check legality of.
	 * @param partial if partial move allowed.
	 * @return true iff move is legal.
	 */
	private boolean isLegalMove(Move move, boolean partial)
	{
	 	 if (move == null) return false;
		 boolean kingMove = move.getPeon() instanceof King;
		 Enumeration hops = move.getHops();
		 Point start = move.getPeon().getPoint();
		 Point source = move.getPeon().getPoint();
		 Point target = null;
		 for ( ; hops.hasMoreElements() ; source = target)
		 {
			target = (Point)hops.nextElement();
			int rowMove = target.getRow() - source.getRow();
			int colMove = target.getCol() - source.getCol();

			if (_board.getPeonAt(target.getRow(),target.getCol()) != null)
				return false; // must be empty..

			int rowAdvance =   (move.getPeon().getPlayer().getColor() == Player.PLAYER_BLACK)
								? -1 : 1;
			if (( (rowMove == rowAdvance)||(kingMove && Math.abs(rowMove)==1))
				&& (Math.abs(colMove) == 1) /*&& first*/)
			{ // regular move -> good direction & one col & first (or king backwards)
				return (!hops.hasMoreElements() && !isCompulsory(move.getPeon().getPlayer()));
			}  // if regular move than no more moves and no compulsory...
			else if (  (Math.abs(rowMove) == 2) && (Math.abs(colMove)==2)  // hitting
					 && (( rowMove*rowAdvance > 0 ) || /*!first ||*/ kingMove))
					 {
						// (first & good direction) | !first || king
  						Point hitPoint = new Point((target.getRow()+source.getRow())/2,
						 						     (target.getCol()+source.getCol())/2);
						Peon capturedPeon = _board.getPeonAt(hitPoint.getRow(),hitPoint.getCol());
						if (capturedPeon == null)
							return false;
						if (capturedPeon.getPlayer().equals(move.getPeon().getPlayer()))
							return false;
						move.addCapturedPeon(capturedPeon);
						//first = false;
					 }
			else return false;
		}
		if (move.getCapturedPeons().hasMoreElements() &&  !partial && target != null)
		{
			Peon peon = move.getPeon();
			peon.move(target);
			for (Enumeration captured = move.getCapturedPeons(); captured.hasMoreElements();)
			{
				Peon p = (Peon) captured.nextElement();
				_board.setPeonAt(p.getPoint(), null);
			}
			for (Enumeration possibleTargets = peon.getPossibleSimpleTargets(true);
				possibleTargets.hasMoreElements();)
				{
					Point t = (Point)possibleTargets.nextElement();
					Move mv = new Move(peon,t);
					if (isLegalPartialMove(mv))
					{
						if (mv.getCapturedPeons().hasMoreElements())
						{
							peon.move(start);
							for (Enumeration captured = move.getCapturedPeons(); captured.hasMoreElements();)
							{
								Peon p = (Peon) captured.nextElement();
								_board.setPeonAt(p.getPoint(), p);
							}

							return false;
						}
					}
				}
			peon.move(start);
			for (Enumeration captured = move.getCapturedPeons(); captured.hasMoreElements();)
			{
				Peon p = (Peon) captured.nextElement();
				_board.setPeonAt(p.getPoint(), p);
			}

		}
		return true;
	}

	/**
	 * Checks if the given Player has any forced captured to take.
	 * @param player The player which might have compulsory moves
	 * @return true iff player has compulsory moves
	 */
	private boolean isCompulsory(Player player)
	{
	  	for (Enumeration peons = player.getPeons();peons.hasMoreElements();)
	  	{
	  		Peon peon = (Peon) peons.nextElement();
			for (Enumeration hitTargets = peon.getPossibleSimpleTargets(true);
				hitTargets.hasMoreElements();)
				{
					Move move = new Move(peon,(Point)hitTargets.nextElement());
					if (isLegalPartialMove(move))
						if (move.getCapturedPeons().hasMoreElements())
							return true;
				}
	  	}
	  	return false;
	}

	/**
	 * Returns the points to which a Peon can move.
	 * @param peon The peon to request legal moves targets for
	 * @return Enumeration of Point with legal simple targets
	 */
	public Vector getLegalSimpleTargets(Peon peon)
	{
		Vector targets = new Vector();
		for (Enumeration possibleTargets = peon.getPossibleSimpleTargets(false);
			possibleTargets.hasMoreElements();)
			{
				Point target = (Point)possibleTargets.nextElement();
				Move move = new Move(peon,target);
				if (isLegalPartialMove(move))
					targets.addElement(target);
			}
		return targets;
	}

	/**
	 * Returns the white Player.
	 * @return white color Player
	 */
	public Player getWhite()
	{
		return _whitePlayer;
	}

	/**
	 * Returns the black Player.
	 * @return black color Player
	 */
	public Player getBlack()
	{
		return _blackPlayer;
	}


	/**
	 * Return the Game's board dimensions (number of rows or columns).
	 * @return size of the board
	 */
	public int getBoardSize()
	{
		return _boardSize;
	}


	/**
	 * Checks for a draw situation.
	 * A draw is announced it the same board position appeared three times.
	 * Also adds the current board to the history, updates _historyLocation to last (discards "Redo")
	 * @return true if draw should be announced.
	 */
	private boolean checkDraw()
	{
		int count = 0;
		for (int i = 0; i < _historyLocation; i++)
		{
			if (_board.equals((Board)(_history.elementAt(i))))
			{
				count++;
			}
		}
		if (_historyLocation+1 < _history.size())
		 // there is "Redo" information but played another move...
			_history.setSize(_historyLocation+1);
		_historyLocation++;
		_history.addElement((Board)_board.clone());
		return (count >= 2);
	}

	/**
	 * Updates action for game
	 * Task may be: Redo/Undo/Restart
	 * The task may be performed later, according to the game thread's state.
	 * @param Task name of the task to perform.
	 */
	public void update(String task)
	{
		_updated = true; // set update in game fields
		_task = task;
	}

	/**
	 * Updates the game according to the task
	 */
	private synchronized void update()
	{

		if (_task.equals("Redo"))
			reDo();
		else if (_task.equals("Undo"))
			unDo();
		else if (_task.equals("Restart"))
			reStartGame();
		_updated = false;
		_display.update(isUndoAvailable(),isRedoAvailable());
	}

	/**
	 * Undo the last move
	 */
	private synchronized void unDo()
	{
		if (isUndoAvailable()==true)
		{
			NativeCheckers.unDo(_undoSize);
			_historyLocation-=_undoSize;
			_turn = (_undoSize%2!=0) ? Player.reverse(_turn) : _turn ;
			updateBoardToHistory();
		}
	}

	/**
	 * Redo the last move that was "Undone".
	 */
	private synchronized void reDo()
	{
		if (isRedoAvailable()==true)
		{
			NativeCheckers.reDo(_undoSize);
			_historyLocation+=_undoSize;
			_turn = (_undoSize%2!=0) ? Player.reverse(_turn) : _turn ;
			updateBoardToHistory();
		}
	}

	/**
	 * Update the board to the history status.
	 */
	private synchronized void updateBoardToHistory()
	{
		_board = (Board)((Board)_history.elementAt(_historyLocation)).clone();
		_board.updatePeonsLocations();
	}

	/**
	 * Checks if Redo can be performed.
	 * @return true iff redo is available
	 */
	private synchronized boolean isRedoAvailable()
	{
		return (_historyLocation + _undoSize < _history.size());
	}

	/**
	 * Checks if Undo can be performed.
	 * @return true iff undo is available
	 */
	private synchronized boolean isUndoAvailable()
	{
		return (_historyLocation > _undoSize-1);
	}

	/**
	 * Sets the "Undo" size.
	 * @param size size of undo in game
	 */
	public void setUndoSize(int size)
	{
		_undoSize = size;
	}


	/**
	 * Runs the game thread. Implements the run() method of Runnable.
	 * This is the main control method, which allows each player to make its move
	 * on its turn.
	 */
	public synchronized void run()
	{
		NativeCheckers.clearHistory();
		_history.addElement(_board.clone());
		_historyLocation = 0;
		boolean draw = false;
		byte winner = -1;
		Player white = _whitePlayer;
		Player black = _blackPlayer;
		while (Thread.currentThread() == _gameThread)
		{
			try {
				synchronized (this)
				{
					if (_stop || draw) break;
					if (white.getNumberOfPeons() > 0 &&  black.getNumberOfPeons() > 0)
					{
						if (_turn == Player.PLAYER_WHITE)
						{
							_display.showMessage("White's turn");
							if (!white.gotMoves())
							{
								winner = Player.PLAYER_BLACK;
								break;
							}
							Move move = white.play();
							if (_updated)
							{
								update();
								continue;
							}
							if (move == null)
							{
								break;
							}
							if (_onlineLearning && (black instanceof CPUPlayer))
								NativeCheckers.learn(Player.PLAYER_BLACK, move);
							_display.showMove(move);
							_board.makeMove(move);
							draw = checkDraw();
							_turn = Player.PLAYER_BLACK;
						}
						else
						{
							_display.showMessage("Black's turn");
							if (!black.gotMoves())
							{
								winner = Player.PLAYER_WHITE;
								break;
							}
							Move move = black.play();
							if (_updated)
							{
								update();
								continue;
							}
							if (move == null)
							{
								break;
							}
							if (_onlineLearning && (white instanceof CPUPlayer))
								NativeCheckers.learn(Player.PLAYER_WHITE, move);
							_display.showMove(move);
							_board.makeMove(move);
							draw = checkDraw();
							_turn = Player.PLAYER_WHITE;
						}
					}
					else
					{
						if (white.getNumberOfPeons() == 0)
							winner = Player.PLAYER_BLACK;
						else
							winner = Player.PLAYER_WHITE;
						break;
					}
					if (!_stop)
						_display.update(isUndoAvailable(),isRedoAvailable());
				}
				wait(100); // allows other thread to stop the game

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		int whiteVictories = 0;
		int blackVictories = 0;
		int draws = 0;
		try
		{
			FileInputStream	fis = new FileInputStream(RESULTS_FILE);
			whiteVictories = fis.read();
			blackVictories = fis.read();
			draws = fis.read();
			fis.close();
		}
		catch (Exception ex)
		{
		}
		if (draw) winner = 0;
		switch (winner)
		{
			case Player.PLAYER_BLACK:
				_display.showMessage("Game Over, Black wins");
				++blackVictories;
				break;
			case Player.PLAYER_WHITE:
				_display.showMessage("Game Over, White wins");
				++whiteVictories;
				break;
			case 0:
				_display.showMessage("Game Over, Draw");
				++draws;
				break;
		}
		try
		{
			FileOutputStream fos = new FileOutputStream(RESULTS_FILE);
			fos.write(whiteVictories);
			fos.write(blackVictories);
			fos.write(draws);
			fos.close();
		}
		catch (Exception ex)
		{
		}
		NativeCheckers.clearHistory();
		_history.clear();
		_historyLocation = 0;
		_display.update(isUndoAvailable(),isRedoAvailable());
	}

	/**
	 * Returns the current Board.
	 * @return Board of Game
	 */
	public Board getBoard()
	{
		return _board;
	}

	/**
	 * Reads game from an ObjectInputStream.
	 * @param stream ObjectInputStream to read from
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream stream)
		throws IOException, ClassNotFoundException {
		String ID = "Checkers Game File";
		for (int i=0; i < ID.length(); i++)
			if (stream.readChar() != ID.charAt(i))
				throw new IOException("Invalid File Type");

		_boardSize = stream.readInt();
		_turn =  stream.readByte();
		_board = (Board) stream.readObject();
		_blackPlayer = (Player) stream.readObject();
		_whitePlayer = (Player) stream.readObject();
	}

	/**
	 * Writes game to an ObjectOutputStream
	 * @param stream ObjectOutputStream to write to
	 * @throws IOException when write fails
	 */
	private void writeObject(java.io.ObjectOutputStream stream)
		throws IOException
	{
		stream.writeChars("Checkers Game File");
		stream.writeInt(_boardSize);
		stream.writeByte(_turn);
		stream.writeObject(_board);
		stream.writeObject(_blackPlayer);
		stream.writeObject(_whitePlayer);
	}

	/**
	 * This method is called immediately after an object of this class is deserialized.
	 * @return The singleton instance.
	 */
	protected Object readResolve() 
	{
		_singleton._blackPlayer = _blackPlayer;
		_singleton._whitePlayer = _whitePlayer;
		_singleton._board = _board;
		_singleton._turn = _turn;
		_singleton._boardSize = _boardSize;
		_singleton._history = new Vector();
		_singleton._historyLocation = 0;
		return _singleton;
	}

	/**
	 * Enables or disables the usage of the opening book.
	 * @param use defines whether the opening book should be used.
	 */
	public void setOpeningBookUsed(boolean use)
	{
		_useOpeningBook = use;
	}

	/**
	 * Returns the status of opening book usage.
	 * @return true, iff an opening book is used.
	 */
	public boolean isOpeningBookUsed()
	{
		return _useOpeningBook;
	}
}
