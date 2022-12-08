//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //DOS com files have a blank header.

  header: new Descriptor([]),

  //Function load is always called first.

  load: function()
  {
    //DOS com Files have no header.

    this.header.setEvent(this, "dataEvent");

    //Dos com files are by default x86-16 bit programs.

    loadCore("core/x86/dis-x86.js");

    //Dos com files are loaded at 0x100 by default in virtual address space.

    file.addV( 0, file.size, 0x0100, file.size );

    //The root node is the binary application.
    
    var root = new treeNode(file.name.substring(file.name.lastIndexOf("/")+1,file.name.length),[],true);

    root.add("Info.h", [0]); root.add("Program Start (Machine code).h", [1]);
    Tree.set(root); tree.prototype.event = this.treeEvent;

    //Show virtual address space.

    if( !virtual.visible ) { showH(true); } file.seekV(0x100); virtual.sc();

    //Set the default selected node.

    tree.prototype.treeClick( Tree.getNode(0).getNode(0).parentElement );
  },

  //Tree event handling.

  treeEvent: function(node)
  {
    var args = node.getArgs();

    if( args[0] == 0 ) { dModel.setDescriptor(format.header); }
    else
    {
      core.addressMap = true; core.resetMap(); core.bitMode = 0; file.seekV(0x100);
      
      core.setCodeSeg((Math.random()*0x2000)<<3); dModel.setCore(core); dModel.coreDisLoc(0x100,true);
    }
  },

  //When user clicks on information for the program header.

  dataEvent: function()
  {
    info.innerHTML = "DOS COM Files have no header or setup information. The program begins at the start of the file and is placed at 0x100 in RAM memory.";
  }
}

//The data descriptor calls this function when we go to click on an address we wish to disassemble.

dModel.coreDisLoc = function(virtual,crawl)
{
  //We still have to move to a section and read the data if it is not loaded.
  //The function dis is called when the data is ready, or is already in position.

  if( this.dis == null ) { this.dis = function()
  {
    //Set binary code relative position within the buffer.

    core.setBinCode(file.dataV,this.vr);
  
    //Begin disassembling the code.
  
    info.innerHTML = "<pre>" + core.disassemble(this.cr) + "</pre>";
    
    file.seekV(file.dataV.offset+this.vr); dModel.adjSize(); dModel.update();
  }}

  //Begin data check.

  this.vr = virtual; this.cr = crawl; core.setAddress(this.vr);

  //If the address we wish to disassemble is within the current memory buffer then we do not have to read any data.

  var pos = this.vr - file.dataV.offset; if(pos >= 0 && pos <= file.dataV.length) { this.vr = pos; this.dis(); }
 
  //Else we need to locate to the section before disassembly can happen.
 
  else { this.vr = virtual & 0x0F; file.call( this, "dis" ); file.seekV(virtual-this.vr); file.readV(window.innerHeight >> 1); }
}