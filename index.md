---
layout: default
image:
  path: https://repository-images.githubusercontent.com/24021024/5e678080-0cfe-11eb-9edf-294da025f0c1
---

## Help, and documentation.

<table border="1">
  <tr><td>Basics: <a href="https://recoskie.github.io/J-Disassembly/docs/Basics.html">Link</a></td></tr>
  <tr><td>Machine code: <a href="https://recoskie.github.io/J-Disassembly/docs/Machine.html">Link</a></td></tr>
  <tr><td>High Level Code: <a href="https://recoskie.github.io/J-Disassembly/docs/Code.html">Link</a></td></tr>
</table>

## What is J-Disassembly.

<table style="width:50%;">
  <tr>
    <td>
      <a href="https://recoskie.github.io/J-Disassembly/docs/Figs/pre1.gif" target="_blank"><img src="https://recoskie.github.io/J-Disassembly/docs/Figs/pre1.gif"></a>
    </td>
    <td>
      <a href="https://recoskie.github.io/J-Disassembly/docs/Figs/pre2.gif" target="_blank"><img src="https://recoskie.github.io/J-Disassembly/docs/Figs/pre2.gif"></a>
    </td>
  </tr>
</table>

J-Disassembly is designed to be user friendly. For both professional, and absolute beginners.

This tool makes binary files, and formats visual, and also easy to understand, and modify.

Is designed, for anyone new learning how binary files are read, or professional use.

The tools are explained in help, and documentation.

J-Disassembly can visually take binary software apart to create source code. No matter what operating system you are on, or CPU.

J-Disassembly also visualizes binary data, and file types. Showing you what every binary digit read is used for.

This tool is also a hex editor, and data forensics tool, for binary files that is fun, visual, easy to use, and understand.

You can also read disk drives sector by sector (one binary digit at a time from start, to end).

# Supported formats.

This application currently supports Microsoft binaries formats: .exe, .dll, .sys, .drv, .ocx.

This project is meant to support many different binary file formats, and application formats.

However, only supports Microsoft binary format at the moment. If you wish to contribute a Format. Then put it under the format folder.

# Running the application.

Download as zip. The file <strong>JD-asm.jar</strong> is the application.

You will need Java 8, or later installed (latest version is preferred).

# IO system.

RandomAccessFileV Is an IO system that can map virtual addresses to byte positions in a file, or disk.

It also has a new IO event system. Which the swing IO components operate on.

The IO system can be found <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

Without it. You do not have a nice virtually mapped binary application in it's proper address space.

Also without it. You would not be able to accurately make changes to sections of a program, or read it.

# Window GUI components.

swing IO is a new set of swing components that respond to IO events.

Such as read, or write, and seek to position in memory.

Without these components. We would not be able to structure binary information visually.

We also would not have a interactive hex editor that updates when you click on a type of data in a data descriptor.

The swing IO components can be found <a href="https://github.com/Recoskie/swingIO">here</a>.

# The Disassembler.

It was cool to write it in JavaScript. The project can be found <a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">here</a>.

The disassembler is rebuilt in Java, for this project.

I plan on creating a ARM core disassembler as well, for mobile devices.

# Planed tools.

[ ] Boot sector decompilation.

[ ] File system array structure reader (NTFS, FAT32, EXT)
