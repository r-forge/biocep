########################################
# Authors : Nianhua Li, MT Morgan
# License : caBIG 
########################################

## create the java test function for the mapping of a rType
getDataTest_JavaPart <- function(lookup, addonType, 
                            mainPkg, testClass, propName, renvName) {
    funcPart <- lapply( addonType, function(rType) {
                jMap <- get(rType, lookup)
                if (identical(jMap@jPackage, ""))
                        fullJType <- jMap@jType
                else   
                        fullJType <- paste(jMap@jPackage, ".", jMap@jType, sep="")
                if(length(jMap@r2j)>0) {
                    r2jtest <- paste(
                        "\n\t/**\n",
                        "\t * data transfer from R ", rType, " to Java\n",
                        "\t * ", fullJType, "\n",
                        "\t */\n",
                        "\t@Ignore(\"please initialize data\")\n",
                        "\t@Test\n",
                        "\tpublic void TestRTo", jMap@jType, "() throws Exception {\n",
                        "\t\t", fullJType, " outputVal = null;\n",
                        "\t\toutputVal = ", getConstructorPart(fullJType), ";\n",
                        "\t\tString rScript = getClass().getResource(\"R/", 
                        rType, "Data.R\").getFile();\n",
                        "\t\tString rVariable = \"", rType, "Data\";\n",
                        "\t\tassertEquals(outputVal, binding.mockR2Java(rScript, rVariable));\n",
                        "\t}\n", 
                        sep="", collapse="")
                } else {
                    r2jtest <- ""
                }
                if (length(jMap@j2r)>0) {
                     j2rtest <- paste(
                        "\n\t/**\n",
                        "\t * data transfer to R ", rType, " from Java\n",
                        "\t * ", fullJType, "\n",
                        "\t */\n",
                        "\t@Ignore(\"please initialize data\")\n",
                        "\t@Test\n",
                        "\tpublic void Test", jMap@jType, "ToR() throws Exception {\n",
                        "\t\t", fullJType, " inputVal = null;\n", 
                        "\t\tinputVal = ", getConstructorPart(fullJType), ";\n",
                        "\t\tString rScript = getClass().getResource(\"R/", 
                        rType, "Data.R\").getFile();\n",
                        "\t\tString rVariable = \"", rType, "Data\";\n",
                        "\t\tassertTrue(binding.mockJava2R(inputVal, rScript, rVariable));\n",
                        "\t}\n", 
                        sep="", collapse="")
                } else {
                    j2rtest <- ""
                }
                paste(r2jtest, j2rtest, sep="", collapse="")
    })
    
    mainServ <- "MockService"                    
    myTest <- paste("\npackage ", mainPkg, ";\n",
        "import junit.framework.JUnit4TestAdapter;\n",
        "import org.junit.BeforeClass;\n",
        "import org.junit.Test;\n",
        "import org.junit.Ignore;\n",
        "import static org.junit.Assert.*;\n",
        "import java.rmi.RemoteException;\n",  
        "import org.kchine.r.*;\n",
        "\n\npublic class ", testClass, " {\n",
        "\tprivate static ", mainServ, " binding;\n",
        "\n\t/**\n",
        "\t * Used for backward compatibility (IDEs, Ant and JUnit 3.x text runner)\n",
        "\t */\n",
        "\tpublic static junit.framework.Test suite() {\n",
        "\t\t return new JUnit4TestAdapter(", testClass, ".class);\n",
        "\t}\n",
        "\n\t/**\n",
        "\t * Sets up the test fixture; Called before all the tests; Called only once.\n",
        "\t */\n", 
        "\t@BeforeClass\n",       
        "\tpublic static void oneTimeSetUp() throws Exception {\n",
        "\t\t", propName, " prop = new ", propName, "();\n",
        "\t\t", renvName, " e = new ", renvName, "(prop);\n",
        "\t\tbinding=new ", mainServ, "(e);\n",
        "\t}\n",
        "\n\t/**\n",
        "\t * we put this test here to let ", testClass, " work right out of box.\n",
        "\t */\n", 
        "\t@Test\n",       
        "\tpublic void test", mainServ, "Service() {\n",
        "\t\tassertNotNull(binding);\n",
        "\t}\n",
        paste(funcPart, sep="", collapse=""),
        "}\n", sep="", collapse="")
    myTest
}

