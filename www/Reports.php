<html>
<head>
  <title>R Server Reports</title>
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
  <h2 id="RHeader">R Server Reports</h2>
</div>

<div id="ContentDiv">

<h2 class="SubpageHeader">Reports</h2>

<?php

function read_dir_content($parent_dir, $depth = 0, $path){
	echo "<ul>";

    if ($handle = opendir($parent_dir))
    {

        while (false !== ($file = readdir($handle)))
        {
            if(in_array($file, array('.', '..'))) continue;
            if( is_dir($parent_dir . "/" . $file) ){

				echo "<li><b><u>";
				echo $file;
				echo "</u></b></li>";
				read_dir_content($parent_dir . "/" . $file, $depth++, $parent_dir . "/" . $file . "/");
            }
        }

        closedir($handle);
    }

    if ($handle = opendir($parent_dir))
    {

        while (false !== ($file = readdir($handle)))
        {
            if(in_array($file, array('.', '..'))) continue;
            if(!is_dir($parent_dir . "/" . $file) ){
				echo "<li><a href=\"$path$file\">$file</a>";
				echo "</li>";
			}
        }

        closedir($handle);
    }

	echo "</ul>";
}


read_dir_content("reports", 0, "reports");

?>

</div>

<div id="FooterDiv">
  <a href="https://github.com/bgweber/RServer">RServer on GitHub</a>
</div>

</div>
 </body>
</html>