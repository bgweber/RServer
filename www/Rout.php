<?php
$running = array_key_exists('task', $_GET);
?>

<html>
<head>
  <title>R Log</title>
  <link rel="stylesheet" type="text/css" href="rserver.css">
<body>
<div id="MainDiv">

<div id="HeaderDiv">
  <div id="NavHeaderDiv"></div>
  <ul id="NavBar">
    <li><a href="https://github.com/bgweber/RServer">EA RServer</a></li>
    <li><a href="NewTask.php">Submit New Task</a>
    <li><a href="Reports.php">Reports</a></li>
    <li><a href="logs">Server Logs</a></li>
    <li><a href="Rout">Script Results</a></li>
    <li><a href="reports/RServer/RServerTasks.html">Task Report</a></li>
  </ul>
</div>
<div id="SubHeaderDiv">
  <h2 id="RHeader"><?php if ($running > 0 ) { echo "Running "; } else { echo "Completed "; } ?>Script Log</h2>
</div>

<div id="ContentDiv">
<h2 class="SubpageHeader"><?php echo $_GET['log']; ?></h2>

<?php
if ($running > 0) {
	$task =$_GET['task'];
	$log =$_GET['log'];
	
	if (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {
		$path = "C:/RServer/scripts/" . $task . "/" . $log;
	}
	else {
		$path = "/var/www/html/RServer/scripts/" . $task . "/" . $log;  
	}
}
else {
	$path =$_GET['path'];
	
	if (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {
		$path = "C:/wamp/www/RServer/Rout/" . $path;
	}
	else {
		$path = "/var/www/html/RServer/Rout/" . $path; 
	}
}

 if (!file_exists($path)) {
	echo "Log file not found: " . $path;
 }
 else {
	echo "Path: " . $path;
	
 	$handle = fopen($path, "r");
	if ($handle) {
		echo "<p>Got handle: " . $path;
	}
}
?>

 </div>

<div id="FooterDiv">
  <a href="https://github.com/bgweber/RServer">RServer on GitHub</a>
</div>

</div>

 </body>
</html>
