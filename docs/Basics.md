---
layout: default
title: Binary Analysis
image:
  path: https://repository-images.githubusercontent.com/24021024/5e678080-0cfe-11eb-9edf-294da025f0c1
---

<table border="1">
  <tr><td>System Memory: <a href="#SysMem">Link</a></td></tr>
  <tr><td>Processor data types: <a href="#dTypes">Link</a></td></tr>
  <tr><td>Overview of data types: <a href="#Overview">Link</a></td></tr>
  <tr><td>Basic binary files, and headers: <a href="#Hfiles">Link</a></td></tr>
</table>

<h1 id="SysMem">Reading, and editing binary data.</h1>

When you open any file or disk drive, you will see an output that looks like this.

<br />

<image src="Figs\Offset.gif"></image>

<br />

This is called a hex editor. It lets you read what the raw binary data is in files or the entire disk and allows you to even change its binary data.

<br />

However, to use it properly, you need to understand how the information is displayed.

<br />

Each single 0 to 9 and A to F character is four binary digits.

<br />

<table border="1px">
  <tr>
    <td>Hex</td><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>A</td><td>B</td><td>C</td><td>D</td><td>E</td><td>F</td>
  </tr>
  <tr>
    <td>Binary</td><td>0000</td><td>0001</td><td>0010</td><td>0011</td><td>0100</td><td>0101</td><td>0110</td><td>0111</td><td>1000</td><td>1001</td><td>1010</td><td>1011</td><td>1100</td><td>1101</td><td>1110</td><td>1111</td>
  </tr>
</table>

<br />

Hex is used to make the binary view more compact and to keep it readable. It is meant to be a shorthand representation of binary.

<br />  

For example, 19FE is 0001 1001 1111 1110 binary. Also 00611 is 0000 0000 0110 0001 0001.

<br />

Double-clicking any hex digit will let you type a 0 to 9 and A to F value. You can use the arrow keys to navigate the binary. Hitting enter or ESC will exit edit mode.

<br />

There is a square around every two hex digits because every eight binary digits are one position in memory.

<br />

Each hex digit is four binary digits. So every two digits are eight binary digits.

<br />

Eight binary digits are called a byte. All memory devices operate in bytes, CD ROMs, blue rays, DVD, Solid-state drives, RAM, and floppy disks. It is a memory standard.

<br />

<h2>Memory byte position.</h2>

<image src="Figs\fig1.gif"></image>

<br />

The highlighted square above is position 9 in the binary file.

<br />

<image src="Figs\fig2.gif"></image>

<br />

The highlighted square above is position 15 in the binary file.

<br />

<image src="Figs\fig3.gif"></image>

<br />

The highlighted square above is position 16 in the binary file. And so on. Also, any memory device can not have data smaller than a byte (eight binary digits) as it is a memory standard.

<br />

Also, binary files are measured in the number of bytes in the binary file. Also, the size of a memory device is measured by how many bytes it can hold.

<h1>System Memory.</h1>

A standard unit of memory is 8 binary digits, which is called a byte.

<br />

Position zero is byte one. Position one is then byte two from the start of the memory device to the end of the memory device.

<br />

Every 1000 metric size of bytes forum a measurement for how big your disk drive is, floppy disk, RAM Memory, or any digital memory conceivable.

<br />

Also, 1000 is 1 kilo in metric. So 1000 kilo is 1 mega in metric. The word "byte" is added to the metric sizes to forum how many bytes in metric.

<br />

