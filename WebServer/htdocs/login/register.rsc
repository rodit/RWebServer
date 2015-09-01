<?rsc
session_start();

if(session.readBool("logged_in")){
	header("Location", "loggedin.rsc");
}

rsc?>

<body>
<form method="post" action="register_check.rsc">
<label for="username">Username:</label>
<input type="text" name="username"/><br>
<label for="email">Email:</label>
<input type="text" name="email"/><br>
<label for="password">Password:</label>
<input type="password" name="password"/><br>
<input type="submit" value="Register"/>
</form>
</body>
