package WindowComponents;

import RandomAccessFileV.*;

public interface ExplorerEventListener
{
  public void elementOpen(String Element);
  public void read(String File, RandomAccessFileV file);
}