<table style="float:left;margin:10;" border="1px;">
  <tr><td>Metric</td><td>Prefix</td><td>Symbol Multiplier (Traditional Notation)</td><td>Exponential</td><td>Description</td></tr>
  <tr><td>Yotta</td><td>Y</td><td>1,000,000,000,000,000,000,000,000</td><td>10<sup>24</sup></td><td>Septillion</td></tr>
  <tr><td>Zetta</td><td>Z</td><td>1,000,000,000,000,000,000,000</td><td>10<sup>21</sup></td><td>Sextillion</td></tr>
  <tr><td>Exa</td><td>E</td><td>1,000,000,000,000,000,000</td><td>10<sup>18</sup></td><td>Quintillion</td></tr>
  <tr><td>Peta</td><td>P</td><td>1,000,000,000,000,000</td><td>10<sup>15</sup></td><td>Quadrillion</td></tr>
  <tr><td>Tera</td><td>T</td><td>1,000,000,000,000</td><td>10<sup>12</sup></td><td>Trillion</td></tr>
  <tr><td>Giga</td><td>G</td><td>1,000,000,000</td><td>10<sup>9</sup></td><td>Billion</td></tr>
  <tr><td>Mega</td><td>M</td><td>1,000,000</td><td>10<sup>6</sup></td><td>Million</td></tr>
  <tr><td>kilo</td><td>k</td><td>1,000</td><td>10<sup>3</sup></td><td>Thousand</td></tr>
  <tr><td>hecto</td><td>h</td><td>100</td><td>10<sup>2</sup></td><td>Hundred</td></tr>
  <tr><td>deca</td><td>da</td><td>10</td><td>10<sup>1</sup></td><td>Ten</td></tr>
  <tr><td>base</td><td>b</td><td>1</td><td>10<sup>0</sup></td><td>One</td></tr>
  <tr><td>deci</td><td>d</td><td>1/10</td><td>10<sup>-1</sup></td><td>Tenth</td></tr>
  <tr><td>centi</td><td>c</td><td>1/100</td><td>10<sup>-2</sup></td><td>Hundredth</td></tr>
  <tr><td>milli</td><td>m</td><td>1/1,000</td><td>10<sup>-3</sup></td><td>Thousandth</td></tr>
  <tr><td>micro</td><td>µ</td><td>1/1,000,000</td><td>10<sup>-6</sup></td><td>Millionth</td></tr>
  <tr><td>nano</td><td>n</td><td>1/1,000,000,000</td><td>10<sup>-9</sup></td><td>Billionth</td></tr>
  <tr><td>pico</td><td>p</td><td>1/1,000,000,000,000</td><td>10<sup>-12</sup></td><td>Trillionth</td></tr>
  <tr><td>femto</td><td>f</td><td>1/1,000,000,000,000,000</td><td>10<sup>-15</sup></td><td>Quadrillionth</td></tr>
  <tr><td>atto</td><td>a</td><td>1/1,000,000,000,000,000,000</td><td>10<sup>-18</sup></td><td>Quintillionth</td></tr>
  <tr><td>zepto</td><td>z</td><td>1/1,000,000,000,000,000,000,000</td><td>10<sup>-21</sup></td><td>Sextillionth</td></tr>
  <tr><td>yocto</td><td>y</td><td>1/1,000,000,000,000,000,000,000,000</td><td>10<sup>-24</sup></td><td>Septillionth</td></tr>
</table>

<br />

1 k is one kilo meaning 1000 of something. Thus 1 kb means 1000 bytes in metric using prefix notation.

<br />

The same applies to ohms for resistors in electronics. Thus 1 kilo is 1000. Thus an ohm is the unit just as the byte is the singular unit.

<br />

So 1-kilo ohm is a 1000 ohm resistor. The same is true with wattage as kilo wat to Giga wat. In which Giga is the measure and the wat is the thing.

<br />

Every position of memory is in bytes, no matter what you are using to store the bytes. It all works the same (even ram memory).

<br />

However, terms like a petabyte of memory are rarely used. You will, however, hear sizes like this when talking supercomputers.

<h1 id="dTypes" style="clear:left;"><hr />Data types.</h1>

Data types are limited and are the same across all systems. Even different system architecture types use the same data types. They are your building blocks for creating new picture formats, creating Disk drive formats such as FAT32, NTFS, or creating something new.

<br />

The processor must be able to do arithmetic operations with the standard primitive data types to work with file formats.

<br />

Clicking a data type limits the hex editor to edit the bytes that make the data type. Double-clicking a data type will let you enter a value manually.

<br />

<image style="float:left;margin:10px;" src="Figs\fig4.gif" />

<h2>Data Length.</h2>

Processors are designed to read bytes of data, which is the standard unit of memory in all systems. The read byte then can be used with various binary operations.

<br />

Originally two bytes created a "word". Thus two words created a "double word" shortened to "DWORD". Also, two "DWORD" created a "quadword" called "QWORD".

<br />

These are the original names given to bytes. And their lengths. We could Read 2 bytes that form a Word just as two letters make a word in English, which we can add as a 16-bit number.

<br />

Today these words are no longer used. Except for in-machine code translation. As the original meaning is used when reading data by CPU.

<br />

Thus the original saying that a picture is worth a "thousand words". Meaning it took a thousand words, for each red, green, blue value, for each pixel. Back in the day, pictures were 2-kilo big, so a thousand words.

<br />

Today when we read these sizes. Then wish to do arithmetic with different read lengths of data. We specify types like "byte", "short" (WORD), “integer” (DWORD), “long” (QWORD).

<br />

The original names stay intact in disassembly between basic CPU operations, like add, multiply, or divide.

<br />

IT also does not matter if you have an ARM core, X86 core, or embedded core. The primitive types are the same even if the processor runs entirely differently binary-encoded instructions.

<br />

A byte is still a byte of memory no matter what the system architecture is. The lengths of data are also still in word size. Thus integers are still the same.

<br />

The processor must be able to do arithmetic and operations with the standard primitive data types. In order to work with file formats. The standard data types are the building blocks for all format types.

<br />

The only thing that can change between different system processors and architectures is the byte order.

<h2>Little endian, and big endian.</h2>

Let's say we read a DWORD (four bytes), from memory that are 11, 22, 33, 44 in hex.

<br />

In a processor that reads bytes in little-endian byte order: Bytes are read in reverse order = 44, 33, 22, 11.

<br />

In a processor that reads bytes in big-endian byte order: Bytes are read in order = 11, 22, 33, 44.

<br />

Big-endian is not used much, if at all, in many systems. Thus, big-endian systems switch the byte order using arithmetic operations to maintain compatibility with the majority of file formats.

<br />

So basically, little-endian is the format that is used the most today.

