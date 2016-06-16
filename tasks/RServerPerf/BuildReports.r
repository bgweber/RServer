

libraries <- c("rmarkdown", "yaml", "scales")  
for (lib in libraries) {
  if (lib %in% rownames(installed.packages()) == FALSE) {
    install.packages(lib, repos='http://cran.us.r-project.org')  
  } 
} 

  
require(rmarkdown)   
render("ServerReport.Rmd", output_format = "html_document", output_file = "RServerReport.html")      
 
if (Sys.info()['sysname'] == "Windows") { 
  render("ServerReport.Rmd", output_format = "pdf_document", output_file = "RServerReport.pdf")    
  render("ServerReport.Rmd", output_format = "word_document", output_file = "RServerReport.docx")    
  
  file.copy("RServerReport.pdf", "C:/wamp/www/RServer/reports/RServer/RServerReport.pdf", overwrite = TRUE)     
  file.copy("RServerReport.html", "C:/wamp/www/RServer/reports/RServer/RServerReport.html", overwrite = TRUE)       
  file.copy("RServerReport.docx", "C:/wamp/www/RServer/reports/RServer/RServerReport.docx", overwrite = TRUE)     
   
} else {
  file.copy("RServerReport.html", "/var/www/html/RServer/reports/RServer/RServerReport.html", overwrite = TRUE)       
}