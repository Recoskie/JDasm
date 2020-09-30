package core;

public interface Core
{
  //Disassemble a single operation.

  public String disASM() throws Exception;

  //Position operations.

  public void setPos( long pos ) throws Exception;
  public void setPosV( long pos ) throws Exception;
  public long getPos() throws Exception;
  public long getPosV() throws Exception;
  public String pos() throws Exception;
  public String posV() throws Exception;

  //Core bit mode.

  public void setBit( int mode );
}