<h2>Integer numbers.</h2>

When we talk integers, we are talking numbers without a decimal point. That operate the same as regular numbers without a decimal point.

<br />

We can count to 9 before adding one to the next place value. In which 9+1=10. So the number of times we have counted to 10 is in the 10 position. The number we have counted to 100 is in the 100 position. As 10 can be added to 10 times to reach 100 position, for the number of 100s.

<br />

A max value for 7 digits with 10 per place value 7 across is 10000000-1=9999999. We could easily say 10 to the power of 7 minus 1, which is 10^7-1=9999999.

<br />

In binary, we limit ourselves to 1 and 0. We go 1+1=10, which is the number of twos we have counted.

<br />

Thus if we count to place value 2 a second time, we forum place value 100, which is 4 because each place value can be counted to twice at each position, making each next position a multiple of two instead of ten.

<br />

This means a number 8 in length has 2^8-1=255 combinations, which is called a byte. Also is what one position of memory is. Thus a WORD which is two bytes, has a max value of 2^16-1=65535.

<br />

Using two symbols or ten symbols does not change how a number counts to the next place value or caries adds. The number of symbols we are using before the next place and carry is called a number base.

<br />

You can make any number system you like using any grouping of numbers you like see <a href="https://www.mathsisfun.com/base-conversion-method.html" target="_blank">Base conversion.</a>

<br />

It just is that using two symbols as off and on for a transistor makes it easy to implement in a digital system. See <a href="https://www.cs.nmsu.edu/~hdp/cs273/notes/binary.html" target="_blank">binary, and radix.</a>

<h2>Negative, and positive numbers.</h2>

<table style="float:left;margin:10px;" border="1px;">
  <tr><td>Binary</td><td>Unsinged Decimal</td><td>Singed Decimal</td></tr>
  <tr><td>0000</td><td>0</td><td>0</td></tr>
  <tr><td>0001</td><td>1</td><td>1</td></tr>
  <tr><td>0010</td><td>2</td><td>2</td></tr>
  <tr><td>0011</td><td>3</td><td>3</td></tr>
  <tr><td>0100</td><td>4</td><td>4</td></tr>
  <tr><td>0101</td><td>5</td><td>5</td></tr>
  <tr><td>0110</td><td>6</td><td>6</td></tr>
  <tr><td>0111</td><td>7</td><td>7</td></tr>
  <tr><td>1000</td><td>8</td><td>-8</td></tr>
  <tr><td>1001</td><td>9</td><td>-7</td></tr>
  <tr><td>1010</td><td>10</td><td>-6</td></tr>
  <tr><td>1011</td><td>11</td><td>-5</td></tr>
  <tr><td>1100</td><td>12</td><td>-4</td></tr>
  <tr><td>1101</td><td>13</td><td>-3</td></tr>
  <tr><td>1110</td><td>14</td><td>-2</td></tr>
  <tr><td>1111</td><td>15</td><td>-1</td></tr>
</table>

<br />

Adding Signed and unsigned numbers are the same.

<br />

Adding 3 + 8 = 11

<br />

As one can see, the singed value for 11 is -5. The singed value for 8 is -8. The singed value for 3 is 3.

<br />

Thus 3+-8=-5

<br />

The numbers are split into two. Thus the numbers descend the further you go down using regular add. This way, we do not need to design a unique add circuit for negative and positive numbers.

<br />

Say we add 1111 = -1, and 1001 = 7. We add 15+7=22. Thus 22 in binary is 10110. The first four numbers are 0110, which is the size of the add operation. Thus 0110 in the table is 6 Singed decimal.

<br />

So -1 + 7 = 6. This is because the carry is completely disregarded as it is outside of the number. Allowing us to go from negative to positive using a regular add.

<br />

The only thing that changes is how we display the value and how we compare such a number.

<br />

In reality, your source code can have singed numbers, but by disassembling its machine operations. You could recreate the code as all unsigned numbers without error.

<br />

As there is no such thing as a singed add or subtract in any CPU. Singed numbers are only displayed graphically differently by value.

<br />

See <a href="https://www.swarthmore.edu/NatSci/echeeve1/Ref/BinaryMath/BinaryMath.html" target="_blank">swarthmore.edu Binary arithmetic.</a>

<br />

It is important to understand that there is no magic way of adding or subtracting different number types (Even fractional numbers).

<br />

This is because you will actually see this in code when translating machine code. Thus you have to make the determination of the number type based on how it is used.

<h2 style="clear:left;">Floating point numbers.</h2>

Now, if we talk about a Float, we are talking about a DWORD number with a positional decimal point.

<br />

You lose some binary combinations because the positional number for the exponent takes up space.

<br />

The decimal point can be placed anywhere in your integer number, allowing fractional arithmetic.

<br />

This is also a primitive data type that is the same across all processor cores and mobile devices.

<br />

Thus a double-precision number gets its name from being twice the size of a float number as a QWORD, giving you a larger integer and bigger exponent section.

<br />

These numbers are added the same as regular numbers. With the integer part adjusted by the exponent position. It is A Standard <a href="https://docs.microsoft.com/en-us/cpp/build/ieee-floating-point-representation?view=msvc-160" target="_blank">IEEE type</a>.

