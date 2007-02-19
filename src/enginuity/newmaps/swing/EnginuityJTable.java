package enginuity.newmaps.swing;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JTable;

public class EnginuityJTable extends JTable
{


    HashSet<Point> cellSelection = new HashSet<Point>();
    HashSet<Point> currentSelection = new HashSet<Point>();
    HashSet<Cell> clipBoard = new HashSet<Cell>();
    Point lastSelectedCell = null;
    Point currentSelectedCell = null;


    private Color oldSelectionBackgroundColor;
    private Color copySelectionColour = Color.red;
    private Color cutSelectionColour = Color.yellow;


    private boolean check = false;

    public static final int COPY = 1;
    public static final int CUT = 2;

    private int action;

	public EnginuityJTable(int columns, int rows)
	{
		super (rows, columns);

		MouseMotionListener[] cls = (MouseMotionListener[])(this.getListeners(MouseMotionListener.class));
		for ( int i = 0; i < cls.length; i++)
		{
			if (i == 2)
				this.removeMouseMotionListener( cls[i] );
		}

		this.addKeyListener( new EventKeyHandler(this)  );
		this.addMouseMotionListener( new MouseMotionH(this) );
                this.setCellSelectionEnabled( true );
                this.setSelectionBackground( Color.blue );
	}


    public void changeSelection(
        int row, int column, boolean toggle, boolean extend)
    {
        super.changeSelection(row, column, toggle, extend);
        if (!check)
		{
			oldSelectionBackgroundColor = getSelectionBackground();
			check = true;
		}


        // Don't think this makes sense
        if (toggle && extend)
        {
        	//currentSelectedCell = new Point(row, column);
        	extendCellSelection(row, column);
        	return;
        }

        if (toggle)
        {
        	if (currentSelectedCell!= null && !currentSelection.isEmpty())
        	{
        		cellSelection.addAll( currentSelection );
        		currentSelection.clear();
        	}
            toggleCellSelection(row, column);
            return;
        }

        if (extend)
        {
            extendCellSelection(row, column);
            return;
        }

        // Clear all selections
        cellSelection.clear();
        currentSelection.clear();
        cellSelection.add(new Point(row, column));
        lastSelectedCell = new Point(row, column);

        /*System.out.println(cellSelection);
        System.out.println(currentSelection);*/
        this.setSelectionBackground( oldSelectionBackgroundColor );
        repaint();
    }

    private void toggleCellSelection(int row, int column)
    {
        Point p = new Point(row, column);

        if (cellSelection.contains(p))
            cellSelection.remove(p);
        else
            cellSelection.add(p);
        lastSelectedCell = p;
        //System.out.println("--------------------");
        repaint();
    }

    private void extendCellSelection(int row, int column)
    {
    	currentSelectedCell = new Point(row, column);
    	/*if a drag only into one cell*/
    	if (currentSelectedCell.equals( lastSelectedCell ))
    		return;
    	currentSelection.clear();
    	Point p = null;
    	int minColumn;
    	int minRow;
    	int maxColumn;
    	int maxRow;
    	if (currentSelectedCell.y < lastSelectedCell.y)
    	{
    		minColumn = currentSelectedCell.y;
    		maxColumn = lastSelectedCell.y;
    	}
    	else
    	{
    		minColumn = lastSelectedCell.y;
    		maxColumn = currentSelectedCell.y;
    	}

    	if (currentSelectedCell.x < lastSelectedCell.x)
    	{
    		minRow = currentSelectedCell.x;
    		maxRow = lastSelectedCell.x;
    	}
    	else
    	{
    		minRow = lastSelectedCell.x;
    		maxRow = currentSelectedCell.x;
    	}
    	for ( int i = minRow; i <= maxRow ; i++)
		{
    		for ( int j = minColumn; j <= maxColumn; j++)
    		{
    			p = new Point( i, j );
    			currentSelection.add( p );
    		}
		}

    	repaint();

    }

    public boolean isCellSelected(int row, int column)
    {
    		return (cellSelection.contains(new Point(row, column))) || (currentSelection.contains(new Point(row, column)));
    }

    public boolean isColumnSelected(int column)
    {
        return false;
    }

    public boolean isRowSelected(int row)
    {
        return false;
    }

	public Point getCurrentSelectedCell()
	{
		return currentSelectedCell;
	}

	public void setCurrentSelectedCell( Point currentSelectedCell )
	{
		this.currentSelectedCell = currentSelectedCell;
	}

	public Point getLastSelectedCell()
	{
		return lastSelectedCell;
	}

