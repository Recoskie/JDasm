//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //The variable des stores the data signatures.

  des: [],

  //Signature lengths.

  sig: new dataType("Signature", Descriptor.String8),

  //Section array name.

  sec: new dataType("Section Name", Descriptor.String8),

  //Reserved data Fields.

  r1: new dataType("Reserved", Descriptor.Other),
  r2: new dataType("Reserved", Descriptor.Other),

  //32/64 bit application.

  is64bit: false,

  //The applications base address.

  baseAddress: 0,

  //Begin loading the file format.

  load: function(r)
  {
    //IO stream must be in ready state.

    if(!r) { file.wait(this,"load"); return; }
    
    //Start by loading in all the microsoft file signatures into des array.

    this.des[0] = new Descriptor([
      this.sig, //2 bytes.
      new dataType("Last 512 bytes", Descriptor.LUInt16),
      new dataType("512 bytes in file", Descriptor.LUInt16),
      new dataType("Number of Relocation Entries", Descriptor.LUInt16),
      new dataType("Header size", Descriptor.LUInt16),
      new dataType("Minimum Memory", Descriptor.LUInt16),
      new dataType("Maximum Memory", Descriptor.LUInt16),
      new dataType("Initial SS relative to start of file", Descriptor.LUInt16),
      new dataType("Initial SP", Descriptor.LUInt16),
      new dataType("Checksum (unused)", Descriptor.LUInt16),
      new dataType("Initial IP", Descriptor.LUInt16),
      new dataType("Initial CS relative to start of file", Descriptor.LUInt16),
      new dataType("Relocations Offset", Descriptor.LUInt16),
      new dataType("Overlay Number", Descriptor.LUInt16),
      this.r1, //8 bytes.
      new dataType("ID", Descriptor.LUInt16),
      new dataType("INFO", Descriptor.LUInt16),
      this.r2, //20 bytes.
      new dataType("PE Header Location", Descriptor.LUInt32)
    ]);
    this.des[1] = new Descriptor([this.dosRel = new arrayType("Rel",[
      new dataType("Segment", Descriptor.LUInt16),
      new dataType("Offset", Descriptor.LUInt16)
    ])]);
    this.des[2] = new Descriptor([
      this.sig, //4 bytes.
      new dataType("Machine", Descriptor.LUInt16),
      new dataType("Number Of Sections", Descriptor.LUInt16),
      new dataType("Time Date Stamp", Descriptor.LUInt32),
      new dataType("Pointer To Symbol Table", Descriptor.LUInt32),
      new dataType("Number Of Symbols", Descriptor.LUInt32),
      new dataType("Size Of OP Header", Descriptor.LUInt16),
      new dataType("Characteristics", Descriptor.LUInt16)
    ]);
    this.des[4] = new Descriptor([this.dataDir = new arrayType("Section",[
      new dataType("Virtual offset", Descriptor.LUInt32),
      new dataType("Size", Descriptor.LUInt32)
    ])]);
    this.des[5] = new Descriptor([this.sections = new arrayType("Section Array element",[
      this.sec, //8 bytes.
      new dataType("Section Size Loaded In Ram", Descriptor.LUInt32),
      new dataType("Where to Store Bytes in Ram", Descriptor.LUInt32),
      new dataType("Byte length to read from EXE file", Descriptor.LUInt32),
      new dataType("Position to Start Reading EXE", Descriptor.LUInt32),
      this.r1, //12 bytes.
      new dataType("Section flags", Descriptor.LUInt32)
    ])]);

    //Scan the header information.

    file.onRead(this, "scan"); file.seek(0); file.read(4096);
  },

  //Initialize the programs setup information.

  scan: function()
  {
    //PE header location.

    var msDos = false, pe = file.tempD[0x3C]|(file.tempD[0x3D]<<8)|(file.tempD[0x3E]<<16)|(file.tempD[0x3F]<<24);

    //Check if PE header exists by signature.

    msDos = (file.tempD[pe]|(file.tempD[pe+1]<<8)|(file.tempD[pe+2]<<16)|(file.tempD[pe+3]<<24))!=0x4550;
    
    //The root node is the binary application.
    
    var root = new treeNode(file.name.substring(file.name.lastIndexOf("/")+1,file.name.length),[],true);

    //Header data.
    
    var hData = new treeNode("Header Data",[],msDos); root.add(hData);
    
    //The mz header is already validated on loading the format reader.

    var mzSize=(file.tempD[8]<<4)|(file.tempD[9]<<12), mzHeader = new treeNode("MZ Header",[1,0,mzSize],msDos); hData.add(mzHeader);
    
    //Add the MS-Dos header.
    
    mzHeader.add("DOS 2.0 Header.h", [0]); this.des[0].offset = 0; this.des[0].setEvent(this, "mzHeader");

    //Dos header relocations if any.

    var rel = file.tempD[6] | (file.tempD[7]<<8), relOff = file.tempD[24] | (file.tempD[25]<<8);
    
    if(rel > 0)
    {
      mzHeader.add("DOS Relocations.h", [4]); this.des[1].setEvent(this, "mzRel");

      this.dosRel.length(rel); this.des[1].offset = relOff;
    }

    //Add ms-dos application entry point for disassembly.

    mzHeader.add("Program Start (Machine Code).h", [2,(file.tempD[20]|(file.tempD[21]<<8))+((file.tempD[22]<<4)|(file.tempD[23]<<12))]);

    //If it is an ms dos application the rest of the file is dumped into RAM memory after the Dos header.

    if(msDos){ pe = mzSize; file.addV(mzSize,file.size-mzSize,0,file.size-mzSize); }

    //Else read the new PE header and dump the dos application that exists between the PE header.

    else
    {
      //We skip the dos header and dump what remains before the PE header.

      file.addV(mzSize,pe-mzSize,0,pe-mzSize);

      //Number of mappable sections.

      var sections = file.tempD[pe+6]|(file.tempD[pe+7]<<8);

      //Add the PE header.

      hData.add("PE Header.h",[8]); this.des[2].offset = pe; this.des[2].setEvent(this, "peHeader"); pe += 24;

      //The OP header has two types that are nearly identical for 32-bit or 64-bit binaries.
      //This is why the OP header is not predefined under this.des[3] like the others.

      hData.add("OP Header.h",[12]);

      var op = file.tempD[pe]|(file.tempD[pe+1]<<8); if(op == 267 || (this.is64bit = op == 523))
      {
        var type = this.is64bit ? Descriptor.LUInt64 : Descriptor.LUInt32, desOp = [
          this.sig, //2 bytes.
          new dataType("Major Linker Version", Descriptor.UInt8),
          new dataType("Minor Linker Version", Descriptor.UInt8),
          new dataType("Size Of Code", Descriptor.LUInt32),
          new dataType("Size Of Initialized Data", Descriptor.LUInt32),
          new dataType("Size Of Uninitialized Data", Descriptor.LUInt32),
          new dataType("Start Of Code.", Descriptor.LUInt32),
          new dataType("Base Of Code", Descriptor.LUInt32),
          new dataType("Base Of Data", Descriptor.LUInt32), //Remove if 64 bit.
          new dataType("Base Address", type),
          new dataType("Section Alignment", Descriptor.LUInt32),
          new dataType("File Alignment", Descriptor.LUInt32),
          new dataType("Major Operating System Version", Descriptor.LUInt16),
          new dataType("Minor Operating System Version", Descriptor.LUInt16),
          new dataType("Major binary Version", Descriptor.LUInt16),
          new dataType("Minor binary Version", Descriptor.LUInt16),
          new dataType("Major Sub system Version", Descriptor.LUInt16),
          new dataType("Minor Sub system Version", Descriptor.LUInt16),
          new dataType("Win 32 Version Value", Descriptor.LUInt32),
          new dataType("Size Of binary", Descriptor.LUInt32),
          new dataType("Size Of Headers", Descriptor.LUInt32),
          new dataType("Check Sum", Descriptor.LUInt32),
          new dataType("Sub system", Descriptor.LUInt16),
          new dataType("Dll Characteristics", Descriptor.LUInt16),
          new dataType("Size Of Stack Reserve", type),
          new dataType("Size Of Stack Commit", type),
          new dataType("Size Of Heap Reserve", type),
          new dataType("Size Of Heap Commit", type),
          new dataType("Loader Flags", Descriptor.LUInt32),
          new dataType("Data Directory Array Size", Descriptor.LUInt32),
        ]; if(this.is64bit){ desOp.splice(8,1); }

        if(this.is64bit) { this.baseAddress = (file.data[pe+24] | (file.data[pe+25] << 8) | (file.data[pe+26] << 16)) + (file.data[pe+27] << 24) + ((file.data[pe+28] * (2**32)) + (file.data[pe+29] * (2**40)) + (file.data[pe+30] * (2**48)) + (file.data[pe+31] * (2**56))); }
        else { this.baseAddress = (file.data[pe+28] | (file.data[pe+29] << 8) | (file.data[pe+30] << 16)) + (file.data[pe+31] << 24); }

        //Add the applications entry point.

        root.add("Program Start (Machine Code).h", [3,this.baseAddress + ((file.data[pe+16] | (file.data[pe+17] << 8) | (file.data[pe+18] << 16)) + (file.data[pe+19] << 24))]);
        
        //Create op header.

        this.des[3] = new Descriptor(desOp); this.des[3].offset = pe; this.des[3].setEvent(this, "opHeader"); pe += this.is64bit ? 112 : 96;

        //Data directory array.

        var ddrSize = file.tempD[pe-4]|(file.tempD[pe-3]<<8)|(file.tempD[pe-2]<<16)|(file.tempD[pe-1]<<24);

        hData.add("Data Directory Array.h",[16]); this.dataDir.length(ddrSize); this.des[4].offset = pe; this.des[4].setEvent(this, "dirArray");
        
        //Scan the data directory array.

        var types = ["function Export Table.h", "DLL Import Table.h", "Resource Files.h", "Exception Table.h", "Security Level Settings.h",
        "Relocations.h", "DEBUG TABLE.h", "Description/Architecture.h", "Machine Value.h", "Thread Storage Location.h", "Load System Configuration.h",
        "Import Table of Functions inside program.h", "Import Address Setup Table.h", "Delayed Import Table.h", "COM Runtime Descriptor.h"];

        for(var e = pe + (ddrSize << 3), i = 0, size = 0, loc = 0; pe < e; pe += 8, i++)
        {
          loc = file.tempD[pe]|(file.tempD[pe+1]<<8)|(file.tempD[pe+2]<<16)|(file.tempD[pe+3]<<24);
          size = file.tempD[pe+4]|(file.tempD[pe+5]<<8)|(file.tempD[pe+6]<<16)|(file.tempD[pe+7]<<24);
          if( size > 0 ) { root.add(types[i],[-(i+1),loc + this.baseAddress,size]); }
        }

        types = undefined;

        //Mapped sections to virtual address space.

        hData.add("Mapped SECTIONS TO RAM.h",[20]); this.sec.length(8); this.sections.length(sections); this.des[5].offset = pe; this.des[5].setEvent(this, "secArray");
        
        for(var e = pe + (sections * 40), vSize = 0, vOff = 0, size = 0, off = 0, i = 0; pe < e; pe += 40, i++)
        {
          vSize = file.tempD[pe+8]|(file.tempD[pe+9]<<8)|(file.tempD[pe+10]<<16)|(file.tempD[pe+11]<<24);
          vOff = file.tempD[pe+12]|(file.tempD[pe+13]<<8)|(file.tempD[pe+14]<<16)|(file.tempD[pe+15]<<24);
          size = file.tempD[pe+16]|(file.tempD[pe+17]<<8)|(file.tempD[pe+18]<<16)|(file.tempD[pe+19]<<24);
          off = file.tempD[pe+20]|(file.tempD[pe+21]<<8)|(file.tempD[pe+22]<<16)|(file.tempD[pe+23]<<24);

          file.addV( off, size, vOff + this.baseAddress, vSize );
        }
      }
      else { this.des[3] = new Descriptor([]); this.des[3].offset = pe; this.des[3].setEvent(this, "badSig"); }
    }

    //Set the computed size of all the headers.

    hData.setArgs([1,0,pe]);

    //Reset data model.

    dModel.clear();

    //Show virtual address space.

    if( !virtual.visible ) { showH(true); }

    //Set tree node event Handler.
    
    Tree.set(root); tree.prototype.event = this.open;

    //Start at the header section for PE, or at DOS header for ms-dos.
    
    tree.prototype.treeClick( (!msDos ? Tree.getNode(0).getNode(0) : Tree.getNode(0).getNode(0).getNode(0).getNode(0)).parentElement );
  },

  //Tree event handling.

  open: function(e)
  {
    e = e.getArgs(); if(e[0] == ""){ return; }

    var cmd = parseInt(e[0]);

    //Check if negative value which are used to load in sections.

    if(cmd < 0)
    {
      dModel.clear(); file.seekV(parseFloat(e[1])); ds.setType(15, 0, parseInt(e[2]), true); info.innerHTML = "No reader, for this section yet."; return;
    }

    //Check if the argument is a command such as start disassembling code, or select bytes.

    des = cmd>>2; cmd &= 3; if(cmd >= 1)
    {
      //CMD 1 is select bytes.

      if(cmd == 1) { dModel.clear(); file.seek(parseInt(e[1])); ds.setType(15, 0, parseInt(e[2]), false); info.innerHTML = format.msg[des]; }

      //Begin disassembling ms-dos app.

      else if(cmd == 2) { dModel.clear(); file.seekV(parseInt(e[1])); ds.setType(15, 0, 1); info.innerHTML = "Not implemented yet."; }

      //Begin disassembling microsoft app.

      else if(cmd == 3) { dModel.clear(); file.seekV(parseInt(e[1])); ds.setType(15, 0, 1); info.innerHTML = "Not implemented yet."; }
    }

    //Else it is a data model node.

    else
    {
      if(e.length > 1){ format.des[des].offset = parseInt(e[1]); } dModel.setDescriptor(format.des[des]);
    }
  },

  //Message output for byte selection command.

  msg: [""],

  //MZ header information.

  mzHeader: function(i)
  {
    if( i < 0 ) { this.sig.length(2); this.r1.length(8); this.r2.length(20); }

    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  },

  //MS-dos relocations information.

  mzRel: function(i)
  {
    if( i < 0 ) { this.sig.length(2); this.r1.length(8); this.r2.length(20); }
  
    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  },

  //PE header information.

  peHeader: function(i)
  {
    if( i < 0 ) { this.sig.length(4); }
  
    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  },

  //OP header information.

  opHeader: function(i)
  {
    if( i < 0 ) { this.sig.length(2); }
  
    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  },

  //Data directory array information.

  dirArray: function(i)
  {
    if( i < 0 ) { }
  
    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  },

  //Section array information.

  secArray: function(i)
  {
    if( i < 0 ) { this.r1.length(12); }
  
    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  },

  //Used to identify bad file signatures in the case of a corrupted application.

  badSig: function(i)
  { 
    info.innerHTML = "A bad signature has been encountered, so the application is corrupted!";
  }
}