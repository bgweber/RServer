
libraries <- c("rmarkdown", "yaml", "scales")  
for (lib in libraries) {
  if (lib %in% rownames(installed.packages()) == FALSE) {
    install.packages(lib, repos='http://cran.us.r-project.org')  
  } 
} 

require(rmarkdown)      
render("TaskReport.rmd", output_format = "html_document", output_file = "RServerTasks.html")      

if (Sys.info()['sysname'] == "Windows") {  
  file.copy("RServerTasks.html", "C:/wamp/www/RServer/reports/RServer/RServerTasks.html", overwrite = TRUE)     
} else {
  file.copy("RServerTasks.html", "/var/www/html/RServer/reports/RServer/RServerTasks.html", overwrite = TRUE)      
}
