# Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.
path <- "/var/www/html/RServer/logs"

if (Sys.info()['sysname'] == "Windows") { 
  path <- "C:/wamp/www/RServer/logs"
}

if ("DT" %in% rownames(installed.packages()) == FALSE) {
  install.packages("DT", repos='http://cran.us.r-project.org') 
} 
library(DT)
 
loadTaskData <- function(daysHistory = 60) { 
  events <- data.frame()  
  
  for (file in list.files(path, full.names = TRUE)) { 
    date <- as.Date(strsplit(file, "_", fixed = TRUE)[[1]][2]) 
    daysAgo <- as.numeric(Sys.Date() - date, units = "days")
    
    if (daysAgo <= daysHistory) {  
      res <- readLines(file)
      
      for (line in res) { 
        
        outcome <- NULL
        
        if (length(grep("R Script completed successfully", line)) > 0) {
          outcome <- "Success"
        }

        if (length(grep("R Script failed", line)) > 0 || length(grep("Unable to run R Script", line)) > 0) { 
          outcome <- "Failure"
        }
        
        if (length(grep("R Script was aborted", line)) > 0) { 
          outcome <- "Aborted"
        }
        
        if (!is.null(outcome)) {
        
          # get the task name           
          atts <- strsplit(line, ":", fixed = TRUE)[[1]]
          task <- atts[length(atts)]
          task <- sub("^\\s+|\\s+$", "", task)
          
          # get timestamp 
          atts <- strsplit(line, ": ", fixed = TRUE)[[1]]
          timestamp <- atts[1]
          
          if (grepl("UTC", line)) {
            timestamp <- gsub("UTC ","", timestamp)
            timestamp <- strptime(timestamp, "%a %b %d %H:%M:%S %Y", tz = "UTC") 
          }
          else {
            if (grepl("PDT", line)) {
              timestamp <- gsub("PDT ","", timestamp)
            }else {
              timestamp <- gsub("PST ","", timestamp)
            }
            
            timestamp <- strptime(timestamp, "%a %b %d %H:%M:%S %Y", tz = "PST8PDT") 
          }

          events <- rbind(events, data.frame(TaskName = c(task), CompletionTime = c(as.character(timestamp)), Outcome = c(outcome), timestamp = c(timestamp)))   
        } 
      }
    }
  }  

  # sort by event time 
  if (nrow(events) > 0) {
    events <- events[order(events$timestamp, decreasing = TRUE), ]
  }
  
  events$timestamp <- NULL
  return (events) 
}

