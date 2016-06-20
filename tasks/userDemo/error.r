warning("This is a warning!")

tryCatch({ 
  stop("This is an error!")
}, error = function(cond) {
  message("Caught the error.")
})   

stop("This is an error!")
print("Reached end of script!")
