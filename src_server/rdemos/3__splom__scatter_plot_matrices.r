# R Graph Gallery  http://addictedtor.free.fr/graphiques/
layout(matrix(1:1))
#--------------------------------------------------------
require(lattice) 
data(iris) 
print(splom(~iris[1:3]|Species, data = iris, 
      layout=c(2,2), pscales = 0, 
      varnames = c("Sepal\nLength", "Sepal\nWidth", "Petal\nLength"),
      page = function(...) { 
          ltext(x = seq(.6, .8, len = 4), 
                y = seq(.9, .6, len = 4), 
                lab = c("Three", "Varieties", "of", "Iris"),
                cex = 2)
      }))
      
#--------------------------------------------------------
layout(matrix(1:1))      
      