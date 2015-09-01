<?rsc

var a = post("arg0");
var b = post("arg1");

if(isset(a) && isset(b)){
	if(a == "delete")
		delete_file(b);
	else if(a == "mkdir")
		mkdir(b);
	else if(a == "mkdirs")
		mkdirs(b);
	else if(a == "create")
		create_file(b);
	else if(a == "read" && file_exists(b))
		echo(read_file(b));
	else if(a == "write")
		write_file(a, b);
	else{
		echo("Invalid operation.");
	}
	echo("Done");
}

rsc?>

<form action="index.rsc" method="post">
<input type="text" placeholder="Arg0" name="arg0"/>
<input type="text" placeholder="Arg1" name="arg1"/>
<input type="submit" value="Go"/>
</form>
