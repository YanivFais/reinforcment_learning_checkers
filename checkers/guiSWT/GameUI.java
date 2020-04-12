/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.guiSWT;

import java.io.*;
import java.util.*;
import swingwt.awt.*;
import swingwt.awt.event.*;
import java.applet.*;
import java.net.*;

import swingwtx.swing.*;
import swingwtx.swing.border.*;
 
import checkers.game.Player;
import checkers.game.CPUPlayer;
import checkers.game.HumanPlayer;
import checkers.game.Point;
import checkers.game.Display;
import checkers.game.Game;
import checkers.game.Move;
import checkers.game.Peon;
import checkers.game.King;


/**
 * The Game User Interface class.
 */
public class GameUI extends JFrame
	implements ActionListener, MouseListener, Display
{
	/**
	 * Singleton instance of GameUI.
	 */
	private static GameUI _instance;
	
	// default and initial board settings
	private static final int DEFAULT_BOARD_SIZE = 8;
	private static final int DEFAULT_NUMBER_OF_ROWS = 3; // number of rows filled with peons
	
	// Folder names
	private static final String IMAGES_FOLDER = "images/";
	private static final String AUDIO_FOLDER = "sound/";
	private static final String HELP_FOLDER = "help/";
	private static final String SAVED_GAMES_FOLDER = "saved-games/";
	
	
	// Images
	private static final String SOUND_ON_ICON = IMAGES_FOLDER+"soundOn.gif";
	private static final String SOUND_OFF_ICON = IMAGES_FOLDER+"soundOff.gif";
	private static final String NEW_ICON = IMAGES_FOLDER+"board.gif";
	private static final String RESTART_ICON = IMAGES_FOLDER+"restart.gif";
	private static final String OPEN_ICON = IMAGES_FOLDER+"open.gif";
	private static final String SAVE_ICON = IMAGES_FOLDER+"save.gif";
	private static final String UNDO_ICON = IMAGES_FOLDER+"undo.gif";
	private static final String REDO_ICON = IMAGES_FOLDER+"redo.gif";
	private static final String STATS_ICON = IMAGES_FOLDER+"chart.gif";
	private static final String HELP_ICON = IMAGES_FOLDER+"help-icon.gif";
	private static final String ABOUT_ICON = IMAGES_FOLDER+"about.gif";
	private static final String EXIT_ICON = IMAGES_FOLDER+"exit.gif";
	private static final String LOGO_ICON = IMAGES_FOLDER+"board.gif";
	private static final String BLACK_SQUARE_IMAGE = IMAGES_FOLDER+"black_empty.JPG";
	private static final String WHITE_SQUARE_IMAGE = IMAGES_FOLDER+"white_square.JPG";
	private static final String WHITE_PEON_IMAGE = IMAGES_FOLDER+"white_peon.JPG";
	private static final String WHITE_PEON_SMALL_IMAGE = IMAGES_FOLDER+"white_peon_small.JPG";	
	private static final String WHITE_KING_IMAGE = IMAGES_FOLDER+"white_king.JPG";
	private static final String BLACK_PEON_IMAGE = IMAGES_FOLDER+"black_peon.JPG";
	private static final String BLACK_PEON_SMALL_IMAGE = IMAGES_FOLDER+"black_peon_small.JPG";
	private static final String BLACK_KING_IMAGE = IMAGES_FOLDER+"black_king.JPG";
	private static final String UNIVERSITY_ICON = IMAGES_FOLDER+"tau.gif";
	
	// Saved games file extension
	private static final String SAVED_GAME_EXTENSION = ".ckg";
	

	/**
	 * GUI widgets
	 */
	private ImageIcon _whiteSquare;
	private ImageIcon _blackSquare;
	private ImageIcon _blackSquareBlackPeon;
	private ImageIcon _blackSquareBlackKing;
	private ImageIcon _blackSquareWhitePeon;
	private ImageIcon _blackSquareWhiteKing;
	private ImageIcon _smallBlackPeon;
	private ImageIcon _smallRedPeon;
	private ImageIcon _soundOnIcon;
	private ImageIcon _soundOffIcon;
	private JMenuItem _learningItem;
	private JMenuItem _openingBookItem;
	private StatisticsFrame _statsFrame;
	private JLabel _messageLabel;
	private JLabel _whiteScore;
	private JLabel _blackScore;
	private JMenuItem _undoItem;
	private JMenuItem _redoItem;
	private JMenuItem _saveItem;
	private JButton _undoButton;
	private JButton _redoButton;
	private JButton _saveButton;
	private JButton _soundButton;
	private JPanel _boardPanel;
	
	/**
	 * Graphical representation of the game board
	 */
	private BoardSquare[][] _squares;
	
	/**
	 *  The Game instance
	 */
	private Game _game;

	/**
	 * White Player 
	 */
	private Player _whitePlayer;
	
	/**
	 * Black Player
	 */
	private Player _blackPlayer;

	/**
	 * Current performed Move 
	 */
	private Move _currentMove;
	
	/**
	 * Legal squares for current move
	 */
	private Vector _legalTargets;

	/**
	 * Indicates if save is needed
	 */
	private boolean _needSave;

	private int _boardSize;
	private int _nextBoardSize = DEFAULT_BOARD_SIZE;
	private int _rowsNumber  = DEFAULT_NUMBER_OF_ROWS;
	
	private AudioClip _pieceMoveAudio;
	private AudioClip _pieceHitAudio;
	private AudioClip _pieceCrowningAudio;
	
	private boolean _useSound;

	private JFileChooser _fileChooser;

	/**
	 * Constructor.
	 * Builds the GUI.
	 */
	private GameUI()
	{
		setTitle("Dam Ka!");

		try
		{
			_useSound = true;
			URL urlMove = new URL("file:sound/move.wav");
			URL urlKing = new URL("file:sound/crown.au");
			URL urlHit = new URL("file:sound/hit.wav");
			_pieceMoveAudio = Applet.newAudioClip(urlMove);
			_pieceCrowningAudio =  Applet.newAudioClip(urlKing);
			_pieceHitAudio = Applet.newAudioClip(urlHit);

		}
		catch (Exception ex)
		{
		}

		Game.setDisplay(this);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		Game.getInstance().setOnlineLearning(false);
		Game.getInstance().setOpeningBookUsed(true);

		try 
		{
			_fileChooser = new JFileChooser(SAVED_GAMES_FOLDER);
			_fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			_fileChooser.setAcceptAllFileFilterUsed(false);
			_fileChooser.setFileFilter(new CKGFileChooser());
		} 
		catch (Exception e) 
		{
		}
		
		_soundOnIcon = new ImageIcon(SOUND_ON_ICON);
		_soundOffIcon = new ImageIcon(SOUND_OFF_ICON);
		_whiteSquare = new ImageIcon(WHITE_SQUARE_IMAGE);
		_blackSquare = new ImageIcon(BLACK_SQUARE_IMAGE);
		_blackSquareBlackPeon = new ImageIcon(BLACK_PEON_IMAGE);
		_blackSquareBlackKing = new ImageIcon(BLACK_KING_IMAGE);
		_blackSquareWhitePeon = new ImageIcon(WHITE_PEON_IMAGE);
		_blackSquareWhiteKing = new ImageIcon(WHITE_KING_IMAGE);
		_smallRedPeon = new ImageIcon(WHITE_PEON_SMALL_IMAGE);
		_smallBlackPeon = new ImageIcon(BLACK_PEON_SMALL_IMAGE);
		ImageIcon helpIcon = new ImageIcon(getToolkit().getImage(HELP_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon restartIcon = new ImageIcon(getToolkit().getImage(RESTART_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon loadIcon = new ImageIcon(getToolkit().getImage(OPEN_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon saveIcon = new ImageIcon(getToolkit().getImage(SAVE_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon undoIcon = new ImageIcon(getToolkit().getImage(UNDO_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon redoIcon = new ImageIcon(getToolkit().getImage(REDO_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon newIcon = new ImageIcon(getToolkit().getImage(NEW_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon statsIcon = new ImageIcon(getToolkit().getImage(STATS_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon exitIcon = new ImageIcon(getToolkit().getImage(EXIT_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		ImageIcon aboutIcon = new ImageIcon(getToolkit().getImage(ABOUT_ICON).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		this.setIconImage(getToolkit().getImage(LOGO_ICON));
		_messageLabel = new JLabel();

		getContentPane().setLayout(new BorderLayout());
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JMenuBar main = new JMenuBar();
//		main.setBorderPainted(false);
		JMenu gameMenu = new JMenu("Game");
		JMenu editMenu = new JMenu("Edit");
		JMenu learningMenu = new JMenu("Learning");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem restartItem = new JMenuItem("Restart Game", restartIcon);
		JMenuItem newItem = new JMenuItem("New Game...", newIcon);
		JMenuItem loadItem = new JMenuItem("Load Game", loadIcon);
		_saveItem = new JMenuItem("Save Game",saveIcon);
		JMenuItem exitItem = new JMenuItem("Exit", exitIcon);
		_undoItem = new JMenuItem("Undo",undoIcon);
		_redoItem = new JMenuItem("Redo", redoIcon);
		_undoItem.addActionListener(this);
		_redoItem.addActionListener(this);

		exitItem.addActionListener(this);
		newItem.addActionListener(this);
		loadItem.addActionListener(this);
		_saveItem.addActionListener(this);
		loadItem.setEnabled(true);
		_saveItem.setEnabled(false);
		restartItem.addActionListener(this);
		gameMenu.add(restartItem);
		gameMenu.add(newItem);
		gameMenu.add(new JSeparator());
		gameMenu.add(_undoItem);
		gameMenu.add(_redoItem);
		gameMenu.add(new JSeparator());
		gameMenu.add(loadItem);
		gameMenu.add(_saveItem);
		gameMenu.add(new JSeparator());
		gameMenu.add(exitItem);

		_learningItem = new JCheckBoxMenuItem("Online Learning", false);
		_openingBookItem = new JCheckBoxMenuItem("Use Opening Book", true);
		JMenuItem statsItem = new JMenuItem("Statistics...", statsIcon);
		statsItem.addActionListener(this);
		_learningItem.addActionListener(this);
		_openingBookItem.addActionListener(this);
		learningMenu.add(_learningItem);
		learningMenu.add(_openingBookItem);
		learningMenu.add(new JSeparator());
		learningMenu.add(statsItem);
		JMenuItem contentsItem = new JMenuItem("Contents",helpIcon);
		JMenuItem aboutItem = new JMenuItem("About", aboutIcon);
		aboutItem.addActionListener(this);
		contentsItem.addActionListener(this);
		helpMenu.add(contentsItem);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutItem);
		main.add(gameMenu);
		main.add(learningMenu);
		main.add(helpMenu);
		menuPanel.add(main);
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		topPanel.add(menuPanel, BorderLayout.NORTH);
		JPanel scorePanel = new JPanel();
		scorePanel.setLayout(new GridLayout(2,2));
		_whiteScore = new JLabel();
		_blackScore = new JLabel();
		scorePanel.add(new JLabel(" White:"));
		scorePanel.add(_whiteScore);
		scorePanel.add(new JLabel(" Black:"));
		scorePanel.add(_blackScore);
		getContentPane().add(scorePanel, BorderLayout.EAST);
		JPanel westPanel = new JPanel();
		getContentPane().add(westPanel, BorderLayout.WEST);
		_boardPanel = new JPanel();
		_boardPanel.setBorder(new LineBorder(Color.blue, 4));
		getContentPane().add(_boardPanel, BorderLayout.CENTER);
		JPanel statusPanel = new JPanel();
		statusPanel.add(_messageLabel);
		getContentPane().add(statusPanel, BorderLayout.SOUTH);

		JButton loadButton = new JButton(loadIcon);
		loadButton.addActionListener(this);
		loadButton.setToolTipText("Load Game");
		_saveButton = new JButton(saveIcon);
		_saveButton.addActionListener(this);
		_saveButton.setToolTipText("Save Game");
		_saveButton.setEnabled(false);
		_undoButton = new JButton(undoIcon);
		_undoButton.addActionListener(this);
		_undoButton.setToolTipText("Undo");
		_redoButton = new JButton(redoIcon);
		_redoButton.addActionListener(this);
		_redoButton.setToolTipText("Redo");
		JButton helpButton = new JButton(helpIcon);
		helpButton.addActionListener(this);
		helpButton.setToolTipText("Contents");
		_soundButton = new JButton(_soundOnIcon);
		_soundButton.setToolTipText("Sound Off");
		_soundButton.addActionListener(this);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(loadButton);
		toolBar.add(_saveButton);
		toolBar.add(_undoButton);
		toolBar.add(_redoButton);
		toolBar.add(_soundButton);
		toolBar.add(helpButton);


		topPanel.add(toolBar,BorderLayout.CENTER);
		getContentPane().add(topPanel, BorderLayout.NORTH);

		setResizable(false);
		setVisible(true);
		startNewGame(new HumanPlayer(Player.PLAYER_WHITE), new CPUPlayer(Player.PLAYER_BLACK, 1));
	}


	/**
	 * Starts a new game.
	 * @param p1 White Player
	 * @param p2 Black Player
	 */
	private void startNewGame(Player p1, Player p2)
	{
		_boardSize = _nextBoardSize;
		_game = Game.getInstance();
		_whitePlayer = p1;
		_blackPlayer = p2;
		_squares = new BoardSquare[_boardSize][_boardSize];
		_boardPanel.setLayout(new GridLayout(_boardSize,_boardSize));
		_boardPanel.removeAll();
		for (int row=0; row<_boardSize; row++)
			for (int col=0; col<_boardSize; col++)
			{
				_squares[row][col] = new BoardSquare(row,col);
				_squares[row][col].addActionListener(this);
				_squares[row][col].addMouseListener(this);
				_boardPanel.add(_squares[row][col]);
			}
		_game.setUndoSize(undoSize());
		_game.startGame(p1, p2, _boardSize ,_rowsNumber);
		update(false,false);
		_needSave = false;
		_saveButton.setEnabled(false);
		_saveItem.setEnabled(false);
		pack();
	}

	/**
	 * Implements ActionListener.
	 * @param evt The event to handle
	 */
	public void actionPerformed(ActionEvent evt)
	{
		String command = (evt.getSource() instanceof JButton)
		 ? ((JButton)evt.getSource()).getToolTipText()
		 : evt.getActionCommand();


		if (evt.getSource() instanceof BoardSquare)
			handleBoardClick((BoardSquare) evt.getSource());
		else if (evt.getSource().equals(_learningItem))
		{
			_game.setOnlineLearning(_learningItem.isSelected());
		}
		else if (evt.getSource().equals(_openingBookItem))
		{
			_game.setOpeningBookUsed(_openingBookItem.isSelected());
		}
		else if (command.equals("Restart Game"))
		{
			if (_whitePlayer instanceof HumanPlayer)
			{
				((HumanPlayer)_whitePlayer).setNextMove(null);
				_whitePlayer = new HumanPlayer(Player.PLAYER_WHITE);
			}
			else
			{
				_whitePlayer = new CPUPlayer(Player.PLAYER_WHITE, ((CPUPlayer)_whitePlayer).getLevel());
			}
			if (_blackPlayer instanceof HumanPlayer)
			{
				((HumanPlayer)_blackPlayer).setNextMove(null);
				_blackPlayer = new HumanPlayer(Player.PLAYER_BLACK);
			}
			else
			{
				_blackPlayer = new CPUPlayer(Player.PLAYER_BLACK, ((CPUPlayer)_blackPlayer).getLevel());
			}
			_game.stopGame();
			startNewGame(_whitePlayer, _blackPlayer);
		}
		else if (command.equals("New Game..."))
		{
			NewGameDialog dialog = new NewGameDialog(this);
			if (dialog.getBlackPlayer() != null)
			{
				discardMove();
				_game.stopGame();
				startNewGame(dialog.getWhitePlayer(), dialog.getBlackPlayer());
			}
		}
		else if (command.equals("Load Game"))
		{
			int returnVal = _fileChooser.showOpenDialog(this);

 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 	 				File file = _fileChooser.getSelectedFile();
 				try {
  		 				FileInputStream fis = new FileInputStream(file.getPath());
  		 				ObjectInputStream in = new ObjectInputStream(fis);
  		 				_game = (Game) in.readObject();
 						_boardSize = in.readInt();
 						_rowsNumber = in.readInt();
 						in.close();

 						clearCurrentMove();
 						_game.update("Restart");
 						discardMove();
 						_whitePlayer = _game.getWhite();
 						_blackPlayer = _game.getBlack();
 						_game.setUndoSize(undoSize());
 						update();
 					} catch (IOException ioe) {
 	 					System.out.println(ioe);
 	 					String cause =
							 "File Type Invalid,use only files saved by Dam Ka !\nGame not loaded! ";
							JOptionPane.showMessageDialog(this, cause , "Error loading file",
									JOptionPane.ERROR_MESSAGE);
 	 				} catch (ClassNotFoundException e) 	{
 	 					System.out.println(e);
 	 					JOptionPane.showMessageDialog(this, " Game not loaded! " , "Error loading file",
 							JOptionPane.ERROR_MESSAGE);
 	 				}
 			}
		}
		else if (command.equals("Save Game"))
		{
			int returnVal = (_needSave
						? _fileChooser.showSaveDialog(this)
						: JFileChooser.APPROVE_OPTION);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = _fileChooser.getSelectedFile();
				try {
					String name = file.getPath();
					if (!name.endsWith(SAVED_GAME_EXTENSION))
						name = name.concat(SAVED_GAME_EXTENSION);
					// Create the necessary output streams to save the scribble.
					FileOutputStream fos = new FileOutputStream(name);
					// Save to file
					ObjectOutputStream out = new ObjectOutputStream(fos);
					// Save objects
					out.writeObject(_game);
					out.writeInt(_boardSize);
					out.writeInt(_rowsNumber);

					out.flush();
					out.close();
					_needSave = false;
					_saveItem.setEnabled(false);
					_saveButton.setEnabled(false);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(this, "file not saved !", "Error saving file",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (command.equals("Undo"))
		{
			_game.update("Undo");
			discardMove();
		}
		else if (command.equals("Redo"))
		{
			_game.update("Redo");
			discardMove();
		}
		else if (command.equals("Statistics..."))
		{
			if (_statsFrame != null)
			{
				_statsFrame.setVisible(true);
				_statsFrame.requestFocus();
			}
			else
			{
				_statsFrame = new StatisticsFrame();
			}
		}

		else if (command.equals("Contents"))
		{
			try
			{
				new HelpFrame("help/help.html");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		else if (command.equals("About"))
			showAboutDialog();
		else if (command.equals("Sound Off"))
		{
			_useSound = false;
			_soundButton.setIcon(_soundOffIcon);
			_soundButton.setToolTipText("Sound On");
		}
		else if (command.equals("Sound On"))
		{
			_useSound = true;
			_soundButton.setIcon(_soundOnIcon);
			_soundButton.setToolTipText("Sound Off");
		}		
		else if (command.equals("Exit"))
			System.exit(0);
	}

	/**
	 * Shows the "About" dialog.
	 * Also used as the splash screen.
	 */
	public void showAboutDialog()
	{
		Object[] objs = new Object[3];
		objs[0] = new ImageIcon(UNIVERSITY_ICON);
		objs[1] = new String("Dam Ka!\nAn Intellignet Game of Checkers.\n\nYaniv Fais, Ron Cohen\nWorkshop in Reinforcement Learning,\nTel Aviv University\n2003-2004");
		JOptionPane.showMessageDialog(this, objs);
	}

	public void mouseReleased(MouseEvent evt)	{}
	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
	public void mousePressed(MouseEvent evt) {}

	/**
	 * Implements MouseListener.
	 * Detects right-click over a board square.
	 */
	public void mouseClicked(MouseEvent evt)
	{
		if ((evt.getSource() instanceof BoardSquare)
			&& (evt.getButton() == MouseEvent.BUTTON3))
			handleRightClick((BoardSquare) evt.getSource());
	}

	/**
	 * Handles a right click on a board square.
	 * Right-click is used to make multiple jumps move.
	 * @param b The clicked square.
	 */
	private void handleRightClick(BoardSquare b)
	{
		if (b.isWhite()) return;
		Peon peon = _game.getPeonAt(b.getPoint());
		if (_currentMove != null)
		{
			_currentMove.addPoint(b.getPoint());
			if (_game.isLegalPartialMove(_currentMove))
			{
				if (_currentMove.getPeon().getPlayer() == _blackPlayer)
					((ImageIcon)b.getIcon()).setImage(_smallBlackPeon.getImage());
				else
					((ImageIcon)b.getIcon()).setImage(_smallRedPeon.getImage());
//				b. updateUI();
				this.revalidate();

				for (Enumeration targets = _legalTargets.elements();targets.hasMoreElements();)
				{
					Point p = (Point) targets.nextElement();
					BoardSquare b1 = _squares[p.getRow()][p.getCol()];
					b1.setBorderPainted(false);
//					b1.updateUI();
					b1.revalidate();
				}
			}
			else
			{
				_currentMove.removeLastHop();
			}
		}
	}

	/**
	 * Handles a left-click on a board square.
	 * @param b The clicked square.
	 */
	private void handleBoardClick(BoardSquare b)
	{
		if (b.isWhite()) return;
		Peon peon = _game.getPeonAt(b.getPoint());
		if (_currentMove != null && peon == null)
		{
			clearCurrentMove();
			_currentMove.addPoint(b.getPoint());
			if (_game.isLegalMove(_currentMove))
			{
				((HumanPlayer)_currentMove.getPeon().getPlayer()).setNextMove(_currentMove);
			}
			_currentMove = null;
		}
		else
		{
			clearCurrentMove();
			_currentMove = null;
			if (peon == null) return;
			if (!(peon.getPlayer() instanceof HumanPlayer)) return;
			if (peon.getPlayer().getColor() != _game.getCurrentPlayerColor()) return;
			_legalTargets = _game.getLegalSimpleTargets(peon);
			for (Enumeration targets = _legalTargets.elements();targets.hasMoreElements();)
			{
				Point p = (Point) targets.nextElement();
				BoardSquare b1 = _squares[p.getRow()][p.getCol()];
				b1.setBorder(new LineBorder(Color.blue, 2));
				b1.setBorderPainted(true);
//				b1.updateUI();
				b1.revalidate();
			}

			_currentMove = new Move(peon);
			b.setBorder(new LineBorder(Color.black, 2));
			b.setBorderPainted(true);
//			b.updateUI();
			b.revalidate();
		}
	}

	/**
	 * Clears the current move.
	 */
	private void clearCurrentMove()
	{
		if (_currentMove == null) return;
		int row = _currentMove.getPeon().getRow();
		int col = _currentMove.getPeon().getCol();
		BoardSquare b = _squares[row][col];
		b.setBorderPainted(false);
//		b.updateUI();
		b.revalidate();
		for (Enumeration en=_currentMove.getHops(); en.hasMoreElements();)
		{
			Point point = (Point) en.nextElement();
			b = _squares[point.getRow()][point.getCol()];
			((ImageIcon)b.getIcon()).setImage(_blackSquare.getImage());
//			b.updateUI();
			b.revalidate();
		}
		for (Enumeration targets = _legalTargets.elements();targets.hasMoreElements();)
		{
			Point p = (Point) targets.nextElement();
			BoardSquare b1 = _squares[p.getRow()][p.getCol()];
			b1.setBorderPainted(false);
//			b1.updateUI();
			b1.revalidate();
		}
		_legalTargets = new Vector();
	}


	/**
	 * Cancels a Move.
	 */
	private void discardMove()
	{
		if (_whitePlayer instanceof HumanPlayer)
			((HumanPlayer)_whitePlayer).setNextMove(null);
		if (_blackPlayer instanceof HumanPlayer)
			((HumanPlayer)_blackPlayer).setNextMove(null);
	}

	/**
	 * Returns the size (in moves) of an undo action.
	 */
	private int undoSize()
	{
		return ((_whitePlayer instanceof HumanPlayer) ^
		       (_blackPlayer instanceof HumanPlayer))
		  		? 2 : 1;
	}
	
	/**
	 * Disables online learning.
	 * This method is used when weights are set manually.
	 */
	public static void disableOnlineLearning()
	{
		_instance._learningItem.setSelected(false);
		_instance._game.setOnlineLearning(false);
	}

	/**
	 * Updates the UI.
	 * @param undo determines if undo is permitted
	 * @param redo determines if redo is permitted 
	 */
	public void update(boolean undo,boolean redo)
	{
		_needSave = true;
		_saveItem.setEnabled(true);
		_saveButton.setEnabled(true);
		_undoItem.setEnabled(undo);
		_redoItem.setEnabled(redo);
		_undoButton.setEnabled(undo);
		_redoButton.setEnabled(redo);
		update();
	}

	/**
	 * Updates the UI.
	 */
	public void update()
	{
		for (int row=0; row<_boardSize; row++)
			for (int col=0; col<_boardSize; col++)
			{
				if (((row + col) % 2) != 0)
				{
					Peon p = _game.getPeonAt(row, col);
					_squares[row][col].setPeon(p);
				}
			}
		_whiteScore.setText(String.valueOf(_whitePlayer.getNumberOfPeons()));
		_blackScore.setText(String.valueOf(_blackPlayer.getNumberOfPeons()));
//		_whiteScore.updateUI();
//		_blackScore.updateUI();
		_whiteScore.revalidate();
		_blackScore.revalidate();
	}

	/**
	 * Returns the size (rows or cols) of the board to use 
	 * when starting a new game.
	 * By default, this value is 8.
	 * @return number of rows on next game's board.
	 */
	public int getNextBoardSize()
	{
		return _nextBoardSize;
	}

	/**
	 * Sets the size (rows or cols) of the board to use 
	 * when starting a new game.
	 * @param size number of rows on next game's board.
	 */
	public void setNextBoardSize(int size)
	{
		_nextBoardSize = size;
	}

	/**
	 * Returns the number of rows to be filled with pieces on each side,
	 * when starting a new game. By default, this value is 3.
	 * @return the number of starting peons' rows.
	 */
	public int getRowsNumber()
	{
		return _rowsNumber;
	}

	/**
	 * Sets the number of rows to be filled with pieces on each side,
	 * when starting a new game. 
	 * @param rows the number of starting peons' rows.
	 */
	public void setRowsNumber(int rows)
	{
		_rowsNumber = rows;
	}


	/**
	 * Displays a message on the status bar.
	 * @param message The message to display.
	 */
	public void showMessage(String message)
	{
		_messageLabel.setText(message);
//		_messageLabel.updateUI();
		_messageLabel.revalidate();
	}


	/**
	 * Shows a Move on the screen. 
	 * This method is used to "animate" the move, and produce the appropriate sound.
	 * @param move The Move to show
	 */
	public void showMove(Move move)
	{
		Peon peon = move.getPeon();
		Image small, normal;
		Player player = peon.getPlayer();
		if (player.getColor() == Player.PLAYER_WHITE)
		{
			small = _smallRedPeon.getImage();
			if (peon instanceof King)
				normal = _blackSquareWhiteKing.getImage();
			else
				normal = _blackSquareWhitePeon.getImage();
		}
		else
		{
			small = _smallBlackPeon.getImage();
			if (peon instanceof King)
				normal = _blackSquareBlackKing.getImage();
			else
				normal = _blackSquareBlackPeon.getImage();
		}

		Point p = peon.getPoint();
		BoardSquare b = _squares[p.getRow()][p.getCol()];
		for (Enumeration hops = move.getHops(); hops.hasMoreElements();)
		{
			if (_useSound) 
			{
				try
				{
					_pieceMoveAudio.stop();
					_pieceMoveAudio.play();
				}
				catch (Exception ex)
				{
				}
			}
			((ImageIcon)b.getIcon()).setImage(small);
			b.revalidate();
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException ie)
			{
			}
			((ImageIcon)b.getIcon()).setImage(_blackSquare.getImage());
			b.revalidate();
			Point p1 = (Point) hops.nextElement();
			if (Math.abs(p1.getRow() - p.getRow()) == 2)
			{
				b = _squares[(p.getRow()+p1.getRow())/2][(p.getCol()+p1.getCol())/2];
				((ImageIcon)b.getIcon()).setImage(small);
				b.revalidate();
				if (_useSound) 
				{
					try
					{
						_pieceHitAudio.stop();
						_pieceHitAudio.play();
						Thread.sleep(200);
					}
					catch (Exception e)
					{
					}
				}
				((ImageIcon)b.getIcon()).setImage(_blackSquare.getImage());
				b.revalidate();
			}
			p = p1;
			b = _squares[p.getRow()][p.getCol()];
			if (!(peon instanceof King) &&
				((p1.getRow() == 0 && player.getColor() == Player.PLAYER_BLACK) ||
				 (p1.getRow() == _boardSize-1 && player.getColor() == Player.PLAYER_WHITE)))
				 {
					if (_useSound) 
					{
					 	try
					 	{
					 		_pieceCrowningAudio.stop();
					 		_pieceCrowningAudio.play();
					 	}
					 	catch (Exception ex)
					 	{
					 	}
					}

				 }
		}
		((ImageIcon)b.getIcon()).setImage(normal);
		b.revalidate();
		if (_statsFrame != null)
			_statsFrame.refresh();
	}

	/**
	 * The BoardSquare class.
	 * Represents a board square in the GUI.
	 * Each square is actually a JButton.
	 */
	class BoardSquare extends JButton
	{
		/**
		 * Square image.
		 */
		private ImageIcon _icon = new ImageIcon();

		/**
		 * The position of the square on the board matrix.
		 */
		private Point _point;
		
		/**
		 * The Peon that is located on the square, if such exists.
		 */
		private Peon _peon;
		
		/**
		 * True iff the square is a white square.
		 */
		private boolean _white = false;


		/**
		 * Constructor.
		 * @param row Row position of the square
		 * @param col Column position of the square
		 */
		public BoardSquare(int row, int col)
		{
			_point = new Point(row, col);
			if (((row + col) % 2) != 0)
				_icon.setImage(_blackSquare.getImage());
			else
			{
				_icon.setImage(_whiteSquare.getImage());
				_white = true;
			}
			super.setIcon(_icon);
			super.revalidate();
		    setDefaultCapable(false);
    		setPreferredSize(new Dimension(_icon.getIconWidth(), _icon.getIconHeight()));
    		setBorderPainted(false);
    		setContentAreaFilled(false);
    		setFocusPainted(false);
		}

		/**
		 * Returns the Peon that "stands" on the square.
		 * @return The square's Peon
		 */
		public Peon getPeon()
		{
			return _peon;
		}

		/**
		 * Sets the square's Peon.
		 * @param p Peon to be placed on the square.
		 */
		public void setPeon(Peon p)
		{
			if (p == _peon)
			{
//				updateUI();
				this.revalidate();
				  return;
			}
			_peon = p;
			if (p != null)
			{
				byte color = p.getPlayer().getColor();
				if (color == Player.PLAYER_BLACK)
				{
					if (p instanceof King)
						_icon.setImage(_blackSquareBlackKing.getImage());
					else
						_icon.setImage(_blackSquareBlackPeon.getImage());
				}
				else
				{
					if (p instanceof King)
						_icon.setImage(_blackSquareWhiteKing.getImage());
					else
						_icon.setImage(_blackSquareWhitePeon.getImage());
				}
			}
			else
				_icon.setImage(_blackSquare.getImage());
//			updateUI();
			this.revalidate();
		}

		/**
		 * Returns the position of the square
		 * @return The Point where the square is positioned.
		 */
		public Point getPoint()
		{
			return _point;
		}
		
		/**
		 * Returns true iff the square is white colored.
		 * @return true iff the square is white.
		 */
		public boolean isWhite()
		{
			return _white;
		}
	}

	/**
	 * Implementing Checkers Game (.ckg) File Filter
	 */
	public class CKGFileChooser extends swingwtx.swing.filechooser.FileFilter 
	{
		public boolean accept(File file) 
		{
			if (file.isDirectory())
				return true;

			return (file.getName().endsWith(SAVED_GAME_EXTENSION));
		}

		public String getDescription() 
		{
			return "Checkers Game File";
		}
	}
	
	/**
	 * Main method. Starts the checkers game application.
	 * @param args not used.
	 */
	public static void main(String[] args)
	{
		_instance = new GameUI();
		_instance.showAboutDialog();
	}

}
