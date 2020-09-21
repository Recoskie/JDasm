package RandomAccessFileV;
import java.io.*;
import java.util.*;
import javax.swing.event.*;

//Basic IO Events.

public interface IOEventListener extends EventListener
{
  public void onSeek( IOEvent evt );
  public void onRead( IOEvent evt );
  public void onWrite( IOEvent evt );
}