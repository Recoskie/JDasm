Microsoft-Exe-and-Dll-decoder
=============================

EXE Header Decoder and DLLimport decode and Resource Reader.

*IMPORTANT.
If I decide to continue working on this project.
I am going to do a lot of clean up, and also what really sucks is Java is limited to 4 GB IO.
I could split an exe into multiple 4 GB files, but I would have to add this into my VRA address reader.
This is because I would have it split the file into 4GB sections into the temporary folder then have it calculate the address, and position between them.
Then I would have to design a Close routine in my VRA reader to close each IO steam to each file then delete the temporary files.

I also have to redesign the MZ header reader because it only supports 32 bit at the moment.

*The Microcode Disassembler is in the works.

I thought it was cool to write it in JavaScript to show what real cpu binary instructions are. I will be changing it to java at the end.

https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS

I could see this as being an fun online tool.