<br />

The implementation of said binary number format is the same on mobile as it is on PC as well.

<br />

The exponent is one byte. So you have a 255 positional point for rally big or rally small number values.

<br />

It is split in half for negative and positive exponent. The integer part is 23^2-1.

<br />

Many call the integer part a fraction part. However, it does not reflect how a float works.

<br />

Float numbers use regular ADD in the CPU, which is used, for integers, not floating-point numbers. This is how it is implemented if there is no native float add in the CPU. I will demonstrate how we add float numbers.

<br />

A float number with a value of 0.1000000000000000000000000.

<br />

Is the same as adding 1+1=10 in binary.

<br />

Adding in the decimal point, it becomes 0.1+0.1=1.0 in binary.

<br />

The two numbers are lined up relative to exponent using a shift then added using a regular CPU ADD.

<br />

Which is how floating-point arithmetic is done if the CPU does not have a Float add operation.

<br />

Adding in the decimal point means you can have values that are a division of 2 rather than a multiple of 2.

<br />

As 0.1 is the same as one divided by two = 0.5. Thus adding 0.5 twice is the same as adding 0.1+0.1=1.0 as binary.

<br />

The following <a href="https://andybargh.com/binary-fractions/" target="_blank">Link</a>. Will go in more depth, for you if you like.

<h2>Text data.</h2>

