package cellPane;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

//Uses custom layout with widgets that control the components in window.

public class CellPane extends JComponent implements MouseMotionListener, MouseListener, AncestorListener
{
  //Rows and cols minium, or preferred size.

  class Dims
  {
    int perf = 0, min = 0, val = 0;

    public Dims( int pf, int m, int p )
    {
      perf = pf; min = m; val = p;
    }
  }

  //All components and rows are divided by adjustable lines.
  //The dividers have a maximum or minium size.

  class adj
  {
    int min = 0, max = 0;

    public adj( int mn, int mx )
    {
      min = mn; max = mx;
    }
  }

  private boolean layoutInitialized = false;
  private CellLayout cLayout;
  private adj adjSize;

  //Row organization.

  private ArrayList<Integer> rowLen = new ArrayList<Integer>();
  private int rows = 0, len = 0;

  //Getting components sizes can take up time. It is better to compute dimensions when components change.

  private ArrayList<Dims> Rows = new ArrayList<Dims>();
  private ArrayList<Dims> Cols = new ArrayList<Dims>();

  //Gap between components.

  private int gap = 7;

  //Row min/max adjustable size Varies based on visible component's.

  public void rowAdjustableSize()
  {
    int min = 0, max = gap;

    for( int r = 0; r < rows; r++ )
    {
      if( r <= eRow ) { min += Rows.get( r ).min; } else { max += Rows.get( r ).min; }
    }

    adjSize = new adj( min, this.getHeight() - max );
  }

  //Col min/max adjustable size Varies based on visible component's in col.

  public void colAdjustableSize()
  {
    int min = 0, max = 0;

    int start = eRow <= 0 ? 0 : rowLen.get( eRow - 1 ) + 1, end = rowLen.get( eRow );

    for( int c = start; c <= end; c++ )
    {
      if( c <= eCol ) { min += Cols.get( c ).min + gap; } else { max += Cols.get( c ).min + gap; }
    }

    adjSize = new adj( min - gap, this.getWidth() - max );
  }

  //The layout system.

  class CellLayout implements LayoutManager
  {
    private int preferredWidth = 0, preferredHeight = 0;
    private int minWidth = 0, minHeight = 0;
 
    public CellLayout() { }

    public void addLayoutComponent(String name, Component comp) {}
  
    public void removeLayoutComponent(Component comp) {}

    //Called when enabling disabling components, or setting visibility.
 
    public void updateSizes(Container parent)
    {
      Rows.clear(); Cols.clear();
      int nComps = parent.getComponentCount();
      Dimension perf = null, min = null;

      Component c;
 
      //Reset preferred/minimum width and height.
    
      preferredWidth = 0; minWidth = 0;
      preferredHeight = 0; minHeight = 0;

      //The components are split by row using the max preferred height.

      int rowHeight = 0;
      int minHeight = 0;
      int w1 = 0, w2 = 0;
      len = rowLen.get(0);
 
      for (int col = 0, row = 0; col < nComps; col++ )
      {
        c = parent.getComponent(col);
        
        if( c.isVisible() )
        {
          perf = c.getPreferredSize(); min = c.getMinimumSize();
        }
        else
        {
          perf = new Dimension( gap, gap ); min = new Dimension( gap, gap );
        }

        preferredWidth += perf.width + gap; minWidth += min.width + gap;
 
        rowHeight = Math.max( perf.height, rowHeight ); minHeight = Math.max( min.height, minHeight );

        Cols.add( new Dims( perf.width, min.width, perf.width ) );

        if( col >= len )
        {
          row += 1; if( row < rows ) { len = rowLen.get(row); }
          
          preferredHeight += rowHeight + gap;

          Rows.add( new Dims( rowHeight, minHeight, rowHeight ) ); rowHeight = 0; minHeight = 0;

          w1 = Math.max( w1, preferredWidth ); w2 = Math.max( w2, minWidth ); preferredWidth = 0; minWidth = 0;
        }
      }

      preferredHeight -= gap; preferredWidth = w1 - gap; minWidth = w2 - gap; layoutInitialized = true;
    }
  
    public Dimension preferredLayoutSize(Container parent)
    {
      Dimension dim = new Dimension(0, 0);
 
      if( !layoutInitialized ) { updateSizes(parent); }
    
      dim.width = preferredWidth; dim.height = preferredHeight;
 
      return( dim );
    }
  
    public Dimension minimumLayoutSize(Container parent)
    {
      Dimension dim = new Dimension(0, 0);
 
      if( !layoutInitialized ) { updateSizes(parent); }
    
      dim.width = minWidth; dim.height = minHeight;
 
      return( dim );
    }
 
