

libraries <- c("shiny", "shinythemes", "dygraphs", "DT", "RCurl", "xts")
for (lib in libraries) {
  if (lib %in% rownames(installed.packages()) == FALSE) {
    install.packages(lib, repos='http://cran.us.r-project.org') 
  } 
}

library(shiny)  
library(shinythemes)
library(dygraphs) 
library(DT)
library(RCurl)  
library(xts)

# Launch shiny 
args <- commandArgs(TRUE)  
if (length(args) > 0) {  
  
  # get the port 
  port <- args[1]   
  runApp(port=as.integer(port))
  
} else {
  runApp()
  stop("No port specified")
}  

