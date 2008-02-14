
## class and functions to capture screen output
setClass( "SinkOutput",
         representation( stdout = "character", stderr = "character" ))

## Real functions assigned by .sinkScreenConfigure
sinkSetup <- function() {}
sinkRetrieve <- function() {}

.sinkScreenConfigure <- function() {    # called during .onLoad
  ## persistent variables
  .options <- .stderr <- .stdout <- NULL
  ## store state & setup sinks
  sinkSetup <<- function() {
    .options <<- options()
    .stderr <<- file()
    .stdout <<- file()
    sink( .stdout )
    sink( .stderr, type = "message" )
  }
  ## close & retrieve sinks, restore state, return
  sinkRetrieve <<- function() {
    sink(); flush( .stdout )
    sink(type = "message" ); flush( .stderr )
    result <- new( "SinkOutput", stdout = readLines( .stdout ), stderr = readLines( .stderr ))
    close( .stdout ); close( .stderr );
    options( .options )
    result
  }
}
