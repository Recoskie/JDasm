//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //Function load is always called first.

  load: function()
  {
    //Dos com files are by default x86-16 bit programs.

    loadCore("core/x86/dis-x86.js");

    //Dos com files are loaded at 0x100 by default in virtual address space.

    file.addV( 0, file.size, 0x0100, file.size );

    //The root node is the binary application.
    
    var root = new treeNode(file.name.substring(file.name.lastIndexOf("/")+1,file.name.length),[],true,false);

    root.add("Info.h", [0],true); root.add("Program Start (Machine code).h", [1]);
    Tree.set(root); tree.prototype.event = this.treeEvent;

    //Show virtual address space.

    if( !virtual.visible ) { showH(true); }

    this.treeEvent({getArgs:function(){return([0]);}})
  },

  //Tree event handling.

  treeEvent: function(node)
  {
    var args = node.getArgs();

    if( args[0] == 0 ) { info.innerHTML = "DOS COM Files have no header or setup information. The program begins at the start of the file and is placed at 0x100 in RAM memory."; }
    else
    {
      core.addressMap = true; core.resetMap(); core.bitMode = 0; file.seekV(0x100); core.setBasePosition("72D2:0100");

      //Code crawling is not yet available, so I will use linear disassembly.

      core.setBinCode(file.data,0); info.innerHTML = "<pre>" + core.disassemble(true) + "</pre>"; dModel.setCore(core);
    }
  }
}