    public void layoutContainer(Container parent)
    {
      Insets insets = parent.getInsets();
      int nComps = parent.getComponentCount();
      int x = 0, y = insets.top;

      int lx = 0; //last visible col.
    
      if ( !layoutInitialized ) { updateSizes(parent); }

      int len = rowLen.get(0); for( lx = len; lx > 0; lx-- ){ if( parent.getComponent( lx ).isVisible() ){ break; } }
 
      for ( int col = 0, row = 0; col < nComps; col++ )
      {
        Component c = parent.getComponent( col );
      
        if (c.isVisible())
        {
          c.setBounds(x, y, col >= lx ? parent.getWidth() - x : Cols.get( col ).val, Rows.get( row ).val );

          if( col < len )
          {
            x += Cols.get(col).val + gap;
          }
          else
          {
            x = 0; y += Rows.get(row).val + gap;

            row += 1; if( row < rowLen.size() ) { for( lx = rowLen.get( row ); lx > len; lx-- ){ if( parent.getComponent( lx ).isVisible() ){ break; } } len = rowLen.get( row ); }
          }
        }
      }
    }
  }

  //Adjust rows on size change.

  public void adj( int w, int h )
  {
    if( layoutInitialized )
    {
      //last active row is resized.

      int c = 0, i = 0;

      for( c = this.getComponentCount() - 1; c > 0; c-- )
      {
        if( this.getComponent(c).isVisible() ) { break; }
      }

      for( i = 0; i < rows; i++ )
      {
        if( rowLen.get(i) >= c ) { eRow = i; break; }
      }

      ny = h; setRow();
    }

    eCol = -1; eRow = -1;
  }

  //Construct the split layout system.

  public CellPane()
  {
    cLayout = new CellLayout(); this.setLayout( cLayout );

    //Note the mouse listener only works between vgap.
    //Useful for mouse enter and exit. Mouse press determines which line we are adjusting in layout.

    this.addMouseListener(this); this.addMouseMotionListener(this);

    //This tracks the mouse on top of the components. For mouse drag, and release when adjusting the layout.
    
    this.addAncestorListener(this);
  }

  //Paint the adjustable lines.

  public void paint( Graphics g )
  {
    super.paint(g); //paints the components.

    //Graphics to draw over components. Drag and drop.

    if( eRow >= 0 )
    {
      g.setColor(Color.GRAY);

      if( eCol >= 0 )
      {
        g.fillRect( nx, rh, gap, Rows.get( eRow ).val );
      }
      else
      {
        g.fillRect( 0, ny, this.getWidth(), gap );
      }
    }

    //The adjustable grid lines.

    g.setColor( Color.BLACK );

    int count = this.getComponentCount() - 1;
    Point pos;
    Dimension dim;
    Component c;
    
    len = rowLen.get(0);

    for( int col = 0, row = 0; col < count; col++ )
    {
      c = this.getComponent( col );
      pos = c.getLocation();
      dim = c.getSize();

      if( c.isVisible() && Cols.get(col).val != -1 )
      {
        if( col >= len )
        {
          row += 1; len = rowLen.get(row);

          g.fillRect( 0, pos.y + dim.height, this.getWidth(), gap );
        }
        else
        {
          g.fillRect( pos.x + dim.width, pos.y, gap, dim.height );
        }
      }
    }
  }

  //Set a row to a new size. If row can not be made smaller. Squishes previous rows to min size. 

  private void setRow()
  {
    if( Rows.size() <= eRow ) { return; }

    int oy = 0; for( int i = 0; i <= eRow; i++ )
    {
      oy += Rows.get(i).val + ( i > 0 ? gap : 0 );
    }

    int dif = ny - oy;

    //Backwards from adjusting slider.

    if( dif < 0 )
    {
      if( eRow < ( rows - 1 ) ) { Rows.get( eRow + 1 ).val -= dif; }

      for( int i = eRow; i >= 0; i--)
      {
        if( ( Rows.get( i ).val + dif ) > Rows.get( i ).min )
        {
          Rows.get( i ).val += dif; dif = 0;
        }
        else
        {
          dif += Rows.get( i ).val - Rows.get( i ).min; Rows.get( i ).val = Rows.get( i ).min;
        }
      }
    }
    else
    {
      Rows.get( eRow ).val += dif;

      for( int i = eRow + 1; i < rows; i++ )
      {
        if( ( Rows.get( i ).val - dif ) > Rows.get( i ).min )
        {
          Rows.get( i ).val -= dif; dif = 0;
        }
        else
        {
          dif -= Rows.get( i ).val - Rows.get( i ).min; Rows.get( i ).val = Rows.get( i ).min;
        }
      }
    }

    revalidate();
  }

