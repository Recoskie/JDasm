import java.io.*;

public class Sys
{
  private static final String Sys = System.getProperty("os.name");

  public static final boolean windows = Sys.startsWith("Windows");
  public static final boolean linux = Sys.startsWith("Linux");
  public static final boolean mac = Sys.startsWith("Mac");
  public static final boolean solaris = Sys.startsWith("SunOS");
  public static final boolean iOS = Sys.startsWith("iOS");

  //Convince method to prompt user if they wish to run application as administrator (Super user).
  //Starts the application with command line arguments.

  public boolean promptAdmin( String args )
  {
    File f = new File(System.getProperty("java.home")), f2; f = new File(f, "bin"); f = new File(f, "javaw.exe");

    //The java engine.
      
    String jre = f.getAbsolutePath(), app = "";

    //The application location.

    try { app = new File(Sys.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath(); } catch(java.net.URISyntaxException e) { }

    //Windows

    if( windows )
    {
      try
      {
        //Create run as admin. Note WScript.shell is not rally necessary. The link can be wrote byte by byte into temp.

        f = File.createTempFile ("JFH", ".js"); f2 = File.createTempFile ("JFH", ".lnk"); args = f2.getAbsolutePath() + " " + args;
        
        PrintWriter script = new PrintWriter(f);

        script.printf("var shell = new ActiveXObject(\"WScript.Shell\"),s = shell.CreateShortcut( WScript.arguments(0) );\r\n");
        script.printf("s.TargetPath = \"" + jre.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
        script.printf("s.Arguments = \"-jar \\\"" + app.replaceAll("\\\\","\\\\\\\\") + "\\\" " + args.replaceAll("\\\\","\\\\\\\\") + "\";\r\n");
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

    //Linux does not have a nice prompt like Mac OS X. So we have to create one.

    else if( linux )
    {
      InputStreamReader input; OutputStreamWriter output;

      try
      {
        //Create the process.
        
        f = File.createTempFile ("JFH", ".sh"); PrintWriter script = new PrintWriter(f);

        script.printf("#!/bin/sh\r\n"); script.printf("sudo nohup java -jar \"" + app + "\" \"" + f.getAbsolutePath() + "\" " + args + " &\r\n"); script.close();
        
        Process p = Runtime.getRuntime().exec(new String[]
        {
          "/bin/bash", "-c", "/usr/bin/sudo -S /bin/cat /etc/sudoers 2>&1 ; echo running ; sudo chmod +x \"" + f.getAbsolutePath() + "\" ; sudo bash \"" + f.getAbsolutePath() + "\""
        });
        
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
          
          if (data.contains("[sudo] password"))
          {
            if( !oneTry )
            {
              int action = javax.swing.JOptionPane.showConfirmDialog(null, pwd,"Enter Password",javax.swing.JOptionPane.OK_CANCEL_OPTION);
          
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
      catch (IOException ex) { System.out.println(ex.getMessage()); }
    }

    //Mac OS X. Much cleaner with osascript. Be nice if linux had an equivalent.

    else if( mac )
    {
      try
      {
        //Create the process.
        
        f = File.createTempFile ("test", ".command"); PrintWriter script = new PrintWriter(f);

        script.printf("osascript -e \"do shell script \\\"java -jar '" + app + "' '" + f.getAbsolutePath() + "' " + args + "\\\" with administrator privileges\"\r\n");
        script.close();
        
        Process p = new ProcessBuilder(new String[] {"chmod", "+x", "'" + f.getAbsolutePath() + "'"} ).start(); p.waitFor();

        p = new ProcessBuilder(new String[] {"sh", "" + f.getAbsolutePath() + ""} ).start();

        //Test if a new process started as administrator.

        while( p.isAlive() ) { if( !f.exists() ) { return( true ); } }

        //User declined run as administrator.
      
        f.delete();
      }
      catch (Exception ex) { System.out.println(ex.getMessage()); }
    }

    //User declined run as administrator, or operation failed.

    return( false );
  }

  //This method tests if the new process started as administrator.

  public boolean start( String[] args )
  {
    boolean test = false;

    if( args.length > 0 )
    {
      File f = new File( args[0] );
      
      test = f.exists();
      
      if( test )
      {
        int i = 1; for( ; i < args.length; args[ i - 1 ] = args[ i ], i++ ); args[ i - 1 ] = ""; f.delete();
      }
    }

    return( test );
  }
}
