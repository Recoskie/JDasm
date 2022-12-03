//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //Function load is always called first.

  load: function()
  {
    //Dos com files are by default x86-16 bit programs.

    loadCore("core/x86/dis-x86.js");

    //Dos com files are loaded at 0x100 by default in virtual address space.

    file.resetV(); file.addV( 0, file.size, 0x0100, file.size );

    //The root node is the binary application.
    
    var root = new treeNode(file.name.substring(file.name.lastIndexOf("/")+1,file.name.length),[],true,false);

    root.add("Info.h", [0],true); root.add("Program Start (Machine code).h", [1]);
    Tree.set(root); tree.prototype.event = this.treeEvent;

    //Show virtual address space.

    if( !virtual.visible ) { showH(true); } virtual.sc();

    this.treeEvent({getArgs:function(){return([0]);}});
  },

  //Tree event handling.

  treeEvent: function(node)
  {
    var args = node.getArgs();

    if( args[0] == 0 ) { info.innerHTML = "DOS COM Files have no header or setup information. The program begins at the start of the file and is placed at 0x100 in RAM memory."; }
    else
    {
      core.addressMap = true; core.resetMap(); core.bitMode = 0; file.seekV(0x100);
      
      dModel.setCore(core); dModel.coreDisLoc(0x100,true);
    }
  }
}

//The data descriptor calls this function when we go to click on an address we wish to disassemble.

var cr = false, vr = 0;

dModel.coreDisLoc = function(virtual,crawl)
{
  //We still have to move to a section and read the data if it is not loaded.
  //The function dis is called when the data is ready, or is already in position.

  if( this.dis == null ) { this.dis = function()
  {
    core.setBinCode(file.dataV,vr); //Relative position within the buffer.
  
    //Begin disassembling the code.
  
    info.innerHTML = "<pre>" + core.disassemble(cr) + "</pre>"; dModel.update();
  }}

  //Begin data check.

  vr = virtual; cr = crawl; core.setBasePosition("72D2:" + vr.toString(16));

  //If the address we wish to disassemble is within the current memory buffer then we do not have to read any data.

  var pos = vr - file.dataV.offset; if(vr >= 0 && pos <= file.dataV.length) { file.seekV(vr); vr = pos; this.dis(); }
 
  //Else we need to locate to the section disassembly can happen.
 
  else { file.call( this, "dis" ); file.seekV(vr); vr = 0; file.readV(data); }
}