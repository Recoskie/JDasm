package core;

public interface Core
{
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
}