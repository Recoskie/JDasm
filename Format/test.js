//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {

  //Function load is always called first.

  load: function()
  {
    //Test the new binary tree component.
    
    var root = new treeNode("test.exe",[],true,false);
    root.add("Add nodes on click.h", ["h1"]);
    root.add("Click count.h", ["h2",0]);
    root.add("Mod Tree.h", ["h3"]);
    var im = new treeNode("Import.dll"); root.add(im);
    im.add("Func1");
    im.add("Func2");
    var sf = new treeNode("SubFunc"); im.add(sf);
    sf.add("SubFunc1");
    sf.add("SubFunc2");
    sf.add("SubFunc3");
    sf.add("SubFunc4");
    var rs = new treeNode("Resource"); root.add(rs);
    rs.add("File1");
    rs.add("File2");
    var fl = new treeNode("Folder"); rs.add(fl);
    fl.add("File1");
    fl.add("File2");
    fl.add("File3");
    fl.add("File4", ["test"]);

    Tree.set(root); tree.prototype.event = this.event;
  },

  //Tree event handling.

  event: function(node)
  {
    var args = node.getArgs();
    
    //Dynamically change the tree.
    
    if(args[0] == "h1")
    {
      var n = new treeNode("Add nodes on click.h",[""],true,true);
      n.add("info1");
      n.add("info2");
      node.setNode(n);
    }
    
    //set or change attributes.
    
    if(args[0] == "h2")
    {
      args[1] = parseInt(args[1]) + 1; node.setArgs(args);
    }

    //Navigate and change tree nodes.

    if(args[0] == "h3")
    {
      node.setArgs([]);

      var rNode = Tree.getNode(0).getNode(3);

      var n = new treeNode("Modified.h",["changed"],true);
      n.add("new node 1");
      n.add("new node 2");
      rNode.setNode(n);
    }
    
    info.innerHTML = "You clicked on node " + node.innerText + "<br /><br />Node changeable or settable attributes = " + args + "<br /><br />The attributes can tell our format reader where to go in the file or what to read and decode on a node click.";
  }
}