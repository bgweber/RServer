# Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.
library(shiny)
library(dygraphs) 
library(DT) 
library(RCurl) 
library(xts) 

logDir <- "C:\\RServerGit\\bin\\"

getLogs <- function(server) { 
  timestamp <- Sys.time()
  
  times <- c()
  logs <- c()
  results <- c() 
  
  for (i in -3:0) {   
    tryCatch({
      
      url <- paste0("http://", server, "/RServer/logs/log_", Sys.Date() + i) 
      url <- gsub("-0", "-", url) 
      
      res <- readLines(url)
      for (line in res) {
        outcome <- "Update" 
        
        if (length(grep("R Script completed successfully", line)) > 0) {
          outcome <- "Success"
        }
        else if (length(grep("R Script failed", line)) > 0 || length(grep("Unable to run R Script", line)) > 0) { 
          outcome <- "Failure"
        }
        else if (length(grep("R Script was aborted", line)) > 0) { 
          outcome <- "Aborted"
        }
        
        time <- strsplit(line, ": ", fixed = T)[[1]][1]
        log <- substring(line, nchar(time) + 3)
        
        times <- c(time, times)
        logs <- c(log, logs)
        results <- c(outcome, results)
      } 
    }, error = function(cond) {}, warning = function(cond) {} )  
  } 
  
  print(paste("Log load time:", Sys.time() - timestamp)) 
  return (data.frame(Time = times, Event = logs, Result = results))
}


shinyServer(function(input, output, session) {  
  
  output$serverStatus <- renderDataTable({
    input$reloadPerf
    #    invalidateLater(5000, session)  
    
    print("loading")
    
#    files <- list.files(path = logDir)
    
    servers <- c()
    status <- c()
    bootTimes <- c()
    cpu <- c()
    threads <- c()
    freeMem <- c()
    updateTime <- c()
    
    
 #   for (file in files) {
#      if (length(grep(".csv", file)) > 0) { 

        path <- "http://localhost/RServer/perf/ServerLoad.csv"
        data <- read.csv(file = path) 
        
#        path <- paste0(logDir, file)
#        data <- read.csv(file = paste0(logDir, file)) 
        
        if (nrow(data) > 0) {
          
          servers <- c(servers, as.character(data$ServerName[1])) 
          threads <- c(threads, data$Threads[nrow(data)]) 
          cpu <- c(cpu, round(data$CpuLoad[nrow(data)]*100.0, 1))
          freeMem <- c(freeMem, round(data$TotalMeb.GB.[nrow(data)] - data$UsedMem.GB.[nrow(data)], 2))
          updateTime <- c(updateTime, (data$HtmlUpdateTime[nrow(data)] + data$LogUpdateTime[nrow(data)])) 
          
          bootTime <- paste(as.character(max(as.POSIXct(data$BootTime/1000, origin="1970-01-01", tz="America/Los_Angeles"))), "PT")
          bootTimes <- c(bootTimes, bootTime)
          
          lastUpdate <- max(as.POSIXlt(data$UpdateTime/1000, origin="1970-01-01", tz="GMT")) 
          
          if (as.numeric(Sys.time() - lastUpdate, units = "secs") < 20) {
            status <- c(status, "Online")
          } else {
            status <- c(status, "Offline")
          }
          
        }
#      }   
#    }         
    
    data <- data.frame(Server = servers, Status = status, Launched = bootTimes, Threads = threads, FreeMemGB = freeMem, updateTime = updateTime)
    
    dt <- datatable(data, rownames = F, options = list(dom = 't', columnDefs = list(list(
      targets = 0, 
      render = JS( 
        "function(data, type, row, meta) {",
        "return '<a href=\"http://' + data + '/RServer\">' + data + '</a>';",
        "}")))),
      colnames = c("Server", "Status", "Launched", "Threads", "Free Memory (GB)", "Refresh Time (ms)")) %>%   
      formatStyle('Status', color = styleEqual(c('Online', 'Offline'), c('green', 'red')), fontWeight = 'bold')    
    
    return (dt)
  })    
  
  
  output$cpuLoad <- renderDygraph({     
    input$reloadPerf 
    invalidateLater(5000, session)

    tryCatch({
      dates <- NULL
      combined <- NULL 

      path <- "http://localhost/RServer/perf/ServerLoad.csv"
          
      tryCatch({
        data <- read.csv(file = path)
      }, error = function(cond) {
        Sys.sleep(0.1)
        data <- read.csv(file = path)
      })
        
      # check for recent data 
      newDates <- as.POSIXlt(data$UpdateTime/1000, origin="1970-01-01", tz="GMT")  
      if (as.numeric(Sys.time() - max(newDates), units = "secs") < 20)  {    
        
        cpu <- data$CpuLoad*100.0
        if (is.null(dates)) {
          dates <- newDates  
        }  
        
        timeData <- xts(cpu, order.by = dates)
        colnames(timeData) <- "localhost"
        
        if (is.null(combined)) {
          combined <- timeData 
        }
        else {
          combined <- merge.xts(combined, timeData) 
        } 
      }

    dygraph(combined) %>%
        dyLegend(labelsSeparateLines = T, width = 170, show = "always") %>%       
        dyAxis("y", label = "CPU Load (%)", valueRange = c(0, 100.2))       
    }, error = function(cond) {
      message(cond)
      return (NULL)
    })
  })
  
  output$memUsage <- renderDygraph({     
    input$reloadPerf
    invalidateLater(5000, session) 
    
    tryCatch({
      
      dates <- NULL
      combined <- NULL 
      maxY <- 0

      path <- "http://localhost/RServer/perf/ServerLoad.csv"
      
      tryCatch({
        data <- read.csv(file = path)
      }, error = function(cond) {
        Sys.sleep(0.1)
        data <- read.csv(file = path)
      })
      
      # check for recent data 
      newDates <- as.POSIXlt(data$UpdateTime/1000, origin="1970-01-01", tz="GMT")  
      if (as.numeric(Sys.time() - max(newDates), units = "secs") < 20) {   
        
        if (is.null(dates)) {
          dates <- newDates 
        } 
        
        mem <- data$UsedMem.GB.
        
        timeData <- xts(mem, order.by = dates)
        colnames(timeData) <- "localhost"
        maxY <- max(maxY, max(data$UsedMem.GB.))
        
        if (is.null(combined)) {
          combined <- timeData 
        }
        else {
          combined <- merge.xts(combined, timeData) 
        }
      }

      dygraph(combined) %>%  
        dyLegend(labelsSeparateLines = T, width = 170, show = "always") %>%        
        dyAxis("y", label = "Memory Used (GB)", valueRange = c(0, 1.2*maxY))        
    }, error = function(cond) {
      message(cond) 
      return (NULL)
    }) 
  })  
  
  output$SelectServer <- renderUI({ 
    input$reloadLogs  
    
    currentSelection <- "localhost" 
    servers <- c(currentSelection) 
    
    selectInput("server", "Choose Server", servers, selected = currentSelection)     
  }) 
  
  
  output$devLog <- renderDataTable({
    input$reloadLogs 
    
    if (is.null(input$server)) { 
      return (NULL) 
    } 
    
    print("Getting Logs")
    print(input$server) 
    
    data <- getLogs(input$server)   
    datatable(data, rownames = F) %>% 
      formatStyle('Result', color = styleEqual(c('Aborted', 'Failure', 'Success'), c('red', 'red', 'green')), fontWeight = 'bold')   
  })    
  
})
