/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.gui;

import javax.swing.*;
import javax.swing.border.*;

import checkers.game.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;


/**
 * GUI dialog for starting a new game 
 */
public class NewGameDialog extends JDialog implements ActionListener 
{
	/**
	 * White player
	 */
	private Player _whitePlayer = null;

	/**
	 * Black player
	 */
	private Player _blackPlayer = null;
	
	/**
	 * White level
	 */
	private static int _whiteLevel = 1;
	
	/**
	 * Black level
	 */
	private static int _blackLevel = 1;	
	
	
	/**
	 * Maximal level for a machine player
	 * Default is 10, if no other limit is given in 'checkers.properties'
	 */
	private static int _maxLevel = 10;	
	
	/**
	 * GUI widgets
	 */
	private JRadioButton _whiteHuman;
	private JRadioButton _blackHuman;
	private JRadioButton _whiteCPU;
	private JSlider _whiteSlider;
	private JRadioButton _blackCPU;
	private JSlider _blackSlider;
	
	/**
	 * level names
	 */
	private static Hashtable _levelLabels;
	
	/**
	 * board size
	 */
	private int _nextBoardSize;
	
	/**
	 * number of starting rows
	 */
	private int _rowsNumber;
	
	/**
	 * owner frame
	 */
	private GameUI _owner;
	
	
	static
	{
		try
		{
			_maxLevel = Integer.parseInt(Game.getInstance().getGameProperty(Game.MAX_LEVEL_PROPERTY));	
		}
		catch (NumberFormatException nfe)
		{
		}
		_levelLabels = new Hashtable();
		_levelLabels.put(new Integer(1), new JLabel("Beginner"));
		_levelLabels.put(new Integer(_maxLevel), new JLabel("Advanced"));
	}
	
