
libraries <- c("rmarkdown", "yaml", "scales")  
for (lib in libraries) {
  if (lib %in% rownames(installed.packages()) == FALSE) {
    install.packages(lib, repos='http://cran.us.r-project.org')  
  } 
} 

require(rmarkdown)      
render("TaskReport.Rmd", output_format = "html_document", output_file = "RServerTasks.html")      
file.copy("RServerTasks.html", "C:/wamp/www/RServer/reports/RServer/RServerTasks.html", overwrite = TRUE) 
