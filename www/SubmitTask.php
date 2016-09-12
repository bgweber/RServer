<!-- Copyright (C) 2016 Electronic Arts Inc.  All rights reserved. -->
<html>
<head>
  <title>R Server: Running Task</title>
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
  <h2 id="RHeader">R Server: Running Task</h2>
</div>

<div id="ContentDiv">

<h2 class="SubpageHeader">Running Task</h2>

<ul id="NewTaskList">
<?php

$name = str_replace(",", " ", $_POST['name']);
$path = str_replace(",", " ", $_POST['path']);
$rscript = str_replace(",", " ", $_POST['rscript']);
$email = str_replace(",", " ", $_POST['email']);
$sendemail = str_replace(",", " ", $_POST['sendemail']);
$params = str_replace(",", " ", $_POST['params']);
$shiny = str_replace(",", " ", $_POST['shiny']);
$timestamp = time();
$jobID = $timestamp . "_" . rand(1, 999);

echo "<li>Task: $name</li>";
echo "<li>Path: $path</li>";
echo "<li>Script: $rscript</li>";
echo "<li>Email: $email</li>";
echo "<li>Notify: $sendemail</li>";
echo "<li>Params: $params</li>";
echo "<li>Shiny App: $shiny</li>";
echo "<li>Job ID: $jobID</li>";

$file = fopen("requests/$name$timestamp.ad", "w");
fwrite($file, "$name,$path,$rscript,$email,$sendemail,$params,$shiny,$jobID");
fclose($file);
?>
</ul>

<p><a href="index.php">Server Dashboard</a>
</div>

<div id="FooterDiv">
  <a href="https://github.com/bgweber/RServer">RServer on GitHub</a>
</div>

</div>
 </body>
</html>
