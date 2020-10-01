package WindowCompoents;

import RandomAccessFileV.*;

public interface ExploerEventListener
{
  public void elementOpen(String Element);
  public void read(String File, RandomAccessFileV file);
}