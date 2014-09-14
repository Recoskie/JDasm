var a=new ActiveXObject("Scripting.filesystemobject");

var f=a.opentextfile("op codes 1 - Copy.txt");

var output=a.opentextfile("styled.txt",2,1);

out=f.ReadAll().replace(/\|/g,"\r\n");

output.write(out);

f=a.opentextfile("styled.txt");
output=a.opentextfile("styled.html",2,1);

var out="<html><body bgcolor=\"#FFFFFF\" text=\"#000000\">",t="";
out+="<center><table border=\"1\">";

for(var i=0;i<211;i++)
{
out+="<tr><td>";

t=f.readLine();
t=t.replace(/\//g,"</td><td>");

if(t.substring(0,2)=="F2"|t.substring(0,2)=="F3")
{t=t.substring(0,4)+"</td><td>"+t.substring(4,t.length);}
else{t=t.substring(0,2)+"</td><td>"+t.substring(2,t.length);}

out+=t;
out+="</td></tr>";
}

output.write(out+"</table></center></body></html>");