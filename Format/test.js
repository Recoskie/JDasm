//Everything in the format reader must be defined as an object called format.
//This ensures that all data in the format reader is overwritten when loading in a new format.

format = {
  //This will be used to hold the descriptors for the file format headers in this example.
  
  headers: [], strTest: new dataType("Adjustable String", Descriptor.String8),

  //Function load is always called first.

  load: function()
  {
    //test the data descriptor model.

    this.arrayTest = new arrayType("Array Test", [
      new dataType("Data 1", Descriptor.Int64),
      this.strTest,
      new dataType("Data 2", Descriptor.LInt32)
    ]);
    this.headers[0] = new Descriptor([
      new dataType("Value 1", Descriptor.LInt64 ),
      this.strTest,
      new dataType("Value 2", Descriptor.Int64 ),
      new dataType("Value 3", Descriptor.LInt32 ),
      new dataType("Value 4", Descriptor.Int64 ),
      new dataType("Value 5", Descriptor.LInt64 ),
      new dataType("Value 6", Descriptor.Int32 ),
      new dataType("Value 7", Descriptor.Int32 ),
      new dataType("Value 8", Descriptor.Int32 ),
      new dataType("Value 9", Descriptor.Int32 ),
      new dataType("Value 10", Descriptor.Int32 ),
      new dataType("Value 11", Descriptor.Int32 ),
      new dataType("Value 12", Descriptor.Int32 ),
      new dataType("Value 13", Descriptor.Int32 ),
      new dataType("Value 14", Descriptor.Int32 ),
      new dataType("Value 15", Descriptor.Int32 ),
      new dataType("Value 16", Descriptor.Int32 ),
      new dataType("Value 17", Descriptor.Int32 ),
      this.arrayTest,
      new dataType("Value 18", Descriptor.Int32 ),
      new dataType("Value 19", Descriptor.Int32 ),
      new dataType("Value 20", Descriptor.Int32 ),
      new dataType("Value 21", Descriptor.Int32 ),
      new dataType("Value 22", Descriptor.Int32 ),
      new dataType("Value 23", Descriptor.Int32 ),
      new dataType("Value 24", Descriptor.Int32 ),
      new dataType("Value 25", Descriptor.Int32 ),
      new dataType("Value 26", Descriptor.Int32 ),
      new dataType("This is a long description! This is a long description!", Descriptor.Int32 ),
      new dataType("Value 28", Descriptor.Int32 ),
      new dataType("Value 29", Descriptor.Int32 ),
      new dataType("Value 30", Descriptor.Int32 ),
      new dataType("Value 31", Descriptor.Int32 ),
      new dataType("Value 32", Descriptor.Int64 ),
      new dataType("Value 33", Descriptor.Int32 ),
      new dataType("Value 34", Descriptor.Int32 ),
      new dataType("Value 35", Descriptor.Int32 ),
      new dataType("Value 36", Descriptor.Int32 ),
      new dataType("Value 37", Descriptor.LInt64 ),
      new dataType("Value 38", Descriptor.Int64 ),
      new dataType("Value 39", Descriptor.LInt32 ),
      new dataType("Value 40", Descriptor.Int64 ),
      this.arrayTest,
      new dataType("Value 41", Descriptor.LInt64 ),
      new dataType("Value 42", Descriptor.Int32 ),
      new dataType("Value 43", Descriptor.Int32 ),
      new dataType("Value 44", Descriptor.Int32 ),
      new dataType("Value 45", Descriptor.Int32 ),
      new dataType("Value 46", Descriptor.Int32 ),
      new dataType("Value 47", Descriptor.Int32 ),
      new dataType("Value 48", Descriptor.Int32 ),
      new dataType("Value 49", Descriptor.Int32 ),
      new dataType("Value 50", Descriptor.Int32 ),
      new dataType("Value 51", Descriptor.Int32 ),
      new dataType("Value 52", Descriptor.Int32 ),
      new dataType("Value 53", Descriptor.Int32 ),
      new dataType("Value 54", Descriptor.Int32 ),
      new dataType("Value 56", Descriptor.Int32 ),
      new dataType("Value 57", Descriptor.Int32 ),
      new dataType("Value 58", Descriptor.Int32 ),
      new dataType("Value 59", Descriptor.Int32 ),
      new dataType("Value 60", Descriptor.Int32 ),
      new dataType("Value 61", Descriptor.Int32 ),
      new dataType("Value 62", Descriptor.Int32 ),
      new dataType("Value 63", Descriptor.Int32 ),
      new dataType("This is a long description! This is a long description!", Descriptor.Int32 ),
      new dataType("Value 64", Descriptor.Int32 ),
      new dataType("Value 65", Descriptor.Int32 ),
      new dataType("Value 66", Descriptor.Int32 ),
      new dataType("Value 67", Descriptor.Int32 ),
      new dataType("Value 68", Descriptor.Int64 ),
      this.strTest,
      new dataType("Value 69", Descriptor.Int32 ),
      new dataType("Value 70", Descriptor.Int32 ),
      new dataType("Value 71", Descriptor.Int32 ),
      new dataType("Value 72", Descriptor.Int32 )
    ]);

    this.headers[0].setEvent(this, "data1Event");

    //test the data descriptor model.

    this.headers[1] = new Descriptor( [
      new dataType("Value 1", Descriptor.Int32 ),
      new dataType("Value 2", Descriptor.Int32 ),
      new dataType("Value 3", Descriptor.Int32 ),
      new dataType("Value 4", Descriptor.Int32 )
    ]);

    dModel.setDescriptor(this.headers[0]);
    
    //Test the new binary tree component.
    
    var root = new treeNode("test.exe",[],true,false);
    root.add(des = new treeNode("Data test"));
    des.add("Descriptor 1(pos=0).h",["d",0,0]);
    des.add("Descriptor 2(pos=0).h",["d",1,0]);
    des.add("Descriptor 1(pos=1024).h",["d",0,1024]);
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

    Tree.set(root); tree.prototype.event = this.treeEvent;

    //Virtual address space is not needed for this demo.

    if( virtual.visible ) { showH(true); }
  },

  //Tree event handling.

  treeEvent: function(node)
  {
    var args = node.getArgs();
    
    //Change or set descriptors.
    
    if(args[0] == "d")
    {
      format.headers[args[1]].offset = parseInt(args[2]); dModel.setDescriptor(format.headers[args[1]]);
    }
    
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

      var rNode = Tree.getNode(0).getNode(4);

      var n = new treeNode("Modified.h",["changed"],true);
      n.add("new node 1");
      n.add("new node 2");
      rNode.setNode(n);
    }
    
    info.innerHTML = "You clicked on node " + node.innerText + "<br /><br />Node changeable or settable attributes = " + args + "<br /><br />The attributes can tell our format reader where to go in the file or what to read and decode on a node click.";
  },

  //Data type descriptor event.

  data1Info: [
    "This is an example Property.",
    "This is the second value read by the descriptor."
  ],

  data1Event: function(index)
  {
    if( index < 0 )
    {
      this.arrayTest.length(Math.round(Math.random()*10));
      this.strTest.length(Math.round(Math.random()*10));
      
      info.innerHTML = "You just set this descriptor, but did not click on any values read in the descriptor.";
    }
    else if( index < 2 )
    {
      info.innerHTML = this.data1Info[ index ];
    }
    else { info.innerHTML = "Value = " + (index + 1) + ""; }
  }
}