//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //Basic header structures.

  header: new Descriptor([]),

  //Function load is always called first.

  load: function()
  {
    //DOS com Files have no header.

    this.header.setEvent(this, "dataEvent");

    //The root node is the binary application.
    
    var root = new treeNode(file.name.substring(file.name.lastIndexOf("/")+1,file.name.length),[],true);

    root.add("Info.h", [0]); Tree.set(root); tree.prototype.event = this.treeEvent;

    //Hide virtual address space.

    if( virtual.visible ) { showH(true); }

    //Set the default selected node.

    tree.prototype.treeClick( Tree.getNode(0).getNode(0).parentElement );
  },

  //Tree event handling.

  treeEvent: function(node) { dModel.setDescriptor(format.header); },

  //When user clicks on information for the program header.

  dataEvent: function()
  {
    info.innerHTML = "The microsoft application plugin is not built for the web version yet.";
  }
}