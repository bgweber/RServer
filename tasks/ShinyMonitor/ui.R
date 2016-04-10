
library(shiny)
library(shinythemes)
library(dygraphs) 
library(DT) 
library(RCurl)  
library(xts) 


page1 <- fluidPage(
  theme = shinytheme("cerulean"), 
  column(8, offset = 2,
  actionButton("reloadPerf", label = "Refresh Server List"),
  titlePanel("Server Status"),
  fluidRow(
    column(10, dataTableOutput(outputId="serverStatus")) 
  ), 
  titlePanel("CPU Load"),
  dygraphOutput("cpuLoad", height = 350),
  titlePanel("Memory Usage"), 
  dygraphOutput("memUsage", height = 350) 
  ,p() 
  )
)

page2 <- fluidPage( 
  theme = shinytheme("cerulean"),  
  column(8, offset = 2,  
         fluidRow(
           column(5, uiOutput("SelectServer")),
           column(7, actionButton("reloadLogs", label = "Refresh Logs"))), 
         tabPanel("Server Logs", dataTableOutput(outputId="devLog")) 
  ) 
)  

shinyUI(  
  navbarPage("R Server Monitor",
  tabPanel("Server Status", page1),
  tabPanel("Server Logs", page2)
))

