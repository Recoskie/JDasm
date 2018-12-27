Microsoft-Exe-and-Dll-decoder
=============================

EXE Header Decoder and DLLimport decode and Resource Reader.

------------------------------------------------------------
IMPORTANT.
------------------------------------------------------------

If I decide to continue working on this project.
I am going to do a lot of clean up.
I also have to redesign the MZ header reader because it only supports 32 bit at the moment.

------------------------------------------------------------
The Microcode Disassembler is in the works.
------------------------------------------------------------

I thought it was cool to write it in JavaScript. I will be changing it to java at the end.

<a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">Disassembler</a>

I could see this as being an fun online tool. I plan on building a java branch.

------------------------------------------------------------
The new Virtual address mapper.
------------------------------------------------------------

The original Virtual address mapper is <a href="https://github.com/Recoskie/Java-Exe-and-Dll-decoder/blob/master/EXEDecode/VraReader.java">here</a> is going to be replaced by the improved address mapper <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

The virtual pointer in the new file system will be the instruction pointer for the java version of my Disassembler.

------------------------------------------------------------
Virtual mapped memory view and hex editor.
------------------------------------------------------------

https://github.com/Recoskie/VHex

The new system will allow you to step through headers as you watch the hex editor highlight bytes that are used while decompiling the program or editing it from the hex editor.