getDataTest_DataCreater <- function(mainPkg, dataCreaterClass, propName, renvName)
{
    myCreater <- paste("\npackage ", mainPkg, ";\n",
        "import org.kchine.r.TestDataCreater;\n",
        "\n\npublic class ", dataCreaterClass, " {\n",
	"\tpublic static void main(String[] args) throws Exception {\n",
        "\t\t", propName, " prop = new ", propName, "();\n",
        "\t\t", renvName, " e = new ", renvName, "(prop);\n",
	"\t\tTestDataCreater tdc = new TestDataCreater(e);\n",
	"\t\tString action = args[0];\n",
	"\t\tString rdataDir = args[1];\n",
	"\t\tif (action.equals(\"load\")) {\n",
	"\t\t\ttdc.loadRDataToJavaData(rdataDir);\n",
	"\t\t} else if (action.equals(\"data\")) {\n",
	"\t\t\tString rdataSet = args[2];\n",
	"\t\t\ttdc.dataRDataToJavaData(rdataSet, rdataDir);\n",
	"\t\t} else {\n",
	"\t\t\tSystem.out.println(\"Invalid action \"+action+\", action should be either 'data' or 'load'.\");\n",
	"\t\t}\n",
	"\t}\n",
	"}\n", sep="", collapse="")
    myCreater
}

getDataTest_RPart <- function(addonType) {
    rScripts <- paste(
                    addonType, "Data <- try(new(\"", addonType, "\"), silent=TRUE)\n", 
                    "if(is(", addonType, "Data, \"try-error\"))\n",
                    "\t", addonType, "Data <- NULL\n", sep="")
    names(rScripts) <- paste(addonType, "Data.R", sep="")
    rScripts
}


## generate java test function template and ant script to run the test function
## write them to files
## param:
##      jTypeInfoByPkg: 
##              A list, each element is an instance of S4 class "RJavaPkgFunction".
##              Each element (e) represent the java mapping of functions in a R
##              package. e@rPackage is the R package name, e@javaClass is the 
##              full name (with package name in front) of the java class which
##              contains all java mapping functions of the functions in the R 
##              package, e@javaTypeInfo is the function siguatures of those java
##              mapping functions, it is a list of "RJavaSignature" class instances.
##      mainServ: class name of the main java API 
## return: NULL
setGeneric("generateFuncTest",
           function(deployMode, ...) standardGeneric("generateFuncTest"))

generateFunctionPart <- function(JtypeSig, calleeName, mainServ) {
    initializer <- function(args, argLabels, argNames) {
      paste("\t\t// initialize ", argLabels, " here.\n",
            "\t\t", args, " ", argNames, " = null;\n", 
            "\t\t// ", argNames, " = new ", args,
            ifelse(regexpr("\\[\\]$", args)>0, " {}", "()"), ";\n",
            "\n", sep="", collapse="")
    }
    if (length(JtypeSig@args)>0) {
        variableNames <- paste(calleeName, names(JtypeSig@args), sep="_")
        initializePart <- initializer(JtypeSig@args, names(JtypeSig@args), variableNames)
        paramPart <- paste(variableNames, sep="", collapse=", ")
    } else {
        initializePart <- character(0)
        paramPart <- character(0)
    }
    expectationName <- paste(calleeName, "ans", sep="_")
    expectationPart <- initializer(JtypeSig@returnType, "expected result", expectationName)
    paste("\n\t/**\n",
          "\t * Tests ", mainServ, ".", calleeName, "\n",
          "\t */\n",
          "\t@Ignore(\"please initialize function parameters\")\n",
          "\t@Test\n",
          "\tpublic void Test", capName(calleeName), "() throws RemoteException {\n",
          initializePart,
          expectationPart,
          "\t\tassertEquals(", expectationName, ", binding.",
          calleeName, "(", 
          paramPart,
          "));\n",
          "\t}\n", 
          sep="", collapse="")
}


