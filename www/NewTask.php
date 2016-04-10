<html>
<head>
  <title>R Server: Submit Task</title>
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
  <h2 id="RHeader">R Server: New Task</h2>
	</div>

<div id="ContentDiv">

<h2 class="SubpageHeader">Run an ad-hoc R Script</h2>

<form action="SubmitTask.php" method="post">

<h4 class="FormHeader">Task Name</h4>
<input type="text" name="name" value="ExampleTask">

<h4 class="FormHeader">Perforce Directory</h4>
<input type="text" name="path" value="//GAI/datascience/tasks/samples/HelloWorld" size=60>

<h4 class="FormHeader">R Script Name</h4>
<input type="text" name="rscript" value="HelloWorld.r">

<h4 class="FormHeader">Email Address</h4>
<input type="text" name="email" value="beweber@ea.com">

<h4 class="FormHeader">Send Email on Success?</h4>
<input type="text" name="sendemail" value="true">

<h4 class="FormHeader">Runtime parameters</h4>
<input type="text" name="params" value="" size=60>

<h4 class="FormHeader">Is Shiny App?</h4>
<input type="text" name="shiny" value="false">

<p><input type="submit">
 </form>

 </div>

<div id="FooterDiv">
  <a href="https://github.com/bgweber/RServer">RServer on GitHub</a>
</div>

</div>

 </body>
</html>