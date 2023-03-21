JDasm.
=============================

<table>
  <tr>
    <td>
      <a href="https://recoskie.github.io/JDasm/docs/Figs/pre1.gif" target="_blank"><img src="https://recoskie.github.io/JDasm/docs/Figs/pre1.gif" style="width:50%;"></a>
    </td>
    <td>
      <a href="https://recoskie.github.io/JDasm/docs/Figs/pre2.gif" target="_blank"><img src="https://recoskie.github.io/JDasm/docs/Figs/pre2.gif" style="width:50%;"></a>
    </td>
  </tr>
</table>

JDasm makes binary files and machine code visual and easy to understand.

Everything is detailed as you navigate through a binary application, from loading to analyzing the program instructions and what it does.

You can follow the program and what it does and even recreate code for programs which you lost the source code for.

It also will let you see how different binary formats are read.

You can navigate to any part of a binary file that interests you and analyze it, or you can go from the beginning all the way through it.

This application lets you analyze files and find hidden data embedded in files.

JDasm is great for anyone concerned about security, such as knowing if a program has hidden code.

JDasm is also meant for educational purposes for anyone who wants to understand how software and hardware work together or to create a basic compiler or operating system.

The application is designed, for anyone, or for professional use.

The tools are explained in help and documentation; see the applications <a href="https://github.com/Recoskie/JDasm/wiki">wiki</a> for details.

------------------------------------------------------------
Application versions.
------------------------------------------------------------

<table>
  <tr><td>Cross platform java application version:</td><td><a href="https://github.com/Recoskie/JDasm/raw/master/JD-asm.jar">Link</a></td></tr>
  <tr><td>iPhone beta version:</td><td><a href="https://testflight.apple.com/join/HL7YrtzH">Link</a></td></tr>
  <tr><td>Web beta version:</td><td><a href="https://recoskie.github.io/JDasm/">Link</a></td></tr>
</table>

The web and mobile versions are in beta and will be the same as the java desktop application, except you will not be able to modify binary data in the hex editor by double-clicking cells (confirming changes) and will not be able to read memory devices from start to end directly.

If you are recovering data on memory devices or want to change a binary file or application, use the java version.

------------------------------------------------------------
Supported formats.
------------------------------------------------------------

This application currently supports microsoft binaries formats: .exe, .dll, .sys, .drv, .ocx, .com, DOS, MS-DOS.

Supports Unix/Linux (ELF) binary formats: .axf, .bin, .elf, .o, .prx, .puff, .ko, .mod, .so

Supports macOS/iPhone (Mach-O) binaries, and also macOS/iPhone (Mach-O) Universal binaries.

Supports decompiling mobile android APK application binaries.

Supports decompiling mobile iPhone IPA application binaries.

iPhone uses macOS (Mach-O) format, and android uses ELF for libraries in the APK.

Android and iPhone store the applications in zip files. You need to find the main application file to decompile it.

Supports .efi boot firmware binaries and boot sector disassembly. You are able to decompile the boot process of firmware files in any os.

The web and mobile versions can not read disk drives directly, so decompiling the operating system boot sectors is only available in the java version. Reading .efi and decompiling firmware files can be done on the web and mobile versions.

## Compression.

Supports analyzing ZIP files.

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

------------------------------------------------------------
x86 Boot sectors.
------------------------------------------------------------

A boot sector is the first 512 bytes of a disk that contains machine instructions for a blank computer to start running at address 0.

Boot sectors run across the line of all AMD, and Intel processors, because the machine code x86 cores run does not change between newer or older systems.

A Windows or Linux installation disk also has a boot sector at address 0 that begins writing the OS to a blank disk that you choose.

Thus it writes a boot sector to the disk you want to put the operating system on. However, that boot sector begins the operating system.

You can analyze boot sectors and bootable media that run on blank computers with JDasm.

------------------------------------------------------------
IO system.
------------------------------------------------------------

RandomAccessFileV Is an IO system that can map virtual addresses to byte positions in a file.

It also has a new IO event system, which the swing IO components operate on.

The IO system can be found <a href="https://github.com/Recoskie/RandomAccessFileV">here</a>.

Without it. You do not have a nice virtually mapped binary application in its proper address space.

Also, without it. You would not be able to accurately make changes to sections of a program or read it.

<a href="https://github.com/Recoskie/RandomAccessFileV/blob/master/RandomAccessDevice.java">RandomAccessDevice</a> extends the functions of RandomAccessFileV, giving it the ability to read any memory device in raw binary format.

------------------------------------------------------------
Window GUI components.
------------------------------------------------------------

Swing IO is a new set of swing components that respond to IO events.

Such as read, or write, and seek to position in memory.

Without these components. We would not be able to structure binary information visually.

We also would not have an interactive hex editor that updates when you click on a type of data in a data descriptor.

The swing IO components can be found <a href="https://github.com/Recoskie/swingIO">here</a>.

------------------------------------------------------------
The Disassembler.
------------------------------------------------------------

The disassembler was originally designed as an x86 disassembly library for javascript only.

The project can be found <a href="https://github.com/Recoskie/core">here</a>.

It was rebuilt in java and existed in this project separately.

The javascript project gained a new branch for the java version from this project and then was added to this project as a submodule.

The disassembler library was renamed from "x86-64-disassembler" to the "core" submodule and added to the project for both web use and java application use.

The project was restructured to have separate folders for each core and to support more cores than just x86 ones.

I plan on creating an ARM core disassembler as well for mobile devices.

------------------------------------------------------------
Planed tools.
------------------------------------------------------------

- [ ] ARM core, for mobile systems.

- [ ] ARM boot sector decompiler.

- [ ] File system array structure reader (NTFS, FAT32, EXT).
