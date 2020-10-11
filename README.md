JFH-Disassembly.
=============================

Welcome to Java Forensics Hex editor.

This is a platform independent application built in Java.

Allowing you to decompile software, hex editing, and forensics, of binary files.

This application currently supports Microsoft binaries .exe, .dll, .sys, .drv, .ocx.

This project makes binary formats visual, easy to map, and modify.

This project is meant to support different binary file formats, and application formats.

------------------------------------------------------------
Running the application.
------------------------------------------------------------

To run the application you only need the JFHApp.jar file. It does not install to the system.

You also need java 8, or higher.

------------------------------------------------------------
IMPORTANT.
------------------------------------------------------------

DLL import reader, and Resource Reader still have to be redesigned, for the new system.

------------------------------------------------------------
The Microcode Disassembler.
------------------------------------------------------------

It was cool to write it in JavaScript. The project can be found <a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">Here</a>.

The disassembler is rebuilt in Java, for this project.

I plan on creating a ARM core disassembler as well, for mobile devices.

------------------------------------------------------------
The new Virtual address mapper.
------------------------------------------------------------

The improved address mapper can be found <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

Without it you do not have a nice virtually mapped binary application in it's proper address space.

Also without it you would not be able to accurately make changes to sections of a program, or read it.

------------------------------------------------------------
Virtual memory view, and hex editor.
------------------------------------------------------------

A fast custom UI hex editor component: https://github.com/Recoskie/VHex

------------------------------------------------------------
Additional planed tools.
------------------------------------------------------------

*Boot sector decompilation.

*File system array structure reader (NTFS, FAT32, EXT)