	/**
	 * Constructor
	 * @param owner The GameUI which opened this frame.
	 */
	public NewGameDialog(GameUI owner)
	{
		super(owner);
		_owner = owner;
		_nextBoardSize = owner.getNextBoardSize();
		_rowsNumber = owner.getRowsNumber();
		setModal(true);
		setTitle("New Game");
		getContentPane().setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER,20,20));
		
		JPanel whitePanel = new JPanel();		
		whitePanel.setLayout(new BorderLayout());
		JPanel whiteRadioPanel = new JPanel();
		whiteRadioPanel.setLayout(new GridLayout(5,1));		
		ButtonGroup whiteGroup = new ButtonGroup();
		_whiteSlider = new JSlider(JSlider.HORIZONTAL ,0,_maxLevel, _whiteLevel);
		_whiteSlider.setMajorTickSpacing(5);
		_whiteSlider.setMinorTickSpacing(3);
		_whiteSlider.setLabelTable(_levelLabels);
		_whiteSlider.setPaintLabels(true);
		if (Game.getInstance().getWhite() instanceof CPUPlayer)
		{
			_whiteHuman = new JRadioButton("Human Player", false);
			_whiteCPU = new JRadioButton("CPU Player", true);
			_whiteSlider.setEnabled(true);	
		}
		else
		{
			_whiteHuman = new JRadioButton("Human Player", true);		
			_whiteCPU = new JRadioButton("CPU Player", false);	
			_whiteSlider.setEnabled(false);	
		}
		
		_whiteHuman.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (_whiteHuman.isSelected())
					_whiteSlider.setEnabled(false);
			}
		});
		
		_whiteCPU.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (_whiteCPU.isSelected())
					_whiteSlider.setEnabled(true);
			}
		});
		
				
		
		whiteGroup.add(_whiteHuman);
		whiteGroup.add(_whiteCPU);
		whiteRadioPanel.add(_whiteHuman);
		whiteRadioPanel.add(_whiteCPU);
		whiteRadioPanel.add(_whiteSlider);
		
		whiteRadioPanel.setBorder(new TitledBorder("White Player"));		
		whitePanel.add(whiteRadioPanel, BorderLayout.CENTER);
		mainPanel.add(whitePanel);
		
		JPanel blackPanel = new JPanel();		
		blackPanel.setLayout(new BorderLayout());
		JPanel blackRadioPanel = new JPanel();
		blackRadioPanel.setLayout(new GridLayout(5,1));
		ButtonGroup blackGroup = new ButtonGroup();
		_blackSlider = new JSlider(JSlider.HORIZONTAL ,0,_maxLevel, _blackLevel);
		_blackSlider.setMajorTickSpacing(5);
		_blackSlider.setMinorTickSpacing(3);
		_blackSlider.setLabelTable(_levelLabels);
		_blackSlider.setPaintLabels(true);	
		if (Game.getInstance().getBlack() instanceof CPUPlayer)
		{
			_blackHuman = new JRadioButton("Human Player", false);
			_blackCPU = new JRadioButton("CPU Player", true);
			_blackSlider.setEnabled(true);
		}
		else
		{
			_blackHuman = new JRadioButton("Human Player", true);
			_blackCPU = new JRadioButton("CPU Player", false);
			_blackSlider.setEnabled(false);
		}		
		

		
		_blackHuman.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (_blackHuman.isSelected())
					_blackSlider.setEnabled(false);
			}
		});
		
		_blackCPU.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				if (_blackCPU.isSelected())
					_blackSlider.setEnabled(true);
			}
		});
		
	
		
		blackGroup.add(_blackHuman);
		blackGroup.add(_blackCPU);
		blackRadioPanel.add(_blackHuman);
		blackRadioPanel.add(_blackCPU);
	
		blackRadioPanel.add(_blackSlider);
		blackRadioPanel.setBorder(new TitledBorder("Black Player"));
		blackPanel.add(blackRadioPanel, BorderLayout.CENTER);
		mainPanel.add(blackPanel);
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JButton advancedButton = new JButton("Advanced...");
		JButton okButton = new JButton("Start!");
		JButton cancelButton = new JButton("Cancel");
		advancedButton.addActionListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);		
		bottomPanel.add(advancedButton);
		bottomPanel.add(okButton);
		bottomPanel.add(cancelButton);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Event handler
	 * @param evt the event to handle.
	 */
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getActionCommand().equals("Advanced..."))
		{
			new GameRulesDialog(this);
			return;
		}
		if (evt.getActionCommand().equals("Start!"))
		{
			_whiteLevel = _whiteSlider.getValue();
			_blackLevel = _blackSlider.getValue();
			if (_whiteHuman.isSelected())
				_whitePlayer = new HumanPlayer(Player.PLAYER_WHITE);
			else if (_whiteCPU.isSelected())
				_whitePlayer = new CPUPlayer(Player.PLAYER_WHITE, _whiteLevel);
			if (_blackHuman.isSelected())
				_blackPlayer = new HumanPlayer(Player.PLAYER_BLACK);
			else if (_blackCPU.isSelected())
				_blackPlayer = new CPUPlayer(Player.PLAYER_BLACK, _blackLevel);
			_owner.setNextBoardSize(_nextBoardSize);
			_owner.setRowsNumber(_rowsNumber);
				
		}
		else if (evt.getActionCommand().equals("Cancel")) 
		{
		}
		setModal(false);
		dispose();
	}
	
	/**
	 * Returns the new white Player.
	 * @return White player of the new game
	 */
	public Player getWhitePlayer()
	{
		return _whitePlayer;
	}
	
	/**
	 * Returns the new black Player
	 * @return Black player of the new game
	 */
	public Player getBlackPlayer()
	{
		return _blackPlayer;
	}

	/**
	 * Returns the board size for the new game
	 * @return board size of new game
	 */
	public int getNextBoardSize()
	{
		return _nextBoardSize;
	}

	/**
	 * Sets the board size of new game
	 * @param size new size
	 */
	public void setNextBoardSize(int size)
	{
		_nextBoardSize = size;
	}

	/**
	 * Returns the number of starting rows for the new game.
	 * @return number of starting rows for the new game.
	 */
	public int getRowsNumber()
	{
		return _rowsNumber;
	}

	/**
	 * Sets the number of starting rows for new game
	 * @param rows number of starting rows
	 */
	public void setRowsNumber(int rows)
	{
		_rowsNumber = rows;
	}

}

