/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen        Yaniv Fais                          *
 *****************************************************************************/
package checkers.guiSWT;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

import checkers.game.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.*;


/**
 * This class is used to display/edit the learned weights, and games results. 
 */
public class StatisticsFrame extends JFrame 
{
	private static final String WEIGHTS_8X8_FILE = "data/weights32.dat";
	private static final String WEIGHTS_10X10_FILE = "data/weights50.dat";
	private static final String WEIGHTS_6X6_FILE = "data/weights18.dat";
	private static final String RESULTS_FILE = "data/results.dat";

	/**
	 * Weight Panel.
	 */
	private WeightsPanel _weightsPanel;
	
	/**
	 * Games Results Panel.
	 */
	private ResultsPanel _resultsPanel;

	/**
	 * Constructor. Creates the Statistics Frame.
	 */
	public StatisticsFrame()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		_weightsPanel = new WeightsPanel();
		_resultsPanel = new ResultsPanel();
		tabbedPane.addTab("Weights", _weightsPanel);
		tabbedPane.addTab("Games Results", _resultsPanel);
		setTitle("Dam Ka! Statistics");
		getContentPane().add(tabbedPane);
		pack();
		setVisible(true);
	}
	
	
	/**
	 * Sets visibility of the frame.
	 * @param b determines if the frame is visible
	 */
	public void setVisible(boolean b)
	{
		if (b) refresh();
		super.setVisible(b);
	}
	
	/**
	 * The ResultsPanel class.
	 * Used to display past games results summary.
	 */
	class ResultsPanel extends JPanel implements ActionListener
	{
		private JLabel _whiteVictories;
		private JLabel _blackVictories;
		private JLabel _draws;
		private JButton _resetButton;
		private JButton _updateButton;
		private DefaultPieDataset _data;
		private ChartPanel _chartPanel;
		
		public ResultsPanel()
		{
			setLayout(new BorderLayout());

			JPanel infoPanel = new JPanel(new GridLayout(3,2, 0, 0));
			_whiteVictories	= new JLabel("0");
			_blackVictories	= new JLabel("0");
			_draws	= new JLabel("0");
			infoPanel.add(new JLabel("White Player Victories: "));			
			infoPanel.add(_whiteVictories);
			infoPanel.add(new JLabel("Black Player Victories: "));
			infoPanel.add(_blackVictories);
			infoPanel.add(new JLabel("Draws: "));
			infoPanel.add(_draws);			
			
			_data = new DefaultPieDataset();
			
			// create a chart...
			JFreeChart chart = ChartFactory.createPieChart3D("Results", _data, true,true,true);
			
			_chartPanel = new ChartPanel(chart);
			add(_chartPanel,BorderLayout.NORTH);
			
			add(new JPanel(), BorderLayout.WEST);
			add(new JPanel(), BorderLayout.EAST);
			
			JPanel centerPanel = new JPanel(new GridLayout(3,1));
			centerPanel.add(new JPanel());
			centerPanel.add(infoPanel);
			centerPanel.add(new JPanel());
			add(centerPanel, BorderLayout.CENTER);
			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			_resetButton = new JButton("Reset");
			_updateButton = new JButton("Refresh");
			_resetButton.addActionListener(this);
			_updateButton.addActionListener(this);
			bottomPanel.add(_updateButton);
			bottomPanel.add(_resetButton);			
			add(bottomPanel, BorderLayout.SOUTH);
			updateResults();			
		}
		
		/**
		 * Refreshes the display of the panel.
		 */
		public void updateResults()
		{
			try
			{
				add(_chartPanel,BorderLayout.NORTH);
				pack();
				FileInputStream fis = new FileInputStream(RESULTS_FILE);
				int white = fis.read();
				_whiteVictories.setText(String.valueOf(white));	
				int black = fis.read();
				_blackVictories.setText(String.valueOf(black));	
				int draw = fis.read();
				_draws.setText(String.valueOf(draw));					
				fis.close();
				_data.setValue("White Player Victories", new Double(white));
				_data.setValue("Black Player Victories", new Double(black));
				_data.setValue("Draws", new Double(draw));
			}
			catch (Exception ex)
			{
				
				_data.setValue("White Player Victories", new Double(0));
				_data.setValue("Black Player Victories", new Double(0));
				_data.setValue("Draws", new Double(0));
				this.remove(_chartPanel);
				_whiteVictories.setText("0");
				_blackVictories.setText("0");
				_draws.setText("0");
				updateUI();
			}
		}
		
		
		/**
		 * Event handler. implements ActionListener
		 * @param evt The event to handle.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			if (evt.getSource() == _resetButton)
			{
				File f = new File("results");
				f.delete();
			}
			updateResults();
		}

	}
	
	
	/**
	 * The WeightsPanel class.
	 * Displays the board parameters' weights.
	 */	
	class WeightsPanel extends JPanel implements ActionListener
	{
		/**
		 * JTable used to display weights while in edit mode
		 */
		private JTable _weightsTable;
		
		/**
		 * Parameter names and weights values.
		 */
		private Vector _weights, _attributeNames;
		
		/**
		 * Selects board size.
		 */
		private JComboBox _boardSizeSelector;
		
		/**
		 * Selects game stage.
		 */
		private JComboBox _stageSelector;
		
		/**
		 * Chart Data
		 */
		private DefaultKeyedValues2DDataset _data;
		
		/**
		 * Edit Button
		 */
		private JButton _editUpdateButton;
		
		/**
		 * Table scroll pane
		 */	
		private JScrollPane _scPanel; 
	
		/**
		 * Chart Panel
		 */
		private ChartPanel _chartPanel; 
		
		/**
		 * Constructor. Builds Weights Panel.
		 */
		public WeightsPanel()
		{
			_weights = new Vector();
			_attributeNames = new Vector();
			_stageSelector = new JComboBox();
			_stageSelector.setEditable(false);
			_stageSelector.addItem("End Game");
			_stageSelector.addItem("Mid Game (4)");
			_stageSelector.addItem("Mid Game (3)");
			_stageSelector.addItem("Mid Game (2)");
			_stageSelector.addItem("Mid Game (1)");
			_stageSelector.addItem("Game Beginning");
			_stageSelector.setSelectedIndex(5);
			_stageSelector.addActionListener(this);
			_boardSizeSelector = new JComboBox();
			_boardSizeSelector.setEditable(false);
			_boardSizeSelector.addItem("8x8 Board");
			_boardSizeSelector.addItem("6x6 Board");
			_boardSizeSelector.addItem("10x10 Board");
			_boardSizeSelector.addActionListener(this);
			
			setLayout(new BorderLayout());
			
			_editUpdateButton = new JButton("Edit");
			_editUpdateButton.addActionListener(this);
			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			topPanel.add(_boardSizeSelector);
			topPanel.add(_stageSelector);
			topPanel.add(_editUpdateButton);
			add(topPanel, BorderLayout.NORTH);
			
			
			_data = new DefaultKeyedValues2DDataset();
			
			JFreeChart chart =	ChartFactory.createBarChart3D(null,"Parameters","weight",_data,
						PlotOrientation.VERTICAL,true,true,false);
	
			_chartPanel = new ChartPanel(chart);
			add(_chartPanel,BorderLayout.CENTER);
			
	
	
			_weightsTable = new JTable(new WeightsTableModel());
			_weightsTable.setRowSelectionAllowed(true);
			_weightsTable.setColumnSelectionAllowed(true);
			_weightsTable.setDefaultEditor(String.class,
					new WeightEditor());
			
			
			_scPanel = new JScrollPane(_weightsTable);
			updateWeightsTable();									
		
			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
			add(bottomPanel, BorderLayout.SOUTH);
		}
	
		/**
		 * Event handler.
		 * Implements ActionListener.
		 * @param evt The event to handle.
		 */	
		public void actionPerformed(ActionEvent evt)
		{
			if (evt.getSource() == _editUpdateButton)
			{
				if (_editUpdateButton.getText()=="Edit")
				{
					remove(_chartPanel);
					this.repaint();
					add(_scPanel, BorderLayout.CENTER);
					_editUpdateButton.setText("Update");
				}
				else 
				{	
					remove(_scPanel);
					this.repaint();
					writeWeights();
					add(_chartPanel, BorderLayout.CENTER);
					_editUpdateButton.setText("Edit");
				}
			}
			updateWeightsTable();
				
		}
		
		/**
		 * Refreshes the contents of the Weights Panel.
		 */
		public void refresh()
		{
			Game game = Game.getInstance();
			int size = game.getBoardSize();
			int count = game.getBlack().getNumberOfPeons() + game.getWhite().getNumberOfPeons();
			switch (size)
			{
				case 8:
					_boardSizeSelector.setSelectedIndex(0);
					_stageSelector.setSelectedIndex((int)Math.ceil(count/4.0) - 1);
					break;
				case 6:
					_boardSizeSelector.setSelectedIndex(1);
					_stageSelector.setSelectedIndex((int)Math.ceil(count/2.0) - 1);
					break;
				case 10:
					_boardSizeSelector.setSelectedIndex(2);
					_stageSelector.setSelectedIndex((int)Math.ceil(count/7.0) - 1);
					break;					
			}
			updateWeightsTable();		
		}
		
		/**
		 * Refreshes the contents of the weights table.
		 */
		private void updateWeightsTable()
		{			
			_attributeNames = new Vector();
			_attributeNames.addElement("Pieces Advantage");
			_attributeNames.addElement("Opponent Liberty");
			_attributeNames.addElement("Kings");
			_attributeNames.addElement("Center Control");
			_attributeNames.addElement("Kings Center Control");
			_attributeNames.addElement("Opponent Center Control");
			_attributeNames.addElement("Advance");
			_attributeNames.addElement("Opponent Kings");
			_attributeNames.addElement("Opponent Guard");
			_attributeNames.addElement("Cramp");
			_attributeNames.addElement("Double Diagonal File");
			_attributeNames.addElement("Diagonal Moment Value");
			_attributeNames.addElement("Dyke");
			_attributeNames.addElement("Exposure");
			_attributeNames.addElement("Gap");
			_attributeNames.addElement("Hole");
			_attributeNames.addElement("Node");
			_attributeNames.addElement("Pole");
			_attributeNames.addElement("Back Row Control");
			_attributeNames.addElement("Opponent Hitting");

			
			_weights = new Vector();
			FileInputStream fis = null;
			try
			{
				switch (_boardSizeSelector.getSelectedIndex())
				{
					case 1:
						fis = new FileInputStream(WEIGHTS_6X6_FILE);
						break;
					case 2:
						fis = new FileInputStream(WEIGHTS_10X10_FILE);
						break;
					default:
						fis = new FileInputStream(WEIGHTS_8X8_FILE);
						break;						
				}				
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				int stage = _stageSelector.getSelectedIndex();
				int num = _attributeNames.size();
							
				for (int i=0; i < stage; i++)
					for (int j=0; j<num; j++)			
						br.readLine();
				for (int j=0; j<num; j++)
				{
					String line = br.readLine();
					//Float f = Float.valueOf(line);
					_weights.addElement(line);
				}			
			}
			catch (IOException ioe)
			{
				System.out.println(ioe);
			}
			finally 
			{
				try
				{
					fis.close();							
				}
				catch (Exception e)
				{
				}
			}
			
			Enumeration aEnum = _attributeNames.elements();
			for (Enumeration wEnum = _weights.elements();
				 wEnum.hasMoreElements() && aEnum.hasMoreElements();)
			{
				double d = (double)Double.valueOf((String)wEnum.nextElement()).doubleValue();
				String s = (String)aEnum.nextElement();
				_data.addValue(d,s,"");
			}
			_weightsTable.repaint();
		}
		
		
		/**
		 * Saves updated weights to weights files.
		 */
		private void writeWeights()
		{
			FileInputStream fis = null;
			FileOutputStream fos = null;
			
			try
			{
				switch (_boardSizeSelector.getSelectedIndex())
				{
				case 1:
					fis = new FileInputStream(WEIGHTS_6X6_FILE);
					break;
				case 2:
					fis = new FileInputStream(WEIGHTS_10X10_FILE);
					break;
				default:
					fis = new FileInputStream(WEIGHTS_8X8_FILE);
					break;						
				}				
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				
				Vector oldWeights = new Vector();
				int stage = _stageSelector.getSelectedIndex();
				int num = _attributeNames.size();
				
				for (int i=0; i < _stageSelector.getItemCount(); i++)
					for (int j=0; j<num; j++)			
						oldWeights.addElement(br.readLine());	
				fis.close();
					
				switch (_boardSizeSelector.getSelectedIndex())
				{
				case 1:
					fos = new FileOutputStream(WEIGHTS_6X6_FILE);
					break;
				case 2:
					fos = new FileOutputStream(WEIGHTS_10X10_FILE);
					break;
				default:
					fos = new FileOutputStream(WEIGHTS_8X8_FILE);
					break;						
				}				
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				
				Enumeration oldWeightsEnum = oldWeights.elements();
				int i;
				for (i=0; i < stage; i++)
					for (int j=0; j<num; j++)			
						bw.write((String)oldWeightsEnum.nextElement()+"\r\n");
				
				Enumeration weightsEnum = _weights.elements();
				for (int j=0; j<num; j++)
				{
					bw.write((String)weightsEnum.nextElement()+"\r\n");
					oldWeightsEnum.nextElement();
				}	
				i++;
				for (; i < _stageSelector.getItemCount(); i++)
					for (int j=0; j<num; j++)			
						bw.write((String)oldWeightsEnum.nextElement()+"\r\n");
				bw.flush();
				GameUI.disableOnlineLearning();
				JOptionPane.showMessageDialog(this, "Changes will take effect once a new game is started.\n\nOnline learning is now disabled.\n");
				
			}
			catch (IOException ioe)
			{
				System.out.println(ioe);
			}
			finally 
			{
				try
				{
					fos.close();		
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
			}
		}
	
		/**
		 * Table Model for Weights Table.
		 */
		class WeightsTableModel extends AbstractTableModel
		{
			/**
			 * Column headers.
			 */
			final String[] columnNames =
				{"Attribute Name", "Weight"};
	
			/**
			 * Returns the column name
			 * @param col column index.
			 * @return column header
			 */
			public String getColumnName(int col)
			{
				return columnNames[col];
			}
	
			/**
			 * Returns the column class.
			 * @param c column number
			 * @return Class of column objects.
			 */
			public Class getColumnClass(int c)
			{
				return getValueAt(0, c).getClass();
			}
	
			/**
			 * Returns the number of rows in table model.
			 * @return number of rows.
			 */
			public int getRowCount()
			{
				return _weights.size();
			}
			
			/**
			 * Returns the number of columns in table model.
			 * @return number of columns.
			 */
			public int getColumnCount()
			{
				return columnNames.length;
			}
			
			/**
			 * Checks if a cell is editable.
			 * Only weight values are editable in this table.
			 * @param row cell's row
			 * @param col cell's column
			 * @return true iff a cell is editable.
			 */
			public boolean isCellEditable(int row, int col)
			{ 
				return (col==1); 
			}
			
			/**
			 * Sets a cell's value.
			 * @param value new value
			 * @param row row number
			 * @param col column number
			 */
			public void setValueAt(Object value, int row, int col) 
			{
				if (col==0)
					return;
				_weights.setElementAt((String)value,row);
				fireTableCellUpdated(row, col);
			}
			
			/**
			 * Returns the contents of a cell.
			 * @param row row number
			 * @param col column number
			 */
			public Object getValueAt(int row, int column)
			{
				switch (column)
				{
				case 0 :
					return _attributeNames.elementAt(row);
				case 1 :
					return _weights.elementAt(row);
				}
				return null;
			}
		}	
	}
	
	/**
	 * Weights Editor class. 
	 * Cell Editor for weights.
	 */
	public class WeightEditor extends DefaultCellEditor 
	{
		/**
		 * Text Format
		 */
		JFormattedTextField ftf;
		
		/**
		 * Previous cell value
		 */
		Object oldValue;
		
		/**
		 * Constructor of Weight Editor.
		 */
		public WeightEditor() 
		{
			super(new JFormattedTextField());
			ftf = (JFormattedTextField)getComponent();
			ftf.setHorizontalAlignment(JTextField.LEADING);
			ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
			//React when the user presses Enter while the editor is
			//active.  (Tab is handled as specified by
			//JFormattedTextField's focusLostBehavior property.)
			ftf.getInputMap().put(KeyStroke.getKeyStroke(
					KeyEvent.VK_ENTER, 0), "check");
			ftf.getActionMap().put("check", new AbstractAction() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					if (!ftf.isEditValid()) 
					{ //The text is invalid.
						if (userSaysRevert()) 
						{ //reverted
							ftf.postActionEvent(); //inform the editor
						}
					} 
					else try 
					{              //The text is valid,
						ftf.commitEdit();     //so use it.
						ftf.postActionEvent(); //stop editing
					} 
					catch (java.text.ParseException exc) 
					{
					}
				}
			});
		}


		/**
		 * Returns the table cell editor component.
		 * @return table cell editor component.
		 */		
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected,
				int row, int column) 
		{
			JFormattedTextField ftf =
				(JFormattedTextField)super.getTableCellEditorComponent(
						table, value, isSelected, row, column);
			oldValue = value;
			ftf.setValue(value);
			return ftf;
		}

		/**
		 * Override to check whether the edit is valid,
		 * setting the value if it is and complaining if
		 * it isn't.  If it's OK for the editor to go
		 * away, we need to invoke the superclass's version 
		 * of this method so that everything gets cleaned up.
		 *  @return true if cell editing should stop.
		 **/
		public boolean stopCellEditing() 
		{
			JFormattedTextField ftf = (JFormattedTextField)getComponent();
			String text = ftf.getText();
			try 
			{
				double d = Double.parseDouble(text);
				if (d<0 | d>1)
					throw (new java.text.ParseException("Not in (0,1)",1));
				ftf.commitEdit();
			} 
			catch (Exception exc) 
			{ 
				//text is invalid
				if (!userSaysRevert()) 
				{ //user wants to edit
					return false; //don't let the editor go away
				} 
			}
			return super.stopCellEditing();
		}

		/** 
		 * Lets the user know that the text they entered is 
		 * bad. Returns true if the user elects to revert to
		 * the last good value.  Otherwise, returns false, 
		 * indicating that the user wants to continue editing.
		 * @return true if the user wants to revert to the last good value.
		 */
		protected boolean userSaysRevert() 
		{
			Toolkit.getDefaultToolkit().beep();
			ftf.selectAll();
			Object[] options = {"Edit",
			"Revert"};
			int answer = JOptionPane.showOptionDialog(
					SwingUtilities.getWindowAncestor(ftf),
					"The value must be a floating point number between "
					+ "0 and 1. \n"
					+ "You can either continue editing "
					+ "or revert to the last valid value.",
					"Invalid Text Entered",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					options,
					options[1]);
			
			if (answer == 1) { //Revert!
				ftf.setValue(oldValue);
				return true;
			}
			return false;
		}
	}
	
	
	/**
	 * Refreshes Statistics Frame displayed information.
	 */	
	public void refresh()
	{
		_weightsPanel.refresh();
		_resultsPanel.updateResults();
	}
	

	
}

