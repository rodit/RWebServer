<?rsc
session_start();

var user = post("txtUser");
var pass = post("txtPass");

mysql_connect("localhost", "test", "root", "Degirmenci9");

var result = mysql_query("SELECT * FROM users WHERE (username=? OR email=?) AND password=?", [user, user, pass]);

if(mysql_num_rows(result) == 1){
	result.next();
	session.setBool("logged_in", true);
	session.set("username", result.getString("username"));
	session.set("email", result.getString("email"));
	session.set("password", result.getString("password"));
	echo("Logged in!");
	header("Location", "loggedin.rsc");
}else{
	echo("Invalid username or password.");
}

rsc?>
