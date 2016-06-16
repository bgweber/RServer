

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
  ip <- as.character(readLines("http://ipinfo.io/ip"))
  print(paste("Using IP:", ip))
  runApp(port=as.integer(port), launch.browser = FALSE, host = "172.31.53.98")
  
} else {
  runApp()
  stop("No port specified")
}  

