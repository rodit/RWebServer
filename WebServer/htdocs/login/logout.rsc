<?rsc
session_start();

if(session.readBool("logged_in")){
	session.setBool("logged_in", false);
}
header("Location", "index.rsc");
rsc?>
