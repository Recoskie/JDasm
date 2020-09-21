Microsoft-Exe-and-Dll-decoder
=============================

EXE Header Decoder and DLLimport decode and Resource Reader.

------------------------------------------------------------
IMPORTANT.
------------------------------------------------------------

DLLimport, and Resource Reader still have to be redesinged, for the new system.

------------------------------------------------------------
The Microcode Disassembler is in the works.
------------------------------------------------------------

I thought it was cool to write it in JavaScript. I will be changing it to java at the end.

<a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">Disassembler</a>

I plan on building a java branch.

------------------------------------------------------------
The new Virtual address mapper.
------------------------------------------------------------

The improved address mapper <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

The virtual pointer in the new file system will be the instruction pointer, for the java version of my Disassembler.

I plan on creating a ARM core disassembler as well.

------------------------------------------------------------
Virtual mapped memory view and hex editor.
------------------------------------------------------------

https://github.com/Recoskie/VHex

The new system will allow you to step through headers as you watch the hex editor highlight bytes that are used while decompiling the program or editing it from the hex editor.

TODO. The hex editor still has to be added to the window during reading headers.
