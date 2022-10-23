//Everything in the format reader must be defined as an object called format.
//This ensures that an load format readers data does not remain when loading in a new format reader.

format = {

  //Function load is always called first.

  load: function()
  {
    //Test the new binary tree component.
    
    var root = new treeNode("test.exe");
    root.add("Header1.h");
    root.add("Header2.h");
    var im = new treeNode("Import.dll");
    im.add("Func1");
    im.add("Func2");
    var sf = new treeNode("SubFunc");
    sf.add("SubFunc1");
    sf.add("SubFunc2");
    sf.add("SubFunc3");
    sf.add("SubFunc4");
    var rs = new treeNode("Resource");
    rs.add("File1");
    rs.add("File2");
    var fl = new treeNode("Folder");
    fl.add("File1");
    fl.add("File2");
    fl.add("File3");
    fl.add("File4");
    
    im.add(sf); root.add(im);
    rs.add(fl); root.add(rs);

    Tree.set(root); tree.prototype.event = this.event;
  },

  //Tree event handling.

  event: function(node)
  {
    info.innerHTML = "You click on node " + node.innerText;
  }
}