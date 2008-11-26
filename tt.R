# efg, 15 April 2005
# Stowers Institute for Medical Research

# Make example reproducible
set.seed(19)

period <- 120

FullList <- 1:120
x <- FullList

# "randomly" make 15 of the points "missing"
MissingList <- sample(x,15)
x[MissingList] <- NA

# Create sine curve with noise
y <- sin(2*pi*x/period) + runif(length(x),-1,1)

# Plot points on noisy curve
plot(x,y, main="Sine Curve + 'Uniform' Noise")
mtext("Using loess smoothed fit to impute missing values")

spanlist <- c(0.50, 1.00, 2.00)
for (i in 1:length(spanlist))
{
  y.loess <- loess(y ~ x, span=spanlist[i], data.frame(x=x, y=y))
  y.predict <- predict(y.loess, data.frame(x=FullList))

  # Plot the loess smoothed curve showing gaps for missing data
  lines(x,y.predict,col=i)

  # Show imputed points to fill in gaps
  y.Missing <-  predict(y.loess, data.frame(x=MissingList))
  points(MissingList, y.Missing, pch=FILLED.CIRCLE<-19, col=i)
}

legend (0,-0.8,
        c(paste("span=", formatC(spanlist, digits=2, format="f"))),
        lty=SOLID<-1, col=1:length(spanlist), bty="n")

