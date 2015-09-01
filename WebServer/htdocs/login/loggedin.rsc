<?rsc
session_start();

if(session.readBool("logged_in")){
	echo("Welcome " + session.read("username"));
	echo("<form method='post' action='logout.rsc'><input type='submit' value='Logout'/>");
}else{
	header("Location", "index.rsc");
}

rsc?>