	public void setLastSelectedCell( Point lastSelectedCell )
	{
		this.lastSelectedCell = lastSelectedCell;
	}


	public HashSet getCellSelection()
	{
		return cellSelection;
	}

	public void setCellSelection( HashSet<Point> cellSelection )
	{
		this.cellSelection = cellSelection;
	}

	public void clearSelection(  )
	{
		if ( cellSelection != null )
			this.cellSelection.clear();
	}

	public void setValueAt(Object aValue, int column, int row) {
		super.setValueAt(aValue, row, column);
	}




    /**
     * The set with the selected cells copied do a new HashSet that playes the role of the clpiboard
     */
	public HashSet getClipBoard()
	{
		return clipBoard;
	}

	public void toClipBoard( int cut )
	{
		Point cell = null;
		HashSet<Point> clipBoardHS = new HashSet<Point>();

		this.clipBoard.clear();
		clipBoardHS.addAll( cellSelection );
		clipBoardHS.addAll( currentSelection );

		 for ( Iterator iter = clipBoardHS.iterator(); iter.hasNext();)
		{
			cell = (Point)iter.next();
			clipBoard.add( new Cell(cell, this.getValueAt((int)cell.getX(), (int)cell.getY())) );

			if (cut == EnginuityJTable.CUT)
			{
				setSelectionBackground(this.cutSelectionColour);
				setAction( EnginuityJTable.CUT);
			}
			else
				if (cut == EnginuityJTable.COPY)
				{
					setSelectionBackground(this.copySelectionColour);
					setAction( EnginuityJTable.COPY);
				}
			repaint();
		}


	}

	public void moveToClipBoard(  )
	{
		Point cell = null;
		HashSet<Point> clipBoardHS = new HashSet<Point>();

		this.clipBoard.clear();
		clipBoardHS.addAll( cellSelection );
		clipBoardHS.addAll( currentSelection );


		 for ( Iterator iter = clipBoardHS.iterator(); iter.hasNext();)
		{
			cell = (Point)iter.next();
			clipBoard.add( new Cell((int)cell.getX(), (int)cell.getY(), this.getValueAt((int)cell.getX(), (int)cell.getY())) );

		}



	}

	/**
	 * Get the cell with the minimum X and the Minimum Y
	 * If doesn't exists this cell then the paste cannot be made
	 * @throws Exception
	 */
	private Cell getMinCell(HashSet clipboard)
	{


		//sort by X Coordinate
		Object[] cells = clipboard.toArray();
		Arrays.sort(cells, new XComparator<Object>());

		//sort by Y Coordinate
		Arrays.sort((Object[])cells, new YComparator<Object>());
		//System.out.println("orderedByY: " + (Cell)cells[0]);
		Cell minY = (Cell)cells[0];

		//sort again by X coordinate
		Arrays.sort((Object[])cells, new XComparator<Object>());
		Cell minX = (Cell)cells[0];
		//System.out.println("orderedByX: " + (Cell)cells[0]);

		if (minX.equals(minY))
			return minX;


		 return null;
	}


	class MouseMotionH implements MouseMotionListener
	{
		private EnginuityJTable myJTable = null;
    	public MouseMotionH(EnginuityJTable myJTable)
    	{
    		super();
    		this.myJTable = myJTable;
    	}
		public void mouseDragged( MouseEvent e )
		{

            Point p = e.getPoint();

            int row = myJTable.rowAtPoint(p);
            int column = myJTable.columnAtPoint(p);
            myJTable.changeSelection(row, column, false, true);
		}
		public void mouseMoved( MouseEvent e )
		{
			//System.out.println("move");

		}

	}

    class EventKeyHandler extends KeyAdapter{
    	private EnginuityJTable myJTable = null;


    	public EventKeyHandler(EnginuityJTable myJTable)
    	{
    		super();
    		this.myJTable = myJTable;
    	}

