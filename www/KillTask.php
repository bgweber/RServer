<html>
<head>
  <title>R Server: Killing Task</title>
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
  <h2 id="RHeader">R Server: Killing Task</h2>
</div>

<div id="ContentDiv">
<h2 class="SubpageHeader">Killing Task</h2>

<ul id="KillTaskList">
<li>
<?php

$name = $_POST['name'];
$timestamp = time();

echo "Task: $name";

$file = fopen("requests/$name$timestamp.kp", "w");
fwrite($file, "$name");
fclose($file);

?>
</li>
</ul>

<p><a href="index.php">Server Dashboard</a>
</div>

<div id="FooterDiv">
  <a href="https://github.com/bgweber/RServer">RServer on GitHub</a>
</div>

</div>
</body>
</html>