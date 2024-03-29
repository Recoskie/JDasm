---
layout: default
image:
  path: https://repository-images.githubusercontent.com/24021024/5e678080-0cfe-11eb-9edf-294da025f0c1
---

## Help and documentation.

<table border="1">
  <tr><td>Basics: <a href="https://recoskie.github.io/JDisassembly/docs/Basics.html">Link</a></td></tr>
  <tr><td>Machine code: <a href="https://recoskie.github.io/JDisassembly/docs/Machine.html">Link</a></td></tr>
  <tr><td>High Level Code: <a href="https://recoskie.github.io/JDisassembly/docs/Code.html">Link</a></td></tr>
</table>

## What is JDisassembly.

<table style="width:50%;">
  <tr>
    <td>
      <a href="https://recoskie.github.io/JDisassembly/docs/Figs/pre1.gif" target="_blank"><img src="https://recoskie.github.io/JDisassembly/docs/Figs/pre1.gif"></a>
    </td>
    <td>
      <a href="https://recoskie.github.io/JDisassembly/docs/Figs/pre2.gif" target="_blank"><img src="https://recoskie.github.io/JDisassembly/docs/Figs/pre2.gif"></a>
    </td>
  </tr>
</table>

JDisassembly makes binary files and machine code visual and also, easy to understand and modify in raw binary form.

It is designed for anyone as an easy way to learn how binary files are read. In which a processor processes standard binary types.

We generally define these binary types as variables when writing software or doing any Arithmetic.

Does not matter what CPU you use as all arithmetic types are the same across all cores. Otherwise, we would have a hard time processing file formats.

The tools are explained in help and documentation. Plus, both audio and graphics use a standard format as well. Which is nice to learn if you are into writing an operating system. All of this is put in detail, as well as machine code and compilation of software.

JDisassembly can visually take binary software apart to allow you to create source code.

JDisassembly also visualizes binary data and file types. Showing you what every binary digit read is actually used for.

This tool is also a hex editor and data forensics tool for binary files that is fun, visual, easy to use, and understand.

You can also read disk drives sector by sector (one binary digit at a time from start to end).

The application is designed, for anyone, or for professional use.

# Supported formats.

This application currently supports Microsoft binaries formats: .exe, .dll, .sys, .drv, .ocx, .com, DOS, MS-DOS.

Supports Unix/Linux binary formats: .axf, .bin, .elf, .o, .prx, .puff, .ko, .mod, .so

Supports .efi boot binaries and boot sector disassembly.

## Picture formats.

Supports analyzing and editing BIT Map pictures.

Supports analyzing and editing JPEG pictures.

Supports analyzing and editing WebP pictures.

Supports analyzing and editing CDR pictures.

Supports analyzing and editing ANI (Animated cursors).

Supports analyzing and editing PAL (Color Palette).

## Audio formats.

Supports analyzing and editing wave Audio files.

Supports analyzing and editing RMI Audio files.

Supports analyzing and editing DLS Audio files.

Supports analyzing and editing XMA Audio files.

## Video formats.

Supports analyzing and editing AVI video files.

## Contributing a file format.

If you wish to contribute a Format. Then put it under the format folder.

The following link to the <a href="https://gist.github.com/Recoskie/1c75264cb072aaf41e871ffd2a1f7370">template</a> will allow you to add new formats to JDisassembly quickly.

# x86 Boot sectors.

A boot sector is the first 512 bytes of a disk that contains machine instructions for a blank computer to start running at address 0.

Boot sectors run across the line of all AMD, and Intel processors, because the machine code x86 cores run does not change between newer or older systems.

A Windows or Linux installation disk also has a boot sector at address 0 that begins writing the OS to a blank disk that you choose.

Thus it writes a boot sector to the disk you want to put the operating system on. However, that boot sector begins the operating system.

You can analyze boot sectors and bootable media that run on blank computers with JDisassembly.

# Running the application.

Download as zip. The file <strong>JD-asm.jar</strong> is the application.

You will need Java 8 or later installed (the latest version is preferred).

# IO system.

RandomAccessFileV Is an IO system that can map virtual addresses to byte positions in a file or disk.

It also has a new IO event system, which the swing IO components operate on.

The IO system can be found <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

Without it. You do not have a nice virtually mapped binary application in its proper address space.

Also, without it. You would not be able to accurately make changes to sections of a program or read it.

# Window GUI components.

Swing IO is a new set of swing components that respond to IO events.

Such as read, or write, and seek to position in memory.

Without these components. We would not be able to structure binary information visually.

We also would not have an interactive hex editor that updates when you click on a type of data in a data descriptor.

The swing IO components can be found <a href="https://github.com/Recoskie/swingIO">here</a>.

# The Disassembler.

It was cool to write it in JavaScript. The project can be found <a href="https://github.com/Recoskie/X86-64-CPU-Binary-Code-Disassembler-JS">here</a>.

The disassembler is rebuilt in Java for this project.

I plan on creating an ARM core disassembler as well for mobile devices.

# Planed tools.

- [ ] Mach-o, for MacOSX reader.

- [ ] ARM core, for mobile systems.

- [ ] ARM boot sector decompiler.

- [ ] File system array structure reader (NTFS, FAT32, EXT).