        public void keyPressed(KeyEvent e)
        {
        	/*Copy*/
        	//System.out.println(e.getModifiers());
        	if ((e.isControlDown() && e.getKeyCode() == 67 ))
        	{
        		myJTable.toClipBoard(EnginuityJTable.COPY);
        		return;
        	}
        	else
        	/*Paste*/
        	if ((e.isControlDown() && e.getKeyCode() == 86 ))
        	{
        		Cell cell = getMinCell(clipBoard);
        		Cell cutCell = null;


				if (cell != null)
				{

					if (myJTable.getAction() == EnginuityJTable.CUT)
						for ( Iterator iter = clipBoard.iterator(); iter.hasNext(); )
						{
							cutCell = (Cell)iter.next();
							myJTable.setValueAt( null ,(int)cutCell.getX(), (int)cutCell.getY() );
						}

					int deltaX, deltaY;
					deltaX = lastSelectedCell.x - cell.getX();
					deltaY = lastSelectedCell.y - cell.getY();
					for (Iterator iter = clipBoard.iterator(); iter.hasNext();)
					{
						Cell vCell = (Cell) iter.next();
						vCell.setX(vCell.getX() + deltaX);
						vCell.setY(vCell.getY() + deltaY);
					}
				}

				else {
					System.err.println("Copy/Cut Selection ambigous. Could not Paste");
					// throw new Exception ("Copy/Cut Selection ambigous");
				}

                for ( Iterator iter = clipBoard.iterator(); iter.hasNext(); )
				{
                	cell = (Cell)iter.next();
                	if (cell.getX() < myJTable.getRowCount() && cell.getY() < myJTable.getColumnCount())
                		myJTable.setValueAt(cell.getValue(), cell.getX(), cell.getY());
				}
                return;
        	}
        	else
        	/*Cut*/
        	if ((e.isControlDown() && e.getKeyCode() == 88 ))
        	{
        		myJTable.toClipBoard(EnginuityJTable.CUT);
        		return;
        	}
        	else
        	if (e.getModifiers() == (InputEvent.CTRL_MASK  | InputEvent.SHIFT_MASK  | InputEvent.BUTTON1_MASK ))
        	{
        		//myJTable.changeSelection(5,5,false, true);

        	}
        	else
        	if (e.getModifiers() == (InputEvent.SHIFT_MASK  | InputEvent.BUTTON1_MASK))
        	{
        		cellSelection.clear();
                //currentSelection.clear();
        		//myJTable.changeSelection((int)myJTable.getLastSelectedCell().getX(),(int)myJTable.getLastSelectedCell().getY(),false,false);
                repaint();
        		//System.out.println("SHIFT + CLICK");
        	}else
            	if (e.getModifiers() == (InputEvent.CTRL_MASK  | InputEvent.BUTTON1_MASK))
            	{
                    //currentSelection.clear();
            		myJTable.changeSelection((int)myJTable.getLastSelectedCell().getX(),(int)myJTable.getLastSelectedCell().getY(),false,true);
                    repaint();
            		//System.out.println("CTRL + CLICK");
            	}

        }
    }


    class Cell
    {
    	private Object value;
    	private int x;
    	private int y;

    	public Cell(){};

    	public Cell(int x, int y, Object value)
    	{
    		this.x = x;
    		this.y = y;
    		this.value = value;
    	}

    	public Cell(Point p, Object value)
    	{
    		this.x = (int)p.getX();
    		this.y = (int)p.getY();
    		this.value = value;
    	}

		public Object getValue()
		{
			return value;
		}
		public void setValue( Object value )
		{
			this.value = value;
		}
		public int getX()
		{
			return x;
		}
		public void setX( int x )
		{
			this.x = x;
		}
		public int getY()
		{
			return y;
		}
		public void setY( int y )
		{
			this.y = y;

		}

    	public boolean equals(Object o)
    	{
    		Cell cell = null;
    		if (!(o instanceof Cell))
    			return false;

    		cell = (Cell)o;
    		if (cell.getX()==this.x && cell.getY() == this.y)
    			return true;

    		return false;
    	}

    	public String toString()
    	{
    		return "(" + this.x + ", " + this.y + ") = " + this.value;
    	}
    }

    /**
     * order the Cells by the X coordinate
     * @author fane
     *
     */
    private class XComparator<T> implements Comparator<T>
    {

		public int compare(T o1, T o2) {
			return ((Cell)o1).getX() - ((Cell)o2).getX();
		}

    }

    /**
     * order the Cells by the Y coordinate
     * @author fane
     *
     */
    private class YComparator<T> implements Comparator<T>
    {
		public int compare(T o1, T o2) {
			return ((Cell)o1).getY() - ((Cell)o2).getY();
		}

    }

	public Color getCopySelectioColour() {
		return copySelectionColour;
	}


	public void setCopySelectionColour(Color copySelectedColour) {
		this.copySelectionColour = copySelectedColour;
	}


	public Color getCutSelectionColour() {
		return cutSelectionColour;
	}


	public void setCutSelectionColour(Color cutSelectedColour) {
		this.cutSelectionColour = cutSelectedColour;
	}


	public int getAction() {
		return action;
	}


	public void setAction(int action) {
		this.action = action;
	}
}
