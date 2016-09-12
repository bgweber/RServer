# Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.
path <- "/var/www/html/RServer/logs"

if (Sys.info()['sysname'] == "Windows") { 
  path <- "C:/wamp/www/RServer/logs"
}

libraries <- c("ggplot2", "reshape2")
for (lib in libraries) {
  if (lib %in% rownames(installed.packages()) == FALSE) {
    install.packages(lib, repos='http://cran.us.r-project.org') 
  }  
}  


plotServerEvents <- function() {
   
  dailyEvents <- data.frame() 
  for (file in list.files(path, full.names = TRUE)) { 
    date <- as.Date(strsplit(file, "_", fixed = TRUE)[[1]][2]) 
    daysAgo <- as.numeric(Sys.Date() - date, units = "days")
    
    if (daysAgo <= 28) { 
      scripts <- 0
      errors <- 0
      adhoc <- 0
      res <- readLines(file)
      
      for (line in res) { 
        if (length(grep("Running R", line)) > 0) { 
          scripts <- scripts + 1    
        }
        
        if (length(grep("ERROR", line)) > 0) { 
          errors <- errors + 1    
        }
        
        if (length(grep("request for ad-hoc", line)) > 0) { 
          adhoc <- adhoc + 1    
        }
      }

      dailyEvents <- rbind(dailyEvents, data.frame(dates = c(date), type = c("Errors"), values = c(errors)))
      dailyEvents <- rbind(dailyEvents, data.frame(dates = c(date), type = c("Ad-hoc Requests"), values = c(adhoc)))
      dailyEvents <- rbind(dailyEvents, data.frame(dates = c(date), type = c("Scripts Ran"), values = c(scripts)))
    }
  }

  library(ggplot2)
  ggplot(dailyEvents, aes(x=dates, y=values, col = type)) + geom_line() + geom_point() +
    scale_colour_discrete(name  ="Event Type") + 
    labs(x = "Date", y = "Daily Events") + 
    ggtitle("Daily Server Events")  
} 


loadTaskData <- function() {
  events <- data.frame()  
  
  for (file in list.files(path, full.names = TRUE)) { 
    date <- as.Date(strsplit(file, "_", fixed = TRUE)[[1]][2]) 
    daysAgo <- as.numeric(Sys.Date() - date, units = "days")
    
    if (daysAgo <= 7) { 
      res <- readLines(file)
      
      for (line in res) { 
        
        # executed tasks 
        if (length(grep("R Script completed successfully", line)) > 0) {
          
          atts <- strsplit(line, ":", fixed = TRUE)[[1]]
          task <- atts[length(atts)]
          task <- sub("^\\s+|\\s+$", "", task)
          
          if (nrow(events[events$task == task, ]) == 0) {
            events <- rbind(events, data.frame(task = c(task), runs = c(1), errors = c(0)))  
          }
          else {
            events$runs <- events$runs + ifelse(events$task == task, 1, 0)  
          }
        } 
        
        # failed tasks 
        if (length(grep("R Script failed", line)) > 0 || length(grep("Unable to run R Script", line)) > 0) { 
          
          atts <- strsplit(line, ":", fixed = TRUE)[[1]]
          task <- atts[length(atts)] 
          task <- sub("^\\s+|\\s+$", "", task)
          
          if (nrow(events[events$task == task, ]) == 0) {  
            events <- rbind(events, data.frame(task = c(task), runs = c(0), errors = c(1))) 
          }
          else {
            events$errors <- events$errors + ifelse(events$task == task, 1, 0)    
          }
        }
        
      }
    }
  }  
  
  events$completed <- ifelse(events$runs > events$errors, events$runs - events$errors, 0)
  events$runs <- NULL
  events$task <- factor(events$task, levels = rev(levels(events$task)))
  
  return (events) 
}

plotTasks <- function(events) {

  library(reshape2)
  df <- melt(events,id.vars = "task")
  df$task <- factor(df$task, levels = rev(levels(df$task)))

  ggplot(df, aes(x=task, y=value, fill=variable )) +
    scale_fill_discrete(name="Event Type") + 
    labs(x = "Task Name", y = "Events") + 
    geom_bar(stat="identity") + 
    coord_flip() + 
    ggtitle("Server Task Events")  
}  
 
showTaskErrors <- function(events) {
  events[events$errors > 0, ]
}


listRecentErrors <- function() {

  messages <- data.frame()  

  for (file in list.files(path, full.names = TRUE)) { 
    date <- as.Date(strsplit(file, "_", fixed = TRUE)[[1]][2]) 
    daysAgo <- as.numeric(Sys.Date() - date, units = "days")
    
    if (daysAgo <= 7) {  
      res <- readLines(file)
    
      for (line in res) {
        
        if (length(grep(": ERROR:", line)) > 0) {
          timestamp <- strsplit(line, ": ERROR:", fixed = TRUE)[[1]][1] 
          
          if (grepl("PDT", line)) {
            timestamp <- gsub("PDT ","", timestamp)
          }else {
            timestamp <- gsub("PST ","", timestamp)
          }
          
          timestamp <- strptime(timestamp, "%a %b %d %H:%M:%S %Y", tz = "PST8PDT") 

          message <- strsplit(line, ": ERROR: ", fixed = TRUE)[[1]][2] 
          messages <- rbind(messages, data.frame(time = timestamp, error = message, stringsAsFactors = FALSE))         
        }          
      }
    }
  }
  
  # sort by message time 
  messages <- messages[order(messages$time, decreasing = TRUE), ]

  cat("Time                        \tMessage\n")
  for (i in 1:10) {
    if (i > nrow(messages)) {
      break;      
    }

    cat(format(messages[i, "time"], "%b %d %X %Y %Z"))
    cat(":  \t" )
    cat(messages[i, "error"])
    cat("\n")
  }
} 


