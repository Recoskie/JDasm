package core;

public interface Core
{
  public java.util.LinkedList<String> mapped_loc = new java.util.LinkedList<String>();
  public java.util.LinkedList<Long> mapped_pos = new java.util.LinkedList<Long>();
  public java.util.LinkedList<Long> locations = new java.util.LinkedList<Long>(); //Used with data model for navigating code.

  //Disassemble a single operation.

  public String disASM() throws java.io.IOException;

  //Position operations.

  public void setPos( long pos ) throws java.io.IOException;
  public void setPosV( long pos ) throws java.io.IOException;
  public long getPos() throws java.io.IOException;
  public long getPosV() throws java.io.IOException;
  public String pos() throws java.io.IOException;
  public String posV() throws java.io.IOException;

  //Core bit mode.

  public void setBit( int mode );

  //Set code segment position.

  public void setSeg( short cs );
}