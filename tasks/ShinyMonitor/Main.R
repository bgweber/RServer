# Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.

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
  
  port <- args[1] 

  # get the IP address 
  if (Sys.info()['sysname'] == "Windows") {
    
    x <- system("ipconfig", intern=TRUE)
    z <- x[grep("IPv4", x)] 
    server <- gsub(".*? ([[:digit:]])", "\\1", z)[1]
  } else {
    server <- system("ifconfig | perl -nle 's/dr:(\\S+)/print $1/e'", intern=TRUE)[1]
  }

  runApp(port=as.integer(port), launch.browser = FALSE, host = server)
  
} else {
  runApp()
  stop("No port specified")
}  

