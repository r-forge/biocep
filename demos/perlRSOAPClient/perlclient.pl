#!/usr/bin/perl

use SOAP::Lite;
use SOAP::WSDL;
use strict;
use warnings;

my $service = SOAP::Lite -> service('http://127.0.0.1:8080/rws/rGlobalEnvFunction?WSDL');
 
my $session = $service->logOn(SOAP::Data->name("arg0" => ""),SOAP::Data->name("arg1" => "guest"), SOAP::Data->name("arg2" => "guest") 
, SOAP::Data->name("arg3" => "nopool=false"), SOAP::Data->name("arg3" => "poolname=R") ) ;
 
$service->consoleSubmit(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "x='test'"));
print "Status: ", $service->getStatus(SOAP::Data->name("arg0" => $session)), "\n";
$service->consoleSubmit(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "setwd('C:/wtest')"));
print "Status: ", $service->getStatus(SOAP::Data->name("arg0" => $session)), "\n";
$service->consoleSubmit(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "save(x,file='tata')"));
print "Status: ", $service->getStatus(SOAP::Data->name("arg0" => $session)), "\n";



my $p=$service->callAndConvert(SOAP::Data->name("arg0" => $session), SOAP::Data->name("arg1" => "paste") , SOAP::Data->name("arg2" => "aaaa"), SOAP::Data->name("arg2" => "bbb") );
   
print "paste result: ", $p;

$service->logOff(SOAP::Data->name("arg0" => $session));
