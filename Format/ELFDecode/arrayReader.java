package Format.ELFDecode;
import swingIO.*;
import swingIO.tree.*;

public class arrayReader extends Data implements sec
{
  public Descriptor[] read() throws java.io.IOException
  {
    java.util.LinkedList<Descriptor> des = new java.util.LinkedList<Descriptor>(); 

    //We only use one descriptor for each array as it is only disassemble locations.

    int ref = 0, end = 0; Descriptor loc;

    //WE read all link library sections.

    JDNode sects = sections[7], curSec = null;

    for( int i = 0, size = sects.getChildCount(); i < size; i++ )
    {
      curSec = (JDNode)sects.getChildAt(i);

      //Setup descriptor.

      file.seekV(curSec.getArg(1)); loc = new Descriptor( file, true );

      //Number of address locations.

      end = (int)( curSec.getArg(2) / ( is64Bit ? 8 : 4 ) );

      //Setup section node.

      curSec.setUserObject( ((String)curSec.getUserObject()).replace(".h","") );

      curSec.setArgs( new long[]{ 5, ref } ); des.add( loc ); ref += 1;

      //Read locations.

      for( int i2 = 0; i2 < end; i2++ )
      {
        if( is64Bit ) { if( isLittle ) { loc.LUINT64("Location"); } else { loc.UINT64("Location"); } }
        else { if( isLittle ) { loc.LUINT32("Location"); } else { loc.UINT32("Location"); } }
        
        curSec.add( new JDNode("Loc #" + i2 + ".h", new long[]{ -1, is64Bit ? (long)loc.value : (int)loc.value } ) );
      }
    }

    return( des.toArray( new Descriptor[ des.size() ] ) );
  }
}