setMethod("generateFuncTest",
          signature(deployMode="demo"),
          function(deployMode, jTypeInfoByPkg, mainServ, verbose) {
              if (verbose)
                cat("****    generate java client for function test  *****\n")
    mainPkg <- ServToServPkg(mainServ, deployMode)
    ## create a TestCase
    testClass <- paste(mainServ, "Test", sep="")
    
    funcPart <- lapply(jTypeInfoByPkg, function(pkgFuncList) {
        rPkg <- pkgFuncList@rPackage
        paste(lapply(pkgFuncList@javaTypeInfo,
                     generateFunctionPart,
                     calleeName=getApiFuncName(rPkg, JtypeSig),
                     mainServ=mainServ),
              sep="", collapse="")
    })
    myTest <- paste("\npackage ", mainPkg, ";\n",
        "import junit.framework.JUnit4TestAdapter;\n",
        "import org.junit.BeforeClass;\n",
        "import org.junit.Test;\n",
        "import org.junit.Ignore;\n",
        "import static org.junit.Assert.*;\n",
        "import java.rmi.RemoteException;\n",
        "\n\npublic class ", testClass, " {\n",
        "\tprivate static ", mainServ, " binding;\n",
        "\n\t/**\n",
        "\t * Used for backward compatibility (IDEs, Ant and JUnit 3.x text runner)\n",
        "\t */\n",
        "\tpublic static junit.framework.Test suite() {\n",
        "\t\t return new JUnit4TestAdapter(", testClass, ".class);\n",
        "\t}\n",
        "\n\t/**\n",
        "\t * Sets up the test fixture; Called before all the tests; Called only once.\n",
        "\t */\n", 
        "\t@BeforeClass\n",       
        "\tpublic static void oneTimeSetUp() throws Exception {\n",
        "\t\tbinding=new ", mainServ, "();\n",
        "\t}\n",
        "\n\t/**\n",
        "\t * we put this test here to let ", testClass, " work right out of box.\n",
        "\t */\n", 
        "\t@Test\n",       
        "\tpublic void test", mainServ, "Service() {\n",
        "\t\tassertNotNull(binding);\n",
        "\t}\n",
        paste(funcPart, sep="", collapse=""),
        "}\n", sep="", collapse="")
   
    testPkg <- file.path("..", "test", "src", gsub(".", .Platform$file.sep, mainPkg, fixed=TRUE)) 
    if (!file.exists(testPkg))
        dir.create(testPkg, showWarnings=FALSE, recursive=TRUE)
    outputFile1 <- file.path(testPkg, paste(testClass, ".java", sep="", collapse=""))
    cat(myTest, file=outputFile1) 
    testDataPkg <-file.path(testPkg, "Data")
    if (!file.exists(testDataPkg))
        dir.create(testDataPkg, showWarnings=FALSE, recursive=TRUE)
    readme <- paste("If you need data to test the functions in ", mainServ, 
		", please put the data files in this folder.\n", sep="", collapse="")
    cat(readme, file=file.path(testDataPkg, "README"))
    return()
})    

setMethod("generateFuncTest", signature(deployMode="jms"),
        function(deployMode, jTypeInfo, mainServ, verbose) {
            if (verbose)
              cat("****    generate java client for function test  *****\n")
    mainPkg <- ServToServPkg(mainServ, deployMode)                                                        
    ## create a TestCase
    testClass <- paste(mainServ, "Test", sep="")
    funcCallList <- lapply(jTypeInfo,
                           function(JTypeSig)
                           generateFunctionPart(JTypeSig, JTypeSig@funcName, mainServ))
    funcPart <- paste(unlist(funcCallList), sep="", collapse="")
    myTest <- paste("\npackage ", mainPkg, ";\n",
        "import junit.framework.JUnit4TestAdapter;\n",
        "import org.junit.BeforeClass;\n",
        "import org.junit.Test;\n",
        "import org.junit.Ignore;\n",
        "import static org.junit.Assert.*;\n",
        "import java.rmi.RemoteException;\n",
        "\n\npublic class ", testClass, " {\n",
        "\tprivate static ", mainServ, " binding;\n",
        "\n\t/**\n",
        "\t * Used for backward compatibility (IDEs, Ant and JUnit 3.x text runner)\n",
        "\t */\n",
        "\tpublic static junit.framework.Test suite() {\n",
        "\t\t return new JUnit4TestAdapter(", testClass, ".class);\n",
        "\t}\n",
        "\n\t/**\n",
        "\t * Sets up the test fixture; Called before all the tests; Called only once.\n",
        "\t */\n", 
        "\t@BeforeClass\n",       
        "\tpublic static void oneTimeSetUp() throws Exception {\n",
        "\t\tbinding=new ", mainServ, "();\n",
        "\t}\n",
        "\n\t/**\n",
        "\t * we put this test here to let ", testClass, " work right out of box.\n",
        "\t */\n", 
        "\t@Test\n",       
        "\tpublic void test", mainServ, "Service() {\n",
        "\t\tassertNotNull(binding);\n",
        "\t}\n",
        paste(funcPart, sep="", collapse=""),
        "}\n", sep="", collapse="")

    testPkg <- file.path("..", "test", "src", gsub(".", .Platform$file.sep, mainPkg, fixed=TRUE))  
    outputFile=file.path(testPkg, paste(testClass, ".java", sep="", collapse=""))
    if (!file.exists(outputFile))
        dir.create(testPkg, showWarnings=FALSE, recursive=TRUE)
    cat(myTest, file=outputFile)  
    return()
} )   
