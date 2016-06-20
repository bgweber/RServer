str(mtcars)

print("Sleeping for 20 seconds")
Sys.sleep(15)


print("Saving RData file")
dir.create("C:/wamp/www/RServer/reports/mtcars")
save(mtcars, file = "C:/wamp/www/RServer/reports/mtcars/mtcars.RData")


fit <- lm(mpg~am + wt + hp, data = mtcars) 
summary(fit)


print("Saving Model")
Sys.sleep(10)
save(fit, file = "C:/wamp/www/RServer/reports/mtcars/Model.RData")