  private void setCol()
  {
    int start = eRow <= 0 ? 0 : rowLen.get( eRow - 1 ), end = rowLen.get( eRow );

    int ox = 0; for( int i = eRow > 0 ? start + 1 : start; i <= eCol; i++ )
    {
      ox += Cols.get(i).val + ( i > start ? gap : 0 );
    }

    int dif = nx - ox;

    //Backwards from adjusting slider.

    if( dif < 0 )
    {
      if( eCol < end ) { Cols.get( eCol + 1 ).val -= dif; }

      for( int i = eCol; i >= start; i--)
      {
        if( ( Cols.get( i ).val + dif ) > Cols.get( i ).min )
        {
          Cols.get( i ).val += dif; dif = 0;
        }
        else
        {
          dif += Cols.get( i ).val - Cols.get( i ).min; Cols.get( i ).val = Cols.get( i ).min;
        }
      }
    }
    else
    {
      Cols.get( eCol ).val += dif;

      for( int i = eCol + 1; i < end; i++ )
      {
        if( ( Cols.get( i ).val - dif ) > Cols.get( i ).min )
        {
          Cols.get( i ).val -= dif; dif = 0;
        }
        else
        {
          dif -= Cols.get( i ).val - Cols.get( i ).min; Cols.get( i ).val = Cols.get( i ).min;
        }
      }
    }

    revalidate();
  }

  //We subtract one in order to match the component index.

  public void rowEnd() { rows += 1; rowLen.add( this.getComponentCount() - 1 ); }

  //Point to element clicked.

  private int nx = 0, ny = 0;
  private int rh = 0;
  private int eCol = -1, eRow = -1;

  public void mouseMoved(MouseEvent e) { }

  public void mouseDragged(MouseEvent e)
  {
    if( eRow >= 0 )
    {
      nx = e.getX(); ny = e.getY();
      
      if( eRow >= 0 )
      {
        if( eCol >= 0 )
        {
          if( nx < adjSize.min ) { nx = adjSize.min; }
          if( nx > adjSize.max ) { nx = adjSize.max; }
        }
        else
        {
          if( ny < adjSize.min ) { ny = adjSize.min; }
          if( ny > adjSize.max ) { ny = adjSize.max; }
        }
      }
      
      repaint();
    }
  }

  public void mouseExited(MouseEvent e)
  {
    if( eRow < 0 ) { this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); }
  }

  public void mouseEntered(MouseEvent e)
  {
    if( eRow < 0 )
    {
      //If in alignment to a row.

      int yp = e.getY();

      for( int row = 0, y = 0; row < rows; row++ )
      {
        y += Rows.get(row).val + gap; if( yp >= ( y - gap ) && yp <= y )
        {
          this.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR)); return;
        }
      }

      //is a col.

      eCol = 0; this.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
    }
  }

  public void mouseReleased(MouseEvent e)
  {
    if( eRow >= 0 )
    {
      if( eCol >= 0 )
      { 
        setCol();
      }
      else
      {
        setRow();
      }
    }

    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    
    eCol = -1; eRow = -1; repaint();
  }

  //Only calculate the exact row or col on mouse press.
  //Activates when mouse enters a bar.

  public void mousePressed(MouseEvent e)
  {
    //The adjustable grid lines.

    nx = e.getX(); ny = e.getY();

    int count = this.getComponentCount() - 1;
    Point pos;
    Dimension dim;
    Component c;
    
    len = rowLen.get(0);

    for( int col = 0, row = 0; col < count; col++ )
    {
      c = this.getComponent( col ); pos = c.getLocation(); dim = c.getSize();

      if( c.isVisible() )
      {
        rh = pos.y;

        if( col >= len )
        {
          row += 1; len = rowLen.get(row);

          if( ny > rh + dim.height && ny < rh + dim.height + gap )
          {
            eCol = -1; eRow = row - 1; rowAdjustableSize(); return;
          }
        }
        else
        {
          if( ( nx > pos.x + dim.width && nx < pos.x + dim.width + gap ) && ( ny > rh && ny < rh + dim.height ) )
          {
            eCol = col; eRow = row; colAdjustableSize(); return;
          }
        }
      }
    }
  }

  public void mouseClicked(MouseEvent e) { }

  //Called by layout managers.

  public void setBounds(int x, int y, int width, int height)
  {
    super.setBounds(x, y, width, height); 
    
    if( layoutInitialized ) { adj( width, height ); }
  }

  public void add(JComponent c)
  {
    c.addAncestorListener(this); super.add(c);
  }

  public void ancestorAdded ( AncestorEvent event )
  {
    if( layoutInitialized ) { cLayout.updateSizes(this); adj( this.getWidth(), this.getHeight() ); }
  }

  public void ancestorRemoved ( AncestorEvent event )
  {
    if( layoutInitialized ) { cLayout.updateSizes(this); adj( this.getWidth(), this.getHeight() ); }
  }

  public void ancestorMoved ( AncestorEvent event ){}
}