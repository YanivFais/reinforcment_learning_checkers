/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.guiSWT;

import swingwt.awt.*;
import swingwt.awt.event.*;
import swingwtx.swing.*;
import swingwtx.swing.border.*;

/**
 * Game rules selection dialog
 * This dialog allows the user to select the board size, and the 
 * number of rows with peons to begin with.
 */
public class GameRulesDialog extends JDialog implements ActionListener
{
	/**
	 * Size of board
	 */
	private int _boardSize;
	
	/**
	 * Number of starting rows with peons
	 */
	private int _rows;
	
	/**
	 * Available sizes of board
	 */
	private static final int[] BOARD_SIZES = {6, 8, 10};
	
	/**
	 * Available options of starting rows
	 */
	private static final int[] ROWS_NUMBER = {1, 2, 3, 4};
	
	/**
	 * GUI members
	 */
	private JRadioButton[] _sizeRadioButtons;
	private JRadioButton[] _rowsNumberButtons;
	private JCheckBox _compulsaryHitButton;
	private NewGameDialog _owner;
	
	
	/**
	 * Constructor for GameRulesDialog.
	 * @param owner owner Frame
	 */
	public GameRulesDialog(NewGameDialog owner) 
	{
		super(owner);
		setTitle("Checkers Settings");
		setModal(true);
		_owner = owner;
		_boardSize = owner.getNextBoardSize();
		_rows = owner.getRowsNumber();
		getContentPane().setLayout(new BorderLayout());				
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3,1));
		
		JPanel boardSizePanel = new JPanel();
		ButtonGroup sizeGroup = new ButtonGroup();
		_sizeRadioButtons = new JRadioButton[BOARD_SIZES.length];
		for (int i=0; i<BOARD_SIZES.length; i++)
		{
			int boardSize = BOARD_SIZES[i];			
			_sizeRadioButtons[i] = new JRadioButton(new String(boardSize+"x"+boardSize));			
			if (boardSize == _boardSize) _sizeRadioButtons[i].setSelected(true);
			//if (boardSize > _boardSize) _sizeRadioButtons[i].setEnabled(false);
			sizeGroup.add(_sizeRadioButtons[i]);
			boardSizePanel.add(_sizeRadioButtons[i]);
		}
		boardSizePanel.setBorder(new TitledBorder("Board Size"));
		mainPanel.add(boardSizePanel);
		
		JPanel rowsNumberPanel = new JPanel();
		ButtonGroup rowsGroup = new ButtonGroup();
		_rowsNumberButtons = new JRadioButton[ROWS_NUMBER.length];
		for (int i=0; i<ROWS_NUMBER.length; i++)
		{
			int rows = ROWS_NUMBER[i];
			_rowsNumberButtons[i] = new JRadioButton(new String(rows+" Rows"));			
			if (rows == _rows) _rowsNumberButtons[i].setSelected(true);
			rowsGroup.add(_rowsNumberButtons[i]);
			rowsNumberPanel.add(_rowsNumberButtons[i]);
		}
		rowsNumberPanel.setBorder(new TitledBorder("Starting rows"));
		mainPanel.add(rowsNumberPanel);
		
	
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		JButton okButton = new JButton("OK");		
		JButton cancelButton = new JButton("Cancel");				
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		bottomPanel.add(okButton);
		bottomPanel.add(cancelButton);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
		setResizable(false);
	}
	
	/**
	 * Event handler
	 * @param evt event
	 */
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getActionCommand().equals("OK"))
		{
			if (applyChanges())
				dispose();
		}
		else if (evt.getActionCommand().equals("Cancel"))
		{
			dispose();
		}
	}
	
	/**
	 * Applies changes to GUI
	 * @return true for success
	 */
	private boolean applyChanges()
	{
		int size=0;
		int rows=0;				
		for (int i=0; i<BOARD_SIZES.length; i++)
		{
			if (_sizeRadioButtons[i].isSelected())
			{
				size = BOARD_SIZES[i];
				break;
			}
		}
		for (int i=0; i<ROWS_NUMBER.length; i++)
		{
			if (_rowsNumberButtons[i].isSelected())
			{
				rows = ROWS_NUMBER[i];
				break;
			}
		}
		if (rows*2 + 2 <= size)
		{
			_rows = rows;		
			_boardSize = size;
			_owner.setNextBoardSize(_boardSize);
			_owner.setRowsNumber(_rows);
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Number of rows is too large for the selected board size!", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

}
