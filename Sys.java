import java.io.*;

public class Sys
{
  private static final String Sys = System.getProperty("os.name");
  public static final boolean windows = Sys.startsWith("Windows");
  public static final boolean linux = Sys.startsWith("Linux");
  public static final boolean mac = Sys.startsWith("Mac");
  public static final boolean solaris = Sys.startsWith("SunOS");
  public static final boolean iOS = Sys.startsWith("iOS");

  //The application is initialized on start.

  private static String cd = "", app = "", Jar = "-jar ";

  //Convince method to prompt user if they wish to run application as administrator (Super user).
  //Starts the application with command line arguments.

  public static boolean promptAdmin( String args )
  {
    if( app == "" ) { return( false ); }

    File f = new File(System.getProperty("java.home")), f2; f = new File(f, "bin"); f = new File(f, windows ? "javaw.exe" : "java" );

    //The java engine.
    
    String jre = f.getAbsolutePath();

    //Windows

    if( windows )
    {
      try
      {
        //Create run as admin. Note WScript.shell is not rally necessary. The link can be wrote byte by byte into temp.

        f = File.createTempFile ("JFH", ".js"); f2 = File.createTempFile ("JFH", ".lnk"); args = f2.getAbsolutePath() + " " + args;
        
        PrintWriter script = new PrintWriter(f);

        script.printf("var shell = new ActiveXObject(\"WScript.Shell\"),s = shell.CreateShortcut( WScript.arguments(0) );\r\n");
        
        if( cd != "" ) { script.printf("s.WorkingDirectory = \"" + cd.replaceAll("\\\\","\\\\\\\\") + "\";\r\n"); }

        script.printf("s.TargetPath = \"" + jre.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
        script.printf("s.Arguments = \"" + Jar + "\\\"" + app.replaceAll("\\\\","\\\\\\\\") + "\\\" " + args.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
        script.printf("s.Save();"); script.close();

        Process p = Runtime.getRuntime().exec("cscript " + f.getAbsolutePath() + " " + f2.getAbsolutePath() ); p.waitFor(); f.delete();

        //Set byte value in link header to run as administrator. We could write the entire link from scratch here without WScript.shell.

        RandomAccessFile d = new RandomAccessFile( f2, "rw" ); d.seek(0x15); int i = d.read() | 0x20; d.seek(0x15); d.write( i ); d.close();

        //Start the process.

        p = Runtime.getRuntime().exec("cmd /c " + f2.getAbsolutePath() + "");

        //Test if a new process started as administrator.

        while( p.isAlive() ) { if( !f2.exists() ) { return( true ); } }

        //User declined run as administrator.
      
        f2.delete();
      }
      catch( Exception e ) { e.printStackTrace(); }
    }

    //This method works for both Linux, and macOS.
    //It has a small bug on macOS with reading files in user folder, so we will not use it.

    else if( linux )
    {
      InputStreamReader input; OutputStreamWriter output;

      try
      {
        //Create the process.
        
        f = File.createTempFile ("JD-asm", ".sh" ); PrintWriter script = new PrintWriter(f);
        
        script.printf("sudo nohup java " + Jar + "\"" + app + "\" \"" + f.getAbsolutePath() + "\" " + args + " &\r\n");

        /*The following works on macOS. It seems to be a little buggy so we will use the terminal instead.
        script.printf("sudo -i & sudo java " + Jar + "\"" + app + "\" \"" + f.getAbsolutePath() + "\" " + args + "\r\n");*/
        
        script.close();
        
        Process p = new ProcessBuilder(new String[]
        {
          "/bin/bash", "-c", "/usr/bin/sudo -S /bin/cat /etc/sudoers 2>&1 ; echo running ; sudo chmod +x \"" + f.getAbsolutePath() + "\" ; sudo bash \"" + f.getAbsolutePath() + "\""
        }).start();
        
        output = new OutputStreamWriter(p.getOutputStream()); input = new InputStreamReader(p.getInputStream());

        int bytes; char buffer[] = new char[1024];
        
        boolean oneTry = false;
        
        //Password input field.
        
        javax.swing.JPasswordField pwd = new javax.swing.JPasswordField(20);
        
        while ((bytes = input.read(buffer, 0, 1024)) != -1 )
        {
          if( bytes == 0 ) { continue; }
          
          //sudo is requesting Password.
          
          String data = String.valueOf(buffer, 0, bytes);
          
          if ( data.contains("[sudo] password") ) //We compare for data.contains("Password:") on macOS.
          {
            if( !oneTry )
            {
              javax.swing.JOptionPane.showConfirmDialog(null, pwd,"Enter Password",javax.swing.JOptionPane.OK_CANCEL_OPTION);
          
              //Give password to system.
            
              char[] password = pwd.getPassword(); output.write(password); output.write('\n'); output.flush();
            
              //Erase password data.
            
              java.util.Arrays.fill(password, '\0'); oneTry = true;
            }
            
            //Kill sudo on second try.
            
            else { p.destroy(); f.delete(); return( false ); }
          }
          
          //Process started as admin.
          
          else if ( data.contains("running") ) { return( true ); }
        }
      }
      catch (IOException ex) { }
    }

    //We are stuck to using the ugly terminal window on macOS to run java as administrator.

    else if( mac )
    {
      try
      {
        //Create the process.

        long pid = ProcessHandle.current().pid();
        
        f = File.createTempFile("JD-asm", ".command"); f.deleteOnExit(); PrintWriter script = new PrintWriter(f);
        
        script.printf("clear ; echo sudo -i \\& sudo java " + Jar + "\"" + app + "\" admin\n" +
        "sudo -i & sudo java " + Jar + "\"" + app + "\" \"" + f.getAbsolutePath() + "\" " + args +
        " & sudo kill -9 " + pid + "\n");
        
        script.close(); new ProcessBuilder(new String[]
        {
          "/bin/bash", "-c", "chmod +x \"" + f.getAbsolutePath() + "\" ; open \"" + f.getAbsolutePath() + "\""
        }).start();
      }
      catch (Exception ex) { }
    }

    //User declined run as administrator, or operation failed.

    return( false );
  }

  //This method tests if the new process started as administrator.

  public static boolean start( Class cl, String[] args )
  {
    try { app = new File(cl.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath(); } catch(java.net.URISyntaxException e) { }

    //If not a jar. Then we must set current dir path, and app to class file name.

    if( !app.toLowerCase().contains(".jar") ) { Jar = ""; cd = app; app = cl.getName(); }

    //Test if user run as admin.

    boolean test = false;

    if( args.length > 0 )
    {
      if( !(test = args[0].equals("admin")) ) { File f = new File( args[0] ); if( test = f.exists() ) { f.delete(); } }
      
      if( test )
      {
        int i = 1; for( ; i < args.length; args[ i - 1 ] = args[ i ], i++ ); args[ i - 1 ] = "";
      }
    }

    //We should notify the user about the limitations on macOS. As macOS hides downloads and documents folders.
    //This makes it that we can not open files we wish to analyze in downloads or user folders.

    if( !test && mac )
    {
      if( javax.swing.JOptionPane.showConfirmDialog(null, "Running JDasm on macOS to analyze binary files is limited as macOS hides files in user folders.\r\n" +
      "You can still copy files to folders which JDasm can read the files.\r\n" +
      "Would you like to launch this java application as administrator?", null, javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION )
      { promptAdmin( "" ); }
    }

    return( test );
  }
}
