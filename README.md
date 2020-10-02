Disassembler, and format decompiler.
=============================

This application supports Microsoft binaries .exe, .dll, .sys, .drv, .ocx.

This project makes binary formats visual, easy to map, and modify.

This project is meant to support different binary file formats, and application formats.

------------------------------------------------------------
IMPORTANT.
------------------------------------------------------------

DLLimport, and Resource Reader still have to be redesigned, for the new system.

------------------------------------------------------------
The Microcode Disassembler.
------------------------------------------------------------

It was cool to write it in JavaScript. The porject can be found <a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">Here</a>.

The disassembler is rebuilt in Java, for this project.

------------------------------------------------------------
The new Virtual address mapper.
------------------------------------------------------------

The improved address mapper can be found <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

The virtual pointer in the new file system will be the instruction pointer, for the java version of my Disassembler.

I plan on creating a ARM core disassembler as well.

------------------------------------------------------------
Virtual memory view, and hex editor.
------------------------------------------------------------

A fast custom UI hex editor component: https://github.com/Recoskie/VHex

------------------------------------------------------------
Additional planed tools.
------------------------------------------------------------

Data type inspector.

Disc sector reader.

Boot sector decompilation.

File system array structure reader (NTFS, FAT32, EXT)
