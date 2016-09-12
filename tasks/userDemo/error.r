# Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.
warning("This is a warning!")

tryCatch({ 
  stop("This is an error!")
}, error = function(cond) {
  message("Caught the error.")
})   

stop("This is an error!")
print("Reached end of script!")