<table style="float:left;margin:10px;" border="1px">
  <tr><td>Binary</td><td>Hex</td><td>Char</td><td>Binary</td><td>Hex</td><td>Char</td><td>Binary</td><td>Hex</td><td>Char</td><td>Binary</td><td>Hex</td><td>Char</td></tr>
  <tr><td>00000000</td><td>00</td><td>NUL</td><td>00100000</td><td>20</td><td>SP</td><td>01000000</td><td>40</td><td>@</td><td>01100000</td><td>60</td><td>`</td></tr>
  <tr><td>00000001</td><td>01</td><td>SOH</td><td>00100001</td><td>21</td><td>!</td><td>01000001</td><td>41</td><td>A</td><td>01100001</td><td>61</td><td>a</td></tr>
  <tr><td>00000010</td><td>02</td><td>STX</td><td>00100010</td><td>22</td><td>“</td><td>01000010</td><td>42</td><td>B</td><td>01100010</td><td>62</td><td>b</td></tr>
  <tr><td>00000011</td><td>03</td><td>ETX</td><td>00100011</td><td>23</td><td>#</td><td>01000011</td><td>43</td><td>C</td><td>01100011</td><td>63</td><td>c</td></tr>
  <tr><td>00000100</td><td>04</td><td>EOT</td><td>00100100</td><td>24</td><td>$</td><td>01000100</td><td>44</td><td>D</td><td>01100100</td><td>64</td><td>d</td></tr>
  <tr><td>00000101</td><td>05</td><td>ENQ</td><td>00100101</td><td>25</td><td>%</td><td>01000101</td><td>45</td><td>E</td><td>01100101</td><td>65</td><td>e</td></tr>
  <tr><td>00000110</td><td>06</td><td>ACK</td><td>00100110</td><td>26</td><td>&</td><td>01000110</td><td>46</td><td>F</td><td>01100110</td><td>66</td><td>f</td></tr>
  <tr><td>00000111</td><td>07</td><td>BEL</td><td>00100111</td><td>27</td><td>‘</td><td>01000111</td><td>47</td><td>G</td><td>01100111</td><td>67</td><td>g</td></tr>
  <tr><td>00001000</td><td>08</td><td>BS</td><td>00101000</td><td>28</td><td>(</td><td>01001000</td><td>48</td><td>H</td><td>01101000</td><td>68</td><td>h</td></tr>
  <tr><td>00001001</td><td>09</td><td>HT</td><td>00101001</td><td>29</td><td>)</td><td>01001001</td><td>49</td><td>I</td><td>01101001</td><td>69</td><td>i</td></tr>
  <tr><td>00001010</td><td>0A</td><td>LF</td><td>00101010</td><td>2A</td><td>*</td><td>01001010</td><td>4A</td><td>J</td><td>01101010</td><td>6A</td><td>j</td></tr>
  <tr><td>00001011</td><td>0B</td><td>VT</td><td>00101011</td><td>2B</td><td>+</td><td>01001011</td><td>4B</td><td>K</td><td>01101011</td><td>6B</td><td>k</td></tr>
  <tr><td>00001100</td><td>0C</td><td>FF</td><td>00101100</td><td>2C</td><td>,</td><td>01001100</td><td>4C</td><td>L</td><td>01101100</td><td>6C</td><td>l</td></tr>
  <tr><td>00001101</td><td>0D</td><td>CR</td><td>00101101</td><td>2D</td><td>-</td><td>01001101</td><td>4D</td><td>M</td><td>01101101</td><td>6D</td><td>m</td></tr>
  <tr><td>00001110</td><td>0E</td><td>SO</td><td>00101110</td><td>2E</td><td>.</td><td>01001110</td><td>4E</td><td>N</td><td>01101110</td><td>6E</td><td>n</td></tr>
  <tr><td>00001111</td><td>0F</td><td>SI</td><td>00101111</td><td>2F</td><td>/</td><td>01001111</td><td>4F</td><td>O</td><td>01101111</td><td>6F</td><td>o</td></tr>
  <tr><td>00010000</td><td>10</td><td>DLE</td><td>00110000</td><td>30</td><td>0</td><td>01010000</td><td>50</td><td>P</td><td>01110000</td><td>70</td><td>p</td></tr>
  <tr><td>00010001</td><td>11</td><td>DC1</td><td>00110001</td><td>31</td><td>1</td><td>01010001</td><td>51</td><td>Q</td><td>01110001</td><td>71</td><td>q</td></tr>
  <tr><td>00010010</td><td>12</td><td>DC2</td><td>00110010</td><td>32</td><td>2</td><td>01010010</td><td>52</td><td>R</td><td>01110010</td><td>72</td><td>r</td></tr>
  <tr><td>00010011</td><td>13</td><td>DC3</td><td>00110011</td><td>33</td><td>3</td><td>01010011</td><td>53</td><td>S</td><td>01110011</td><td>73</td><td>s</td></tr>
  <tr><td>00010100</td><td>14</td><td>DC4</td><td>00110100</td><td>34</td><td>4</td><td>01010100</td><td>54</td><td>T</td><td>01110100</td><td>74</td><td>t</td></tr>
  <tr><td>00010101</td><td>15</td><td>NAK</td><td>00110101</td><td>35</td><td>5</td><td>01010101</td><td>55</td><td>U</td><td>01110101</td><td>75</td><td>u</td></tr>
  <tr><td>00010110</td><td>16</td><td>SYN</td><td>00110110</td><td>36</td><td>6</td><td>01010110</td><td>56</td><td>V</td><td>01110110</td><td>76</td><td>v</td></tr>
  <tr><td>00010111</td><td>17</td><td>ETB</td><td>00110111</td><td>37</td><td>7</td><td>01010111</td><td>57</td><td>W</td><td>01110111</td><td>77</td><td>w</td></tr>
  <tr><td>00011000</td><td>18</td><td>CAN</td><td>00111000</td><td>38</td><td>8</td><td>01011000</td><td>58</td><td>X</td><td>01111000</td><td>78</td><td>x</td></tr>
  <tr><td>00011001</td><td>19</td><td>EM</td><td>00111001</td><td>39</td><td>9</td><td>01011001</td><td>59</td><td>Y</td><td>01111001</td><td>79</td><td>y</td></tr>
  <tr><td>00011010</td><td>1A</td><td>SUB</td><td>00111010</td><td>3A</td><td>:</td><td>01011010</td><td>5A</td><td>Z</td><td>01111010</td><td>7A</td><td>z</td></tr>
  <tr><td>00011011</td><td>1B</td><td>ESC</td><td>00111011</td><td>3B</td><td>;</td><td>01011011</td><td>5B</td><td>[</td><td>01111011</td><td>7B</td><td>{</td></tr>
  <tr><td>00011100</td><td>1C</td><td>FS</td><td>00111100</td><td>3C</td><td>&lt;</td><td>01011100</td><td>5C</td><td>\</td><td>01111100</td><td>7C</td><td>|</td></tr>
  <tr><td>00011101</td><td>1D</td><td>GS</td><td>00111101</td><td>3D</td><td>=</td><td>01011101</td><td>5D</td><td>]</td><td>01111101</td><td>7D</td><td>}</td></tr>
  <tr><td>00011110</td><td>1E</td><td>RS</td><td>00111110</td><td>3E</td><td>&gt;</td><td>01011110</td><td>5E</td><td>^</td><td>01111110</td><td>7E</td><td>~</td></tr>
  <tr><td>00011111</td><td>1F</td><td>US</td><td>00111111</td><td>3F</td><td>?</td><td>01011111</td><td>5F</td><td>_</td><td>01111111</td><td>7F</td><td>DEL</td></tr>
</table>

<br />

A Char is short for charterer. Each key code on your keyboard sends a byte, which corresponds to the standard binary values in the table.

<br />

This format stays the same between systems. Otherwise, documents would fail to load and would end up printing out gibberish.

<br />

Char codes 00 to 1F hex are not really text codes. Thus are never saved to text documents or web pages.

<br />

Processor cores come equipped with text processing functions. Such as changing a binary number to a hex number. A byte is changed into two bytes with values 0 to 9, A to F.

<br />

An array of characters is called a String of text. So you could say a text document is an Array of characters.

<br />

The space bar is 20 hex. Without space as a code, there is no space between words in documents. Also, 30 hex to 39 hex is your 0 to 9 numbers. Smalls and capitals ascend from 40 and 60 hex.

<br />

When dividing a binary number by any base 2 to 36, any remainder that is less than 10 is added to 30 hex, which creates numbers 0 to 9 per place value.

<br />

When remainders are higher than 10 we subtract 10 and add the extra to 60 hex for the alphabet, which creates numbers A to Z per place value.

<br />

This is how the <em>toString</em> method is implemented in all programming languages to convert any number to any number base 2 to 36.

<br />

Text data is also the same across systems, with each byte value representing a different character to draw from a font file.

<br />

When decoding a binary file, you will often run into magic numbers (Signatures).

<br />

When decoded as characters usually mean something like in a Microsoft executable. The first two bytes are 4D 5A = “MZ”.

<br />

In which the two-character string "MZ" is the initials for Mark Zbikowski.

<br />

System protocols in operating systems also accept a string of text in this format. It is also the same across systems.

<br />

There is also Unicode text, also called UTF16 text, which is rarely used. It reads a word instead of a byte. This way, there are 2^16-1=65535 combinations to represent characters.

<br />

Also, UTF16 can end with UTF16-LE, or UTF16-BE, which is little, or big-endian byte order.

<br />

Also you can read over <a href="https://unicode.org/standard/principles.html#:~:text=The%20Unicode%20Standard%20is%20the,International%20Standard%20ISO%2FIEC%2010646." target="_blank">The Unicode Standard: A Technical Introduction</a>.

<br />

Thus UTF8 is just the smaller version of the standard text format. The original UTF8 codes are still the same values as UTF16. The difference is that it has more combinations for more characters after 255.

<h1 style="clear:left;">Fast binary conversion.</h1>

Any number base that is a multiple of 2, such as base 4, base 8 (Octal), base 16 (hexadecimal), base 32, can be directly translated to and from binary.

<br />

<table border="1px">
  <tr>
    <td>Hex</td><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>A</td><td>B</td><td>C</td><td>D</td><td>E</td><td>F</td>
  </tr>
  <tr>
    <td>Binary</td><td>0000</td><td>0001</td><td>0010</td><td>0011</td><td>0100</td><td>0101</td><td>0110</td><td>0111</td><td>1000</td><td>1001</td><td>1010</td><td>1011</td><td>1100</td><td>1101</td><td>1110</td><td>1111</td>
  </tr>
</table>

<br />

A hexadecimal number uses 16 symbols. The last value, F = 15 is exactly equal to the last value of a 4-bit binary number 1111=15.

<br />

In which F is the last hex digit. So F + 1 = 10. In place value. Thus to change the value back to binary, we go.

<br />

10 = 1 (0001), 0 (0000) = 0001 0000

<br />

So 9E hex is.

<br />

9E = 9 (1001), E (1110) = 1001 1110

<br />

To change a really long binary number to hex-like.

<br />

101000101010100101010100101001010

<br />

Splitting any size of binary number in sections of 4 from right to left. Allows us to convert to hex instantly.

<br />

Split=(1),(0100),(0101),(0101),(0010),(1010),(1001),(0100),(1010)

<br />

Hex=(1),(4),(5),(5),(2),(A),(9),(4),(A)

<br />

By matching each 4 binary digit combination in place value to each hex digit in 0 to 15, alows us to quickly change back and forth between binary or hex in one's head.

<br />

Thus hex characters can easily be translated from two-byte characters to a single byte. Writing the real byte value to memory.

<br />

Modern x86 cores have this operation built-in.

<br />

Thus changing back, and forth between binary and hex byte character codes instantly is universal.

<br />

<table border="1px">
  <tr>
    <td>Octal</td><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>
  </tr>
  <tr>
    <td>Binary</td><td>000</td><td>001</td><td>010</td><td>011</td><td>100</td><td>101</td><td>110</td><td>111</td>
  </tr>
</table>

<br />

Octal uses base 8, which is the byte character codes 0 to 7. The last Octal digit is also equal to the last 3 digit binary combination 111 = 7.

<br />

So 7+1=10 in pace value. Thus every octal digit is 3 binary digits. Thus to change the value back to binary, we go.

<br />

10 = 1 (001), 0 (000) = 001 000

<br />

So 53 Octal is.

<br />

53 = 5 (101), 3 (011) = 101 011

<br />

To change a really long binary number to hex-like.

<br />

101000101010100101010100101001010

<br />

Splitting any size of binary number in sections of 3 from right to left. Allows us to convert to octal instantly.

<br />

Split = (101), (000), (101), (010), (100), (101), (010), (100), (101), (001), (010)

<br />

Octal = (5), (0), (5), (2), (4), (5), (2), (4), (5), (1), (2)

<br />

<strong>Base 10 does not match any multiple of 2, base 4, base 8 (Octal), base 16 (hexadecimal), base 32.</strong>

<br />

You can use division by 16 for each hex digit. Because you then are using division to divide the number up in place value in sections just as we are doing above.

<br />

Number bases that are not a multiple of 2 must be divided by number base, and the remainders are each place value.

<br />

In our case, we divide by 10 and add the remainders to character byte codes to forum our string.

<br />

Changing a number back into an integer is just a matter of multiplying the digits by the multiple of the number base.

<br />

So it does not matter the number of digits you are using to display a number. You can translate a number to any groping of digits you like using division and remainder.

<h1>Array.</h1>

An array is a set of bytes or words. Written one after another in memory.

<br />

The first DWord of an array is the boundaries of the array. If the first DWORD is 2 in value, you can only read the first element then the next element before the end.

<br />

All primitive data types can be read one after another linearly in an array, which gives the best performance. However, it can get more complicated.

<br />

Each number in the array can locate to a string. Which a string is a variable in length array built on char UTF8/UTF16 or another array in the case of a 2D array.

<br />

The x86 processor has an address system built into Handel reading elements one after another, plus an index.

<br />

<a href="https://stackoverflow.com/questions/34058101/referencing-the-contents-of-a-memory-location-x86-addressing-modes/34058400#34058400" target="_blank">Scale index base.</a>

<br />

The scale is how much index is multiplied by. A byte is the next address from the base address. So base plus index is the selected byte. While a word is two bytes per array element, so scale is times 2 the array selected index.

<br />

Thus an array of double words meaning two words is 4 bytes. Lastly, an array of qword means two double words put together per array index. Meaning scale takes on the index times 8.

<br />

ARM cores do not have a fancy addressing system built-in. It takes two ARM processor's instructions to run such code. So reading arrayed, and indexed files take longer. Or reading file system array structure. That contains all files on your disk drive.

<br />

Thus programming languages are built on primitives and arrays of primitives types in aligned memory. You will learn more about this in the "Code" document.

<br />

<h1 id="Overview">Overview.</h1>

Programming languages all use the same primitive data types.

<br />

<table border="1px">
  <tr><td>JavaScript</td><td>JavaScript Assumes all numbers are float64 (double). During any bitwise operation changes to int 32, and back to float64.</td></tr>
  <tr>
    <td>Java</td>
    <td>
      Java assumes that all integers are signed, so the regular <em>toString</em> method splits the number into two from the center and adds the sing.

      <br />

      In Java 8, and later a new unsigned <em>toString</em> method was added to convert to a string that shows the integer as is. As an unsigned value.

      <br />

      Adding, and subtracting singed, and unsigned numbers are the same as regular numbers. All that changes is how we display the value as characters.
    </td>
  </tr>
  <tr><td>Languages C, and C++</td><td>Lets you pick how you want a integer to be treated in code. All data types are also the same with better control.</td></tr>
</table>

<br />

The primitive data types are the same as the CPU/ALU is what processes them, not the programming language. Thus the arithmetic units in CPU processors are the same. As positional arithmetic never changes format. Even if you switch processor types, also, text data is standardized.

<br />

Also, memory is in bytes no matter what system you use. Thus data types are still in word size even if you switch processor architecture.

<br />

<strong>Understanding the primitive data types at machine code level and how the language treats them. Can make a big difference in understanding your code.</strong>

<br />

Generally speaking. The primitives are the same across systems. The same is true for code recompiled from other systems in emulators.

<br />

Integer numbers and floating-point numbers are the same as they can not be anything other than positional in 2. A string of text is the same.

<br />

Processors today even have built-in number base conversion to characters and back again to a number. Using the standard character codes.

<br />

Even Arrays are read the same across systems. However, x86 cores are excellent with reading matrices, and arrays, because of the address system.

<br />

<strong>The most we can do is encrypt an entire file. Then decrypt the file to bring it back to the raw standard formats, to have some level of security.</strong>

<br />

Binary files are basic structures built on primitive data types. The only thing you need to know is what each primitive is used for as it is read in the file.

<br />

Some old video games on PlayStation will read a single byte. Then divide it using CPU bitwise arithmetic. This allows the read byte to be split into sections. Such byte values are called packed bytes in binary file documentation.

<br />

It is rare for files to use this unless it is an old file format where memory was a scares thing. Every developer did their best to get a single byte to represent as much as they could.

<br />

Today such things do not matter anymore. Plus, it takes more CPU power to read such bytes and divided them up. Everything in today's binary formats is in bytes, words, dwords, and qwords.

<br />

<h1 id="Hfiles">Binary files, and default hardware data types.</h1>

All file formats have what you call headers, which are bytes that are read in series at the start of the file. They can be lengths byte 1, word 2, dword 4, or qword 8.

<br />

A header usually has a signature which should always be the same byte values. If the bytes do not match, then the file type we are reading is most likely corrupted.

<br />

A header can specify the width, height if it is a picture and various things after the file signature.

<br />

Also, there are still a few different types of data stored in binary files given to external hardware devices that are also standardized.

<br />

Pictures can store pairs of three bytes in Red, Green, Blue per pixel in an array. Thus after index exceeds the width, it moves to the next line of the picture, till picture height.

<br />

As such is how a bit map picture works and is read <a href="https://en.wikipedia.org/wiki/BMP_file_format#:~:text=The%20BMP%20file%20format%2C%20also,and%20OS%2F2%20operating%20systems" target="_blank">Bit Map picture format.</a>

<br />

I use to actually write bit maps pictures one byte at a time in a hex editor. It is the equivalent of doing pixel art. Also is good practice.

<br />

When you click, save, and see your picture load in a picture program. That is the moment it is magical. Considering you just wrote the picture in 1's, and 0's.

<br />

Also, colors of light add together, so it is not hard to visualize the added color in your head. You may want to learn what <a href="https://en.wikipedia.org/wiki/Additive_color" target="_blank">additive colors</a> of light are, and practice making red, green, blue values a little.

<h2>Video memory.</h2>

A bitmap closely resembles video memory, which video memory is how graphics is done without a graphics card. The CPU writes red, green, blue values per pixel to the very last pixel of the display's resolution.

<br />

The operating system does not set up video memory. It is set up by the BIOS (Boot integrated operating system), which is built into all system motherboards. Thus video memory follows the same video modes and standard of a bit map.

<br />

The CPU then can do any graphics function you like on any display or monitor without requiring graphics drivers, which is called software-rendered graphics.

<br />

Graphics cards have methods that can be called that run graphics methods. Such as filling in a rectangle of pixels or calculating 3D angels. This frees up the CPU because the graphics card then does the graphics.

<br />

In actuality, you can build an operating system that only does software-rendered graphics, which runs on all systems, and all motherboard configurations.

<br />

However, it is recommended to add Hardware accelerated graphics through a GPU by implementing basic method calls on the GPU.

<br />

Lastly you can get very creative in software with raw graphics pixel format. You can compare the difference in colors and define edges and shapes and create an artificial intelligence that learns and understands its surroundings, or create video/picture enhancers, and filters. The possibilities are limitless.

<h2>Audio format.</h2>

Audio is also another standard format. It can be a bit confusing at first. It also does not change between systems, just like video memory.

<br />

An audio file can consist of sizes byte 1, word 2, dword 4, qword 8 array.

<br />

An integer that is a dword (32 binary digits) gives a range of control 2^32-1. The value is the point to move the magnet in the speaker. We call this an sample.

<br />

The header defines the speed at each integer is given to the PCM (Pulse-code modulation) device per second. A sample rate of 10 means 10 points per second.

<br />

We would also call this 10 hertz. We use metric to represent larger numbers like 1 kilo hertz would mean 1000 points a second.

<br />

The easiest way to think about audio is that sound is movement (vibration). Thus it is best to describe how it is recorded and played back.

<br />

The values reflect the time of each recorded position the magnet was in a microphone. Allowing us to capture in time the movement and vibration of sound.

<br />

The speed at which each value is recorded is called the sample rate in hertz. Which is how many sample points we are recording in one second.

<br />

The faster the sample rate, then the more precise the audio reproduction is. Also, The bigger of an integer number we use, the more precise each point is for the position of the magnet in the speaker coil.

<br />

Now lets say we wish to output two different audio signals to two different speakers. We specify to the PCM that we are using 2 audio channels.

<br />

Now normally a sample rate of 10 means 10 points per second. However, this time the PCM will read two points at a time. This means we need 20 points, for 10 points per second using 2 audio channels set in the PCM.

<br />

There is no limit on how many audio channels you can have, and how high in quality you can go, but remember that when you generate an audio stream with two channels that every second point is the second audio channel. Also, with three audio channels, then every third point is the third audio channel and so on.

<br />

This is how uncompressed audio works across all systems. Thus generally, this is how audio is given as an audio stream at system level. Similar to how Video memory works.

<br />

In order for audio files to be playable, they must convert to the standard PCM audio. Then we set number of channels and sample rate, and sample integer size in the PCM.

<br />

The wave audio format does not have to convert format as it is wrote in raw PCM audio data format. The wave audio header specifies the points per second and number of bits for each point.

<br />

You can learn more about digital audio and the wave audio format viable <a href="http://www.topherlee.com/software/pcm-tut-wavformat.html">link</a>.

<br />

Lastly you can get very creative in software with raw audio stream format. You can compare the difference in vibration and define sounds and create an artificial intelligence that learns and understands, or create sound enhancers, and filters. The possibilities are limitless.

<h2>Closing.</h2>

Both wave audio files, and bitmaps require very little effort to play/display, or to create/modify. As they are stored in standard hardware format for both audio, or graphics.

<br />

Some of the basic things we can computationally do with the standard hardware formats graphics/audio.

<br />

We can create our own stream samples using basic sin(x) at different speeds of vibration and decay rate calculations to make synthesized sounds.

<br />

We do not have to write the PCM data to an wave audio file to play standard PCM audio. We simply give the integers in RAM memory to the PCM device.

<br />

Any developer that has built a keyboard application, for android, console, or PC should already know what uncompressed audio looks like and how to play it.

<br />

We can also do graphics using sin and cos to do rotation around x, y, z vertices to make 3D models. It does help to store small pictures containing colors to draw between vertices to make textured models.

<br />

Another file that is fun to read byte by byte. Is GIF pictures. <a href="http://giflib.sourceforge.net/whatsinagif/bits_and_bytes.html" target="_blank">W3 GIF picture format.</a>

<br />

GIF pictures store which colors are used in the picture into an array. The array is then indexed by the color data section.

<br />

Thus some picture programs will not let you edit GIF pictures in Red, Green, Blue because each color has to be profiled that you wish to use.

<br />

Unless you change the format to say Bit map, then make changes, then save to GIF. A GIF picture also does not lose any color data and is lossless.