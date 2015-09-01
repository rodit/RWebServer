<?rsc
session_start();

if(session.readBool("logged_in")){
	header("Location", "loggedin.rsc");
}

var username = post("username");
var email = post("email");
var password = post("password");

if(!(isset(username) && isset(email) && isset(password))){
	echo("Please complete all the fields.");
}else{
	var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
	if(!re.test(email)){
		echo("Please enter a valid email.");
	}else{
		mysql_connect("localhost", "test", "root", "Degirmenci9");
		var utaken = mysql_query("SELECT * FROM users WHERE username=?", [username]);
		var etaken = mysql_query("SELECT * FROM users WHERE email=?", [email]);
		if(mysql_num_rows(utaken) != 0){
			echo("That username is already taken.");
		}else if(mysql_num_rows(etaken) != 0){
			echo("That email is already in use.");
		}else{
			mysql_query_update("INSERT INTO users (username, email, password) VALUES (?, ?, ?)", [username, email, password]);
			echo("Successfully created user.");
		}
	}
}

rsc?>
