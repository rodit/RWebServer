<?rsc
session_start();
echo("Logged in: " + session.readBool("logged_in") + "<br>");

if(session.readBool("logged_in")){
	header("Location", "loggedin.rsc");
}else{
	echo("<form method='post' action='check_login.rsc'><input type='text' name='txtUser'/><br><input type='password' name='txtPass'/><br><input type='submit' name='btnSubmit' value='Login'/></form><form action='register.rsc' method='post'><input type='submit' value='Register'/></form>");
}

rsc?>
