//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //The variable des stores the data signatures.

  des: [],

  //Signature lengths.

  sig: new dataType("Signature", Descriptor.String8),

  //Section array name.

  sec: new dataType("Section Name", Descriptor.String8),

  //DLL, or driver name.

  dllName: new dataType("DLL Name", Descriptor.String8),

  //DLL, or driver function name.

  funcName: new dataType("Method name", Descriptor.String8),

  //Reserved data Fields.

  r1: new dataType("Reserved", Descriptor.Other),
  r2: new dataType("Reserved", Descriptor.Other),

  //32/64 bit application.

  is64bit: false,

  //The processor core instruction type that the code is intended to run on.

  coreType: 0,

  //The applications base address.

  baseAddress: 0,

  //Disassembly virtual address.

  disV: 0,

  //IO stream must be in ready state before we can Initialize the applications setup information.

  load: function(r) { if(!r) { file.wait(this,"load"); return; } file.onRead(this, "scan"); file.seek(0); file.read(4096); },

  /*-------------------------------------------------------------------------------------------------------------------------
  Initialize the programs setup information.
  -------------------------------------------------------------------------------------------------------------------------*/

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
    
    //The dos 2.0 header structure.

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
    var mzSize=(file.tempD[8]<<4)|(file.tempD[9]<<12);

    //Add the dos header information node.

    var mzHeader = new treeNode("MZ Header",[1,0,mzSize],msDos); hData.add(mzHeader);
    
    //Add the MS-Dos header.
    
    mzHeader.add("DOS 2.0 Header.h", [0]); this.des[0].offset = 0; this.des[0].setEvent(this, "mzHeader");

    //Dos header relocations if any.

    var rel = file.tempD[6] | (file.tempD[7]<<8), relOff = file.tempD[24] | (file.tempD[25]<<8); if(rel > 0)
    {
      //Dos 2.0 relocation structure.

      this.des[1] = new Descriptor([this.dosRel = new arrayType("Rel",[
        new dataType("Segment", Descriptor.LUInt16),
        new dataType("Offset", Descriptor.LUInt16)
      ])]);

      //Add dos relocation node to dos header information.

      mzHeader.add("DOS Relocations.h", [4]); this.des[1].setEvent(this, "mzRel"); this.dosRel.length(rel); this.des[1].offset = relOff;
    }

    //Add ms-dos application entry point for disassembly.

    mzHeader.add("Program Start (Machine Code).h", [2,(file.tempD[20]|(file.tempD[21]<<8))+((file.tempD[22]<<4)|(file.tempD[23]<<12))]);

    //If it is an ms dos application the rest of the file is mapped into RAM memory after the Dos header.

    if(msDos){ pe = mzSize; file.addV(mzSize,file.size-mzSize,0,file.size-mzSize); }

    //Else read the new PE header and dump the dos application that exists between the PE header.

    else
    {
      //We skip the dos header and map what remains before the PE header.

      file.addV(mzSize,pe-mzSize,0,pe-mzSize);

      //The PE header structure.

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

      //Add the PE header node.

      hData.add("PE Header.h",[8]); this.des[2].offset = pe; this.des[2].setEvent(this, "peHeader");

      //Core type and mappable sections to ram.

      var sections = file.tempD[pe+6]|(file.tempD[pe+7]<<8); this.coreType = file.tempD[pe+4]|(file.tempD[pe+5]<<8); pe += 24;

      //The OP header has two types that are nearly identical for 32-bit or 64-bit binaries.

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
        
        //Add the OP header node.

        hData.add("OP Header.h",[12]); this.des[3] = new Descriptor(desOp); this.des[3].offset = pe; this.des[3].setEvent(this, "opHeader");

        //Add the applications entry point.

        if(this.is64bit) { this.baseAddress = (file.data[pe+24] | (file.data[pe+25] << 8) | (file.data[pe+26] << 16)) + (file.data[pe+27] << 24) + ((file.data[pe+28] * (2**32)) + (file.data[pe+29] * (2**40)) + (file.data[pe+30] * (2**48)) + (file.data[pe+31] * (2**56))); }
        else { this.baseAddress = (file.data[pe+28] | (file.data[pe+29] << 8) | (file.data[pe+30] << 16)) + (file.data[pe+31] << 24); }

        root.add("Program Start (Machine Code).h", [3,this.baseAddress + ((file.data[pe+16] | (file.data[pe+17] << 8) | (file.data[pe+18] << 16)) + (file.data[pe+19] << 24))]);

        //The data directory array size is the last 4 bytes of the OP header.

        pe += this.is64bit ? 112 : 96; var ddrSize = file.tempD[pe-4]|(file.tempD[pe-3]<<8)|(file.tempD[pe-2]<<16)|(file.tempD[pe-1]<<24);

        //Data directory array structure.

        this.des[4] = new Descriptor([this.dataDir = new arrayType("Section",[
          new dataType("Virtual offset", Descriptor.LUInt32),
          new dataType("Size", Descriptor.LUInt32)
        ])]);

        //Add the data directory array node.

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
        
        types = undefined; this.readSec =
        [
          this.noReader,this.readDLL,this.noReader,this.noReader,this.noReader,this.noReader,this.noReader,this.noReader,
          this.noReader,this.noReader,this.noReader,this.noReader,this.noReader,this.noReader,this.noReader,this.noReader
        ];

        //Application section map to virtual address space structure.

        this.des[5] = new Descriptor([this.sections = new arrayType("Section Array element",[
          this.sec, //8 bytes.
          new dataType("Section Size Loaded In Ram", Descriptor.LUInt32),
          new dataType("Where to Store Bytes in Ram", Descriptor.LUInt32),
          new dataType("Byte length to read from EXE file", Descriptor.LUInt32),
          new dataType("Position to Start Reading EXE", Descriptor.LUInt32),
          this.r1, //12 bytes.
          new dataType("Section flags", Descriptor.LUInt32)
        ])]);

        //Add the section array node.

        hData.add("Mapped SECTIONS TO RAM.h",[20]); this.sec.length(8); this.sections.length(sections); this.des[5].offset = pe; this.des[5].setEvent(this, "secArray");

        //Read and map the applications sections into virtual address space.
        
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

  /*-------------------------------------------------------------------------------------------------------------------------
  Scan DLL import table.
  -------------------------------------------------------------------------------------------------------------------------*/

  tempNode: null, readDLL: function(vPos, size)
  {
    //DLL import table array.

    format.vPos = vPos; format.des[6] = new Descriptor([format.dArray = new arrayType("Array Element ",[
      new dataType("DLL Array Functions Location 1", Descriptor.LUInt32),
      new dataType("Time Date Stamp", Descriptor.LUInt32),
      new dataType("Forward Chain", Descriptor.LUInt32),
      new dataType("DLL Name Location", Descriptor.LUInt32),
      new dataType("DLL Array Functions Location 2", Descriptor.LUInt32),
    ])]); format.des[6].offset = vPos;

    //DLL Name.

    format.des[7] = new Descriptor([format.dllName]);

    //DLL function array.

    format.des[8] = new Descriptor([format.funcArray = new arrayType("Array Element ",[
      new dataType("Import Name Location, or Index", format.is64bit ? Descriptor.LUInt64 : Descriptor.LUInt32),
    ])]);

    //DLL method name.

    format.des[9] = new Descriptor([format.funcName]);

    //Data is in virtual address space.

    format.des[6].virtual = format.des[7].virtual = format.des[8].virtual = format.des[9].virtual = true;

    //Temporary data statures used for loading the dll import data.

    format.dllEl = function(n,f1,f2){this.nLoc=n;this.name="";this.f1=f1;this.f2=f2;this.fn1=[""];this.fn2=[""];};
    format.dllArray = [new format.dllEl(0,0,0)]; format.curEl = 0; format.curFn = 0;

    //Begin reading dll array.

    format.callBack=format.scanDLL; file.onRead(format, "scanDLL"); file.seekV(vPos); file.readV(size+20);
  },

  //Scan the dll array. 

  scanDLL: function(name)
  {
    //When Dll array end is reached we load the function lists and names.

    if(format.dllArray[0].name == "f1")
    {
      //Load in function names.

      if(format.dllArray[format.curEl].fn1[0] == "end")
      {

      }

      //Else load the function array.

      else
      {

      }

      //Scan complete up until here.

      format.dllScanDone(); return;
    }
    else if(format.dllArray[0].name == "f2")
    {
      //Load in function names.

      if(format.dllArray[format.curEl].fn1[0] == "end")
      {

      }

      //Else load the function array.

      else
      {
        
      }
    }

    //When Dll array is loaded we can locate the dll names.

    else if(format.dllArray[0].name == "end")
    {
      //Set dll name and increment dll name scan.

      if(name) { format.dllArray[format.curEl++].name=name; }

      //Scan next dll name.

      if(format.curEl<format.dllArray.length)
      {
        file.onRead(format, "stringZ"); file.seekV(format.dllArray[format.curEl].nLoc); file.readV(128);
      }
      else
      {
        format.dllArray[0].name="f1"; format.curEl=1;format.scanDLL();
      }
    }

    //Else load the dll array.

    else
    {
      var n = -1, f1 = 0, f2 = 0, pos = 0, end = false; while(!(end=((n|f1|f2)==0)) && (file.tempD.length-20)>pos)
      {
        f1 = file.tempD[pos]|(file.tempD[pos+1]<<8)|(file.tempD[pos+2]<<16)|(file.tempD[pos+3]<<24);
        n = file.tempD[pos+12]|(file.tempD[pos+13]<<8)|(file.tempD[pos+14]<<16)|(file.tempD[pos+15]<<24);
        f2 = file.tempD[pos+16]|(file.tempD[pos+17]<<8)|(file.tempD[pos+18]<<16)|(file.tempD[pos+19]<<24);
        format.dllArray.push(new format.dllEl(format.baseAddress+n,format.baseAddress+f1,format.baseAddress+f2)); pos += 20;
      }

      if(end)
      {
        format.dllArray[0].name="end";format.curEl=1;format.dllArray.pop();format.scanDLL();
      }
      else
      {
        file.onRead(format,"scanDLL");file.seekV(pos+file.tempD.offset);file.readV(60);
      }
    }
  },

  //Create the tree nodes and setup the function map and clear the temporary data structures.

  dllScanDone: function()
  {
    var n = new treeNode("DLL Import Table",[24],true); format.dArray.length(format.dllArray.length);

    for(var i1=1;i1<format.dllArray.length;i1++)
    {
      n.add(new treeNode(format.dllArray[i1].name+".dll",[28,format.dllArray[i1].nLoc,format.dllArray[i1].name.length+1]));
    }

    format.node.setNode(n); dModel.setDescriptor(format.des[6]);

    //Clear temporary data.

    format.dllArray = format.dllEl = format.curEl = format.curFn = undefined;
  },

  /*-------------------------------------------------------------------------------------------------------------------------
  Section readers that are not yet implemented.
  -------------------------------------------------------------------------------------------------------------------------*/

  noReader: function(vPos, size)
  {
    dModel.clear(); file.seekV(vPos); ds.setType(15, 0, size, true);
    
    info.innerHTML = "No reader, for this section yet in the web version.";
  },

  /*-------------------------------------------------------------------------------------------------------------------------
  Read a zero terminated string and call back function.
  -------------------------------------------------------------------------------------------------------------------------*/

  callBack: function(){}, stringZ: function(str)
  {
    for(var i=0,str=str||"";i<file.tempD.length;i++)
    {
      if(file.tempD[i]==0){format.callBack(str);return;}

      str+=String.fromCharCode(file.tempD[i]);
    }

    //Read additional 32 bytes if the end of the string has not been reached.

    file.onRead(format,"stringZ");file.seekV(file.tempD.length+file.tempD.offset);file.readV(32);
  },

  /*-------------------------------------------------------------------------------------------------------------------------
  Tree event handling.
  -------------------------------------------------------------------------------------------------------------------------*/

  open: function(n)
  {
    var e = (format.node = n).getArgs(), cmd = parseInt(e[0]); if(e[0] == ""){ return; }

    //Check if negative value which are used to load in sections.

    if(cmd < 0) { format.readSec[-(cmd+1)](parseFloat(e[1]),parseInt(e[2])); return; }

    //Check if the argument is a command such as start disassembling code, or select bytes.

    des = cmd>>2; cmd &= 3; if(cmd >= 1)
    {
      //CMD 1 is select bytes.

      if(cmd == 1) { dModel.clear(); file.seek(parseInt(e[1])); ds.setType(15, 0, parseInt(e[2]), false); info.innerHTML = format.msg[des]; }

      //Begin disassembling ms-dos app. MS dos files are by default 16 bit x86.

      else if(cmd == 2) { format.disV = parseInt(e[1]); coreReady = format.disMSDos; loadCore("core/x86/dis-x86.js"); }

      //Begin disassembling microsoft app.

      else if(cmd == 3)
      {
        format.disV = parseFloat(e[1]); coreReady = format.disEXE;
        
        if(format.coreType == 0x014C || format.coreType == 0x8664) { loadCore("core/x86/dis-x86.js"); } else
        {
          info.innerHTML = "Core type instruction set not yet supported.";
        }
      }
    }

    //Else it is a data model node.

    else
    {
      if(e.length > 1){ format.des[des].offset = parseInt(e[1]); }
      
      //In special cases the length of a data can be included.

      if(e.length > 2)
      {
        //DLL variable length strings amd function lists.

        if(des == 7){ format.dllName.length(parseInt(e[2])); }
        if(des == 8){ format.funcArray.length(parseInt(e[2])); }
        if(des == 9){ format.funcName.length(parseInt(e[2])); }
      }
      
      dModel.setDescriptor(format.des[des]);
    }
  },

  /*-------------------------------------------------------------------------------------------------------------------------
  Detailed information output goes bellow this comment.
  -------------------------------------------------------------------------------------------------------------------------*/

  //Message output for byte selection command.

  msg: ["Detailed information is not added yet to the Microsoft plugin on the web version."],

  //MZ header information.

  mzHeader: function(i)
  {
    if( i < 0 ) { this.sig.length(2); this.r1.length(8); this.r2.length(20); }

    info.innerHTML = format.msg[0];
  },

  //MS-dos relocations information.

  mzRel: function(i)
  {
    if( i < 0 ) { this.sig.length(2); this.r1.length(8); this.r2.length(20); }
  
    info.innerHTML = format.msg[0];
  },

  //PE header information.

  peHeader: function(i)
  {
    if( i < 0 ) { this.sig.length(4); }
  
    info.innerHTML = format.msg[0];
  },

  //OP header information.

  opHeader: function(i)
  {
    if( i < 0 ) { this.sig.length(2); }
  
    info.innerHTML = format.msg[0];
  },

  //Data directory array information.

  dirArray: function(i)
  {
    if( i < 0 ) { }
  
    info.innerHTML = format.msg[0];
  },

  //Section array information.

  secArray: function(i)
  {
    if( i < 0 ) { this.r1.length(12); }
  
    info.innerHTML = format.msg[0];
  },

  //Used to identify bad file signatures in the case of a corrupted application.

  badSig: function(i)
  { 
    info.innerHTML = "A bad signature has been encountered, so the application is corrupted!";
  },

  /*-------------------------------------------------------------------------------------------------------------------------
  Disassembly methods goes bellow this comment. Note it is possible to add am scanner that translates code to C/C++.
  -------------------------------------------------------------------------------------------------------------------------*/

  //The x86 core is ready and we can now begin ms dos disassembly.

  disMSDos: function()
  {
    core.showInstructionHex = false;

    core.scan = format.dosScan; core.addressMap = true; core.resetMap(); core.bitMode = 0;
    
    core.setCodeSeg((Math.random()*0x2000)<<3); dModel.setCore(core); dModel.coreDisLoc(format.disV,true);
  },

  //The x86 core is ready and we can now begin Microsoft application disassembly.

  disEXE: function()
  {
    core.showInstructionHex = false;

    core.scanReset(); core.addressMap = true; core.resetMap(); core.bitMode = format.is64bit ? 2 : 1;
      
    dModel.setCore(core); dModel.coreDisLoc(format.disV,true);
  },

  //MSDos code scanner. Ensures proper disassembly of old 16 ms dos applications.

  Dos_exit: 0, dosScan: function(crawl)
  {
    var i = core.instruction + " " + core.insOperands;
    
    if( format.Dos_exit == 0 && ( i.startsWith("MOV AX,4C") || i.startsWith("MOV AH,4C") ) ) { format.Dos_exit = 1; }
    else if( format.Dos_exit == 1 && ( i.indexOf("AX,") > 0 || i.indexOf("AH,") > 0 ) ) { format.Dos_exit = 0; }
    if( format.Dos_exit == 1 && i == "INT 21" ) { format.Dos_exit = 2; }
    
    return( format.Dos_exit == 2 || i.startsWith("RET") || i.startsWith("JMP") || i == "INT 20" );
  }
}

//The data descriptor calls this function when we go to click on an address we wish to disassemble.

dModel.coreDisLoc = function(virtual,crawl)
{
  //Begin data check.

  format.Dos_exit = 0; this.cr = crawl; core.setAddress(virtual);

  //If the address we wish to disassemble is within the current memory buffer then we do not have to read any data.

  file.bufRead(this, "dis"); file.seekV(format.disV = virtual); file.initBufV();
}

dModel.dis = function()
{
  //Set binary code relative position within the buffer.

  core.setBinCode(file.dataV,format.disV - file.dataV.offset);
  
  //Begin disassembling the code.
  
  info.innerHTML = "<pre>" + core.disassemble(this.cr) + "</pre>";

  window.offset.slen = 1; window.virtual.slen = core.getAddress() - format.disV;
    
  dModel.adjSize(); dModel.update(); file.seekV(format.disV);
}