This tool allows you to decompile software. Allowing you to create source code, of binary files.

It is also a hex editor, and data forensics tool, for binary files.

You can also read disk drives sector by sector.

This application currently supports Microsoft binaries formats .exe, .dll, .sys, .drv, .ocx.

This project is meant to support many different binary file formats, and application formats.

However only supports Microsoft binary format at the moment. If you wish to contribute a Format. Put it under the format folder.

------------------------------------------------------------

# Help, and documentation.

------------------------------------------------------------

Using JFH-Disassembly Basics: <a href="http://recoskie.github.io/JFH-Disassembly/docs/Basics.html">Link</a><br />
Machine code: <a href="http://recoskie.github.io/JFH-Disassembly/docs/Machine.html">Link</a><br />
High Level Code: <a href="http://recoskie.github.io/JFH-Disassembly/test/docs/Code.html">Link</a>

------------------------------------------------------------

# Running the application.

------------------------------------------------------------

To run the application you need to open the file <strong>JFHApp.jar</strong>. It does not install to the system.

You will also need Java 8, or later installed (latest version is preferred).

You can also download the project. Clone the entire project if you wish to make changes, or have a copy of the source code.

------------------------------------------------------------

# The Microcode Disassembler.

------------------------------------------------------------

It was cool to write it in JavaScript. The project can be found <a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">Here</a>.

The disassembler is rebuilt in Java, for this project.

I plan on creating a ARM core disassembler as well, for mobile devices.

------------------------------------------------------------

# The new Virtual address mapper.

------------------------------------------------------------

The improved address mapper can be found <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

Without it you do not have a nice virtually mapped binary application in it's proper address space.

Also without it. You would not be able to accurately make changes to sections of a program, or read it.

------------------------------------------------------------

# Virtual memory view, and hex editor.

------------------------------------------------------------

A fast custom UI hex editor component: https://github.com/Recoskie/VHex

------------------------------------------------------------

# Additional planed tools.

------------------------------------------------------------

[ ] Boot sector decompilation.

[ ] File system array structure reader (NTFS, FAT32, EXT)
